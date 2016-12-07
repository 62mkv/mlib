static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: This routine frees data associated with mocaDataRow.
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
#include <string.h>
#include <stdlib.h>

#include <common.h>
#include <sqllib.h>

void MOCAEXPORT sql_FreeRow(mocaDataRes *res, mocaDataRow *row)
{
    long ii;

    if (!res)
	return;

    for (ii = 0; ii < res->NumOfColumns; ii++)
    {
        if (row->DataPtr[ii])
	{
	    if (res->DataType && res->DataType[ii] == COMTYP_RESULTS)
	    {
	        sqlFreeResults(*(mocaDataRes **)row->DataPtr[ii]);
	    }
            else if (res->DataType && res->DataType[ii] == COMTYP_JAVAOBJ)
            {
                sql_FreeObjectRef(*(mocaObjectRef **)row->DataPtr[ii]);
            }
	    free(row->DataPtr[ii]);
	}
    }

    if (row->NullInd) free(row->NullInd);
    if (row->DataPtr) free(row->DataPtr);

    free(row);

    return;
}
