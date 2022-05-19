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

char port[4] = "5467";
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

// les actions que peut faire le joueur avant le debut d'une partie
bool actionAvantPartie(int socketTCP, char *ch)
{
    char *choix = ch;
    char *l;
    uint8_t num;
    switch (choix[0])
    {
    case 'c':; // creer une partie
        if (strlen(ch) == 2)
        {
            creerPartie(socketTCP, identifiant, port);
            inscrit = true;
        }
        else
        {
            fprintf(stdout, "Respectez le format : c.\n");
        }
        return false;
        break;

    case 'r':; // rejoindre une partie
        if (ch[1] == ' ')
        {
            l = choix + 1;
            if (!isDigit(l, strlen(l)))
            {
                fprintf(stdout, "Vous devez entrer un chiffre, et non :%s.\n", l);
            }
            else
            {
                num = atoi(l);
                inscrit = rejoindrePartie(socketTCP, identifiant, port, num);
            }
        }
        else
        {
            fprintf(stdout, "Respectez le format : r entier.\n");
        }
        return false;
        break;

    case 'd':; // desinscription d'une partie
        if (strlen(ch) == 2)
        {
            desinscription(socketTCP);
            inscrit = false;
        }
        else
        {
            fprintf(stdout, "Respectez le format : d.\n");
        }
        return false;
        break;

    case 't':; // taille labyrinthe de la partie m
        if (ch[1] == ' ')
        {
            l = choix + 1;
            if (!isDigit(l, strlen(l)))
            {
                fprintf(stdout, "Vous devez entrer un chiffre, et non :%s.\n", choix);
            }
            else
            {
                num = atoi(l);
                fprintf(stdout, "num apres atoi : %d\n", num);
                tailleLaby(socketTCP, num);
            }
        }
        else
        {
            fprintf(stdout, "Respectez le format : t entier.\n");
        }
        return false;
        break;

    case 'j':; // liste joueurs de la partie partie m
        if (ch[1] == ' ')
        {
            l = choix + 1;
            if (!isDigit(l, strlen(l)))
            {
                fprintf(stdout, "Vous devez entrer un chiffre, et non :%s.", l);
            }
            else
            {
                num = atoi(l);
                listeJoueurs(socketTCP, num);
            }
        }
        else
        {
            fprintf(stdout, "Respectez le format : j entier.\n");
        }
        return false;
        break;

    case 'p':; // liste parties qui n'ont pas encore commencé
        if (strlen(ch) == 2)
        {
            listeParties(socketTCP);
        }
        else
        {
            fprintf(stdout, "Respectez le format : p.\n");
        }
        return false;
        break;

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
                fprintf(stdout, "Vous ne pouvez faire ca !\n");
                return false;
            }
        }
        else
        {
            fprintf(stdout, "Respectez le format : s.\n");
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
        char *s = "***";
        choix = strtok(NULL, s);
        fprintf(stdout, "mess : %s", choix);
        envoiMessATous(socketTCP, &ch[2]);
        break;

    case 'w':; // Message à un joueur
        choix = strtok(NULL, sep);
        char *id = choix;
        choix = strtok(NULL, sep);
        envoiMessAJoueur(socketTCP, choix, id);
        break;

    case 'x':;
        if (strcmp(ch, "xtrichexlabyx"))
        {
            tricheLaby(socketTCP, largeur, hauteur);
        }
        else if (strcmp(ch, "xtrichexfantx"))
        {
            tricheFant(socketTCP);
        }
        else
        {
            fprintf(stdout, "Ce n'est pas correct.\n");
        }
        break;
    default:;
        fprintf(stdout, "Ce n'est pas correct.\n");
        break;
    }
}

void receptMultiDiff(int socketMultiDiff, char *received)
{
    char *buff = strtok(received, "+    ");
    char *action = strtok(buff, " ");
    printf("%s has been received\n", action);
    if (strcmp(action, "GHOST") == 0)
    {
        
        int x = atoi(&buff[6]);
        int y = atoi(&buff[10]);

        fprintf(stdout, "Un fantome s'est déplacé en (%d,%d).", x, y);
    }
    else if (strcmp(action, "SCORE") == 0)
    {
        char *infos = strtok(NULL, " ");
        char *id = infos;

        uint16_t points = (uint16_t) atoi(&buff[15]);
        int x = atoi(&buff[20]);
        int y = atoi(&buff[24]);

        fprintf(stdout, "%s a attrapé un fantome en (%d,%d) et a maintenant %u points", id, x, y, points);
    }
    else if (strcmp(action, "MESSA") == 0)
    {
        char *infos = strtok(NULL, " ");
        char *id = infos;

        char *mess = &buff[15];

        fprintf(stdout, "Message de %s: %s\n", id, mess);
    }
    else if (strcmp(action, "ENDGA") == 0)
    {
        
        char *infos = strtok(NULL, " ");
        char *id = infos;

        uint16_t points = (uint16_t) atoi(&buff[15]);

        fprintf(stdout, "La partie est terminée!\n%s a gagné avec %u points!", id, points);
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

        fprintf(stdout, "%s vous a envoyé : %s\n", id, mess);
    }

    close(socketUDP);
    return NULL;
}

void receptWelcPos(int socketTCP) // Reception format [WELCO␣m␣h␣w␣f␣ip␣port***] et [POSIT␣id␣x␣y***]
{
    size_t t = 5 + 1 + 2 + 2 + 1 + 15 + 4 + 3 + 6; // 39
    char buf[t];
    recvError(recv(socketTCP, buf, t, 0));
    uint8_t gameID = atoi(&buf[6]);
    hauteur = atoi(&buf[8]);
    largeur = atoi(&buf[11]);
    uint8_t nbFantomes = atoi(&buf[14]);
    char *multi = strtok(&buf[16], " ");
    memmove(addrMC, multi, 15);
    addrMC[15] = '\0';

    multi = strtok(NULL, "***");
    memmove(portMC, multi, 4);

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

    receptWelcPos(socketTCP);
    struct sockaddr_in address_sockMC;
    address_sockMC.sin_family = AF_INET;
    address_sockMC.sin_port = htons(8448);
    address_sockMC.sin_addr.s_addr = htonl(INADDR_ANY);

    int *socketMultiDiff = (int *) malloc(sizeof(int));
    *socketMultiDiff = socket(PF_INET, SOCK_DGRAM, 0);
    int ok = 1;
    int r2=setsockopt(*socketMultiDiff,SOL_SOCKET,SO_REUSEPORT,&ok,sizeof(ok));
    r2=bind(*socketMultiDiff,(struct sockaddr *)&address_sockMC,sizeof(struct sockaddr_in));
    struct ip_mreq mreq;
    mreq.imr_multiaddr.s_addr = inet_addr("229.100.100.0");
    mreq.imr_interface.s_addr=htonl(INADDR_ANY);
    r2=setsockopt(*socketMultiDiff,IPPROTO_IP,IP_ADD_MEMBERSHIP,&mreq,sizeof(mreq));

    pthread_t th1, th2;
    pthread_create(&th1, NULL, multiCast, socketMultiDiff);
    pthread_create(&th1, NULL, receptUdp, &socketUDP);

    while (enPartie)
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
        actionEnPartie(socketTCP, line);
        free(line);
    }
}
