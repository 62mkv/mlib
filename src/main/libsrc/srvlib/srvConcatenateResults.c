static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
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
 *#END*************************************************************************/

#include <moca.h>

#include <string.h>
#include <stdlib.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <sqllib.h>
#include <oslib.h>
#include "srvprivate.h"

/* 
 * Take result set 2 and add it to result set 1 provided
 * all columns, etc, match.  Result set 2 is then cleaned up
 * and memory is freed where needed. 
 */
static long sCopyResults(RETURN_STRUCT **target, mocaDataRes *source)
{
    mocaDataRes *res;
    mocaDataRow *row;
    long colCount;
    long i;
    long status;
    double double_val;
    long long_val;

    RETURN_STRUCT *TmpRes, *ReturnSet;
    SRV_RESULTS_LIST *reslist;

    res = source;
    
    ReturnSet = NULL;
    TmpRes = NULL;

    colCount = sqlGetNumColumns(res);

    if (res && sqlGetRow(res) == NULL)
    {
	mocaDataRes *newres;

	/* we've got an empty result set - go ahead and copy the 
	 * header information.
	 */
	newres = sql_AllocateResultHdr(colCount);
	for (i = 0; i < colCount; i++)
	{
	    sql_SetColName(newres, 
			   i,
			   sqlGetColumnName(res, i),
			   sqlGetDataTypeByPos(res, i),
			   sqlGetDefinedColumnLenByPos(res, i));
	    sql_SetColNullable(newres, 
			       sqlGetColumnName(res, i),
			       sqlIsNullable(res, sqlGetColumnName(res, i)));
	}
	
	ReturnSet = srvAddSQLResults(newres, eOK);

	*target = ReturnSet;
	return(eOK);
    }
    /* 
     * NOTE:  We cannot use the very attractive srvAddSQLResults
     * to accomplish the below...the reason is that it does NOT
     * actually copy results and insteads just redirects a pointer 
     * ...this routine must COPY the data...
     */

    for (row = sqlGetRow(res); row; row = sqlGetNextRow(row))
    {
	reslist = srvCreateResultsList(colCount);

	for (i = 0; i < colCount; i++)
	{
	    switch (sqlGetDataTypeByPos(res, i))
	    {
	    case COMTYP_TEXT:
	    case COMTYP_STRING:
	    case COMTYP_DATTIM:
	    case COMTYP_GENERIC:
	    case COMTYP_RESULTS:
	    case COMTYP_JAVAOBJ:

		status = 
		    srvBuildResultsList(reslist, 
					i, 
					sqlGetColumnName(res, i),
					sqlGetDataTypeByPos(res, i),
					sqlGetDefinedColumnLenByPos(res,i),
					sqlIsNullByPos(res, row, i),
					sqlGetValueByPos(res, row, i));
		break;
	    case COMTYP_BOOLEAN:
	    case COMTYP_INT:
	    case COMTYP_LONG:
		if (sqlIsNullByPos(res, row, i))
		    long_val = 0;
		else
		    long_val = *(long *)sqlGetValueByPos(res, row, i);

		status = 
		    srvBuildResultsList(reslist, 
					i, 
					sqlGetColumnName(res, i),
					sqlGetDataTypeByPos(res, i),
					sqlGetDefinedColumnLenByPos(res, i),
					sqlIsNullByPos(res, row, i),
					long_val);
		break;
	    case COMTYP_FLOAT:
		if (sqlIsNullByPos(res, row, i))
		    double_val = 0;
		else
		    double_val = *(double *)sqlGetValueByPos(res, row, i);

		status = 
		    srvBuildResultsList(reslist, 
					i, 
					sqlGetColumnName(res, i),
					sqlGetDataTypeByPos(res, i),
					sqlGetDefinedColumnLenByPos(res, i),
					sqlIsNullByPos(res, row, i),
					double_val);
		break;
	    default:
		misLogError("srvConcatenateResults: Reached unexpected code");
		misLogError("                       Data Type: %c",
	            sqlGetDataTypeByPos(res, i));
		return eERROR;
		break;
	    }
	    if (status != eOK)
	    {
		if (ReturnSet)
		    srvFreeMemory(SRVRET_STRUCT, ReturnSet);
		srvFreeResultsList(reslist);
		return(status);
	    }
	}
	
	TmpRes = srvResultsList(eOK, colCount, reslist);

	srvFreeResultsList(reslist);
	srvCombineResults(&ReturnSet, &TmpRes);
    }

    *target = ReturnSet;
    return(eOK);
}

long srvConcatenateResults(RETURN_STRUCT **target, mocaDataRes *source)
{
    long           status;
    
    RETURN_STRUCT *TmpRes;

    /* Make sure we have something to work with. */
    if (!target || !source)
	return(eINVALID_ARGS);

    status = sCopyResults(&TmpRes, source);
    if (status != eOK)
	return(status);

    status = srvCombineResults(target, &TmpRes);
    return(status);
}
