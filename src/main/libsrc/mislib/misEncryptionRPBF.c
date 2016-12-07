static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to support encryption/decryption of given data.
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

void misEncryptRPBF(char *string, long inlength, long *outlength)
{
    long ii;

    unsigned char bitmask,
		  *ptr;

    for (ii = 0, ptr = (unsigned char *) string; ii < inlength; ii++, ptr++)
    {
	bitmask = (unsigned char) (ii % 255) + 1;

        if (bitmask & *ptr)
            *ptr ^= bitmask;
        else
            *ptr |= bitmask;
    }

    *outlength = inlength;

    return;
}

void misDecryptRPBF(char *string, long inlength, long *outlength)
{
    long ii;

    unsigned char bitmask,
		  *ptr;

    for (ii = 0, ptr = (unsigned char *) string; ii < inlength; ii++, ptr++)
    {
	bitmask = (unsigned char) (ii % 255) + 1;
        if (bitmask & *ptr)
            *ptr ^= bitmask;
        else
            *ptr |= bitmask;
    }
    
    *outlength = inlength;

    return;
}
