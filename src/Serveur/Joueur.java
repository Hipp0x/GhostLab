package Serveur;

public class Joueur {

    private final String id;
    private final int port;

    public Joueur(String id, int port){
        this.id = id;
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public int getPort() {
        return port;
    }
}
