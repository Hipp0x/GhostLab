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

                System.out
                        .println("Need an adresse and a port to start.");
                System.exit(0);

            } else {

                ServerSocketChannel server = ServerSocketChannel.open();
                server.bind(new InetSocketAddress(args[0], Integer.parseInt(args[1])));

                ServiceConnexion connexion = new ServiceConnexion(server, parties);
                Thread t = new Thread(connexion);
                t.start();
            }

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid port.");
        } catch (UnresolvedAddressException e) {
            System.out.println("Please enter a valid adresse.");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
