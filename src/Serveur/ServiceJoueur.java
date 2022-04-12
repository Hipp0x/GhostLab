package Serveur;

import java.net.*;
import java.io.*;
import java.lang.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ServiceJoueur implements Runnable {

    private final Socket socket;
    private Partie game;
    private Joueur player;
    private static ArrayList<Partie> parties = new ArrayList<Partie>();

    public ServiceJoueur(Socket s) {
        this.socket = s;
    }

    public void run(){
        try{
            InputStream iso = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            boolean good = false;
            String id;
            int port;
            int gameId = -1;
            String[] infos;

            // Loop for the player to register into a game or create one.
            do {

                printAvailableGames(os);
                String action = getAction(iso, os);

                switch (action) {
                    case "NEWPL":
                        infos = getInfos(0, iso);
                        if (!verifyInfos(infos)) {
                            os.write(("REGNO***").getBytes());
                            os.flush();
                            continue;
                        }
                        id = infos[0];
                        port = Integer.parseInt(infos[1]);
                        player = new Joueur(id, port);
                        game = new Partie();
                        game.addJoueur(player);
                        synchronized ((Object) parties) {
                            parties.add(game);
                        }
                        os.write(("REGOK " + game.getId() + "***").getBytes());
                        os.flush();
                        good = true;
                        break;
                    case "REGIS":
                        infos = getInfos(1, iso);
                        if (!verifyInfos(infos)) {
                            os.write(("REGNO***").getBytes());
                            os.flush();
                            continue;
                        }
                        id = infos[0];
                        port = Integer.parseInt(infos[1]);
                        System.out.println(infos[2]);
                        gameId = Integer.parseInt(infos[2]);
                        player = new Joueur(id, port);
                        for (Partie p : parties) {
                            if (p.getId() == gameId) {
                                p.addJoueur(player);
                                game = p;
                                good = true;
                                os.write(("REGOK " + gameId + "***").getBytes());
                                os.flush();
                                break;
                            }
                        }
                        if (!good) {
                            os.write(("REGNO***").getBytes());
                            os.flush();
                        }
                        break;
                    case "GAME?":
                        clearIS(iso);
                        printAvailableGames(os);
                        break;
                    case "SIZE?":
                        break;
                    case "LIST?":
                        infos = getInfos(2, iso);
                        gameId = Integer.parseInt(infos[0]);
                        for (Partie p : parties) {
                            if (p.getId() == gameId) {
                                os.write(("LIST! " + gameId + " " + p.getNbJoueurs() + "***").getBytes(), 0, 12);
                                os.flush();
                                for (Joueur j : p.getJoueurs()) {
                                    os.write(("PLAYR " + j.getId() + "***").getBytes(),0,17);
                                    os.flush();
                                }
                                break;
                            }
                        }
                        break;
                    default:
                        dunno(os);
                        break;
                }

            } while (!good);

            while(true){
                String action = getAction(iso,os);

                switch (action) {
                    case "UNREG":
                        gameId = game.getId();
                        clearIS(iso);
                        game.removeJoueur(player);
                        os.write(("UNROK " + gameId + "***").getBytes(), 0, 10);
                        os.flush();
                        break;
                    case "SIZE?":
                        break;
                    case "LIST?":
                        infos = getInfos(2, iso);
                        gameId = Integer.parseInt(infos[0]);
                        for (Partie p : parties) {
                            if (p.getId() == gameId) {
                                os.write(("LIST! " + gameId + " " + p.getNbJoueurs() + "***").getBytes(), 0, 12);
                                os.flush();
                                for (Joueur j : p.getJoueurs()) {
                                    os.write(("PLAYR " + j.getId() + "***").getBytes(),0,17);
                                    os.flush();
                                }
                                break;
                            }
                        }
                        break;
                    case "GAME?":
                        clearIS(iso);
                        printAvailableGames(os);
                        break;
                    case "START":
                        boolean start = false;
                        while(!start){
                            synchronized ((Object)parties) {
                                for (Partie p : parties) {
                                    if (p.getId() == gameId) {
                                        start = true;
                                        for (Joueur j : p.getJoueurs()) {
                                            if (!j.isReady()) {
                                                start = false;
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        dunno(os);
                        break;
                }
                break;
            }

            iso.close();
            os.close();
            socket.close();
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public void trashAsterisks(BufferedReader br) throws IOException {
        char[] trash = new char[3];
        readError(br.read(trash, 0, 3), socket);
    }

    public void printAvailableGames(OutputStream os) throws IOException {

        os.write(("GAMES "+parties.size()+"***").getBytes(),0,10);
        os.flush();
        byte[] game = new byte[12];
        //Envoi de toutes les parties créées à l'utilisateur
        for(Partie p : parties){
            os.write(("OGAME " + p.getId() + " " + p.getNbJoueurs() + "***").getBytes(),0,12);
            os.flush();
        }
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
        int r = iso.read(buf,0,5);
        if(!(r > 0)){
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
                return new String[]{Integer.toString(gameID)};
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
        int ret = 0;
        while(iso.available() > 0){
            ret = iso.read();
        }
    }


    public void readError(int readRet, Socket sock) throws IOException {
        if (!(readRet > 0)) {
            sock.close();
        }
    }

    public void dunno(OutputStream os) throws IOException {
        os.write(("DUNNO***").getBytes(),0,8);
        os.flush();
    }

    public static void main(String[] args) {
    }

}
