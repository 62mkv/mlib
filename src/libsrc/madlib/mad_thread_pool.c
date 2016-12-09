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
 *  Copyright (c) 2008
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
#include <stdio.h>

#include <moca.h>
#include <oslib.h>

#include "mad_mutex.h"
#include "mad_queue.h"
#include "mad_async_queue.h"
#include "mad_thread_pool.h"

struct madThreadPool 
{
    MadAsyncQueue *q;

    void (*process)(void *data, void *user_data);
    void (*empty)(void *user_data);

    void *process_user_data;
    void *empty_user_data;

    int num_threads;
    OS_THREAD *threads;

    MadMutex done_mutex;
    int done;
};

#ifdef UNIX
static void delay(void)
{
    struct timespec t;

    t.tv_sec = 0;
    t.tv_nsec = 1;

    nanosleep(&t, NULL);
}
#endif

static void *thread(void *ptr) 
{
    MadThreadPool *pool = (MadThreadPool *)ptr;
    void *data = NULL;
    MadQueue *queue;

    for(;;)
    {
#ifdef UNIX
        delay();
#endif
        /* If there is only one thread for processing messages, just get
         * the whole queue because no other threads will be processing
         * it.  This reduces the amount of locking significantly.
         */
        if (pool->num_threads == 1)
        {
            queue = madAsyncQueueGetAll(pool->q);

            while ((data = madQueuePop(queue))) 
            {
                (*pool->process)(data, pool->process_user_data);
            }

            madQueueFree(queue);
        }
        else 
        {
            data = madAsyncQueuePopWait(pool->q);

            (*pool->process)(data, pool->process_user_data);
        }

        /* Call the on_empty function if provided */
        if ((pool->empty != NULL) && 
            (madThreadPoolNumToProcess(pool) == 0))
        {
            (*pool->empty)(pool->empty_user_data);
        }
    }

    return NULL;
}

MadThreadPool 
*madThreadPoolNew(
        void (*process_func)(void *data, void *user_data),
        void (*empty_func)(void *user_data),
        int num_threads,
        void *process_user_data,
        void *empty_user_data) 
{
    MadThreadPool *pool = NULL;
    int i;

    pool = (MadThreadPool *) calloc(1, sizeof(MadThreadPool));

    pool->q = madAsyncQueueNew();
    pool->process = process_func;
    pool->process_user_data = process_user_data;

    pool->empty = empty_func;
    pool->empty_user_data = empty_user_data;

    pool->num_threads = num_threads;

    pool->threads = (OS_THREAD *) calloc(1, sizeof(OS_THREAD) *
            num_threads);

    madMutexInit(&pool->done_mutex);

    pool->done = 0;

    for (i = 0; i < num_threads; i++) 
    {
        osCreateThread(&pool->threads[i], thread, pool);
    }

    return pool;
} 

void madThreadPoolPush(MadThreadPool *pool, void *data) 
{
    madAsyncQueuePush(pool->q, data);
}

void madThreadPoolFree(MadThreadPool *pool) 
{
    int i;

    madMutexLock(&pool->done_mutex);
    pool->done = 1;
    madMutexUnlock(&pool->done_mutex);

    for (i = 0; i < pool->num_threads; i++) 
    {
        /* We probably can't join the threads without killing them */
        /* osJoinThread(pool->threads[i], NULL); */
    }

}

int madThreadPoolNumToProcess(MadThreadPool *pool) 
{
    return madAsyncQueueCount(pool->q);
}
