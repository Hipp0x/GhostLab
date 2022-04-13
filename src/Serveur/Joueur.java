package Serveur;

import java.net.Socket;

public class Joueur {

    private final String id;
    private final int port;
    private boolean ready;
    private Socket socket;
    int x;
    int y;
    String pX;
    String pY;
    int point = 0;

    public Joueur(String id, int port, Socket s) {
        this.id = id;
        this.port = port;
        ready = false;
        socket = s;
    }

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

    public void setX(int x2) {
        x = x2;
        setPosToString(x2, pX);
    }

    public void setY(int y2) {
        y = y2;
        setPosToString(y2, pY);
    }
}
