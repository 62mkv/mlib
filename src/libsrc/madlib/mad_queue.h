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

#ifndef _MAD_QUEUE_H_
#define _MAD_QUEUE_H_

#if defined (__cplusplus)
extern "C" {
#endif

typedef struct madQueue MadQueue;

MadQueue *madQueueNew(void);

void madQueuePush(MadQueue *queue, void *data);

void *madQueuePeek(MadQueue *queue);

void *madQueuePop(MadQueue *queue);

void madQueueFree(MadQueue *queue);

int madQueueCount(MadQueue *queue);

#if defined (__cplusplus)
}
#endif

#endif /* _MAD_QUEUE_H_ */
