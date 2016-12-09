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

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>

#include <mocaerr.h>
#include <mocagendef.h>

#include "socksrvmgr.h"

long cons_ShowOptions(CONS *c, int argc, char *argv[])
{
    int ii;

    cons_printf(c, "Normal Port:     %d\n"
		   "Console Port:    %d\n"
		   "Min Servers:     %d\n"
		   "Max Servers:     %d\n",
		   param.port, 
		   param.console_port,
		   param.min_servers, 
		   param.max_servers);

    for (ii=0; ii < param.nopts; ii++)
	cons_printf(c, "%s ", param.opts[ii]);

    cons_printf(c, "\n");

    return eOK;
}
