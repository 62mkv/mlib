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
#include <stdlib.h>

#include <mocagendef.h>
#include <sqllib.h>

moca_bool_t MOCAEXPORT sqlGetBoolean(mocaDataRes *res, 
                                     mocaDataRow *row, 
				     char *name)
{
    long col;

    /* Get the column number. */    
    col = sqlFindColumn(res, name);

    return sqlGetBooleanByPos(res, row, col);
}

moca_bool_t MOCAEXPORT sqlGetBooleanByPos(mocaDataRes *res, 
                                          mocaDataRow *row, 
					  long col)
{
    moca_bool_t val = MOCA_FALSE; 

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
	val = (moca_bool_t) * (long *) row->DataPtr[col];
    }
    else if (res->DataType[col] == COMTYP_FLOAT)
    {
	val = (moca_bool_t) * (double *) row->DataPtr[col];
    }
    else
    {
	val = (moca_bool_t) atol((char *) row->DataPtr[col]);
    }

    /* Cooerce any value to a true value. */
    if (val)
	val = MOCA_TRUE;

    return val;
}
