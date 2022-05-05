package Serveur;

import java.net.*;
import java.util.ArrayList;

public class Serveur {

    private static ArrayList<Partie> parties = new ArrayList<Partie>();

    public static void main(String[] args) {

        try {

            ServerSocket server = new ServerSocket(5621);

            ServiceConnexion connexion = new ServiceConnexion(server, parties);
            Thread t = new Thread(connexion);
            t.start();

        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

}
