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
                        break;
                    case RivalConnected:
                        String name = received.content.toString();
                        System.out.println("[CLIENT] Rival connected: " + name);
                        Game.ThisGame.txt_rival_name.setText(name);
                        Game.ThisGame.btn_send_message.setEnabled(true);

                        // Eşleşme sağlandığında otomatik olarak tahtaları göster
                        Game.ThisGame.showGameBoards();
                        break;

                    case Disconnect:
                        System.out.println("[CLIENT] Disconnect message received");
                        break;

                    case Text:
                        System.out.println("[CLIENT] Text message received: " + received.content);
                        Game.ThisGame.txt_receive.setText(received.content.toString());
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
                                    Game.enemyBoard.placeHitMarker(resultRow, resultCol);
                                    Game.ThisGame.txt_receive.setText("Vurdun! 🔥");
                                } else {
                                    Game.enemyBoard.placeMissMarker(resultRow, resultCol);
                                    Game.ThisGame.txt_receive.setText("Iskaladın! 💦");
                                }
                            } else {
                                // Ben vuruldum → playerBoard (sağ)
                                if (hit) {
                                    Game.playerBoard.placeHitMarker(resultRow, resultCol);
                                    Game.ThisGame.txt_receive.setText("Rakip vurdu! 🔥");
                                } else {
                                    Game.playerBoard.placeMissMarker(resultRow, resultCol);
                                    Game.ThisGame.txt_receive.setText("Rakip ıskaladı! 💦");
                                }
                            }

                        });

                        Game.ThisGame.btnFire.setEnabled(true);
                        break;

                    case Attack:
                        System.out.println("[CLIENT] Attack message received: " + received.content);
                        // Bu mesaj rakipten geldiğinde işlem yap
                        if (Game.ThisGame != null) {
                            String[] coords = received.content.toString().split(",");
                            int attackRow = Integer.parseInt(coords[0]);
                            int attackCol = Integer.parseInt(coords[1]);

                            // Kendi tahtamızda vuruş kontrolü yap
                            boolean isHit = Game.playerBoard.checkEnemyShot(attackRow, attackCol);

                            // Sonucu rakibe gönder
                            Message resultMsg = new Message(Message.Message_Type.AttackResult);
                            resultMsg.content = attackRow + "," + attackCol + "," + isHit;
                            Client.Send(resultMsg);
                        }
                        break;

                    case Bitis:
                        System.out.println("[CLIENT] Game end message received");
                        break;

                    case Ready:
                        System.out.println("[CLIENT] Ready message received");
                        break;

                    case SHIP_INFO:
                        System.out.println("[CLIENT] Ship info message received: " + received.content);
                        break;


                    case PairStatus:
                        System.out.println("[CLIENT] Pair status message received: " + received.content);
                        break;

                    case Start:
                        System.out.println("[CLIENT] Start message received");
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

            //ilk mesaj olarak isim gönderiyorum
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
            if (Client.socket != null) {
                System.out.println("[CLIENT] Stopping client connection");
                Client.listenMe.stop();
                Client.socket.close();
                Client.sOutput.flush();
                Client.sOutput.close();
                Client.sInput.close();
            }
        } catch (IOException ex) {
            System.out.println("[CLIENT] Error in Stop: " + ex.getMessage());
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
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
