package Serveur;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class ServiceConnexion implements Runnable {

    ServerSocket serveur;
    private static ArrayList<Partie> parties = new ArrayList<Partie>();

    public ServiceConnexion(ServerSocket s, ArrayList<Partie> p) {
        serveur = s;
        parties = p;
    }

    @Override
    public void run() {

        while (true) {

            Socket socket;
            try {

                socket = serveur.accept();
                ServiceJoueur serv = new ServiceJoueur(socket, parties);
                Thread t = new Thread(serv);
                t.start();

            } catch (IOException e) {

                e.printStackTrace();
            }

        }

    }

}
