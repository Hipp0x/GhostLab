#include "fonctions.h"
#include "actionsBefore.h"

// demande de la creation d'une partie
void creerPartie(int socketTCP, char identifiant[], char port[])
{
    char buf22[22];
    memcpy(buf22, "NEWPL ", 6);
    size_t taille = 6;
    memmove(buf22 + taille, identifiant, 8);
    taille += 8;
    memmove(buf22 + taille, " ", 1);
    taille += 1;
    memmove(buf22 + taille, port, 4);
    taille += 4;
    memmove(buf22 + taille, "***", 3);
    sendError(send(socketTCP, buf22, 22, 0));

    char buf5[6];
    recvError(recv(socketTCP, buf5, 5, 0));
    buf5[5] = '\0';
    if (strcmp(buf5, "REGOK") == 0)
    {
        size_t t = 4 + sizeof(uint8_t);
        char buf[t];
        recvError(recv(socketTCP, buf, t, 0));
        uint8_t m = atoi(&buf[1]);
        fprintf(stdout, "Vous êtes dans la partie %d.\n", m);
    }
    else if (strcmp(buf5, "REGNO") == 0)
    {
        char buf2[3];
        recvError(recv(socketTCP, buf2, 3, 0));
        fprintf(stdout, "Action incomprise.\n");
    }
    else
    {
        fprintf(stderr, "Fail.\n");
        close(socketTCP);
        exit(EXIT_FAILURE);
    }
}

// demande pour rejoindre la partie num
void rejoindrePartie(int socketTCP, char identifiant[], char port[], uint8_t num)
{
    // envoi format [REGIS␣id␣port␣m***]
    size_t t = 23 + sizeof(uint8_t);
    char buf22[t];
    memcpy(buf22, "REGIS ", 6);
    size_t taille = 6;
    memmove(buf22 + taille, identifiant, 8);
    taille += 8;
    memmove(buf22 + taille, " ", 1);
    taille += 1;
    memmove(buf22 + taille, port, 4);
    taille += 4;
    memmove(buf22 + taille, " ", 1);
    taille += 1;
    uint8_t m = num;
    memmove(buf22 + taille, &m, sizeof(uint8_t));
    taille += sizeof(uint8_t);
    memmove(buf22 + taille, "***", 3);
    sendError(send(socketTCP, buf22, t, 0));

    char buf5[6];
    recvError(recv(socketTCP, buf5, 5, 0));
    buf5[5] = '\0';
    if (strcmp(buf5, "REGOK") == 0)
    {
        size_t t = 4 + sizeof(uint8_t);
        char buf[t];
        recvError(recv(socketTCP, buf, t, 0));
        uint8_t m = atoi(&buf[1]);
        fprintf(stdout, "Vous êtes dans la partie %d.\n", m);
    }
    else if (strcmp(buf5, "REGNO") == 0)
    {
        char buf2[3];
        recvError(recv(socketTCP, buf2, 3, 0));
        fprintf(stdout, "Action incomprise.\n");
    }
    else
    {
        fprintf(stderr, "Fail.\n");
        close(socketTCP);
        exit(EXIT_FAILURE);
    }
}

// demande de desinscription
void desinscription(int socketTCP)
{
    // envoi format [UNREG***]
    char buf8[8];
    memcpy(buf8, "UNREG***", 8);
    sendError(send(socketTCP, buf8, sizeof(buf8), 0));

    // reception  [UNROK␣m***] ou  [DUNNO***]
    char buf5[6];
    recvError(recv(socketTCP, buf5, 5, 0));
    buf5[5] = '\0';
    printf("char : %s\n", buf5);
    if (strcmp(buf5, "UNROK") == 0)
    {
        size_t t = 4 + sizeof(uint8_t);
        char buf[t];
        recvError(recv(socketTCP, buf, t, 0));
        uint8_t m = atoi(&buf[1]);
        fprintf(stdout, "Vous êtes desinscris de la partie %d.\n", m);
    }
    else if (strcmp(buf5, "DUNNO") == 0)
    {
        char buf2[3];
        recvError(recv(socketTCP, buf2, 3, 0));
        fprintf(stdout, "Action incomprise.\n");
    }
    else
    {
        fprintf(stderr, "Fail.\n");
        close(socketTCP);
        exit(EXIT_FAILURE);
    }
}

// demande de la taille du labyrinthe de la partie num
void tailleLaby(int socketTCP, uint8_t num)
{
    // envoi format [SIZE?␣m***]
    size_t t = 9 + sizeof(uint8_t);
    char buf10[t];
    memcpy(buf10, "SIZE? ", 6);
    size_t taille = 6;
    uint8_t m = num;
    memmove(buf10 + taille, &m, sizeof(uint8_t));
    taille += sizeof(uint8_t);
    memmove(buf10 + taille, "***", 3);
    sendError(send(socketTCP, buf10, t, 0));

    // reception  [SIZE!␣m␣h␣w***] ou  [DUNNO***]
    char buf5[6];
    recvError(recv(socketTCP, buf5, 5, 0));
    buf5[5] = '\0';
    if (strcmp(buf5, "SIZE!") == 0)
    {
        size_t t = 6 + 3 * sizeof(uint8_t);
        char buf[t];
        recvError(recv(socketTCP, buf, t, 0));
        uint8_t m = atoi(&buf[1]);
        uint8_t h = atoi(&buf[3]);
        uint8_t w = atoi(&buf[5]);
        fprintf(stdout, "Labyrinthe %d : w = %d et h = %d.\n", m, w, h);
    }
    else if (strcmp(buf5, "DUNNO") == 0)
    {
        char buf2[3];
        recvError(recv(socketTCP, buf2, 3, 0));
        fprintf(stdout, "Action incomprise.\n");
    }
    else
    {
        fprintf(stderr, "Fail.\n");
        close(socketTCP);
        exit(EXIT_FAILURE);
    }
}

// receptions des s joueurs d'une partie
void recupereJoueur(uint8_t s, int socketTCP)
{
    for (uint8_t i = 0; i < s; i++)
    {
        // reception du message [PLAYR␣id***]
        char buf[18];
        recvError(recv(socketTCP, buf, 17, 0));
        buf[17] = '\0';
        // affichage dans le terminal
        fprintf(stdout, "Joueur %s\n", &buf[6]);
    }
}

// demande de la liste de joueurs de la partie num
void listeJoueurs(int socketTCP, uint8_t num)
{
    // envoi format [LIST?␣m***]
    size_t t = 9 + sizeof(uint8_t);
    char buf10[t];
    memcpy(buf10, "LIST? ", 6);
    size_t taille = 6;
    uint8_t m = num;
    memmove(buf10 + taille, &m, sizeof(uint8_t));
    taille += sizeof(uint8_t);
    memmove(buf10 + taille, "***", 3);
    sendError(send(socketTCP, buf10, t, 0));

    // et reception   [LIST!␣m␣s***] puis s message    ou  [DUNNO***]
    char buf5[6];
    recvError(recv(socketTCP, buf5, 5, 0));
    buf5[5] = '\0';
    if (strcmp(buf5, "LIST!") == 0)
    {
        char buf[5 + 2 * (sizeof(uint8_t))];
        recvError(recv(socketTCP, buf, 7, 0));
        uint8_t m = atoi(&buf[1]);
        uint8_t s = atoi(&buf[3]);
        fprintf(stdout, "%d joueurs dans la partie %d :\n", s, m);
        recupereJoueur(s, socketTCP);
    }
    else if (strcmp(buf5, "DUNNO") == 0)
    {
        char buf2[3];
        recvError(recv(socketTCP, buf2, 3, 0));
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
        size_t t = 10 + 2 * sizeof(uint8_t);
        char buf[t];
        recvError(recv(socketTCP, buf, t, 0));
        // affichage dans le terminal
        uint8_t m = atoi(&buf[6]);
        uint8_t s = atoi(&buf[8]);
        fprintf(stdout, "OGAME %d %d\n", m, s);
    }
}

// demande de la liste des parties
void listeParties(int socketTCP)
{
    //  [GAME?***]
    char buf8[8];
    memcpy(buf8, "GAME?***", 8);
    sendError(send(socketTCP, buf8, 8, 0));

    // et reception [GAMES␣n***] puis n message [OGAME␣m␣s***] ou  [DUNNO***]
    char buf5[6];
    recvError(recv(socketTCP, buf5, 5, 0));
    buf5[5] = '\0';
    if (strcmp(buf5, "GAMES") == 0)
    {
        size_t t = 4 + sizeof(uint8_t);
        char buf[t];
        recvError(recv(socketTCP, buf, t, 0));
        uint8_t n = atoi(&buf[1]);
        fprintf(stdout, "Games : %d\n", n);
        recupereGames(n, socketTCP);
    }
    else if (strcmp(buf5, "DUNNO") == 0)
    {
        char buf2[3];
        recvError(recv(socketTCP, buf2, 3, 0));
        fprintf(stdout, "Action incomprise.\n");
    }
    else
    {
        fprintf(stderr, "Fail.\n");
        close(socketTCP);
        exit(EXIT_FAILURE);
    }
}

// demande de start
void start(int socketTCP)
{
    //[START***]
    char buf8[8];
    memcpy(buf8, "START***", 8);
    sendError(send(socketTCP, buf8, 8, 0));
}