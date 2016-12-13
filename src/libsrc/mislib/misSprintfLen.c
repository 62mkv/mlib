static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to determine what the length of an applied sprintf
 *               call would be.
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
#include <stdarg.h>
#include <string.h>
#include <errno.h>

#include <mocaerr.h>
#include <mislib.h>
#include <oslib.h>

long misSprintfLen(char *fmt, va_list args)
{
    long length;

    static FILE *outfile;

    /* Don't bother if we were passed a null pointer. */
    if (!fmt)
        return 0;
   
    /* Open the NULL device. */
    if (!outfile)
    {
        if ((outfile = fopen(OS_NULL_DEVICE, "w")) == NULL)
        {
	    misLogError("fopen: %s", strerror(errno));
	    misLogError("misSprintfLen: Could not open NULL device");
	    OS_PANIC;
        }
    }

    /* Get the length of the command. */
    if ((length = vfprintf(outfile, fmt, args)) < 0)
    {
	misLogError("vfprintf: %s", strerror(errno));
	misLogError("misSprintfLen: Could not get length");
	return -1;
    }

    return length;
}
