/*#START***********************************************************************
 *
 *  $URL$
 *  $Author$
 *
 *  Description: 
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 *
 *#END*************************************************************************/

#include "socksrvmgr.h"

long cons_KillConn(CONS *c, int argc, char *argv[])
{
    int ii;

    CONN *conn;

    for (ii = 0; ii < argc; ii++)
    {
	for (conn = ConnIdleTop; conn; conn = conn->next)
	{
	    if (atol(argv[ii]) == conn->fd)
	    {
		osSockShutdown(conn->fd);
		osSockClose(conn->fd);
		FD_CLR(conn->fd, &readfds);
		DelConnIdle(conn);
		break;
	    }
	}

	if (!conn)
	    cons_printf(c, "Could not find connection %s\n", argv[ii]);
    }

    return eOK;
}
