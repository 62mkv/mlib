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
#include <sqllib.h>

/* 
 * Take result set 2 and add it to result set 1 provided they
 * contain the same metadata.  Result set 2 is then cleaned up
 * and memory is free'd where needed.  
 */

long MOCAEXPORT sqlCombineResults(mocaDataRes **res1, mocaDataRes **res2)
{
    long ii;
    mocaDataRes *rs1, 
		*rs2;
    mocaDataRow *row1;

    /* Make sure we have something to work with. */
    if (!res1 || !res2)
	return(eINVALID_ARGS);

    /* Just return if the second result set is empty. */
    if (!*res2)
	return(eOK);

    /* If the first result set is empty, we can just point to the second. */
    if (!*res1)
    {
	*res1 = *res2;
	*res2 = NULL;
	return(eOK);
    }

    /* Give ourselves nicer pointers to work with. */
    rs1 = *res1;
    rs2 = *res2;

    /* Make sure the number of columns match. */
    if (rs1->NumOfColumns != rs2->NumOfColumns)
	return(eINVALID_ARGS);

    /* Make sure the data types match. */
    if (!sqlCompareTypes(rs1->DataType, rs2->DataType))
    {
	return(eINVALID_ARGS);
    }

    /* Make sure the column names match. */
    for (ii = 0; ii < rs1->NumOfColumns; ii++)
    {
	if (strcmp(rs1->ColName[ii], rs2->ColName[ii]) != 0)
	    return(eINVALID_ARGS);
    }

    /* Just return if there aren't any rows in the second result set. */
    if (sqlGetRow(rs2) == NULL)
	return (eOK);

    /* Tweak the actual and defined max. lengths if necessary. */
    for (ii = 0; ii < rs1->NumOfColumns; ii++)
    {
	if (rs1->ActualMaxLen[ii] < rs2->ActualMaxLen[ii])
	    rs1->ActualMaxLen[ii] = rs2->ActualMaxLen[ii];
	if (rs1->DefinedMaxLen[ii] < rs2->DefinedMaxLen[ii])
	    rs1->DefinedMaxLen[ii] = rs2->DefinedMaxLen[ii];
    }

    /* Increment the number of rows. */
    rs1->NumOfRows += rs2->NumOfRows;

    /* Get a pointer to the last row of the first result set. */
    row1 = sqlGetLastRow(rs1);

    /* 
     * At this point, row1 either points to nothing or points to 
     * the current last row.
     */

    /* Add the rows to the first result set. */
    if (!row1)
    {
	/* We don't have a last row... let's add our whole structure. */
	rs1->Data = sqlGetRow(rs2);
	sql_SetLastRow(rs1, sqlGetLastRow(rs2));
    }
    else
    {
	row1->NextRow = sqlGetRow(rs2);
	sql_SetLastRow(rs1, sqlGetLastRow(rs2));
    }

    /* 
     * Set the data attribute to NULL.  The first result set is now
     * pointing to the second result set's data, so we have to set it
     * to null so that it doesn't get free'd when we free the rest of 
     * the second result set.
     */
    rs2->Data = NULL;

    /* Free the second result set. */
    sqlFreeResults(rs2);
    *res2 = NULL;

    return(eOK);
}
