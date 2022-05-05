package Serveur;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class ServiceConnexion implements Runnable {

    ServerSocket serveur;
    private ArrayList<Partie> parties;

    public ServiceConnexion(ServerSocket s, ArrayList<Partie> parties) {
        serveur = s;
        this.parties = parties;
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
