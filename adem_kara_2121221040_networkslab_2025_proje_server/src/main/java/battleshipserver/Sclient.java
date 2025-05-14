/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package battleshipserver;

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
public class SClient {
    int id;
    public String name = "NoName";
    Socket soket;
    ObjectOutputStream sOutput;
    ObjectInputStream sInput;
    //clientten gelenleri dinleme threadi
    Listen listenThread;

    //rakip client
    SClient rival;
    
    public SClient(Socket gelenSoket, int id) {
        this.soket = gelenSoket;
        this.id = id;
        try {
            this.sOutput = new ObjectOutputStream(this.soket.getOutputStream());
            this.sInput = new ObjectInputStream(this.soket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        //thread nesneleri
        this.listenThread = new Listen(this);


    }
    class Listen extends Thread {
        SClient TheClient;

        //thread nesne alması için yapıcı metod
        Listen(SClient TheClient) {
            this.TheClient = TheClient;
        }
    }
    public void run() {
    while (TheClient.soket.isConnected()) {

                    //mesajı bekleyen kod satırı
                    Message received = (Message) (TheClient.sInput.readObject());
                    //mesaj gelirse bu satıra geçer
                    //mesaj tipine göre işlemlere ayır
                    switch (received.type) {
                        case Name:
                            String originalName = received.content.toString();
                            String uniqueName = originalName;
                            int suffix = 1;

                            // Aynı isimde biri varsa sonuna numara ekleyerek benzersiz yap
                            for (SClient existing : Server.Clients) {
                                if (existing.name.equals(uniqueName)) {
                                    uniqueName = originalName + "_" + suffix;
                                    suffix++;
                                }
                            }

                            TheClient.name = uniqueName;

                            // İstemciye yeni ismi bildir
                            Message nameMsg = new Message(Message.Message_Type.Name);
                            nameMsg.content = uniqueName;
                            Server.Send(TheClient, nameMsg);

                            TheClient.pairThread.start();
                            break;
                        case Disconnect:
                            Server.Clients.remove(TheClient);
                            TheClient.rival.hp = 17;
                            TheClient.hp = 17;
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
                            break;}
}
    }