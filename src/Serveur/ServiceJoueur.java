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
            int id;
            int port;
            int gameId = -1;
            String[] infos;
            do {
            pw.print("GAMES "+parties.size()+"***");
            pw.flush();
            //Envoi de toutes les parties créées à l'utilisateur
            for(Partie p : parties){
                pw.print("OGAME " + p.getId() + " " + p.getNbJoueurs() + "***");
                pw.flush();
            }

            String mess=br.readLine();
            if(mess.length() < 12){
                dunno(pw);
                continue;
            }
            String info = mess.substring(0,mess.length() - 3);
            //Séparation du message reçu en Action || id du Joueur || port du joueur (|| id de la partie à rejoindre) + ***
            infos = info.split(" ");


                try {
                    id = Integer.parseInt(infos[1]);
                    port = Integer.parseInt(infos[2]);
                    if(infos.length == 4){
                        gameId = Integer.parseInt(infos[3]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("pd");
                    pw.print("REGNO***");
                    pw.flush();
                    continue;
                }

                Joueur player = new Joueur(id,port);
                if(infos[0].equals("NEWPL")){
                    if(id == -1 || port == -1){
                        System.out.println("Problème de cerveau");
                    }

                    Partie game = new Partie();
                    game.addJoueur(player);
                    synchronized ((Object)parties) {
                        parties.add(game);
                    }
                    pw.print("REGOK "+ game.getId()+"***");
                    pw.flush();
                    good = true;
                }else if(infos[0].equals("REGIS")){

                    for(Partie p : parties){
                        System.out.println(p.getId());
                        if(p.getId() == gameId){
                            p.addJoueur(player);
                            p.incrNbJoueurs();
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

            br.close();
            pw.close();
            socket.close();
        }
        catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public void dunno(PrintWriter pw){
        pw.print("DUNNO***");
        pw.flush();
    }

}

