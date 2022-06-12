#### HOW TO USE ####
# To add a file to cassini :
# - add the dir/name.o to the line CASSINI_OBJFILES
# - don't forget the .o instead of .c => the object files are created
#   automatically from the source files
#
# Don't touch anything else, the variables are here to do the work for you

JC = javac

CC ?= gcc
CLFAGS ?= -Wall


.SUFFIXES: .java .class


.java.class:
	$(JC) $*.java

%.o: %.c $(DEPS)
	$(CC) -pthread -c -o $@ $< $(CFLAGS)

CLASSES = \
	Serveur/Case.java \
	Serveur/Fantome.java \
	Serveur/Joueur.java \
	Serveur/Labyrinthe.java \
	Serveur/Partie.java \
	Serveur/Serveur.java \
	Serveur/ServiceConnexion.java \
	Serveur/ServiceJoueur.java \
	Serveur/ServicePartie.java

	# all the object files cassini needs
OBJFILES = Client/joueur.o Client/fonctions.o Client/actionsBefore.o Client/actionsInGame.o

all: codejava codec

codejava : $(CLASSES:.java=.class)

codec: $(OBJFILES)
	$(CC) $(CFLAGS) -pthread -o joueur $(OBJFILES)


# to remove the compiled files
.PHONY: clean
clean:
	rm -f joueur $(OBJFILES)
	$(RM) Serveur/*.class

