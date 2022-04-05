package Serveur;

import java.util.ArrayList;

public class Partie {
    private final int id;
    private static int idCompt = 0;
    private int nbJoueurs;
    private ArrayList<Joueur> joueurs = new ArrayList<>();

    public Partie(){
        id = idCompt++;
        nbJoueurs = 1;
    }

    public int getId() {
        return id;
    }

    public int getNbJoueurs() {
        return nbJoueurs;
    }

    public synchronized void incrNbJoueurs() {
        this.nbJoueurs++;
    }

    public synchronized void addJoueur(Joueur joueur){
        joueurs.add(joueur);
    }
}
