# GhostLab
Groupe 31

--------------------------

Fonctionnement :

Pour compiler :  (En étant dans le dossier src)
make

Pour executer :  (En étant dans le dossier src)
pour lancer un serveur : java Serveur.Serveur (adresse serveur) (port serveur)
    ex : java Serveur.Serveur 123.45.67.890 1234
pour lancer un client : ./joueur (adresse serveur) (port tcp en numerique) (port upd en numerique)  
    ex :  ./joueur 123.45.67.890 1234 4242   

Pour nettoyer :  (en étant dans le dossier src)
make clean  

--------------------------

Utilisation :

Pour jouer avec un client, nous avons mis en place une interface.
textuelle afin de rendre l'interaction avec le serveur plus simple.
Il suffit simplement de suivre les instructions écrites sur le terminal.
et de rentrer les choix voulus.

--------------------------

Architecture :

La partie Serveur se décompose en 2 services :
Un service pour les connexions des joueurs :
    A chaque nouveau joueur, un nouveau thread est créé.
    La gestion des entrées se fait en mode bloquant (TCP).
Un service pour les parties : 
    A chaque début de partie, un nouveau thread est créé.
    La gestion des entrées se fait en mode non bloquant.

La partie Client fonctionne en mode bloquant (TCP) avant d'entrer dans une partie.
Lorsqu'un client se trouve dans une partie,
2 threads sont créés pour la reception du multicast et de l'UDP.
Le TCP ne change pas.

--------------------------

Extensions :

Commandes de triches afin de voir le labyrinthe dans son entierté
-> coté client, écrire : "xtrichexlabyx"
