static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Get the length of the binary data from a named column.
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

#include <sqllib.h>

long MOCAEXPORT sqlGetBinaryDataLen(mocaDataRes *res, 
                                    mocaDataRow *row, 
				    char *name)
{
    long col;

    /* Get the column number. */
    col = sqlFindColumn(res, name);

    return sqlGetBinaryDataLenByPos(res, row, col);
}

long MOCAEXPORT sqlGetBinaryDataLenByPos(mocaDataRes *res, 
                                         mocaDataRow *row, 
					 long col)
{
    long val = -1;

    /* Validate the column number. */
    if (col < 0 || col >= res->NumOfColumns)
        return val;

    /* Deal with hidden columns. */
    if (res->Hidden)
        return val;

    /* Get the actual value. */
    if (res->DataType[col] == COMTYP_BINARY)
        val = sqlDecodeBinaryLen(row->DataPtr[col]);

    return val;
}
