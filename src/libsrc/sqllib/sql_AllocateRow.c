static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Allocate a mocaDataRow structure (w/o memory for data itself)
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

#include <sqllib.h>

/*
 *  sqlAllocateRow - this function is used to allocate the memory structure
 *                   for a mocaDataRow structure.  When complete,
 *                   the row structure has been allocated and 
 *                   the array for all DataPtr's has been allocated
 *                   (however, no space for the actual data is allocated.)
 *
 */
mocaDataRow *sql_AllocateRow(mocaDataRes *res)
{
    long ii;
    mocaDataRow *row, *rptr;

    row = (mocaDataRow *) calloc(1, sizeof(mocaDataRow));
    if (!row)
        return(NULL);

    row->DataPtr = (void **) calloc(res->NumOfColumns, sizeof(void *));
    if (!row->DataPtr && res->NumOfColumns) 
    {
        free(row);
        return(NULL);
    }

    row->NullInd = (short *) calloc(res->NumOfColumns, sizeof(short));
    if (!row->NullInd && res->NumOfColumns) 
    {
        free(row->DataPtr);
        free(row);
        return(NULL);
    }

    for (ii=0; ii<res->NumOfColumns; ii++)
        row->NullInd[ii] = 1;

    if (res->Data) 
    {
	rptr = res->LastRow;
        rptr->NextRow = row;
    }
    else 
    {
        res->Data = row;
    }
    res->LastRow = row;

    return(row);
}
