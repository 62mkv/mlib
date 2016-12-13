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
 *#END************************************************************************/

#include <moca.h>

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <common.h>
#include <mocaerr.h>
#include <srvlib.h>
#include <sqllib.h>
#include <mislib.h>

static int getRowCount(char *table)
{
    long status;
    mocaDataRes *res = NULL;
    mocaDataRow *row = NULL;
    char buf[1000];
    int count;

    sprintf(buf, "select count(*) foo from %s", table);
    status = dbExecStr(buf, &res);

    if (status != eOK)
    {
	sqlFreeResults(res);
	misLogError("testSavepoint: Unexpected error status: %d", status);
	return -1;
    }

    row = sqlGetRow(res);

    count = sqlGetLong(res, row, "foo");

    sqlFreeResults(res);

    return count;
}

LIBEXPORT RETURN_STRUCT *testSavepoint(char *table, long *count)
{
    long status;
    char buf[1000];
    int initialCount;
    int midCount;
    int finalCount;

    initialCount = getRowCount(table);

    if (count && *count != initialCount)
    {
        return srvErrorResults(444,
			       "Bad Initial Count: expected ^expected^, got ^actual^",
			       "expected", COMTYP_INT, *count, 0,
			       "actual", COMTYP_INT, initialCount, 0,
			       NULL);
    }

    dbSetSavepoint("mocatest_savepoint");

    sprintf(buf, "delete from %s where 1=1", table);
    status = dbExecStr(buf, NULL);
    if (status != eOK && status != eDB_NO_ROWS_AFFECTED)
    {
	return srvResults(status, NULL);
    }

    midCount = getRowCount(table);
    if (midCount != 0) 
    {
        return srvErrorResults(444,
			       "Bad Count: expected ^expected^, got ^actual^",
			       "expected", COMTYP_INT, 0, 0,
			       "actual", COMTYP_INT, midCount, 0,
			       NULL);
    }

    dbRollbackToSavepoint("mocatest_savepoint");

    finalCount = getRowCount(table);
    if (finalCount != initialCount) 
    {
        return srvErrorResults(444,
			       "Bad Final Count: expected ^expected^, got ^actual^",
			       "expected", COMTYP_INT, initialCount, 0,
			       "actual", COMTYP_INT, finalCount, 0,
			       NULL);
    }

    return srvResults(eOK, NULL);
}

