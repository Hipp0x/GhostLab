package Serveur;

import java.net.*;
import java.util.*;

public class ServiceLancementPartie implements Runnable {

    ServerSocket server;
    private static ArrayList<Partie> parties = new ArrayList<Partie>();

    public ServiceLancementPartie(ArrayList<Partie> p, ServerSocket serv) {
        parties = p;
        server = serv;
    }

    @Override
    public void run() {

        while (true) {

            synchronized ((Object) parties) {
            }

            for (Partie p : parties) {

                if (p.peutDemarer()) {

                    synchronized ((Object) parties) {
                        parties.remove(p);
                    }

                }

                // lancer le thread de la partie
                ServicePartie partie = new ServicePartie(p, server);
                Thread t2 = new Thread(partie);
                t2.start();
            }

        }

    }

}
