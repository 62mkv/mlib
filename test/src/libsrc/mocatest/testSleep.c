static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL: https://athena.redprairie.com/svn/prod/moca/trunk/src/libsrc/mislib/mi
sGetSleepPassword.c $
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to deal with passwords that we encode/decode.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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
 *#END************************************************************************/

#include <moca.h>

#ifdef WIN32
#include <windows.h>
#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <oslib.h>
#include <srvlib.h>

LIBEXPORT 
RETURN_STRUCT *testSleep(long *duration)
{
    RETURN_STRUCT *results = NULL;

#ifdef WIN32    
    Sleep(*duration);
#else
    sleep(*duration);
#endif
    results = srvResults(eOK, "duration", COMTYP_INT, sizeof(*duration), *duration, NULL); 

    return results;
}
