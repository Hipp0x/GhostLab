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
    ArrayList<ServerSocketChannel> ssc;

    public ServicePartie(Partie p, ServerSocket s) {
        serveur = s;
        partie = p;
        joueurs = partie.getJoueurs();
        ssc = new ArrayList<>();
    }

    @Override
    public void run() {

        InputStream iso;
        OutputStream os;
        try {

            Selector selector = Selector.open();

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
                ssc.add(acceptor);

            }

            while (true) {

                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey sk = iterator.next();
                    iterator.remove();
                    for (ServerSocketChannel s : ssc) {
                        if (sk.isReadable() && sk.channel() == s) {
                            // readAction(s, ssc.indexOf(s));
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

    // envoi du move
    public void sendMove(OutputStream os, String x, String y) throws IOException {
        os.write(
                ("MOVE! " + x + " " + y + "***").getBytes(), 0, (10 + 3 + 3));
        os.flush();
    }

    // envoi du move avec point
    public void sendMoveFant(OutputStream os, String x, String y, String p) throws IOException {
        os.write(
                ("MOVEF " + x + " " + y + " " + p + "***").getBytes(), 0, (11 + 3 + 3 + 4));
        os.flush();
    }

    public void sendBye(OutputStream os) throws IOException {
        os.write(
                ("GOBYE***").getBytes(), 0, (8));
        os.flush();
    }

    // lecture de l'action d'un joueur
    public void readAction(SocketChannel s, int pos) throws IOException {
        Joueur joueur = joueurs.get(pos);

        ByteBuffer buf = ByteBuffer.allocate(5);
        s.read(buf);
        String action = new String(buf.array());

        if (partie.isFinish()) {
            sendBye(joueur.getSocket().getOutputStream());
        }

        int d, fant;

        switch (action) {
            case "UPMOV":
                buf = ByteBuffer.allocate(1);
                s.read(buf);
                buf = ByteBuffer.allocate(3);
                s.read(buf);
                d = buf.getInt();
                fant = partie.moveU(d, joueur);
                buf = ByteBuffer.allocate(3);
                s.read(buf);

                if (fant > 0) {
                    sendMoveFant(joueur.getSocket().getOutputStream(), joueur.getPosX(), joueur.getPosY(),
                            joueur.getPPoint());
                } else {
                    sendMove(joueur.getSocket().getOutputStream(), joueur.getPosX(), joueur.getPosY());
                }
                break;

            case "DOMOV":
                buf = ByteBuffer.allocate(1);
                s.read(buf);
                buf = ByteBuffer.allocate(3);
                s.read(buf);
                d = buf.getInt();
                fant = partie.moveU(d, joueur);
                buf = ByteBuffer.allocate(3);
                s.read(buf);

                if (fant > 0) {
                    sendMoveFant(joueur.getSocket().getOutputStream(), joueur.getPosX(), joueur.getPosY(),
                            joueur.getPPoint());
                } else {
                    sendMove(joueur.getSocket().getOutputStream(), joueur.getPosX(), joueur.getPosY());
                }
                break;

            case "LEMOV":
                buf = ByteBuffer.allocate(1);
                s.read(buf);
                buf = ByteBuffer.allocate(3);
                s.read(buf);
                d = buf.getInt();
                fant = partie.moveL(d, joueur);
                buf = ByteBuffer.allocate(3);
                s.read(buf);

                if (fant > 0) {
                    sendMoveFant(joueur.getSocket().getOutputStream(), joueur.getPosX(), joueur.getPosY(),
                            joueur.getPPoint());
                } else {
                    sendMove(joueur.getSocket().getOutputStream(), joueur.getPosX(), joueur.getPosY());
                }
                break;

            case "RIMOV":
                buf = ByteBuffer.allocate(1);
                s.read(buf);
                buf = ByteBuffer.allocate(3);
                s.read(buf);
                d = buf.getInt();
                fant = partie.moveR(d, joueur);
                buf = ByteBuffer.allocate(3);
                s.read(buf);

                if (fant > 0) {
                    sendMoveFant(joueur.getSocket().getOutputStream(), joueur.getPosX(), joueur.getPosY(),
                            joueur.getPPoint());
                } else {
                    sendMove(joueur.getSocket().getOutputStream(), joueur.getPosX(), joueur.getPosY());
                }
                break;

            case "IQUIT":
                sendBye(joueur.getSocket().getOutputStream());
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
