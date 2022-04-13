package Serveur;

import java.util.*;
import java.net.*;

public class Labyrinthe {

    private int h;
    private int w;
    private Case[][] laby;

    Labyrinthe() {

    }

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