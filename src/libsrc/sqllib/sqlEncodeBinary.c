static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to deal with encoded binary data.
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

#include <mocagendef.h>
#include <mocaerr.h>
#include <sqllib.h>

#define HEXSTR_LEN 8

/*
 * MOCA encodes the raw (actual) binary data as a byte stream with the first 
 * 8 characters being the length of the raw (actual) binary data as a hex 
 * string followed by the raw (actual) binary data itself.
 *
 *     --------------------------------------------
 *     | Length | Raw (actual) binary data  . . . |
 *     --------------------------------------------
 *
 *      8 bytes   X bytes
 */

/*
 *  FUNCTION: sqlEncodeBinary
 *
 *  PURPOSE:  Encode the given raw (actual) binary data and length.
 *
 *  RETURNS:  A pointer to the encoded binary data.
 */

void *sqlEncodeBinary(void *raw, long length)
{
    char  hexstr[HEXSTR_LEN+1],
	 *temp,
	 *encoded;

    /* Convert the length to a hex string. */
    sprintf(hexstr, "%-*.*X", HEXSTR_LEN, HEXSTR_LEN, length);

    /* Allocate space for the encoded binary data. */
    if ((encoded = (void *) calloc(length + HEXSTR_LEN, 1)) == NULL)
	return NULL;

    /* Get a pointer to the start of the raw binary data. */
    temp = (void *) ((unsigned long) encoded + HEXSTR_LEN);

    /* Build the encoded binary data. */
    strncpy(encoded, hexstr, HEXSTR_LEN);
    if (length)
    {
        memcpy(temp, raw, length);
    }

    return encoded;
}

/*
 *  FUNCTION: sqlEncodeBinaryLen
 *
 *  PURPOSE:  Get the length of the encoded binary data stream.
 *
 *  RETURNS:  Length of the encoded binary data.
 */

long sqlEncodeBinaryLen(void *encoded)
{
    long length;

    /* Get the length of the raw (actual) binary data. */
    if ((length = sqlDecodeBinaryLen(encoded)) < 0)
        return -1;

    /* Add the length of the hex string. */
    length += HEXSTR_LEN;

    return length;
}
