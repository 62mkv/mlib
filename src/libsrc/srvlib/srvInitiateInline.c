static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions used by the server and server applications
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2002
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
#include <string.h>
#include <stdlib.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <jnilib.h>

#include "srvprivate.h"

long srvInitiateInline(char *command, RETURN_STRUCT **ret)
{
    long status;

    RETURN_STRUCT *tempRet = NULL;

    /* Actually initiate the command. */
    status = jni_srvInitiateExecute(command, &tempRet, NULL, 1);

    /* Make sure we have a return stucture for our caller. */
    if (!tempRet)
	tempRet = srvResults(status, NULL);

    /* Point the caller's return struct to our's if they gave us one. */
    if (ret)
	*ret = tempRet;
    else if (tempRet)
	srvFreeMemory(SRVRET_STRUCT, tempRet);

    return status;
}

long srvInitiateInlineFormat(RETURN_STRUCT **ret, char *fmt, ...)
{
    long status;

    char *command;

    va_list args;

    /* Build the command. */
    va_start(args, fmt);
    command = srv_FormatCommand(fmt, args);
    va_end(args);

    /* Actually initiate the command. */
    status = srvInitiateInline(command, ret);

    /* Free the memory. */
    free(command);

    return status;
}
