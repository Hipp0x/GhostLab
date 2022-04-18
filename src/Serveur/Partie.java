package Serveur;

import java.util.ArrayList;

public class Partie {
    private final int id;
    private static int idCompt = 0;
    private int nbJoueurs;
    private ArrayList<Joueur> joueurs ;
    private Labyrinthe laby;

    public Partie(){
        id = idCompt++;
        nbJoueurs = 0;
        joueurs = new ArrayList<Joueur>();
        laby = new Labyrinthe();
    }

    public int getId() {
        return id;
    }

    public int getNbJoueurs() {
        return nbJoueurs;
    }

    public synchronized void addJoueur(Joueur joueur){
        joueurs.add(joueur);
        ++nbJoueurs;
    }

    public synchronized void removeJoueur(Joueur joueur){
        joueurs.removeIf(j -> j.getId().equals(joueur.getId()));
        --nbJoueurs;
    }

    public Labyrinthe getLaby() {
        return laby;
    }

    public ArrayList<Joueur> getJoueurs() {
        return joueurs;
    }
}
