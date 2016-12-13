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

#include <stdlib.h>
#include <stddef.h>

#include <mocagendef.h>

#include "osprivate.h"

#ifdef WIN32

static HANDLE hShutdown, hParent;

static DWORD WINAPI tmain(LPVOID args)
{
    int eventCount = 0;
    HANDLE hEvents[2];
    DWORD dwStatus;
    MSG msg;

    /* Added each handle that is actually set into our event list. */
    if (hParent)   hEvents[eventCount++] = hParent;
    if (hShutdown) hEvents[eventCount++] = hShutdown;

    /*
     * Handle the message loop.  This is needed because of concerns over
     * using COM single threaded apartments.  We NEED to handle
     * all windows messages, on our thread, if we want to play nicely
     * with COM STAs.
     */
    while(1)
    {
	dwStatus = MsgWaitForMultipleObjects(eventCount, 
			                     hEvents, 
					     FALSE, 
					     INFINITE,
		                             QS_ALLINPUT);

	if (dwStatus == WAIT_OBJECT_0 || 
	    dwStatus == WAIT_OBJECT_0 + eventCount - 1)
	{
            misTrc(T_SERVER, "Exiting from osInit because parent died...");
	    exit(0);
	}
	else if (dwStatus == WAIT_OBJECT_0 + eventCount)
	{
            misTrc(T_SERVER, "Dispatching a message...");
	    while(PeekMessage(&msg, NULL, 0, 0, PM_REMOVE))
	    {
		TranslateMessage(&msg);
		DispatchMessage(&msg);
	    }
	}
	else if (dwStatus == WAIT_FAILED)
	{
	    return 0;
	}
    }
}
#endif

void osInit(void)
{
#ifdef WIN32   
    HANDLE hThread;
    DWORD threadid;
    char *var;
    static int init_done = 0;

    /* Set the initial thread. */
    os_SetInitialThread( );

    if (!init_done)
    {
	hParent   = (HANDLE) 0;
	hShutdown = (HANDLE) 0;

        /* Get the parent processes' handle. */
        var = osGetVar(ENV_PARENT_PID);
        if (var)
        {
            hParent = OpenProcess(SYNCHRONIZE, FALSE, atol(var));
	    if (!hParent)
	        misTrc(T_FLOW, "OpenProcess: %s", osError( ));
        }

	/* Get the shutdown event handle. */
	var = osGetVar(ENV_SHUTDOWN_EVENT);
        if (var)
        {
	    osDecodeDescriptor(var, &hShutdown);
        }

	if (hParent || hShutdown)
	{
	    hThread = CreateThread(NULL, 0, tmain, NULL, 0, &threadid);
	}

	init_done = 1;
    }

#else

    /* Set the initial thread. */
    os_SetInitialThread( );

#endif
}
