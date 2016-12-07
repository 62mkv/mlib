static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions used by the server and server applications
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

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <sqllib.h>

static long GetResSize(mocaDataRes *Res);
static long EncodeRes(mocaDataRes *Res, char *buffer);

#define MAIN_DELIM_STR "^"
#define MAIN_DELIM_CHAR '^'

#define COLS_DELIM_STR "~"
#define COLS_DELIM_CHAR '~'

/*
 * These two functions are used to calculate the size of an integer or floating point
 * number. 
 */
static int intlen(long longval)
{  
    register int len;
    register long tmpval;
    len = (longval <= 0L) ? 1 : 0;

    for (tmpval = longval; tmpval; tmpval /= 10, len++)
        ;
    return len;
}

static int floatlen(double floatval)
{   
    char buf[100];
    return sprintf(buf, MOCA_FLT_FMT, floatval);
}

/*
 *  Construct a communications header to send back to the client.
 */
static long EncodeHdr(mocaDataRes *Res, char *buffer)
{
    long i;
    char *bufptr;

    if (!Res)
	return 0;

    bufptr = buffer;
    bufptr += sprintf(bufptr,
		      "NROWS=%ld" MAIN_DELIM_STR /* # Rows */
		      "NCOLS=%ld" MAIN_DELIM_STR, /* # Columns */
	              Res->NumOfRows,Res->NumOfColumns);

    bufptr += sprintf(bufptr, "DTYPE=");

    for (i = 0; i < Res->NumOfColumns; i++)
    {
	*bufptr++ = (char) (Res->Nullable[i] ?
			    (Res->DataType[i] | COMTYP_NULL_MASK) :
			    Res->DataType[i]);
    }

    *bufptr++ = MAIN_DELIM_CHAR;

    bufptr += sprintf(bufptr, "CINFO=");

    for (i = 0; i < Res->NumOfColumns; i++)
    {
	bufptr += sprintf(bufptr, "%s" COLS_DELIM_STR
		                  "%ld" COLS_DELIM_STR
		                  "%ld" COLS_DELIM_STR,
		          Res->ColName[i]?Res->ColName[i]:"",
			  Res->DefinedMaxLen[i],
			  Res->ActualMaxLen[i]);
    }

    *bufptr++ = MAIN_DELIM_CHAR;
    if (Res->Message)
    {
	long msgSize = strlen(Res->Message) * 3 + 1;
	char *tmpMessage = malloc(msgSize);
	misHTTPURLEncode(Res->Message, tmpMessage, msgSize);
	bufptr += sprintf(bufptr, "EMESG=%s" MAIN_DELIM_STR, tmpMessage);
	free(tmpMessage);
    }

    bufptr += sprintf(bufptr, "RDATA=");

    return (bufptr-buffer);
}

/*
 * Create the buffer needed to represent a single row.
 */
static long EncodeRow(mocaDataRes *Res, mocaDataRow *Row, char *buffer)
{
    long i;
    long Length;
    char *bufptr;

    if (Row == NULL)
    {
	return (0);
    }

    bufptr = buffer;

    for (i=0;i<Res->NumOfColumns;i++)
    {
	/* Data is in the form: Dll...l^DATA....
	   Where D    = DataType
	   l    = length (delimited by MAIN_DELIM_STR)
	   DATA = Ascii representation of data...
	   A null in both datatype and bytes to follow means end of data */

	*bufptr++ = Res->DataType[i];

	switch (Res->DataType[i])
	{

	case COMTYP_INT:
	case COMTYP_BOOLEAN:

	    if (Row->NullInd[i] == 0)
		bufptr += sprintf(bufptr, "%d" MAIN_DELIM_STR "%ld",
			          intlen(*(long *)Row->DataPtr[i]),
			          *(long *)Row->DataPtr[i]);
	    else
		bufptr += sprintf(bufptr, "0" MAIN_DELIM_STR);

	    break;

	case COMTYP_FLOAT:

	    if (Row->NullInd[i] == 0)
		bufptr += sprintf(bufptr, "%d" MAIN_DELIM_STR MOCA_FLT_FMT,
			          floatlen(*(double *)Row->DataPtr[i]),
			          *(double *)Row->DataPtr[i]);
	    else
		bufptr += sprintf(bufptr, "0" MAIN_DELIM_STR);

	    break;

	case COMTYP_STRING:
	case COMTYP_DATTIM:
	    /* If we don't have a null, but misTrimLen returns 0, then we
	       should send the string 'as is'.  Somehow, spaces are in our
	       database, and though they shouldn't be, we should simply
	       fire off what is in the field... */

	    Length = 0;
	    if (Row->NullInd[i] == 0)
	    {
		Length = (long) strlen((char *) Row->DataPtr[i]);
	        bufptr += sprintf(bufptr, "%ld" MAIN_DELIM_STR, Length);
                memcpy(bufptr, Row->DataPtr[i], Length);
                bufptr += Length;
	    }
	    else
	    {
		bufptr += sprintf(bufptr, "0" MAIN_DELIM_STR);
	    }

	    break;


	case COMTYP_GENERIC:
	    bufptr += sprintf(bufptr, "0" MAIN_DELIM_STR);
	    break;

	case COMTYP_RESULTS:
	    bufptr += EncodeRes(* (mocaDataRes **) Row->DataPtr[i], bufptr);
	    break;

	case COMTYP_BINARY:
	    if (Row->NullInd[i] == 0)
	    {
		Length = sqlEncodeBinaryLen(Row->DataPtr[i]);
		bufptr += sprintf(bufptr, "%ld" MAIN_DELIM_STR, Length);
		memcpy(bufptr, Row->DataPtr[i], Length);
		bufptr += Length;
	    }
	    else
	    {
		bufptr += sprintf(bufptr, "0" MAIN_DELIM_STR);
	    }
	    break;

	default:

	    if (Row->NullInd[i] == 0)
	    {
		Length = (long) Res->ActualMaxLen[i];
		bufptr += sprintf(bufptr, "%ld" MAIN_DELIM_STR, Length);
		memcpy(bufptr, Row->DataPtr[i], (long)Res->ActualMaxLen[i]);
		bufptr += Length;
	    }
	    else
	    {
		bufptr += sprintf(bufptr, "0" MAIN_DELIM_STR);
	    }
	}
    }

    return (bufptr - buffer);
}

long GetHdrSize(mocaDataRes *Res)
{
    long i;
    long delimLen;
    long idLen;
    long BytesInHeader;
    long ColNameLen;

    if (!Res)
	return 0;

    delimLen = 1; /* ~ or ^ */
    idLen = 6; /* NNNNN= */
    ColNameLen = 0;

    for (i = 0; i < Res->NumOfColumns; i++)
    {
	ColNameLen += (Res->ColName[i]?strlen(Res->ColName[i]):0) + delimLen +
	              intlen(Res->DefinedMaxLen[i]) + delimLen +
	              intlen(Res->ActualMaxLen[i]) + delimLen;
    }

    /* So...bytes up to the end of the header are equal to the addition
       of all the fields and their delimiters starting with the complstatus
       field... */
    BytesInHeader = idLen + intlen(Res->NumOfRows) + delimLen +
	            idLen + intlen(Res->NumOfColumns) + delimLen +
		    idLen + Res->NumOfColumns + delimLen +
		    idLen + ColNameLen + delimLen + 
		    idLen;

    if (Res->Message)
    {
	long msgSize = strlen(Res->Message) * 3 + 1;
	char *tmpMessage = malloc(msgSize);
	misHTTPURLEncode(Res->Message, tmpMessage, msgSize);
	BytesInHeader += idLen + strlen(tmpMessage) + delimLen;
	free(tmpMessage);
    }
    return BytesInHeader;
}

/*
 * Determine the size of buffer needed for a particular row.
 */
static long GetRowSize(mocaDataRes * Ifc, mocaDataRow * dptr)
{
    long i;
    char *NextType;
    long TotalSize;
    long Length;

    if (dptr == NULL)
	return (0);

    NextType = Ifc->DataType;
    i = 0;
    TotalSize = 0;

    while (NextType && NextType[i] != '\0')
    {

	/* All we are doing here is determining how large of a buffer
	   we will need to use to hold the entire string that we build
	   in ConstructBuffer...the first chunk, "DBB", stays constant
	   for each data type returned...we add that in up front...the
	   data size is added in the switch statement....   

	   We will add in space for the NULL datatype and NULL BytesToFollow
	   only when we send it, so that gets ignored for now...  */

	/* First add in size for DataType */
	TotalSize += (sizeof(char));

	/* Next add in size required for each datatype... */

	switch (NextType[i])
	{
	case COMTYP_INT:
	case COMTYP_BOOLEAN:

	    Length = (dptr->NullInd[i] == 0) ?
			 intlen(*(long *)dptr->DataPtr[i]) :
			 0;

	    TotalSize += intlen(Length) + 1 + Length;

	    break;

	case COMTYP_FLOAT:

	    Length = (dptr->NullInd[i] == 0) ?
		         floatlen(*(double *)dptr->DataPtr[i]) :
			 0;

	    TotalSize += intlen(Length) + 1 + Length;
	    break;

	case COMTYP_STRING:
	case COMTYP_DATTIM:
	    Length = 0;
	    if (dptr->NullInd[i] == 0)
	    {
		Length = (long) strlen((char *) dptr->DataPtr[i]);
	    }

	    TotalSize += intlen(Length) + 1 + Length;
	    break;

	case COMTYP_GENERIC:
	    TotalSize += 2; /* "0" + MAIN_DELIM_STR */
	    break;

	case COMTYP_BINARY:
	    if (dptr->NullInd[i] == 0)
		Length = sqlEncodeBinaryLen(dptr->DataPtr[i]);
	    else
		Length = 0;

	    TotalSize += intlen(Length) + 1 + Length;
	    break;

	case COMTYP_RESULTS:
	    Length = GetResSize(* (mocaDataRes * *) dptr->DataPtr[i]);
	    TotalSize += intlen(Length) + 1 + Length;
	    break;
	}

	i++;
    }
    return (TotalSize);
}

static long GetResSize(mocaDataRes *Res)
{
    int size;
    mocaDataRow *Row;

    size = GetHdrSize(Res);

    for (Row=sqlGetRow(Res); Row; Row = sqlGetNextRow(Row))
	size += GetRowSize(Res, Row);

    return size;
}

static long EncodeRes(mocaDataRes *Res, char *buffer)
{
    mocaDataRow *Row;
    char *bufptr;

    if (!Res)
	return 0;

    bufptr = buffer;

    bufptr += EncodeHdr(Res, bufptr);
    for (Row=sqlGetRow(Res); Row; Row = sqlGetNextRow(Row))
    {
	bufptr += EncodeRow(Res, Row, bufptr);
    }

    return (bufptr - buffer);
}

long sqlEncodeResultsLen(mocaDataRes *Res)
{
    return GetResSize(Res);
}

long sqlEncodeResults(mocaDataRes *Res, char **ret_buffer)
{
    long res_size;

    res_size = GetResSize(Res);
    *ret_buffer = malloc(res_size+1);
    res_size = EncodeRes(Res, *ret_buffer);
    (*ret_buffer)[res_size] = '\0';

    return res_size;
}
