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
 *  Build a return structure for data to be returned to
 *  the client.
 */
long srvResultsAddList(RETURN_STRUCT *Ret, SRV_RESULTS_LIST *list)
{
    long ret_status;
    mocaDataRes *Res;
    mocaDataRow *Row;
    int ii;

    if (!Ret || !Ret->ReturnedData)
        return eERROR;

    Res = Ret->ReturnedData;

    Row = sql_AllocateRow(Res);
    Res->NumOfRows++;
    Ret->rows++;

    /*
     * Go through the argument list again.  This time, we actually pay
     * attention to the data.
     */
    for (ii=0;ii<Res->NumOfColumns;ii++)
    {
        switch (list[ii].type)
        {
        case COMTYP_STRING:
        case COMTYP_DATTIM:
            /* Place the value in the Res structure...  */
            ret_status = sql_AddRowItem(Res, Row, ii,
                                        srvTrimLen(list[ii].data.cdata, list[ii].size),
                                        list[ii].data.cdata);

            if (ret_status != eOK)
                return (ret_status);
            break;

        case COMTYP_BOOLEAN:
        case COMTYP_INT:
        case COMTYP_LONG:
            ret_status = sql_AddRowItem(Res, Row, ii,
                                        sizeof(long), &list[ii].data.ldata);
            if (ret_status != eOK)
                return (ret_status);
            break;

        case COMTYP_FLOAT:
            ret_status = sql_AddRowItem(Res, Row, ii,
                                        sizeof(double), &list[ii].data.fdata);
            if (ret_status != eOK)
                return (ret_status);
            break;

        case COMTYP_GENERIC:
            ret_status = sql_AddRowItem(Res, Row, ii,
                                        sizeof(void *), &list[ii].data.vdata);
            if (ret_status != eOK)
                return (ret_status);
            break;
    
        case COMTYP_BINARY:
            ret_status = sql_AddRowItem(Res, Row, ii,
                        list[ii].size, list[ii].data.vdata);
            if (ret_status != eOK)
                return (ret_status);
            break;
        }
        Row->NullInd[ii] = list[ii].nullind;
    }

    return eOK;
}
