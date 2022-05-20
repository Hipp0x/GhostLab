package Serveur;

import java.io.IOException;
import java.util.*;

public class Partie {
    private final int id;
    private static int idCompt = 0;

    private int nbJoueurs;
    private ArrayList<Joueur> joueurs;

    private Labyrinthe labyrinthe;
    // adresse ip
    private String ip = "229.100.100.";
    private static int ipLastNum = 0;
    // port multi diffusion
    private int portMulti;
    // nb fantome
    private int nbFant;
    private ArrayList<Fantome> fantomes;

    private boolean isFinish;

    public Partie() {
        id = idCompt++;
        nbJoueurs = 0;
        isFinish = false;
        fantomes = new ArrayList<>();
        joueurs = new ArrayList<>();
        labyrinthe = new Labyrinthe();
        nbFant = 0;
        portMulti = 8448;
        ip += Integer.toString(ipLastNum++);
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
        if (joueurs.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void placerFantome() {

        int w = labyrinthe.getW();
        int h = labyrinthe.getH();
        Case[][] laby = labyrinthe.getLaby();
        Case cas;
        int normaux;
        int moyen;
        int rare;
        if (nbFant <= 2) {
            for (int i = 0; i < nbFant; i++) {
                int x;
                int y;
                do {
                    x = (new Random()).nextInt(w);
                    y = (new Random()).nextInt(h);
                    cas = laby[x][y];

                } while (cas.isMur());
                Fantome a = new Fantome(x, y, 1);
                fantomes.add(a);
            }
        } else {
            normaux = (int) (0.7 * 5);
            moyen = (int) (0.7 * (nbFant - normaux));
            rare = nbFant - normaux;
            for (int i = 0; i < nbFant; i++) {
                int x;
                int y;
                do {
                    x = (new Random()).nextInt(w);
                    y = (new Random()).nextInt(h);
                    cas = laby[x][y];

                } while (cas.isMur());
                if (i < normaux) {
                    Fantome a = new Fantome(x, y, 1);
                    fantomes.add(a);
                } else if (i < moyen) {
                    Fantome a = new Fantome(x, y, 2);
                    fantomes.add(a);
                } else {
                    Fantome a = new Fantome(x, y, 3);
                    fantomes.add(a);
                }
            }
        }

    }

    public int moveU(int d, Joueur j) {
        Case[][] laby = labyrinthe.getLaby();
        int x = j.getX();
        int y = j.getY();
        int compt = 0;
        int fant = 0;
        ArrayList<Fantome> l = new ArrayList<Fantome>();
        for (int i = 1; i <= d; i++) {
            if (x - i >= 0) {
                if (!laby[x - i][y].isMur()) {
                    compt++;
                    for (Fantome f : fantomes) {
                        if (f.getI() == (x - i) && f.getJ() == (y)) {
                            l.add(f);
                            j.setPoint(f.getPoint());
                            try {
                                ServicePartie.sendUpdateScoreJoueur(j, f);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                } else {
                    break;
                }
            } else {
                break;
            }
        }
        for (Fantome e : l) {
            fantomes.remove(e);
            fant += 1;
            nbFant -= 1;
        }
        if (fantomes.size() == 0) {
            setFinish(true);
        }
        j.setX(x - compt);
        return fant;
    }

    public int moveD(int d, Joueur j) {
        Case[][] laby = labyrinthe.getLaby();
        int h = labyrinthe.getH();
        int x = j.getX();
        int y = j.getY();
        int compt = 0;
        int fant = 0;
        ArrayList<Fantome> l = new ArrayList<Fantome>();
        for (int i = 1; i <= d; i++) {
            if (x + i < h) {
                if (!laby[x + i][y].isMur()) {
                    compt++;
                    for (Fantome f : fantomes) {
                        if (f.getI() == (x + i) && f.getJ() == (y)) {
                            l.add(f);
                            j.setPoint(f.getPoint());
                            try {
                                ServicePartie.sendUpdateScoreJoueur(j, f);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                } else {
                    break;
                }
            } else {
                break;
            }
        }
        for (Fantome e : l) {
            fantomes.remove(e);
            fant += 1;
            nbFant -= 1;
        }
        if (fantomes.size() == 0) {
            setFinish(true);
        }
        j.setX(x + compt);
        return fant;
    }

    public int moveR(int d, Joueur j) {
        Case[][] laby = labyrinthe.getLaby();
        int w = labyrinthe.getW();
        int x = j.getX();
        int y = j.getY();
        int compt = 0;
        int fant = 0;
        ArrayList<Fantome> l = new ArrayList<Fantome>();
        for (int i = 1; i <= d; i++) {
            if (y + i < w) {
                if (!laby[x][y + i].isMur()) {
                    compt++;
                    for (Fantome f : fantomes) {
                        if (f.getI() == (x) && f.getJ() == (y + i)) {
                            l.add(f);
                            j.setPoint(f.getPoint());
                            fant += 1;
                            try {
                                ServicePartie.sendUpdateScoreJoueur(j, f);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                } else {
                    break;
                }
            } else {
                break;
            }
        }
        for (Fantome e : l) {
            fantomes.remove(e);
            nbFant -= 1;
        }
        if (fantomes.size() == 0) {
            setFinish(true);
        }
        j.setY(y + compt);
        return fant;
    }

    public int moveL(int d, Joueur j) {
        Case[][] laby = labyrinthe.getLaby();
        int x = j.getX();
        int y = j.getY();
        int compt = 0;
        int fant = 0;
        ArrayList<Fantome> l = new ArrayList<Fantome>();
        for (int i = 1; i <= d; i++) {
            if (y - i >= 0) {
                if (!laby[x][y - i].isMur()) {
                    compt++;
                    for (Fantome f : fantomes) {
                        if (f.getI() == (x) && f.getJ() == (y - i)) {
                            l.add(f);
                            j.setPoint(f.getPoint());
                            fant += 1;
                            try {
                                ServicePartie.sendUpdateScoreJoueur(j, f);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                } else {
                    break;
                }
            } else {
                break;
            }
        }
        for (Fantome e : l) {
            fantomes.remove(e);
            nbFant -= 1;
        }
        if (fantomes.size() == 0) {
            setFinish(true);
        }
        j.setY(y - compt);
        return fant;
    }

    // renvoi true si le joueur avec id existe dans la partie
    public boolean exists(String id) {
        for (Joueur j : joueurs) {
            if (j.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    // renvoi le joueur correspondant a l'id
    public Joueur getPlayer(String id) {
        for (Joueur j : joueurs) {
            if (j.getId().equals(id)) {
                return j;
            }
        }
        return null;
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

    public ArrayList<Fantome> getFantomes() {
        return fantomes;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean v) {
        isFinish = v;
    }

    public void setFantome(int x) {
        nbFant = x;
    }

    public void removeFantome(Fantome f) {
        fantomes.remove(f);
    }

    public String getPortMultiString() {
        int v = portMulti;
        StringBuilder s = new StringBuilder();
        if (v < 10) {
            s.append("00");
            s.append(v);
        } else if (v < 100) {
            s.append("0");
            s.append(v);
        } else {
            s.append(v);
        }
        return s.toString();
    }

    public String getIpString() {
        StringBuilder s = new StringBuilder();
        s.append(ip);
        for (int i = ip.length(); i < 15; i++) {
            s.append("#");
        }
        return s.toString();
    }

    public void printFant() {
        System.out.println("Print Fantomes");
        for (Fantome a : fantomes) {
            System.out.println("j : " + a.getJ() + "i : " + a.getI());
        }
    }

    public void printJoueur() {
        System.out.println("Print Joueur");
        for (Joueur a : joueurs) {
            System.out.println("x : " + a.getX() + "y : " + a.getY());
        }
    }
}
