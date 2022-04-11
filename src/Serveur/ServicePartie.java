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
                sendPosition(os);
            }

        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    public void sendWelcome(OutputStream os) throws IOException {
        String ip = partie.getIp();
        int portMulti = partie.getPortMulti();

        os.write(("REGOK***").getBytes());
        os.flush();
    }

    public void sendPosition(OutputStream os) throws IOException {
        String ip = partie.getIp();
        int portMulti = partie.getPortMulti();

        os.write(("REGOK***").getBytes());
        os.flush();
    }

}
