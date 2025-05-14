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

                        // Giriş ekranı üzerindeki rakip adı
                       

                    case Disconnect:
                        System.out.println("[CLIENT] Disconnect message received");
                        break;

                    // İlk mesajlaşma
                    case Text:
                        System.out.println("[CLIENT] Text message received: " + received.content);
                        
                        break;
                            
                    // ikinci mesajlaşma
                    case Text2:
                        System.out.println("[CLIENT] Text message received: " + received.content);
                        
                        break;

                    case Selected:
                        System.out.println("[CLIENT] Selected message received: " + received.content);
                        break;

                   
                    case Bitis:
                        System.out.println("[CLIENT] Bitis mesajı alındı: " + received.content);


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

                // Sunucuya disconnect mesajı gönder
                Message disconnectMsg = new Message(Message.Message_Type.Disconnect);
               
                Client.Send(disconnectMsg);

                // Kaynakları temizle
                Client.listenMe.stop();
                Client.socket.close();
                Client.sOutput.flush();
                Client.sOutput.close();
                Client.sInput.close();
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
