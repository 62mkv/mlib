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

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <oslib.h>

#include "socksrvmgr.h"

static int server_count;
static int serv_thread_count;
static int called_atexit;

struct
{
    HANDLE          hThread;
    HANDLE          hEvent;
} *ServThreadList;

static void free_thread_list(void)
{
    free(ServThreadList);

    return;
}

/*
 * Fork n servers and add them to our linked list.
 */
BOOL fork_servers(int num_servers)
{
    SERV            tmp;
    HANDLE          read_end;
    PROCESS_INFORMATION procinfo;
    SECURITY_ATTRIBUTES security;
    STARTUPINFO     NewInfo;
    char            command_line[512];
    char            pipe_str[32];
    char            event_str[32];
    char            server_event_str[32];
    int             i, j;
    char            *envstr;
    char            idlist[1024];
    SERV           *serv;

    memset(idlist, 0, sizeof idlist);

    for (serv = ServFreeTop; serv; serv = serv->next)
        idlist[serv->id] = 1;
    for (serv = ServBusyFirst; serv; serv = serv->next)
        idlist[serv->id] = 1;

    /*
     * Set up our Security Attributes. This structure is used for our
     * pipes and our Event Handle.
     */
    security.nLength = sizeof security;
    security.lpSecurityDescriptor = NULL;
    security.bInheritHandle = TRUE;

    for (i = 0; i < num_servers; i++)
    {
	int ii, jj;
        int id = -1;

        for (ii=0; ii < param.max_servers; ii++)
        {
            if (idlist[ii] == 0)
            {
               idlist[ii] = 1;
               id = ii;
               break;
            }
        }

	/*
	 * Increment the server count.
	 */
	server_count++;

	/*
	 * Initialize the Server structure
	 */
	memset(&tmp, 0, sizeof tmp);

	/*
	 * Do we need to create a new thread?
	 */
	if (server_count > serv_thread_count * 30)
	{
	    /*
	     * We don't know how many threads we'll have, so we need a dynamic-
	     * sized array.
	     */
	    if (ServThreadList)
		ServThreadList = realloc(ServThreadList,
			(sizeof *ServThreadList) * (serv_thread_count + 1));
	    else
		ServThreadList = malloc((sizeof *ServThreadList) * (serv_thread_count + 1));

	    if (! called_atexit)
	    {
		called_atexit++;
		osAtexit(free_thread_list);
	    }

	    /*
	     * Create the signalling event for us to let the thread know there's
	     * something going on.
	     */
	    ServThreadList[serv_thread_count].hEvent =
		CreateEvent(NULL, FALSE, FALSE, NULL);

	    /*
	     * Create the thread.
	     */
	    ServThreadList[serv_thread_count].hThread =
		serv_thread(serv_thread_count, ServThreadList[serv_thread_count].hEvent);

	    serv_thread_count++;

	}

	if (!CreatePipe(&read_end, &tmp.hPipe, &security, 0))
	{
	    misLogError("CreatePipe: %s", osError());
	    return FALSE;
	}

	/*
	 * Create an event.  This will signal to us that there's something
	 * to be looked at on our pipe.
	 */
	tmp.hEvent = CreateEvent(&security, TRUE, FALSE, NULL);
	if (!tmp.hEvent)
	{
	    misLogError("CreateEvent: %s", osError());
	    return FALSE;
	}

	tmp.hServerEvent = CreateEvent(&security, TRUE, FALSE, NULL);
	if (!tmp.hServerEvent)
	{
	    misLogError("CreateEvent: %s", osError());
	    return FALSE;
	}

	/*
	 * Set the inherit flag on my end of the pipes to FALSE.  This keeps
	 * the child process from having extra handles open.
	 */
	if (!SetHandleInformation(tmp.hPipe, HANDLE_FLAG_INHERIT, 0L))
	{
	    misLogError("SetHandleInformation: %s", osError());
	    return FALSE;
	}

	/*
	 * Process startup information.  This is used by CreateProcess to
	 * tell what sort of process we want to create.  We're creating a
	 * console child process that inherits most of our handles.
	 */
	memset(&NewInfo, 0, sizeof NewInfo);
	NewInfo.cb = sizeof NewInfo;
	NewInfo.lpReserved = NULL;
	NewInfo.lpDesktop = NULL;
	NewInfo.lpTitle = NULL;
	NewInfo.dwFlags = STARTF_USESTDHANDLES;
	NewInfo.cbReserved2 = 0;
	NewInfo.lpReserved2 = NULL;
	NewInfo.hStdOutput = GetStdHandle(STD_OUTPUT_HANDLE);
	NewInfo.hStdInput = GetStdHandle(STD_INPUT_HANDLE);
	NewInfo.hStdError = GetStdHandle(STD_ERROR_HANDLE);

	/*
	 * The command-line is the only way we can communicate to our
	 * child process at this point.
	 */
	osEncodeDescriptor(read_end, pipe_str);
	osEncodeDescriptor(tmp.hEvent, event_str);
	osEncodeDescriptor(tmp.hServerEvent, server_event_str);
	sprintf(command_line, "\"%s\"%s%s -P%s -p%d -e%s -E%s -i%d",
          SERVER_PROGRAM,
     (param.console_section && *param.console_section)?" -S":"",
     (param.console_section && *param.console_section)?param.console_section:"",
          pipe_str, param.port, event_str, server_event_str, id);
	
	if (param.trace_level[0])
	{
	    strcat(command_line, " -t");
	    strcat(command_line, param.trace_level);
	}

	for (j = 0; j < param.nopts; j++)
	{
	    strcat(command_line, " ");
	    strcat(command_line, param.opts[j]);
	}

        /*
	 * Get the environment strings for this server process.
	 */
	envstr = get_environment_strings(SERVER_PROGRAM);

	if (!CreateProcess(NULL, command_line, NULL, NULL, TRUE, 0L,
			   envstr, NULL, &NewInfo, &procinfo))
	{
	    misLogError("CreateProcess: %s", osError());
            free(envstr);
	    return FALSE;
	}

        free(envstr);

	/*
	 * Close the extraneous handles to the pipes. We don't care about
	 * those.
	 */
	CloseHandle(read_end);

	tmp.hProcess     = procinfo.hProcess;
	tmp.hThread      = procinfo.hThread;
	tmp.hThreadEvent = ServThreadList[serv_thread_count - 1].hEvent;
	tmp.threadno     = serv_thread_count - 1;
	tmp.dwPid        = procinfo.dwProcessId;

	/* Don't allow new server processes to inherit these handles. */
	SetHandleInformation(tmp.hEvent,       HANDLE_FLAG_INHERIT, 0L);
	SetHandleInformation(tmp.hServerEvent, HANDLE_FLAG_INHERIT, 0L);
	SetHandleInformation(tmp.hProcess,     HANDLE_FLAG_INHERIT, 0L);
	SetHandleInformation(tmp.hThread,      HANDLE_FLAG_INHERIT, 0L);
	SetHandleInformation(tmp.hThreadEvent, HANDLE_FLAG_INHERIT, 0L);

	time(&tmp.info.created);
	tmp.info.busy = 0;

	tmp.closing = 0;
	tmp.shutdown = 0;

	misTrc(T_MGR, "Created server process (PID: %d)", 
	       procinfo.dwProcessId);

	/* Add it to the list */
	AddServFree(&tmp);

        /*
         * It doesn't matter when we set this event, since this function
         * can only be called from within a critical section.
         */
        SetEvent(tmp.hThreadEvent);
    }

    return TRUE;
}

/*
 * Find a server and send a socket handle to it.
 */
int send_server(CONN * conn)
{
    SERV           *tmp;
    long           send_cmd;

    /* 
     * Look for an eligible server. If we can't find one, make one.
     */
    if (!ServFreeTop)
    {
	/*
	 * If no server could be found, and we're over the limit of server
	 * processes, put this connection into a list of "pending"
	 * connections.
	 */
	if (ServBusyCount >= param.max_servers)
	{
	    misTrc(T_MGR, "Over our limit - putting on the 'pending' queue");
	    return SEND_WAIT;
	}

	fork_servers(1);	/* Fork a new server and try again */

	/* If that failed, we're in trouble */
	if (!ServFreeTop)
	{
	    misTrc(T_MGR, "Error forking a new server process");
	    return SEND_ERROR;
	}
    }

    tmp = ServFreeTop;

    misTrc(T_MGR, "Sending connection to server... (PID: %lu)", tmp->dwPid);

    /*
     * If we noticed CLOSE activity on the socket, send the connection to the
     * child, but don't keep it in our list.
     */
    if (conn->closing)
    {
	tmp->connection = NULL;
	send_cmd = OS_SF_CLOSETCP;
    }
    else
    {
	tmp->connection = conn;
	send_cmd = OS_SF_SENDFILE;
    }

    /*
     * Now our process is busy. Move it to the busy list.
     */
    MoveServBusy(tmp);
    SetEvent(tmp->hThreadEvent);
    time(&tmp->info.busy);
    tmp->info.count++;

    /*
     * Send the socket to a child process.
     */

    if (eOK != osSendFile(tmp->hPipe, send_cmd, conn->fd, &conn->addr,
	                  tmp->dwPid, tmp->hServerEvent))
    {
	misLogError("osSendFile: %s", osSockError());
        tmp->connection = NULL;
        misTrc(T_MGR, "Error duplicating socket.  Closing connection to server %d", tmp->dwPid);
        MoveServFree(tmp);
        shutdown_server(tmp);

	return SEND_ERROR;
    }

    misTrc(T_MGR, "Sent connection to server (PID: %lu)", tmp->dwPid);

    return SEND_OK;
}

int shutdown_server(SERV * serv)
{
    MoveServBusy(serv);
    SetEvent(serv->hThreadEvent);
    serv->shutdown = 1;

    if (osSendFile(serv->hPipe, OS_SF_SHUTDOWN, (SOCKET) 0, NULL, 0,
	           serv->hServerEvent) < 0)
    {
	misLogError("osSendFile: %s");
	return eERROR;
    }

    return eOK;
}

int shutdown_idle(void)
{
    while (ServFreeTop)
    {
	if (eOK != shutdown_server(ServFreeTop))
	    break;
    }

    return eOK;
}

int shutdown_busy(void)
{
    SERV *p;

    for (p = ServBusyFirst; p; p = p->next)
    {
	p->closing = 1;
    }

    return eOK;
}
