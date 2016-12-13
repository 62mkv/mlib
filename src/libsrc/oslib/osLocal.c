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
# include <sys/types.h>
# include <sys/socket.h>
# include <sys/un.h>
# include <fcntl.h>
# include <unistd.h>
# include <netdb.h>
#endif

#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include <mocaerr.h>
#include "osprivate.h"

extern int os_SocketInited;

#ifdef UNIX
long osLocalListen(SOCKET_FD *desc, char *path, int backlog, int reuse)
{
    struct sockaddr_un addr;
    int trueval = 1;

    if (!os_SocketInited) osSockInit();

    /* Open socket for communication endpoint */
    if ((*desc = socket(AF_UNIX, SOCK_STREAM, 0)) == INVALID_SOCKET)
    {
	return eERROR;
    }

    if (reuse)
    {
	/* Set SO_REUSEADDR on socket so we can re-bind quickly */
	if (0 != setsockopt(*desc, SOL_SOCKET, SO_REUSEADDR,
		            (const char *)&trueval, sizeof trueval))
	{
	    return eERROR;
	}
    }

    /*
     * Bind the socket to our expected port.
     */
    addr.sun_family = AF_INET;
    strncpy(addr.sun_path, path, sizeof(addr.sun_path));

    if (SOCKET_ERROR == bind(*desc, (struct sockaddr *) &addr, sizeof addr))
    {
	return eERROR;
    }

    if (backlog == OS_MAX_BACKLOG)
    {
        /* Defined in sys/socket.h under Unix, wsock32.h under NT */
        backlog = SOMAXCONN;
    }

    if (SOCKET_ERROR == listen(*desc, backlog))
    {
	return eERROR;
    }

    return eOK;
}

long osLocalConnect(SOCKET_FD *desc, char *path)
{
    struct sockaddr_un remote_addr;

    if (!os_SocketInited) osSockInit();

    /* Open socket for communication endpoint */
    if ((*desc = socket(AF_UNIX, SOCK_STREAM, 0)) == INVALID_SOCKET)
    {
	return eERROR;
    }

    remote_addr.sun_family = AF_UNIX;
    strncpy(remote_addr.sun_path, path, sizeof(remote_addr.sun_path));

    if (SOCKET_ERROR == connect(*desc, (struct sockaddr *)&remote_addr, sizeof remote_addr))
    {
	return eERROR;
    }

    return eOK;
}
#endif
