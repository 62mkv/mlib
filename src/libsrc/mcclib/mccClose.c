static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Function to close a client connection.
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
#include <stdlib.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mcclib.h>

#include "mccprivate.h"

long MOCAEXPORT mccClose(mccClientInfo *client)
{
    /* Make sure we have a client to work with. */
    if (!client)
        return eINVALID_ARGS;

    /* Cleanup everything associated with JNI. */
    jni_mccClose(client->mocaClientAdapter);

    /* Cleanup dynamically allocated memory. */
    free(client->msg);
    free(client->appId);
    free(client->sessionId);
    free(client); 

    return eOK;
}
