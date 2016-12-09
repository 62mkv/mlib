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
 * UTF8 Note:  We assume maxlenB is passed in as the max number of bytes
 */
long misBuildWhereList(char *colnam, char *colval, long maxlenB,
            char **whereList)
{
    char *temp, tempcol[100];

    /*
     * Let's assume they're not going to hit us with a huge column name
     */
    sprintf (tempcol,
         "%s%.*s=",
             *whereList?" and " :"", (int)(sizeof tempcol-4), colnam);

    if (!misDynStrcat(whereList, tempcol))
        return eERROR;

    temp = misQuoteString(colval, maxlenB, '\'');
    if (temp == NULL)
        return eERROR;

    if (!misDynStrcat(whereList, temp))
        return eERROR;

    free(temp);

    return (eOK);
}

long misBuildWhereListN(char *colnam, char *colval, long n_maxlen,
            char **whereList)
{
    char *temp, tempcol[100];

    /*
     * Let's assume they're not going to hit us with a huge column name
     */
    sprintf (tempcol,
         "%s%.*s=",
             *whereList?" and " :"", (int)(sizeof tempcol-4), colnam);

    if (!misDynStrcat(whereList, tempcol))
        return eERROR;

    temp = misQuoteStringN(colval, n_maxlen, '\'');
    if (temp == NULL)
        return eERROR;

    if (!misDynStrcat(whereList, temp))
        return eERROR;

    free(temp);

    return (eOK);
}
