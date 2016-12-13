static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: This routine removes a row from a result set.
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

#include <sqllib.h>

void MOCAEXPORT sqlRemoveRow(mocaDataRes *res, mocaDataRow *remove)
{
    mocaDataRow *row, 
		*prev;

    /* Make sure we have a result set to search. */
    if (! res)
	return;

    /* Make sure we have a row to search for. */
    if (! remove)
	return;

    /* Initialize the previous row pointer. */
    prev = NULL;

    /* Cycle through each row in the result set. */
    for (row = sqlGetRow(res); row; row = sqlGetNextRow(row))
    {
	if (remove == row)
	{
	    /* 
	     * Fix the row pointers to remove this row.
	     * The first and last rows must be dealt with differently.
	     */
	    if (remove == res->Data)
		res->Data = remove->NextRow;
	    else
	        prev->NextRow = row->NextRow;

	    if (remove == res->LastRow)
		res->LastRow = prev;

	    /* Free up the row to remove. */
	    sql_FreeRow(res, remove);

	    /* Decrement the number of rows in the result set. */
	    res->NumOfRows--;

	    break;
	}

	/* Keep a pointer to this row for the next iteration. */
	prev = row;
    }
   
    return;
}
