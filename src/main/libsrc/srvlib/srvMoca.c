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
 *#END************************************************************************/

#include <moca.h>

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <oslib.h>
#include <mcclib.h>

#include "srvprivate.h"

static mccClientInfo *gServerConnection;

long srvMoca_InitConnection(char *url, char *port, char *env)
{
    long status = eOK;
    char skey[100];
    char *temp = NULL;

    /* Spin up a JVM, which we'll need for both client and serve-mode. */
    status = srvInitialize("DAEMON", 1);
    if (status != eOK)
    {
	misLogError("Could not initialize process");
	return status;
    }

    /* We don't do anything if the caller just wants server mode. */
    if (!url || !strlen(url))
	return eOK;

    /* Generate a session keyu. */
    srvMakeSessionKey("DAEMON", skey, sizeof skey);

    /* Add the session key to the environment string. */
    misDynSprintf(&temp, "SESSION_KEY=%s", skey);

    /* Add the user's environment to the environment string. */
    if (env && strlen(env))
    {
        misDynStrcat(&temp, ":");
        misDynStrcat(&temp, env);
    }

    /* Initialize the client connection. */
    gServerConnection = mccInit(url, 0, temp);
    if (!gServerConnection)
    {
	status = eMCC_FAILED_TO_CONNECT;
	goto cleanup;
    }

    /* Ping the server to make sure we actually connected. */
    status = mccExecStr(gServerConnection, "ping", NULL);
    if (status != eOK)
    {
	status = eMCC_FAILED_TO_CONNECT;
	goto cleanup;
    }

cleanup:
    
    free(temp);

    return status;
}

long srvMoca_Execute(char *command, mocaDataRes **res)
{
    long status;

    RETURN_STRUCT *ret;

    if (gServerConnection)
    {
	return mccExecStr(gServerConnection, command, res);
    }
    else
    {
	status = srvInitiateCommand(command, &ret);

	if (ret && res)
	{
	    *res = ret->ReturnedData;
	    ret->ReturnedData = NULL;
	}
	    
	srvFreeMemory(SRVRET_STRUCT, ret);

	return status;
    }
}

void srvMoca_CloseConnection(void)
{
    if (gServerConnection)
	mccClose(gServerConnection);

    gServerConnection = NULL;
}
