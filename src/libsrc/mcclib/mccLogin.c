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

#define MOCA_ALL_SOURCE

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mcclib.h>
#include <sqllib.h>

#include "mccprivate.h"

long MOCAEXPORT mccLogin(mccClientInfo *client, char *userid, char *password)
{
    /* Validate our arguments. */
    if (!client || !userid || !password)
	return eINVALID_ARGS;

    return jni_mccLogin(client->mocaClientAdapter, userid, password, "");
}

long MOCAEXPORT mccLoginWithClientKey(mccClientInfo *client, char *userid, char *password, char *clientKey)
{
    /* Validate our arguments. */
    if (!client || !userid || !password)
	return eINVALID_ARGS;

    return jni_mccLogin(client->mocaClientAdapter, userid, password, clientKey);
}
