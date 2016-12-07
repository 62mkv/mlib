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
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie
 *  Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 *
 *#END*************************************************************************/

#ifndef _MAD_MUTEX_H_
#define _MAD_MUTEX_H_

#include <moca.h>

#ifdef UNIX 
#include <pthread.h>
#else 
#include <windows.h>
#endif

struct madMutex 
{
#ifdef UNIX
    pthread_mutex_t mutex;
#else
    HANDLE mutex;
#endif
};

typedef struct madMutex MadMutex;

struct madCond 
{
#ifdef UNIX
    pthread_cond_t cond;
#else
    HANDLE cond;
#endif
};

typedef struct madCond MadCond;

void madMutexInit(MadMutex *mutex);
void madMutexLock(MadMutex *mutex);
void madMutexUnlock(MadMutex *mutex);
void madMutexDestroy(MadMutex *mutex);

void madCondInit(MadCond *cond);
void madCondSignal(MadCond *cond);
void madCondWait(MadCond *cond, MadMutex *mutex);
void madCondDestroy(MadCond *cond);

#endif /* _MAD_MUTEX_H_ */
