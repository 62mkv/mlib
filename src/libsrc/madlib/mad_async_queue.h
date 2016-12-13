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

#ifndef MAD_ASYNC_QUEUE_H
#define MAD_ASYNC_QUEUE_H

#include "mad_queue.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct madAsyncQueue MadAsyncQueue;

MadAsyncQueue *madAsyncQueueNew(void);

void madAsyncQueuePush(MadAsyncQueue *q, void *data);

void *madAsyncQueuePop(MadAsyncQueue *q);

void *madAsyncQueuePopWait(MadAsyncQueue *q);

void madAsyncQueueFree(MadAsyncQueue *q);

int madAsyncQueueCount(MadAsyncQueue *q);

MadQueue *madAsyncQueueGetAll(MadAsyncQueue *q);

#ifdef __cplusplus
}
#endif

#endif /* MAD_ASYNC_QUEUE_H */
