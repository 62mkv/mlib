static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: System monitor routines.  
 *
 *               These routines are used to set the status of processes 
 *               for system monitoring.
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

#include <mocaerr.h>
#include <mislib.h>

#include <oslib.h>

static char gAppName[60];

void misEnterProcess(char *appName)
{
    memset(gAppName, 0, sizeof(gAppName));
    strncpy(gAppName, appName, sizeof(gAppName) - 1);

    misLogInfo("Application %s started", gAppName);

    return;
}

void misExitProcess(int status)
{
    if (status == eOK)
    {
	misLogInfo("Application %s exited ok", gAppName);
    }
    else
    {
	misLogInfo("Application %s aborted, code (%d)", gAppName, status);
    }

    osExit(status);
}

char *misGetApplicationName(void)
{
    return gAppName;
}
