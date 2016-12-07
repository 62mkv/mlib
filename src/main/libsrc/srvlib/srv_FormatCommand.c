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
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <errno.h>

#include <mocaerr.h>
#include <mislib.h>

#include "srvprivate.h"


char *srv_FormatCommand(char *fmt, va_list args)
{
    long length;

    char *command;

    /* Determine what the length of the command will be. */
    if ((length = misSprintfLen(fmt, args)) < 0)
	return NULL;

    /* Allocate space for the command. */
    if ((command = malloc(length + 1)) == NULL)
    {
	misLogError("malloc: %s", strerror(errno));
	misLogError("srv_FormatCommand: Could not allocate space for command");
	return NULL;
    }

    /* Build the command. */
    vsprintf(command, fmt, args);

    return command;
}
