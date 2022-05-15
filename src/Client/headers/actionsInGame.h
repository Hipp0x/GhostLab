#include <unistd.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <stdbool.h>
#include <poll.h>
#include <fcntl.h>

bool seDeplacer(int socketTCP, int distance, char direction);
bool quitterPartie(int socketTCP);
bool printJoueurs(uint8_t j, int socketTCP);
bool listeJoueursIG(int socketTCP);
bool envoiMessATous(int socketTCP, char *mess);
bool envoiMessAJoueur(int socketTCP, char *mess, char *id);
bool tricheLaby(int socketTCP, int w, int h);
bool tricheFant(int socketTCP);