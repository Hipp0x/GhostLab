package Serveur;

public class Fantome {

    private int i;
    private int j;
    private int point;
    private boolean capture = false;

    public Fantome(int i, int j) {
        this.i = i;
        this.j = j;
        point = 2;
    }

    /*
     * -----
     * Getters et Setters
     * -----
     */

    public boolean getCapture() {
        return capture;
    }

    public void setCapture(boolean c) {
        capture = c;
    }

    public int getPoint() {
        return point;
    }

}
