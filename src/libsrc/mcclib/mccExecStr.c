static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Function to execute a command.
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
 *#END*************************************************************************/

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mcclib.h>

#include "mccprivate.h"

long MOCAEXPORT mccExecStr(mccClientInfo *client, char *cmd, mocaDataRes **res)
{
    /* Validate our arguments. */
    if (!client || !cmd)
	return eINVALID_ARGS;

    return jni_mccExecStr(client->mocaClientAdapter, cmd, res);
}

long MOCAEXPORT mccExecStrWithApp(mccClientInfo *client, 
				  char *cmd, 
                                  char *appId,
                                  mocaDataRes **res)
{
    long status;

    char *savedAppId;

    /* Validate our arguments. */
    if (!client || !cmd)
	return eINVALID_ARGS;

    /* Save off the current application id. */
    savedAppId = client->appId;

    /* Set the application id for this command execution. */
    if (appId != NULL)
        mccSetApplicationID(client, appId);

    /* Execute the given command. */
    status = mccExecStr(client, cmd, res);

    /* Put back the saved application id back. */
    if (appId != NULL)
        mccSetApplicationID(client, savedAppId);

    return status;
}
