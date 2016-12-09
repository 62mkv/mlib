/*#START***********************************************************************
 *
 *  $URL$
 *  $Author$
 *
 *  Description: 
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

#include "socksrvmgr.h"

typedef struct
{
    int             threadno;
    HANDLE         *hEvent;
} SERV_THREAD_ARGS;

DWORD WINAPI serv_tmain(LPVOID args)
{
    int             threadno;
    int             status;
    HANDLE          hThreadEvent;
    SERV           *nextserv;

    threadno = ((SERV_THREAD_ARGS *) args)->threadno;
    hThreadEvent = ((SERV_THREAD_ARGS *) args)->hEvent;

    /*
     * It was malloced just for us. We have to free it.
     */
    free(args);

    /*
     * Our main loop.  Do this forever.
     */
    for (;;)
    {
	HANDLE          handle_list[MAXIMUM_WAIT_OBJECTS];
	DWORD           handle_count;
	SERV           *serv;

	handle_count = 0;

	/* Listen for a wake-up call */
	handle_list[handle_count++] = hThreadEvent;

	EnterCriticalSection(&listCrit);

	/* Listen for Servers to signal or Die */
	for (serv = ServBusyFirst; serv; serv = serv->next)
	{
	    if (serv->threadno == threadno)
	    {
		handle_list[handle_count++] = serv->hEvent;
		handle_list[handle_count++] = serv->hProcess;
	    }
	}

	for (serv = ServFreeTop; serv; serv = serv->next)
	{
	    if (serv->threadno == threadno)
	    {
		handle_list[handle_count++] = serv->hProcess;
	    }
	}

	LeaveCriticalSection(&listCrit);

	/*
	 * We'll come out of this when at least one event is tripped.
	 */
	status = WaitForMultipleObjects(handle_count, handle_list, FALSE, INFINITE);

	if (status == WAIT_FAILED)
	{
	    misLogError("serv_tmain: WaitForMultipleObjects: %s", osError());
	}
	else
	{
	    HANDLE          hEvent;

	    /*
	     * This is how we determine which of our list of events caused
	     * us to get out of WaitForMultipleObjects().
	     */
	    hEvent = handle_list[status - WAIT_OBJECT_0];

	    if (hEvent == hThreadEvent)
	    {
		misTrc(T_MGR, "Woke up because of server activity");
		ResetEvent(hThreadEvent);
	    }
	    else
	    {
		EnterCriticalSection(&listCrit);

		/*
		 * Loop through our list of servers, looking for activity.
		 */
		for (serv = ServBusyFirst; serv; serv = nextserv)
		{
		    nextserv = serv->next;
		    if (hEvent == serv->hProcess)
		    {
			/* The Server Process Terminated */
			misTrc(T_MGR, "We lost a connection to a server (PID: %d)", serv->dwPid);

			ResetEvent(serv->hEvent);
			CloseHandle(serv->hPipe);
			CloseHandle(serv->hEvent);
			CloseHandle(serv->hProcess);
			CloseHandle(serv->hThread);
			CloseHandle(serv->hServerEvent);
			if (serv->connection)
			{
			    osSockShutdown(serv->connection->fd);
			    osSockClose(serv->connection->fd);
			    MoveConnIdle(serv->connection);
			    DelConnIdle(serv->connection);
			}
			DelServBusy(serv);

                        if (ConnPendingFirst)
                        {
                            SetEvent(pending_event);
                        }
		    }

		    else if (hEvent == serv->hEvent)
		    {
			misTrc(T_MGR, "Handling server activity... (PID: %d)",
			       serv->dwPid);
			ResetEvent(serv->hEvent);
			/*
			 * When the server flips the event, it's done with the 
			 * socket descriptor we sent to it. All we have to do is
			 * is put the socket back into our list. (We never closed it)
			 */

			if (serv->connection)
			{
			    misTrc(T_MGR, "Received FD %d from server %d",
				   serv->connection->fd, serv->dwPid);
			    if (0 != WSAEventSelect(serv->connection->fd,
						     socket_event,
						     FD_READ | FD_CLOSE))
			    {
				misLogError("WSAEventSelect:(%d) %s",
					    WSAGetLastError(),
					    osSockError());
                                MoveConnIdle(serv->connection);
				osSockShutdown(serv->connection->fd);
				osSockClose(serv->connection->fd);
				DelConnIdle(serv->connection);
			    }
			    else
			    {
                                misTrc(T_MGR, "Set socket to non-blocking (FD: %d)", serv->connection->fd);
                                time(&serv->connection->info.idle);
                                MoveConnIdle(serv->connection);
                            }
                            serv->connection = NULL;
			}
			else
			{
			    misTrc(T_MGR, "Received NULL FD from server %d",
				   serv->dwPid);
			}

			MoveServFree(serv);

                        if (param.max_commands > 0 && 
			    serv->info.count >= param.max_commands)
                        {
                            misTrc(T_MGR,
                                   "Request limit reached... (PID: %d)",
                                   serv->dwPid);
                            serv->closing = 1;
                        }
                        else if (ServFreeCount > param.min_servers)
                        {
                            misTrc(T_MGR,
                                   "Idle server limit reached... (PID: %d)",
                                   serv->dwPid);
                            serv->closing = 1;
                        }

                        if (serv->closing)
                        {
                            misTrc(T_MGR,
                                   "Shutting down a previously busy server... (PID: %d)",
                                   serv->dwPid);
                            shutdown_server(serv);
                        }

                        if (ConnPendingFirst)
                        {
                            SetEvent(pending_event);
                        }
                        
		    }
		}

		/*
		 * Next check to see if an idle process has died.
		 */
		for (serv = ServFreeTop; serv; serv = nextserv)
		{
		    nextserv = serv->next;
		    if (hEvent == serv->hProcess)
		    {
			/* The Server Process Terminated */
			misTrc(T_MGR, "We lost a connection to a server (PID: %d)", 
			       serv->dwPid);

			CloseHandle(serv->hPipe);
			CloseHandle(serv->hEvent);
			CloseHandle(serv->hProcess);
			CloseHandle(serv->hThread);
			CloseHandle(serv->hServerEvent);
			DelServFree(serv);
		    }
		}

                /*
                 * Normalize Server Process Count.  This might mean spawning
                 * new processes.
                 */
                if ((ServBusyCount + ServFreeCount) < param.min_servers)
                {
                    int need = param.min_servers - (ServBusyCount + ServFreeCount);
        
                    misTrc(T_MGR, "Resizing server pool -- forking %d servers", need);
       
                    fork_servers(need);
                }

		LeaveCriticalSection(&listCrit);
	    }
	}
    }

    /* We should never get here */
    return 0;
}

/*
 * This is the entry point for the thread.
 */
HANDLE serv_thread(int threadno, HANDLE hEvent)
{
    SERV_THREAD_ARGS *args;
    DWORD           threadid;
    HANDLE          hThread;

    /*
     * Malloc an args structure.  The thread's main will free it.
     */
    args = malloc(sizeof(SERV_THREAD_ARGS));

    args->threadno = threadno;
    args->hEvent = hEvent;
    hThread = CreateThread(NULL, 0, serv_tmain, args, 0, &threadid);
    SetHandleInformation(hThread, HANDLE_FLAG_INHERIT, 0);
    SetHandleInformation(hEvent, HANDLE_FLAG_INHERIT, 0);
    return hThread;
}
