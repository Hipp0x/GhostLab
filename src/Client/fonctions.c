#include "headers/fonctions.h"

// gestion d'erreur pour la connexion
void connectError(int ret)
{
    if (ret != 0)
    {
        fprintf(stdout, "Connexion problem : %s", strerror(errno));
        close(ret);
        exit(EXIT_FAILURE);
    }
}

// gestion d'erreur pour l'envoi
void sendError(int ret)
{
    if (ret <= 0)
    {
        fprintf(stderr, "Sending problem : %s", strerror(errno));
        exit(EXIT_FAILURE);
    }
}

// gestion d'erreur pour la reception
void recvError(int ret)
{
    if (ret <= 0)
    {
        fprintf(stderr, "Reception problem, ret = %d : %s", ret, strerror(errno));
        exit(EXIT_FAILURE);
    }
}