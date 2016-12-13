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
#include <string.h>
#include <stdlib.h>

#include <mocaerr.h>

#include "srvprivate.h"

/*
 * Initiates a command that has previously been compiled.
 */
long srvInitiateCompiled(SRV_COMPILED_COMMAND *compiled, mocaBindList *bindList,
                         RETURN_STRUCT **ret, short useContext)
{
    long status;

    RETURN_STRUCT *tempRet = NULL;

    /* Actually initiate the command. */
    status = jni_srvInitiateExecute(compiled->command_text, &tempRet, bindList,
                                    useContext);

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
