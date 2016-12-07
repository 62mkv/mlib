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

long srvCombineResults(RETURN_STRUCT **ret1, RETURN_STRUCT **ret2)
{
    long           status;
    mocaDataRes   *rs1, 
		  *rs2;
    RETURN_STRUCT *rt1, 
                  *rt2;

    /* Make sure we have something to work with. */
    if (!ret1 || !ret2)
	return(eINVALID_ARGS);

    /* Just return if the second return struct is empty. */
    if (!*ret2)
	return(eOK);

    /* If the first return struct is empty, we can just point to the second. */
    if (!*ret1)
    {
	*ret1 = *ret2;
	*ret2 = NULL;
	return(eOK);
    }

    /* Give ourselves nicer aliases to work with. */
    rt1 = *ret1;
    rt2 = *ret2;
    rs1 = rt1->ReturnedData;
    rs2 = rt2->ReturnedData;

    /* Make sure the data types match. */
    if (!sqlCompareTypes(rt1->DataTypes, rt2->DataTypes))
	return(eINVALID_ARGS);

    /* Combine the result sets. */
    if ((status = sqlCombineResults(&rs1, &rs2)) != eOK)
        return(status);

    /* Set the number of rows in the combined return struct. */
    rt1->rows = rs1->NumOfRows;

    /* Set the second return struct's returned data pointer to null. */
    rt2->ReturnedData = NULL;

    /* Free the second return struct. */
    srvFreeMemory(SRVRET_STRUCT, rt2);
    *ret2 = NULL;

    return(eOK);
}
