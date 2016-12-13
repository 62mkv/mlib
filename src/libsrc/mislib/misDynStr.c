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
#include <stdarg.h>
#include <string.h>
#include <ctype.h>
#include <oslib.h>
#include <mislib.h>

char *misDynStrcpy(char **s1, const char *s2)
{
    char *t1;

    if (!s2)
    {
        if (*s1)
            free(*s1);
        *s1 = NULL;
        return NULL;
    }

    if ((t1 = (char *) calloc(sizeof(char), strlen(s2) + 1)) == NULL)
    {
        if (*s1)
            free(*s1);
        *s1 = NULL;
        return (NULL);
    }

    strcpy(t1, s2);

    if (*s1)
        free(*s1);
    *s1 = t1;

    return (t1);
}

char *misDynStrncpy(char **s1, const char *s2, size_t count)
{
    char *t1;

    if (!s2)
    {
        if (*s1)
            free(*s1);
        *s1 = NULL;
        return NULL;
    }

    if ((t1 = (char *) calloc(sizeof(char), count + 1)) == NULL)
    {
        if (*s1)
            free(*s1);
        *s1 = NULL;
        return (NULL);
    }

    strncpy(t1, s2, count);

    if (*s1)
        free(*s1);
    *s1 = t1;

    return (t1);
}

char *misDynStrncpyN(char **s1, const char *s2, size_t n_count)
{
    char *t1;
    char *start;
    long charCnt;
    long byteCnt;
    char *p;

    if (!s2)
    {
        if (*s1)
            free(*s1);
        *s1 = NULL;
        return NULL;
    }

    /* make sure and allocate enough for the max possible need */
    if ((t1 = (char *) calloc(sizeof(char), (4* n_count) + 1)) == NULL)
    {
        if (*s1)
            free(*s1);
        *s1 = NULL;
        return (NULL);
    }

    start = t1;
    charCnt = 0;
    p = (char*)s2;

    while (*p && charCnt < (long )n_count)
    {
        byteCnt=utf8NextCharLen(p);
        while(byteCnt--)
            *t1++ = *p++;
        charCnt++;
    }


    if (*s1)
        free(*s1);

    /* reallocate to the appropriate length */
    if (t1 - start < ((long )n_count *4))
    {
        start = (char *)realloc(start, sizeof(char) * ((t1 - start)+1));
    }
    *s1 = start;

    return (start);
}

char *misDynStrcat(char **s1, const char *s2)
{
    char *t1;
    size_t origSize;
    size_t addSize;

    if (*s1)
        origSize = strlen(*s1);
    else
        origSize = 0;

    if (s2)
        addSize = strlen(s2);
    else
        addSize = 0;

    if ((t1 = (char *) realloc(*s1, origSize + addSize + 1)) == NULL)
        return (NULL);

    memcpy(t1 + origSize, s2, addSize + 1);

    *s1 = t1;

    return (t1);
}

char *misDynStrncat(char **s1, const char *s2, size_t count)
{
    char *t1;
    size_t origSize;

    if (*s1)
        origSize = strlen(*s1);
    else
        origSize = 0;

    if ((t1 = (char *) realloc(*s1, origSize + count + 1)) == NULL)
        return (NULL);

    strncpy(t1 + origSize, s2, count);

    t1[count + origSize] = 0;

    *s1 = t1;

    return (t1);
}

char *misDynStrncatN(char **s1, const char *s2, size_t n_count)
{
    char *t1;
    char *temp=NULL;
    size_t origSize;

    if (*s1)
        origSize = strlen(*s1);
    else
        origSize = 0;

    temp = misDynStrncpyN(&temp, s2, n_count);

    if (temp == NULL)
        return (NULL);

    if ((t1 = (char *) realloc(*s1, origSize + strlen(temp) + 1)) == NULL)
    {
        free(temp);
        return (NULL);
    }

    strcpy(t1 + origSize, temp);

    *s1 = t1;

    return (t1);
}


char *misDynCharcat(char **s1, const char s2)
{
    char *t1;
    size_t origSize;

    if (*s1)
        origSize = strlen(*s1);
    else
        origSize = 0;

    if ((t1 = (char *) realloc(*s1, origSize + 2)) == NULL)
        return (NULL);

    t1[origSize]   = s2;
    t1[origSize+1] = '\0';

    *s1 = t1;

    return t1;
}

long misDynSprintf(char **buffer, char *fmt, ...)
{
    long status,
     length;

    va_list args;

    free(*buffer);

    *buffer = NULL;

    va_start(args, fmt);

    /* Determine the length of the buffer. */
    if ((length = misSprintfLen(fmt, args)) < 0)
    {
        status = -1;
        goto cleanup;
    }

    /* Allocate space for the buffer. */
    if ((*buffer = malloc(length + 1)) == NULL)
    {
        status = -2;
        goto cleanup;
    }

    /* Build the buffer. */
    status = vsprintf(*buffer, fmt, args);

cleanup:
    va_end(args);

    return status;
}

void misFree(void *ptr)
{
    free(ptr);
}


/*
 * This is for safely copying and trimming a string variable, all at
 * once. Either call as prototyped or as misTrimcpy(out, in, len), and
 * outsize will be assumed to be len+1.
 */

/* UTF-8:  This has been modified to work with utf-8.
 * It's important to note that the oustsize is still byte based, because
 * it is used to define the length of the buffer being used.
 */
char *misDynTrimncpyN(char **s1, const char *s2, long n_count)
{
    char *t1;
    char *start;
    long charCnt;
    long byteCnt;
    char *p;
    char *endpos;
    long targetSize;

    if (!s2)
    {
        if (*s1)
            free(*s1);
        *s1 = NULL;
        return NULL;
    }

    /* make sure and allocate enough for the max possible need */
    targetSize = n_count >=0 ?  (4* n_count) : strlen(s2);

    if ((t1 = (char *) calloc(sizeof(char), targetSize + 1)) == NULL)
    {
        if (*s1)
            free(*s1);
        *s1 = NULL;
        return (NULL);
    }

    start = t1;
    charCnt = 0;

    p = (char*)s2;
    endpos = start;

    while (*p && (n_count <0 || charCnt < n_count ))
    {
        byteCnt = utf8NextCharLen(p);
        if  (byteCnt > 1 || !isspace((unsigned char)*p))
        {
            endpos = t1+byteCnt;
        }
        while (byteCnt--)
        {
            *t1++ = *p++;
        }
        charCnt++;
    }

    if (*s1)
        free(*s1);

    /* reallocate to the appropriate length */
    if (endpos - start < targetSize -1)
    {
        *(endpos) = '\0';
        start = (char *)realloc(start, sizeof(char) * ((endpos - start)+1));
    }
    *s1 = start;

    return (start);
}
char *misDynTrimncpy(char **s1, const char *s2, long n_count)
{
    long targetSize;

    if (!s2)
    {
        if (*s1)
            free(*s1);
        *s1 = NULL;
        return NULL;
    }

    /* make sure and allocate enough for the max possible need */
    targetSize = n_count >=0 ?  n_count : strlen(s2);

    if ((*s1 = (char *) calloc(sizeof(char), targetSize + 1)) == NULL)
    {
        if (*s1)
            free(*s1);
        *s1 = NULL;
        return (NULL);
    }

    misTrimncpy(*s1, (char *)s2, targetSize, targetSize+1);

    return (*s1);

}


