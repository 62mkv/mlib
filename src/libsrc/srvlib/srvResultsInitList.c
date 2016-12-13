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
RETURN_STRUCT *srvResultsInitList(long status, int ncols, SRV_RESULTS_LIST *list)
{
    int ii;
    long ret_status;
    mocaDataRes *res;
    RETURN_STRUCT *ret;

    /*
     * Unfortunately, due to the structure of the sqllib calls, we've
     * got to know the number of columns ahead of time.  This means
     * we've got to loop through the arguments, counting the passed in
     * columns.
     */

    /*
     * Set up the Header structures.
     */
    ret = calloc(1, sizeof(RETURN_STRUCT));
    if (ret == NULL) return NULL;

    ret->DataTypes = calloc(1, ncols+1);
    if (ret->DataTypes == NULL)
    {
	free(ret);
	return NULL;
    }

    res = sql_AllocateResultHdr(ncols);
    ret->ReturnedData = res;

    if (ret->ReturnedData == NULL)
    {
	free(ret->DataTypes);
	free(ret);
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
	/*
	 * Go through the argument list again.  This time, we actually pay
	 * attention to the data.
	 */
	for (ii=0;ii<ncols;ii++)
	{
	    ret_status = sql_SetColName(res, ii, list[ii].colname,
		                        list[ii].type, list[ii].size);
	    if (ret_status != eOK)
	    {
		srvFreeMemory(SRVRET_STRUCT, ret);
		return NULL;
	    }
	}

	strcpy(ret->DataTypes, res->DataType);
    }

    return ret;
}