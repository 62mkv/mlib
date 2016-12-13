static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to handle threads.
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

#ifdef UNIX
#include <pthread.h>
#include <signal.h>
#endif

#include <mocaerr.h>
#include <mislib.h>
#include <oslib.h>


#ifdef UNIX

static int gInitialized;

static pthread_t gInitialThread;

void os_SetInitialThread(void)
{
    gInitialized = 1;

    gInitialThread = pthread_self( );
}

long osIsInitialThread( )
{
    /* If we haven't been initialized we'll assume we are single threaded. */
    if (!gInitialized)
        return 1;

    return pthread_equal(pthread_self( ), gInitialThread);
}

void osSignalInitialThread(int signo)
{
    fprintf(stderr, "Sending signal %d to initial thread...\n", signo);

    /* Send the kill signal to the initial thread. */
    pthread_kill(gInitialThread, signo);

    return;
}

long osCreateThread(OS_THREAD *thread, void *(*fptr)(void *), void *arg)
{ 
    long status;

    /* Create the thread. */
    if ((status = pthread_create((pthread_t *) thread, NULL, fptr, arg)) != 0)
    {
        misLogError("pthread_create: %s (%d)", osError( ), status);
        misLogError("osCreateThread: Could not create thread");
	return status;
    }
 
    return eOK;
}

long osJoinThread(OS_THREAD thread, void **value)
{
    long status;

    /* Attempt to join the thread. */
    if ((status = pthread_join(thread, value)) != 0)
    {
        misLogError("pthread_join: %s (%d)", osError( ), status);
        misLogError("osJoinThread: Could not join thread");
	return status;
    }

    return status;
}

void osExitThread(void *value)
{
    pthread_exit(value);  
}

long osThreadIsRunning(OS_THREAD thread)
{
    long status;

    /* 
     *  Send a dummy signal to the thread and deterine if it's 
     *  running based off of the return status of the kill call.
     */
    status = pthread_kill((pthread_t) thread, 0);
    if (status == 0)
        return 1;
    else
        return 0;
}

#else

void os_SetInitialThread(void)
{
    return;
}

long osIsInitialThread( )
{
    return 1;
}

void osSignalInitialThread(int signo)
{
    return;
}

long osCreateThread(OS_THREAD *thread, void *(*fptr)(void *), void *arg)
{ 
    /* Create the thread. */
    *thread = CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE) fptr, 
	                   arg, 0, NULL);
    if (thread == NULL)
    {
        misLogError("CreateThread: %s (%d)", osError( ), osErrno( ));
        misLogError("osCreateThread: Could not create thread");
	return eERROR;
    }

    return eOK;
}

long osJoinThread(OS_THREAD thread, void **inValue)
{
    long status;

    DWORD value;

    /* Wait for the thread to complete. */
    if ((status = WaitForSingleObject(thread, INFINITE)) == WAIT_FAILED)
    {
        misLogError("WaitForSingleObject: %s (%d)", osError( ), status);
	misLogError("osJoinThread: Could not join thread");
	return status;
    }

    /* Get the return value from the thread. */
    GetExitCodeThread(thread, &value);

    /* Set the return value for the caller. */
    *inValue = (void *) value;

    /* Close the handle to the thread so the o/s can release resources. */
    CloseHandle(thread);

    return eOK;
}

void osExitThread(void *value)
{
    ExitThread((DWORD) value);
}

long osThreadIsRunning(OS_THREAD thread)
{
    long status;

    /* Is the thread still running? */
    status = WaitForSingleObject(thread, 0);
    switch (status)
    {
    case WAIT_FAILED:      /* Assume the thread isn't running. */
        misLogError("WaitForSingleObject: %s", osError( ));
        misLogError("osThreadIsRunning: Could not determine if thread is running");
        return 0;

    case WAIT_TIMEOUT:     /* The thread is still running. */
    case WAIT_ABANDONED:   /* Assume the thread is still running. */
        return 1;    

    case WAIT_OBJECT_0:    /* The thread is waiting to be joined. */
    default:               /* Assume the thread isn't running. */
        return 0;         
    }
}

#endif
