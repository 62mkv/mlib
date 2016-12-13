static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: 
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
#include <mocagendef.h>
#include <sqllib.h>

#define MAIN_DELIM_STR "^"
#define MAIN_DELIM_CHAR '^'

#define COLS_DELIM_STR "~"
#define COLS_DELIM_CHAR '~'

long sqlDecodeResults(char *buffer, mocaDataRes **Res)
{
    long Rows;
    long Cols;
    char *ptr, *delimiter_ptr, *eqpos;
    char tmpColumn[100];
    char tmpbuf[100];
    char dtype;
    long dsize;
    long TempLong;
    double TempDouble;
    long tmpActualLen, tmpDefinedLen;
    int i,r;
    long ret_status;
    char *dtype_ptr, *rdata_ptr, *nrows_ptr, *ncols_ptr, *cinfo_ptr, *emesg_ptr;
    mocaDataRes *tmpRes;
    mocaDataRow *Row;
    char *Message;

    dtype_ptr = rdata_ptr = nrows_ptr = ncols_ptr = cinfo_ptr = emesg_ptr = NULL;
    ptr = buffer;
    while (ptr && *ptr)
    {
	if ((eqpos = strchr(ptr, '=')))
	{
	    if (0 == strncmp(ptr, "RDATA=", 6))
	    {
		rdata_ptr = ptr+6;
		break;
	    }

	    if (0 == strncmp(ptr, "NROWS=", 6))
		nrows_ptr = ptr+6;
	    else if (0 == strncmp(ptr, "NCOLS=", 6))
		ncols_ptr = ptr+6;
	    else if (0 == strncmp(ptr, "DTYPE=", 6))
		dtype_ptr = ptr+6;
	    else if (0 == strncmp(ptr, "CINFO=", 6))
		cinfo_ptr = ptr+6;
	    else if (0 == strncmp(ptr, "EMESG=", 6))
		emesg_ptr = ptr+6;

	    ptr = strchr(ptr, MAIN_DELIM_CHAR);
	    if (ptr) ptr++;
	}
	else
	    break;
    }

    if (nrows_ptr)
	Rows = strtol(nrows_ptr, &delimiter_ptr, 10);
    else
	Rows = 0;

    if (ncols_ptr)
	Cols = strtol(ncols_ptr, &delimiter_ptr, 10);
    else
	Cols = 0;

    Message = NULL;

    if (emesg_ptr)
    {
	ptr = strchr(emesg_ptr, MAIN_DELIM_CHAR);
	if (ptr)
	{
	    Message = malloc(ptr - emesg_ptr + 1);
	    strncpy(Message, emesg_ptr, ptr - emesg_ptr);
	    Message[ptr - emesg_ptr] = '\0';
	    misHTTPURLDecode(Message, Message, ptr - emesg_ptr + 1);
	}

    }


    *Res = sql_AllocateResultHdr(Cols);

    (*Res)->Message = Message;

    ptr = cinfo_ptr;
    for (i=0;i<Cols;i++)
    {
	delimiter_ptr = strchr(ptr, COLS_DELIM_CHAR);
	if (!delimiter_ptr)
	    return eERROR;

	strncpy(tmpColumn, ptr, (delimiter_ptr - ptr));
	tmpColumn[delimiter_ptr - ptr] = '\0';
	ptr = delimiter_ptr + 1;

	tmpDefinedLen = strtol(ptr, &delimiter_ptr, 10);
	if (!delimiter_ptr || *delimiter_ptr != COLS_DELIM_CHAR)
	    return eERROR;
	ptr = delimiter_ptr + 1;

	tmpActualLen = strtol(ptr, &delimiter_ptr, 10);
	if (!delimiter_ptr || *delimiter_ptr != COLS_DELIM_CHAR)
	    return eERROR;
	ptr = delimiter_ptr + 1;

	sql_SetColName(*Res, i, tmpColumn, 
		       (char) ((~COMTYP_NULL_MASK) & dtype_ptr[i]),
		       tmpDefinedLen);
	(*Res)->Nullable[i] = ((dtype_ptr[i] & COMTYP_NULL_MASK) != 0);
	(*Res)->ActualMaxLen[i] = tmpActualLen;
    }

    /*
     * Next is the row data
     */
    ptr = rdata_ptr;
    for (r=0; r<Rows; r++)
    {
	Row = sql_AllocateRow(*Res);
	(*Res)->NumOfRows++;

	for (i=0; i<Cols; i++)
	{
	    dtype = *ptr++;
	    dsize = strtol(ptr, &delimiter_ptr, 10);
	    if (!delimiter_ptr || *delimiter_ptr != MAIN_DELIM_CHAR)
		return eERROR;

	    ptr = delimiter_ptr + 1;

	    switch (dtype)
	    {
	    case COMTYP_STRING:
	    case COMTYP_DATTIM:
		/* Place the value in the Res structure...  */
		ret_status = sql_AddRowItem(*Res, Row, i, dsize, ptr);

		if (ret_status != eOK)
		    return eERROR;
		break;

	    case COMTYP_BOOLEAN:
	    case COMTYP_INT:
		strncpy(tmpbuf, ptr, dsize);
		tmpbuf[dsize] = '\0';
		TempLong = atol(tmpbuf);

		ret_status = sql_AddRowItem(*Res, Row, i,
					    sizeof(long), &TempLong);
		if (ret_status != eOK)
		{
		    return eERROR;
		}
		break;
		
	    case COMTYP_FLOAT:
		strncpy(tmpbuf, ptr, dsize);
		tmpbuf[dsize] = '\0';
		TempDouble = atof(tmpbuf);
		
		ret_status = sql_AddRowItem(*Res, Row, i,
					    sizeof(double), &TempDouble);
		if (ret_status != eOK)
		{
		    return eERROR;
		}
		break;

	    case COMTYP_RESULTS:
		ret_status = sqlDecodeResults(ptr, &tmpRes);
		if (ret_status != eOK)
		{
		    sqlFreeResults(tmpRes);
		    return eERROR;
		}

		ret_status = sql_AddRowItem(*Res, Row, i,
					    sizeof(mocaDataRes *), &tmpRes);
		if (ret_status != eOK)
		{
		    sqlFreeResults(tmpRes);
		    return eERROR;
		}
		break;

	    case COMTYP_BINARY:
		ret_status = sql_AddRowItem(*Res, Row, i, dsize, ptr);

		if (ret_status != eOK)
		{
		    return eERROR;
		}
		break;
	    }

	    if (dsize == 0)
		Row->NullInd[i] = 1;
	    else
		Row->NullInd[i] = 0;

	    ptr += dsize;
	}
    }

    return eOK;
}
