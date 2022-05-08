# GhostLab
GhostLab game : serveur + client

--------------------------

Fonctionnement :

make  
pour lancer un serveur : java Serveur.Serveur  
pour lancer un client : ./joueur ("adresse serveur") (port tcp en numerique)  

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
Pauline : serveur + partie  
Ugo : client + création des parties (positionnement joueur + fantome ...)

Déroulement d'une partie :  
Pauline : serveur + partie  
Ugo : client