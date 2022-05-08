#include "headers/fonctions.h"

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
        fprintf(stdout, "///Connexion établie avec le serveur.\n");
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
        fprintf(stdout, "///Envoi de %d data.\n", ret);
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
        fprintf(stdout, "///Reception de %d data.\n", ret);
    }
}