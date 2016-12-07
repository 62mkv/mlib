static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to determine the string lengths of a value.
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
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <sqllib.h>

long sqlLengthInt(long value)
{
    long temp,
	 length;

    /* We may need to add one for the minus sign. */
    length = (value <= 0L) ? 1 : 0;

    /* Pick off one digit at a time. */
    for (temp = value; temp; temp /= 10, length++)
        ;

    return length;
}

long sqlLengthLong(long value)
{
    return sqlLengthInt(value);
}

long sqlLengthBoolean(long value)
{
    return sqlLengthInt(value);
}

long sqlLengthChar(char *value)
{
    return strlen(value);
}

long sqlLengthString(char *value)
{
    return sqlLengthChar(value);
}

long sqlLengthDattim(char *value)
{
    return sqlLengthChar(value);
}

long sqlLengthFloat(double value)
{
    char temp[1000];

    return sprintf(temp, MOCA_FLT_FMT, value);
}

long sqlLengthBinary(void *value)
{
    return sqlDecodeBinaryLen(value);
}
