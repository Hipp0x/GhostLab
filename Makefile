#### HOW TO USE ####
# To add a file to cassini :
# - add the dir/name.o to the line CASSINI_OBJFILES
# - don't forget the .o instead of .c => the object files are created
#   automatically from the source files
#
# Don't touch anything else, the variables are here to do the work for you

CC ?= gcc
CLFAGS ?= -Wall

# tell the linker where to find the .h files
LIBINCLUDE = include
CFLAGS += -I$(LIBINCLUDE)

# all the object files cassini needs
CASSINI_OBJFILES = joueur.o fonctions.o actionsBefore.o


%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

# compile both the client and the daemon
all: cassini

# the client
cassini: $(CASSINI_OBJFILES)
	$(CC) $(CFLAGS) -o joueur $(CASSINI_OBJFILES)


# to remove the compiled files
.PHONY: clean
clean:
	rm -f joueur $(CASSINI_OBJFILES)