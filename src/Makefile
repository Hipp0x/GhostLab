#### HOW TO USE ####
# To add a file to cassini :
# - add the dir/name.o to the line CASSINI_OBJFILES
# - don't forget the .o instead of .c => the object files are created
#   automatically from the source files
#
# Don't touch anything else, the variables are here to do the work for you

JFLAGS = -g
JC = javac

CC ?= gcc
CLFAGS ?= -Wall


.SUFFIXES: .java .class


.java.class:
	$(JC) $(JFLAGS) $*.java

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

CLASSES = \
	Serveur/Joueur.java \
	Serveur/Partie.java \
	Serveur/Serveur.java \
	Serveur/ServiceJoueur.java

	# all the object files cassini needs
OBJFILES = Client/joueur.o Client/fonctions.o Client/actionsBefore.o

all: codejava codec

codejava : $(CLASSES:.java=.class)

codec: $(OBJFILES)
	$(CC) $(CFLAGS) -o joueur $(OBJFILES)


# to remove the compiled files
.PHONY: clean
clean:
	rm -f joueur $(OBJFILES)
	$(RM) Serveur/*.class

