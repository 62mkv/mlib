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

#include <stdlib.h>

#include "mad_queue.h"

struct node 
{
    struct node *next;

    void *data;
};

struct madQueue 
{
    struct node *head;
    struct node *tail;

    int count;
};

/*
 * Create new MadQueue.
 */
MadQueue *madQueueNew(void) 
{
    return (MadQueue *) calloc(1, sizeof(MadQueue));
}

/*
 * Push data onto the queue.
 */
void madQueuePush(MadQueue *queue, void *data) 
{
    struct node *node = (struct node *) calloc(1, sizeof(struct node));

    node->next = NULL;
    node->data = data;

    if (queue->tail) 
    {
        queue->tail->next = node;
        queue->tail = node;
    } 
    else 
    {
        queue->tail = queue->head = node;
    }

    queue->count++;
}

/*
 * Return head of queue but don't remove.
 */
void *madQueuePeek(MadQueue *queue) 
{
    if (queue->head) 
    {
        return queue->head->data;
    }

    return NULL;
}

/*
 * Pop queue and return data.
 */
void *madQueuePop(MadQueue *queue) 
{
    void *rv = NULL;
    struct node *node;

    if (queue->head) 
    {
        node = queue->head;
        rv = queue->head->data;

        if (queue->head->next) 
        {
            queue->head = queue->head->next;
        } 
        else 
        {
            queue->tail = queue->head = NULL;
        }

        queue->count--;
        free(node);
    }

    return rv;
}

/*
 * Free resources held by queue.
 */
void madQueueFree(MadQueue *queue) 
{
    struct node *node = queue->head;
    struct node *temp = NULL;

    while (node) 
    {
        temp = node;
        node = node->next;

        free(temp);
    }

    free(queue);
}

/*
 * Return number of elements in queue.
 */
int madQueueCount(MadQueue *queue) 
{
    return queue->count;
}
