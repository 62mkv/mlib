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

#include <string.h>
#include <stdlib.h>

#include <moca.h>
#include <oslib.h>

#include "mad_byte_buffer.h"

#define MAD_BYTE_BUFFER_DEFAULT_SIZE 1024

/* 
 * Return the closest power of 2. In other words, return
 * the lowest power of 2 that is greater than x.
 * This works with 32 bit uint by filling ones and incrementing
 * by 1 to get the next power of two. 
 * For bitwise example, 1010 -> 1111 -> 10000.
 */
static int gep2(int x) 
{
    x = x - 1;
    x = x | (x >> 1);
    x = x | (x >> 2);
    x = x | (x >> 4);
    x = x | (x >> 8);
    x = x | (x >> 16);
                                    
    return x + 1;
}

/*
 * Allocate default heap space to MadByteBuffer bytes pointer.
 */
void madByteBufferNew(MadByteBuffer *buf) 
{
    madByteBufferNewWithSize(buf, MAD_BYTE_BUFFER_DEFAULT_SIZE);
}

/*
 * Allocate +size+ # of characters heap space to MadByteBuffer bytes pointer.
 */
void madByteBufferNewWithSize(MadByteBuffer *buf, int size) 
{
    buf->nbytes = 0;
    buf->size = size;
    buf->bytes = (char *)calloc(size, sizeof(char));
}

/*
 * Append contents into MadByteBuffer, allocating more space if necessary.
 */
void madByteBufferWrite(MadByteBuffer *buf, const void *ptr, int size) 
{
    int new_nbytes = buf->nbytes + size;

    /* grow capacity if needed to the closest power of two */
    if (new_nbytes > buf->size)
    {
        buf->size = gep2(new_nbytes);
        buf->bytes = (char *)realloc(buf->bytes, buf->size);
    }

    memcpy(buf->bytes + buf->nbytes, ptr, size);
    buf->nbytes = new_nbytes;
}

/*
 * Append an integer to MadByteBuffer, allocating more space if necessary.
 */
void madByteBufferWriteInt(MadByteBuffer *buf, int value)
{
    int nvalue = htonl(value);

    madByteBufferWrite(buf, &nvalue, sizeof(int));
}

/*
 * Append an unsigned short to MadByteBuffer, allocating more space if necessary.
 */
void madByteBufferWriteUnsignedShort(MadByteBuffer *buf, unsigned short value)
{
    unsigned short nvalue = htons(value);

    madByteBufferWrite(buf, &nvalue, sizeof(unsigned short));
}

/*
 * Append null-terminated string to MadByteBuffer, allocating more space if necessary.
 * Preceding the string is an unsigned short showing the length of the string.
 */
void madByteBufferWriteString(MadByteBuffer *buf, const char *value) 
{
    int len = 0;

    if (value != NULL) 
    {
        len = strlen(value);
    }
    
    madByteBufferWriteUnsignedShort(buf, (unsigned short) len);
    madByteBufferWrite(buf, value, len);
}

