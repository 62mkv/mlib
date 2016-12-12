static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL: https://athena.redprairie.com/svn/prod/moca/trunk/src/libsrc/mislib/mi
sGetSleepPassword.c $
 *  $Revision$
 *  $Author$
 *
 *  Description: 
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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
#include <madlib.h>

LIBEXPORT 
RETURN_STRUCT *testMadTaskId(long *duration)
{
    RETURN_STRUCT *results = NULL;

    char *taskId = osGetVar("MOCA_TASK_ID");

    madIntegerSet("1","1","1","1", 1);

    madIntegerSet("com.redprairie.moca", "Tasks", "cfiles-processed",
                         taskId, rand() % (1 << 16 - 1));


    madStringSet("com.redprairie.moca", "Tasks", "name", taskId,
            taskId);

    madStringSet("com.redprairie.moca", "TasksS", "name", NULL, taskId);
    results = srvResults(eOK, "value", COMTYP_INT, sizeof(int), 1, NULL); 

    return results;
}
