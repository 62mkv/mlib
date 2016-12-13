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

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <oslib.h>

#include "socksrvmgr.h"

long cons_KillConn(CONS *c, int argc, char *argv[])
{
    int i;

    CONN *conn;

    for (i = 0; i < argc; i++)
    {
	for (conn=ConnIdleTop; conn; conn=conn->next)
	{
	    if (atol(argv[i]) == (long) conn->fd)
	    {
		unsigned long nonblock = 0L;

		misTrc(T_MGR, "Closing connection %d at user request", conn->fd);

		/* Set socket to blocking. */
		if (0 != WSAEventSelect(conn->fd, (WSAEVENT) NULL, 0))
		{
		    misLogError("WSAEventSelect:(%d) %s", WSAGetLastError(),
				osSockError());
		}

		if (0 != ioctlsocket(conn->fd, FIONBIO, &nonblock))
		{
		    misLogError("ioctlsocket(FIONBIO): (%d) %s", WSAGetLastError(), 
				osSockError());
		}

		osSockShutdown(conn->fd);
		osSockClose(conn->fd);
		DelConnIdle(conn);
		break;
	    }
	}

	if (!conn)
	{
	    cons_printf(c, "Couldnot find connection %s\n", argv[i]);
	}
    }

    return eOK;
}
