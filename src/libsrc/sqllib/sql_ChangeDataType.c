static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Change the data type of a column.
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

#include <mocaerr.h>
#include <sqllib.h>


/*
 *  FUNCTION: sql_ChangeDataType
 *
 *  PURPOSE:  Change the data type of a column.
 *
 *  RETURNS:  eOK - All ok.
 *            Some error code.
 */

long sql_ChangeDataType(mocaDataRes *res, char *name, char dtype)
{
    long number;

    /* Get the column number of this column. */
    if ((number = sqlFindColumn(res, name)) < 0)
    {
	misLogError("sql_ChangeDataType: Could not get column number");
	return eERROR;
    }

    /* Change the data type in the return struct. */
    res->DataType[number] = dtype;

    return eOK;
}

/*
 *  FUNCTION: sql_ChangeDataTypeByPos
 *
 *  PURPOSE:  Change the data type of a column.
 *
 *  RETURNS:  eOK - All ok.
 *            Some error code.
 */

long sql_ChangeDataTypeByPos(mocaDataRes *res, long number, char dtype)
{
    long numColumns;

    /* Get the number of columns in this result set. */
    numColumns = sqlGetNumColumns(res);

    /* Validate the given column number. */
    if (number < 0 || number >= numColumns)
    {
	misLogError("sql_ChangeDataTypeByPos: Column number out of range");
	return eINVALID_ARGS;
    }

    /* Change the data type in the result set. */
    res->DataType[number] = dtype;

    return eOK;
}

