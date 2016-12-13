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
#include <stdarg.h>
#include <string.h>

#include <mocaerr.h>
#include <mislib.h>
#include <sqllib.h>

#include "srvprivate.h"

/*
 *  Build a return structure for data to be returned to
 *  the client.
 */
RETURN_STRUCT *srvResultsList(long status, int ncols, SRV_RESULTS_LIST *list)
{
    int ii;
    long ret_status;

    mocaDataRes *res;
    mocaDataRow *row;
    unsigned char *encoded;
    long encodedLen;


    RETURN_STRUCT *ret;

    /*
     * Unfortunately, due to the structure of the sqllib calls, we've
     * got to know the number of columns ahead of time.  This means
     * we've got to loop through the arguments, counting the passed in
     * columns.
     */
    ret = calloc(1, sizeof(RETURN_STRUCT));
    if (ret == NULL) return NULL;

    ret->DataTypes = calloc(1, ncols+1);
    if (ret->DataTypes == NULL) return NULL;

    res = sql_AllocateResultHdr(ncols);

    ret->ReturnedData = res;

    if (ret->ReturnedData == NULL) return NULL;

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
        for (ii=0;ii<ncols;ii++)
        {
            ret_status = sql_SetColName(res,
                                        ii,
                                        list[ii].colname,
                                        list[ii].type,
                                        list[ii].size);
            if (ret_status != eOK)
            {
                srvFreeMemory(SRVRET_STRUCT, ret);
                return NULL;
            }

            switch (list[ii].type)
            {
            case COMTYP_STRING:
            case COMTYP_DATTIM:
                /* Place the value in the Res structure...  */
                ret_status = sql_AddRowItem(res,
                                            row,
                                            ii,
                                            srvTrimLen(list[ii].data.cdata,
                                            list[ii].size),
                                            list[ii].data.cdata);

                if (ret_status != eOK)
                    return NULL;
                break;

            case COMTYP_BOOLEAN:
            case COMTYP_INT:
            case COMTYP_LONG:
                ret_status = sql_AddRowItem(res,
                                            row,
                                            ii,
                                            sizeof(long),
                                            &list[ii].data.ldata);
                if (ret_status != eOK)
                    return NULL;
                break;

            case COMTYP_FLOAT:
                ret_status = sql_AddRowItem(res,
                                            row,
                                            ii,
                                            sizeof(double),
                                            &list[ii].data.fdata);
                if (ret_status != eOK)
                    return NULL;
                break;

            case COMTYP_GENERIC:
            case COMTYP_RESULTS:
            case COMTYP_JAVAOBJ:
                ret_status = sql_AddRowItem(res,
                                            row,
                                            ii,
                                            sizeof(void *),
                                            &list[ii].data.vdata);
                if (ret_status != eOK)
                    return NULL;
                break;

            case COMTYP_BINARY:
                encoded = sqlEncodeBinary(list[ii].data.vdata, list[ii].size);
                encodedLen = sqlEncodeBinaryLen(encoded);

                ret_status = sql_AddRowItem(res,
                                            row,
                                            ii,
                                            encodedLen,
                                            encoded);

                free(encoded);

                if (ret_status != eOK)
                    return NULL;
                break;
            }

            row->NullInd[ii] = list[ii].nullind;
        }

        strcpy(ret->DataTypes, res->DataType);
    }

    return ret;
}
