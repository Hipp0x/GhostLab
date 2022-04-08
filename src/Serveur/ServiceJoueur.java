package Serveur;
import java.net.*;
import java.io.*;
import java.lang.*;
import java.util.ArrayList;

public class ServiceJoueur implements Runnable{

    private final Socket socket;
    private Partie game;
    private Joueur player;
    private static  ArrayList<Partie> parties = new ArrayList<Partie>();

    public ServiceJoueur(Socket s){
        this.socket=s;
    }

    public void run(){
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            boolean good = false;
            String id;
            int port;
            int gameId = -1;
            String[] infos;

            //Loop for the player to register into a game or create one.
            do {

                printAvailableGames(pw);
                String action = getAction(br, pw);

                switch (action) {
                    case "NEWPL" -> {
                        infos = getInfos(0, br);
                        if (!verifyInfos(infos)) {
                            pw.print("REGNO***");
                            pw.flush();
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
                        pw.print("REGOK " + game.getId() + "***");
                        pw.flush();
                        good = true;
                    }

                    case "REGIS" -> {
                        infos = getInfos(1, br);
                        if (!verifyInfos(infos)) {
                            pw.print("REGNO***");
                            pw.flush();
                            continue;
                        }
                        id = infos[0];
                        port = Integer.parseInt(infos[1]);
                        gameId = Integer.parseInt(infos[2]);
                        player = new Joueur(id, port);
                        for (Partie p : parties) {
                            if (p.getId() == gameId) {
                                p.addJoueur(player);
                                game = p;
                                good = true;
                                pw.print("REGOK " + gameId + "***");
                                pw.flush();
                                break;
                            }
                        }
                        if (!good) {
                            pw.print("REGNO***");
                            pw.flush();
                        }
                    }
                    default -> dunno(pw);
                }

            }while(!good);

            while(true){
                String action = getAction(br,pw);

                switch (action) {
                    case "UNREG" -> {
                        gameId = game.getId();
                        trashAsterisks(br);
                        game.removeJoueur(player);
                        pw.print("UNROK " + gameId + "***");
                        pw.flush();
                    }
                    case "SIZE?" -> {

                    }
                    case "LIST?" ->{
                        infos = getInfos(2,br);
                        gameId = Integer.parseInt(infos[0]);
                        for (Partie p : parties) {
                            if (p.getId() == gameId) {
                                pw.print("LIST! " + gameId + " " + p.getNbJoueurs() + "***");
                                pw.flush();
                                for (Joueur j : p.getJoueurs()){
                                    pw.print("PLAYR " + j.getId() + "***");
                                    pw.flush();
                                }
                                break;
                            }
                        }
                    }
                    case "GAME?" ->{
                        trashAsterisks(br);
                        printAvailableGames(pw);
                    }
                    default -> dunno(pw);

                }
                break;
            }

            br.close();
            pw.close();
            socket.close();
        }
        catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public void trashAsterisks(BufferedReader br) throws IOException {
        char[] trash = new char[3];
        readError(br.read(trash,0,3), socket);
    }

    public void printAvailableGames(PrintWriter pw){
        pw.print("GAMES "+parties.size()+"***");
        pw.flush();
        //Envoi de toutes les parties créées à l'utilisateur
        for(Partie p : parties){
            pw.print("OGAME " + p.getId() + " " + p.getNbJoueurs() + "***");
            pw.flush();
        }
    }

    public boolean verifyInfos(String[] infos){
        System.out.println(infos);
        //Vérifie le port
        if( !(infos[1].length() == 4 && infos[1].matches("[0-9]+")) ){
            return false;
        }
        System.out.println("Past Port :" );
        //Vérifie l'identifiant
        if( !(infos[0].length() == 8 && infos[0].matches("[a-zA-Z0-9]+")) ){
            return false;
        }
        System.out.println(infos.length);
        //Vérifie le numéro de la partie si il y en a un
        if( infos.length == 3 && !infos[1].matches("[0-9]+") ){
            return false;
        }
        return true;
    }

    public String getAction(BufferedReader br, PrintWriter pw) throws IOException {
        char[] buf = new char[5];
        int r = br.read(buf,0,5);
        if(!(r > 0)){
            pw.close();
            br.close();
            socket.close();
        }
        return new String(buf);
    }

    public String[] getInfos(int which, BufferedReader br)throws IOException{
        switch (which) {
            case 0 -> {
                char[] create = new char[17];
                readError(br.read(create, 0, 17), socket);
                System.out.println(new String(create));
                return ((new String(create)).substring(1, 14)).split(" ");
            }
            case 1 -> {
                String[] info = new String[3];
                char[] join = new char[15];
                readError(br.read(join, 0, 15), socket);
                String[] tmp = ((new String(join)).substring(1, 14)).split(" ");
                trashAsterisks(br);
                info[0] = tmp[0];
                info[1] = tmp[1];
                info[2] = getGameId(br);
                return info;
            }
            case 2 -> {
                char[] trash = new char[1];
                readError(br.read(trash,0,1), socket);
                String gameId = getGameId(br);
                trashAsterisks(br);
                return new String[]{gameId};
            }
        }
        return new String[]{""};
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


    public void readError(int readRet, Socket sock) throws IOException {
        if(!(readRet > 0)){
            sock.close();
        }
    }

    public void dunno(PrintWriter pw){
        pw.print("DUNNO***");
        pw.flush();
    }

    public static void main(String[] args){
    }

}

