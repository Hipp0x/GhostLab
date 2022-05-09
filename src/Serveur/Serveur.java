package Serveur;

import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;

public class Serveur {

    private static ArrayList<Partie> parties = new ArrayList<Partie>();

    public static void main(String[] args) {

        try {

            ServerSocketChannel server = ServerSocketChannel.open();
            server.bind(new InetSocketAddress(5467));

            ServiceConnexion connexion = new ServiceConnexion(server, parties);
            Thread t = new Thread(connexion);
            t.start();

        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

}
