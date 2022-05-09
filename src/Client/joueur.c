#include "headers/fonctions.h"
#include "headers/actionsBefore.h"
#include "headers/actionsInGame.h"

#include <sys/socket.h>
#include <netinet/in.h>
#include <stdbool.h>
#include <ctype.h>

/*
-----------VARIABLES-----------
*/

char port[4] = "5467";
char *identifiant;
char portMC[4];
char addrMC[15];
bool inscrit;
bool enPartie;

/*
-----------FONCTIONS-----------
*/

bool isDigit(char *a){
    int l = strlen(a);
    for (int i = 0; i < l; i++){
        if (isalpha(a[i])){
            return false;
        } else if (a[i] = '\n'){
            return true;
        }
    }
    return true;
}

// les actions que peut faire le joueur avant le debut d'une partie
bool actionAvantPartie(int socketTCP, char *ch)
{
    char *sep = " ";
    char *choix = strtok(ch, sep);
    printf("choix : %s\n", choix);
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
        printf("numero : %s\n", choix);
        if (!isDigit(choix))
        {
            fprintf(stdout, "Vous devez entrer un chiffre, et non %s", choix);
            return false;
        }
        else
        {
            num = atoi(choix);
            rejoindrePartie(socketTCP, identifiant, port, num);
            inscrit = true;
            return false;
        }
        break;

    case 'd':; // desinscription d'une partie
        desinscription(socketTCP);
        inscrit = false;
        return false;
        break;

    case 't':; // taille labyrinthe de la partie m
        choix = strtok(NULL, sep);
        printf("numero : %s\n", choix);
        if (!isDigit(choix))
        {
            fprintf(stdout, "Vous devez entrer un chiffre, et non %s", choix);
        }
        else
        {
            num = atoi(choix);
            tailleLaby(socketTCP, num);
        }
        return false;
        break;

    case 'j':; // liste joueurs de la partie partie m
        choix = strtok(NULL, sep);
        printf("numero : %s\n", choix);
        if (!isDigit(choix))
        {
            fprintf(stdout, "Vous devez entrer un chiffre, et non %s", choix);
            return false;
            break;
        }
        else
        {
            num = atoi(choix);
            listeJoueurs(socketTCP, num);
            return false;
            break;
        }

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

void actionEnPartie(int socketTCP, char *ch)
{
    enPartie = true;
    char *sep = " ";
    char *choix = strtok(ch, sep);
    printf("char : %s\n", choix);
    uint8_t num;
    switch (choix[0])
    {
    case 'l':; // Aller a gauche
    case 'r':; // Aller à droite
    case 'd':; // Aller en bas
    case 'u':; // Aller en haut
        char dir = choix[0];
        choix = strtok(NULL, sep);
        int dist = atoi(&choix[0]);
        enPartie = seDeplacer(socketTCP, dist, dir);
        break;
    case 'q':; // Quitter la partie
        enPartie = quitterPartie(socketTCP);
        break;

    case 'p':; // Liste des joueurs dans la partie
        enPartie = listeJoueursIG(socketTCP);
        break;
    case 'm':; // Message à tous les joueurs de la partie
        choix = strtok(NULL, sep);
        envoiMessATous(socketTCP, choix);
        break;
    case 'w':; // Message à un joueur
        choix = strtok(NULL, sep);
        char *id = choix;
        choix = strtok(NULL, sep);
        envoiMessAJoueur(socketTCP, choix, id);
        break;
    default:;
        fprintf(stdout, "Ce n'est pas correct.\n");
        break;
    }
}

void receptMultiDiff(int socketMultiDiff)
{
    char buf5[6];
    recvError(recv(socketMultiDiff, buf5, 5, 0));
    buf5[5] = '\0';
    if (strcmp(buf5, "GHOST") == 0)
    {
        size_t t = 3 + 3 + 3 + 2;
        char buf[t];
        recvError(recv(socketMultiDiff, buf, t, 0));
        int x = atoi(&buf[1]);
        int y = atoi(&buf[5]);

        fprintf(stdout, "Un fantome s'est déplacé en (%d,%d).", x, y);
    }
    else if (strcmp(buf5, "SCORE") == 0)
    {
        size_t t = 8 + 4 + 3 + 3 + 3 + 4;
        char buf[t];
        recvError(recv(socketMultiDiff, buf, t, 0));
        char *infos = strtok(&buf[1], " ");

        char *id = infos;
        uint16_t points = buf[10];
        int x = buf[15];
        int y = buf[19];

        fprintf(stdout, "%s a attrapé un fantome en (%d,%d) et a maintenant %u points", id, x, y, points);
    }
    else if (strcmp(buf5, "MESSA") == 0)
    {
        size_t t = 8 + 200 + 3 + 2;
        char buf[t];
        recvError(recv(socketMultiDiff, buf, t, 0));

        char *infos = strtok(&buf[1], " ");
        char *id = infos;

        infos = strtok(NULL, " ");
        char *rest = infos;

        char *s = strtok(rest, "*");
        char *mess = s;

        fprintf(stdout, "%s: %s\n", id, mess);
    }
    else if (strcmp(buf5, "ENDGA") == 0)
    {
        size_t t = 8 + 4 + 3 + 2;
        char buf[t];
        recvError(recv(socketMultiDiff, buf, t, 0));

        char *infos = strtok(&buf[1], " ");
        char *id = infos;
        uint16_t points = atoi(&buf[10]);

        fprintf(stdout, "La partie est terminée!\n%s a gagné avec %u points!", id, points);
    }
}

void receptWelcPos(int socketTCP, int socketMultiDiff) // Reception format [WELCO␣m␣h␣w␣f␣ip␣port***] et [POSIT␣id␣x␣y***]
{
    size_t t = 5 + 1 + 2 + 2 + 1 + 15 + 4 + 3 + 6; // 39
    char buf[t];
    recvError(recv(socketTCP, buf, t, 0));
    uint8_t gameID = atoi(&buf[6]);
    uint16_t hauteur = atoi(&buf[8]);
    uint16_t largeur = atoi(&buf[11]);
    uint8_t nbFantomes = atoi(&buf[14]);
    char *multi = strtok(&buf[16], " ");
    memmove(addrMC, multi, 15);
    multi = strtok(NULL, " ");
    memmove(portMC, multi, 4);

    struct sockaddr_in address_sock;
    address_sock.sin_family = AF_INET;
    address_sock.sin_port = htons(atoi(portMC));
    address_sock.sin_addr.s_addr = htonl(INADDR_ANY);

    int ok = 1;
    int r = setsockopt(socketMultiDiff, SOL_SOCKET, SO_REUSEPORT, &ok, sizeof(ok));
    r = bind(socketMultiDiff, (struct sockaddr *)&address_sock, sizeof(struct sockaddr_in));

    char *addr = strtok(addrMC, "#");
    struct ip_mreq mreq;
    mreq.imr_multiaddr.s_addr = inet_addr(addr);
    mreq.imr_interface.s_addr = htonl(INADDR_ANY);
    r = setsockopt(socketMultiDiff, IPPROTO_IP, IP_ADD_MEMBERSHIP, &mreq, sizeof(mreq));

    fprintf(stdout, "Bienvenue dans la partie %u!\nLe labyrinthe a une hauteur de %u et une largeur de %u.\nIl y a %u fantomes à attraper. Bonne chance!\n", gameID, hauteur, largeur, nbFantomes);

    t = 5 + 8 + 3 + 3 + 3 + 3;
    char buf25[t];
    recvError(recv(socketTCP, buf, t, 0));
    int x = atoi(&buf[15]);
    int y = atoi(&buf[19]);

    fprintf(stdout, "Vous êtes à la position (%d,%d).\n", x, y);
    enPartie = true;
}

void getID()
{
    bool isok = false;
    while (!isok)
    {
        bool test = true;
        fprintf(stdout, "Choisissez un identifiant : 8 char, lettres et/ou chiffres.\n");
        char *line = NULL;
        ssize_t len = 0;
        ssize_t lineSize = 0;
        lineSize = getline(&line, &len, stdin);
        if (lineSize != 9)
        {
            fprintf(stdout, "Incorrect. SVP pas %ld mais 8 char\n", (lineSize - 1));
        }
        else
        {
            for (int i = 0; i < 8; i++)
            {
                if (!isalnum(line[i]))
                {
                    test = false;
                    fprintf(stdout, "Incorrect. SVP le %d n'est pas alphanumerique\n", (i + 1));
                }
            }
            if (test)
            {
                identifiant = malloc(8);
                strcpy(identifiant, line);
                fprintf(stdout, "Pseudo %s ok\n", identifiant);
                isok = true;
            }
        }
        free(line);
    }
}

void verifport(char *a)
{
    if (strlen(a) != 4)
    {
        fprintf(stdout, "Incorrect. SVP le port %s doit faire 4 characteres numeriques.\n", a);
        exit(EXIT_FAILURE);
    }
    for (int i = 0; i < 4; i++)
    {
        if (!isdigit(a[i]))
        {
            fprintf(stdout, "Incorrect. SVP le port %s doit etre en numerique.\n", a);
            exit(EXIT_FAILURE);
        }
    }
}

int main(int argc, char *argv[])
{

    if (argc != 4)
    {
        fprintf(stderr, "Mauvais nombre de parametres au lancement.\n");
        exit(EXIT_FAILURE);
    }

    verifport(argv[2]);
    verifport(argv[3]);

    // recuperation du port voulu
    uint16_t portTCP = atoi(argv[2]);
    uint16_t portUDP = atoi(argv[3]);

    //
    struct sockaddr_in address_sock;
    address_sock.sin_family = AF_INET;
    // address_sock.sin_port = htons(5621);
    // address_sock.sin_addr.s_addr = htonl(INADDR_ANY);
    address_sock.sin_port = htons(portUDP);
    inet_aton(argv[1], &address_sock.sin_addr);

    getID();

    // socket tcp serveur
    int socketTCP = socket(PF_INET, SOCK_STREAM, 0);

    // socket udp client
    int socketUDP = socket(PF_INET, SOCK_DGRAM, 0);
    int r = bind(socketUDP, (struct sockaddr *)&address_sock, sizeof(struct sockaddr_in));
    if (r != 0)
    {
        perror("Erreur de bind");
        exit(-1);
    }

    // socket multi diffusion serveur
    int socketMultiDiff = socket(PF_INET, SOCK_DGRAM, 0);

    // connexion au serveur
    address_sock.sin_port = htons(portTCP);
    int sock_client = connect(socketTCP, (struct sockaddr *)&address_sock, sizeof(struct sockaddr_in));
    connectError(sock_client);

    // reception de [GAMES␣n***]
    size_t t = 9 + sizeof(uint8_t);
    char buf[t];
    recvError(recv(socketTCP, buf, t, 0));
    uint8_t n = atoi(&buf[6]);
    fprintf(stdout, "GAMES %d \n", n);

    // reception de n message [OGAME␣m␣s***]
    recupereGames(n, socketTCP);

    bool ans = false;
    inscrit = false;
    while (!ans)
    {
        // lecture du choix du joueur
        fprintf(stdout, "\nQue voulez-vous faire ?\n");
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

    receptWelcPos(socketTCP, socketUDP);
    fcntl(socketTCP, F_SETFL, O_NONBLOCK);
    fcntl(socketUDP, F_SETFL, O_NONBLOCK);
    fcntl(socketMultiDiff, F_SETFL, O_NONBLOCK);
    struct pollfd p[3];

    p[0].fd = socketTCP;
    p[0].events = POLLOUT;
    p[1].fd = socketUDP;
    p[1].events = POLLIN;
    p[2].fd = socketMultiDiff;
    p[2].events = POLLIN;

    while (enPartie)
    {

        int ret = poll(p, 3, -1);
        if (ret > 0)
        {
            if (p[0].revents == POLLOUT)
            {
                // lecture du choix du joueur
                fprintf(stdout, "Que voulez-vous faire ?\n");
                fprintf(stdout, "l (aller à gauche) x, r (aller à droite) x, d (aller en bas) x, u (aller en haut) x, q (quitter partie), p (liste joueurs), m (message à tous) q, w (message a joueur) y  q.\n");
                fprintf(stdout, "avec x = distance souhaitée, y = id du joueur, q = message si necessaire.\n");
                // action

                char *line = NULL;
                ssize_t len = 0;
                ssize_t lineSize = 0;
                lineSize = getline(&line, &len, stdin);
                actionEnPartie(p[0].fd, line);
                free(line);
            }

            if (p[1].revents == POLLIN)
            {
                size_t t = 5 + 8 + 200 + 3 + 2;
                char buf[t];
                recvError(recv(socketTCP, buf, t, 0));

                char *infos = strtok(buf, " ");
                infos = strtok(NULL, " ");

                char *id = infos;
                infos = strtok(NULL, " ");

                char *s = infos;
                char *mess = strtok(s, "*");

                fprintf(stdout, "%s vous a envoyé : %s\n", id, mess);
            }

            if (p[2].revents == POLLIN)
            {
                receptMultiDiff(socketMultiDiff);
            }
        }
    }
}
