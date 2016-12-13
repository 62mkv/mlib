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

#define MAIN
#define FD_SETSIZE 2048

#include "socksrvmgr.h"

static BOOL activity(CONN * conn, long event_type);

int main(int argc, char *argv[])
{
    SOCKET          listen_fd, console_fd;
    WSAEVENT        listen_event, console_event;
    DWORD           status;
    CONN           *conn, *nextconn;
    int             nfds;
    char           *shutdown_var;
    char pid[16];
    HANDLE          shutdown_event;
    HANDLE          handle_list[MAXIMUM_WAIT_OBJECTS];
    DWORD           handle_count;

    memset(&param, '\0', sizeof(param));

    printf(misGetStartBanner(APPNAME));

    /* Initialize runtime parameters. */
    InitializeParameters(argc, argv);

    /* Initialize signal handling. */
    InitializeSignalHandling( );

    /* Initialize the socket layer. */
    osSockInit();

    /* Initialize the critical section variable we use to protect our lists. */
    InitializeCriticalSection(&listCrit);

    /*
     * Check for a shutdown event in the environment.
     * This is a sign that we're running as a service.
     */
    shutdown_event = (HANDLE)0;
    shutdown_var = osGetVar(ENV_SHUTDOWN_EVENT);

    if (shutdown_var)
    {
	misTrc(T_MGR, "Running as a service - waiting on shutdown event...");
	osDecodeDescriptor(shutdown_var, &shutdown_event);
    }

    /* 
     * Set the parent process id environment variable so that the 
     * socksrvprc processes can use it and wait on it.  This will allow
     * them to shutdown when this process gets slammed down by a
     * TerminateProcess( ) call.
     */
    sprintf(pid, "%ld", GetCurrentProcessId( ));
    SetEnvironmentVariable(ENV_PARENT_PID, pid);

    EnterCriticalSection(&listCrit);

    /*
     * Then pre-fork the servers.  Ideally, we should do this before we set up
     * our listen socket.  The param.min_servers CAN be zero, meaning we
     * fork as necessary.
     */
    fork_servers(param.min_servers);

    /*
     * Set up our listen sockets, one for the main port, and one for the
     * console.
     */
    if (eOK != osTCPListen(&listen_fd, param.port, OS_MAX_BACKLOG, TRUE) ||
	eOK != osTCPListen(&console_fd, param.console_port, OS_MAX_BACKLOG, TRUE))
    {
	misLogError("osTCPListen:%s", osSockError());
	exit(EXIT_FAILURE);
    }

    /*
     * We're using WSAEventSelect, so we need an event to associate with our
     * sockets.
     */
    socket_event = WSACreateEvent();
    if (socket_event == WSA_INVALID_EVENT)
    {
	misLogError("WSACreateEvent:%s", osSockError());
	exit(EXIT_FAILURE);
    }

    listen_event = WSACreateEvent();
    if (listen_event == WSA_INVALID_EVENT)
    {
	misLogError("WSACreateEvent:%s", osSockError());
	exit(EXIT_FAILURE);
    }

    console_event = WSACreateEvent();
    if (console_event == WSA_INVALID_EVENT)
    {
	misLogError("WSACreateEvent:%s", osSockError());
	exit(EXIT_FAILURE);
    }

    pending_event = CreateEvent(NULL, FALSE, FALSE, NULL);

    SetHandleInformation((HANDLE)listen_event, HANDLE_FLAG_INHERIT, 0);
    SetHandleInformation((HANDLE)socket_event, HANDLE_FLAG_INHERIT, 0);
    SetHandleInformation((HANDLE)console_event, HANDLE_FLAG_INHERIT, 0);
    SetHandleInformation(pending_event, HANDLE_FLAG_INHERIT, 0);

    /*
     * The event associated with a socket is inherited by accept()ed sockets,
     * so we only have to do this once.
     */
    WSAEventSelect(listen_fd, listen_event, FD_ACCEPT);
    WSAEventSelect(console_fd, console_event, FD_ACCEPT);

    handle_count = 0;
    handle_list[handle_count++] = socket_event;
    handle_list[handle_count++] = listen_event;
    handle_list[handle_count++] = console_event;
    handle_list[handle_count++] = pending_event;
    if (shutdown_var)
	handle_list[handle_count++] = shutdown_event;

    LeaveCriticalSection(&listCrit);

    /*
     * Our main loop.  Do this forever.
     */
    for (;;)
    {
	/*
	 * We'll come out of this when at least one event is tripped.
	 */
	status = WaitForMultipleObjects(handle_count, handle_list, FALSE, INFINITE);

	if (status == WAIT_FAILED)
	{
	    misLogError("mocaconmgr: WaitForMultipleObjects: %s", osError());
	}
	else
	{
	    HANDLE          hEvent;

	    /*
	     * This is how we determine which of our list of events caused
	     * us to get out of WaitForMultipleObjects().
	     */
	    hEvent = handle_list[status - WAIT_OBJECT_0];

	    if (hEvent == pending_event)
	    {
		EnterCriticalSection(&listCrit);
		ResetEvent(pending_event);
		while (ConnPendingFirst && (ServFreeCount > 0 || ServBusyCount < param.max_servers))
		{
		    conn = ConnPendingFirst;
		    DetachConnPending(conn);
		    MoveConnIdle(conn);
		    activity(conn, FD_READ);
		}
		LeaveCriticalSection(&listCrit);
	    }
	    else if (hEvent == console_event)
	    {
		SOCKET          new_console;

		WSAResetEvent(console_event);

		/*
		 * Because we got word of the connecting process, accept()
		 * won't block.
		 */
		if (eOK == osSockAccept(console_fd, &new_console, 0))
		{
		    if (!osFilterTCP(new_console))
		    {
			misTrc(T_MGR, "Rejected console connection request (FD: %d)", new_console);
			osSockShutdown(new_console);
			osSockClose(new_console);
		    }
		    else
		    {
			misTrc(T_MGR, "Accepted console connection request (FD: %d)", new_console);

			osTCPKeepalive(new_console, 1);

			/*
			 * Start off a new thread with our console connection.
			 */
			console_thread(new_console);
		    }
		}
	    }
	    else if (hEvent == listen_event)
	    {
		SOCKET          newfd;

		misTrc(T_MGR, "Received a new client connection request");

		EnterCriticalSection(&listCrit);

		WSAResetEvent(listen_event);

		/*
		 * Because we got word of the connecting process, accept()
		 * won't block.
		 */
		if (eOK != osSockAccept(listen_fd, &newfd, 0))
		{
		    /* What happened? */
		    misLogError("accept:%s", osSockError());
		}
		else
		{
		    CONN            tmp;

		    if (!osFilterTCP(newfd))
		    {
			misTrc(T_MGR, "Rejected client connection request (FD: %d)", newfd);
			osSockShutdown(newfd);
			osSockClose(newfd);
		    }
		    else
		    {
			misTrc(T_MGR, "Accepted client connection request (FD: %d)", newfd);

			osTCPKeepalive(newfd, 1);

			/*
			 * We were successful. Add us to the list of active
			 * connections
			 */
			memset(&tmp, 0, sizeof tmp);

			if (0 != WSAEventSelect(newfd, socket_event,
						FD_READ | FD_CLOSE))
			{
			    misLogError("WSAEventSelect:(%d) %s", WSAGetLastError(),
					osSockError());
			}

			tmp.fd = newfd;
			tmp.server = NULL;
			tmp.closing = FALSE;
			osSockAddress(newfd, tmp.addr.ip, sizeof tmp.addr.ip,
				      &tmp.addr.port);
			time(&tmp.info.connected);
			tmp.info.idle = tmp.info.connected;
			tmp.info.busy = 0;
			tmp.info.commands = 0;

			AddConnIdle(&tmp);
		    }
		}

		LeaveCriticalSection(&listCrit);
	    }
	    else if (hEvent == socket_event)
	    {
		fd_set          readfds;
		struct timeval  timeout;

		FD_ZERO(&readfds);

		EnterCriticalSection(&listCrit);

		for (conn = ConnIdleTop; conn; conn = conn->next)
		{
		    FD_SET(conn->fd, &readfds);
		}

		timeout.tv_sec = 0;
		timeout.tv_usec = 0;

		nfds = select(0, &readfds, NULL, NULL, &timeout);

		misTrc(T_MGR, "There's activity on %d FD's", nfds);

		switch (nfds)
		{
		case -1:
		    misLogError("select:%s", osSockError());
		    break;

		case 0:
		    /*
		     * Timeout...this is a normal occurrence, or at least
		     * seems to be.  Apparently, there are some timing conditions
		     * that cause the socket_event event to be tripped after the
		     * socket has been sent to a server.
		     */
		    misTrc(T_MGR, "select(): Timeout");
		    break;

		default:
		    /*
		     * Loop through our list of connections.
		     */
		    for (conn = ConnIdleTop; conn; conn = nextconn)
		    {
			nextconn = conn->next;
			if (FD_ISSET(conn->fd, &readfds))
			{
			    WSANETWORKEVENTS NetEvent;

			    /*
			     * Figure out what just happened.
			     */
			    WSAEnumNetworkEvents(conn->fd, socket_event, &NetEvent);
			    activity(conn, NetEvent.lNetworkEvents);
			}
		    }
		}

		LeaveCriticalSection(&listCrit);
		WSAResetEvent(socket_event);
	    }
	    else if (shutdown_var && hEvent == shutdown_event)
	    {
		misTrc(T_MGR, "Received shutdown request via service");
		break;
	    }
	}
    }

    /* We should never get here */
    return 0;
}

static BOOL activity(CONN * conn, long event_type)
{
    long send_status;
    unsigned long nonblock = 0L;

    misTrc(T_MGR, "Handling client activity... (FD: %d)", conn->fd);

    /* Is it read activity or close activity? */
    if (event_type & FD_CLOSE)
    {
	misTrc(T_MGR, "Close activity on FD %d", conn->fd);
	conn->closing = TRUE;
    }
    else
    {
	misTrc(T_MGR, "Read activity on FD %d", conn->fd);
    }

    if (!conn->closing)
    {
	/* Set socket to blocking */
	if (0 != WSAEventSelect(conn->fd, (WSAEVENT) NULL, 0))
	{
	    misLogError("WSAEventSelect:(%d) %s", WSAGetLastError(),
			osSockError());
	}

	if (0 != ioctlsocket(conn->fd, FIONBIO, &nonblock))
	{
	    misLogError("ioctlsocket(FIONBIO): (%d) %s", WSAGetLastError(), 
			osSockError());
	}

	misTrc(T_MGR, "Set socket to blocking (FD: %d)", conn->fd);

	time(&conn->info.busy);
	conn->info.commands++;

	/* Send a connection to a forked child */
	send_status = send_server(conn);
    }
    else
    {
	send_status = SEND_OK;
    }

    if (SEND_OK == send_status)
    {
	if (conn->closing)
	{
	    osSockShutdown(conn->fd);
	    osSockClose(conn->fd);
	    DelConnIdle(conn);
	}
	else
	{
	    misTrc(T_MGR, "Sent connection to server");
	    DetachConnIdle(conn);
	}
    }
    else if (SEND_WAIT == send_status)
    {
	misTrc(T_MGR, "Connection is waiting");
	MoveConnPending(conn);
    }
    else
    {
	misTrc(T_MGR, "Problem sending to server");
	osSockShutdown(conn->fd);
	osSockClose(conn->fd);
	DelConnIdle(conn);
    }

    return TRUE;
}
