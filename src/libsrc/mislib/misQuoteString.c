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
 *#END************************************************************************/

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mislib.h>
#include <mocaerr.h>

/*
 * This function is used to add 'quotes' around a string and escape the
 * 'quotes' in the string by doubling them up.
 *
 * The character used for quotes is passed in to the function.
 *
 * This function takes an original string, a maximum length to trim the
 * string at and the character to use for the quote.
 *
 * It returns a dynamically allocated string that contains the newly quoted
 * string.  It is the caller's responsibility to free this string after use
 *
 */
char *misQuoteString(char *orig, long maxlen, char quote)
{
    long tempSize;
    char *newstr=NULL;
    char *newptr;
    char *origptr;
    long charCnt;

    if (orig == NULL || maxlen < 0)
        return NULL;

    /* If we didn't get a maxlen, we'll assume we're quoting the whole
     * string */
    if (maxlen==0)
    {
        tempSize = strlen(orig);
    }
    else
    {
        tempSize = maxlen;
    }

    charCnt = tempSize;

    /* add room for the quotes and the NULL */
    tempSize += 3;

    /* Double up single quotes */
    for (origptr = orig; *origptr; origptr++)
    {
        if (*origptr ==quote) 
	    tempSize++;
    }

    newstr = malloc(tempSize);

    newptr=newstr;
    origptr=orig;

    /* add the first quote */
    *newptr++ = quote;

    while (*origptr && charCnt-- > 0)
    {
        if (*origptr == quote)
        {
            *newptr++=quote;
        }
        *newptr++ = *origptr++;
    }

    /* add the trailing quote and a null */
    *newptr++=quote;
    *newptr++='\0';

    return newstr;
}
char *misQuoteStringN(char *orig, long n_maxlen, char quote)
{
    long tempSize;
    char *newstr=NULL;
    char *newptr;
    char *origptr;
    long charCnt;

    if (orig == NULL || n_maxlen < 0)
        return NULL;

    /* If we didn't get a maxlen, we'll assume we're quoting the whole
     * string */
    if (n_maxlen==0)
    {
        /* charCnt might not be accurate here if we have utf-8 characters
         * but it won't really matter because we won't hit the max anyway */
        tempSize = strlen(orig);
        charCnt = tempSize;
    }
    else
    {
        tempSize = n_maxlen*4;
        charCnt = n_maxlen;
    }

    /* add room for the quotes and the NULL */
    tempSize += 3;

    /* Double up single quotes */
    for (origptr = orig; *origptr; origptr++)
    {
        if (*origptr ==quote) 
	    tempSize++;
    }
    newstr = malloc(tempSize);

    newptr=newstr;
    origptr=orig;

    /* add the first quote */
    *newptr++ = quote;

    while (*origptr && charCnt-- > 0)
    {
        if (*origptr == quote)
        {
            *newptr++=quote;
        }
        *newptr++ = *origptr++;

        /* copy the rest of the character if it has extended bytes
           Note we treat each byte as a character if no max is passed in */
        while (n_maxlen >0 && misCharIsExt(*origptr))
        {
            *newptr++ = *origptr++;
        }
    }

    /* add the trailing quote and a null */
    *newptr++=quote;
    *newptr++='\0';

    return newstr;
}
