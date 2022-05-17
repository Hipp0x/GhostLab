package Serveur;

import java.io.*;
import java.net.*;
import java.net.InetAddress;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServicePartie implements Runnable {

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
                System.out.println("La socket est connectée ? : " + acceptor.isConnected());
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
                    sendDeplacementFantome(f);
                }

            };

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(task, 40, 30, TimeUnit.SECONDS);

            while (partie.getFantomes().size() > 0 && partie.getJoueurs().size() > 0) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    for (SocketChannel s : ssc) {
                        if (key.isReadable()) {
                            readAction(s, ssc.indexOf(s));
                            partie.printFant();
                        }
                    }
                }

            }
            if (partie.getFantomes().size() == 0) {
                sendFinGame();
            }

        } catch (IOException | InterruptedException e) {

            e.printStackTrace();
        }

    }

    // envoi du welcome [WELCO␣m␣h␣w␣f␣ip␣port***]
    public void sendWelcome(OutputStream os) throws IOException {
        String ip = partie.getIpString();
        String portMulti = partie.getPortMultiString();

        os.write(("WELCO " + partie.getId() + " " + partie.getLabyrinthe().getH() + " " + partie.getLabyrinthe().getW()
                + " " + partie.getNbFantome() + " " + ip + " " + portMulti + "***").getBytes(),
                0, (5 + 1 + 2 + 2 + 1 + 15 + 4 + 3 + 6));
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
        while (!(new String(buf.array())).equals("*")) {
            mess.append(new String(buf.array()));
            buf = ByteBuffer.allocate(1);
            s.read(buf);
        }
        buf = ByteBuffer.allocate(1);
        s.read(buf);
        buf = ByteBuffer.allocate(1);
        s.read(buf);

        System.out.println("message : " + mess.toString());
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

    // multidiffuser le score d'un joueur lors de la prise d'un fantome
    public void sendUpdateScoreJoueur(Joueur j) {
        // SCORE id p x y+++
        String mess = "SCORE " + j.getId() + " " + j.getPPoint() + " " + j.getPosX() + " " + j.getPosY() + "+++";
    }

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
    public void sendFinGame() {
        // ENDGA id p+++
        Joueur j = getBestPlayer();
        String mess = "ENDGA " + j.getId() + " " + j.getPPoint() + "+++";
    }

    // multidiffuser le deplacement d'un fantome
    public void sendDeplacementFantome(Fantome f) {
        // GHOST x y+++
        String mess = "GHOST " + f.getPosX() + " " + f.getPosY() + "+++";
    }

    // multidiffuser un message pour tous les joueurs
    public void sendMessageForAll() {
        // MESSA id mess+++
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
    public void readAction(SocketChannel s, int pos) throws IOException, InterruptedException {
        Joueur joueur = joueurs.get(pos);

        ByteBuffer buf = ByteBuffer.allocate(5);
        s.read(buf);
        String action = new String(buf.array());

        // System.out.println("action : " + action);

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
                    joueur.setPoint(joueur.getPoint() + fant);
                    sendMoveFant(s, joueur.getPosX(), joueur.getPosY(),
                            joueur.getPPoint(), joueur.getId());
                    sendUpdateScoreJoueur(joueur);
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
                    sendUpdateScoreJoueur(joueur);
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
                    sendUpdateScoreJoueur(joueur);
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
                    sendUpdateScoreJoueur(joueur);
                } else {
                    sendMove(s, joueur.getPosX(), joueur.getPosY());
                }
                break;

            case "IQUIT":
                sendBye(s);
                break;

            case "GLIS?":
                ByteBuffer buff = ByteBuffer.allocate(3);
                s.read(buff);
                sendListJoueur(s, joueurs);
                break;

            case "MALL?": // utiliser fonction sendMessageForAll quand ca sera fonctionnel
                // lire le message jusqu'aux 3* (max 200 char) et le stocker

                mess = getMess(s);
                // multi diffuser sur l'adresse + port de la partie
                String en = "MESSA " + joueur.getId() + " " + mess + "+++";
                ByteBuffer buffMC = ByteBuffer.wrap(en.getBytes());

                System.out.println("ip de partie : " + partie.getIp());
                System.out.println("port multi : " + partie.getPortMulti());
                InetSocketAddress ia = new InetSocketAddress(partie.getIp(), partie.getPortMulti());
                DatagramChannel channel = DatagramChannel.open();
                channel.bind(null);
                channel.send(buffMC, ia);

                // paquet = new DatagramPacket(dat, dat.length,
                // InetAddress.getByName(partie.getIp()), partie.getPortMulti());
                // dso.send(paquet);

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
            case "XTLX?": // triche pour labyrinthe
                buf = ByteBuffer.allocate(3);
                s.read(buf);
                sendLaby(s);
                break;
            case "XTFX?": // triche pour fantome
                buf = ByteBuffer.allocate(3);
                s.read(buf);
                sendFant(s);
                break;
            default:
                clearIS(s);
                buf = ByteBuffer.wrap(("DUNNO***").getBytes(), 0, (8));
                s.write(buf);
                break;
        }

    }

    // clear la lecture jusqu'aux ***
    public void clearIS(SocketChannel iso) throws IOException {
        System.out.println("dans clear IS");
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
