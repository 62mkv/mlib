static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to handle mutexes.
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


/*
 *  A mutex object is a synchronization object whose state is set to signaled 
 *  when it is not owned by any thread, and nonsignaled when it is owned. Only 
 *  one thread at a time can own a mutex object, whose name comes from the fact 
 *  that it is useful in coordinating mutually exclusive access to a shared 
 *  resource. For example, to prevent two threads from writing to shared memory 
 *  at the same time, each thread waits for ownership of a mutex object before 
 *  executing the code that accesses the memory. After writing to the shared 
 *  memory, the thread releases the mutex object.
 */


#define MOCA_ALL_SOURCE

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>

#ifdef UNIX
#include <pthread.h>
#endif

#include <mocaerr.h>
#include <mislib.h>
#include <oslib.h>

#ifdef UNIX

long osCreateMutex(OS_MUTEX *mutex)
{
    long status;

    /* Initialize the mutex. */
    if ((status = pthread_mutex_init(mutex, NULL)) != 0)
    {
	misLogError("pthread_mutex_init: %s (%d)", osError( ), status);
	misLogError("osCreateMutex: Could not create mutex");
	return status;
    }

    return eOK;
}

long osDestroyMutex(OS_MUTEX *mutex)
{
    long status;

    /* Destroy the mutex. */
    if ((status = pthread_mutex_destroy(mutex)) != 0)
    {
	misLogError("pthread_mutex_destroy: %s (%d)", osError( ), status);
	misLogError("osDestroyMutex: Could not destroy mutex");
	return status;
    }

    return eOK;
}

long osTryLockMutex(OS_MUTEX *mutex)
{ 
    long status;

    /* Trylock the mutex. */
    if ((status = pthread_mutex_trylock(mutex)) != 0)
    {
	misLogError("pthread_mutex_trylock: %s (%d)", osError( ), status);
	misLogError("osTryLockMutex: Could not trylock mutex");
	return status;
    }
 
    return eOK;
}

long osLockMutex(OS_MUTEX *mutex)
{ 
    long status;

    /* Lock the mutex. */
    if ((status = pthread_mutex_lock(mutex)) != 0)
    {
	misLogError("pthread_mutex_lock: %s (%d)", osError( ), status);
	misLogError("osLockMutex: Could not lock mutex");
	return status;
    }
 
    return eOK;
}

long osUnlockMutex(OS_MUTEX *mutex)
{ 
    long status;

    /* Unlock the mutex. */
    if ((status = pthread_mutex_unlock(mutex)) != 0)
    {
	misLogError("pthread_mutex_unlock: %s (%d)", osError( ), status);
	misLogError("osUnlockMutex: Could not unlock mutex");
	return status;
    }
 
    return eOK;
}

#else

long osCreateMutex(OS_MUTEX *mutex)
{
    /* Initialize the mutex. */
    if ((*mutex = CreateMutex(NULL, FALSE, NULL)) == NULL)
    {
	misLogError("CreateMutex: %s (%d)", osError( ), osErrno( ));
	misLogError("osCreateMutex: Could not create mutex");
	return osErrno( );
    }

    return eOK;
}

long osDestroyMutex(OS_MUTEX *mutex)
{
    long status;

    /* Destroy the mutex. */
    if ((status = CloseHandle(*mutex)) == 0)
    {
	misLogError("CloseHandle: %s (%d)", osError( ), status);
	misLogError("osDestroyMutex: Could not destroy mutex");
	return status;
    }

    return eOK;
}

long osTryLockMutex(OS_MUTEX *mutex)
{ 
    long status;

    /* Trylock the mutex. */
    if ((status = WaitForSingleObject(*mutex, 0)) == WAIT_FAILED)
    {
	misLogError("WaitForSingleObject: %s (%d)", osError( ), status);
	misLogError("osTryLockMutex: Could not trylock mutex");
	return status;
    }
 
    return eOK;
}

long osLockMutex(OS_MUTEX *mutex)
{ 
    long status;

    /* Lock the mutex. */
    if ((status = WaitForSingleObject(*mutex, INFINITE)) == WAIT_FAILED)
    {
	misLogError("WaitForSingleObject: %s (%d)", osError( ), status);
	misLogError("osLockMutex: Could not lock mutex");
	return status;
    }
 
    return eOK;
}

long osUnlockMutex(OS_MUTEX *mutex)
{ 
    long status;

    /* Unlock the mutex. */
    if ((status = ReleaseMutex(*mutex)) == 0)
    {
	misLogError("ReleaseMutex: %s (%d)", osError( ), status);
	misLogError("osUnlockMutex: Could not unlock mutex");
	return status;
    }
 
    return eOK;
}

#endif

