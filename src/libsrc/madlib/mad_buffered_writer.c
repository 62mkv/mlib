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
 *  Copyright (c) 20168
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
#include <string.h>

#include <moca.h>
#include <mocaerr.h>
#include <oslib.h>

#include "mad_buffered_writer.h"

#define MAD_BUFFERED_WRITER_DEFAULT_SIZE 8192

struct madBufferedWriter 
{
    SOCKET_FD fd;
    size_t size;
    size_t pos;
    char *buf;
};

/*
 * Write +nbytes+ from +buf+ to a socket.
 */
static long writeall(SOCKET_FD fd, const void *buf, size_t nbytes) 
{
    long nwritten;
    long towrite = nbytes;
    char *ptr = (char *)buf;

    while (towrite > 0) 
    {
        nwritten = osSockSend(fd, ptr, towrite, 0);

        if (nwritten == -1) 
        {
            if (osSockErrno() == OS_EINTR) 
            {
                /* Interrupted system call; continue */
                continue;
            } 
            else 
            {
                return nwritten;
            }
        }

        towrite -= nwritten;
        ptr += nwritten;
    }

    return nbytes;
}

/*
 * New buffered writer with default size.
 */
MadBufferedWriter *madBufferedWriterNew(int fd) 
{
    return madBufferedWriterNewWithSize(fd, MAD_BUFFERED_WRITER_DEFAULT_SIZE);
}

/*
 * New buffered writer. 
 */
MadBufferedWriter *madBufferedWriterNewWithSize(int fd, size_t size) 
{
    MadBufferedWriter *writer = NULL;

    writer = (MadBufferedWriter *) calloc(1, sizeof (MadBufferedWriter));

    writer->fd = fd;
    writer->size = size;
    writer->pos = 0;

    writer->buf = (char *) calloc(writer->size, sizeof(char));

    return writer;
}

/*
 * Write +len+ characters of a message with a MadBufferedWriter.
 */
long madBufferedWriterWrite(MadBufferedWriter *writer, const void
        *msg, size_t len) {
    const char *ptr = msg;
    int navailable = writer->size - writer->pos;
    int rv = len;

    if (len >= navailable) 
    {
        memcpy(writer->buf + writer->pos, ptr, navailable);

        if (-1 == writeall(writer->fd, writer->buf, writer->size)) 
        {
            rv = 0;
        }

        len -= navailable;
        ptr += navailable;

        while (len > writer->size) 
        {
            memcpy(writer->buf, ptr, writer->size);

            if (-1 == writeall(writer->fd, writer->buf, writer->size)) 
            {
                rv = 0;
                break;
            }

            len -= writer->size;
            ptr += writer->size;
        }

        writer->pos = 0;
    }
        
    memcpy(writer->buf + writer->pos, ptr, len);

    writer->pos += len;

    return rv;    
}

/*
 * Flush a MadBufferedWriter.
 */
long madBufferedWriterFlush(MadBufferedWriter *writer) 
{
    long rv = 0;

    if (writer->pos != 0) 
    {
        rv = writeall(writer->fd, writer->buf, writer->pos);

        writer->pos = 0;
        memset(writer->buf, '\0', writer->size);
    }

    return rv;
}    

/*
 * Flush and close a MadBufferedWriter. This gives up any held resources
 * such as file descriptors or heap space.
 */
void madBufferedWriterClose(MadBufferedWriter *writer) 
{
    madBufferedWriterFlush(writer);
    osSockClose(writer->fd);

    free(writer->buf);
    free(writer);
}
