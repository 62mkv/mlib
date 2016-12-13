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

long cons_SetMinServers(CONS *c, int argc, char *argv[])
{
    if (!argc)
    {
	cons_printf(c, "Usage: set min-servers #\n");
    }
    else
    {
	char tmp[50];
	misExpandVars(tmp, argv[0], sizeof tmp, NULL);
	param.min_servers = atoi(tmp);
    }

    return eOK;
}
