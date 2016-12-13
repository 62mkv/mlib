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

#define MOCA_ALL_SOURCE		/* Required for the LOG_ macros on AIX. */

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef UNIX
#include <syslog.h>
#endif

#ifdef WIN32
#include <tchar.h>
#endif

#include <mocagendef.h>

#include "osprivate.h"

void osLogEvent(int level, char *msg)
{
#ifdef WIN32
    WORD wType;
    TCHAR szMsg[2000];
    LPTSTR lpszStrings[2];
    HANDLE hEventSource;
    char *envname;
    char source[500];


    if (level == OS_EVT_ERROR)
	wType = EVENTLOG_ERROR_TYPE;
    else if (level == OS_EVT_WARNING)
	wType = EVENTLOG_WARNING_TYPE;
    else if (level == OS_EVT_INFORM)
	wType = EVENTLOG_INFORMATION_TYPE;
    else
	return;

    _stprintf(szMsg, TEXT("%s"), msg);
    lpszStrings[0] = szMsg;
    lpszStrings[1] = NULL;

    strcpy(source, "MOCA.");
    if ((envname = osGetVar(ENV_ENVNAME)))
    {
	strcat(source, envname);
    }

    hEventSource = RegisterEventSource(NULL, source);
    
    ReportEvent(hEventSource, wType, 0, 0, NULL, 1, 0, lpszStrings, NULL);

    DeregisterEventSource(hEventSource);
#else
    static int opened;
    static char source[500];
    int priority;

    if (level == OS_EVT_ERROR)
	priority = LOG_ERR;
    else if (level == OS_EVT_WARNING)
	priority = LOG_WARNING;
    else if (level == OS_EVT_INFORM)
	priority = LOG_INFO;
    else
	return;

    if (!opened)
    {
	char *envname;

	strcpy(source, "MOCA");
	if ((envname = osGetVar(ENV_ENVNAME)))
	{
	    strcat(source, ".");
	    strcat(source, envname);
	}
	openlog(source, LOG_PID, LOG_DAEMON);
	opened=1;
    }

    syslog(priority, "%s", msg);

#endif
}
