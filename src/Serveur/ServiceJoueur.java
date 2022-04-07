package Serveur;
import java.net.*;
import java.io.*;
import java.lang.*;
import java.util.ArrayList;

public class ServiceJoueur implements Runnable{

    private final Socket socket;
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
                pw.print("GAMES "+parties.size()+"***");
                pw.flush();
                //Envoi de toutes les parties créées à l'utilisateur
                for(Partie p : parties){
                    pw.print("OGAME " + p.getId() + " " + p.getNbJoueurs() + "***");
                    pw.flush();
                }


                String action = getAction(br, pw);

                Joueur player;
                if(action.equals("NEWPL")){
                    infos = getInfos(0, br);
                    if(!verifyInfos(infos)){
                        pw.print("REGNO***");
                        pw.flush();
                        continue;
                    }
                    id = infos[0];
                    port = Integer.parseInt(infos[1]);
                    player = new Joueur(id, port);
                    Partie game = new Partie();
                    game.addJoueur(player);
                    synchronized ((Object)parties) {
                        parties.add(game);
                    }
                    pw.print("REGOK "+ game.getId()+"***");
                    pw.flush();
                    good = true;
                }else if(action.equals("REGIS")){
                    infos = getInfos(1, br);
                    if(!verifyInfos(infos)){
                        pw.print("REGNO***");
                        pw.flush();
                        continue;
                    }
                    id = infos[0];
                    port = Integer.parseInt(infos[1]);
                    gameId = Integer.parseInt(infos[2]);
                    player = new Joueur(id, port);

                    for(Partie p : parties){
                        if(p.getId() == gameId){
                            p.addJoueur(player);
                            good = true;
                            pw.print("REGOK "+ gameId +"***");
                            pw.flush();
                            break;
                        }
                    }
                    if(!good) {
                        pw.print("REGNO***");
                        pw.flush();
                    }
                }else{
                    dunno(pw);
                }

            }while(!good);

            while(true){

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
        if(which == 0) {
            char[] create = new char[17];
            readError(br.read(create, 0, 17), socket);
            System.out.println(new String(create));
            return ((new String(create)).substring(1, 14)).split(" ");
        }else if(which == 1){
            char[] create = new char[19];
            readError(br.read(create, 0, 19), socket);
            return ((new String(create)).substring(1, 16)).split(" ");
        }
        return new String[]{""};
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

