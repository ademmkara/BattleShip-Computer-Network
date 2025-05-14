/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package battleshipclient;

import static battleshipclient.Client.sInput;

import game.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cvsbilisim
 */
class Listen extends Thread {

    public void run() {
        //soket bağlı olduğu sürece dön
        while (Client.socket.isConnected()) {
      
                //mesaj gelmesini bloking olarak dinyelen komut
                Message received = (Message) (sInput.readObject());

                // Log received message type
                System.out.println("[CLIENT] Received message of type: " + received.type);

                //mesaj gelirse bu satıra geçer
                //mesaj tipine göre yapılacak işlemi ayır.
                switch (received.type) {
                    case Name:
                        System.out.println("[CLIENT] Name message received: " + received.content);

                  
                    case Disconnect:
                        System.out.println("[CLIENT] Disconnect message received");
                        break;

                    case Text:
                        System.out.println("[CLIENT] Text message received: " + received.content);
                        
                        break;

                    case Text2:
                        System.out.println("[CLIENT] Text message received: " + received.content);
                    
                        break;

                   

                    case Bitis:
                        System.out.println("[CLIENT] Bitis mesajı alındı: " + received.content);

                       
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
          
            // input stream
            Client.sInput = new ObjectInputStream(Client.socket.getInputStream());
            // output stream
            Client.sOutput = new ObjectOutputStream(Client.socket.getOutputStream());
            Client.listenMe = new Listen();
            Client.listenMe.start();

        } catch (IOException ex) {
            System.out.println("[CLIENT] Error in Start: " + ex.getMessage());
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
