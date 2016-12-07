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

#include <moca.h>
#include <mocaerr.h>
#include <oslib.h>

#include "mad_send.h"
#include "mad_buffered_writer.h"
#include "mad_thread_pool.h"

struct madSender 
{
    SOCKET_FD sockfd;
    int enabled;

    MadBufferedWriter *writer;
};

/*
 * Send a MadMsg with a MadSender.
 */
void madSenderSend(MadSender *sender, MadMsg *msg) 
{
    int size = ntohl(msg->hdr.size) + sizeof(MadHeader);

    if (sender != NULL && sender->enabled == 1) 
    {
        madBufferedWriterWrite(sender->writer, msg, size);
    }
}

/*
 * Create new MadSender.
 */
MadSender *madSenderNew(int port)
{

    MadSender *sender = NULL;

    if (port != 0) 
    {
        sender = (MadSender *) calloc(1, sizeof (MadSender));

        sender->sockfd = -1;

        if (osTCPConnect(&sender->sockfd, "localhost", port) == eOK)
        {
            sender->enabled = 1;
            sender->writer = madBufferedWriterNew(sender->sockfd);
        }
        else
        {
            /* TODO free and return null */
        }
    }

    return sender;
}

/*
 * Flush a MadSender's underlying MadBufferedWriter.
 */
void madSenderFlush(MadSender *sender) {
    if (sender->enabled) 
    {
        madBufferedWriterFlush(sender->writer);
    }
}
