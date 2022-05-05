package Serveur;

public class Labyrinthe {

    private int h; // nb de lignes
    private int w; // nb de colonnes
    private Case[][] laby;

    Labyrinthe() {
        laby = new Case[10][10];
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                laby[i][j] = new Case(false);
            }
        }
        h = 10;
        w = 10;
    }

    /*
     * -----
     * Fonctions
     * -----
     */

    /*
     * -----
     * Getters et Setters
     * -----
     */

    public int getH() {
        return h;
    }

    public int getW() {
        return w;
    }

    public Case[][] getLaby() {
        return laby;
    }

}