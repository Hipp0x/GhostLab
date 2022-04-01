#include <sys/socket.h>
#include <netinet/in.h>
#include <errno.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stdbool.h>

/*
-----------VARIABLES-----------
*/

int v8 = 8;
int v12 = 12;
char port[4];
char identifiant[8];

/*
-----------ERREURS-----------
*/

// gestion d'erreur pour la connexion
void connectError(int ret)
{
    if (ret != 0)
    {
        fprintf(stdout, "Probleme de connexion : %s", strerror(errno));
        close(ret);
        exit(EXIT_FAILURE);
    }
    else
    {
        fprintf(stdout, "Connexion établie avec le serveur.\n");
    }
}

// gestion d'erreur pour l'envoi
void sendError(int ret)
{
    if (ret <= 0)
    {
        fprintf(stderr, "Probleme d'envoi : %s", strerror(errno));
        exit(EXIT_FAILURE);
    }
    else
    {
        fprintf(stdout, "Envoi de %d data.\n", ret);
    }
}

// gestion d'erreur pour la reception
void recvError(int ret)
{
    if (ret <= 0)
    {
        fprintf(stderr, "Probleme de reception : %s", strerror(errno));
        exit(EXIT_FAILURE);
    }
    else
    {
        fprintf(stdout, "Reception de %d data.\n", ret);
    }
}

/*
-----------FONCTIONS-----------
*/

// reception des n messages [OGAME␣m␣s***]
void receptionOGAME(uint8_t n, int socketTCP)
{
    for (uint8_t i = 0; i < n; i++)
    {
        // reception du message [OGAME␣m␣s***]
        char buf[v12];
        recvError(recv(socketTCP, buf, v12, 0));
        // affichage dans le terminal
        uint8_t m = atoi(&buf[6]);
        uint8_t s = atoi(&buf[8]);
        fprintf(stdout, "OGAME %d %d\n", m, s);
    }
}

// les actions que peut faire le joueur avant le debut d'une partie
bool actionAvantPartie(int socketTCP, int a)
{
    size_t taille = 6;
    char buf8[8];
    char buf10[10];
    char buf22[22];
    uint8_t m;
    switch (a)
    {
    case 1:; // creer une partie
        // envoi format [NEWPL␣id␣port***]
        memcpy(buf22, "NEWPL ", 6);
        taille = 6;
        memmove(buf22 + taille, identifiant, sizeof(identifiant));
        taille += sizeof(identifiant);
        memmove(buf22 + taille, " ", 1);
        taille += 1;
        memmove(buf22 + taille, port, sizeof(port));
        taille += sizeof(port);
        memmove(buf22 + taille, "***", 3);
        sendError(send(socketTCP, buf22, sizeof(buf22), 0));

        // reception [REGOK␣m***] / [REGNO***]

        return false;
        break;

    case 2:; // rejoindre une partie
        // envoi format [REGIS␣id␣port␣m***]
        memcpy(buf22, "REGIS ", 6);
        taille = 6;
        memmove(buf22 + taille, identifiant, sizeof(identifiant));
        taille += sizeof(identifiant);
        memmove(buf22 + taille, " ", 1);
        taille += 1;
        memmove(buf22 + taille, port, sizeof(port));
        taille += sizeof(port);
        memmove(buf22 + taille, "***", 3);
        sendError(send(socketTCP, buf22, sizeof(buf22), 0));

        // reception [REGOK␣m***] / [REGNO***]

        return false;
        break;

    case 3:; // desinscription d'une partie
        // envoi format [UNREG***]
        memcpy(buf8, "UNREG***", v8);
        sendError(send(socketTCP, buf8, sizeof(buf8), 0));
        // et reception  [UNROK␣m***] ou  [DUNNO***]
        return false;
        break;

    case 4:; // taille labyrinthe de la partie m
        // envoi format [SIZE?␣m***]
        fprintf(stdout, "Coucou\n");
        memcpy(buf10, "SIZE? ", 6);
        m = atoi("43");
        memmove(buf10 + taille, &m, sizeof(uint8_t));
        taille += sizeof(uint8_t);
        memmove(buf10 + taille, "***", 3);
        sendError(send(socketTCP, buf10, sizeof(buf10), 0));

        // et reception  [SIZE!␣m␣h␣w***] ou  [DUNNO***]
        return false;
        break;

    case 5:; // liste joueurs de la partie partie m
        // envoi format [LIST?␣m***]
        memcpy(buf10, "LIST? ", 6);
        m = atoi("43");
        memmove(buf10 + taille, &m, sizeof(uint8_t));
        taille += sizeof(uint8_t);
        memmove(buf10 + taille, "***", 3);
        sendError(send(socketTCP, buf10, sizeof(buf10), 0));

        // et reception   [LIST!␣m␣s***] puis s message  [PLAYR␣id***]  ou  [DUNNO***]
        return false;
        break;

    case 6:; // liste parties qui n'ont pas encore commencé
        //  [GAME?***]
        memcpy(buf8, "GAME?***", v8);
        sendError(send(socketTCP, buf8, sizeof(buf8), 0));
        // et reception  [GAMES␣n***] puis n message [OGAME␣m␣s***] ou  [DUNNO***]
        return false;
        break;

    case 7:; // start
        //[START***]
        memcpy(buf8, "START***", v8);
        sendError(send(socketTCP, buf8, sizeof(buf8), 0));
        return true;
        break;

    default:
        return true;
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
    receptionOGAME(n, socketTCP);

    bool ans = false;

    while (!ans)
    {
        // lecture du choix du joueur
        fprintf(stdout, "Que voulez-vous faire ?\n");
        // action
        ans = actionAvantPartie(socketTCP, 2);
    }
}
