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


#include <mocaerr.h>
#include <oslib.h>
#include <mislib.h>

/*
 *
 * This function takes a column name, a value for the column, and a pointer
 * to a character pointer that we concantenate the update value to.
 * It assumes that the value passed is a database keywork(reserved) word
 *
 */

long misBuildInsertListDBKW(char *colnam, char *colval, long maxlen,
                char **columnList, char **valueList)
{
    char *temp;
    long newSize;

    if (*columnList)
    {
        if (!misDynStrcat(columnList, ","))
        return eERROR;
    }

    if (!misDynStrcat(columnList, colnam))
        return eERROR;

    if (maxlen == 0)
        maxlen = strlen (colval);

    /* need to null terminate the string */
    newSize = maxlen + 1;

    temp = calloc(newSize, 1);
    if (!temp)
        return (eERROR);

    memcpy(temp, colval, maxlen);

    /* add a comma */
    if (*valueList)
    {
        if (!misDynStrcat(valueList, ","))
        {
            free(temp);
            return eERROR;
        }
    }

    if (!misDynStrcat(valueList, temp))
    {
        free(temp);
        return eERROR;
    }

    free(temp);

    return eOK;
}

long misBuildInsertListDBKWN(char *colnam, char *colval, long n_maxlen,
                             char **columnList, char **valueList)
{
    char *temp=NULL;

    if (*columnList)
    {
        if (!misDynStrcat(columnList, ","))
            return eERROR;
    }

    if (!misDynStrcat(columnList, colnam))
        return eERROR;

    if (n_maxlen <=0)
    {
        n_maxlen = utf8CharLen(colval);
    }
    if (!misDynTrimncpyN(&temp, colval, n_maxlen))
        return (eERROR);

    /* add a comma */
    if (*valueList)
    {
        if (!misDynStrcat(valueList, ","))
        {
            free(temp);
            return eERROR;
        }
    }

    if (!misDynStrcat(valueList, temp))
    {
        free(temp);
        return eERROR;
    }

    free(temp);

    return eOK;
}
