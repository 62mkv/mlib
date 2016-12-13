/*#START***********************************************************************
 *
 *  $URL$
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
#include <stdlib.h>
#include <signal.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>

#include "socksrvmgr.h"

static PHANDLER_ROUTINE cleanup_on_kill(DWORD signo)
{
    /* We only care about someone doing a CTRL-C. */
    if (signo != CTRL_C_EVENT)
        return FALSE;

    /*
     * Calling functions like misTrc from a signal handler is not
     * always a good idea, but we're exiting, so very little bad could
     * happen at this point
     */
    misTrc(T_MGR, "Caught signal - cleaning up...");

    /*
     * The kill_all_tasks routine has been installed as an atexit handler, so
     * we should be in good shape just calling exit.
     */
    exit(EXIT_SUCCESS);
}

void InitializeSignalHandling(void)
{
    SetConsoleCtrlHandler((PHANDLER_ROUTINE) cleanup_on_kill, TRUE);

    return;
}
