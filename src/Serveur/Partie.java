package Serveur;

import java.net.*;
import java.util.*;

public class Partie {
    private final int id;
    private static int idCompt = 0;
    private int nbJoueurs;
    private ArrayList<Joueur> joueurs = new ArrayList<>();
    private Labyrinthe labyrinthe;
    // adresse ip
    private String ip;
    // port multi diffusion
    private int portMulti;
    // nb fantome
    private int nbFant;

    private boolean isFinish = false;

    public Partie() {
        id = idCompt++;
        nbJoueurs = 0;
    }

    /*
     * -----
     * Fonctions
     * -----
     */

    public synchronized void addJoueur(Joueur joueur) {
        joueurs.add(joueur);
        ++nbJoueurs;
    }

    public synchronized void removeJoueur(Joueur joueur) {
        joueurs.removeIf(j -> j.getId().equals(joueur.getId()));
        --nbJoueurs;
    }

    public boolean peutDemarer() {
        for (Joueur j : joueurs) {
            if (!j.isReady()) {
                return false;
            }
        }
        return false;
    }

    public void placerFantome() {

    }

    /*
     * -----
     * Getters et Setters
     * -----
     */

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getPortMulti() {
        return portMulti;
    }

    public int getNbJoueurs() {
        return nbJoueurs;
    }

    public Labyrinthe getLabyrinthe() {
        return labyrinthe;
    }

    public int getNbFantome() {
        return nbFant;
    }

    public ArrayList<Joueur> getJoueurs() {
        return joueurs;
    }

    public boolean isFinish() {
        return isFinish;
    }

}
