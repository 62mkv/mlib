/*#START***********************************************************************
 *
 *  $URL$
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

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <oslib.h>
#include <srvlib.h>

#include "socksrvprc.h"

static void SwallowStream(char *command, SOCKET_FD fd)
{
    int c;
    char *ptr;
    char pathname[1024];
    misSocket *sock;
    FILE *outfile;

    /* A second argument can be provided as a pathname. */
    ptr = strchr(command, ' ');
    if (ptr)
    {
	ptr++;

        /* Expand environment variables in the pathname. */
        misExpandVars(pathname, ptr, sizeof pathname, NULL);

        /* Fix the pathname to be platform appropriate. */
        misFixFilePath(pathname);

        /* Open a debug output file. */
        outfile = fopen(pathname, "a+");
        if (!outfile)
        {
            outfile = stderr;
            misLogError("fopen: %s", osError( ));
            misLogError("Could not open debug output file");
            misLogError("Redirecting output to stderr...");
        }
    }
    else
    {
	outfile = stderr;
    }

    fprintf(outfile, "Swallowing stream from socket...\n");

    /* Open a socket buffer. */
    sock = misSockOpenFD(fd);
    if (!sock)
	return;

    /* Read each character off the socket. */
    while ((c = misSockGetc(sock)) >= 0)
	fprintf(outfile, "%c", c);

    fprintf(outfile, "\n");

    fprintf(outfile, "Swallowed stream from socket\n");

    misSockClose(sock);

    if (outfile != stderr)
        fclose(outfile);

    return;
}

long ExecuteCommand(char *command, 
	            SOCKET_FD fd, 
		    unsigned short port, 
		    int dedicated)
{   
    long status;

    char *buffer = NULL;

    /* We have a special command that we use for debugging. */
    if (misCiStrncmp(command, "debug", strlen("debug")) == 0)
    {
        SwallowStream(command, fd);
        return eOK;
    }

    /* Add the socket fd and listen port as arguments to the command. */
    misDynSprintf(&buffer, "publish data "
	                   "    where sock_fd = %ld "
			   "      and listen_port = %u "
			   " | "
			   "%s", 
	          fd,
		  port,
	          command);

    do
    {
	/* Execute the given command. */
        status = srvInitiateCommand(buffer, NULL);

	/* Commit or rollback based on the status of the command execution. */
        if (status == eOK)
            srvCommit();
        else
            srvRollback();

	/* 
	 * If we're running in dedicated mode we need to make sure the client
	 * didn't disconnect it's end of the socket. 
	 */
        if (dedicated)
        {    
            int nread;
            char buffer[2];

            /* Peek at the first byte in the stream. */
            nread = osSockRecv(fd, buffer, 1, OS_SOCK_PEEK);
            if (nread <= 0)
            {
                if (nread == 0 || osSockErrno( ) == OS_ECONNRESET)
                {
                    misTrc(T_FLOW, "Client disconnected");
                    status = eSRV_DISCONNECTED;
                }
                else
                {
                    misLogError("osSockRecv( ): %s", osSockError());
                    status = eERROR;
                }
            }
        }

    } while (dedicated && status == eOK);

    free(buffer);

    return status;
}
