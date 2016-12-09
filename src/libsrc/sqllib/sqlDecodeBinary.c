static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to deal with decoded binary data.
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
 *  FUNCTION: sqlDecodeBinary
 *
 *  PURPOSE:  Get the pointer to the raw (actual) binary data given
 *            a pointer to the encoded binary data.
 *
 *  RETURNS:  Pointer to the raw (actual) binary data.
 */

void *sqlDecodeBinary(void *encoded)
{
    return (void *) ((unsigned long) encoded + HEXSTR_LEN);
}

/*
 *  FUNCTION: sqlDecodeBinaryLen
 *
 *  PURPOSE:  Get the length of the raw (actual) binary data given
 *            a pointer to the encoded binary data.
 *
 *  RETURNS:  Pointer to the raw (actual) binary data.
 */

long sqlDecodeBinaryLen(void *encoded)
{
    long  length;
    char  hexLength[HEXSTR_LEN+1],
         *unconverted = NULL;

    /* Build the hex string from the encoded binary data. */
    strncpy(hexLength, encoded, HEXSTR_LEN);
    hexLength[HEXSTR_LEN] = '\0';

    /* Get the length of the raw (actual) binary data. */
    length = strtol(hexLength, &unconverted, 16);

    /* Make sure the hex string was converted properly. */
    if (unconverted && *unconverted)
	return -1;
    
    return length;
}
