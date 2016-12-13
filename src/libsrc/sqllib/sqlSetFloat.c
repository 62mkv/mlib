static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: This routine sets the float value of a named column.
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
#include <sqllib.h>

long MOCAEXPORT sqlSetFloat(mocaDataRes *res, mocaDataRow *row, char *name,
                            double value)
{
    long colNumber;
    
    /* Get the column number of the given column. */
    if ((colNumber = sqlFindColumn(res, name)) == -1)
	return(eINVALID_ARGS);

    /* We don't support changing the datatype, just the value. */
    if (sqlGetDataTypeByPos(res, colNumber) != COMTYP_FLOAT)
        return(eINVALID_OPERATION);

    /* Change the value of the column. */
    return(sql_AddRowItem(res, row, colNumber, sizeof(double), 
	                  (void *) &value));
}
