package Serveur;

import java.net.*;
import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ServiceJoueur implements Runnable {

    private final SocketChannel socketCh;
    private final Socket socket;

    private Partie game;
    private Joueur player;
    private static ArrayList<Partie> parties;

    public ServiceJoueur(SocketChannel s, ArrayList<Partie> parties) {
        this.socketCh = s;
        this.socket = s.socket();
        ServiceJoueur.parties = parties;
    }

    public void run() {
        try {
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            boolean exit = false;

            printAvailableGames(os);

            do {
                optionsNotInGame(os, is);
                exit = optionsInGame(os, is);
            } while (!exit);

            Partie ready = null;
            synchronized ((Object) parties) {
                for (Partie p : parties) {

                    if (p.peutDemarer()) {
                        ready = p;
                        // initialiser le nb de fantome
                        int nbjoueur = p.getNbJoueurs();
                        int nbFant = Math.min(15, nbjoueur * 3);
                        p.setFantome(nbFant);

                        // lancer le thread de la partie
                        ServicePartie partie = new ServicePartie(p);
                        Thread t2 = new Thread(partie);
                        t2.start();

                        System.out.println("The game n°" + p.getId() + " started.");
                    }
                }

                if (ready != null) {
                    parties.remove(ready);
                }

            }

            // Loop for the player to register into a game or create one.

        } catch (Exception e) {
            System.out.println(e);
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

    public boolean optionsInGame(OutputStream os, InputStream is) throws IOException {
        String[] infos;
        int gameId;
        Partie p;
        String s;
        byte[] request;
        while (true) {
            String action = getAction(is, os);

            switch (action) {
                case "UNREG":
                    System.out.println("//recv UNREG from " + player.getId());
                    gameId = game.getId();
                    removeJoueur(gameId, player);
                    s = "UNROK m***";
                    request = s.getBytes();
                    request[6] = (byte) gameId;
                    os.write(request);
                    os.flush();
                    System.out.println("//send UNROK to " + gameId);
                    clearIS(is);
                    return false;
                case "SIZE?":
                    System.out.println("//recv SIZE? from " + player.getId());
                    infos = getInfos(2, is);
                    gameId = Integer.parseInt(infos[0]);
                    p = findGame(gameId);
                    byte[] h = intToLittleEndian(game.getLabyrinthe().getH());
                    byte[] w = intToLittleEndian(game.getLabyrinthe().getW());
                    if (p != null) {

                        s = "SIZE! m hh ww***";
                        request = s.getBytes();
                        request[6] = (byte) gameId;
                        request[8] = h[1];
                        request[9] = h[0];
                        request[11] = w[1];
                        request[12] = w[0];

                        os.write(request);
                        os.flush();

                        System.out.println("//send SIZE! for Game n°" + gameId + "'s labyrinth");
                    } else {
                        dunno(os);
                    }
                    break;
                case "LIST?":
                    System.out.println("//recv LIST? from " + player.getId());
                    infos = getInfos(2, is);
                    gameId = Integer.parseInt(infos[0]);
                    p = findGame(gameId);
                    if (p != null) {
                        s = "LIST! i n***";
                        request = s.getBytes();
                        request[6] = (byte) gameId;
                        request[8] = (byte) p.getNbJoueurs();
                        os.write(request);
                        os.flush();
                        System.out.println("//send LIST!");
                        for (Joueur j : p.getJoueurs()) {
                            os.write(("PLAYR " + j.getId() + "***").getBytes());
                            os.flush();
                            System.out.println("//send PLAYR " + j.getId());
                        }
                    } else {
                        dunno(os);
                    }
                    break;
                case "GAME?":
                    System.out.println("//recv GAME? from " + player.getId());
                    printAvailableGames(os);
                    clearIS(is);
                    break;
                case "START":
                    System.out.println("//recv START from " + player.getId());
                    joueurReady();
                    clearIS(is);
                    return true;
                case "NEWPL":
                    System.out.println("//recv NEWPL from " + player.getId());
                    os.write(("REGNO***").getBytes());
                    os.flush();
                    System.out.println("//send REGNO");
                    clearIS(is);
                    break;
                case "REGIS":
                    System.out.println("//recv REGIS from " + player.getId());
                    os.write(("REGNO***").getBytes());
                    os.flush();
                    System.out.println("//send REGNO");
                    clearIS(is);
                    break;
                default:
                    System.out.println("//recv " + action + " from " + player.getId());
                    System.out.println("Unknown action");
                    clearIS(is);
                    dunno(os);
                    break;
            }
        }
    }

    public void optionsNotInGame(OutputStream os, InputStream is) throws IOException {
        boolean good = false;
        int port;
        int gameId;
        String id;
        String[] infos;
        String s;
        byte[] request;
        do {
            String action = getAction(is, os);
            Partie p = null;
            switch (action) {
                case "NEWPL":
                    System.out.print("//recv NEWPL ");
                    infos = getInfos(0, is);
                    if (!verifyInfos(infos)) {
                        os.write(("REGNO***").getBytes(), 0, 8);
                        os.flush();
                        System.out.println();
                        System.out.println("//send REGNO");
                        break;
                    }
                    id = infos[0];
                    port = Integer.parseInt(infos[1]);
                    player = new Joueur(id, port, socket);
                    game = new Partie();
                    game.addJoueur(player);
                    synchronized ((Object) parties) {
                        parties.add(game);
                    }
                    System.out.println("from " + player.getId());

                    s = "REGOK m***";
                    request = s.getBytes();
                    request[6] = (byte) game.getId();
                    os.write(request);
                    os.flush();

                    System.out.println("//send REGOK for Game n°" + game.getId());
                    good = true;
                    break;
                case "REGIS":
                    System.out.print("//recv REGIS ");
                    infos = getInfos(1, is);
                    if (!verifyInfos(infos)) {
                        os.write(("REGNO***").getBytes());
                        os.flush();
                        System.out.println();
                        System.out.println("//send REGNO");
                        continue;
                    }
                    id = infos[0];
                    port = Integer.parseInt(infos[1]);
                    gameId = Integer.parseInt(infos[2]);
                    player = new Joueur(id, port, socket);
                    System.out.println("from " + player.getId());
                    synchronized ((Object) parties) {
                        for (Partie partie : parties) {
                            if (partie.getId() == gameId) {
                                partie.addJoueur(player);
                                game = partie;
                                good = true;
                                s = "REGOK m***";
                                request = s.getBytes();
                                request[6] = (byte) game.getId();
                                os.write(request);
                                os.flush();
                                System.out.println("//send REGOK for the game n°" + game);
                                break;
                            }
                        }
                    }
                    if (!good) {
                        os.write(("REGNO***").getBytes(), 0, 8);
                        os.flush();
                        System.out.println("//send REGNO");
                    }
                    break;
                case "GAME?":
                    System.out.println("//recv GAME?");
                    printAvailableGames(os);
                    clearIS(is);
                    break;
                case "SIZE?":
                    System.out.println("//recv SIZE?");
                    infos = getInfos(2, is);
                    gameId = Integer.parseInt(infos[0]);
                    p = findGame(gameId);
                    byte[] h = intToLittleEndian(game.getLabyrinthe().getH());
                    byte[] w = intToLittleEndian(game.getLabyrinthe().getW());
                    if (p != null) {

                        s = "SIZE! m hh ww***";
                        request = s.getBytes();
                        request[6] = (byte) gameId;
                        request[8] = h[1];
                        request[9] = h[0];
                        request[11] = w[1];
                        request[12] = w[0];

                        os.write(request);
                        os.flush();

                        System.out.println("//send SIZE! for Game n° " + gameId + "'s labyrinth.");
                    } else {
                        dunno(os);
                    }
                    break;
                case "LIST?":
                    System.out.println("//recv LIST?");
                    infos = getInfos(2, is);
                    gameId = Integer.parseInt(infos[0]);
                    p = findGame(gameId);
                    if (p != null) {
                        s = "LIST! i n***";
                        request = s.getBytes();
                        request[6] = (byte) gameId;
                        request[8] = (byte) p.getNbJoueurs();
                        os.write(request);
                        os.flush();
                        System.out.println("//send LIST!");
                        for (Joueur j : p.getJoueurs()) {
                            os.write(("PLAYR " + j.getId() + "***").getBytes());
                            os.flush();
                            System.out.println("//send PLAYR " + j.getId());
                        }
                    } else {
                        dunno(os);
                    }
                    break;
                default:
                    System.out.println("Unknown action : " + action);
                    clearIS(is);
                    dunno(os);
                    break;
            }

        } while (!good);
    }

    // affiche les parties disponibles
    public void printAvailableGames(OutputStream os) throws IOException {
        String s = "GAMES m***";
        byte[] request = s.getBytes();
        request[6] = (byte) parties.size();
        os.write(request);
        os.flush();

        System.out.println("//send GAMES " + parties.size());
        // Envoi de toutes les parties créées à l'utilisateur
        for (Partie p : parties) {
            s = "OGAME i n***";
            request = s.getBytes();
            request[6] = (byte) p.getId();
            request[8] = (byte) p.getNbJoueurs();
            os.write(request);
            os.flush();
            System.out.println("//send OGAME, for Game n°" + p.getId());
        }
    }

    // cherche la partie associée au gameID (null sinon)
    public Partie findGame(int gameID) {
        for (Partie p : parties) {
            if (p.getId() == gameID) {
                return p;
            }
        }
        return null;
    }

    // verifie les infos
    public boolean verifyInfos(String[] infos) {
        // Vérifie le port
        if (!(infos[1].length() == 4 && infos[1].matches("[0-9]+"))) {
            return false;
        }
        // Vérifie l'identifiant
        if (!(infos[0].length() == 8 && infos[0].matches("[a-zA-Z0-9]+"))) {
            return false;
        }
        // Vérifie le numéro de la partie si il y en a un
        if (infos.length == 3 && !infos[2].matches("[0-9]+")) {
            return false;
        }
        return true;
    }

    // recupere l'action
    public String getAction(InputStream iso, OutputStream os) throws IOException {
        byte[] buf = new byte[5];
        int r = iso.read(buf, 0, 5);
        if (!(r > 0)) {
            os.close();
            iso.close();
            socket.close();
        }
        return new String(buf);
    }

    // recupere les infos
    public String[] getInfos(int which, InputStream iso) throws IOException {
        switch (which) {
            case 0:
                byte[] create = new byte[17];
                readError(iso.read(create, 0, 17), socket);
                return ((new String(create)).substring(1, 14)).split(" ");
            case 1:
                String[] info = new String[3];
                byte[] join = new byte[15];
                readError(iso.read(join, 0, 15), socket);
                String infos = new String(join);
                String[] tmp = (infos.substring(1)).split(" ");

                // lire le m
                byte[] gameId = new byte[1];
                readError(iso.read(gameId, 0, 1), socket);

                int iD = new BigInteger(gameId).intValue();

                // lire ***
                byte[] star = new byte[3];
                readError(iso.read(star, 0, 3), socket);
                info[0] = tmp[0];
                info[1] = tmp[1];
                info[2] = String.valueOf(iD);
                return info;
            case 2:

                // lire lespace
                byte[] vide = new byte[1];
                readError(iso.read(vide, 0, 1), socket);

                byte[] gameID = new byte[1];
                readError(iso.read(gameID, 0, 1), socket);

                int id = new BigInteger(gameID).intValue();

                // lire ***
                byte[] stAr = new byte[3];
                readError(iso.read(stAr, 0, 3), socket);

                return new String[] { String.valueOf(id) };

        }
        return new String[] { "" };
    }

    //
    public String getGameId(BufferedReader br) throws IOException {
        char[] curr;
        StringBuilder gameID = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            curr = new char[1];
            readError(br.read(curr, 0, 1), socket);
            if (new String(curr).matches("[0-9]")) {
                gameID.append(new String(curr));
            } else {
                break;
            }
        }
        return new String(gameID);
    }

    // clear la lecture jusqu'aux ***
    public void clearIS(InputStream iso) throws IOException {
        byte[] trash = new byte[1];
        String r = new String(trash, StandardCharsets.UTF_8);
        while (!("*").equals(r)) {
            trash = new byte[1];
            readError(iso.read(trash, 0, 1), socket);
            r = new String(trash, StandardCharsets.UTF_8);
        }

        trash = new byte[1];
        readError(iso.read(trash, 0, 1), socket);

        trash = new byte[1];
        readError(iso.read(trash, 0, 1), socket);

    }

    // regarde si la lecture a fait une erreur
    public void readError(int readRet, Socket sock) throws IOException {
        if (!(readRet > 0)) {
            sock.close();
        }
    }

    // envoi dunno
    public void dunno(OutputStream os) throws IOException {
        System.out.println("//send DUNNO");
        os.write(("DUNNO***").getBytes(), 0, 8);
        os.flush();
    }

    // supprime un jouuer de la partie
    public void removeJoueur(int gameID, Joueur player) {
        for (Partie p : parties) {
            if (p.getId() == gameID) {
                p.removeJoueur(player);
                return;
            }
        }
    }

    //
    public void joueurReady() {
        for (Partie p : parties) {
            if (p.getId() == game.getId()) {
                for (Joueur j : p.getJoueurs()) {
                    if (j.getId() == player.getId()) {
                        j.switchReady();
                    }
                }
            }
        }
    }

}
