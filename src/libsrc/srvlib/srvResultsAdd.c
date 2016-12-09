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
#include <string.h>
#include <stdlib.h>
#include <stdarg.h>

#include <mocaerr.h>
#include <mislib.h>
#include <sqllib.h>

#include "srvprivate.h"

/*
 *  Add to a return structure to be sent to the client.
 */
long srvResultsAdd(RETURN_STRUCT *Ret,...)
{
    va_list Arguments;
    int i;
    long ret_status;
    long TempLong;
    long TempSize;
    char *TempString;
    void *TempAddr;
    double TempDouble;
    mocaDataRes *Res;
    mocaDataRow *Row;

    if (!Ret || !Ret->ReturnedData)
        return eERROR;

    Res = Ret->ReturnedData;

    Row = sql_AllocateRow(Res);
    Res->NumOfRows++;
    Ret->rows++;

    /*
     * Go through the argument list.
     */
    va_start(Arguments, Ret);
    for (i=0; i<Res->NumOfColumns; i++)
    {
        switch (Res->DataType[i])
        {
        case COMTYP_STRING:
        case COMTYP_DATTIM:
            TempString = va_arg(Arguments, char *);

            /* Place the value in the Res structure...  */
            /* only trim the length if there is a defined max length */
            if (Res->DefinedMaxLen[i] <=0)
            {
                TempSize = TempString ? strlen(TempString) : 0;
            }
            else
            {
    
                TempSize = srvTrimLen(TempString, Res->DefinedMaxLen[i]);
            }
            ret_status = sql_AddRowItem(Res, Row, i, TempSize, TempString);
    
            if (ret_status != eOK)
            {
            va_end(Arguments);
            return eERROR;
            }
            break;

        case COMTYP_BOOLEAN:
        case COMTYP_INT:
        case COMTYP_LONG:
            TempLong = va_arg(Arguments, long);
    
            ret_status = sql_AddRowItem(Res, Row, i,
                        sizeof(long), &TempLong);
            if (ret_status != eOK)
            {
                va_end(Arguments);
                return eERROR;
            }
            break;

        case COMTYP_FLOAT:
            TempDouble = va_arg(Arguments, double);
    
            ret_status = sql_AddRowItem(Res, Row, i,
                        sizeof(double), &TempDouble);
            if (ret_status != eOK)
            {
                va_end(Arguments);
                return eERROR;
            }
            break;

        case COMTYP_GENERIC:
        case COMTYP_RESULTS:
        case COMTYP_JAVAOBJ:
            TempAddr = va_arg(Arguments, void *);
    
            ret_status = sql_AddRowItem(Res, Row, i,
                        sizeof(void *), &TempAddr);
            if (ret_status != eOK)
            {
                va_end(Arguments);
                return eERROR;
            }
            break;

        case COMTYP_BINARY:
            TempLong = va_arg(Arguments, long);
            TempAddr = va_arg(Arguments, void *);
    
            TempAddr = sqlEncodeBinary(TempAddr, TempLong);
            TempLong = sqlEncodeBinaryLen(TempAddr);
    
            ret_status = sql_AddRowItem(Res, Row, i,
                            TempLong, TempAddr);
            free(TempAddr);

            if (ret_status != eOK)
            {
                va_end(Arguments);
                return eERROR;
            }
            break;
        }
    }
    va_end(Arguments);

    return eOK;
}
