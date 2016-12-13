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
#include <oslib.h>

#include "socksrvmgr.h"

long cons_KillServer(CONS *c, int argc, char *argv[])
{
    int i,
        killhard = 0;

    long pid;

    SERV *s;

    HANDLE hPid;

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

    for (i = 0; i < argc; i++)
    {
	pid = atol(argv[i]);

	if (!pid) continue;

	if (killhard)
	{
	    if (NULL == (hPid = OpenProcess(PROCESS_TERMINATE, FALSE, pid)))
	    {
		cons_printf(c, "Unable to get process %ld handle:%s\n", 
			    pid, osError());
	    }
	    else
	    {
		if (TerminateProcess(hPid, 2))
		{
		    cons_printf(c, "Process %ld Terminated\n", pid);
		}
		else
		{
		    cons_printf(c, "Unable to terminate process %ld:%s\n",
			        pid, osError());
		}
		CloseHandle(hPid);
	    }
	}
	else
	{
	    for (s=ServFreeTop;s;s=s->next)
	    {
		if (pid == (long)s->dwPid)
		{
		    shutdown_server(s);
		    break;
		}
	    }

            if (!s)
            {
                for (s=ServBusyFirst;s;s=s->next)
                {
                    if (atol(argv[i]) == (long) s->dwPid)
                    {
                        s->closing = TRUE;
                        break;
                    }
                }
            }

	    if (!s)
	    {
		cons_printf(c, "Could not find server process %s\n", argv[i]);
	    }
	}
    }

    return eOK;
}

