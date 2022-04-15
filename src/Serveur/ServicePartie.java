package Serveur;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

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

            Selector selector = Selector.open();

            ServerSocketChannel[] tabSSC = new ServerSocketChannel[joueurs.size()];
            int compt = 0;
            for (Joueur joueur : joueurs) {

                iso = joueur.getSocket().getInputStream();
                os = joueur.getSocket().getOutputStream();

                // envoie du message [WELCO␣m␣h␣w␣f␣ip␣port***] a chacun des joueurs
                sendWelcome(os);
                // envoie du message [POSIT␣id␣x␣y***] a chacun des joueurs
                sendPosition(os, joueur);

                // ajout d'une socket du joueur
                ServerSocketChannel acceptor = ServerSocketChannel.open();
                acceptor.configureBlocking(false);
                acceptor.socket().setReuseAddress(true);
                // acceptor.socket().bind(new InetSocketAddress(addr, port));
                acceptor.register(selector, SelectionKey.OP_READ);
                tabSSC[compt] = acceptor;

                compt = compt + 1;
            }

            while (true) {

                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey sk = iterator.next();
                    iterator.remove();
                    for (ServerSocketChannel s : tabSSC) {
                        if (sk.isReadable() && sk.channel() == s) {
                            // readAction(s);
                        }
                    }
                }

            }

        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    // envoi du welcome
    public void sendWelcome(OutputStream os) throws IOException {
        String ip = partie.getIp();
        int portMulti = partie.getPortMulti();

        os.write(
                ("WELCO " + partie.getId() + " " + partie.getLabyrinthe().getH() + " " + partie.getLabyrinthe().getW()
                        + " " + partie.getNbFantome() + " " + ip + " " + portMulti + "***").getBytes(),
                0, (13 + 1 + 2 + 2 + 1 + 8 + 4));
        os.flush();
    }

    // envoi de la position du joueur
    public void sendPosition(OutputStream os, Joueur j) throws IOException {
        String id = j.getId();
        String x = j.getPosX();
        String y = j.getPosY();

        os.write(
                ("POSIT " + id + " " + x + " " + y + "***").getBytes(),
                0, (11 + 8 + 3 + 3));
        os.flush();
    }

    public void sendBye() {

    }

    // lecture de l'action d'un joueur
    public void readAction(SocketChannel s) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(5);
        s.read(buf);
        String action = new String(buf.array());

        if (partie.isFinish()) {
            sendBye();
        }

        switch (action) {
            case "UPMOV":
                buf = ByteBuffer.allocate(1);
                s.read(buf);
                buf = ByteBuffer.allocate(3);
                s.read(buf);
                int d = buf.getInt();
                // int dep = partie.getLabyrinthe().moveU(d);

                buf = ByteBuffer.allocate(3);
                s.read(buf);
                break;
            case "DOMOV":
                break;
            case "LEMOV":
                break;
            case "RIMOV":
                break;
            case "IQUIT":
                sendBye();
                break;
            case "GLIS?":
                break;
            case "MALL?":
                break;
            case "SEND?":
                break;
        }

    }

}
