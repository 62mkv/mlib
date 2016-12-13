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

#ifndef MAD_THREAD_POOL_H
#define MAD_THREAD_POOL_H

typedef struct madThreadPool MadThreadPool;

MadThreadPool
*madThreadPoolNew(void (*process_func)(void *data, void *user_data),
                  void (*empty_func)(void *user_data),
                  int num_threads,
                  void *process_user_data,
                  void *empty_user_data);
void madThreadPoolPush(MadThreadPool *pool, void *data);
void madThreadPoolFree(MadThreadPool *pool);
int madThreadPoolNumToProcess(MadThreadPool *pool);
#endif /* MAD_THREAD_POOL_H */
