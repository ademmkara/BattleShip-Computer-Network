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
    public String name = "NoName";           // sistem içi eşsiz isim
    public String displayName = "NoName";    // kullanıcıya gösterilecek isim
    Socket soket;
    ObjectOutputStream sOutput;
    ObjectInputStream sInput;

    //clientten gelenleri dinleme threadi
    Listen listenThread;
    //cilent eşleştirme thredi
    PairingThread pairThread;
    //rakip client
    SClient rival;
    //eşleşme durumu
    private List<Ship> ships;

    public boolean paired = false;
    public int hp = 2;
    public boolean restartRequest = false;

    public SClient(Socket gelenSoket, int id) {
        this.soket = gelenSoket;
        this.id = id;
        this.name = "NoName";
        this.displayName = "NoName";
        this.paired = false;
        this.rival = null;
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

        SClient TheClient;

        //thread nesne alması için yapıcı metod
        Listen(SClient TheClient) {
            this.TheClient = TheClient;
        }

        public void run() {
            //client bağlı olduğu sürece dönsün
            try {
                while (TheClient.soket.isConnected()) {

                    //mesajı bekleyen kod satırı
                    Message received = (Message) (TheClient.sInput.readObject());
                    //mesaj gelirse bu satıra geçer
                    //mesaj tipine göre işlemlere ayır
                    switch (received.type) {
                        case Name:
                            String originalName = received.content.toString();
                            TheClient.displayName = originalName;

                            String uniqueName = originalName;
                            int suffix = 1;

                            for (SClient existing : Server.Clients) {
                                if (existing.name.equals(uniqueName)) {
                                    uniqueName = originalName + "_" + suffix;
                                    suffix++;
                                }
                            }

                            TheClient.name = uniqueName;

                            Message nameMsg = new Message(Message.Message_Type.Name);
                            nameMsg.content = uniqueName;
                            Server.Send(TheClient, nameMsg);

                            TheClient.pairThread.start();
                            break;

                        case Disconnect:
                            
    try {
                            TheClient.sInput.close();
                            TheClient.sOutput.close();
                            TheClient.soket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Server.Clients.remove(TheClient);
                        break;

                        case Text:
                            //gelen metni direkt rakibe gönder
                            Server.Send(TheClient.rival, received);
                            break;
                        case Text2:
                            //gelen metni direkt rakibe gönder
                            Server.Send(TheClient.rival, received);
                            break;
                        case Selected:
                            //gelen seçim yapıldı mesajını rakibe gönder
                            Server.Send(TheClient.rival, received);
                            break;

                        case Bitis:
                            System.out.println("Oyun bitiş mesajı geldi. Kazanan: " + received.sender);

                            // Rakibe de bildir
                            Message notifyRival = new Message(Message.Message_Type.Bitis);
                            notifyRival.content = "Kaybettiniz! Kazanan: " + received.sender;
                            Server.Send(TheClient.rival, notifyRival);

                            // Gönderen oyuncuya da kazanma mesajı gönder
                            Message notifyWinner = new Message(Message.Message_Type.Bitis);
                            notifyWinner.content = "Tebrikler! Oyunu kazandınız.";
                            Server.Send(TheClient, notifyWinner);
                            break;
                        case PairStatus:
                            // Eşleşme durumunu kontrol et
                            Message reply = new Message(Message.Message_Type.Text);
                            if (TheClient.paired) {
                                reply.content = "Eşleşme sağlandı! Rakip: " + TheClient.rival.name + " start'a basabilirsiniz...";
                            } else {
                                reply.content = "Hala rakip bekleniyor. Rakip ismi gördüğünüzde start'a basabilirsiniz...";
                            }
                            Server.Send(TheClient, reply);
                            break;

                        case Ready:
                            System.out.println("[SERVER] Ready message received");
                            // Rakibe forward et → onun clientinde rivalIsReady = true olsun
                            Server.Send(TheClient.rival, received);
                            break;

                        case SHIP_INFO:
                            System.out.println("Gemi bilgileri alındı: " + received.content);
                            String[] shipData = received.content.toString().split(";");
                            TheClient.ships = new ArrayList<>();

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
                                TheClient.ships.add(ship);

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
                            for (Ship ship : TheClient.ships) {
                                if (ship.checkHit(attackRow, attackCol)) {
                                    isHit = true;
                                    break;
                                }
                            }

                            // Sonucu gönder
                            Message resultMsg = new Message(Message.Message_Type.AttackResult);
                            resultMsg.content = attackRow + "," + attackCol + "," + isHit;
                            Server.Send(TheClient.rival, resultMsg); // Saldıran oyuncuya sonuç

                            break;

                        case Attack:
                            System.out.println("Attack mesajı alındı: " + received.content);
                            String[] coordss = received.content.toString().split(",");
                            int row = Integer.parseInt(coordss[0]);
                            int col = Integer.parseInt(coordss[1]);

                            boolean isHitt = false;

                            // 🔍 Rakibin gemileri kontrol ediliyor
                            for (Ship ship : TheClient.rival.ships) {
                                if (ship.checkHit(row, col)) {
                                    isHitt = true;

                                    break;
                                }
                            }
                            // Rakibin HP'sini güncelle
                            if (isHitt) {
                                TheClient.rival.hp--;
                                System.out.println("Rakibin HP: " + TheClient.rival.hp);

                                if (TheClient.rival.hp == 0) {
                                    System.out.println(">>> Rakibin tüm gemileri vuruldu, oyun bitiyor!");

                                    Message winnerMsg = new Message(Message.Message_Type.Bitis);
                                    winnerMsg.content = "Tebrikler! Kazandınız." + TheClient.name;
                                    Server.Send(TheClient, winnerMsg);  // saldıran kazandı

                                    Message loserMsg = new Message(Message.Message_Type.Bitis);
                                    loserMsg.content = "Tüm gemileriniz vuruldu. Kaybettiniz." + TheClient.rival.name;
                                    Server.Send(TheClient.rival, loserMsg);
                                }
                            }

                            // 🎯 Saldıran kişiye sonuç gönder (kendi enemyBoard’unu güncelleyecek)
                            Message resultToAttacker = new Message(Message.Message_Type.AttackResult);
                            resultToAttacker.content = row + "," + col + "," + isHitt;
                            resultToAttacker.sender = TheClient.name;
                            Server.Send(TheClient, resultToAttacker);

                            Message resultToDefender = new Message(Message.Message_Type.AttackResult);
                            resultToDefender.content = row + "," + col + "," + isHitt;
                            resultToDefender.sender = TheClient.name;
                            Server.Send(TheClient.rival, resultToDefender);

                            System.out.println("Sonuç iki tarafa da gönderildi: " + row + "," + col + " → " + (isHitt ? "HIT" : "MISS"));
                            break;

                        case RestartRequest:
                            TheClient.restartRequest = true;
                            System.out.println(TheClient.name + " tekrar başlamak istiyor.");

                            if (TheClient.rival != null) {
                                Message notifyMsg = new Message(Message.Message_Type.Text2);
                                notifyMsg.content = TheClient.name + " tekrar başlamak istiyor.";
                                Server.Send(TheClient.rival, notifyMsg);
                            }

                            // Eğer rakip de hazırsa yeni oyunu başlat
                            if (TheClient.rival != null && TheClient.rival.restartRequest) {
                                System.out.println("Her iki oyuncu tekrar başlamak istiyor. Yeni oyun başlatılıyor.");
                                Message notifyMsg = new Message(Message.Message_Type.Text2);
                                notifyMsg.content = "Her iki oyuncu tekrar başlamak istiyor. Yeni oyun başlatılıyor.";
                                Server.Send(TheClient.rival, notifyMsg);

                                // Reset flags
                                TheClient.restartRequest = false;
                                TheClient.rival.restartRequest = false;
                                TheClient.rival.hp = 17;
                                TheClient.hp = 17;

                                // Yeni eşleşme mesajı gönder
                                Message msgTo1 = new Message(Message.Message_Type.Start);
                                Server.Send(TheClient, msgTo1);

                                Message msgTo2 = new Message(Message.Message_Type.Start);
                                Server.Send(TheClient.rival, msgTo2);
                            }
                            break;

                    }

                }
            } catch (IOException ex) {
                Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
                //client bağlantısı koparsa listeden sil
                Server.Clients.remove(TheClient);

            } catch (ClassNotFoundException ex) {
                Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
                //client bağlantısı koparsa listeden sil
                Server.Clients.remove(TheClient);
            } finally {
                try {
                    TheClient.sInput.close();
                    TheClient.sOutput.close();
                    TheClient.soket.close();
                } catch (IOException ex) {
                    Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
                }

                // 🔥 Listeden kesin silme: aynı referans veya aynı isimde olan
                Server.Clients.removeIf(client
                        -> client == TheClient
                        || client.name.equals(TheClient.name)
                        || !client.soket.isConnected()
                );

                if (TheClient.rival != null) {
                    Message infoMsg = new Message(Message.Message_Type.Text2);
                    infoMsg.content = "Rakibiniz oyunu terk etti. Yeni rakip bekleniyor.";
                    Server.Send(TheClient.rival, infoMsg);

                    Message clearRivalMsg = new Message(Message.Message_Type.RivalDisconnected);
                    Server.Send(TheClient.rival, clearRivalMsg);

                    TheClient.rival.rival = null;
                    TheClient.rival = null;
                    TheClient.rival.paired = false;

                    TheClient.rival.pairThread = new PairingThread(TheClient.rival);
                    TheClient.rival.pairThread.start();
                }

                Server.Display("Client gitti...");

            }
        }
    }

    //eşleştirme threadi
    //her clientin ayrı bir eşleştirme thredi var
    class PairingThread extends Thread {

        SClient TheClient;

        PairingThread(SClient TheClient) {
            this.TheClient = TheClient;
        }

        public void run() {
            //client bağlı ve eşleşmemiş olduğu durumda dön
            while (TheClient.soket.isConnected() && TheClient.paired == false) {
                try {
                    //lock mekanizması
                    //sadece bir client içeri grebilir
                    //diğerleri release olana kadar bekler
                    Server.pairTwo.acquire(1);

                    //client eğer eşleşmemişse gir
                    if (!TheClient.paired) {
                        SClient crival = null;
                        //eşleşme sağlanana kadar dön
                        while (crival == null && TheClient.soket.isConnected()) {
                            //liste içerisinde eş arıyor
                            for (SClient clnt : Server.Clients) {
                                if (TheClient != clnt
                                        && clnt.rival == null
                                        && clnt.soket != null
                                        && clnt.soket.isConnected()
                                        && !clnt.soket.isClosed()) {

                                    // eşleşme sağlandı
                                    crival = clnt;
                                    crival.paired = true;
                                    crival.rival = TheClient;
                                    TheClient.rival = crival;
                                    TheClient.paired = true;
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
                        msg1.content = TheClient.name;
                        Server.Send(TheClient.rival, msg1);

                        Message msg2 = new Message(Message.Message_Type.RivalConnected);
                        msg2.content = TheClient.rival.name;
                        Server.Send(TheClient, msg2);
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
