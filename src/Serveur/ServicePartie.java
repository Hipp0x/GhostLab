package Serveur;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ServicePartie implements Runnable {

    ServerSocket serveur;
    Partie partie;
    ArrayList<Joueur> joueurs;

    public ServicePartie(Partie p, ServerSocket s) {
        serveur = s;
        partie = p;
        joueurs = partie.getJoueurs();
    }

    @Override
    public void run() {

        InputStream iso;
        OutputStream os;
        try {

            for (Joueur joueur : joueurs) {
                iso = joueur.getSocket().getInputStream();
                os = joueur.getSocket().getOutputStream();

                // envoie du message [WELCO␣m␣h␣w␣f␣ip␣port***] a chacun des joueurs
                sendWelcome(os);
                // envoie du message [POSIT␣id␣x␣y***] a chacun des joueurs
                sendPosition(os, joueur);
            }

        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    public void sendWelcome(OutputStream os) throws IOException {
        String ip = partie.getIp();
        int portMulti = partie.getPortMulti();

        os.write(
                ("WELCO " + partie.getId() + " " + partie.getLabyrinthe().getH() + " " + partie.getLabyrinthe().getW()
                        + " " + partie.getNbFantome() + " " + ip + " " + portMulti + "***").getBytes(),
                0, (13 + 1 + 2 + 2 + 1 + 8 + 4));
        os.flush();
    }

    public void sendPosition(OutputStream os, Joueur j) throws IOException {
        String id = j.getId();
        String x = j.getPosX();
        String y = j.getPosY();

        os.write(
                ("POSIT " + id + " " + x + " " + y + "***").getBytes(),
                0, (11 + 8 + 3 + 3));
        os.flush();
    }

}
