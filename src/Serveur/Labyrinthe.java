package Serveur;

import java.util.*;
import java.net.*;

public class Labyrinthe {

    private int h; // nb de lignes
    private int w; // nb de colonnes
    private Case[][] laby;

    Labyrinthe() {

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