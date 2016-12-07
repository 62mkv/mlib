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

#include <stdlib.h>

#include "mad_mutex.h"
#include "mad_async_queue.h"
#include "mad_queue.h"

struct madAsyncQueue 
{
    MadQueue *queue;

    MadMutex mutex;
    MadCond cond;

    int waiting_threads;
};

/*
 * Construct new MadAsyncQueue.
 */
MadAsyncQueue *madAsyncQueueNew(void) 
{
   MadAsyncQueue *queue;

   queue = (MadAsyncQueue *) calloc(1, sizeof(MadAsyncQueue));

   queue->queue = madQueueNew();

   madMutexInit(&queue->mutex);
   madCondInit(&queue->cond);

   queue->waiting_threads = 0;

   return queue;
}

/*
 * Push data on to the queue.
 */
void madAsyncQueuePush(MadAsyncQueue *q, void *data) 
{
    madMutexLock(&q->mutex);

    madQueuePush(q->queue, data);
    
    if (q->waiting_threads > 0) 
    {
        madCondSignal(&q->cond);
    }
    
    madMutexUnlock(&q->mutex);
}

/*
 * Pop data from the queue.
 */
void *madAsyncQueuePop(MadAsyncQueue *q) 
{
    void *data = NULL;

    madMutexLock(&q->mutex);

    data = madQueuePop(q->queue);

    madMutexUnlock(&q->mutex);

    return data;
}

/*
 * Pop data from queue.
 * Block until MadAsyncQueue has at least one element.
 */
void *madAsyncQueuePopWait(MadAsyncQueue *q) 
{
    void *data = NULL;

    madMutexLock(&q->mutex);

    if (!madQueueCount(q->queue)) 
    {
        q->waiting_threads++;

        madCondWait(&q->cond, &q->mutex);

        q->waiting_threads--;
    }

    data = madQueuePop(q->queue);

    madMutexUnlock(&q->mutex);

    return data;
}

/*
 * Free resources held by MadAsynQueue.
 */
void madAsyncQueueFree(MadAsyncQueue *q) 
{
    madQueueFree(q->queue);

    madCondDestroy(&q->cond);

    madMutexDestroy(&q->mutex);
}

/*
 * Count number of elements in MadAsyncQueue.
 */
int madAsyncQueueCount(MadAsyncQueue *q) 
{
    int count;

    madMutexLock(&q->mutex);

    count = madQueueCount(q->queue);

    madMutexUnlock(&q->mutex);

    return count;
}

/*
 * Return all elements of MadAsyncQueue as MadQueue.
 * Block until MadAsyncQueue has at least one element.
 */
MadQueue *madAsyncQueueGetAll(MadAsyncQueue *q) 
{
    MadQueue *all = NULL;

    madMutexLock(&q->mutex);

    if (!madQueueCount(q->queue)) 
    {
        q->waiting_threads++;

        madCondWait(&q->cond, &q->mutex);

        q->waiting_threads--;
    }

    all = q->queue;
    q->queue = madQueueNew();

    madMutexUnlock(&q->mutex);

    return all;
}