package Serveur;

import java.io.*;
import java.net.*;
import java.net.InetAddress;
import java.nio.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class  ServicePartie implements Runnable {

    Partie partie;
    ArrayList<Joueur> joueurs;
    ArrayList<SocketChannel> ssc;
    DatagramSocket dso;

    public ServicePartie(Partie p) {
        partie = p;
        joueurs = partie.getJoueurs();
        ssc = new ArrayList<>();
    }

    @Override
    public void run() {

        OutputStream os;

        try {

            Selector selector = Selector.open();
            dso = new DatagramSocket(partie.getPortMulti(), (InetAddress.getByName(partie.getIp())));

            for (Joueur joueur : joueurs) {

                os = joueur.getSocket().getOutputStream();

                // envoi du message [WELCO␣m␣h␣w␣f␣ip␣port***] a chacun des joueurs
                sendWelcome(os);

                Case[][] laby = partie.getLabyrinthe().getLaby();
                int h = laby.length;
                int w = laby[0].length;
                int x;
                int y;
                Case cas;
                do {
                    x = (new Random()).nextInt(w);
                    y = (new Random()).nextInt(h);
                    cas = laby[x][y];

                }while(cas.isMur());
                joueur.setPos(x,y);
                System.out.println("Joueur " + joueur.getId() + ": Position (" + x + "," + y + ").");
                // envoi du message [POSIT␣id␣x␣y***] a chacun des joueurs
                sendPosition(os, joueur);

                // ajout d'une socket du joueur
                SocketChannel acceptor = joueur.getSocket().getChannel();
                acceptor.configureBlocking(false);
                System.out.println("La socket est connectée ? : " + acceptor.isConnected());
                acceptor.socket().setReuseAddress(true);
                acceptor.register(selector, SelectionKey.OP_READ);
                ssc.add(acceptor);

            }

            while (true) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    for (SocketChannel s : ssc) {
                        if (key.isReadable()) {
                            readAction(s, ssc.indexOf(s));
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
        System.out.println(x + "   " + y);

        os.write(
                ("POSIT " + id + " " + x + " " + y + "***").getBytes(),
                0, (11 + 8 + 3 + 3));
        os.flush();
    }

    // envoi du move
    public void sendMove(SocketChannel s, String x, String y) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(("MOVE! " + x + " " + y + "***").getBytes(), 0, 16);
        s.write(buf);
    }

    // envoi du move avec point
    public void sendMoveFant(SocketChannel s, String x, String y, String p, String id) throws IOException {
        // [SCORE␣id␣p␣x␣y+++]
        ByteBuffer buf = ByteBuffer.wrap(
                ("MOVEF " + x + " " + y + " " + p + "***").getBytes(), 0, (11 + 3 + 3 + 4));
        s.write(buf);

        byte[] data;
        for (Fantome f : partie.getFantomes()) {
            if (f.getCapture()) {
                partie.removeFantome(f);
                String env = "SCORE " + id + " " + p + " " + x + " " + y + "***";
                data = env.getBytes();
                InetSocketAddress ia = new InetSocketAddress(partie.getIp(), partie.getPortMulti());
                DatagramPacket paquet = new DatagramPacket(data, data.length, ia);
                dso.send(paquet);
            }
        }

    }

    // envoi de la liste des joueurs
    public void sendListJoueur(SocketChannel s, ArrayList<Joueur> liste) throws IOException {
        int l = liste.size();
        System.out.println(l);
        ByteBuffer buf = ByteBuffer.wrap(
                ("GLIS! " + l + "***").getBytes(), 0, 10);
        s.write(buf);

        for (int i = 0; i < l; i++) {
            Joueur j = liste.get(i);
            buf = ByteBuffer.wrap(
                    ("GPLYR " + j.getId() + " " + j.getPosX() + " " + j.getPosY() + " " + j.getPPoint() + "***")
                            .getBytes(),
                    0, (12 + 8 + 3 + 3 + 4));
            s.write(buf);
        }
    }

    // recupere le message (au + 200 char)
    public String getMess(SocketChannel s) throws IOException {
        StringBuilder mess = new StringBuilder();
        ByteBuffer buf = ByteBuffer.allocate(1);
        while (!buf.toString().equals("*")) {
            s.read(buf);
            mess.append(buf.toString());
        }
        s.read(buf);
        s.read(buf);

        return mess.toString();
    }

    // recupere l'id du joueur
    public String getID(SocketChannel s) throws IOException {
        StringBuilder id = new StringBuilder();
        ByteBuffer buf = ByteBuffer.allocate(8);
        s.read(buf);

        return id.toString();
    }

    // envoi de Mall
    public void sendMall(SocketChannel s) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(("MALL!***").getBytes(), 0, (8));
        s.write(buf);
    }

    // envoi du bye
    public void sendBye(SocketChannel s) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(("GOBYE***").getBytes(), 0, (8));
        s.write(buf);
    }

    // lecture de l'action d'un joueur
    public void readAction(SocketChannel s, int pos) throws IOException {
        Joueur joueur = joueurs.get(pos);

        ByteBuffer buf = ByteBuffer.allocate(5);
        s.read(buf);
        String action = new String(buf.array());

        if (partie.isFinish()) {
            sendBye(s);
        }

        int d, fant;
        String mess;
        String env;
        byte[] data;
        DatagramPacket paquet;

        switch (action) {
            case "UPMOV":
                buf = ByteBuffer.allocate(1);
                s.read(buf);
                buf = ByteBuffer.allocate(3);
                s.read(buf);
                d = Integer.parseInt((new String(buf.array())));
                fant = partie.moveU(d, joueur);
                buf = ByteBuffer.allocate(3);
                s.read(buf);

                if (fant > 0) {
                    sendMoveFant(s, joueur.getPosX(), joueur.getPosY(),
                            joueur.getPPoint(), joueur.getId());
                } else {
                    sendMove(s, joueur.getPosX(), joueur.getPosY());
                }
                break;

            case "DOMOV":
                buf = ByteBuffer.allocate(1);
                s.read(buf);
                buf = ByteBuffer.allocate(3);
                s.read(buf);
                d = Integer.parseInt((new String(buf.array())));
                fant = partie.moveD(d, joueur);
                buf = ByteBuffer.allocate(3);
                s.read(buf);

                if (fant > 0) {
                    sendMoveFant(s, joueur.getPosX(), joueur.getPosY(),
                            joueur.getPPoint(), joueur.getId());
                } else {
                    sendMove(s, joueur.getPosX(), joueur.getPosY());
                }
                break;

            case "LEMOV":
                buf = ByteBuffer.allocate(1);
                s.read(buf);
                buf = ByteBuffer.allocate(3);
                s.read(buf);
                d = Integer.parseInt((new String(buf.array())));
                System.out.println(d);
                fant = partie.moveL(d, joueur);
                buf = ByteBuffer.allocate(3);
                s.read(buf);

                if (fant > 0) {
                    sendMoveFant(s, joueur.getPosX(), joueur.getPosY(),
                            joueur.getPPoint(), joueur.getId());
                } else {
                    sendMove(s, joueur.getPosX(), joueur.getPosY());
                }
                break;

            case "RIMOV":
                buf = ByteBuffer.allocate(1);
                s.read(buf);
                buf = ByteBuffer.allocate(3);
                s.read(buf);
                d = Integer.parseInt((new String(buf.array())));
                fant = partie.moveR(d, joueur);
                buf = ByteBuffer.allocate(3);
                s.read(buf);

                if (fant > 0) {
                    sendMoveFant(s, joueur.getPosX(), joueur.getPosY(),
                            joueur.getPPoint(), joueur.getId());
                } else {
                    sendMove(s, joueur.getPosX(), joueur.getPosY());
                }
                break;

            case "IQUIT":
                sendBye(s);
                break;

            case "GLIS?":
                ArrayList<Joueur> sub;
                synchronized ((Object)joueurs) {
                    sub = joueurs;
                }
                sendListJoueur(s, sub);
                break;

            case "MALL?":
                // lire le message jusqu'aux 3* (max 200 char) et le stocker
                mess = getMess(s);
                // multi diffuser sur l'adresse + port de la partie
                env = "MESSA " + joueur.getId() + " " + mess + "***";
                data = env.getBytes();
                InetSocketAddress ia = new InetSocketAddress(partie.getIp(), partie.getPortMulti());
                paquet = new DatagramPacket(data, data.length, ia);
                dso.send(paquet);

                sendMall(s);
                break;

            case "SEND?":
                // stocker id 8char
                String id = getID(s);
                // stocker mess
                mess = getMess(s);

                OutputStream os2 = joueur.getSocket().getOutputStream();

                // verifier si id du joueur est ds partie
                if (partie.exists(id)) {

                    // si oui
                    Joueur wanted = partie.getPlayer(id);

                    // envoi [MESSP␣id2␣mess+++] sur adresse + port udp du joueur id ou id2 = joueur
                    // qui fait la demande

                    env = "MESSP " + joueur.getId() + " " + mess + "***";
                    data = env.getBytes();
                    paquet = new DatagramPacket(data, data.length,
                            wanted.getSocket().getInetAddress(),
                            wanted.getPort());
                    dso.send(paquet);

                    // envoi [SEND!***] au joueur qui fait la demande
                    os2.write(
                            ("SEND!***").getBytes(), 0, (8));
                    os2.flush();

                } else {

                    // sinon
                    // [NSEND***]
                    os2.write(
                            ("NSEND***").getBytes(), 0, (8));
                    os2.flush();

                }

                break;

            default:
                // finir la lecture
                // envoi dunno
                break;
        }

    }

}
