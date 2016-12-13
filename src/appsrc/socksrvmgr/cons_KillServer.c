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

#include "socksrvmgr.h"
#include <signal.h>

long cons_KillServer(CONS *c, int argc, char *argv[])
{
    int ii,
        killhard = 0;

    SERV *s;

    if (argc > 0 &&
        (0 == strncmp(argv[0], "-term", 2) ||
         0 == strcmp(argv[0], "-9")))
    {
        argv++;
        argc--;
        killhard++;
    }

    if (argc == 0)
    {
	cons_printf(c, "Usage: kill server <pid> [ <pid>]...\n"
		       " where <pid> is the process ID of a running server\n");
	return eOK;
    }

    for (ii = 0; ii < argc; ii++)
    {
        for (s = ServFreeTop; s; s = s->next)
        {
            if (atol(argv[ii]) == s->pid)
            {
                if (killhard)
                    kill(s->pid, SIGTERM);
                else
                    shutdown_server(s);
                break;
            }
        }

        if (!s)
        {
            for (s = ServBusyFirst; s; s = s->next)
            {
                if (atol(argv[ii]) == s->pid)
                {
                    if (killhard)
                        kill(s->pid, SIGTERM);
                    else
                        s->closing = 1;
                    break;
                }
            }
        }

        if (!s)
            cons_printf(c, "Could not find server process %s\n", argv[ii]);
    }

    return eOK;
}
