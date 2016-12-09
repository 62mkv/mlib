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

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <mocaerr.h>
#include <mislib.h>
#include "srvprivate.h"

RETURN_STRUCT *srvAddSQLResults(mocaDataRes *res, long status)
{
    int ii;

    RETURN_STRUCT *ret;

    ret = (RETURN_STRUCT *) calloc(1, sizeof(RETURN_STRUCT));

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

    if (res)
    {
        ret->rows = res->NumOfRows;

        ret->DataTypes = (char *) calloc(1, res->NumOfColumns + 1);

        for (ii = 0; ii < res->NumOfColumns; ii++)
            ret->DataTypes[ii] = (char) res->DataType[ii];

        ret->ReturnedData = res;
    }
    else
    {
        ret->rows = 0;
    }

    return ret;
}
