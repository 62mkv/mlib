static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Function to set the 'default' application id of a connection.
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

#define MOCA_ALL_SOURCE

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mcclib.h>
#include <mislib.h>

#include "mccprivate.h"

long MOCAEXPORT mccSetApplicationID(mccClientInfo *client, char *appId)
{
    /* Validate our arguments. */
    if (!client || !appId)
	return eINVALID_ARGS;

    /* Free any existing application ID. */
    if (client->appId)
	free(client->appId);

    /* Populate the new application id. */
    client->appId = malloc(strlen(appId ? appId: "") + 1);
    strcpy(client->appId, appId ? appId : "");

    jni_mccSetApplicationId(client->mocaClientAdapter, appId);

    return eOK;
}
