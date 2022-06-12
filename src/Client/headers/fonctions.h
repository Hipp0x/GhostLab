#include <string.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

void connectError(int ret);
void sendError(int ret);
void recvError(int ret);