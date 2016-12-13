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

/*
 *  Set the Null Indicator for a result set to be sent to the client.
 */
long srvResultsNull(RETURN_STRUCT *Ret,...)
{
    va_list Arguments;
    int i;
    int isnull;
    mocaDataRes *Res;
    mocaDataRow *Row;

    if (!Ret || !Ret->ReturnedData || !Ret->ReturnedData->LastRow)
	return eERROR;

    Res = Ret->ReturnedData;
    Row = Res->LastRow;

    /*
     * Go through the argument list.
     */
    va_start(Arguments, Ret);
    for (i=0; i<Res->NumOfColumns; i++)
    {
	isnull = va_arg(Arguments, int);

	/* Set the indicator to either 1 or 0. */
	Row->NullInd[i] = (short)(0 != isnull);
    }
    va_end(Arguments);

    return eOK;
}
