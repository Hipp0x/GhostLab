#include "fonctions.h"
#include "actionsBefore.h"

void creerPartie(int socketTCP, char identifiant[], char port[])
{
    char buf22[22];
    memcpy(buf22, "NEWPL ", 6);
    size_t taille = 6;
    memmove(buf22 + taille, identifiant, strlen(identifiant));
    taille += strlen(identifiant);
    memmove(buf22 + taille, " ", 1);
    taille += 1;
    memmove(buf22 + taille, port, strlen(port));
    taille += strlen(port);
    memmove(buf22 + taille, "***", 3);
    sendError(send(socketTCP, buf22, strlen(buf22), 0));

    char buf5[6];
    recvError(recv(socketTCP, buf5, 5, 0));
    if (strcmp(buf5, "REGOK"))
    {
        char espace;
        recvError(recv(socketTCP, &espace, 1, 0));
        uint8_t m;
        recvError(recv(socketTCP, &m, sizeof(uint8_t), 0));
        fprintf(stdout, "Vous êtes dans la partie %d.\n", m);
    }
    else if (strcmp(buf5, "REGNO"))
    {
        fprintf(stdout, "Action incomprise.\n");
    }
    else
    {
        fprintf(stderr, "Fail.\n");
        close(socketTCP);
        exit(EXIT_FAILURE);
    }
}

void rejoindrePartie(int socketTCP, char identifiant[], char port[])
{
    // envoi format [REGIS␣id␣port␣m***]
    char buf22[22];
    memcpy(buf22, "REGIS ", 6);
    size_t taille = 6;
    memmove(buf22 + taille, identifiant, strlen(identifiant));
    taille += strlen(identifiant);
    memmove(buf22 + taille, " ", 1);
    taille += 1;
    memmove(buf22 + taille, port, strlen(port));
    taille += strlen(port);
    memmove(buf22 + taille, "***", 3);
    sendError(send(socketTCP, buf22, strlen(buf22), 0));

    char buf5[6];
    recvError(recv(socketTCP, buf5, 5, 0));
    if (strcmp(buf5, "REGOK"))
    {
        char espace;
        recvError(recv(socketTCP, &espace, 1, 0));
        uint8_t m;
        recvError(recv(socketTCP, &m, sizeof(uint8_t), 0));
        fprintf(stdout, "Vous êtes dans la partie %d.\n", m);
    }
    else if (strcmp(buf5, "REGNO"))
    {
        fprintf(stdout, "Action incomprise.\n");
    }
    else
    {
        fprintf(stderr, "Fail.\n");
        close(socketTCP);
        exit(EXIT_FAILURE);
    }
}

void desinscription(int socketTCP)
{
    // envoi format [UNREG***]
    char buf8[8];
    memcpy(buf8, "UNREG***", 8);
    sendError(send(socketTCP, buf8, sizeof(buf8), 0));

    // reception  [UNROK␣m***] ou  [DUNNO***]
    char buf5[6];
    recvError(recv(socketTCP, buf5, 5, 0));
    if (strcmp(buf5, "UNROK"))
    {
        char espace;
        recvError(recv(socketTCP, &espace, 1, 0));
        uint8_t m;
        recvError(recv(socketTCP, &m, sizeof(uint8_t), 0));
        fprintf(stdout, "Vous êtes desinscris de la partie %d.\n", m);
    }
    else if (strcmp(buf5, "DUNNO"))
    {
        fprintf(stdout, "Action incomprise.\n");
    }
    else
    {
        fprintf(stderr, "Fail.\n");
        close(socketTCP);
        exit(EXIT_FAILURE);
    }
}

void tailleLaby(int socketTCP)
{
    // envoi format [SIZE?␣m***]
    char buf10[10];
    memcpy(buf10, "SIZE? ", 6);
    size_t taille = 6;
    uint8_t m = atoi("43");
    memmove(buf10 + taille, &m, sizeof(uint8_t));
    taille += sizeof(uint8_t);
    memmove(buf10 + taille, "***", 3);
    sendError(send(socketTCP, buf10, strlen(buf10), 0));

    // reception  [SIZE!␣m␣h␣w***] ou  [DUNNO***]
    char buf5[6];
    recvError(recv(socketTCP, buf5, 5, 0));
    if (strcmp(buf5, "SIZE!"))
    {
        char buf[9];
        recvError(recv(socketTCP, buf, 9, 0));
        uint8_t m = atoi(&buf[1]);
        uint8_t h = atoi(&buf[3]);
        uint8_t w = atoi(&buf[5]);
        fprintf(stdout, "Labyrinthe %d : w = %d et h = %d.\n", m, w, h);
    }
    else if (strcmp(buf5, "DUNNO"))
    {
        fprintf(stdout, "Action incomprise.\n");
    }
    else
    {
        fprintf(stderr, "Fail.\n");
        close(socketTCP);
        exit(EXIT_FAILURE);
    }
}

void recupereJoueur(uint8_t s, int socketTCP)
{
    for (uint8_t i = 0; i < s; i++)
    {
        // reception du message [PLAYR␣id***]
        char buf[11];
        recvError(recv(socketTCP, buf, 11, 0));
        // affichage dans le terminal
        fprintf(stdout, "Joueur %s\n", &buf[6]);
    }
}

void listeJoueurs(int socketTCP)
{
    // envoi format [LIST?␣m***]
    char buf10[10];
    memcpy(buf10, "LIST? ", 6);
    size_t taille = 6;
    uint8_t m = atoi("43");
    memmove(buf10 + taille, &m, sizeof(uint8_t));
    taille += sizeof(uint8_t);
    memmove(buf10 + taille, "***", 3);
    sendError(send(socketTCP, buf10, strlen(buf10), 0));

    // et reception   [LIST!␣m␣s***] puis s message    ou  [DUNNO***]
    char buf5[6];
    recvError(recv(socketTCP, buf5, 5, 0));
    if (strcmp(buf5, "LIST!"))
    {
        char buf[7];
        recvError(recv(socketTCP, buf, 7, 0));
        uint8_t m = atoi(&buf[1]);
        uint8_t s = atoi(&buf[3]);
        fprintf(stdout, "%d joueurs dans la partie %d :\n", s, m);
        recupereJoueur(s, socketTCP);
    }
    else if (strcmp(buf5, "DUNNO"))
    {
        fprintf(stdout, "Action incomprise.\n");
    }
    else
    {
        fprintf(stderr, "Fail.\n");
        close(socketTCP);
        exit(EXIT_FAILURE);
    }
}

// reception des n messages [OGAME␣m␣s***]
void recupereGames(uint8_t n, int socketTCP)
{
    for (uint8_t i = 0; i < n; i++)
    {
        // reception du message [OGAME␣m␣s***]
        char buf[12];
        recvError(recv(socketTCP, buf, 12, 0));
        // affichage dans le terminal
        uint8_t m = atoi(&buf[6]);
        uint8_t s = atoi(&buf[8]);
        fprintf(stdout, "OGAME %d %d\n", m, s);
    }
}

void listeParties(int socketTCP)
{
    //  [GAME?***]
    char buf8[8];
    memcpy(buf8, "GAME?***", 8);
    sendError(send(socketTCP, buf8, strlen(buf8), 0));

    // et reception [GAMES␣n***] puis n message [OGAME␣m␣s***] ou  [DUNNO***]
    char buf5[6];
    recvError(recv(socketTCP, buf5, 5, 0));
    if (strcmp(buf5, "GAMES"))
    {
        char buf[5];
        recvError(recv(socketTCP, buf, 5, 0));
        uint8_t n = atoi(&buf[1]);
        fprintf(stdout, "Games : %d\n", n);
        recupereGames(n, socketTCP);
    }
    else if (strcmp(buf5, "DUNNO"))
    {
        fprintf(stdout, "Action incomprise.\n");
    }
    else
    {
        fprintf(stderr, "Fail.\n");
        close(socketTCP);
        exit(EXIT_FAILURE);
    }
}

void start(int socketTCP)
{
    //[START***]
    char buf8[8];
    memcpy(buf8, "START***", 8);
    sendError(send(socketTCP, buf8, strlen(buf8), 0));
}