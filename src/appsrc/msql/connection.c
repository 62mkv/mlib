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
#include <mcclib.h>
#include <mislib.h>
#include <sqllib.h>

#include "msql.h"

static mccClientInfo *client;

/*
 * Connect
 */

static long sConnect_Client(char *url, char *inUsername, char *inPassword, char *clientKey)
{
    long status;

    char tempUsername[200], 
	 tempPassword[200];

    char *username = inUsername,
         *password = inPassword;

    Print("Connecting to service: %s\n\n", url);

    /* Connect to the server. */
    client = mccInit(url, 0, NULL);
    if (!client)
    {
        Print("ERROR: Could not connect to server\n");
	return eERROR;
    }

    /* Turn on autocommit and set an application id. */
    mccEnableAutoCommit(client);
    mccSetApplicationID(client, "msql");

    /* Prompt for a user id if necessary. */
    if (!username)
    {
	Print("Login: ");
	ReadInput(tempUsername, sizeof tempUsername, stdin);
	misTrim(tempUsername);
	username = tempUsername;
    }

    /* Prompt for a password if necessary. */
    if (!password)
    {
	Print("Password: ");
	SetEchoMode(0);
	ReadInput(tempPassword, sizeof tempPassword, stdin);
	misTrim(tempPassword);
	password = tempPassword;
	SetEchoMode(1);
	Print("\n");
    }

    /* Login the user. */
    status = mccLoginWithClientKey(client, username, password, clientKey);
    if (status != eOK)
    {
        Print("Login failed: %s\n", mccErrorMessage(client));
	return eERROR;
    }
   
    return eOK;
}

static long sConnect_Server(void)
{
    long status;

    Print("Running in server mode...\n\n");

    /* Initialize as a server application. */
    status = srvInitialize("msql", (gServerMode == 1));
    if (status != eOK)
    {
        Print("ERROR: Could not initialize server\n");
	return eERROR;
    }

    return eOK;
}

long Connect(char *url, char *userid, char *password, char *clientKey)
{
    if (gServerMode)
	return sConnect_Server( );
    else
	return sConnect_Client(url, userid, password, clientKey);
}

/*
 * Close
 */

static void sClose_Client(void)
{
    long status;

    mocaDataRes *res = NULL;

    /* Commit any outstanding transaction. */
    status = mccExecStr(client, "commit", &res);
    if (status != eOK)
    {
        if (res && res->Message)
            Print("%s\n", res->Message);

        Print("ERROR: %ld - Could not commit outstanding transaction\n",status);
    }

    sqlFreeResults(res);

    mccClose(client);
    client = NULL;
}

static void sClose_Server(void)
{
    long status;

    /* Commit any outstanding transaction. */
    status = srvCommit( );
    if (status != eOK)
        Print("ERROR: %ld - Could not commit\n", status);
}

void Close(void)
{
    if (gServerMode)
	sClose_Server( );
    else
	sClose_Client( );
}

/*
 * ExecuteCommand
 */

static long sExecuteCommand_Client(char *command, RETURN_STRUCT **ret)
{
    long status;

    mocaDataRes *res = NULL;

    status = mccExecStr(client, command, &res);

    *ret = srvAddSQLResults(res, status);

    return status;
}

static long sExecuteCommand_Server(char *command, RETURN_STRUCT **ret)
{
    return srvInitiateCommand(command, ret);
}

long ExecuteCommand(char *command, RETURN_STRUCT **ret)
{
    if (gServerMode)
	return sExecuteCommand_Server(command, ret);
    else
	return sExecuteCommand_Client(command, ret);
}

/*
 * Commit
 */

static void sCommit_Client(void)
{
    /* The client is committed on the server-side automatically. */
    return;
}

static void sCommit_Server(void)
{
    srvCommit( );
}

void Commit(void)
{
    if (!gAutoCommit)
        return;

    if (gServerMode)
	sCommit_Server( );
    else
	sCommit_Client( );
}

/*
 * Rollback
 */

static void sRollback_Client(void)
{
    /* The client is rolled back on the server-side automatically. */
    return;
}

static void sRollback_Server(void)
{
    srvRollback( );
}

void Rollback(void)
{
    if (!gAutoCommit)
        return;

    if (gServerMode)
	sRollback_Server( );
    else
	sRollback_Client( );
}

/*
 * SetAutoCommit
 */

static void sSetAutoCommit_Client(long on)
{
    if (on)
	mccEnableAutoCommit(client);
    else
	mccDisableAutoCommit(client);
}

static void sSetAutoCommit_Server(long on)
{
    return;
}

void SetAutoCommit(long on)
{
    if (on)
	gAutoCommit = 1;
    else
	gAutoCommit = 0;

    if (gServerMode)
	sSetAutoCommit_Server(on);
    else
	sSetAutoCommit_Client(on);
}

/*
 * SetEnvironment
 */

static void sSetEnvironment_Client(char *env)
{
    mccSetupEnvironment(client, env);
}

static void sSetEnvironment_Server(char *env)
{
    return;
}

void SetEnvironment(char *env)
{
    if (gServerMode)
	sSetEnvironment_Server(env);
    else
	sSetEnvironment_Client(env);
}

