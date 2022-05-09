package Serveur;

import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.ArrayList;

public class Serveur {

    private static ArrayList<Partie> parties = new ArrayList<Partie>();

    public static void main(String[] args) {

        try {

            if (args.length != 2) {

                System.out.println("Veuillez entrer une adresse et un port pour le serveur au lancement de la commande.");
                System.exit(0);

            } else {

                ServerSocketChannel server = ServerSocketChannel.open();
                server.bind(new InetSocketAddress(args[0], Integer.parseInt(args[1])));

                ServiceConnexion connexion = new ServiceConnexion(server, parties);
                Thread t = new Thread(connexion);
                t.start();
            }

        } catch (NumberFormatException e) {
            System.out.println("Veuillez rentrer un port correct pour le serveur.");
        } catch (UnresolvedAddressException e) {
            System.out.println("Veuillez rentrer une adresse correcte pour le serveur.");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
