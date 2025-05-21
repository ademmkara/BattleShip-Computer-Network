
package battleshipserver;

import game.Message;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

//scp -i "C:\Users\cvsbilisim\Desktop\battleship-servers.pem" "C:\Users\cvsbilisim\.m2\repository\com\mycompany\adem_kara_2121221040_networkslab_2025_proje_server\1.0-SNAPSHOT\adem_kara_2121221040_networkslab_2025_proje_server-1.0-SNAPSHOT.jar" ubuntu@13.60.186.154:/home/ubuntu
//ssh -i "C:\Users\cvsbilisim\Desktop\battleship-servers.pem" ubuntu@13.60.186.154
//java -jar


class ServerThread extends Thread {

    public void run() {

        //server kapanana kadar dinle
        while (!Server.serverSocket.isClosed()) {
            try {
                Server.Display("Client Bekleniyor...");
                //bir client gelene kadar bekler
                Socket clientSocket = Server.serverSocket.accept();
                //client gelirse bu satıra geçer
                Server.Display("Client Baglandi...");
                //her client a bir adet id 
                SClient nclient = new SClient(clientSocket, Server.IdClient);

                Server.IdClient++;
                //clienti listeye ekle.
                Server.Clients.add(nclient);
                //client mesaj dinlemesini başlat
                nclient.listenThread.start();

            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

public class Server {

    //server soketi eklemeliyiz
    public static ServerSocket serverSocket;
    public static int IdClient = 0;
    // Serverın dileyeceği port
    public static int port = 0;
    //Serverı sürekli dinlemede tutacak thread nesnesi
    public static ServerThread runThread;
    //public static PairingThread pairThread;

    public static ArrayList<SClient> Clients = new ArrayList<>();

    //semafor nesnesi
    public static Semaphore pairTwo = new Semaphore(1, true);

    // başlaşmak için sadece port numarası veriyoruz
    public static void Start(int openport) {
        try {
            Server.port = openport;
            Server.serverSocket = new ServerSocket(Server.port);

            Server.runThread = new ServerThread();
            Server.runThread.start();

        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void Display(String msg) {

        System.out.println(msg);

    }

    // serverdan clietlara mesaj gönderme
    //clieti alıyor ve mesaj olluyor
    public static void Send(SClient cl, Message msg) {

        try {
            cl.sOutput.writeObject(msg);
        } catch (IOException ex) {
            Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


}
