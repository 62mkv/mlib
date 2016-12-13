static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: sql_Set* routines are used to set underlying data element
 *               values in SQLLIB structure.  Their purpose is to hide the
 *               the underlying data structure from NON-SQLLIB callers
 *               (specifically, srvlib)
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
#include <string.h>
#include <stdlib.h>

#include <mocaerr.h>
#include <mislib.h>
#include <sqllib.h>

/*
 * sqlSetFirstRow - set the first data row for an mocaDataRes struct
 *
 */
void sql_SetFirstRow(mocaDataRes *res, mocaDataRow *row)
{
    if (res)
        res->Data = row;

    return;
}

/*
 * sqlSetLastRow - set the Last data row for an mocaDataRes struct
 *
 */
void sql_SetLastRow(mocaDataRes *res, mocaDataRow *row)
{
    if (res)
        res->LastRow = row;

    return;
}

/*
 * sqlSetNextRow - set the next data row in a list of
 *                 data rows.
 *
 */
void sql_SetNextRow(mocaDataRow *origRow,
                    mocaDataRow *newRow,
                    mocaDataRes *origRes,
                    mocaDataRes *newRes)
{
    if (!origRow)
        return;

    if (newRes && origRes)
    {
        long i;
        /* Buzz through and ensure that the lengths are
           set appropriately for the header information */
        for (i = 0; i < origRes->NumOfColumns; i++)
        {
            if (i < newRes->NumOfColumns)
            {
                if (newRes->ActualMaxLen[i] > origRes->ActualMaxLen[i])
                    origRes->ActualMaxLen[i] = newRes->ActualMaxLen[i];
                if (newRes->DefinedMaxLen[i] > origRes->DefinedMaxLen[i])
                    origRes->DefinedMaxLen[i] = newRes->DefinedMaxLen[i];
            }
        }
    }

    origRow->NextRow = newRow;

    return;
}

/*
 *  sqlSetColName - sets the name of a column along with it's
 *                  data type.
 */
long sql_SetColName(mocaDataRes *res,
                    long colNumber,
                    char *colName,
                    char colType,
                    long colLength)
{
    long slen;

    slen = strlen(colName);
    res->ColName[colNumber] = (char *) calloc(1, slen + 1);

    if (res->ColName[colNumber] == NULL)
        return(eNO_MEMORY);

    misTrimncpy(res->ColName[colNumber], colName, slen, slen + 1);

    res->HashValue[colNumber] = misCiHash(res->ColName[colNumber]);

    res->DataType[colNumber] = colType;
    res->DefinedMaxLen[colNumber] = colLength;
    res->ActualMaxLen[colNumber] = 0;
    res->Nullable[colNumber] = 1;

    return(eOK);
}

long sql_SetColNullableByPos(mocaDataRes *res,
                             long colNumber,
                             moca_bool_t nullable)
{
    if (!res)
        return eERROR;

    res->Nullable[colNumber] = nullable;

    return(eOK);
}

long sql_SetColNullable(mocaDataRes *res,
                        char *colName,
                        moca_bool_t nullable)
{
    long Col;

    if (!res)
        return eERROR;

    if (-1 == (Col = sqlFindColumn(res, colName)))
        return eERROR;

    return sql_SetColNullableByPos(res,Col,nullable);
}

long sql_SetActualColumnLen(mocaDataRes *res,
                            char *colName,
                            long colLength)
{
    long Col;

    if (!res)
        return eERROR;

    if (-1 == (Col = sqlFindColumn(res, colName)))
        return eERROR;

    res->ActualMaxLen[Col] = colLength;

    return(eOK);
}
