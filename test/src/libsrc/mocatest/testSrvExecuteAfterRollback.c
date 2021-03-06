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
 *#END************************************************************************/

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <oslib.h>
#include <srvlib.h>

static int callCount = 0;

void incrementCall(void *dummy)
{
    callCount++;
}

LIBEXPORT 
int *testSrvExecuteAfterRollback()
{
    srvExecuteAfterRollback(incrementCall, NULL); 

    /* We also want to set keepalive so we keep the native process */
    srvRequestKeepalive("test", NULL);

    return 0;
}

LIBEXPORT
RETURN_STRUCT *testGetCount()
{
    return srvResults(eOK, "count", COMTYP_INT, sizeof(int), callCount, NULL);
}
