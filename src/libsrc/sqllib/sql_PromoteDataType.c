static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to support the promotion of communication types.
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
#include <mocagendef.h>
#include <sqllib.h>


/*
 *  FUNCTION: sql_CompareDataTypes
 *
 *  PURPOSE:  Compare two data types and return the "greatest" one.
 *
 *  RETURNS:   1 - The first data type is "greater".
 *             0 - The data types are the same.
 *            -1 - The second data type is "greater".
 */

long sql_CompareDataTypes(char dtype1, char dtype2)
{
    long ii;

    static char dtype[] = 
    {
        COMTYP_BOOLEAN,
        COMTYP_INT,
        COMTYP_LONG,
        COMTYP_FLOAT,
        COMTYP_DATTIM,
        COMTYP_TEXT,
        COMTYP_STRING,
        0
    };

    /* We don't need to do anything special if they're just the same. */
    if (dtype1 == dtype2)
	return 0;

    /* The first one we find in the dtype list is the lesser of the two. */
    for (ii=0; dtype[ii]; ii++)
    {
	if (dtype[ii] == dtype1)
	    return -1;
        if (dtype[ii] == dtype2)
	    return 1;
    }

    /* We should never get here. */
    misLogError("sql_CompareDataTypes: Reached unexpected code");

    return -999;
}


/*
 *  FUNCTION: sql_PromoteToInt
 *
 *  PURPOSE:  Promote the given column to an integer.
 *
 *  NOTE(S):  COMTYP_BOOLEAN and COMTYP_INT are both stored internally
 *            as longs, so all we have to do is change the data type.
 *
 *  RETURNS:  eOK - All ok.
 *            Some error code.
 */

static long sql_PromoteToInt(mocaDataRes *res, long number)
{
    long status;

    /* Change the data type in the result set. */
    status = sql_ChangeDataTypeByPos(res, number, COMTYP_INT);
    if (status != eOK)
    {
	misLogError("sql_PromoteToInt: Could not change data type");
	return status;
    }

    return eOK;
}


/*
 *  FUNCTION: sql_PromoteToLong
 *
 *  PURPOSE:  Promote the given column to a long.
 *
 *  NOTE(S):  COMTYP_INT and COMTYP_LONG are both stored internally
 *            as longs, so all we have to do is change the data type.
 *
 *  RETURNS:  eOK - All ok.
 *            Some error code.
 */

static long sql_PromoteToLong(mocaDataRes *res, long number)
{
    long status;

    /* Change the data type in the result set. */
    status = sql_ChangeDataTypeByPos(res, number, COMTYP_LONG);
    if (status != eOK)
    {
	misLogError("sql_PromoteToLong: Could not change data type");
	return status;
    }

    return eOK;
}


/*
 *  FUNCTION: sql_PromoteToFloat
 *
 *  PURPOSE:  Promote the given column to a float.
 *
 *  RETURNS:  eOK - All ok.
 *            Some error code.
 */

static long sql_PromoteToFloat(mocaDataRes *res, long number)
{
    long status;
    double value;
    void *temp;
    mocaDataRow *row;

    /* Change the data type in the result set. */
    status = sql_ChangeDataTypeByPos(res, number, COMTYP_FLOAT);
    if (status != eOK)
    {
	misLogError("sql_PromoteToFloat: Could not change data type");
	return status;
    }

    /* Cycle through each row in the result set. */
    for (row=sqlGetRow(res); row; row=sqlGetNextRow(row))
    {
	/* Get a pointer to the value of this column. */
	temp = sqlGetValueByPos(res, row, number);

	/* Get the value of this column as a float. */
	value = temp ? (double) * (long *) temp : 0;

	/* Set the value of the column as a float. */
	status = sql_AddRowItem(res, row, number, sizeof(double), &value);
        if (status != eOK)
        {
	    misLogError("sql_PromoteToFloat: Could not set value");
	    return status;
        }
    }

    return eOK;
}


/*
 *  FUNCTION: sql_PromoteToStringFromFloat
 *
 *  PURPOSE:  Promote the given column to a string.
 *
 *  RETURNS:  eOK - All ok.
 *            Some error code.
 */

static long sql_PromoteToStringFromFloat(mocaDataRes *res, long number)
{
    long status;
    double value;
    char str[1024];
    void *temp;
    mocaDataRow *row;

    /* Change the data type in the result set. */
    status = sql_ChangeDataTypeByPos(res, number, COMTYP_STRING);
    if (status != eOK)
    {
	misLogError("sql_PromoteToStringFromFloat: Could not change data type");
	return status;
    }

    /* Cycle through each row in the result set. */
    for (row=sqlGetRow(res); row; row=sqlGetNextRow(row))
    {
	/* Get a pointer to the value of this column. */
	temp = sqlGetValueByPos(res, row, number);

	/* Get the value of this column as a float. */
	value = temp ? * (double *) temp : 0;

	/* Convert the value to a string. */
	sprintf(str, MOCA_FLT_FMT, value);

	/* Set the value of the column as a float. */
	status = sql_AddRowItem(res, row, number, strlen(str), str);
        if (status != eOK)
        {
	    misLogError("sql_PromoteToFloat: Could not set value");
	    return status;
        }
    }

    return eOK;
}


/*
 *  FUNCTION: sql_PromoteToStringFromDate
 *
 *  PURPOSE:  Promote the given column to a string.
 *
 *  RETURNS:  eOK - All ok.
 *            Some error code.
 */

static long sql_PromoteToStringFromDate(mocaDataRes *res, long number)
{
    long status;

    /* Change the data type in the result set. */
    status = sql_ChangeDataTypeByPos(res, number, COMTYP_STRING);
    if (status != eOK)
    {
	misLogError("sql_PromoteToStringFromDate: Could not change data type");
	return status;
    }

    return eOK;
}


/*
 *  FUNCTION: sql_PromoteToStringFromText
 *
 *  PURPOSE:  Promote the given column to a string.
 *
 *  RETURNS:  eOK - All ok.
 *            Some error code.
 */

static long sql_PromoteToStringFromText(mocaDataRes *res, long number)
{
    long status;

    /* Change the data type in the result set. */
    status = sql_ChangeDataTypeByPos(res, number, COMTYP_STRING);
    if (status != eOK)
    {
	misLogError("sql_PromoteToStringFromText: Could not change data type");
	return status;
    }

    return eOK;
}


/*
 *  FUNCTION: sql_PromoteDataType
 *
 *  PURPOSE:  Promote a column's data type.
 *
 *  NOTE(S):  It would be much faster to promote the data type directly to
 *            the target data type.  There would be more code, though, and
 *            we'll usually only be promoting one level anyway.
 *
 *  RETURNS:  eOK - All ok.
 *            Some error code.
 */

long sql_PromoteDataType(mocaDataRes *res, long number, char toDataType)
{
    long numColumns,
	 currDataType;

    /* Get the number of columns in this result set. */
    numColumns = sqlGetNumColumns(res);

    /* Validate the given column number. */
    if (number < 0 || number >= numColumns)
    {
	misLogError("sql_PromoteDataType: Column number out of range");
	return eINVALID_ARGS;
    }

    /* Promote the data type until we get to our target data type. */
    for (;;)
    {
	/* Get the current data type. */
	currDataType = sqlGetDataTypeByPos(res, number);
	if (currDataType == toDataType)
	    break;

	/* Promote it one level. */
        switch (currDataType)
	{
	    case COMTYP_BOOLEAN:
		sql_PromoteToInt(res, number);
		break;
	    case COMTYP_INT:
		sql_PromoteToLong(res, number);
		break;
	    case COMTYP_LONG:
		sql_PromoteToFloat(res, number);
		break;
	    case COMTYP_FLOAT:
		sql_PromoteToStringFromFloat(res, number);
		break;
	    case COMTYP_DATTIM:
		sql_PromoteToStringFromDate(res, number);
		break;
	    case COMTYP_TEXT:
		sql_PromoteToStringFromText(res, number);
		break;
	    case COMTYP_STRING:
	        misLogError("sql_PromoteDataType: Reached unexpected code");
	        return eERROR;
	}
    }

    return eOK;
}
