package Serveur;

import java.net.Socket;

public class Joueur {

    private final String id;
    private final int port;
    private boolean ready;
    private Socket socket;
    private int x; // num de ligne ~= i dans for
    private int y; // num de colonne ~= j dans for
    private String pX;
    private String pY;
    private int point = 0;
    private String pPoint;;

    public Joueur(String id, int port, Socket s) {
        this.id = id;
        this.port = port;
        ready = false;
        socket = s;
    }

    /*
     * -----
     * Getters et Setters
     * -----
     */

    public String getId() {
        return id;
    }

    public int getPort() {
        return port;
    }

    public synchronized void switchReady() {
        ready = !ready;
    }

    public boolean isReady() {
        return ready;
    }

    public Socket getSocket() {
        return socket;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getPoint() {
        return point;
    }

    public String getPosX() {
        return pX;
    }

    public String getPosY() {
        return pY;
    }

    public String getPPoint() {
        return pPoint;
    }

    public void setPosToString(int v, String res) {
        StringBuilder s = new StringBuilder();
        if (v < 10) {
            s.append("00");
            s.append(Integer.toString(v));
        } else if (v < 100) {
            s.append("0");
            s.append(Integer.toString(v));
        } else {
            s.append(Integer.toString(v));
        }
        res = s.toString();
    }

    public void setPointToString(int v, String res) {
        StringBuilder s = new StringBuilder();
        if (v < 10) {
            s.append("000");
            s.append(Integer.toString(v));
        } else if (v < 100) {
            s.append("00");
            s.append(Integer.toString(v));
        } else if (v < 1000) {
            s.append("0");
            s.append(Integer.toString(v));
        } else {
            s.append(Integer.toString(v));
        }
        res = s.toString();
    }

    public void setX(int x2) {
        x = x2;
        setPosToString(x2, pX);
    }

    public void setY(int y2) {
        y = y2;
        setPosToString(y2, pY);
    }

    public void setPoint(int p2) {
        point = p2;
        setPointToString(point, pPoint);
    }
}
