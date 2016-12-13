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

long cons_Shutdown(CONS *c, int argc, char *argv[])
{
    if (argc < 1 || 0 != strcmp(argv[0], "hard"))
    {
	cons_printf(c, "Usage: shutdown hard\n");
	cons_printf(c, "    hard:    shut down, killing all pool processes and clients.\n");
	return eERROR;
    }

    shutdown_idle();

    exit(EXIT_SUCCESS);

    return eOK;
}