static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Add a row to a result set.
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

#include <mocaerr.h>
#include <oslib.h>
#include <sqllib.h>

mocaDataRow *sqlAddRow(mocaDataRes *res)
{
    long ii;
    mocaDataRow *row,
                *temp;

    /* Allocate the new row. */
    row = (mocaDataRow *) calloc(1, sizeof(mocaDataRow));
    if (! row)
    {
        misLogError("calloc: %s", osError( ));
        misLogError("sqlAddRow: Could not allocate new row");
        return NULL;
    }

    /*
     * It's OK to allocate a zero-column row.  We can add columns to it
     * on the fly.
     */
    if (res->NumOfColumns == 0)
    {
        row->DataPtr = NULL;
        row->NullInd = NULL;
    }
    else
    {
        /* Allocate the data pointer. */
        row->DataPtr = (void **) calloc(res->NumOfColumns, sizeof(void *));
        if (!row->DataPtr)
        {
            misLogError("calloc: %s", osError( ));
            misLogError("sqlAddRow: Could not allocate new data pointer");
            free(row);
            return NULL;
        }

        /* Allocate the null indicator. */
        row->NullInd = (short *) calloc(res->NumOfColumns, sizeof(short));
        if (!row->NullInd) 
        {
            misLogError("calloc: %s", osError( ));
            misLogError("sqlAddRow: Could not allocate new null indicator");
            free(row->DataPtr);
            free(row);
            return NULL;
        }

        /* Initialize the null indicator. */
        for (ii=0; ii<res->NumOfColumns; ii++)
            row->NullInd[ii] = 1;
    }

    /* Connect the row to the result set. */
    if (res->Data)
    {
        temp = res->LastRow;
        temp->NextRow = row;
    }
    else
    {
        res->Data = row;
    }
    res->LastRow = row;

    /* Increment the number of rows in the result set. */
    res->NumOfRows++;

    return row;
}
