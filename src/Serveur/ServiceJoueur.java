package Serveur;

import java.net.*;
import java.io.*;
import java.lang.*;
import java.util.ArrayList;

public class ServiceJoueur implements Runnable {

    private final Socket socket;
    private Partie game;
    private Joueur player;
    private static ArrayList<Partie> parties;

    public ServiceJoueur(Socket s, ArrayList<Partie> parties) {
        this.socket = s;
        this.parties = parties;
    }

    public void run(){
        try{
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            boolean exit = false;

            do{
                optionsNotInGame(os,is);
                exit = optionsInGame(os,is);
            }while(!exit);

            ServiceLancementPartie partie = new ServiceLancementPartie(parties);
            Thread t2 = new Thread(partie);
            t2.start();



            // Loop for the player to register into a game or create one.



        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public boolean optionsInGame(OutputStream os, InputStream is) throws IOException {
        String[] infos;
        int gameId;
        Partie p;
        while(true){
            String action = getAction(is,os);

            switch (action) {
                case "UNREG":
                    gameId = game.getId();
                    removeJoueur(gameId, player);
                    os.write(("UNROK " + gameId + "***").getBytes(), 0, 10);
                    os.flush();
                    clearIS(is);
                    return false;
                case "SIZE?":
                    infos = getInfos(2,is);
                    gameId = Integer.parseInt(infos[0]);
                    p = findGame(gameId);
                    if(p != null){
                        os.write(("SIZE! "+ gameId + " " + game.getLabyrinthe().getH() + " " + game.getLabyrinthe().getW() + "***").getBytes(),0,16);
                        os.flush();
                    }else{
                        dunno(os);
                    }
                    clearIS(is);
                    break;
                case "LIST?":
                    infos = getInfos(2, is);
                    gameId = Integer.parseInt(infos[0]);
                    p = findGame(gameId);
                    if (p != null) {
                        os.write(("LIST! " + gameId + " " + p.getNbJoueurs() + "***").getBytes(), 0, 12);
                        os.flush();
                        for (Joueur j : p.getJoueurs()) {
                            os.write(("PLAYR " + j.getId() + "***").getBytes(), 0, 17);
                            os.flush();
                        }
                    }else{
                        dunno(os);
                    }
                    clearIS(is);
                    break;
                case "GAME?":
                    printAvailableGames(os);
                    clearIS(is);
                    break;
                case "START":
                    joueurReady();
                    clearIS(is);
                    return true;
                default:
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
        do {

            printAvailableGames(os);
            String action = getAction(is, os);
            Partie p;
            switch (action) {
                case "NEWPL":
                    infos = getInfos(0, is);
                    if (!verifyInfos(infos)) {
                        os.write(("REGNO***").getBytes(),0,8);
                        os.flush();
                        break;
                    }
                    id = infos[0];
                    port = Integer.parseInt(infos[1]);
                    player = new Joueur(id, port,socket);
                    game = new Partie();
                    game.addJoueur(player);
                    synchronized ((Object) parties) {
                        parties.add(game);
                    }
                    os.write(("REGOK " + game.getId() + "***").getBytes(),0,10);
                    os.flush();
                    good = true;
                    clearIS(is);
                    break;
                case "REGIS":
                    infos = getInfos(1, is);
                    if (!verifyInfos(infos)) {
                        os.write(("REGNO***").getBytes());
                        os.flush();
                        continue;
                    }
                    id = infos[0];
                    port = Integer.parseInt(infos[1]);
                    System.out.println(infos[2]);
                    gameId = Integer.parseInt(infos[2]);
                    player = new Joueur(id, port,socket);
                    synchronized ((Object)parties) {
                        for (Partie partie : parties) {
                            if (partie.getId() == gameId) {
                                partie.addJoueur(player);
                                game = partie;
                                good = true;
                                os.write(("REGOK " + gameId + "***").getBytes(), 0, 10);
                                os.flush();
                                break;
                            }
                        }
                    }
                    if (!good) {
                        os.write(("REGNO***").getBytes(),0,8);
                        os.flush();
                    }
                    clearIS(is);
                    break;
                case "GAME?":
                    printAvailableGames(os);
                    clearIS(is);
                    break;
                case "SIZE?":
                    infos = getInfos(2, is);
                    gameId = Integer.parseInt(infos[0]);
                    p = findGame(gameId);
                    if (p != null) {
                        os.write(("SIZE! " + gameId + " " + p.getLabyrinthe().getH() + " " + p.getLabyrinthe().getW() + "***").getBytes(), 0, 16);
                    }else{
                        dunno(os);
                    }
                    clearIS(is);
                    break;
                case "LIST?":
                    infos = getInfos(2, is);
                    gameId = Integer.parseInt(infos[0]);
                    p = findGame(gameId);
                    if (p != null) {
                        os.write(("LIST! " + gameId + " " + p.getNbJoueurs() + "***").getBytes(), 0, 12);
                        os.flush();
                        for (Joueur j : p.getJoueurs()) {
                            os.write(("PLAYR " + j.getId() + "***").getBytes(), 0, 17);
                            os.flush();
                        }
                    } else {
                        dunno(os);
                    }
                    clearIS(is);
                    break;
                default:
                    clearIS(is);
                    dunno(os);
                    break;
            }

        } while (!good);
    }

    public void trashAsterisks(BufferedReader br) throws IOException {
        char[] trash = new char[3];
        readError(br.read(trash, 0, 3), socket);
    }

    public void printAvailableGames(OutputStream os) throws IOException {
        os.write(("GAMES "+parties.size()+"***").getBytes(),0,10);
        os.flush();
        //Envoi de toutes les parties créées à l'utilisateur
        for(Partie p : parties){
            os.write(("OGAME " + p.getId() + " " + p.getNbJoueurs() + "***").getBytes(),0,12);
            os.flush();
        }
    }

    public Partie findGame(int gameID){
        for (Partie p : parties) {
            if (p.getId() == gameID) {
                return p;
            }
        }
        return null;
    }

    public boolean verifyInfos(String[] infos){
        //Vérifie le port
        if( !(infos[1].length() == 4 && infos[1].matches("[0-9]+")) ){
            return false;
        }
        //Vérifie l'identifiant
        if( !(infos[0].length() == 8 && infos[0].matches("[a-zA-Z0-9]+")) ){
            return false;
        }
        //Vérifie le numéro de la partie si il y en a un
        if( infos.length == 3 && !infos[2].matches("[0-9]+") ){
            return false;
        }
        return true;
    }

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

    public String[] getInfos(int which, InputStream iso)throws IOException{
        switch (which) {
            case 0:
                byte[] create = new byte[17];
                readError(iso.read(create, 0, 17), socket);
                clearIS(iso);
                return ((new String(create)).substring(1, 14)).split(" ");
            case 1:
                String[] info = new String[3];
                byte[] join = new byte[14];
                readError(iso.read(join, 0, 14), socket);
                String infos = new String(join);
                String[] tmp = (infos.substring(1)).split(" ");
                int trash = iso.read();
                int gameId = iso.read();
                System.out.println("Valeur originale : " + gameId);
                System.out.println("Valeur après conversion : " + (gameId & 0xFF));
                info[0] = tmp[0];
                info[1] = tmp[1];
                info[2] = Integer.toString(gameId);
                clearIS(iso);
                return info;
            case 2:
                int trashh = iso.read();
                int gameID = iso.read();
                clearIS(iso);
                return new String[] { Integer.toString(gameID) };
        }
        return new String[] { "" };
    }

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

    public void clearIS(InputStream iso) throws IOException {
        while (iso.available() > 0) {
            iso.read();
        }
    }


    public void readError(int readRet, Socket sock) throws IOException {
        if (!(readRet > 0)) {
            sock.close();
        }
    }

    public void dunno(OutputStream os) throws IOException {
        os.write(("DUNNO***").getBytes(), 0, 8);
        os.flush();
    }

    public void removeJoueur(int gameID, Joueur player){
        for(Partie p : parties){
            if(p.getId() == gameID){
                p.removeJoueur(player);
                return;
            }
        }
    }

    public void joueurReady(){
        for(Partie p : parties){
            if(p.getId() == game.getId()){
                for(Joueur j : p.getJoueurs()){
                    if(j.getId() == player.getId()){
                        j.switchReady();
                    }
                }
            }
        }
    }

}
