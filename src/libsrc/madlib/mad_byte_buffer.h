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

#ifndef _MAD_BYTE_BUFFER_H_
#define _MAD_BYTE_BUFFER_H_

typedef struct madByteBuffer MadByteBuffer;

struct madByteBuffer {
    int nbytes;
    int size;
    char *bytes;
};

void madByteBufferNew(MadByteBuffer *buf);
void madByteBufferNewWithSize(MadByteBuffer *buf, int size);
void madByteBufferWrite(MadByteBuffer *buf, const void *ptr, int size);
void madByteBufferWriteInt(MadByteBuffer *buf, int value);
void madByteBufferWriteUnsignedShort(MadByteBuffer *buf, unsigned short value);
void madByteBufferWriteString(MadByteBuffer *buf, const char *value);

#endif /* _MAD_BYTE_BUFFER_H_ */
