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

    public int moveU(int d, Joueur j) {
        int x = j.getX();
        int y = j.getY();
        int compt = 0;
        for (int i = 1; i <= d; i++) {
            if (x - i >= 0) {
                if (!laby[x - i][y].isMur()) {
                    compt++;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        j.setX(x + compt);
        return compt;
    }

    public int moveD(int d, Joueur j) {
        int x = j.getX();
        int y = j.getY();
        int compt = 0;
        for (int i = 1; i <= d; i++) {
            if (x + i < h) {
                if (!laby[x + i][y].isMur()) {
                    compt++;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        j.setX(x + compt);
        return compt;
    }

    public int moveR(int d, Joueur j) {
        int x = j.getX();
        int y = j.getY();
        int compt = 0;
        for (int i = 1; i <= d; i++) {
            if (y + i < w) {
                if (!laby[x][y + i].isMur()) {
                    compt++;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        j.setY(y + compt);
        return compt;
    }

    public int moveL(int d, Joueur j) {
        int x = j.getX();
        int y = j.getY();
        int compt = 0;
        for (int i = 1; i <= d; i++) {
            if (y - i >= 0) {
                if (!laby[x][y - i].isMur()) {
                    compt++;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        j.setY(y - compt);
        return compt;
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