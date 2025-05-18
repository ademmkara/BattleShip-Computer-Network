package battleshipserver;

import game.Message;
import static game.Message.Message_Type.Selected;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SClient {

    int id;
    public String name = "isimYok";
    public String displayName = "isimYok";
    Socket soket;
    ObjectOutputStream sOutput;
    ObjectInputStream sInput;

    //clientten gelenleri dinleme threadi
    Listen listenThread;
    //cilent eşleştirme thredi
    PairingThread pairThread;
    //rakip client
    SClient rakip;
    //eşleşme durumu
    private List<Ship> ships;

    public boolean paired = false;
    public int hp = 17;
    public boolean restartRequest = false;
    public boolean isReady = false;

    public SClient(Socket gelenSoket, int id) {
        this.soket = gelenSoket;
        this.id = id;
        this.name = "NoName";
        this.displayName = "NoName";
        this.paired = false;
        this.rakip = null;
        this.hp = 17;
        this.restartRequest = false;
        this.ships = new ArrayList<>();
        try {
            this.sOutput = new ObjectOutputStream(this.soket.getOutputStream());
            this.sInput = new ObjectInputStream(this.soket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        //thread nesneleri
        this.listenThread = new Listen(this);
        this.pairThread = new PairingThread(this);

    }

    //client mesaj gönderme
    public void Send(Message message) {
        try {
            this.sOutput.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //client dinleme threadi
    //her clientin ayrı bir dinleme thredi var
    class Listen extends Thread {

        SClient Client;

        //thread nesne alması için yapıcı metod
        Listen(SClient Client) {
            this.Client = Client;
        }

        public void run() {
            //client bağlı olduğu sürece dönsün
            try {
                while (Client.soket.isConnected()) {

                    //mesajı bekleyen kod satırı
                    Message received = (Message) (Client.sInput.readObject());
                    //mesaj gelirse bu satıra geçer
                    //mesaj tipine göre işlemlere ayır
                    switch (received.type) {
                        case Name:
                            String originalName = received.content.toString();
                            Client.displayName = originalName;

                            String uniqueName = originalName;
                            int suffix = 1;

                            for (SClient existing : Server.Clients) {
                                if (existing.name.equals(uniqueName)) {
                                    uniqueName = originalName + "_" + suffix;
                                    suffix++;
                                }
                            }

                            Client.name = uniqueName;

                            Message nameMsg = new Message(Message.Message_Type.Name);
                            nameMsg.content = uniqueName;
                            Server.Send(Client, nameMsg);

                            Client.pairThread.start();
                            break;

                        case Disconnect:
                            
    try {
                            Client.sInput.close();
                            Client.sOutput.close();
                            Client.soket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Server.Clients.remove(Client);
                        break;

                        case Text:
                            //gelen metni direkt rakibe gönder
                            Server.Send(Client.rakip, received);
                            break;
                        case Text2:
                            //gelen metni direkt rakibe gönder
                            Server.Send(Client.rakip, received);
                            break;
                        case Selected:
                            //gelen seçim yapıldı mesajını rakibe gönder
                            Server.Send(Client.rakip, received);
                            break;

                        case Bitis:
                            System.out.println("Oyun bitiş mesajı geldi. Kazanan: " + received.sender);

                            // Rakibe de bildir
                            Message notifyRival = new Message(Message.Message_Type.Bitis);
                            notifyRival.content = "Kaybettiniz! Kazanan: " + received.sender;
                            Server.Send(Client.rakip, notifyRival);

                            // Gönderen oyuncuya da kazanma mesajı gönder
                            Message notifyWinner = new Message(Message.Message_Type.Bitis);
                            notifyWinner.content = "Tebrikler! Oyunu kazandınız.";
                            Server.Send(Client, notifyWinner);
                            break;
                        case PairStatus:
                            // Eşleşme durumunu kontrol et
                            Message reply = new Message(Message.Message_Type.Text);
                            if (Client.paired) {
                                reply.content = "Eşleşme sağlandı! Rakip: " + Client.rakip.name + " start'a basabilirsiniz...";
                            } else {
                                reply.content = "Hala rakip bekleniyor. Rakip ismi gördüğünüzde start'a basabilirsiniz...";
                            }
                            Server.Send(Client, reply);
                            break;

                        case Ready:
                            System.out.println("[SERVER] Ready message received");

                            Client.isReady = true;

                            // Rakibe de haber ver
                            Server.Send(Client.rakip, received);

                            // Eğer her iki oyuncu da hazırsa → sırayı belirle
                            if (Client.rakip != null && Client.rakip.isReady) {
                                System.out.println("[SERVER] Her iki oyuncu hazır. Sıra belirleniyor...");

                                // Rastgele bir oyuncuya ilk hamle hakkı ver
                                boolean clientStarts = new java.util.Random().nextBoolean();

                                Message turnForThis = new Message(Message.Message_Type.Turn);
                                turnForThis.content = String.valueOf(clientStarts); // true ya da false

                                Message turnForrakip = new Message(Message.Message_Type.Turn);
                                turnForrakip.content = String.valueOf(!clientStarts);

                                Server.Send(Client, turnForThis);
                                Server.Send(Client.rakip, turnForrakip);
                            }
                            break;

                        case SHIP_INFO:
                            System.out.println("Gemi bilgileri alındı: " + received.content);
                            String[] shipData = received.content.toString().split(";");
                            Client.ships = new ArrayList<>();

                            for (String data : shipData) {
                                if (data.isEmpty()) {
                                    continue;
                                }

                                String[] parts = data.split(",");
                                int row = Integer.parseInt(parts[0]);
                                int col = Integer.parseInt(parts[1]);
                                boolean isVertical = Boolean.parseBoolean(parts[2]);
                                int length = Integer.parseInt(parts[3]); // Uzunluk bilgisini oku

                                Ship ship = new Ship("", length); // İsim önemsiz, uzunluk kritik
                                ship.setPos(row, col, isVertical);
                                Client.ships.add(ship);

                                System.out.println("Gemi oluşturuldu: " + row + "," + col
                                        + " Uzunluk:" + length
                                        + " Dikey:" + isVertical);
                            }
                            break;

                        case AttackResult:
                            String[] coords = received.content.toString().split(",");
                            int attackRow = Integer.parseInt(coords[0]);
                            int attackCol = Integer.parseInt(coords[1]);

                            boolean isHit = false;
                            for (Ship ship : Client.ships) {
                                if (ship.checkHit(attackRow, attackCol)) {
                                    isHit = true;
                                    break;
                                }
                            }

                            // Sonucu gönder
                            Message resultMsg = new Message(Message.Message_Type.AttackResult);
                            resultMsg.content = attackRow + "," + attackCol + "," + isHit;
                            Server.Send(Client.rakip, resultMsg); // Saldıran oyuncuya sonuç

                            break;

                        case Attack:
                            System.out.println("Attack mesajı alındı: " + received.content);
                            String[] coordss = received.content.toString().split(",");
                            int row = Integer.parseInt(coordss[0]);
                            int col = Integer.parseInt(coordss[1]);

                            boolean isHitt = false;

                            // Rakibin gemileri kontrol ediliyor
                            for (Ship ship : Client.rakip.ships) {
                                if (ship.checkHit(row, col)) {
                                    isHitt = true;

                                    break;
                                }
                            }
                            // Rakibin HP'sini güncelle
                            if (isHitt) {
                                Client.rakip.hp--;
                                System.out.println("Rakibin HP: " + Client.rakip.hp);

                                if (Client.rakip.hp == 0) {
                                    System.out.println(">>> Rakibin tüm gemileri vuruldu, oyun bitiyor!");

                                    Message winnerMsg = new Message(Message.Message_Type.Bitis);
                                    winnerMsg.content = "Tebrikler! Kazandınız." + Client.name;
                                    Server.Send(Client, winnerMsg);  // saldıran kazandı

                                    Message loserMsg = new Message(Message.Message_Type.Bitis);
                                    loserMsg.content = "Tüm gemileriniz vuruldu. Kaybettiniz." + Client.rakip.name;
                                    Server.Send(Client.rakip, loserMsg);
                                }
                            }

                            //Saldıran kişiye sonuç gönder (kendi enemyBoard’unu güncelleyecek)
                            Message resultToAttacker = new Message(Message.Message_Type.AttackResult);
                            resultToAttacker.content = row + "," + col + "," + isHitt;
                            resultToAttacker.sender = Client.name;
                            Server.Send(Client, resultToAttacker);

                            Message resultToDefender = new Message(Message.Message_Type.AttackResult);
                            resultToDefender.content = row + "," + col + "," + isHitt;
                            resultToDefender.sender = Client.name;
                            Server.Send(Client.rakip, resultToDefender);

                            
                            //Sırayı değiştir
                            Message newTurnForAttacker = new Message(Message.Message_Type.Turn);
                            newTurnForAttacker.content = "false";
                            Server.Send(Client, newTurnForAttacker);

                            Message newTurnForDefender = new Message(Message.Message_Type.Turn);
                            newTurnForDefender.content = "true";
                            Server.Send(Client.rakip, newTurnForDefender);

                            System.out.println("Sıra değiştirildi → " + Client.rakip.name + " şimdi oynayabilir.");
                            break;

                        case RestartRequest:
                            Client.restartRequest = true;
                            System.out.println(Client.name + " tekrar başlamak istiyor.");

                            if (Client.rakip != null) {
                                Message notifyMsg = new Message(Message.Message_Type.Text2);
                                notifyMsg.content = Client.name + " tekrar başlamak istiyor.";
                                Server.Send(Client.rakip, notifyMsg);
                            }

                            // Eğer rakip de hazırsa yeni oyunu başlat
                            if (Client.rakip != null && Client.rakip.restartRequest) {
                                System.out.println("Her iki oyuncu tekrar başlamak istiyor. Yeni oyun başlatılıyor.");
                                Message notifyMsg = new Message(Message.Message_Type.Text2);
                                notifyMsg.content = "Her iki oyuncu tekrar başlamak istiyor. Yeni oyun başlatılıyor.";
                                Server.Send(Client.rakip, notifyMsg);

                                // Reset flags
                                Client.restartRequest = false;
                                Client.rakip.restartRequest = false;
                                Client.rakip.hp = 17;
                                Client.hp = 17;

                                // Yeni eşleşme mesajı gönder
                                Message msgTo1 = new Message(Message.Message_Type.Start);
                                Server.Send(Client, msgTo1);

                                Message msgTo2 = new Message(Message.Message_Type.Start);
                                Server.Send(Client.rakip, msgTo2);
                            }
                            break;

                    }

                }
            } catch (IOException ex) {
                Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
                //client bağlantısı koparsa listeden sil
                Server.Clients.remove(Client);

            } catch (ClassNotFoundException ex) {
                Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
                //client bağlantısı koparsa listeden sil
                Server.Clients.remove(Client);
            } finally {
                try {
                    Client.sInput.close();
                    Client.sOutput.close();
                    Client.soket.close();
                } catch (IOException ex) {
                    Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
                }

                //Listeden kesin silme: aynı referans veya aynı isimde olan
                Server.Clients.removeIf(client
                        -> client == Client
                        || client.name.equals(Client.name)
                        || !client.soket.isConnected()
                );

                if (Client.rakip != null) {
                    Message infoMsg = new Message(Message.Message_Type.Text2);
                    infoMsg.content = "Rakibiniz oyunu terk etti. Yeni rakip bekleniyor. \nRakip bağlandığında isim görülecek \nİsim gördüğünüzde gemileri dizebilirsiniz.";
                    Server.Send(Client.rakip, infoMsg);

                    Message clearRivalMsg = new Message(Message.Message_Type.RivalDisconnected);
                    Server.Send(Client.rakip, clearRivalMsg);

                    Client.rakip.rakip = null;
                    Client.rakip = null;
                    Client.rakip.paired = false;

                    Client.rakip.pairThread = new PairingThread(Client.rakip);
                    Client.rakip.pairThread.start();
                }

                Server.Display("Client gitti...");

            }
        }
    }

    //eşleştirme threadi
    //her clientin ayrı bir eşleştirme thredi var
    class PairingThread extends Thread {

        SClient Client;

        PairingThread(SClient Client) {
            this.Client = Client;
        }

        public void run() {
            //client bağlı ve eşleşmemiş olduğu durumda dön
            while (Client.soket.isConnected() && Client.paired == false) {
                try {
                    //lock mekanizması
                    //sadece bir client içeri grebilir
                    //diğerleri release olana kadar bekler
                    Server.pairTwo.acquire(1);

                    //client eğer eşleşmemişse gir
                    if (!Client.paired) {
                        SClient crival = null;
                        //eşleşme sağlanana kadar dön
                        while (crival == null && Client.soket.isConnected()) {
                            //liste içerisinde eş arıyor
                            for (SClient client : Server.Clients) {
                                if (Client != client
                                        && client.rakip == null
                                        && client.soket != null
                                        && client.soket.isConnected()
                                        && !client.soket.isClosed()) {

                                    // eşleşme sağlandı
                                    crival = client;
                                    crival.paired = true;
                                    crival.rakip = Client;
                                    Client.rakip = crival;
                                    Client.paired = true;
                                    break;
                                }
                            }

                            //sürekli dönmesin 1 saniyede bir dönsün
                            //thredi uyutuyoruz
                            sleep(1000);
                        }
                        //eşleşme oldu
                        //her iki tarafada eşleşme mesajı gönder 
                        //oyunu başlat
                        Message msg1 = new Message(Message.Message_Type.RivalConnected);
                        msg1.content = Client.name;
                        Server.Send(Client.rakip, msg1);

                        Message msg2 = new Message(Message.Message_Type.RivalConnected);
                        msg2.content = Client.rakip.name;
                        Server.Send(Client, msg2);
                    }
                    //lock mekanizmasını servest bırak
                    //bırakılmazsa deadlock olur.
                    Server.pairTwo.release(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PairingThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
