#include "headers/fonctions.h"
#include "headers/actionsInGame.h"

bool receptSeDeplacer(int socketTCP)
{
    char buf5[6];
    recvError(recv(socketTCP, buf5, 5, 0));
    buf5[5] = '\0';
    if (strcmp(buf5, "MOVE!") == 0) // reception [MOVE!␣x␣y***]
    {
        size_t t = 3 + 3 + 3 + 2;
        char buf[t];
        recvError(recv(socketTCP, buf, t, 0));
        int x = atoi(&buf[1]);
        int y = atoi(&buf[5]);
        fprintf(stdout, "**You're in (%d,%d)**\n", x, y);
        return true;
    }
    else if (strcmp(buf5, "MOVEF") == 0) // reception [MOVEF␣x␣y␣p***]
    {
        size_t t = 3 + 3 + 3 + 3 + 4;
        char buf[t];
        recvError(recv(socketTCP, buf, t, 0));
        int x = atoi(&buf[1]);
        int y = atoi(&buf[5]);
        uint32_t p = atoi(&buf[9]);
        fprintf(stdout, "**You're in (%d,%d)**\n**You've caught a ghost, you've got %d points**\n", x, y, p);
        return true;
    }
    else if (strcmp(buf5, "DUNNO") == 0) // reception [DUNNO***]
    {
        char buf2[3];
        recvError(recv(socketTCP, buf2, 3, 0));
        fprintf(stdout, "**The server doesn't understand**\n");
        return true;
    }
    else if (strcmp(buf5, "GOBYE") == 0)
    {
        char buf2[3];
        recvError(recv(socketTCP, buf2, 3, 0));
        fprintf(stderr, "**The game has ended**\n");
        return false;
    }
    else
    {
        fprintf(stderr, "Fail : %s.\n", buf5);
        close(socketTCP);
        exit(EXIT_FAILURE);
    }
}

// Demande à se déplacer dans une direction sur une distance
bool seDeplacer(int socketTCP, int distance, char direction)
{

    char buf12[12];
    // Debut du message en fonction de la direction donnée
    switch (direction)
    {
    case 'L':
    case 'l':
        memcpy(buf12, "LEMOV ", 6);
        break;
    case 'R':
    case 'r':
        memcpy(buf12, "RIMOV ", 6);
        break;
    case 'U':
    case 'u':
        memcpy(buf12, "UPMOV ", 6);
        break;
    case 'D':
    case 'd':
        memcpy(buf12, "DOMOV ", 6);
        break;
    }

    size_t curr = 6;
    char dist[10];
    int distLength = sprintf(dist, "%d", distance);
    char sentDist[3];

    // Ajout des "0" au début du string si nécessaire
    if (distLength < 4)
    {
        int i;
        for (i = 0; i < (3 - distLength); i++)
        {
            memmove(sentDist + i, "0", 1);
        }
        memmove(sentDist + i, dist, distLength);
    }
    else
    {
        memmove(sentDist, "000", 3);
    }
    char affichage[4];
    memmove(affichage, sentDist, 3);
    affichage[3] = '\0';

    memmove(buf12 + curr, sentDist, 3);
    curr += 3;
    memmove(buf12 + curr, "***", 3);
    sendError(send(socketTCP, buf12, 12, 0));

    return receptSeDeplacer(socketTCP);
}

bool quitterPartie(int socketTCP)
{
    size_t t = 5 + 3;
    char buf[t];
    memcpy(buf, "IQUIT***", 8);
    sendError(send(socketTCP, buf, 8, 0));

    char buf8[8];
    recvError(recv(socketTCP, buf8, 8, 0));
    fprintf(stdout, "**You left the game**\n");

    return false;
}

bool printJoueurs(uint8_t j, int socketTCP)
{

    // reception des j [GPLYR␣id␣x␣y␣p***]
    for (uint8_t i = 0; i < j; i++)
    {
        size_t sizeJ = 5 + 8 + 3 + 3 + 4 + 3 + 4;
        char buf30[sizeJ];
        recvError(recv(socketTCP, buf30, sizeJ, 0));
        char *infos = strtok(buf30, " ");
        infos = strtok(NULL, " ");
        char *id = infos;
        id[8] = '\0';
        int x = atoi(&buf30[16]);
        int y = atoi(&buf30[20]);
        uint32_t points = atoi(&buf30[24]);

        // affichage dans le terminal
        fprintf(stdout, "**Player %s is at (%d,%d) and has %u points**\n", id, x, y, points);
    }
}

bool listeJoueursIG(int socketTCP)
{
    // Envoi format [GLIS?***]
    size_t t = 5 + 3;
    char buf8[t];
    memcpy(buf8, "GLIS?***", 8);
    sendError(send(socketTCP, buf8, t, 0));

    // Reception format [GLIS! s***] ou [GOBYE***]
    char buf5[6];
    recvError(recv(socketTCP, buf5, 5, 0));
    buf5[5] = '\0';
    if (strcmp(buf5, "GLIS!") == 0)
    {
        size_t t = 3 + 1 + 1;
        char buf[t];
        recvError(recv(socketTCP, buf, t, 0));
        uint8_t j = (uint8_t) buf[1];
        fprintf(stdout, "**There is %d players in the game.**\n", j);

        // Reception  des données de chaque joueur
        printJoueurs(j, socketTCP);
        return true;
    }
    else if (strcmp(buf5, "GOBYE"))
    {
        char buf2[3];
        recvError(recv(socketTCP, buf2, 3, 0));
        fprintf(stderr, "**The game has ended!**\n");
        return false;
    }
    else
    {
        fprintf(stderr, "Fail.\n");
        close(socketTCP);
        exit(EXIT_FAILURE);
    }
}

bool envoiMessATous(int socketTCP, char *mess)
{
    // Envoi format [MALL?␣mess***]
    size_t t = 5 + strlen(mess) + 3 + 1;
    char buf[t];
    memcpy(buf, "MALL? ", 6);
    size_t curr = 6;
    memmove(buf + curr, mess, strlen(mess));
    curr += strlen(mess);
    memmove(buf + curr, "***", 3);
    sendError(send(socketTCP, buf, t, 0));
    buf[t] = '\0';
    printf("%s\n", buf);

    // Reception format [MALL!***]
    char buf5[6];
    recvError(recv(socketTCP, buf5, 5, 0));
    buf5[5] = '\0';
    if (strcmp(buf5, "MALL!") == 0)
    {
        char buf2[3];
        recvError(recv(socketTCP, buf2, 3, 0));
        fprintf(stderr, "**Your message has been sent to everyone**\n");
        return true;
    }
    else if (strcmp(buf5, "GOBYE"))
    {
        char buf2[3];
        recvError(recv(socketTCP, buf2, 3, 0));
        fprintf(stderr, "**The game has ended!**\n");
        return false;
    }
    else
    {
        fprintf(stderr, "Fail.\n");
        close(socketTCP);
        exit(EXIT_FAILURE);
    }
}

// Envoi de message à un joueur de la partie
bool envoiMessAJoueur(int socketTCP, char *mess, char *id)
{
    // Envoi format [SEND?␣id␣mess***]
    size_t t = 5 + 8 + strlen(mess) + 3 + 2;
    char buf[t];
    memcpy(buf, "SEND? ", 6);
    size_t curr = 6;
    memmove(buf + curr, id, 8);
    curr += 8;
    memmove(buf + curr, " ", 1);
    curr += 1;
    memmove(buf + curr, mess, strlen(mess));
    curr += strlen(mess);
    memmove(buf + curr, "***", 3);
    sendError(send(socketTCP, buf, t, 0));
    buf[t] = '\0';

    // Reception format [SEND!***] ou [NSEND***]
    char buf5[6];
    recvError(recv(socketTCP, buf5, 5, 0));
    buf5[5] = '\0';

    if (strcmp(buf5, "SEND!") == 0)
    {
        char buf[3];
        recvError(recv(socketTCP, buf, 3, 0));
        fprintf(stdout, "**Your message has been sent**\n");
        return true;
    }
    else if (strcmp(buf5, "NSEND") == 0)
    {
        char buf2[3];
        recvError(recv(socketTCP, buf2, 3, 0));
        fprintf(stdout, "**This player is not valid**\n");
        return true;
    }
    else if (strcmp(buf5, "GOBYE"))
    {
        char buf2[3];
        recvError(recv(socketTCP, buf2, 3, 0));
        fprintf(stderr, "**The game has ended!**\n");
        return false;
    }
    else
    {
        fprintf(stderr, "Fail.\n");
        close(socketTCP);
        exit(EXIT_FAILURE);
    }
}

bool recupLaby(int socketTCP, int w, int h)
{

    for (int i = 0; i < w; i++)
    {
        for (int j = 0; j < h; j++)
        {
            char buf[8];
            recvError(recv(socketTCP, buf, 8, 0));
            char *p;
            p = strtok(buf, "*");

            if (strcmp(p, "TRUE!") == 0)
            {
                fprintf(stdout, "XX");
            }
            else if (strcmp(p, "FALSE") == 0)
            {
                fprintf(stdout, "  ");
            }
            else
            {
                fprintf(stderr, "Fail : reception %s.\n", buf);
                close(socketTCP);
                exit(EXIT_FAILURE);
            }
        }
        fprintf(stdout, "\n");
    }
}

// triche pour voir le labyrinthe
bool tricheLaby(int socketTCP, int w, int h)
{
    char *mess = "XTLX?***";
    sendError(send(socketTCP, mess, 8, 0));

    char buf5[6];
    recvError(recv(socketTCP, buf5, 5, 0));
    buf5[5] = '\0';

    if (strcmp(buf5, "DUNNO") == 0)
    {
        char buf3[3];
        recvError(recv(socketTCP, buf3, 3, 0));
        fprintf(stdout, "**The server doesn't understand**\n");
        return true;
    }
    else if (strcmp(buf5, "TRCHL") == 0)
    {
        char buf3[3];
        recvError(recv(socketTCP, buf3, 3, 0));
        return recupLaby(socketTCP, w, h);
    }
    else
    {
        fprintf(stderr, "Fail : reception %s.\n", buf5);
        close(socketTCP);
        exit(EXIT_FAILURE);
    }
}