static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: This routine returns the long value of a named column.
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

long MOCAEXPORT sqlGetLong(mocaDataRes *res, mocaDataRow *row, char *name)
{
    long col;
   
    /* Get the column number. */
    col = sqlFindColumn(res, name);

    return sqlGetLongByPos(res, row, col);
}

long MOCAEXPORT sqlGetLongByPos(mocaDataRes *res, mocaDataRow *row, long col)
{
    long val = 0;

    /* Validate the column number. */
    if (col < 0 || col >= res->NumOfColumns)
        return val;
    
    /* Deal with hidden and null columns. */
    if (res->Hidden || row->NullInd[col])
        return val;

    /* Get the actual value. */
    if (res->DataType[col] == COMTYP_INT ||
	res->DataType[col] == COMTYP_LONG ||
	res->DataType[col] == COMTYP_BOOLEAN)
    {
	val = * (long *) row->DataPtr[col];
    }
    else if (res->DataType[col] == COMTYP_FLOAT)
    {
	val = (long) * (double *) row->DataPtr[col];
    }
    else
    {
        val = atol((char *) row->DataPtr[col]);
    }

    return val;
}
