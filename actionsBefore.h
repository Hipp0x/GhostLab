#include <unistd.h>
#include <arpa/inet.h>

void creerPartie(int socketTCP, char identifiant[], char port[]);
void rejoindrePartie(int socketTCP, char identifiant[], char port[]);
void desinscription(int socketTCP);
void tailleLaby(int socketTCP);
void listeJoueurs(int socketTCP);
void listeParties(int socketTCP);
void recupereGames(uint8_t n, int socketTCP);
void start(int socketTCP);