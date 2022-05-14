#include "headers/fonctions.h"
#include "headers/actionsInGame.h"

bool receptSeDeplacer(int socketTCP){

    struct pollfd p[1];

    p[0].fd = socketTCP;
    p[0].events = POLLIN;

    while (true)
    {

        int ret = poll(p, 1, -1);
        if (ret > 0)
        {
            if (p[0].revents == POLLIN)
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
                    fprintf(stdout, "Vous êtes à la position (%d,%d) (lig,col).\n", x, y);
                    return true;
                }
                else if (strcmp(buf5, "MOVEF") == 0) // reception [MOVEF␣x␣y␣p***]
                {
                    size_t t = 3 + 3 + 3 + 2 + 4;
                    char buf[t];
                    recvError(recv(socketTCP, buf, t, 0));
                    int x = atoi(&buf[1]);
                    int y = atoi(&buf[5]);
                    uint32_t p = atoi(&buf[9]);
                    fprintf(stdout, "Vous êtes à la position (%d,%d) (col,lig).\nAprès avoir récolté un fantome vous avez %d points.", x, y, p);
                    return true;
                }
                else if (strcmp(buf5, "DUNNO")) // reception [DUNNO***]
                {
                    char buf2[3];
                    recvError(recv(socketTCP, buf2, 3, 0));
                    fprintf(stdout, "Action incomprise.\n");
                    return true;
                }
                else if (strcmp(buf5, "GOBYE"))
                {
                    char buf2[3];
                    recvError(recv(socketTCP, buf2, 3, 0));
                    fprintf(stderr, "La partie est terminée !\n");
                    return false;
                }
                else
                {
                    fprintf(stderr, "Fail.\n");
                    close(socketTCP);
                    exit(EXIT_FAILURE);
                }

            }
        }
    }

}

// Demande à se déplacer dans une direction sur une distance
bool seDeplacer(int socketTCP, int distance, char direction){

    char buf12[12];
    // Debut du message en fonction de la direction donnée
    switch (direction)
    {
    case 'l':
        memcpy(buf12, "LEMOV ", 6);
        break;
    case 'r':
        memcpy(buf12, "RIMOV ", 6);
        break;
    case 'u':
        memcpy(buf12, "UPMOV ", 6);
        break;
    case 'd':
        memcpy(buf12, "DOMOV ", 6);
        break;
    }
    
    size_t curr = 6;
    char dist[10];
    int distLength = sprintf(dist, "%d", distance);
    printf("%s\n", dist);
    char sentDist[3];

    // Ajout des "0" au début du string si nécessaire
    if (distLength < 4)
    {
        int i;
        for (i = 0; i < (3 - distLength); i++){
            memmove(sentDist + i, "0", 1);
        }
        memmove(sentDist + i, dist, distLength);
    }else{
        memmove(sentDist, "000", 3);
    }
    char affichage[4];
    memmove(affichage, sentDist,3);
    affichage[3] = '\0';
    printf("String envoyé : %s\n", affichage);

    memmove(buf12 + curr, sentDist, 3);
    curr += 3;
    memmove(buf12 + curr, "***", 3);
    sendError(send(socketTCP, buf12, 12, 0));

    return receptSeDeplacer(socketTCP);
}


bool quitterPartie(int socketTCP){
    size_t t = 5 + 3;
    char buf[t];
    memcpy(buf, "IQUIT***", 8);
    sendError(send(socketTCP, buf, 8, 0));

    struct pollfd p[1];

    p[0].fd = socketTCP;
    p[0].events = POLLIN;

    while (true)
    {

        int ret = poll(p, 1, -1);
        if (ret > 0)
        {
            if (p[0].revents == POLLIN)
            {
                char buf8[8];
                recvError(recv(socketTCP, buf8, 8, 0));
                fprintf(stdout, "Vous avez quitté la partie.\n");
                break;
            }
        }
    }

    return false;
}

bool printJoueurs(uint8_t j, int socketTCP)
{
    struct pollfd p[1];

    p[0].fd = socketTCP;
    p[0].events = POLLIN;

    while (true)
    {

        int ret = poll(p, 1, -1);
        if (ret > 0)
        {
            if (p[0].revents == POLLIN)
            {
                // reception des j [GPLYR␣id␣x␣y␣p***]
                for (uint8_t i = 0; i < j; i++)
                {
                    size_t sizeJ = 5 + 8 + 3 + 3 + 4 + 3 + 4;
                    char buf30[sizeJ];
                    recvError(recv(socketTCP, buf30, sizeJ, 0));
                    printf("caca\n");
                    char *infos = strtok(buf30, " ");
                    infos = strtok(NULL, " ");
                    char *id = infos;
                    id[8] = '\0';
                    int x = atoi(&buf30[16]);
                    int y = atoi(&buf30[20]);
                    uint32_t points = atoi(&buf30[24]);

                        // affichage dans le terminal
                    fprintf(stdout, "Le Joueur %s en (%d,%d) a %u points.\n", id, x, y, points);
                }
                break;
            }
        }
    }

}

bool listeJoueursIG(int socketTCP){
    // Envoi format [GLIS?***]
    size_t t = 5 + 3;
    char buf8[t];
    memcpy(buf8, "GLIS?***", 8);
    sendError(send(socketTCP, buf8, t, 0));

    struct pollfd p[1];

    p[0].fd = socketTCP;
    p[0].events = POLLIN;

    while (true)
    {

        int ret = poll(p, 1, -1);
        if (ret > 0)
        {
            if (p[0].revents == POLLIN)
            {
                // Reception format [GLIS! s***] ou [GOBYE***]
                char buf5[6];
                recvError(recv(socketTCP, buf5, 5, 0));
                buf5[5] = '\0';
                if (strcmp(buf5, "GLIS!") == 0)
                {
                    size_t t = 3 + 1 + 1;
                    char buf[t];
                    recvError(recv(socketTCP, buf, t, 0));
                    uint8_t j = atoi(&buf[1]);
                    fprintf(stdout, "Il y a %d joueurs dans la partie.\n", j);

                    // Reception  des données de chaque joueur
                    printJoueurs(j, socketTCP);
                    return true;
                }
                else if (strcmp(buf5, "GOBYE"))
                {
                    char buf2[3];
                    recvError(recv(socketTCP, buf2, 3, 0));
                    fprintf(stderr, "La partie est terminée !\n");
                    return false;
                }
                else
                {
                    fprintf(stderr, "Fail.\n");
                    close(socketTCP);
                    exit(EXIT_FAILURE);
                }
            }
        }
    }
}

bool envoiMessATous(int socketTCP, char* mess){
    // Envoi format [MALL?␣mess***]
    size_t t = 5 + strlen(mess) - 1 + 3 + 1;
    char buf[t];
    memcpy(buf, "MALL? ", 6);
    size_t curr = 6;
    memmove(buf + curr, mess, strlen(mess) - 1);
    curr += strlen(mess) - 1;
    memmove(buf + curr, "***", 3);
    sendError(send(socketTCP, buf, t, 0));
    buf[t] = '\0';
    printf("%s\n", buf);

    struct pollfd p[1];

    p[0].fd = socketTCP;
    p[0].events = POLLIN;

    while (true)
    {

        int ret = poll(p, 1, -1);
        if (ret > 0)
        {
            if (p[0].revents == POLLIN)
            {
                // Reception format [MALL!***]
                char buf5[6];
                recvError(recv(socketTCP, buf5, 5, 0));
                buf5[5] = '\0';
                if (strcmp(buf5, "MALL!") == 0)
                {
                    char buf2[3];
                    recvError(recv(socketTCP, buf2, 3, 0));
                    fprintf(stderr, "La message a été envoyé à tous les joueurs.\n");
                    return true;
                }
                else if (strcmp(buf5, "GOBYE"))
                {
                    char buf2[3];
                    recvError(recv(socketTCP, buf2, 3, 0));
                    fprintf(stderr, "La partie est terminée !\n");
                    return false;
                }
                else
                {
                    fprintf(stderr, "Fail.\n");
                    close(socketTCP);
                    exit(EXIT_FAILURE);
                }
            }
        }
    }

}

// Envoi de message à un joueur de la partie
bool envoiMessAJoueur(int socketTCP, char *mess, char* id){
    // Envoi format [SEND?␣id␣mess***]
    size_t t = 5 + + 8 + strlen(mess) + 3 + 1;
    char buf[t];
    memcpy(buf, "SEND? ", 6);
    size_t curr = 6;
    memmove(buf + curr, id, 8);
    curr += 8;
    memmove(buf + curr, mess, strlen(mess));
    curr += strlen(mess);
    memmove(buf + curr, "***", 3);
    sendError(send(socketTCP, buf, t, 0));

    struct pollfd p[1];

    p[0].fd = socketTCP;
    p[0].events = POLLIN;

    while (true)
        {

        int ret = poll(p, 1, -1);
        if (ret > 0)
        {
            if (p[0].revents == POLLIN)
            {
                // Reception format [SEND!***] ou [NSEND***]
                char buf5[6];
                recvError(recv(socketTCP, buf5, 5, 0));
                buf5[5] = '\0';
                if (strcmp(buf5, "SEND!") == 0)
                {
                    char buf[3];
                    recvError(recv(socketTCP, buf, 3, 0));
                    fprintf(stdout, "Le message à été envoyé\n");
                    return true;
                }
                else if (strcmp(buf5, "NSEND") == 0)
                {
                    char buf2[3];
                    recvError(recv(socketTCP, buf2, 3, 0));
                    fprintf(stdout, "Le joueur spécifié n'existe pas ou n'est pas dans la partie.\n");
                    return true;
                }
                else if (strcmp(buf5, "GOBYE"))
                {
                    char buf2[3];
                    recvError(recv(socketTCP, buf2, 3, 0));
                    fprintf(stderr, "La partie est terminée !\n");
                    return false;
                }
                else
                {
                    fprintf(stderr, "Fail.\n");
                    close(socketTCP);
                    exit(EXIT_FAILURE);
                }
            }
        }
    }
}