package Serveur;

import java.net.*;
import java.util.*;

public class ServiceLancementPartie implements Runnable {

    private ArrayList<Partie> parties;

    public ServiceLancementPartie(ArrayList<Partie> p) {
        parties = p;
    }

    @Override
    public void run() {
        synchronized ((Object) parties) {
            for (Partie p : parties) {
                for (Joueur j : p.getJoueurs()) {
                    System.out.println(j.isReady());
                }

                if (p.peutDemarer()) {

                    // lancer le thread de la partie
                    ServicePartie partie = new ServicePartie(p);
                    Thread t2 = new Thread(partie);
                    t2.start();

                    System.out.println("La partie " + p.getId() + " a commencé.");
                }
            }

        }
    }


}
