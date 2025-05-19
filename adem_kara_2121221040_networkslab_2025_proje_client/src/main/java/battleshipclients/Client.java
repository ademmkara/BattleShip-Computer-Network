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


                //mesaj gelirse bu satıra geçer
                //mesaj tipine göre yapılacak işlemi ayır.
                switch (received.type) {
                    case Name:


                        String uniqueName = received.content.toString();

                        // İstemci adını arayüzde güncelle
                        Game.ThisGame.txt_name.setText(uniqueName);

                        break;

                    case RivalConnected:
                        String name = received.content.toString();


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
                        Game.ThisGame.txt_rival_name.setText("Rival");
                        GameBoard.rivalIsReady = false;
                        GameBoard.enemyBoard.resetBoard();
                        GameBoard.playerBoard.resetBoard();
                        GameBoard.iAmReady = false;
                        GameBoard.rivalIsReady = false;
                        GameBoard.ThisGame.btnFire.setVisible(true);
                        GameBoard.ThisGame.btnFire.setEnabled(false);
                        GameBoard.ThisGame.btnReady.setEnabled(true);
                        GameBoard.ThisGame.btnRestart.setVisible(false);
                        GameBoard.ThisGame.btnHorizantal.setEnabled(true);
                        GameBoard.ThisGame.btnVertical.setEnabled(true);
                        //GameBoard.ThisGame.txt_receive.setText(received.content.toString());
                        break;

                    case Disconnect:

                        break;

                    case Text:

                        Game.ThisGame.txt_receive.setText(received.content.toString());
                        break;

                    case Text2:
                        GameBoard.ThisGame.txt_receive.setText(received.content.toString());
                        break;

//                    case Selected:
//                        System.out.println("[CLIENT] Selected message received: " + received.content);
//                        break;

                    case AttackResult:
                        String[] result = received.content.toString().split(",");
                        int resultRow = Integer.parseInt(result[0]);
                        int resultCol = Integer.parseInt(result[1]);
                        boolean hit = Boolean.parseBoolean(result[2]);
                        String sender = received.sender;

                        SwingUtilities.invokeLater(() -> {
                            String myName = Game.ThisGame.txt_name.getText();

                            if (sender.equals(myName)) {
                                // Ben saldırmışım
                                if (hit) {
                                    GameBoard.ThisGame.enemyBoard.placeHitMarker(resultRow, resultCol);
                                    GameBoard.ThisGame.txt_receive.setText("Vurdun!");
                                } else {
                                    GameBoard.ThisGame.enemyBoard.placeMissMarker(resultRow, resultCol);
                                    GameBoard.ThisGame.txt_receive.setText("Iskaladın!");
                                }
                            } else {
                                // Rakip saldırmış
                                if (hit) {
                                    GameBoard.ThisGame.playerBoard.placeHitMarker(resultRow, resultCol);
                                    GameBoard.ThisGame.txt_receive.setText("Rakip vurdu!");
                                } else {
                                    GameBoard.ThisGame.playerBoard.placeMissMarker(resultRow, resultCol);
                                    GameBoard.ThisGame.txt_receive.setText("Rakip ıskaladı!");
                                }
                            }
                        });

                        // 🔥 Artık burada btnFire kontrolü YOK!
                        break;

                    case Attack:
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

                        GameBoard.ThisGame.btnFire.setVisible(false);
                        GameBoard.ThisGame.txt_receive.setText(received.content.toString());

                        javax.swing.SwingUtilities.invokeLater(() -> {
                            javax.swing.JOptionPane.showMessageDialog(null, received.content.toString(), "Oyun Bitti", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        });

                        GameBoard.ThisGame.btnRestart.setVisible(true);

                        break;

                    case Ready:

                        GameBoard.rivalIsReady = true;

                        if (GameBoard.iAmReady) {
                            GameBoard.ThisGame.btnFire.setEnabled(true);
                            System.out.println("Her iki oyuncu da hazır! Saldırı yapabilirsiniz.");
                        }
                        break;

                    case SHIP_INFO:
                        break;

                    case PairStatus:
                        break;

                    case Start:
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
                        break;
                }

            } catch (IOException ex) {

                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                //Client.Stop();
                break;
            } catch (ClassNotFoundException ex) {

                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                //Client.Stop();
                break;
            }
        }

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

            Client.Send(msg);
        } catch (IOException ex) {

            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //client durdurma fonksiyonu
    public static void Stop() {
        try {
            if (Client.socket != null && !Client.socket.isClosed()) {


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

        }
    }

    public static void Display(String msg) {

    }

    //mesaj gönderme fonksiyonu
    public static void Send(Message msg) {
    try {
        if (socket == null || socket.isClosed()) {
            return; 
        }

        if (msg.content != null) {
        }

        sOutput.writeObject(msg);
        sOutput.flush();
    } catch (IOException ex) {
        
    }
}

}
