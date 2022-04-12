package Serveur;

public class Joueur {

    private final String id;
    private final int port;
    private boolean ready;

    public Joueur(String id, int port){
        this.id = id;
        this.port = port;
        ready = false;
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
}
