package Serveur;

import java.net.Socket;

public class Joueur {

    private final String id;
    private final int port;
    private boolean ready;
    private Socket socket;
    int x;
    int y;
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
}
