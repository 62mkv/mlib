static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions used to perform Dynamic Memory allocation.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2002
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 *
 *#END*************************************************************************/

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>

#include <mislib.h>

struct mis__DynBuf
{
    int len;
    int pos;
    char *buf;
};

/* Allocate len additional bytes in buffer. */
static void sResizeBuffer(MIS_DYNBUF *buf, int len)
{
    if ((buf->pos + len + 1) > buf->len)
    {
        do
        {
            if (buf->len > 65535)
                buf->len += 65536;
            else
                buf->len *= 2;
        } while ((buf->pos + len + 1) > buf->len);

        buf->buf = realloc(buf->buf, buf->len);
        memset(buf->buf + buf->pos, 0, (buf->len - buf->pos));
    }
}

static void sAppendToBuffer(MIS_DYNBUF *buf, const void *ptr, int len)
{
    if (!len) return;

    sResizeBuffer(buf, len);
    memcpy(buf->buf + buf->pos, ptr, len);
    buf->pos += len;
}

static char sAppendCharToBuffer(MIS_DYNBUF *buf, char c)
{
    sResizeBuffer(buf, 1);
    buf->buf[buf->pos++] = c;
    return c;
}

/*
 * Initialize a dynamic buffer.  If we have a notion of the intial size to
 * allocate, send it in.
 */
MIS_DYNBUF *misDynBufInit(int size)
{
    MIS_DYNBUF *buf = malloc(sizeof(MIS_DYNBUF));

    /* Things would be bad if we ever tried to allocate a size <= 0 */
    if (size < 16)
       	size = 16;

    buf->len = size;
    buf->pos = 0;
    buf->buf = calloc(1, size);

    return buf;
}

/*
 * Append a string to our buffer
 */
void misDynBufAppendString(MIS_DYNBUF *buf, const char *str)
{
    if (!str) return;
    sAppendToBuffer(buf, str, strlen(str));
}

/*
 * Append a string to our buffer
 */
void misDynBufAppendBytes(MIS_DYNBUF *buf, void *ptr, int len)
{
    if (!ptr || !len) return;
    sAppendToBuffer(buf, ptr, len);
}

/*
 * Append a single character to our buffer
 */
void misDynBufAppendChar(MIS_DYNBUF *buf, char c)
{
    sAppendCharToBuffer(buf, c);
}

/*
 * Get the string, temporarily, from this buffer.  This string must not be
 * freed by the caller!
 */
char *misDynBufGetString(MIS_DYNBUF *buf)
{
    return buf ? buf->buf : NULL;
}

/*
 * Get the length of this buffer. 
 */
int misDynBufGetSize(MIS_DYNBUF *buf)
{
    return buf ? buf->pos : 0;
}

/*
 * Closes this buffer, getting the resulting string.  The string returned
 * from this function MUST be freed by the caller.  Callers should either
 * call misDynBufClose or misDynBufFree, but not both.
 */
char *misDynBufClose(MIS_DYNBUF *buf)
{
    char *tmp = NULL;

    if (buf)
    {
	tmp = buf->buf;
	tmp = realloc(tmp, buf->pos + 1);
	buf->buf = NULL;
	buf->len = 0;
	buf->pos = 0;
	misDynBufFree(buf);
    }

    return tmp;
}

/*
 * Closes this buffer, freeing any inprocess allocation. Callers should either
 * call misDynBufClose or misDynBufFree, but not both.
 */
void misDynBufFree(MIS_DYNBUF *buf)
{
    if (buf)
    {
	if (buf->buf) free(buf->buf);
	buf->buf = NULL;
	buf->len = 0;
	free(buf);
    }
}
