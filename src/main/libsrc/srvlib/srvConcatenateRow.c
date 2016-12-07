static char RCS_Id[] = "$Id$";
/*#START********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions used by the server and server applications
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
 *#END*********************************************************************/

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <oslib.h>
#include <sqllib.h>

#include "srvprivate.h"

/* 
 * Take the given row of the given result set and put it into a new
 * return struct.
 */
static long sCopyRow(RETURN_STRUCT **ret, 
		     mocaDataRes *res, 
		     mocaDataRow *row)
{
    long ii,
         status,
         long_val,
         colCount;

    double double_val;

    void *data;

    RETURN_STRUCT *TmpRes, 
		  *ReturnSet;
    SRV_RESULTS_LIST *reslist;

    ReturnSet = NULL;
    TmpRes = NULL;

    colCount = sqlGetNumColumns(res);

    if (res && ! row)
    {
	mocaDataRes *newres;

	/* we've got an empty result set - go ahead and copy the 
	 * header information.
	 */
	newres = sql_AllocateResultHdr(colCount);
	for (ii = 0; ii < colCount; ii++)
	{
	    sql_SetColName(newres, 
			   ii,
			   sqlGetColumnName(res, ii),
			   sqlGetDataTypeByPos(res, ii),
			   sqlGetDefinedColumnLenByPos(res, ii));
	    sql_SetColNullable(newres, 
			       sqlGetColumnName(res, ii),
			       sqlIsNullable(res, sqlGetColumnName(res, ii)));
	}
	
	ReturnSet = srvAddSQLResults(newres, eOK);

	*ret = ReturnSet;

	return eOK;
    }

    /* 
     * NOTE:  We cannot use the very attractive srvAddSQLResults
     * to accomplish the below...the reason is that it does NOT
     * actually copy results and insteads just redirects a pointer 
     * ...this routine must COPY the data...
     */

    reslist = srvCreateResultsList(colCount);

    for (ii = 0; ii < colCount; ii++)
    {
        switch (sqlGetDataTypeByPos(res, ii))
	{
	case COMTYP_TEXT:
	case COMTYP_STRING:
	case COMTYP_DATTIM:
	    status = 
	        srvBuildResultsList(reslist, 
				    ii, 
				    sqlGetColumnName(res, ii),
				    sqlGetDataTypeByPos(res, ii),
				    sqlGetDefinedColumnLenByPos(res,ii),
				    sqlIsNullByPos(res, row, ii),
				    sqlGetValueByPos(res, row, ii));
            break;
	case COMTYP_BOOLEAN:
	case COMTYP_INT:
	case COMTYP_LONG:
	    if (sqlIsNullByPos(res, row, ii))
		long_val = 0;
	    else
	        long_val = *(long *)sqlGetValueByPos(res, row, ii);

	    status = 
	        srvBuildResultsList(reslist, 
				    ii, 
				    sqlGetColumnName(res, ii),
				    sqlGetDataTypeByPos(res, ii),
				    sqlGetDefinedColumnLenByPos(res, ii),
				    sqlIsNullByPos(res, row, ii),
				    long_val);
		break;
	case COMTYP_FLOAT:
	    if (sqlIsNullByPos(res, row, ii))
		double_val = 0;
	    else
		double_val = *(double *)sqlGetValueByPos(res, row, ii);

	    status = 
		srvBuildResultsList(reslist, 
				    ii, 
				    sqlGetColumnName(res, ii),
				    sqlGetDataTypeByPos(res, ii),
				    sqlGetDefinedColumnLenByPos(res, ii),
				    sqlIsNullByPos(res, row, ii),
				    double_val);
	    break;
	case COMTYP_JAVAOBJ:
	case COMTYP_RESULTS:
	case COMTYP_GENERIC:
	    data = sqlGetValueByPos(res, row, ii);
	    status = 
	        srvBuildResultsList(reslist, 
				    ii, 
				    sqlGetColumnName(res, ii),
				    sqlGetDataTypeByPos(res, ii),
				    sizeof (void *),
				    0,
				    * (void **) data);
            break;
	default:
	    misLogError("srvConcatenateResults: Reached unexpected code");
	    misLogError("                       Data Type: %c",
	                sqlGetDataTypeByPos(res, ii));
	    return eERROR;
	    break;
	}

	if (status != eOK)
	{
	    if (ReturnSet)
	        srvFreeMemory(SRVRET_STRUCT, ReturnSet);
            srvFreeResultsList(reslist);
	    return status;
	}
    }
	
    TmpRes = srvResultsList(eOK, colCount, reslist);

    srvFreeResultsList(reslist);
    srvCombineResults(&ReturnSet, &TmpRes);

    *ret = ReturnSet;

    return eOK;
}

long srvConcatenateRow(RETURN_STRUCT **ret, 
		       mocaDataRes *res, 
		       mocaDataRow *row)
{
    long status;
    
    RETURN_STRUCT *temp;

    /* Make sure we have something to work with. */
    if (!ret || !res)
	return eINVALID_ARGS;

    status = sCopyRow(&temp, res, row);
    if (status != eOK)
	return status;

    status = srvCombineResults(ret, &temp);

    return status;
}
