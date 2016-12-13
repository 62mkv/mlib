static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to support tracing.
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
#include <stdarg.h>
#include <string.h>
#include <ctype.h>
#include <time.h>
#include <limits.h>

#include <mocagendef.h>
#include <mocaerr.h>
#include <mislib.h>
#include <oslib.h>

#include "../jnilib/miscalls.h"

static char traceFilePathname[1024];

static char traceLevelArg[] =
    {'W', 'S', 'M', 'X', 'A', 'R'};

static char *traceLevelStr[] =
    {"T-FLW", "T-SQL", "T-MGR", "T-SRV", "T-ARG", "T-PRF"};

static char *traceOptionsStr =
    "\n"
    "Trace Level Switches\n"
    "   W - Application Flow Messages\n"
    "   M - Manager Flow Messages\n"
    "   X - Server Flow Messages\n"
    "   A - Server Arguments\n"
    "   S - SQL Calls\n"
    "   R - Performance Statistics\n";

int misGetTraceLevelsString(char ***lvls, char **args)
{
    *lvls = traceLevelStr;
    *args = traceLevelArg;

    return sizeof(traceLevelArg);
}

char *misGetTraceOptionsString(void)
{
    return traceOptionsStr;
}

void misSetTraceLevelFromArg(char *optarg)
{
    if (strchr(optarg, '*'))
    {
	misSetTraceLevel(T_FLOW|T_SQL|T_MGR|T_SERVER|T_SRVARGS|T_PERF|T_PID, 1);
    }
    else 
    {
	misSetTraceLevel(T_FLOW,
			 strchr(optarg, 'W') || strchr(optarg, 'w') ? 1 : 0);
	misSetTraceLevel(T_SQL,
			 strchr(optarg, 'S') || strchr(optarg, 's') ? 1 : 0);
	misSetTraceLevel(T_MGR,
			 strchr(optarg, 'M') || strchr(optarg, 'm') ? 1 : 0);
	misSetTraceLevel(T_SERVER,
			 strchr(optarg, 'X') || strchr(optarg, 'x') ? 1 : 0);
	misSetTraceLevel(T_SRVARGS,
			 strchr(optarg, 'A') || strchr(optarg, 'a') ? 1 : 0);
	misSetTraceLevel(T_PERF,
			 strchr(optarg, 'R') || strchr(optarg, 'r') ? 1 : 0);
	misSetTraceLevel(T_PID,
			 strchr(optarg, 'P') || strchr(optarg, 'p') ? 1 : 0);
    }
}

void misSetTraceFile(char *inTraceFilePathname, char *mode)
{
    char temp[1024];

    /* Set the new trace file pathnames. */
    if (inTraceFilePathname && *inTraceFilePathname)
    {
	/* 
	 *  Set the new trace file. 
	 *  Prepend the log directory if we don't have a full pathname. 
	 */
	if (!strchr(inTraceFilePathname, '/') && 
	    !strchr(inTraceFilePathname, '\\'))
        {
	    sprintf(traceFilePathname, "$LESDIR/log/%s", inTraceFilePathname);
        }
	else
	{
	    sprintf(traceFilePathname, "%s", inTraceFilePathname);
        }
    }

    /* Open the new trace file. */
    if (inTraceFilePathname && *inTraceFilePathname)
    {
	/* Clean up the pathname. */
	misExpandVars(temp, traceFilePathname, sizeof(temp), NULL);
	misFixFilePath(temp);
	misTrim(temp);

	/* Open the trace file. */
        jni_misSetTraceFileName(temp, mode);
    }

    return;
}

char *misGetTraceFile(void)
{
    return traceFilePathname;
}

FILE *misGetTraceFilePointer(void)
{
    /* Does it make sense to have this anymore */
    return stdout;
}

void misResetTraceLevel(void)
{
    /* Now we pass in the max int and disable every level */
    misSetTraceLevel(INT_MAX, 0);
}

void misSetTraceLevel(int bitmask, int onoff)
{
    /* Get the current trace level from Java */
    int traceLevel = misGetTraceLevel();
    /* 
       Now we do a bit operation on the value to disable or enable it 
       appropriately
     */
    if (onoff)
	traceLevel = traceLevel | bitmask;
    else
	traceLevel = traceLevel & (~bitmask);
	
    jni_misSetTraceLevel(traceLevel);
}

int misGetTraceLevel(void)
{
    return jni_misGetTraceLevel();
}

void misVTrc(int level, char *format, va_list args)
{
    char *msg = NULL;
    long length;

    /* Determine the length of the buffer. */
    if ((length = misSprintfLen(format, args)) < 0)
        goto cleanup;

    /* Allocate space for the msg. */
    if ((msg = malloc(length + 1)) == NULL)
        goto cleanup;

    vsprintf(msg, format, args);

    jni_misTrace(level, msg);

cleanup:
    free(msg);
}

void misTrc(int level, char *format, ...)
{
    va_list args;

    va_start(args, format);

    misVTrc(level, format, args);

    va_end(args);
}
