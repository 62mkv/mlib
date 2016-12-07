static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions for returning data from the server and preparing
 *               results to return
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

#include <mocaerr.h>
#include <sqllib.h>

#include "srvprivate.h"

long srvAddColumnWithValue(RETURN_STRUCT *ret, char *column, char datatype,
                       long length, void *value)
{
    long status,
         colNumber,
         binLength;

    void *binValue;

    mocaDataRow *row;
    mocaDataRes *res;

    /* Add the new column to the return structure. */
    status = srvAddColumn(ret, column, datatype, length);
    if (status != eOK)
    {
        misLogError("srvAddColumnWithValue: Could not add column");
        return status;
    }

    /* Get the result set for this return structure. */
    res = srvGetResults(ret);
    if (! res)
    {
        misLogError("srvAddColumnWithValue: Could not get result set");
        return eERROR;
    }

    /* Get the column number for the new column. */
    colNumber = sqlFindColumn(res, column);
    if (colNumber < 0)
    {
        misLogError("srvAddColumnWithValue: Could not get column number");
        return eERROR;
    }

    /* Add a new row if the result set is empty. */
    if (! sqlGetRow(res))
    {
        status = srvAddRow(ret);
        if (status != eOK)
        {
            misLogError("srvAddColumnWithValue: Could not add row");
            return status;
        }
    }

    /* Populate the new column value in our result set. */
    for (row = sqlGetRow(res); row; row = sqlGetNextRow(row))
    {
        switch (datatype)
        {
            case COMTYP_STRING:
            case COMTYP_DATTIM:
                status = sql_AddRowItem(res, row, colNumber,
                                        srvTrimLen(value, length),
                                        value);
                break;

            case COMTYP_BOOLEAN:
            case COMTYP_INT:
            case COMTYP_LONG:
                status = sql_AddRowItem(res, row, colNumber,
                                        sizeof(long),
                                        value);
                break;

            case COMTYP_FLOAT:
                status = sql_AddRowItem(res, row, colNumber,
                                        sizeof(double),
                                        value);
                break;

            case COMTYP_JAVAOBJ:
            case COMTYP_RESULTS:
            case COMTYP_GENERIC:
                status = sql_AddRowItem(res, row, colNumber,
                                        sizeof(void *),
                                        value);
                break;

            case COMTYP_BINARY:

                binValue  = sqlEncodeBinary(value, length);
                binLength = sqlEncodeBinaryLen(binValue);

                status = sql_AddRowItem(res, row, colNumber,
                                        binLength,
                                        binValue);

                free(binValue);
                break;

            default:
                status = eNOT_IMPLEMENTED;
                misLogError("srvAddColumnWithValue: Unsupported datatype");
                break;
        }

        if (status != eOK)
        {
            misLogError("srvAddColumnWithValue: Could not add column value");
            return status;
        }
    }

    return eOK;
}
