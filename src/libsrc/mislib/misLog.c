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
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <errno.h>

#include <oslib.h>
#include <mocaerr.h>
#include <mislib.h>

#include "../jnilib/miscalls.h"

#define LOGMSG_ERROR       1
#define LOGMSG_WARNING     2
#define LOGMSG_INFO        3
#define LOGMSG_DEBUG       4
#define LOGMSG_UPDATE      5

long gErrorCount = 0;
char **gErrorMessage = NULL;

static void misLogMessage(long Type, char *LogMsg)
{
    if (Type == LOGMSG_ERROR)
    osLogEvent(OS_EVT_ERROR, LogMsg);
    else if (Type == LOGMSG_WARNING)
    osLogEvent(OS_EVT_WARNING, LogMsg);
    else if (Type == LOGMSG_INFO)
    osLogEvent(OS_EVT_INFORM, LogMsg);
    else
    {
    /*
     * By default, we do nothing here.
     */
    }
}

static void mis_LogMessage(long type, char *fmt, va_list args)
{
    char *msg = NULL;
    long length;

    if ((length = misSprintfLen(fmt, args)) < 0)
        goto cleanup;

    /* Allocate space for the msg. */
    if ((msg = malloc(length + 1)) == NULL)
        goto cleanup;

    vsprintf(msg, fmt, args);

    if (type == LOGMSG_ERROR)
        jni_misLog(4, msg);
    else if (type == LOGMSG_WARNING)
        jni_misLog(3, msg);
    else if (type == LOGMSG_INFO)
        jni_misLog(2, msg);
    /* We default everything to debug if it isn't other types */
    else
        jni_misLog(1, msg);

cleanup:
    free(msg);
}

void misLogDebug(char *Format,...)
{
    va_list Arguments;

    va_start(Arguments, Format);
    mis_LogMessage(LOGMSG_DEBUG, Format, Arguments);
    va_end(Arguments);

    misClearErrorStack( );
}

void misLogInfo(char *Format,...)
{
    va_list Arguments;

    va_start(Arguments, Format);
    mis_LogMessage(LOGMSG_INFO, Format, Arguments);
    va_end(Arguments);

    misClearErrorStack( );
}

void misLogWarning(char *Format,...)
{
    va_list Arguments;

    va_start(Arguments, Format);
    mis_LogMessage(LOGMSG_WARNING, Format, Arguments);
    va_end(Arguments);

    misClearErrorStack( );
}

void misLogError(char *Format,...)
{
    va_list Arguments;

    va_start(Arguments, Format);
    mis_LogMessage(LOGMSG_ERROR, Format, Arguments);
    va_end(Arguments);

    misClearErrorStack( );
}

void misLogUpdate(char *Format,...)
{
    va_list Arguments;

    va_start(Arguments, Format);
    mis_LogMessage(LOGMSG_UPDATE, Format, Arguments);
    va_end(Arguments);

    misClearErrorStack( );
}

void misStackError(char *fmt,...)
{
    long length;

    va_list args;

    /* Get the length of the formatted error message. */
    va_start(args, fmt);
    length = misSprintfLen(fmt, args);
    va_end(args);

    /* Don't bother if we got an error. */
    if (length < 0)
    {
        misLogError("Could not determine stacked error message length");
        return;
    }

    /* Increment the number of errors we have stacked. */
    gErrorCount++;

    /* Allocate space for the new error message pointer. */
    gErrorMessage = realloc(gErrorMessage, gErrorCount * sizeof(char *));
    if (!gErrorMessage)
        OS_PANIC;

    /* Allocate space for the new error message. */
    gErrorMessage[gErrorCount-1] = calloc(1, length + 1);
    if (!gErrorMessage[gErrorCount-1])
        OS_PANIC;

    /* Make a copy of the new error message. */
    va_start(args, fmt);
    vsprintf(gErrorMessage[gErrorCount-1], fmt, args);
    va_end(args);
}

void misLogErrorStack(void)
{
    long ii;

    /* Log all the stacked error messages. */
    for (ii = 0; ii < gErrorCount; ii++)
        mis_LogMessage(LOGMSG_ERROR, gErrorMessage[ii], NULL);

    /* Clear the stacked error messages. */
    misClearErrorStack( );
}

void misClearErrorStack(void)
{
    long ii;

    /* Free all the stacked error messages. */
    for (ii = 0; ii < gErrorCount; ii++)
        free(gErrorMessage[ii]);

    /* Free and reset the stacked error message pointer and counter. */
    free(gErrorMessage);
    gErrorMessage = NULL;
    gErrorCount = 0;
}
