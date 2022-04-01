#include "fonctions.h"
#include "actionsBefore.h"

#include <sys/socket.h>
#include <netinet/in.h>
#include <stdbool.h>

/*
-----------VARIABLES-----------
*/

char port[4];
char identifiant[8];

/*
-----------FONCTIONS-----------
*/

// les actions que peut faire le joueur avant le debut d'une partie
bool actionAvantPartie(int socketTCP, char *choix)
{
    char a = choix[0];
    switch (a)
    {
    case 'c':; // creer une partie
        creerPartie(socketTCP, identifiant, port);
        return false;
        break;

    case 'r':; // rejoindre une partie
        rejoindrePartie(socketTCP, identifiant, port);
        return false;
        break;

    case 'd':; // desinscription d'une partie
        desinscription(socketTCP);
        return false;
        break;

    case 't':; // taille labyrinthe de la partie m
        tailleLaby(socketTCP);
        return false;
        break;

    case 'j':; // liste joueurs de la partie partie m
        listeJoueurs(socketTCP);
        return false;
        break;

    case 'p':; // liste parties qui n'ont pas encore commencé
        listeParties(socketTCP);
        return false;
        break;

    case 's':; // start
        start(socketTCP);
        return true;
        break;

    default:
        fprintf(stdout, "Ce n'est pas correct.\n");
        return false;
        break;
    }
}

int main()
{

    //
    struct sockaddr_in address_sock;
    address_sock.sin_family = AF_INET;
    address_sock.sin_port = htons(5555);
    address_sock.sin_addr.s_addr = htonl(INADDR_ANY);

    // socket tcp serveur
    int socketTCP = socket(PF_INET, SOCK_STREAM, 0);

    // socket udp client
    int socketUDP = socket(PF_INET, SOCK_DGRAM, 0);

    // connexion au serveur
    int sock_client = connect(socketTCP, (struct sockaddr *)&address_sock, sizeof(struct sockaddr_in));
    connectError(sock_client);

    // reception de [GAMES␣n***]
    char buf[10];
    recvError(recv(socketTCP, buf, 10, 0));
    uint8_t n = atoi(&buf[6]);
    fprintf(stdout, "GAMES %d \n", n);

    // reception de n message [OGAME␣m␣s***]
    recupereGames(n, socketTCP);

    bool ans = false;
    while (!ans)
    {
        // lecture du choix du joueur
        fprintf(stdout, "Que voulez-vous faire ?\n");
        fprintf(stdout, "c (create), r (rejoindre), d (desinscrire), t (taille), \n j (liste joueurs), p (liste parties), s (start).\n");
        // action

        char *line = NULL;
        ssize_t len = 0;
        ssize_t lineSize = 0;
        lineSize = getline(&line, &len, stdin);
        ans = actionAvantPartie(socketTCP, line);
        free(line);
    }
}
