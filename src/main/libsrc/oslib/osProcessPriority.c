static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Function to support changing the priority of a running
 *               process.
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

#define MOCA_ALL_SOURCE

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>

#ifdef UNIX
# include <errno.h>
# include <unistd.h>
# include <sys/resource.h>
#endif

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <oslib.h>

#ifdef UNIX

long osSetProcessPriority(long priority)
{
    pid_t pid;

    long oldPriority;

    /* Validate our arguments. */
    if (priority != MOCA_PRIORITY_LOW &&
        priority != MOCA_PRIORITY_NORMAL &&
        priority != MOCA_PRIORITY_HIGH)
    {
        misLogError("Invalid priority given");
        return -1;
    }

    /* Get our process id. */
    pid = getpid( );

    /* 
     *  Clear the global errno because getpriority annoyingly returns
     *  -1 both when the current process scheduling priority is -1
     *  and when an error occurred.
     */
    errno = 0;

    /* Get our current process scheduling priority. */
    oldPriority = getpriority(PRIO_PROCESS, pid);
    if (oldPriority == -1 && errno != 0)
    {
        misLogError("getpriority: %s", osError( ));
        misLogError("Could not get current process priority");
        return -1;
    }
    else if (oldPriority == -1)
    {
	oldPriority = 0;
    }

    /* Set our new process scheduling priority. */
    if (setpriority(PRIO_PROCESS, pid, priority) == -1)
    {
        misLogError("setpriority: %s", osError( ));
        misLogError("Could not set process priority");
        return -1;
    }

printf("Returning priority of %ld...\n", oldPriority);

    return oldPriority;
}

#else

long osSetProcessPriority(long priority)
{
    long status,
         oldPriority;

    HANDLE hProcess;

    /* Validate our arguments. */
    if (priority != MOCA_PRIORITY_LOW &&
        priority != MOCA_PRIORITY_NORMAL &&
        priority != MOCA_PRIORITY_HIGH)
    {
        misLogError("Invalid priority given");
        return -1;
    }

    /* Get a our process handle. */
    hProcess = GetCurrentProcess( );

    /* Get our current priority class. */
    oldPriority = GetPriorityClass(hProcess);
    if (oldPriority == 0)
    {
        status = osErrno( );
        misLogError("GetPriorityClass: %s", osError( ));
        misLogError("Could not get current process priority");
        return -1;
    }

    /* Set our new priority class. */
    if (SetPriorityClass(hProcess, priority) == 0)
    {
        status = osErrno( );
        misLogError("SetPriorityClass: %s", osError( ));
        misLogError("Could not set process priority");
        return -1;
    }

    return oldPriority;
}

#endif
