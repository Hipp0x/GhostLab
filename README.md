# GhostLab
GhostLab game : serveur + client

--------------------------

Fonctionnement :

make  
pour lancer un serveur : java Serveur.Serveur (adresse serveur) (port serveur)
pour lancer un client : ./joueur (adresse serveur) (port tcp en numerique) (port upd en numerique)  
    ex :  ./joueur "127.0.01" 5621 4242   

make clean  

--------------------------

Code :

En C : joueur (client)  
En Java : serveur (+ services), partie, labyrinthe (+ case), fantome, joueur (objet)  

--------------------------

Extensions :

...

--------------------------

Répartition du travail :  
  
Avant de commencer une partie :  
Pauline : joueur  
Ugo : serveur (+ tout ce qui est lié)

Début d'une partie :  
Pauline : serveur + partie + creation labyrinthes   
Ugo : client + création des parties (positionnement joueur + fantome ...)  

Déroulement d'une partie :  
Pauline : serveur + partie   
Ugo : client + correction pb non bloquant   