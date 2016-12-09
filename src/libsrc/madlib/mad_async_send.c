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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "mad_send.h"
#include "mad_async_send.h"
#include "mad_thread_pool.h"

static MadSender *sender = NULL;
static int port = 0;

void madAsyncInit(void) 
{
    static int initialized = 0;
    
    if (initialized == 0) 
    {
        if (port != 0) 
        {
            sender = madSenderNew(port);
        }

        initialized = 1;
    }
}

static void process_msg(void *data, void *user_data) 
{
    MadMsg *msg = (MadMsg *) data;

    (void) user_data;

    madAsyncInit();

    if (sender != NULL) 
    {
        madSenderSend(sender, msg);
    } 

    madMsgFree(msg);
}

static void empty(void *user_data)
{
    madSenderFlush(sender);
}

void madAsyncSend(MadMsg *msg) 
{
    static MadThreadPool *mad_pool = NULL;
    char *port_str = NULL;

    /* Start the thread to process messages */
    if (mad_pool == NULL) 
    {
        /* get port from the environment */
        port_str = getenv("MAD_PORT");
        
        if (port_str != NULL) 
        {
            port = atoi(port_str);
        }

        mad_pool = madThreadPoolNew(process_msg, empty, 1, NULL, NULL);
    }

    if (port != 0) 
    {
        madThreadPoolPush(mad_pool, msg);
    } 
    else 
    {
        madMsgFree(msg);
    }
}


