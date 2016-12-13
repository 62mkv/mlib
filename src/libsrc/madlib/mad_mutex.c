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
 *  Copyright (c) 2012
 *  Sam Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by Sam
 *  Corporation.
 *
 *  Sam Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by Sam Corporation.
 *
 *  $Copyright-End$
 *
 *#END*************************************************************************/

#include "mad_mutex.h"

#ifdef UNIX

void madMutexInit(MadMutex *mutex) 
{
    pthread_mutex_init(&mutex->mutex, NULL);
}

void madMutexLock(MadMutex *mutex) 
{
    pthread_mutex_lock(&mutex->mutex);
}

void madMutexUnlock(MadMutex *mutex) 
{
    pthread_mutex_unlock(&mutex->mutex);
}

void madMutexDestroy(MadMutex *mutex) 
{
    pthread_mutex_destroy(&mutex->mutex);
}

void madCondInit(MadCond *cond) 
{
    pthread_cond_init(&cond->cond, NULL);
}

void madCondSignal(MadCond *cond) 
{
    pthread_cond_signal(&cond->cond);
}

void madCondWait(MadCond *cond, MadMutex *mutex) 
{
    pthread_cond_wait(&cond->cond, &mutex->mutex);
}

void madCondDestroy(MadCond *cond) 
{
    pthread_cond_destroy(&cond->cond);
}

#else /* WIN32 */

void madMutexInit(MadMutex *mutex) 
{
    mutex->mutex = CreateMutex(NULL, FALSE, NULL);
}

void madMutexLock(MadMutex *mutex) 
{
    WaitForSingleObject(mutex->mutex, INFINITE);
}

void madMutexUnlock(MadMutex *mutex) 
{
    ReleaseMutex(mutex->mutex);
}

void madMutexDestroy(MadMutex *mutex) 
{
    CloseHandle(mutex->mutex);
}

void madCondInit(MadCond *cond) 
{
    cond->cond = CreateEvent(0, FALSE, FALSE, NULL);
}

void madCondSignal(MadCond *cond) 
{
    SetEvent(cond->cond);
}

void madCondWait(MadCond *cond, MadMutex *mutex) 
{
    madMutexUnlock(mutex);
    WaitForSingleObject(cond->cond, INFINITE);
    madMutexLock(mutex);
}

void madCondDestroy(MadCond *cond) 
{
    CloseHandle(cond->cond);
}

#endif



