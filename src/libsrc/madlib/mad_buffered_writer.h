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

#ifndef _MAD_BUFFERED_WRITER_H
#define _MAD_BUFFERED_WRITER_H

typedef struct madBufferedWriter MadBufferedWriter;

MadBufferedWriter *madBufferedWriterNewWithSize(int fd, size_t size);

MadBufferedWriter *madBufferedWriterNew(int fd);

long madBufferedWriterWrite(MadBufferedWriter *writer, 
                            const void *msg, 
                            size_t len);

long madBufferedWriterFlush(MadBufferedWriter *writer);

void madBufferedWriterClose(MadBufferedWriter *writer);

#endif
