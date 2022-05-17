#include <unistd.h>
#include <arpa/inet.h>
#include <stdbool.h>

void creerPartie(int socketTCP, char identifiant[], char port[]);
bool rejoindrePartie(int socketTCP, char identifiant[], char port[], uint8_t num);
void desinscription(int socketTCP);
void tailleLaby(int socketTCP, uint8_t num);
void listeJoueurs(int socketTCP, uint8_t num);
void listeParties(int socketTCP);
void recupereGames(uint8_t n, int socketTCP);
void start(int socketTCP);