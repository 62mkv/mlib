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
 *  Copyright (c) 2016
 *  Sam Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by Sam Corporation.
 *
 *  Sam Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by Sam Corporation.
 *
 *  $Copyright-End$
 *
 *#END*************************************************************************/

#include <moca.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

/* UTF-8 encoding
 * U+000000-U+00007F    -  0xxxxxxx
 * U+000080-U+0007FF    -  110yyyxx 10xxxxxx
 * U+000800-U+00FFFF    -  1110yyyy 10yyyyxx 10xxxxxx
 * U+010000-U+10FFFF    -  11110zzz 10zzyyyy 10yyyyxx 10xxxxxx
 */

int utf8NextCharLen(char *s)
{
    if (*s & 0x80)
    {
        /* UTF 8 multi-byte characters will have the first bit set always
         * This is a utf-8 character of 2-4 bytes */
        if ((*s & 0xF0) == 0xF0)
        {
            /*
             * It is a four byte character: 11110zzz 10zzyyyy 10yyyyxx 10xxxxxx
             */
            return 4;
        }
        else if ((*s & 0xE0) == 0xE0)
        {
            /*
             * It is a three-byte character: 1110yyyy 10yyyyxx 10xxxxxx
             */
            return 3;
        }
        else
        {
            /*
             * It is a two-byte character: 110yyyxx 10xxxxxx
             */
            return 2;
        }
    }
    return 1;
}
/*
 * This function moves the character string to the next character
 * It does this by interpreting the first byte to determine how many
 * bytes the character includes
 */
int utf8NextChar(char **s)
{
    int charlen;

    charlen = utf8NextCharLen(*s);
    *s+= charlen;
    return charlen;
}


/*
 * Returns the number of bytes for the specified number of characters
 * in an UTF8 encoded string
 */
long utf8ByteLen(char *in, long n_char_cnt)
{
    char *p;
    long charCnt=0;

    if (in == NULL)
    {
        return 0;
    }

    p = in;
    while (*p && charCnt <  n_char_cnt)
    {
        utf8NextChar(&p);
        charCnt++;
    }
    return (p-in);
}

/*
 * Returns the number of characters found in an UTF8 encoded string
 * This differs from bytes, as there can be 1-4 bytes per character
 */
long utf8CharLen(char *in)
{
    char *p;
    long charCnt;

    if (in == NULL) return 0;

    /* Starting point of the string */
    p = in;
    charCnt=0;
    while(*p)
    {
        utf8NextChar(&p);
        charCnt++;
    }
    return charCnt;
}


