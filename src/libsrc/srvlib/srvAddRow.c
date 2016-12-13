static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Add a row to a return struct.
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

#include <mocaerr.h>
#include <sqllib.h>
#include <srvlib.h>

#include "srvprivate.h"


/*
 *  FUNCTION: srvAddRow
 *
 *  PURPOSE:  Add a row to a return struct.
 *
 *  RETURNS:  eOK - All ok.
 *            Some error code.
 */

long srvAddRow(RETURN_STRUCT *ret)
{
    mocaDataRow *row;
    mocaDataRes *res;

    /* Validate the arguments. */
    if (!ret)
	return eINVALID_ARGS;

    /* Get the result set for this return struct. */
    res = srvGetResults(ret);
    if (! res)
    {
        misLogError("srvAddRow: NULL result set inside return struct");
	return eERROR;
    }

    /* Add a row to the result set. */
    row = sqlAddRow(res);
    if (! row)
    {
        misLogError("srvAddRow: Could not add row to return struct");
	return eERROR;
    }

    /* Increment the number of rows in the return struct. */
    ret->rows++;

    return eOK;
}
