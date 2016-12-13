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
#include <errno.h>

#include <mislib.h>
#include <oslib.h>


/*
 *  FUNCTION: osPanic
 *
 *  PURPOSE:  Abort execution of the current process due to a fatal
 *            error, dumping an error message to the log file.
 *
 *  NOTE(S):  This is actually called via the osPanic macro is oslib.h.
 *
 *  RETURNS:  void
 */

void osPanic(char *filename, long line)
{
    char *dummy = NULL;

#ifdef WIN32
    *dummy = 'x';
#else
    abort( );
#endif
}
