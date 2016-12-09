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
 *  Copyright (c) 2002-2009
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
 *#END************************************************************************/

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>

#include "msql.h"

void DescribeTable(char *name)
{
    long status;

    char *command = NULL;

    RETURN_STRUCT *ret = NULL;

    /* Trim whitespace from each side of the name. */
    name = misTrimLR(name);

    /* Build the command we'll be executing. */
    misDynSprintf(&command, "describe table where table_name = '%s'", name);

    /* Execute the command. */
    status = ExecuteCommand(command, &ret);
    if (status == eOK)
    {
        Commit( );
    }
    if (status == eDB_NO_ROWS_AFFECTED || status == eSRV_NO_ROWS_AFFECTED)
    {
        printf("\nTable does not exist\n\n");
        Rollback( );
	goto cleanup;
    }
    else if (status != eOK)
    {
        printf("ERROR: An error occurred - %ld", status);
        Rollback( );
	goto cleanup;
    }

    /* Print any results from the command. */
    PrintHeadings(ret);
    PrintResults(ret);

cleanup:

    free(command);
    srvFreeMemory(SRVRET_STRUCT, ret);

    return;
}

int ExecuteShellCommand(char *command)
{
	int stat = 0; /* This could fail in legacy, but can't in NG */

    /* Trim whitespace from each side of the command. */
    command = misTrimLR(command);

    Print("\n");

    if (!command || !strlen(command))
    {
#ifdef UNIX
        system(osGetVar("SHELL") ? osGetVar("SHELL") : "/bin/sh");
#else
        system(osGetVar("COMSPEC") ? osGetVar("COMSPEC") : "cmd.exe");
#endif
    }
    else
    {
	system(command);
	/* Old behavior: Print("Non-interactive - unable to shell.\n"); */
    }

    Print("\n");

    return stat;
}
