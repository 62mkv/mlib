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
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>

#include <mocaerr.h>
#include <mislib.h>
#include <sqllib.h>

#include "srvprivate.h"

/*
 *  Build a return structure for data to be returned to
 *  the client.
 */
RETURN_STRUCT *srvResults(long status, ...)
{
    va_list args;
    int ncols;
    long ret_status;
    long tempLong;
    long tempSize;
    char *tempString;
    void *tempAddr;
    double tempDouble;
    RETURN_STRUCT *ret;
    mocaDataRes *res;
    mocaDataRow *row;
    char *column;
    char datatype;
    int length;

    /*
     * Unfortunately, due to the structure of the sqllib calls, we've
     * got to know the number of columns ahead of time.  This means
     * we've got to loop through the arguments, counting the passed in
     * columns.
     */
    ncols = 0;
    va_start(args, status);
    while ((column = va_arg(args, char *)) != 0)
    {
        datatype = (char) va_arg(args, int);
        length = va_arg(args, int);

        switch (datatype)
        {
        case COMTYP_STRING:
        case COMTYP_DATTIM:
            tempString = (char *)va_arg(args, char *);
            break;
        case COMTYP_BOOLEAN:
        case COMTYP_INT:
        case COMTYP_LONG:
            tempLong = va_arg(args, long);
            break;
        case COMTYP_FLOAT:
            tempDouble = va_arg(args, double);
            break;
        case COMTYP_RESULTS:
        case COMTYP_JAVAOBJ:
        case COMTYP_BINARY:
        case COMTYP_GENERIC:
            tempAddr = va_arg(args, void *);
            break;
        }

        ncols++;
    }
    va_end(args);

    ret = calloc (1, sizeof(RETURN_STRUCT));
    if (ret == NULL) return NULL;

    ret->DataTypes = calloc(1,ncols+1);
    if (ret->DataTypes == NULL)
    {
        free(ret);
        return NULL;
    }

    res = sql_AllocateResultHdr(ncols);

    ret->ReturnedData = res;

    if (ret->ReturnedData == NULL)
    {
        srvFreeMemory(SRVRET_STRUCT, ret);
        return NULL;
    }

    ret->Error.Code       = status;
    /*
     * SERIOUS HACK.
     *
     * As a bridge to the next release, we'll be using the obsolete
     * "Header" attribute to populate the caught error code.
     *
    ret->Error.CaughtCode = status;
    */
    ret->Header = (char *) status;

    ret->Error.Args = NULL;

    if (ncols > 0)
    {
        row = sql_AllocateRow(res);
        res->NumOfRows++;
        ret->rows++;

        /*
         * Go through the argument list again.  This time, we actually pay
         * attention to the data.
         */
        ncols = 0;
        va_start(args, status);
        while ((column = va_arg(args, char *)) != 0)
        {
            datatype = (char) va_arg(args, int);
            length = va_arg(args, int);

            ret_status = sql_SetColName(res, ncols, column, datatype, length);
            if (ret_status != eOK)
            {
                srvFreeMemory(SRVRET_STRUCT, ret);
                return NULL;
            }

            switch (datatype)
            {
            case COMTYP_STRING:
            case COMTYP_DATTIM:
                tempString = va_arg(args, char *);
                if (length <= 0)
                {
                    tempSize = tempString ? strlen(tempString) : 0;
                }
                else
                {
                    tempSize = srvTrimLen(tempString, length);
                }

                /* Place the value in the res structure...  */
                ret_status = sql_AddRowItem(res, row, ncols, tempSize, tempString);

                if (ret_status != eOK)
                {
                    va_end(args);
                    srvFreeMemory(SRVRET_STRUCT, ret);
                    return NULL;
                }
                break;

            case COMTYP_BOOLEAN:
            case COMTYP_INT:
            case COMTYP_LONG:
                tempLong = va_arg(args, long);

                ret_status = sql_AddRowItem(res, row, ncols,
                                sizeof(long), &tempLong);
                if (ret_status != eOK)
                {
                    va_end(args);
                    srvFreeMemory(SRVRET_STRUCT, ret);
                    return NULL;
                }
                break;

            case COMTYP_FLOAT:
                tempDouble = va_arg(args, double);

                ret_status = sql_AddRowItem(res, row, ncols,
                                sizeof(double), &tempDouble);
                if (ret_status != eOK)
                {
                    va_end(args);
                    srvFreeMemory(SRVRET_STRUCT, ret);
                    return NULL;
                }
                break;

            case COMTYP_RESULTS:
            case COMTYP_JAVAOBJ:
            case COMTYP_GENERIC:
                tempAddr = va_arg(args, void *);

                if (tempAddr)
                    ret_status = sql_AddRowItem(res, row, ncols,
                                    sizeof(void *), &tempAddr);
                if (ret_status != eOK)
                {
                    va_end(args);
                    srvFreeMemory(SRVRET_STRUCT, ret);
                    return NULL;
                }
                break;

            case COMTYP_BINARY:
                tempAddr = va_arg(args, void *);

                tempAddr = sqlEncodeBinary(tempAddr, length);
                tempLong = sqlEncodeBinaryLen(tempAddr);

                ret_status = sql_AddRowItem(res, row, ncols,
                                tempLong, tempAddr);
                free(tempAddr);

                if (ret_status != eOK)
                {
                    va_end(args);
                    srvFreeMemory(SRVRET_STRUCT, ret);
                    return NULL;
                }

                break;
            }
            ncols++;
        }
        va_end(args);

        strcpy(ret->DataTypes, res->DataType);
    }

    return ret;
}
