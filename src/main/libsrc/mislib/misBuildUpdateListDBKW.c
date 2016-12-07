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
 *#END************************************************************************/

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <oslib.h>
#include <mislib.h>

/*
 * This function is used to build a update list and
 * where list for a process table action call. This function assumes that
 * the column value passed is a database keyword
 *
 * ProcessTableAction (in update mode) takes a where list of
 * command delimited fields and a update list of comma delimited
 * values to change.
 *
 * This function takes a column name, a value for the column, and a pointer
 * to a character pointer that we concantenate the update value to.
 */

long misBuildUpdateListDBKW(char *colnam, char *colval, long maxlen,
                            char **updateList)
{
    long tempSize;
    char *temp, tempcol[100];

    /*
     * Let's assume they're not going to hit us with a huge column name
     */
    sprintf(tempcol, "%s%.*s=",
        *updateList ? "," : "",
        (int) (sizeof tempcol-4), colnam);

    if (!misDynStrcat(updateList, tempcol))
        return eERROR;

    /*
     * We need to have a string that can have the string passed, including
     * a null
     */
    if (maxlen==0)
        maxlen = strlen(colval);

    tempSize = 1 + maxlen;

    temp = calloc(tempSize, 1);
    if (!temp)
        return eERROR;

    memcpy(temp, colval, maxlen);

    if (!misDynStrcat(updateList, temp))
    {
        free(temp);
        return eERROR;
    }

    free(temp);

    return eOK;
}

long misBuildUpdateListDBKWN(char *colnam, char *colval, long n_maxlen,
                char **updateList)
{

    char tempcol[100];
    char *temp = NULL;

    /*
     * Let's assume they're not going to hit us with a huge column name
     */
    sprintf(tempcol, "%s%.*s=",
        *updateList ? "," : "",
        (int) (sizeof tempcol-4), colnam);

    if (!misDynStrcat(updateList, tempcol))
        return eERROR;
    if (n_maxlen <=0)
    {
        n_maxlen = utf8CharLen(colval);
    }

    if (!misDynTrimncpyN(&temp, colval, n_maxlen) )
        return eERROR;

    if (!misDynStrcat(updateList, temp))
    {
        free(temp);
        return eERROR;
    }

    free(temp);

    return eOK;
}
