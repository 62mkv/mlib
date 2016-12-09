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

#include <srvlib.h>
#include <mislib.h>

#include <mocaerr.h>

/*
 *
 * This function takes a column name, a value for the column, and a pointer
 * to a character pointer that we concantenate the update value to.
 *
 */

long  misBuildInsertListType(char *colnam, char dataType, void *colval,
             long maxlen, char **columnList, char **valueList)
{
    char *p;
    char *temp=NULL;
    char *tempVal=NULL;

    if (colval == NULL)
        return eOK;

    if (*columnList)
    {
       if (!misDynStrcat(columnList, ","))
       return (eERROR);
    }
    if (!misDynStrcat(columnList, colnam))
    return(eERROR);

    switch(dataType)
    {
        case COMTYP_STRING:
            /*
             * We need to have a string that can have the string passed,
             * including * doubling up of quotes,
             * a trailing quote and a NUL.
             */
            if (maxlen==0)
                maxlen = strlen((char *)colval);

            if (!misDynStrncpy(&tempVal, (char *)colval, maxlen))
                return eERROR;

            p = misStrReplaceAll(tempVal, "'", "''");
            free(tempVal);

            if (!misDynSprintf(&temp, "'%s'", p))
            {
                free(p);
                return eERROR;
            }
            free(p);
            break;
        case COMTYP_DATTIM:
            misDynSprintf(&temp, "to_date('%s')", (char *)colval);
            break;
        case COMTYP_BOOLEAN:
        case COMTYP_LONG:
        case COMTYP_INT:
            if (!misDynSprintf(&temp, "%ld", *(long *)colval))
                return eERROR;
            break;
        case COMTYP_FLOAT:
            if (!misDynSprintf(&temp, "%23.8f", *(double *)colval))
                return eERROR;
            break;
        default:
            if (!misDynSprintf(&temp, "'%.*s'",
                     maxlen==0 ? strlen((char *)colval) : maxlen,
                     (char *)colval))
                return eERROR;

            break;
    }
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
        return (eERROR);
    }
    free(temp);

    return (eOK);
}

long  misBuildInsertListTypeN(char *colnam, char dataType, void *colval,
             long n_maxlen, char **columnList, char **valueList)
{

    char *temp=NULL;

    if (colval == NULL)
        return eOK;

    if (*columnList)
    {
       if (!misDynStrcat(columnList, ","))
           return (eERROR);
    }
    if (!misDynStrcat(columnList, colnam))
        return(eERROR);

    switch(dataType)
    {
        case COMTYP_STRING:
            temp = misQuoteStringN((char*)colval, n_maxlen, '\'');
            if (temp == NULL)
                return eERROR;
            break;
        case COMTYP_DATTIM:
            misDynSprintf(&temp, "to_date('%s')", (char *)colval);
            break;
        case COMTYP_BOOLEAN:
        case COMTYP_LONG:
        case COMTYP_INT:
            if (!misDynSprintf(&temp, "%ld", *(long *)colval))
                return eERROR;
            break;
        case COMTYP_FLOAT:
            if (!misDynSprintf(&temp, "%23.8f", *(double *)colval))
                return eERROR;
            break;
        default:
            if (!misDynSprintf(&temp, "'%.*s'",
                      utf8ByteLen((char*)colval, n_maxlen),
                     (char *)colval))
                return eERROR;

            break;
    }
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
      return (eERROR);
    }
    free(temp);

    return (eOK);
}

