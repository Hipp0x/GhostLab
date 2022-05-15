package Serveur;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class ServiceConnexion implements Runnable {

    ServerSocketChannel serveur;
    private ArrayList<Partie> parties;

    public ServiceConnexion(ServerSocketChannel s, ArrayList<Partie> parties) {
        serveur = s;
        this.parties = parties;
    }

    @Override
    public void run() {

        while (true) {

            SocketChannel socket;
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
