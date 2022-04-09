package Serveur;

public class Case {

    private boolean mur;
    // liste de joueur
    // liste de fantome

    public Case(boolean val) {
        mur = val;
    }

    public boolean isMur() {
        return mur;
    }

}
