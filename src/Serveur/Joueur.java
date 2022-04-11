package Serveur;

import java.net.Socket;

public class Joueur {

    private final String id;
    private final int port;
    private Socket socket;
    int x;
    int y;
    int point = 0;

    public Joueur(String id, int port, Socket s) {
        this.id = id;
        this.port = port;
        socket = s;
    }

    public String getId() {
        return id;
    }

    public int getPort() {
        return port;
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
