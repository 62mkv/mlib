static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: 
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
 *  Sam Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by Sam Corporation.
 *
 *  Sam Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by Sam Corporation.
 *
 *  $Copyright-End$
 *
 *#END*************************************************************************/


#define MOCA_ALL_SOURCE

#include <moca.h>

#ifdef UNIX

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <signal.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/msg.h>

#include <mocaerr.h>

typedef struct { long mtype; char mbuff[1]; } MY_MSGBUF;

int main(int argc, char *argv[])
{
    int pipefd;
    int queue_id;
    long max_msg;
    long nbytes;
    MY_MSGBUF *buffer;
    char *pipebuffer;
    int headlen;

    if (argc < 5)
    {
	fprintf(stderr, "Invalid Startup arguments\n");
	exit(EXIT_FAILURE);
    }
    pipefd = atol(argv[2]);
    queue_id = atol(argv[3]);
    max_msg = atol(argv[4]);

    buffer = malloc(sizeof(MY_MSGBUF)+max_msg-1);
    pipebuffer = malloc(max_msg + 9);

    while ((nbytes = msgrcv(queue_id, buffer, max_msg, 0, MSG_NOERROR)) != -1)
    {
	sprintf(pipebuffer, "%08lx|", nbytes);
	headlen = strlen(pipebuffer);
	memcpy(pipebuffer+headlen, buffer->mbuff, nbytes);
	if ((nbytes+headlen) != write(pipefd, pipebuffer, nbytes+headlen))
	{
	    fprintf(stderr, "Error writing to pipe\n");
	    exit(EXIT_FAILURE);
	}
    }

    fprintf(stderr, "message queue error\n");
    exit(EXIT_FAILURE);
}
#endif
