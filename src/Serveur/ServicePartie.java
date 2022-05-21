package Serveur;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServicePartie implements Runnable {

    static Partie partie;
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

            partie.placerFantome();

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

                } while (cas.isMur());
                joueur.setPos(x, y);
                System.out.println("Joueur " + joueur.getId() + ": Position (" + x + "," + y + ").");
                // envoi du message [POSIT␣id␣x␣y***] a chacun des joueurs
                sendPosition(os, joueur);

                // ajout d'une socket du joueur
                SocketChannel acceptor = joueur.getSocket().getChannel();
                acceptor.configureBlocking(false);
                acceptor.socket().setReuseAddress(true);
                acceptor.register(selector, SelectionKey.OP_READ);
                ssc.add(acceptor);

            }

            partie.printFant();
            partie.printJoueur();

            Runnable task = new Runnable() {
                @Override
                public void run() {
                    Random random = new Random();
                    System.out.println("nb de fantome : " + partie.getNbFantome());
                    int indice = random.nextInt(partie.getNbFantome());
                    Fantome f = partie.getFantomes().get(indice);

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

                    } while (cas.isMur());
                    f.setPosition(x, y);

                    partie.printFant();
                    try {
                        sendDeplacementFantome(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            };

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(task, 40, 30, TimeUnit.SECONDS);

            ArrayList<SocketChannel> deleted;
            boolean delete = false;
            while (!partie.isFinish()) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    deleted = new ArrayList<>();
                    for (SocketChannel s : ssc) {
                        if (key.isReadable() && key.channel() == s.socket().getChannel()) {
                            Joueur j = getJoueur(s);
                            if (j != null) {
                                System.out.println("Nom du joueur : " + j.getId());
                            }
                            delete = readAction(s, ssc.indexOf(s));
                            if (delete) {
                                deleted.add(s);
                            }
                        }
                    }
                    if (!deleted.isEmpty()) {
                        for (SocketChannel sc : deleted) {
                            sc.close();
                            ssc.remove(sc);
                        }
                    }
                }

            }
            executor.shutdown();
            if (partie.getFantomes().size() == 0) {
                sendFinGame();
            }

            while (joueurs.size() != 0) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    for (SocketChannel s : ssc) {
                        if (key.isReadable()) {
                            sendBye(s);
                        }
                    }
                }

            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public byte[] intToLittleEndian(int taille) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort((short) taille);

        byte[] val = bb.array();
        return val;
    }

    // envoi du welcome [WELCO␣m␣h␣w␣f␣ip␣port***]
    public void sendWelcome(OutputStream os) throws IOException {
        String ip = partie.getIpString();
        String portMulti = partie.getPortMultiString();

        byte[] h = intToLittleEndian(partie.getLabyrinthe().getH());
        byte[] w = intToLittleEndian(partie.getLabyrinthe().getW());

        String s = "WELCO m hh ww f " + ip + " " + portMulti + "***";
        byte[] request = s.getBytes();
        request[6] = (byte) partie.getId();
        request[8] = h[0];
        request[9] = h[1];
        request[11] = w[0];
        request[12] = w[1];
        request[14] = (byte) partie.getNbFantome();
        os.write(request, 0, 39);
        os.flush();
    }

    // envoi de la position du joueur
    public void sendPosition(OutputStream os, Joueur j) throws IOException {
        String id = j.getId();
        String x = j.getPosX();
        String y = j.getPosY();
        System.out.println(x + "   " + y);

        os.write(("POSIT " + id + " " + x + " " + y + "***").getBytes(), 0, (11 + 8 + 3 + 3));
        os.flush();
    }

    // envoi du move
    public void sendMove(SocketChannel s, String x, String y) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(("MOVE! " + x + " " + y + "***").getBytes(), 0, 16);
        System.out.println(new String(buf.array()));
        s.write(buf);
    }

    // envoi du move avec point
    public void sendMoveFant(SocketChannel s, String x, String y, String p, String id) throws IOException {
        // [SCORE␣id␣p␣x␣y+++]
        ByteBuffer buf = ByteBuffer.wrap(("MOVEF " + x + " " + y + " " + p + "***").getBytes(), 0, (11 + 3 + 3 + 4));
        s.write(buf);
    }

    // envoi de la liste des joueurs
    public void sendListJoueur(SocketChannel s, ArrayList<Joueur> liste) throws IOException {
        int l = liste.size();
        System.out.println("taille du joueur : " + l);
        ByteBuffer buf = ByteBuffer.wrap(("GLIS! " + l + "***").getBytes(), 0, 10);
        s.write(buf);

        for (int i = 0; i < l; i++) {
            Joueur j = liste.get(i);
            buf = ByteBuffer
                    .wrap(("GPLYR " + j.getId() + " " + j.getPosX() + " " + j.getPosY() + " " + j.getPPoint() + "***")
                            .getBytes(), 0, (12 + 8 + 3 + 3 + 4));
            s.write(buf);
        }
    }

    // recupere le message (au + 200 char)
    public String getMess(SocketChannel s) throws IOException, InterruptedException {
        StringBuilder mess = new StringBuilder();
        ByteBuffer buf = ByteBuffer.allocate(1);
        do {
            s.read(buf);
            mess.append(new String(buf.array()));
            buf = ByteBuffer.allocate(1);
            s.read(buf);
        } while (!(new String(buf.array())).equals("*"));
        buf = ByteBuffer.allocate(1);
        s.read(buf);
        buf = ByteBuffer.allocate(1);
        s.read(buf);

        System.out.println("Message :" + mess.toString());
        return mess.toString();
    }

    // recupere l'id du joueur
    public String getID(SocketChannel s) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1);
        s.read(buf);
        buf = ByteBuffer.allocate(8);
        s.read(buf);

        System.out.println("ID :" + (new String(buf.array())));
        return new String(buf.array());
    }

    // envoi de Mall
    public void sendMall(SocketChannel s) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(("MALL!***").getBytes(), 0, (8));
        s.write(buf);
    }

    public Joueur getJoueur(SocketChannel s) {
        for (Joueur j : joueurs) {
            if (j.getSocket() == s.socket()) {
                return j;
            }
        }
        return null;
    }

    // envoi du bye
    public void sendBye(SocketChannel s) throws IOException {
        clearIS(s);
        ByteBuffer buf = ByteBuffer.wrap(("GOBYE***").getBytes(), 0, (8));
        s.write(buf);

        Joueur j = getJoueur(s);
        if (j != null) {
            joueurs.remove(j);
        }

    }

    // multidiffuser le score d'un joueur lors de la prise d'un fantome
    public static void sendUpdateScoreJoueur(Joueur j, Fantome f) throws IOException {
        // SCORE id p x y+++
        String mess = "SCORE " + j.getId() + " " + j.getPPoint() + " " + f.getPosX() + " " + f.getPosY() + "+++";

        // multi diffuser sur l'adresse + port de la partie
        ByteBuffer buffMC = ByteBuffer.wrap(mess.getBytes());

        InetSocketAddress ia = new InetSocketAddress(partie.getIp(), partie.getPortMulti());
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(null);
        channel.send(buffMC, ia);
        System.out.println(new String(buffMC.array()) + "     SENT");
    }

    // retourne le meilleur joueur de la partie
    public Joueur getBestPlayer() {
        Joueur j = joueurs.get(0);
        for (Joueur x : joueurs) {
            if (x.getPoint() > j.getPoint()) {
                j = x;
            }
        }
        return j;
    }

    // multidiffuser la fin du jeu
    public void sendFinGame() throws IOException {
        // ENDGA id p+++
        Joueur j = getBestPlayer();
        String mess = "ENDGA " + j.getId() + " " + j.getPPoint() + "+++";

        // multi diffuser sur l'adresse + port de la partie
        ByteBuffer buffMC = ByteBuffer.wrap(mess.getBytes());

        InetSocketAddress ia = new InetSocketAddress(partie.getIp(), partie.getPortMulti());
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(null);
        channel.send(buffMC, ia);
        System.out.println(new String(buffMC.array()) + "     SENT");
    }

    // multidiffuser le deplacement d'un fantome
    public void sendDeplacementFantome(Fantome f) throws IOException {
        // GHOST x y+++
        String mess = "GHOST " + f.getPosX() + " " + f.getPosY() + "+++";

        // multi diffuser sur l'adresse + port de la partie
        ByteBuffer buffMC = ByteBuffer.wrap(mess.getBytes());

        InetSocketAddress ia = new InetSocketAddress(partie.getIp(), partie.getPortMulti());
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(null);
        channel.send(buffMC, ia);
        System.out.println(new String(buffMC.array()) + "     SENT");
    }

    // multidiffuser un message pour tous les joueurs
    public void sendMessageForAll(String mess, Joueur joueur) throws IOException {
        // MESSA id mess+++
        // multi diffuser sur l'adresse + port de la partie
        String en = "MESSA " + joueur.getId() + " " + mess + "+++";
        ByteBuffer buffMC = ByteBuffer.wrap(en.getBytes());

        System.out.println("ip de partie : " + partie.getIp());
        System.out.println("port multi : " + partie.getPortMulti());
        InetSocketAddress ia = new InetSocketAddress(partie.getIp(), partie.getPortMulti());
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(null);
        channel.send(buffMC, ia);
        System.out.println(new String(buffMC.array()) + "     SENT");
    }

    // envoi du labyrinthe
    public void sendLaby(SocketChannel s) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(("TRCHL***").getBytes(), 0, (8));
        s.write(buf);
        Labyrinthe l = partie.getLabyrinthe();
        int w = l.getW();
        int h = l.getH();
        Case[][] laby = l.getLaby();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (laby[i][j].isMur()) {

                    buf = ByteBuffer.wrap(("TRUE!***").getBytes(), 0, (8));
                    s.write(buf);
                    System.out.println("envoie true");
                } else {

                    buf = ByteBuffer.wrap(("FALSE***").getBytes(), 0, (8));
                    s.write(buf);
                    System.out.println("envoie false");
                }
            }
        }
    }

    // envoi d'un fantome
    public void sendFant(SocketChannel s) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(("TRCHF***").getBytes(), 0, (8));
        s.write(buf);
        ArrayList<Fantome> l = partie.getFantomes();
        Random rand = new Random();
        int i = rand.nextInt(l.size());
        Fantome f = l.get(i);
        buf = ByteBuffer.wrap((f.getPosX() + " " + f.getPosY() + "***").getBytes(), 0, (8));
        s.write(buf);

    }

    // lecture de l'action d'un joueur
    public boolean readAction(SocketChannel s, int pos) throws IOException, InterruptedException {
        Joueur joueur = joueurs.get(pos);

        ByteBuffer buf = ByteBuffer.allocate(5);
        s.read(buf);
        String action = new String(buf.array());

        int d, fant;
        String mess;
        String env;
        byte[] data;
        DatagramPacket paquet;

        System.out.println("Recu :" + action + ".");
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
                    joueur.setPoint(joueur.getPoint() + fant);
                    sendMoveFant(s, joueur.getPosX(), joueur.getPosY(),
                            joueur.getPPoint(), joueur.getId());
                } else {
                    sendMove(s, joueur.getPosX(), joueur.getPosY());
                }
                return false;

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
                return false;

            case "LEMOV":
                buf = ByteBuffer.allocate(1);
                s.read(buf);
                buf = ByteBuffer.allocate(3);
                s.read(buf);
                d = Integer.parseInt((new String(buf.array())));
                fant = partie.moveL(d, joueur);
                buf = ByteBuffer.allocate(3);
                s.read(buf);

                if (fant > 0) {
                    sendMoveFant(s, joueur.getPosX(), joueur.getPosY(),
                            joueur.getPPoint(), joueur.getId());
                } else {
                    sendMove(s, joueur.getPosX(), joueur.getPosY());
                }
                return false;

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
                return false;

            case "IQUIT":
                sendBye(s);
                return true;

            case "GLIS?":
                buf = ByteBuffer.allocate(3);
                s.read(buf);
                sendListJoueur(s, joueurs);
                return false;

            case "MALL?":
                // lire le message jusqu'aux 3* (max 200 char) et le stocker

                mess = getMess(s);

                sendMessageForAll(mess, joueur);

                sendMall(s);
                return false;

            case "SEND?":
                // stocker id 8char
                String id = getID(s);

                // stocker mess
                mess = getMess(s);

                // verifier si id du joueur est ds partie
                if (partie.exists(id)) {

                    // si oui
                    Joueur wanted = partie.getPlayer(id);

                    // envoi [MESSP␣id2␣mess+++] sur adresse + port udp du joueur id ou id2 = joueur
                    // qui fait la demande

                    env = "MESSP " + joueur.getId() + " " + mess + "+++";
                    ByteBuffer buffUdp = ByteBuffer.wrap(env.getBytes());
                    System.out.println("Adresse IP UDP = " + wanted.getSocket().getInetAddress().getHostAddress()
                            + "\n Port UDP = " + wanted.getPort());
                    InetSocketAddress addr = new InetSocketAddress(wanted.getSocket().getInetAddress().getHostAddress(),
                            wanted.getPort());
                    DatagramChannel chan = DatagramChannel.open();
                    chan.bind(null);
                    chan.send(buffUdp, addr);

                    // envoi [SEND!***] au joueur qui fait la demande
                    ByteBuffer send = ByteBuffer.wrap(("SEND!***").getBytes(), 0, 8);
                    s.write(send);

                } else {

                    // sinon
                    // [NSEND***]
                    ByteBuffer nsend = ByteBuffer.wrap(("NSEND***").getBytes(), 0, 8);
                    s.write(nsend);

                }

                return false;
            case "XTLX?": // triche pour labyrinthe
                buf = ByteBuffer.allocate(3);
                s.read(buf);
                sendLaby(s);
                return false;
            case "XTFX?": // triche pour fantome
                buf = ByteBuffer.allocate(3);
                s.read(buf);
                sendFant(s);
                return false;
            default:
                clearIS(s);
                buf = ByteBuffer.wrap(("DUNNO***").getBytes(), 0, (8));
                s.write(buf);
                return false;
        }

    }

    // clear la lecture jusqu'aux ***
    public void clearIS(SocketChannel iso) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1);
        String r = new String(buf.array());
        while (!("*").equals(r)) {
            buf = ByteBuffer.allocate(1);
            iso.read(buf);
            r = new String(buf.array());
        }

        buf = ByteBuffer.allocate(1);
        iso.read(buf);

        buf = ByteBuffer.allocate(1);
        iso.read(buf);

    }

}
