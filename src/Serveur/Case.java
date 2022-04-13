package Serveur;

public class Case {

    private boolean mur;
    // liste de joueur
    // liste de fantome

    public Case(boolean val) {
        mur = val;
    }

    /*
     * -----
     * Getters et Setters
     * -----
     */

    public boolean isMur() {
        return mur;
    }

}
