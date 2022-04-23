package Serveur;

import java.util.ArrayList;

public class Case {

    private boolean mur;
    // liste de joueur
    private ArrayList<Joueur> joueurs = new ArrayList<>();
    // liste de fantome
    private ArrayList<Fantome> fants = new ArrayList<>();

    public Case(boolean val) {
        mur = val;
    }

    /*
     * -----
     * Getters et Setters
     * -----
     */

    public int getNbJoueurs() {
        return joueurs.size();
    }

    public ArrayList<Fantome> getFantomes() {
        return fants;
    }

    public void removeJoueur(String id) {

    }

    public void removeFantome() {
        fants = new ArrayList<>();
    }

    public void addJoueur(Joueur a) {
        joueurs.add(a);
    }

    public int getNbFantome() {
        return fants.size();
    }

    public void removeFant(Fantome a) {
        fants.remove(a);
    }

    public void addFant(Fantome a) {
        fants.add(a);
    }

    public void setCapture() {
        for (Fantome a : fants) {
            a.setCapture(true);
        }
    }

    public boolean isMur() {
        return mur;
    }

}
