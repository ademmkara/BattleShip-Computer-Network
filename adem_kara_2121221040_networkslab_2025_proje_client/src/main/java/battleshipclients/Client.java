package battleshipclient;

import static battleshipclient.Client.sInput;
import game.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import static battleshipclient.Client.sInput;
import game.Game;
import game.GameBoard;
import javax.swing.SwingUtilities;

// serverdan gelecek mesajları dinleyen thread
class Listen extends Thread {

    public void run() {
        //soket bağlı olduğu sürece dön
        while (Client.socket.isConnected()) {
            try {
                //mesaj gelmesini bloking olarak dinyelen komut
                Message received = (Message) (sInput.readObject());

                // Log received message type
                System.out.println("[CLIENT] Received message of type: " + received.type);

                //mesaj gelirse bu satıra geçer
                //mesaj tipine göre yapılacak işlemi ayır.
                switch (received.type) {
                    case Name:
                        System.out.println("[CLIENT] Name message received: " + received.content);

                        String uniqueName = received.content.toString();

                        // İstemci adını arayüzde güncelle
                        Game.ThisGame.txt_name.setText(uniqueName);

                        break;

                    case RivalConnected:
                        String name = received.content.toString();
                        System.out.println("[CLIENT] Rival connected: " + name);

                        // Giriş ekranı üzerindeki rakip adı
                        if (Game.ThisGame != null) {
                            Game.ThisGame.txt_rival_name.setText(name);
                        }

                        // Oyun ekranı üzerindeki rakip adı
                        if (GameBoard.ThisGame != null) {
                            GameBoard.ThisGame.txt_rival_name.setText(name + "'s board");
                        }

                        Game.ThisGame.btn_send_message.setEnabled(true);
                        break;

                    case RivalDisconnected:

                        GameBoard.ThisGame.txt_rival_name.setText("Rival");
                        GameBoard.rivalIsReady = false;
                        GameBoard.enemyBoard.resetBoard();
                        GameBoard.playerBoard.resetBoard();
                        GameBoard.iAmReady = false;
                        GameBoard.rivalIsReady = false;
                        GameBoard.ThisGame.btnFire.setVisible(true);
                        GameBoard.ThisGame.btnFire.setEnabled(false);
                        GameBoard.ThisGame.btnReady.setEnabled(true);
                        GameBoard.ThisGame.btnRestart.setVisible(false);
                        //GameBoard.ThisGame.txt_receive.setText(received.content.toString());
                        break;

                    case Disconnect:
                        System.out.println("[CLIENT] Disconnect message received");
                        break;

                    case Text:
                        System.out.println("[CLIENT] Text message received: " + received.content);
                        Game.ThisGame.txt_receive.setText(received.content.toString());
                        break;

                    case Text2:
                        System.out.println("[CLIENT] Text message received: " + received.content);
                        GameBoard.ThisGame.txt_receive.setText(received.content.toString());
                        break;

                    case Selected:
                        System.out.println("[CLIENT] Selected message received: " + received.content);
                        break;

                    case AttackResult:
                        String[] result = received.content.toString().split(",");
                        int resultRow = Integer.parseInt(result[0]);
                        int resultCol = Integer.parseInt(result[1]);
                        boolean hit = Boolean.parseBoolean(result[2]);
                        String sender = received.sender;  // saldırıyı yapanın adı

                        SwingUtilities.invokeLater(() -> {
                            String myName = Game.ThisGame.txt_name.getText();

                            if (sender.equals(myName)) {
                                // Ben saldırmışım → enemyBoard (sol)
                                if (hit) {
                                    GameBoard.ThisGame.enemyBoard.placeHitMarker(resultRow, resultCol);
                                    Game.ThisGame.txt_receive.setText("Vurdun! 🔥");
                                } else {
                                    GameBoard.ThisGame.enemyBoard.placeMissMarker(resultRow, resultCol);
                                    Game.ThisGame.txt_receive.setText("Iskaladın! 💦");
                                }
                            } else {
                                // Ben vuruldum → playerBoard (sağ)
                                if (hit) {
                                    GameBoard.ThisGame.playerBoard.placeHitMarker(resultRow, resultCol);
                                    Game.ThisGame.txt_receive.setText("Rakip vurdu! 🔥");
                                } else {
                                    GameBoard.ThisGame.playerBoard.placeMissMarker(resultRow, resultCol);
                                    Game.ThisGame.txt_receive.setText("Rakip ıskaladı! 💦");
                                }
                            }

                        });

                        // Sadece ben saldırdıysam, btnFire tekrar açılmalı
                        if (sender.equals(Game.ThisGame.txt_name.getText())) {
                            GameBoard.ThisGame.btnFire.setEnabled(false); // saldırdım, sıra rakipte → kapanmalı
                        } else {
                            GameBoard.ThisGame.btnFire.setEnabled(true);  // rakip saldırdı, şimdi sıra bende
                        }

                        break;

                    case Attack:
                        System.out.println("[CLIENT] Attack message received: " + received.content);
                        // Bu mesaj rakipten geldiğinde işlem yap
                        if (GameBoard.ThisGame != null) {
                            String[] coords = received.content.toString().split(",");
                            int attackRow = Integer.parseInt(coords[0]);
                            int attackCol = Integer.parseInt(coords[1]);

                            // Kendi tahtamızda vuruş kontrolü yap
                            boolean isHit = GameBoard.ThisGame.playerBoard.checkEnemyShot(attackRow, attackCol);

                            // Sonucu rakibe gönder
                            Message resultMsg = new Message(Message.Message_Type.AttackResult);
                            resultMsg.content = attackRow + "," + attackCol + "," + isHit;
                            Client.Send(resultMsg);
                        }
                        break;

                    case Bitis:
                        System.out.println("[CLIENT] Bitis mesajı alındı: " + received.content);

                        GameBoard.ThisGame.btnFire.setVisible(false);
                        GameBoard.ThisGame.txt_receive.setText(received.content.toString());

                        javax.swing.SwingUtilities.invokeLater(() -> {
                            javax.swing.JOptionPane.showMessageDialog(null, received.content.toString(), "Oyun Bitti", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        });

                        GameBoard.ThisGame.btnRestart.setVisible(true);

                        break;

                    case Ready:
                        System.out.println("[CLIENT] Ready message received");

                        GameBoard.rivalIsReady = true;

                        if (GameBoard.iAmReady) {
                            GameBoard.ThisGame.btnFire.setEnabled(true);
                            System.out.println("Her iki oyuncu da hazır! Saldırı yapabilirsiniz.");
                        }
                        break;

                    case SHIP_INFO:
                        System.out.println("[CLIENT] Ship info message received: " + received.content);
                        break;

                    case PairStatus:
                        System.out.println("[CLIENT] Pair status message received: " + received.content);
                        break;

                    case Start:
                        System.out.println("[CLIENT] Yeni oyun başlatılıyor...");
                        SwingUtilities.invokeLater(() -> {
                            GameBoard.ThisGame.initializeNewGame();
                        });
                        break;

                    case Turn:
                        boolean isMyTurn = Boolean.parseBoolean(received.content.toString());
                        GameBoard.myTurn = isMyTurn;

                        // Fire sadece oyuncuların her ikisi de hazırsa ve sıra kendisindeyse açılır
                        boolean allowFire = isMyTurn && GameBoard.iAmReady && GameBoard.rivalIsReady;
                        GameBoard.ThisGame.btnFire.setEnabled(allowFire);

                        GameBoard.ThisGame.txt_receive.setText(
                                allowFire ? "Sıra sizde!" : "Rakip hamlesi bekleniyor..."
                        );
                        break;

                    default:
                        System.out.println("[CLIENT] Unknown message type received: " + received.type);
                        break;
                }

            } catch (IOException ex) {
                System.out.println("[CLIENT] IOException in Listen thread: " + ex.getMessage());
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                //Client.Stop();
                break;
            } catch (ClassNotFoundException ex) {
                System.out.println("[CLIENT] ClassNotFoundException in Listen thread: " + ex.getMessage());
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                //Client.Stop();
                break;
            }
        }
        System.out.println("[CLIENT] Listen thread ending");
    }
}

public class Client {

    //her clientın bir soketi olmalı
    public static Socket socket;

    //verileri almak için gerekli nesne
    public static ObjectInputStream sInput;
    //verileri göndermek için gerekli nesne
    public static ObjectOutputStream sOutput;
    //serverı dinleme thredi 
    public static Listen listenMe;

    public static void Start(String ip, int port) {
        try {
            // Client Soket nesnesi
            Client.socket = new Socket(ip, port);
            Client.Display("Servera bağlandı");
            // input stream
            Client.sInput = new ObjectInputStream(Client.socket.getInputStream());
            // output stream
            Client.sOutput = new ObjectOutputStream(Client.socket.getOutputStream());
            Client.listenMe = new Listen();
            Client.listenMe.start();

            Message msg = new Message(Message.Message_Type.Name);
            msg.content = Game.ThisGame.txt_name.getText();
            System.out.println("[CLIENT] Sending Name message: " + msg.content);
            Client.Send(msg);
        } catch (IOException ex) {
            System.out.println("[CLIENT] Error in Start: " + ex.getMessage());
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //client durdurma fonksiyonu
    public static void Stop() {
        try {
            if (Client.socket != null && !Client.socket.isClosed()) {
                System.out.println("[CLIENT] Stopping client connection");

                // Önce Disconnect mesajını gönder
                Message disconnectMsg = new Message(Message.Message_Type.Disconnect);
                Client.Send(disconnectMsg); // 💡 önce mesaj

                // Sonra soket bağlantısını kes
                Client.listenMe.stop();
                Client.sOutput.flush();
                Client.sOutput.close();
                Client.sInput.close();
                Client.socket.close();
            }
        } catch (IOException ex) {
            System.out.println("[CLIENT] Error in Stop: " + ex.getMessage());
        }
    }

    public static void Display(String msg) {
        System.out.println("[CLIENT] " + msg);
    }

    //mesaj gönderme fonksiyonu
    public static void Send(Message msg) {
        try {
            System.out.println("[CLIENT] Sending message of type: " + msg.type);
            if (msg.content != null) {
                System.out.println("[CLIENT] Message content: " + msg.content.toString());
            }
            Client.sOutput.writeObject(msg);
        } catch (IOException ex) {
            System.out.println("[CLIENT] Error in Send: " + ex.getMessage());
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
