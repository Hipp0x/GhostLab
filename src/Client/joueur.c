#include "headers/fonctions.h"
#include "headers/actionsBefore.h"

#include <sys/socket.h>
#include <netinet/in.h>
#include <stdbool.h>

/*
-----------VARIABLES-----------
*/

char port[4] = "5467";
char identifiant[8] = "pseudo00";
bool inscrit;

/*
-----------FONCTIONS-----------
*/

// les actions que peut faire le joueur avant le debut d'une partie
bool actionAvantPartie(int socketTCP, char *ch)
{
    char *sep = " ";
    char *choix = strtok(ch, sep);
    printf("char : %s\n", choix);
    uint8_t num;
    switch (choix[0])
    {
    case 'c':; // creer une partie
        creerPartie(socketTCP, identifiant, port);
        inscrit = true;
        return false;
        break;

    case 'r':; // rejoindre une partie
        choix = strtok(NULL, sep);
        printf("num : %s\n", choix);
        num = atoi(choix);
        rejoindrePartie(socketTCP, identifiant, port, num);
        inscrit = true;
        return false;
        break;

    case 'd':; // desinscription d'une partie
        desinscription(socketTCP);
        return false;
        break;

    case 't':; // taille labyrinthe de la partie m
        choix = strtok(NULL, sep);
        printf("num : %s\n", choix);
        num = atoi(choix);
        tailleLaby(socketTCP, num);
        return false;
        break;

    case 'j':; // liste joueurs de la partie partie m
        choix = strtok(NULL, sep);
        printf("num : %s\n", choix);
        num = atoi(choix);
        listeJoueurs(socketTCP, num);
        return false;
        break;

    case 'p':; // liste parties qui n'ont pas encore commencé
        listeParties(socketTCP);
        return false;
        break;

    case 's':; // start
        if (inscrit)
        {
            start(socketTCP);
            return true;
        }
        else
        {
            fprintf(stdout, "Vous ne pouvez faire ca !\n");
            return false;
        }
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
    address_sock.sin_port = htons(5621);
    address_sock.sin_addr.s_addr = htonl(INADDR_ANY);

    // socket tcp serveur
    int socketTCP = socket(PF_INET, SOCK_STREAM, 0);

    // socket udp client
    int socketUDP = socket(PF_INET, SOCK_DGRAM, 0);

    // connexion au serveur
    int sock_client = connect(socketTCP, (struct sockaddr *)&address_sock, sizeof(struct sockaddr_in));
    connectError(sock_client);

    // reception de [GAMES␣n***]
    size_t t = 9 + sizeof(uint8_t);
    char buf[t];
    recvError(recv(socketTCP, buf, t, 0));
    printf("%s\n", buf);
    uint8_t n = atoi(&buf[6]);
    fprintf(stdout, "GAMES %d \n", n);

    // reception de n message [OGAME␣m␣s***]
    recupereGames(n, socketTCP);

    bool ans = false;
    inscrit = false;
    while (!ans)
    {
        // lecture du choix du joueur
        fprintf(stdout, "Que voulez-vous faire ?\n");
        fprintf(stdout, "c (create), r (rejoindre) x, d (desinscrire), t (taille) x, j (liste joueurs) x, p (liste parties), s (start).\n");
        fprintf(stdout, "avec x = num partie, si necessaire.\n");
        // action

        char *line = NULL;
        ssize_t len = 0;
        ssize_t lineSize = 0;
        lineSize = getline(&line, &len, stdin);
        ans = actionAvantPartie(socketTCP, line);
        free(line);
    }
}
