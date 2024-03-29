package Serveur;

public class Fantome {

    private int i;
    private int j;
    private int point;

    public Fantome(int i, int j, int p) {
        this.i = i;
        this.j = j;
        point = p;
    }

    /*
     * -----
     * Getters et Setters
     * -----
     */

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public int getPoint() {
        return point;
    }

    public String setPosToString(int v) {
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

    public String getPosX() {
        return setPosToString(i);
    }

    public String getPosY() {
        return setPosToString(j);
    }

    public void setPosition(int x, int y) {
        i = x;
        j = y;
    }

}
