static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Swap two columns within a return structure.
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

#include "srvprivate.h"

static void sSwapChars(char *srcChar, char *dstChar)
{
    char temp;

    /* Swap the characters. */
    temp     = *dstChar;
    *dstChar = *srcChar;
    *srcChar = temp;
}

long srvSwapColumns(RETURN_STRUCT *ret, long src, long dst)
{
    long status,
	 numColumns;

    mocaDataRes *res;

    /* Don't bother doing anything if both are the same. */
    if (src == dst)
        return eOK;

    /* Make sure we got a return structure. */
    if (! ret)
    {
	misLogError("Return structure is null");
        return eINVALID_ARGS;
    }

    /* Get the result set for this return structure. */
    res = srvGetResults(ret);
    if (! res)
    {
	misLogError("Could not get result set from return structure");
	return eERROR;
    }

    /* Get the number of columns in this result set. */
    numColumns = sqlGetNumColumns(res);

    /* Make sure our source and destination columns are in range. */
    if (src < 0 || src > (numColumns - 1))
    {
	misLogError("Source column index is out of range");
        return eINVALID_ARGS;
    }
    if (dst < 0 || dst > (numColumns - 1))
    {
	misLogError("Destination column index is out of range");
        return eINVALID_ARGS;
    }

    /* Swap the data types. */
    sSwapChars(&(ret->DataTypes[src]), &(ret->DataTypes[dst]));

    /* Swap the header??? */

    /* Swap the columns within this result set. */
    if ((status = sqlSwapColumns(res, src, dst)) != eOK)
    {
	misLogError("Could not swap columns in result set");
	return status;
    }
    
    return eOK;
}
