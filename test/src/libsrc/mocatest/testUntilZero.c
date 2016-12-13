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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <oslib.h>
#include <srvlib.h>

LIBEXPORT 
RETURN_STRUCT *testUntilZero(long * value_i, char *arg)
{
    RETURN_STRUCT *results = NULL;
    mocaDataRes *sqlRes = NULL;
    long status = eOK;

    if (!value_i) {
        return (srvResults(eINVALID_ARGS, NULL));
    }
    
    if (*value_i > 0) {
        srvInitiateInlineFormat(&results, "test until zero where value = %li", *value_i - 1);
    }
    else
    {
        /* Create the return structure. */
        status = dbExecStr(arg, &sqlRes);
        results = srvAddSQLResults(sqlRes, status);
    }

    return results;
}
