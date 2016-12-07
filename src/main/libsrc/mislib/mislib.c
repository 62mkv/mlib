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

#define DEBUG_OFF 1
#define DEBUG_ON  0

int app_debug_flag = DEBUG_OFF;
int lib_debug_flag = DEBUG_OFF;

/******************************************************************
 **
 ** misSetLibDebug() - set the state of library debugging (use "ON" or "OFF")
 **
 *******************************************************************/
void misSetLibDebug(char *status)
{
    if (strcmp(status, "ON") == 0)
        lib_debug_flag = DEBUG_ON;
    if (strcmp(status, "OFF") == 0)
        lib_debug_flag = DEBUG_OFF;

    return;
}


/******************************************************************
 **
 ** misLibDebug() - returns state of library debugging (on or off)
 **
 *******************************************************************/
int misLibDebug()
{
    if (lib_debug_flag == DEBUG_ON)
        return (eOK);
    else
        return (!eOK);
}


/******************************************************************
 **
 ** misSetDebug() - set the state of application debugging (use "ON" or "OFF")
 **
 *******************************************************************/
void misSetDebug(char *status)
{
    if (strcmp(status, "ON") == 0)
        app_debug_flag = DEBUG_ON;
    if (strcmp(status, "OFF") == 0)
        app_debug_flag = DEBUG_OFF;

    return;
}


/******************************************************************
 **
 ** misDebug() - returns state of application debugging (on or off)
 **
 *******************************************************************/
int misDebug()
{
    if (app_debug_flag == DEBUG_ON)
        return (eOK);
    else
        return (!eOK);
}


/* used in place of strncat in UTF\-8 encodings.  c
 * charcnt is a character count, which is different than
 * the byte count that strncpy uses */
char *misStrncatN(char *dest, char *src, long len)
{
    long byteLen;

    byteLen = utf8ByteLen(src, len);
    strncat(dest, src, byteLen);

    return dest;
}

/* used in place of strncpy in UTF-8 encodings. 
 * charcnt is a character count, which is different than
 * the byte count that strncpy uses
 * WARNING: This function will null terminate destination string and 
 * assumes enough space was allocated to accomidate it */
char *misStrncpyN(char *dest, char *src, long len)
{
    long charCnt, byteCnt;
    char *pSrc, *pDest;

    if (!src)  
        return NULL;

    charCnt =0;
    pSrc = src;
    pDest = dest;

    while (*pSrc && charCnt < len)
    {
        byteCnt = utf8NextCharLen(pSrc);
        while (byteCnt -- && *pSrc)
        {
            *pDest++ = *pSrc++;
        }
        charCnt ++;
    }
    /* NOTE: NULL terminate string no matter what -- dest buffer is assumed to be large enough */
    *pDest = '\0';


    return dest;
}

char *misStrtok(char *to_be_searched, char *chars)
{
    char *first;
    static char *last;

    if (to_be_searched)
    {
        last = strpbrk(to_be_searched, chars);
        if (last)
        {
            *last = '\0';
            return (to_be_searched);
        }

        return (last);
    }

    if (last)
    {
        first = last + 1;
        last = strpbrk(first, chars);
        if (last)
        {
            *last = '\0';
            return (first);
        }
        else
        {
            last = NULL;
            return (first);
        }
    }

    return (last);
}

char *misStrReplaceAll(char *str1, char *str2, char *str3)
{
    long ii, len1, len2, len3, newlen, templen, oldlen;

    char *temp;
    char *p      = NULL;
    char *newstr = NULL;
    char *oldstr = NULL;

    len1 = strlen(str1);
    len2 = strlen(str2);
    len3 = strlen(str3);

    oldstr = (char *) malloc(len1+1);
    strcpy(oldstr, str1);

    /* Don't bother if these strings are the same. */
    if (strcmp(str2, str3) == 0)
        return oldstr;

    /* Don't bother if we can't find a match. */
    temp = strstr(oldstr, str2);
    if (!temp)
        return oldstr;

    newlen = 0; ii = 0; oldlen = len1;

    while (temp)
    {
        ii++;

        /* Establish the length of our new string. */
        newlen = len1 + ii * (len3 - len2) + 1;

        /* Reallocate space for our new string. */
        newstr = (char *) realloc(newstr, newlen * sizeof(char));
        memset (newstr, 0, newlen);

        /* Get the offset in the string to the string we're looking for. */
        templen = strlen(temp);

        /* Copy into new string up to token position. */
        if (oldlen - templen > 0)
            strncpy(newstr, oldstr, (oldlen - templen));

        /* Tack on the replacement string. */
        strcat(newstr, str3);
        templen = strlen(newstr);

        /* Offset beyond our token to the remainder of the string. */
        temp = temp + len2;

        /* And tack on the rest of the string past the token. */
        if (strlen(temp))
            strcat(newstr, temp);

        /* Save old as new to prepare for subsequent replace */
        oldstr = (char *) realloc(oldstr, newlen * sizeof(char));
        strcpy(oldstr, newstr);
        oldlen = strlen(oldstr);

        /* Find next occurence of token. */
        temp = oldstr + templen;
        temp = strstr(temp, str2);
    }

    free(oldstr);

    return newstr;
}

char *misCiStrReplaceAll(char *str1, char *str2, char *str3)
{
    long ii, len1, len2, len3, newlen, templen, oldlen;

    char *temp;
    char *p      = NULL;
    char *newstr = NULL;
    char *oldstr = NULL;

    len1 = strlen(str1);
    len2 = strlen(str2);
    len3 = strlen(str3);

    oldstr = (char *) malloc(len1+1);
    strcpy(oldstr, str1);

    /* Don't bother if these strings are the same. */
    if (misCiStrcmp(str2, str3) == 0)
        return oldstr;

    /* Don't bother if we can't find a match. */
    temp = misCiStrstr(oldstr, str2);
    if (!temp)
        return oldstr;

    newlen = 0; ii = 0; oldlen = len1;

    while (temp)
    {
        ii++;

        /* Establish the length of our new string. */
        newlen = len1 + ii * (len3 - len2) + 1;

        /* Reallocate space for our new string. */
        newstr = (char *) realloc(newstr, newlen * sizeof(char));
        memset (newstr, 0, newlen);

        /* Get the offset in the string to the string we're looking for. */
        templen = strlen(temp);

        /* Copy into new string up to token position. */
        if (oldlen - templen > 0)
            strncpy(newstr, oldstr, (oldlen - templen));

        /* Tack on the replacement string. */
        strcat(newstr, str3);
        templen = strlen(newstr);

        /* Offset beyond our token to the remainder of the string. */
        temp = temp + len2;

        /* And tack on the rest of the string past the token. */
        if (strlen(temp))
            strcat(newstr, temp);

        /* Save old as new to prepare for subsequent replace */
        oldstr = (char *) realloc(oldstr, newlen * sizeof(char));
        strcpy(oldstr, newstr);
        oldlen = strlen(oldstr);

        /* Find next occurence of token. */
        temp = oldstr + templen;
        temp = misCiStrstr(temp, str2);
    }

    free(oldstr);

    return newstr;
}

char *misStrReplace(char *str1, char *str2, char *str3)
{
    char *newstr, *temp;
    int len1, len2, len3, newlen, templen;

    /* Don't bother if these strings are the same. */
    if (strcmp(str2, str3) == 0)
        return str1;

    len1 = strlen(str1);
    len2 = strlen(str2);
    len3 = strlen(str3);

    newlen = len1 + (len3 - len2) + 1;

    temp = strstr(str1, str2);
    if (temp == NULL)
        return temp;

    newstr = (char *) malloc(newlen * sizeof(char));
    memset(newstr, '\0', newlen);

    templen = strlen(temp);
    strncpy(newstr, str1, (len1 - templen));
    strcat(newstr, str3);
    temp = temp + len2;
    strcat(newstr, temp);

    return newstr;
}

char *misCiStrReplace(char *str1, char *str2, char *str3)
{
    char *newstr, *temp;
    int len1, len2, len3, newlen, templen;

    /* Don't bother if these strings are the same. */
    if (misCiStrcmp(str2, str3) == 0)
        return str1;

    len1 = strlen(str1);
    len2 = strlen(str2);
    len3 = strlen(str3);

    newlen = len1 + (len3 - len2) + 1;

    temp = misCiStrstr(str1, str2);
    if (temp == NULL)
        return temp;

    newstr = (char *) malloc(newlen * sizeof(char));
    memset(newstr, '\0', newlen);

    templen = strlen(temp);
    strncpy(newstr, str1, (len1 - templen));
    strcat(newstr, str3);
    temp = temp + len2;
    strcat(newstr, temp);

    return newstr;
}

long misCiStrcmp(char *s1, char *s2)
{   
    int upper=1;

    /* Check if it the same character array */
    if (s1 == s2)
       return 0;

    /* Check if strings are the same with out conversion */
    if (strcmp(s1,s2) == 0)
       return 0;

    /* Determine which comparision routine to use */
    if (isupper(*s1))
       upper=1;
    else
       upper=0;

    /* Check if strings are the same doing conversion to upper case */
    for (;((upper) && toupper((unsigned char)*s1) == toupper((unsigned char)*s2))
      ||
          ((!upper) && tolower((unsigned char)*s1) == tolower((unsigned char)*s2))
         ;s1++,s2++)
    {
        if (*s1 == '\0')
            return (0);
    }

    return ( (upper)?((toupper((unsigned char)*s1) < toupper((unsigned char)*s2))?-1:+1):
                     ((tolower((unsigned char)*s1) < tolower((unsigned char)*s2))?-1:+1));
}

long misCiStrncmp(char *s1, char *s2, long len)
{
    int upper=1;

    /* Check if it the same character array */
    if (s1 == s2)
       return 0;

    /* Check if strings are the same with out conversion */
    if (strncmp(s1,s2,len) == 0)
       return 0;

    /* Determine which comparision routine to use */
    if (isupper(*s1))
       upper=1;
    else
       upper=0;

    /* Check if strings are the same doing conversion to upper case */
    for (; 0 < len; s1++, s2++, len--)
    {
        unsigned char c1,c2;

        c1=(upper)?toupper((unsigned char)*s1):tolower((unsigned char)*s1);
        c2=(upper)?toupper((unsigned char)*s2):tolower((unsigned char)*s2);

        if (c1 != c2)
            return ((c1 < c2) ? -1 : +1);
        else if (*s1 == '\0')
            return (0);
    }

    return 0;
}

/* UTFTODO: Not really sure how do to this...
 * toupper/tolower may not work right for what we need
 * plus collation becomes a major issue here
 * For now it's just doing a byte length, which is bad
 */
long misCiStrncmpN(char *s1, char *s2, long len)
{
    int upper=1;

    /* Check if it the same character array */
    if (s1 == s2)
       return (0);

    /* Check if strings are the same with out conversion */
    if (strncmp(s1,s2,len) == 0)
       return (0);

    /* Determine which comparision routine to use */
    if (isupper(*s1))
       upper=1;
    else
       upper=0;

    /* Check if strings are the same doing conversion to upper case */
    for (; 0 < len; s1++, s2++, len--)
    {
        unsigned char c1,c2;

        c1=(upper)?toupper((unsigned char)*s1):tolower((unsigned char)*s1);
        c2=(upper)?toupper((unsigned char)*s2):tolower((unsigned char)*s2);

        if (c1 != c2)
            return ((c1 < c2) ? -1 : +1);
        else if (*s1 == '\0')
            return (0);
    }

    return (0);
}

long misStrncmpN(char *s1, char *s2, long len)
{
    unsigned char c1;
    unsigned char c2;
    int nBytes;

    /* Check if it the same character array */
    if (s1 == s2)
       return (0);

    /* Check if strings are the same doing conversion to upper case */
    for (nBytes=0; 0 < len; s1++, s2++)
    {
        c1 = (unsigned)*s1;
        c2 = (unsigned)*s2;

        if (c1 != c2)
            return ((c1 < c2) ? -1 : +1);
        else if (*s1 == '\0')
            return 0;

        /* if they are equal, we can advance them both */
        if (nBytes==0)
        {
            len --;
            /* get the number of bytes in this char */
            nBytes = utf8NextCharLen(s1) -1;
        }
    }

    return (0);
}

long misStrncmp(char *s1, char *s2, long len)
{
    /* Check if it the same character array */
    if (s1 == s2)
       return (0);

    return 0;
}

char *misCiStrstr(char *s1, char *s2)
{ 
    int upper;

    /* Determine which comparision routine to use */
    if (isupper(*s1))
       upper=1;
    else
       upper=0;

    for (; *s1; s1++)
    {
        if (((upper) && toupper((unsigned char)*s1) == toupper((unsigned char)*s2)) ||
            ((!upper) && tolower((unsigned char)*s1) == tolower((unsigned char)*s2)))
        {
            const char *sc1, *sc2;
            for (sc1=s1,sc2=s2;;)
            {
                if (*++sc2 == '\0')
                    return (char *)s1;
                else if (((upper) && toupper((unsigned char)*++sc1)!=toupper((unsigned char)*sc2))
                 ||
                     ((!upper) && tolower((unsigned char)*++sc1)!=tolower((unsigned char)*sc2)))
                break;
            }
        }
    }

    return NULL;
}

/* 
 * This function is used to "scrub" characters from an input
 * string and replace them with other characters.
 * Preconditions: input_string, search_string, and replace_string
 *          must be NULL terminated, passed by reference
 *          and NOT be separated by spaces
 * Postconditions: input_string will have characters replaced
 *          with appropriate substitutions from
 *          replace_string
 * Returns: Number of characters that have been replaced
 *
 */

char *misReplaceChars(char *input_string,
                      char *search_string,
                      char *replace_string)
{
    char *ptr;
    int i;

    for (ptr = input_string; (ptr = strpbrk(ptr, search_string)); ptr++)
    {
        for (i=0;search_string[i];i++)
        {
            if (*ptr == search_string[i])
            {
                *ptr = replace_string[i];
                break;
            }
        }
    }

    return input_string;
}

char *misToLower(char *s)
{
    register char *p;

    for (p=s;*p;p++)
        *p = tolower((unsigned char)*p);

    return s;
}

char *misToUpper(char *s)
{
    register char *p;

    for (p=s;*p;p++)
        *p = toupper((unsigned char)*p);

    return s;
}
