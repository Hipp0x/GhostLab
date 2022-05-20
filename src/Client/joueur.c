#include "headers/fonctions.h"
#include "headers/actionsBefore.h"
#include "headers/actionsInGame.h"

#include <sys/socket.h>
#include <netinet/in.h>
#include <stdbool.h>
#include <ctype.h>
#include <pthread.h>

/*
-----------VARIABLES-----------
*/

char *port;
char *identifiant;
char portMC[4];
char addrMC[16];
bool inscrit;
bool enPartie;
uint16_t hauteur;
uint16_t largeur;

/*
-----------FONCTIONS-----------
*/

bool isDigit(char *a, int l)
{
    for (int i = 0; i < l; i++)
    {
        if (a[i] == '\n')
        {
            return true;
        }
        else if (isalpha(a[i]) || ispunct(a[i]) || (isspace(a[i]) && i != 0))
        {
            return false;
        }
    }
    return true;
}

bool isVide(char *a)
{
    int l = strlen(a);
    for (int i = 0; i < l; i++)
    {
        if (isalpha(a[i]))
        {
            return false;
        }
    }
    return true;
}

// les actions que peut faire le joueur avant le debut d'une partie
bool actionAvantPartie(int socketTCP, char *ch)
{
    char *choix = ch;
    char *l;
    uint8_t num;
    switch (choix[0])
    {
    case 'C':;
    case 'c':; // creer une partie
        if (strlen(ch) == 2)
        {
            creerPartie(socketTCP, identifiant, port);
            inscrit = true;
        }
        else
        {
            fprintf(stdout, "Respect the format : c.\n");
        }
        return false;
        break;

    case 'J':;
    case 'j':; // rejoindre une partie
        if (ch[1] == ' ')
        {
            l = choix + 1;
            if (!isDigit(l, strlen(l)))
            {
                fprintf(stdout, "Youd need to enter a digital, not :%s.\n", l);
            }
            else
            {
                num = atoi(l);
                if (rejoindrePartie(socketTCP, identifiant, port, num))
                {
                    inscrit = true;
                }
            }
        }
        else
        {
            fprintf(stdout, "Respect the format : r entier.\n");
        }
        return false;
        break;

    case 'U':;
    case 'u':; // desinscription d'une partie
        if (strlen(ch) == 2)
        {
            desinscription(socketTCP);
            inscrit = false;
        }
        else
        {
            fprintf(stdout, "Respect the format : d.\n");
        }
        return false;
        break;

    case 'L':;
    case 'l':; // taille labyrinthe de la partie m
        if (ch[1] == ' ')
        {
            l = choix + 1;
            if (!isDigit(l, strlen(l)))
            {
                fprintf(stdout, "Youd need to enter a digital, not :%s.\n", choix);
            }
            else
            {
                num = atoi(l);
                tailleLaby(socketTCP, num);
            }
        }
        else
        {
            fprintf(stdout, "Respect the format : t entier.\n");
        }
        return false;
        break;

    case 'P':;
    case 'p':; // liste joueurs de la partie partie m
        if (ch[1] == ' ')
        {
            l = choix + 1;
            if (!isDigit(l, strlen(l)))
            {
                fprintf(stdout, "Youd need to enter a digital, not :%s.", l);
            }
            else
            {
                num = atoi(l);
                listeJoueurs(socketTCP, num);
            }
        }
        else
        {
            fprintf(stdout, "Respect the format : j entier.\n");
        }
        return false;
        break;

    case 'G':;
    case 'g':; // liste parties qui n'ont pas encore commencé
        if (strlen(ch) == 2)
        {
            listeParties(socketTCP);
        }
        else
        {
            fprintf(stdout, "Respect the format : p.\n");
        }
        return false;
        break;

    case 'S':;
    case 's':; // start
        if (strlen(ch) == 2)
        {
            if (inscrit)
            {
                start(socketTCP);
                return true;
            }
            else
            {
                fprintf(stdout, "You can't do this!\n");
                return false;
            }
        }
        else
        {
            fprintf(stdout, "Respect the format : s.\n");
            return false;
        }
        break;
    default:
        fprintf(stdout, "Wrong entry.\n");
        return false;
        break;
    }
}

void actionEnPartie(int socketTCP, char *ch)
{
    enPartie = true;
    char *choix;
    char *sep;
    uint8_t num;
    switch (ch[0])
    {
    case 'l':; // Aller a gauche
    case 'L':
    case 'r':; // Aller à droite
    case 'R':
    case 'd':; // Aller en bas
    case 'D':
    case 'u':; // Aller en haut
    case 'U':
        if (ch[1] == ' ')
        {
            char *l = ch + 1;
            if (isDigit(l, strlen(l)))
            {
                char dir = ch[0];
                int dist = atoi(l);
                enPartie = seDeplacer(socketTCP, dist, dir);
            }
            else
            {
                fprintf(stdout, "Youd need to enter a digital, not :%s.\n", l);
            }
        }
        else
        {
            fprintf(stdout, "Respect the format : c.\n");
        }

        break;
    case 'Q':;
    case 'q':; // Quitter la partie
        if (strlen(ch) == 2)
        {
            enPartie = quitterPartie(socketTCP);
        }
        else
        {
            fprintf(stdout, "Respect the format : q.\n");
        }
        break;

    case 'P':;
    case 'p':; // Liste des joueurs dans la partie
        if (strlen(ch) == 2)
        {
            enPartie = listeJoueursIG(socketTCP);
        }
        else
        {
            fprintf(stdout, "Respect the format : p.\n");
        }
        break;

    case 'M':;
    case 'm':; // Message à tous les joueurs de la partie
        if (ch[1] == ' ')
        {
            if (strlen(ch) > 3)
            {
                sep = " ";
                choix = strtok(ch, sep);
                sep = "\n";
                choix = strtok(NULL, sep);
                envoiMessATous(socketTCP, choix);
            }
            else
            {
                fprintf(stdout, "Empty message.\n");
            }
        }
        else
        {
            fprintf(stdout, "Respect the format : m message.\n");
        }
        break;

    case 'W':;
    case 'w':; // Message à un joueur
        if (ch[1] == ' ' && ch[10] == ' ' && strlen(ch) > 12)
        {
            sep = " ";
            choix = strtok(ch, sep); // pour w

            choix = strtok(NULL, sep); // pour id
            char *id = choix;
            if (strlen(id) == 8)
            {
                choix = strtok(NULL, sep);
                char *k = choix;
                if (!isVide(k))
                {
                    sep = "\n";
                    choix = strtok(k, sep);

                    envoiMessAJoueur(socketTCP, choix, id);
                }
                else
                {
                    fprintf(stdout, "Empty message.\n");
                }
            }

            else
            {
                fprintf(stdout, "The id need to contains 8 char.\n");
            }
        }
        else
        {
            fprintf(stdout, "Respect the format : w id message.\n");
        }
        break;

    case 'X':;
    case 'x':;
        if (strcmp(ch, "xtrichexlabyx\n") == 0)
        {
            tricheLaby(socketTCP, largeur, hauteur);
        }
        else if (strcmp(ch, "xtrichexfantx\n") == 0)
        {
            tricheFant(socketTCP);
        }
        else
        {
            fprintf(stdout, "Wrong entry.\n");
        }
        break;
    default:;
        fprintf(stdout, "Wrong entry.\n");
        break;
    }
}

void receptMultiDiff(int socketMultiDiff, char *received)
{
    char *buff = strtok(received, "+");
    char *action = strtok(buff, " ");
    if (strcmp(action, "GHOST") == 0)
    {

        int x = atoi(&buff[6]);
        int y = atoi(&buff[10]);

        fprintf(stdout, "\n++A gosht moved on(%d,%d).++\n", x, y);
    }
    else if (strcmp(action, "SCORE") == 0)
    {
        char *infos = strtok(NULL, " ");
        char *id = infos;
        if (!(strcmp(id, identifiant) == 0))
        {
            return;
        }

        uint16_t points = (uint16_t)atoi(&buff[15]);
        int x = atoi(&buff[20]);
        int y = atoi(&buff[24]);

        fprintf(stdout, "++%s catched a gosth in (%d,%d) and have now %u points++\n", id, x, y, points);
    }
    else if (strcmp(action, "MESSA") == 0)
    {
        action = strtok(NULL, " ");

        char *id = action;
        char *mess = &buff[16];

        fprintf(stdout, "++Message from %s : %s++\n", id, mess);
    }
    else if (strcmp(action, "ENDGA") == 0)
    {

        char *infos = strtok(NULL, " ");
        char *id = infos;

        uint16_t points = (uint16_t)atoi(&buff[15]);

        fprintf(stdout, "++The game is finish!\n%s won with %u points!++\n", id, points);
    }
}

void *multiCast(void *arg)
{
    int socketMC = *((int *)arg);

    size_t t = 5 + 8 + 200 + 3 + 2;
    char buf[t];
    while (1)
    {
        int len = recv(socketMC, buf, t, 0);
        buf[len] = '\0';
        receptMultiDiff(socketMC, buf);
    }

    close(socketMC);
    return NULL;
}

void *receptUdp(void *arg)
{
    int socketUDP = *((int *)arg);

    size_t t = 5 + 8 + 200 + 3 + 2;
    char buf[t];
    while (1)
    {
        int len = recv(socketUDP, buf, t, 0);
        buf[len] = '\0';

        char *buff = strtok(buf, "+");

        char *infos = strtok(buff, " ");
        infos = strtok(NULL, " ");

        char *id = infos;
        infos = strtok(NULL, " ");

        char *mess = &buff[15];

        fprintf(stdout, "%s sent you : %s\n", id, mess);
    }

    close(socketUDP);
    return NULL;
}

void receptWelcPos(int socketTCP) // Reception format [WELCO␣m␣h␣w␣f␣ip␣port***] et [POSIT␣id␣x␣y***]
{
    char buf[6];
    recvError(recv(socketTCP, buf, 6, 0));

    uint8_t gameID;
    recvError(recv(socketTCP, &gameID, 1, 0));
    gameID = ntohs(gameID);

    char buf1[1];
    recvError(recv(socketTCP, buf1, 1, 0));

    uint16_t height;
    recvError(recv(socketTCP, &height, 2, 0));
    hauteur = ntohs(height);

    char buf11[1];
    recvError(recv(socketTCP, buf11, 1, 0));

    uint16_t width;
    recvError(recv(socketTCP, &width, 2, 0));
    largeur = ntohs(width);

    char buf111[1];
    recvError(recv(socketTCP, buf111, 1, 0));

    uint8_t fant;
    recvError(recv(socketTCP, &fant, 1, 0));

    char buf1111[1];
    recvError(recv(socketTCP, buf1111, 1, 0));

    recvError(recv(socketTCP, addrMC, 15, 0));

    char buf1x5[1];
    recvError(recv(socketTCP, buf1x5, 1, 0));

    recvError(recv(socketTCP, portMC, 4, 0));

    char buf3[3];
    recvError(recv(socketTCP, buf3, 3, 0));

    fprintf(stdout, "Welcome in the game n.%u!\nLe labyrinthe a une height de %d et une width de %d.\nThere is %u ghost to catch.\nGood Luck!\n", gameID, hauteur, largeur, fant);

    char buf15[15];
    recvError(recv(socketTCP, buf15, 15, 0));

    char buf3x2[4];
    recvError(recv(socketTCP, buf3x2, 3, 0));
    int x = atoi(buf3x2);

    char buf1x6[1];
    recvError(recv(socketTCP, buf1x6, 1, 0));

    char buf3x3[4];
    recvError(recv(socketTCP, buf3x3, 3, 0));
    int y = atoi(buf3x3);

    char buf3x4[3];
    recvError(recv(socketTCP, buf3x4, 3, 0));

    fprintf(stdout, "You're in (%d,%d).\n", x, y);
    enPartie = true;
}

void getID()
{
    bool isok = false;
    while (!isok)
    {
        bool test = true;
        fprintf(stdout, "Choose an username with 8 char.\n");
        char *line = NULL;
        ssize_t len = 0;
        ssize_t lineSize = 0;
        lineSize = getline(&line, &len, stdin);
        if (lineSize != 9)
        {
            fprintf(stdout, "Incorrect. Please, not %ld but 8 char\n", (lineSize - 1));
        }
        else
        {
            for (int i = 0; i < 8; i++)
            {
                if (!isalnum(line[i]))
                {
                    test = false;
                    fprintf(stdout, "Incorrect. Please, the %d char is not a digital/alpha\n", (i + 1));
                }
            }
            if (test)
            {
                identifiant = malloc(8);
                char *d = strtok(line, "\n");
                strcpy(identifiant, d);
                fprintf(stdout, "   -> Your username is %s.\n", identifiant);
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
        fprintf(stdout, "Incorrect. %s doesn't containts 4 char.\n", a);
        exit(EXIT_FAILURE);
    }
    for (int i = 0; i < 4; i++)
    {
        if (!isdigit(a[i]))
        {
            fprintf(stdout, "Incorrect. %s need to be digital.\n", a);
            exit(EXIT_FAILURE);
        }
    }
}

int main(int argc, char *argv[])
{

    if (argc != 4)
    {
        fprintf(stderr, "Need to have 3 arguments to start.\n");
        exit(EXIT_FAILURE);
    }

    verifport(argv[2]);
    verifport(argv[3]);

    // recuperation du port voulu
    uint16_t portTCP = atoi(argv[2]);
    uint16_t portUDP = atoi(argv[3]);
    port = argv[3];

    //
    struct sockaddr_in address_sock;
    address_sock.sin_family = AF_INET;
    address_sock.sin_addr.s_addr = htonl(INADDR_ANY);
    address_sock.sin_port = htons(portUDP);

    getID();

    // socket tcp serveur
    int socketTCP = socket(PF_INET, SOCK_STREAM, 0);

    // socket udp client
    int socketUDP = socket(PF_INET, SOCK_DGRAM, 0);
    int r = bind(socketUDP, (struct sockaddr *)&address_sock, sizeof(struct sockaddr_in));
    if (r != 0)
    {
        perror("Bind error.\n");
        exit(-1);
    }

    // connexion au serveur
    address_sock.sin_port = htons(portTCP);
    inet_aton(argv[1], &address_sock.sin_addr);
    int sock_client = connect(socketTCP, (struct sockaddr *)&address_sock, sizeof(struct sockaddr_in));
    connectError(sock_client);

    // reception de [GAMES␣n***]
    size_t t = 10;
    char buf[t];
    recvError(recv(socketTCP, buf, t, 0));
    uint8_t n = (uint8_t)buf[6];
    fprintf(stdout, "GAMES %u\n", n);

    // reception de n message [OGAME␣m␣s***]
    recupereGames(n, socketTCP);

    bool ans = false;
    inscrit = false;
    while (!ans)
    {
        // lecture du choix du joueur
        fprintf(stdout, "\nWhat do you want to do ?\n\n");
        fprintf(stdout, "(C)reate, (J)oin x, (U)nregister,\n");
        fprintf(stdout, "(L)abyrinth's size x, List (p)layers x, List (g)ames,\n");
        fprintf(stdout, "(S)tart.\n");
        fprintf(stdout, "With x = partie's id.\n");

        // action
        char *line = NULL;
        ssize_t len = 0;
        ssize_t lineSize = 0;
        lineSize = getline(&line, &len, stdin);
        ans = actionAvantPartie(socketTCP, line);
        free(line);
    }

    receptWelcPos(socketTCP);
    struct sockaddr_in address_sockMC;
    address_sockMC.sin_family = AF_INET;
    address_sockMC.sin_port = htons(atoi(portMC));
    address_sockMC.sin_addr.s_addr = htonl(INADDR_ANY);

    char *addr = strtok(addrMC, "#");

    int *socketMultiDiff = (int *)malloc(sizeof(int));
    *socketMultiDiff = socket(PF_INET, SOCK_DGRAM, 0);
    int ok = 1;
    int r2 = setsockopt(*socketMultiDiff, SOL_SOCKET, SO_REUSEPORT, &ok, sizeof(ok));
    r2 = bind(*socketMultiDiff, (struct sockaddr *)&address_sockMC, sizeof(struct sockaddr_in));
    struct ip_mreq mreq;
    mreq.imr_multiaddr.s_addr = inet_addr(addr);
    mreq.imr_interface.s_addr = htonl(INADDR_ANY);
    r2 = setsockopt(*socketMultiDiff, IPPROTO_IP, IP_ADD_MEMBERSHIP, &mreq, sizeof(mreq));

    pthread_t th1, th2;
    pthread_create(&th1, NULL, multiCast, socketMultiDiff);
    pthread_create(&th1, NULL, receptUdp, &socketUDP);

    while (enPartie)
    {
        // lecture du choix du joueur
        fprintf(stdout, "\nWhat do you want to do ?\n\n");

        fprintf(stdout, "Go (l)eft x, Go (r)ight x, Go (d)own x, Go (u)p x,\n");
        fprintf(stdout, "(M)essage everyone q, (W)hisper to someone y  q,\n");
        fprintf(stdout, "List (p)layers, (Q)uit.\n");
        fprintf(stdout, "avec x = distance, y = player's id, q = message.\n");
        // action

        char *line = NULL;
        ssize_t len = 0;
        ssize_t lineSize = 0;
        lineSize = getline(&line, &len, stdin);
        actionEnPartie(socketTCP, line);
        free(line);
    }
}
