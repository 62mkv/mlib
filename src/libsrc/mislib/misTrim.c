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
#include <string.h>
#include <ctype.h>

#include <mocaerr.h>
#include <mislib.h>
#include <oslib.h>

/*
 * Trim spaces (white space) from the end of a string.
 */
char *misTrim(char *string)
{
    char *ptr, *eptr;

    if (string)
    {
        /* The variable eptr is going to be the end of the string...   */
        for (eptr = ptr = string; *ptr; ptr++)
            if (!isspace((unsigned char)*ptr)) eptr = ptr + 1;

        *eptr = '\0';
    }

    return string;
}

/*
 * returns 1 if the string contains nothing or no non-space charcters
 * before the length specified.  This can be used instead of logic like:
 *   if (*foo == NULL || misTrimLen(foo, FOO_LEN)==0)
 * It is more efficient than misTrimLen in that it exists as soon
 * as a non-space character is found
 */
short misTrimIsNull(char *string, long max_len)
{
    long nLen;

    if (string == NULL) 
	return 1;

    nLen =0;
    while (*string && isspace(*string) && (max_len == 0 || nLen < max_len))
    {
        string++;
        nLen++;
    }
    if (!*string || (nLen >= max_len && max_len >0) )
        return 1;
    return 0;
}

/*
 * returns 1 if the string contains nothing or no non-space charcters
 * before the length specified.  This can be used instead of logic like:
 *   if (*foo == NULL || misTrimLen(foo, FOO_LEN)==0)
 * It is more efficient than misTrimLen in that it exists as soon
 * as a non-space character is found
 */
short misTrimIsNullN(char *string, long max_len)
{
    long nLen;

    if (string == NULL) 
	return 1;

    nLen =0;
    while (*string && isspace(*string) && (max_len ==0 || nLen < max_len))
    {
        utf8NextChar(&string);
        nLen++;
    }

    if (!*string || nLen >= max_len)
        return 1;

    return 0;
}

/*
 * UNICODE Version:  This method has been modified to use character based
 * lengths since most of the API usage is character based.
 */
long misTrimLenN(char *string, long n_max_len)
{
    long r, charcnt;
    char *ptr;

    if (!string)
        return (0);

    /* the variable 'r' is going to be the length we return...   */
    /* ...the maximum len we will return is either going to be   */
    /* the max_len that is passed in or the length of the string */
    /* if it is null terminated...                               */

    for (charcnt= r= 0, ptr = string;
         charcnt < n_max_len && *ptr; charcnt++)
    {
        if  (!isspace((unsigned char)*ptr))
        {
            r = charcnt+1;
        }
        utf8NextChar(&ptr);
    }

    return (r);
}

long misTrimByteLenN(char *string, long n_max_len)
{
    long charcnt;
    char *ptr;
    char *last;

    if (!string)
        return (0);

    /* ...the maximum len we will return is either going to be   */
    /* the max_len that is passed in or the length of the string */
    /* if it is null terminated...                               */
    last = NULL;
    for (charcnt= 0, ptr = string;
         charcnt < n_max_len && *ptr; charcnt++)
    {
        if  (!isspace((unsigned char)*ptr))
        {
            last =ptr;
            last += utf8NextChar(&ptr) -1;
        }
        else
        {
            utf8NextChar(&ptr);
        }
    }

    if (last)
        return (last - string +1);

    return (0);
}

/*
 * Give the length of the string modulo the extra white space at the end.
 * This does the same thing as misTrim, but doesn't modify the string.
 */
long misTrimLen(char *string, long max_len)
{
    long r, i;
    char *ptr;

    if (!string)
        return (0);

    /* the variable 'r' is going to be the length we return...   */
    /* ...the maximum len we will return is either going to be   */
    /* the max_len that is passed in or the length of the string */
    /* if it is null terminated...                               */

    for (i = r = 0, ptr = string; i < max_len && *ptr; ptr++, i++)
    {
        if (!isspace((unsigned char)*ptr)) 
	    r = i + 1;
    }

    return (r);
}

char *misTrimR(char *String)
{
    return (misTrim(String));
}

char *misTrimLR(char *string)
{
    char *src, *dst, *eptr;

    if (string)
    {
        /* The variable eptr is going to be the end of the string...   */
        eptr = src = dst = string;

        /* Skip leading space */
        for (; *src && isspace((unsigned char)*src); src++)
            ;

        /* Skip trailing space */
        while ((*dst = *src))
        {
            if (!isspace((unsigned char)*dst)) eptr = dst + 1;
            dst++;
            src++;
        }

        *eptr = '\0';
    }

    return string;
}

/*
 * This is for safely copying and trimming a string variable, all at
 * once. Either call as prototyped or as misTrimcpy(out, in, len), and
 * outsize will be assumed to be len+1.
 */
char *misTrimncpy(char *out, char *in, long len, long outsize)
{
    long pos = 0;
    char *endpos = out;
    char *startpos = out;

    while (pos < len && pos < outsize && in && *in)
    {
        *out = *in;
        if (*out != ' ')
            endpos = out + 1;
        out++;
        in++;
        pos++;
    }

    /*
     * We only null-terminate if we didn't exceed the size. This is
     * only possible if (outsize <= len).
     */
    if (endpos - startpos <= outsize)
        *endpos = '\0';

    return startpos;
}

/* UTF-8:  This has been modified to work with utf-8.
 * It's important to note that the oustsize is still byte based, because
 * it is used to define the length of the buffer being used.
 */
char *misTrimncpyN(char *out, char *in, long n_len, long outsize)
{
    long cnt = 0;
    long pos = 0;
    long byteCnt;
    char *endpos = out;
    char *startpos = out;

    while (cnt < n_len && (outsize <0 || pos < outsize) && in && *in)
    {
        byteCnt = utf8NextCharLen(in);
        if  (byteCnt == 1 || !isspace((unsigned char)*in))
        {
            endpos = out+byteCnt;
        }
        while (byteCnt--)
        {
            *out++ = *in++;
            pos++;
        }
        cnt++;
    }


    /*
     * We only null-terminate if we didn't exceed the size. This is
     * only possible if (outsize <= n_len).
     */
    if (endpos - startpos <= outsize)
        *endpos = '\0';

    return startpos;
}

/*
 * This is for safely copying and trimming a string variable, all at once.
 */
char *misTrimLRcpy(char *out, char *in, long len)
{
    char *temp = NULL;

    /* Make a copy of the input string we can play with. */
    misDynStrcpy(&temp, in);

    /* Trim both sides of the string. */
    misTrimLR(temp);

    /* Copy it into the output string. */
    memset(out, 0, len+1);
    strncpy(out, temp, len);

    free(temp);

    return out;
}

/*
 * This is for safely copying and trimming a string variable, all at once.
 * UTF8: Careful here.  n_len is a character length.  It's important to make
 *       sure that the out buffer has enough space to hold all the bytes the
 *       string requires in utf8
 */
char *misTrimLRcpyN(char *out, char *in, long n_len)
{
    char *temp = NULL;
    long byteLen;

    /* Make a copy of the input string we can play with. */
    misDynStrcpy(&temp, in);

    /* Trim both sides of the string. */
    misTrimLR(temp);

    /* Copy it into the output string. */
    byteLen = utf8ByteLen(temp, n_len);

    memset(out, 0, byteLen+1);
    strncpy(out, temp, byteLen);

    free(temp);

    return out;
}

long misTrimStrncmp(char *s1, char *s2, long len)
{
    long l1, l2;

    l1 = misTrimLen(s1,len);
    l2 = misTrimLen(s2,len);

    return(strncmp(s1,s2,l1>l2?l1:l2));
}

long misTrimStrncmpN(char *s1, char *s2, long len)
{
    long l1, l2;

    l1 = misTrimLenN(s1,len);
    l2 = misTrimLenN(s2,len);

    return(strncmp(s1,s2,l1>l2?l1:l2));
}
