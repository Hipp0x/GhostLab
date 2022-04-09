package Serveur;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {

    public static void main(String[] args){
        try{
            ServerSocket server=new ServerSocket(5621);
            while(true){
                Socket socket=server.accept();
                ServiceJoueur serv = new ServiceJoueur(socket);
                Thread t = new Thread(serv);
                t.start();
            }
        }
        catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
