static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Swap two columns within a result set.
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
#include <sqllib.h>

static void sSwapPointers(void **srcPointer, void **dstPointer)
{
    void *temp;

    /* Swap the pointers. */
    temp        = *dstPointer;
    *dstPointer = *srcPointer;
    *srcPointer = temp;
}

static void sSwapStrings(char **srcString, char **dstString)
{
    char *temp;

    /* Swap the strings. */
    temp       = *dstString;
    *dstString = *srcString;
    *srcString = temp;
}

static void sSwapChars(char *srcChar, char *dstChar)
{
    char temp;

    /* Swap the characters. */
    temp     = *dstChar;
    *dstChar = *srcChar;
    *srcChar = temp;
}

static void sSwapShorts(short *srcShort, short *dstShort)
{
    short temp;

    /* Swap the shorts. */
    temp      = *dstShort;
    *dstShort = *srcShort;
    *srcShort = temp;
}

static void sSwapLongs(long *srcLong, long *dstLong)
{
    long temp;

    /* Swap the longs. */
    temp     = *dstLong;
    *dstLong = *srcLong;
    *srcLong = temp;
}

static void sSwapUlongs(unsigned long *srcUlong, unsigned long *dstUlong)
{
    unsigned long temp;

    /* Swap the unsigned longs. */
    temp      = *dstUlong;
    *dstUlong = *srcUlong;
    *srcUlong = temp;
}

long sqlSwapColumns(mocaDataRes *res, long src, long dst)
{
    long numColumns;

    mocaDataRow *row;

    /* Don't bother doing anything if both are the same. */
    if (src == dst)
        return eOK;

    /* Make sure we got a result set. */
    if (! res)
    {
	misLogError("Result set is null");
	return eINVALID_ARGS;
    }

    /* Get the number of columns in this result set. */
    numColumns = sqlGetNumColumns(res);

    /* Make sure our source and destination columns are in range. */
    if (src < 0 || src > (numColumns - 1))
    {
	misLogError("Source column index is out of range");
	return eINVALID_ARGS;
    }
    if (dst < 0 || dst > (numColumns - 1))
    {
	misLogError("Destination column index is out of range");
	return eINVALID_ARGS;
    }

    /*
     * MOCA DATA RESULT SET
     */

    /* Swap the column names. */
    sSwapStrings(&(res->ColName[src]), &(res->ColName[dst]));

    /* Swap the short descriptions, which can have a null pointer. */
    if (res->ShortDescription && res->ShortDescription[src])
    {
        sSwapStrings(&(res->ShortDescription[src]), 
		     &(res->ShortDescription[dst]));
    }

    /* Swap the long descriptions, which can have a null pointer. */
    if (res->LongDescription && res->LongDescription[src])
    {
        sSwapStrings(&(res->LongDescription[src]), 
		     &(res->LongDescription[dst]));
    }

    /* Swap the data types. */
    sSwapChars(&(res->DataType[src]), &(res->DataType[dst]));

    /* Swap the actual max lengths. */
    sSwapLongs(&(res->ActualMaxLen[src]), &(res->ActualMaxLen[dst]));

    /* Swap the defined max lengths. */
    sSwapLongs(&(res->DefinedMaxLen[src]), &(res->DefinedMaxLen[dst]));

    /* Swap the nullable flags. */
    sSwapLongs(&(res->Nullable[src]), &(res->Nullable[dst]));

    /* Swap the hash values. */
    sSwapUlongs(&(res->HashValue[src]), &(res->HashValue[dst]));

    /*
     * MOCA DATA ROW
     */

    /* Cycle through each row in the result set. */
    for (row = sqlGetRow(res); row; row = sqlGetNextRow(row))
    {
        /* Swap the data pointers for each row. */
        sSwapPointers(&(row->DataPtr[src]), &(row->DataPtr[dst]));

        /* Swap the null indicators for each row. */
        sSwapShorts(&(row->NullInd[src]), &(row->NullInd[dst]));
    }

    return eOK;
}
