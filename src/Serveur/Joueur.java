package Serveur;

public class Joueur {

    private final int id;
    private final int port;

    public Joueur(int id, int port){
        this.id = id;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public int getPort() {
        return port;
    }
}
