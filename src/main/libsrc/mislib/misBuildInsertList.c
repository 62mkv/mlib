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
 *
 * This function takes a column name, a value for the column, and a pointer
 * to a character pointer that we concantenate the update value to.
 *
 * UTF8 Note:  We assume b_maxlen is passed in as the max number of bytes
 */

long  misBuildInsertList(char *colnam, char *colval,
             long b_maxlen, char **columnList, char **valueList)
{
    char *temp;

    if (*columnList)
    {
       if (!misDynStrcat(columnList, ","))
       return (eERROR);
    }
    if (!misDynStrcat(columnList, colnam))
        return(eERROR);

    temp = misQuoteString(colval, b_maxlen, '\'');
    if (temp == NULL)
        return (eERROR);
    if (*valueList)
    {
        if (!misDynStrcat(valueList, ","))
        {
            return (eERROR);
        }
    }

    if (!misDynStrcat(valueList, temp))
    {
        free(temp);
        return (eERROR);
    }
    free(temp);

    return (eOK);
}


long  misBuildInsertListN(char *colnam, char *colval,
             long n_maxlen, char **columnList, char **valueList)
{
    char *temp;

    if (*columnList)
    {
        if (!misDynStrcat(columnList, ","))
            return (eERROR);
    }

    if (!misDynStrcat(columnList, colnam))
        return(eERROR);

    temp = misQuoteStringN(colval, n_maxlen, '\'');
    if (temp == NULL)
        return (eERROR);

    if (*valueList)
    {
        if (!misDynStrcat(valueList, ","))
        {
            return (eERROR);
        }
    }

    if (!misDynStrcat(valueList, temp))
    {
        free(temp);
        return (eERROR);
    }
    free(temp);

    return (eOK);
}

