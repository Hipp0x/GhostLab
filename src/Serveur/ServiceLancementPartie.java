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
        for (Partie p : parties) {
            for (Joueur j : p.getJoueurs()) {
                System.out.println(j.isReady());
            }
            if (p.peutDemarer()) {

                // lancer le thread de la partie
                ServicePartie partie = new ServicePartie(p);
                Thread t2 = new Thread(partie);
                t2.start();
                synchronized ((Object) parties) {
                    parties.remove(p);
                    System.out.println("La partie " + p.getId() + " a commencé.");
                }

            }
        }
    }


    public ArrayList<Partie> getParties() {
        return parties;
    }

    public void updateParties(ArrayList<Partie> parties){
        this.parties = parties;
    }

    public void addPartie(Partie p){
        parties.add(p);
    }

    public void addJoueur(int idPartie, Joueur j){
        for(Partie p : parties){
            if(p.getId() == idPartie){
                p.addJoueur(j);
                return;
            }
        }
    }
}
