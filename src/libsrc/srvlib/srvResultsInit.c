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
#include <string.h>
#include <stdarg.h>

#include <mocaerr.h>
#include <mislib.h>
#include <sqllib.h>

#include "srvprivate.h"

/*
 *  Build a return structure for data to be returned to the client.
 *  This just initializes the header structure and sets up the number
 *  and types of columns.
 */

RETURN_STRUCT *srvResultsInit(long status, ...)
{
    va_list args;
    int ncols;
    long ret_status;
    RETURN_STRUCT *ret;
    mocaDataRes *res;
    char *column;
    char datatype;
    int length;

    /*
     * Unfortunately, due to the structure of the sqllib calls, we've
     * got to know the number of columns ahead of time.  This means
     * we've got to loop through the arguments, counting the passed in
     * columns.
     */
    ncols = 0;
    va_start(args, status);
    while ((column = va_arg(args, char *)) != 0)
    {
        datatype = (char) va_arg(args, int);
        length = va_arg(args, int);
        ncols++;
    }
    va_end(args);

    /*
     * Now set up the Header structures.
     */
    ret = calloc (1, sizeof(RETURN_STRUCT));
    if (ret == NULL) return NULL;

    ret->DataTypes = calloc(ncols+1, sizeof(char));
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

    /*
     * Go through the argument list again.
     */
    ncols = 0;
    va_start(args, status);
    while ((column = va_arg(args, char *)) != 0)
    {
        datatype = (char) va_arg(args, int);
        length = va_arg(args, int);

        ret_status = sql_SetColName(res, ncols, column, datatype, length);
        if (ret_status != eOK)
        {
            srvFreeMemory(SRVRET_STRUCT, ret);
            return NULL;
        }
        ncols++;
    }
    va_end(args);

    if (res->DataType)
        strcpy(ret->DataTypes, res->DataType);

    return ret;
}
