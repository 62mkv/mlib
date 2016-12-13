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
#include <string.h>
#include <stdlib.h>
#include <stdarg.h>

#include <mocaerr.h>
#include <mislib.h>
#include <sqllib.h>

#include "srvprivate.h"

long srvAddColumn(RETURN_STRUCT *ret, char *column, char datatype, long length)
{
    long status,
	 numColumns;

    mocaDataRes *res;

    /* Get the result set for this return structure. */
    res = srvGetResults(ret);
    if (res == NULL)
	return eERROR;

    /* Add the column to the result set. */
    status = sqlAddColumn(res, column, datatype, length);
    if (status != eOK)
	return status;

    /* Give ourselves a nicer alias to work with. */
    numColumns = sqlGetNumColumns(res);

    /* Allocate space for the additional data type. */
    ret->DataTypes = realloc(ret->DataTypes, (numColumns + 1) * sizeof(char));
    if (ret->DataTypes == NULL)
	return eNO_MEMORY;

    /* Terminate our newly reallocated data types attribute. */
    ret->DataTypes[numColumns] = '\0';

    /* Tack on the new column datatype to our list */
    ret->DataTypes[numColumns-1] = datatype;

    return eOK;
}
