static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Add a column to a row.
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

#include <mocaerr.h>
#include <sqllib.h>

long sqlAddColumn(mocaDataRes *res, char *column, char datatype, long length)
{
    long status,
	 numColumns;

    mocaDataRow *row;

    /* Give ourselves a nicer alias to work with. */
    numColumns = res->NumOfColumns + 1;

    /*
     *  Add the column to the result set.
     */

    /* Increment the number of columns in the result set. */
    res->NumOfColumns = numColumns;

    /* Allocate space for the additional result set attributes. */
    res->DataType = 
	(char *) realloc(res->DataType, (numColumns + 1) * sizeof(char));
    res->ColName = 
	(char **) realloc(res->ColName, numColumns * sizeof(char *));
    res->ShortDescription = 
	(char **) realloc(res->ShortDescription, numColumns * sizeof(char *));
    res->LongDescription = 
	(char **) realloc(res->LongDescription, numColumns * sizeof(char *));
    res->Nullable = 
	(long *) realloc(res->Nullable, numColumns * sizeof(long));
    res->ActualMaxLen = 
	(long *) realloc(res->ActualMaxLen, numColumns * sizeof(long));
    res->DefinedMaxLen = 
	(long *) realloc(res->DefinedMaxLen, numColumns * sizeof(long));
    res->HashValue = 
	(unsigned long *) realloc(res->HashValue, numColumns * sizeof(unsigned long));

    /* Terminate the newly reallocated data types attribute. */
    res->DataType[numColumns] = '\0';

    /* Initialize the short and long column descriptions. */
    res->ShortDescription[numColumns - 1] = NULL;
    res->LongDescription[numColumns - 1] = NULL;

    /* Set the addtional result set attributes. */
    status = sql_SetColName(res, (numColumns - 1), column, datatype, length);
    if (status != eOK)
	return status;

    /*
     *  Add the column to each row in the result set.
     */

    /* Cycle through each row in the result set. */
    for (row = sqlGetRow(res); row; row = sqlGetNextRow(row))
    {
	/* Allocate space for the additional column data pointer. */
        row->DataPtr = 
	    (void **) realloc(row->DataPtr, numColumns * sizeof(void *));
        if (!row->DataPtr) 
            return eNO_MEMORY;

	/* Initialize the additional column data pointer. */
        row->DataPtr[numColumns-1] = NULL;

	/* Allocate space for the additional null indicator. */
        row->NullInd = 
	    (short *) realloc(row->NullInd, numColumns * sizeof(short));
        if (!row->NullInd) 
            return eNO_MEMORY;

	/* Initialize the additional null indicator. */
        row->NullInd[numColumns-1] = 1;
    }

    return eOK;
}
