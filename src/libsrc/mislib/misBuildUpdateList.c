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
 * This function is used to build a update list and
 * where list for a process table action call.
 *
 * ProcessTableAction (in update mode) takes a where list of
 * command delimited fields and a update list of comma delimited
 * values to change.
 *
 * This function takes a column name, a value for the column, and a pointer
 * to a character pointer that we concantenate the update value to.
 */

long misBuildUpdateList(char *colnam, char *colval, long maxlen, char **updateList)
{
    char *p,*q;
    long tempSize;
    char *temp, tempcol[100];

    /*
     * Let's assume they're not going to hit us with a huge column name
     */
    sprintf (tempcol,
         "%s%.*s='", *updateList?"," :"", (int)(sizeof tempcol-4), colnam);
    if (!misDynStrcat(updateList, tempcol))
        return eERROR;

    /*
     * We need to have a string that can have the string passed, including
     * doubling up of quotes, a trailing quote and a NUL.
     */
    if (maxlen==0)
        maxlen = strlen(colval);

    tempSize = 1 + maxlen + 1;

    /* Double up single quotes */
    for (p = colval; *p; p++)
    {
        if (*p == '\'') 
	    tempSize++;
    }

    temp = malloc(tempSize);

    p=temp;
    q=colval;
    while ((q-colval < maxlen) && *q)
    {
        if (*q == '\'')
        {
            *p++='\'';
        }
        *p++ = *q++;
    }
    *p++='\'';
    *p++='\0';

    if (!misDynStrcat(updateList, temp))
    {
        free(temp);
        return eERROR;
    }

    free(temp);

    return (eOK);
}

long misBuildUpdateListN(char *colnam, char *colval, long n_maxlen,
            char **updateList)
{
    char *temp, tempcol[100];

    /*
     * Let's assume they're not going to hit us with a huge column name
     */
    sprintf (tempcol,
         "%s%.*s=", *updateList?"," :" ", (int)(sizeof tempcol-2), colnam);
    if (!misDynStrcat(updateList, tempcol))
        return eERROR;

    /* we can just use misQuoteString */
    temp = misQuoteStringN(colval, n_maxlen, '\'');
    if (temp == NULL)
        return eERROR;

    if (!misDynStrcat(updateList, temp))
    {
        free(temp);
        return eERROR;
    }

    free(temp);

    return (eOK);
}
