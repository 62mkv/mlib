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

#include "srvprivate.h"

/*
 *  FUNCTION: srvCompileCommand
 *
 *  PURPOSE:  Compile a command into a pre-parsed form to be used to execute
 *            at a later time.
 *
 *  RETURNS: eOK on success
 *
 */
long srvCompileCommand(char *command, SRV_COMPILED_COMMAND **exec)
{
    *exec = NULL;

    *exec = malloc(sizeof (SRV_COMPILED_COMMAND));
    (*exec)->command_text = strcpy(malloc(strlen(command)+1), command);

    return eOK;
}
