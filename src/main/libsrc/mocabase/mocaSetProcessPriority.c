static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Function to turn tracing on/off for a server process.
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
#include <stdlib.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <srvlib.h>


/*
 *  Priority Definitions
 */

#define MY_PRIORITY_LOW     "low"
#define MY_PRIORITY_NORMAL  "normal"
#define MY_PRIORITY_HIGH    "high"

long gOldPriority;

static void sResetProcessPriority(void *dummy)
{
    /* Reset our original priority level. */
    osSetProcessPriority(gOldPriority);

    return;
}

LIBEXPORT 
RETURN_STRUCT *mocaSetProcessPriority(char *priority)
{
    long mocaPriority;

    /* Translate our priority to the MOCA priority. */
    if (misCiStrcmp(priority, MY_PRIORITY_LOW) == 0)
    {
        mocaPriority = MOCA_PRIORITY_LOW;
    }
    else if (misCiStrcmp(priority, MY_PRIORITY_NORMAL) == 0)
    {
        mocaPriority = MOCA_PRIORITY_NORMAL;
    }
    else if (misCiStrcmp(priority, MY_PRIORITY_HIGH) == 0)
    {
        mocaPriority = MOCA_PRIORITY_HIGH;
    }
    else
    {
        misLogWarning("Invalid priority given - using default value");
        mocaPriority = MOCA_PRIORITY_NORMAL;
    }

    /* Set our new priority level. */
    gOldPriority = osSetProcessPriority(mocaPriority);
    if (gOldPriority == 0)
    {
        misLogError("Could not set process priority");
        return srvErrorResults(eERROR, "Could not set process priority", NULL);
    }

    /* Register ourselves so we can reset our priority class. */
    srvExecuteAfterCommit(sResetProcessPriority, NULL);
    srvExecuteAfterRollback(sResetProcessPriority, NULL);

    return srvResults(eOK, NULL);
}
