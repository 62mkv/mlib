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

#include "socksrvmgr.h"

#define SRV_STATUS_DONE 1

static int conn_activity(CONN * conn, time_t now);
static int serv_activity(SERV * serv, int isbusy, time_t now);

SOCKET_FD listen_fd  = INVALID_SOCKET, 
	  console_fd = INVALID_SOCKET;

int main(int argc, char *argv[])
{
    int nfds,
	status;

    time_t client_time;

    CONN *conn, *nextconn;
    SERV *serv, *nextserv;
    CONS *cons, *nextcons;

    memset(&param, '\0', sizeof param);

    printf(misGetStartBanner(APPNAME));

    /* Initialize runtime parameters. */
    InitializeParameters(argc, argv);

    /* Initialize signal handling. */
    InitializeSignalHandling( );

    /*
     * We have to conditionally add a lot of different descriptors to
     * our fd_set variables. First we initialize them to zero and set
     * the numbfer of fd's for select to check using max_fd.
     */
    FD_ZERO(&readfds);
    FD_ZERO(&writefds);
    max_fd = 0;

    /*
     * Then pre-fork the servers.  Ideally, we should do this before we set up
     * our listen socket.  The param.min_servers CAN be zero, meaning we
     * fork as necessary.
     */
    fork_servers(param.min_servers);

    /*
     * Set up our listen socket.
     */
    if (eOK != osTCPListen(&listen_fd, param.port, OS_MAX_BACKLOG, TRUE))
    {
	misLogError("osTCPListen(port): %s", osSockError( ));
	exit(EXIT_FAILURE);
    }

    /* Add this FD to the list of read FD's select should check. */
    FD_SET(listen_fd, &readfds);
    if (listen_fd > max_fd) max_fd = listen_fd;

    /*
     * Set up our console listen socket.
     */
    if (eOK != osTCPListen(&console_fd, 
		           param.console_port, 
			   OS_MAX_BACKLOG,
			   TRUE))
    {
	misLogError("osTCPListen(console): %s", osSockError( ));
	exit(EXIT_FAILURE);
    }

    /* Add this FD to the list of read FD's select should check. */
    FD_SET(console_fd, &readfds);
    if (console_fd > max_fd) max_fd = console_fd;

    /* 
     * Set the next client cleanup times.  
     */
    client_time = time(NULL) + param.conn_timeout;

    /*
     * Our main loop.  Do this forever.
     */
    for (;;)
    {
	fd_set tmprfds, tmpwfds;
	struct timeval timeout;
	time_t now;

	tmprfds = readfds;
	tmpwfds = writefds;
	timeout.tv_sec = 60 - (time(NULL) % 60) + 1;
	timeout.tv_usec = 0;

	nfds = select(max_fd+1, &tmprfds, &tmpwfds, NULL, &timeout);

	now = time(NULL);

	switch (nfds)
	{
	case -1:
	    misTrc(T_MGR, "select: %s\n", osSockError( ));
	    break;

	case 0:
	    break;

	default:
	    misTrc(T_MGR, "There's activity on %d FD's", nfds);

	    /*
	     * 1 - Check for a new client connection request. 
	     */
	    if (FD_ISSET(listen_fd, &tmprfds))
	    {
		SOCKET_FD newfd;

		misTrc(T_MGR,"Received client connection request");

		/*
		 * Because we got word of the connecting process, accept( )
		 * won't block.
		 */
		if (eOK != osSockAccept(listen_fd, &newfd, 0))
		{
		    /* What happened? */
		    misLogError("accept(port): %s", osSockError( ));
		}
		else
		{
		    CONN tmp;

		    misTrc(T_MGR,"Accepted client connection request (FD: %d)", newfd);

		    osTCPKeepalive(newfd, 1);

		    /*
		     * Build the connection info. for this new client.
		     *
		     * We were successful. Add us to the list of active
		     * connections.
		     */
		    tmp.fd = newfd;
		    tmp.server = NULL;
		    tmp.closing = FALSE;
		    tmp.info.commands = 0;
		    tmp.info.connected = now;
		    tmp.info.idle = now;
		    osSockAddress(newfd, 
			          tmp.addr.ip, 
				  sizeof tmp.addr.ip,
				  &tmp.addr.port);

                    /* 
		     * Add this client connections to the list of idle 
		     * client connections.  The activity associated with
		     * this client connection will then be handled down
		     * below.
		     */
		    AddConnIdle(&tmp);

		    /* Add this FD to the list select should check. */
		    FD_SET(newfd, &readfds);
		    if (newfd > max_fd) max_fd = newfd;
		}
	    }

	    /*
	     * 2 - Check free servers for activity.
	     *
	     *     We have to check the free servers before checking the
	     *     busy servers because busy servers can be moved to the
	     *     free servers list, which could screw up our logic a bit.
	     */
	    for (serv = ServFreeTop; serv; serv = nextserv)
	    {
		/* 
		 * Get a pointer to the next server in the list, because
		 * this server could end up being removed from the list
		 * in the serv_activity( ) call.
		 */
		nextserv = serv->next;

		/* Deal with this server if it has activity. */
		if (FD_ISSET(serv->pipefd, &tmprfds))
		{
		    FD_CLR(serv->pipefd, &tmprfds);

		    /*
		     * Figure out what just happened.
		     */
		    serv_activity(serv, FALSE, now);
		}
	    }

	    /*
	     * 3 - Check busy servers for activity.
	     */
	    for (serv = ServBusyFirst; serv; serv = nextserv)
	    {
		/* 
		 * Get a pointer to the next server in the list, because
		 * this server could end up being removed from the list
		 * in the serv_activity( ) call.
		 */
		nextserv = serv->next;

		/* Deal with this server if it has activity. */
		if (FD_ISSET(serv->pipefd, &tmprfds))
		{
		    FD_CLR(serv->pipefd, &tmprfds);

		    /*
		     * Figure out what just happened.
		     */
		    serv_activity(serv, TRUE, now);
		}
	    }

	    /*
	     * 4 - Check idle client connections for activity.
	     */
	    for (conn = ConnIdleTop; conn; conn = nextconn)
	    {
		/* 
		 * Get a pointer to the next connection in the list, because
		 * this connection could end up being removed from the list
		 * in the conn_activity( ) call.
		 */
		nextconn = conn->next;
		if (FD_ISSET(conn->fd, &tmprfds))
		{
		    /*
		     * Figure out what just happened.
		     */
		    conn_activity(conn, now);
		}
	    }

	    /*
	     * 5 - Check for a new console connection request. 
	     */
	    if (FD_ISSET(console_fd, &tmprfds))
	    {
		SOCKET_FD newfd;

		misTrc(T_MGR,"Received console connection request");

		if (eOK != osSockAccept(console_fd, &newfd, 0))
		{
		    misLogError("accept(console): %s", osSockError( ));
		}
		else
		{
		    CONS tmp;

		    misTrc(T_MGR,"Accepted console connection request (FD: %d)", newfd);

		    osTCPKeepalive(newfd, 1);

		    /*
		     * Build the console info. for this new connection.
		     *
		     * We were successful. Add us to the list of active
		     * connections.
		     */
		    tmp.fd = newfd;
		    tmp.ibuf = tmp.obuf = NULL;
		    tmp.ibytes = tmp.obytes = 0;
		    tmp.closing = 0;
		    tmp.echo = 0;
		    tmp.status = 0;
		    tmp.privlevel = PRIV_LUSER;

                    /* 
		     * Add this console connections to the list of console 
		     * connections.  The activity associated with
		     * this console connection will then be handled down
		     * below.
		     */
		    AddCons(&tmp);

		    /* Add this FD to the list select should check. */
		    FD_SET(newfd, &readfds);
		    if (newfd > max_fd) max_fd = newfd;

		    /* Display the console prompt for the connection. */
		    cons_prompt(&tmp);
		}
	    }

	    /*
	     * 6 - Check console connections for activity.
	     */
	    for (cons = ConsTop; cons; cons = nextcons)
	    {
		char buf[2];
		int nbytes;

		/* 
		 * Get a pointer to the next connection in the list, because
		 * this connection could end up being removed from the list
		 * down below.
		 */
		nextcons = cons->next;

		/* Deal with this console connection if it has activity. */
		if (FD_ISSET(cons->fd, &tmpwfds))
		{
		    if (eOK != cons_sync(cons))
		    {
			osSockShutdown(cons->fd);
			osSockClose(cons->fd);
			FD_CLR(cons->fd, &readfds);
			FD_CLR(cons->fd, &writefds);
			if (cons->ibuf) free(cons->ibuf);
			if (cons->obuf) free(cons->obuf);
			DelCons(cons);
			cons = NULL;
		    }
		    else if (!cons->obytes)
		    {
			FD_CLR(cons->fd, &writefds);
			FD_SET(cons->fd, &readfds);
		    }
		}
		else if (FD_ISSET(cons->fd, &tmprfds))
		{
		    nbytes = osSockRecv(cons->fd, buf, sizeof buf, OS_SOCK_PEEK);
		    if (nbytes == 0 || nbytes == SOCKET_ERROR)
		    {
			osSockShutdown(cons->fd);
			osSockClose(cons->fd);
			FD_CLR(cons->fd, &readfds);
			FD_CLR(cons->fd, &writefds);
			if (cons->ibuf) free(cons->ibuf);
			if (cons->obuf) free(cons->obuf);
			DelCons(cons);
		    }
		    else
		    {
			cons_read(cons);
			while (eOK == cons_process(cons))
			{
			    if (!cons->closing) cons_prompt(cons);
			}

			if (cons->obytes)
			{
			    FD_CLR(cons->fd, &readfds);
			    FD_SET(cons->fd, &writefds);
			}
		    }
		}
	
		/*
		 * If we're closing a console, we only want to do it when
		 * there's no mora data to send out.
		 */
		if (cons && cons->closing && !cons->ibytes && !cons->obytes)
		{
		    osSockShutdown(cons->fd);
		    osSockClose(cons->fd);
		    FD_CLR(cons->fd, &readfds);
		    FD_CLR(cons->fd, &writefds);
		    if (cons->ibuf) free(cons->ibuf);
		    if (cons->obuf) free(cons->obuf);
		    DelCons(cons);
		    cons = NULL;
		}
	    }
	}

	/*
	 * Check for client timeout.  If the client timeout
	 * capability is enabled, then we shouldn't have many idle
	 * clients connected, so this is not too taxing an operation.
	 */
	if (param.conn_timeout && now >= client_time)
	{
	    for (conn = ConnIdleTop; conn; conn = nextconn)
	    {
		nextconn = conn->next;
		if (now - conn->info.idle >= param.conn_timeout)
		{
		    misTrc(T_MGR, "Closing idle connection... (FD: %d)", 
			   conn->fd);
		    osSockShutdown(conn->fd);
		    osSockClose(conn->fd);
		    FD_CLR(conn->fd, &readfds);
		    DelConnIdle(conn);
		}
	    }
	    client_time = now + param.conn_timeout;
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
    }
}

static int conn_activity(CONN * conn, time_t now)
{
    char buf[4];
    int nbytes;
    long send_status;

    misTrc(T_MGR,"Handling client activity... (FD: %d)", conn->fd);

    /* Is it read activity or close activity? */

    /* Detect Closure */
    nbytes = osSockRecv(conn->fd, buf, sizeof buf, OS_SOCK_PEEK);

    if (nbytes == 0 || nbytes == SOCKET_ERROR)		/* Closed by peer */
    {
	conn->closing = TRUE;
	misTrc(T_MGR,"Close activity (FD: %d)", conn->fd);
    }
    else
    {
	misTrc(T_MGR,"Read activity (FD: %d)", conn->fd);
    }

    FD_CLR(conn->fd, &readfds);

    if (!conn->closing)
    {
	/* Send a connection to a forked child */
	send_status = send_server(conn);
    }
    else
    {
	misTrc(T_MGR,"Not sending connection request to a server");

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
	    DetachConnIdle(conn);
	    conn->info.busy = now;
	    conn->info.commands++;
	}
    }
    else if (SEND_WAIT == send_status)
    {
	misTrc(T_MGR, "Connection must wait");
	MoveConnPending(conn);
    }
    else
    {
	misTrc(T_MGR,"Problem sending to a server");
	osSockShutdown(conn->fd);
	osSockClose(conn->fd);
	DelConnIdle(conn);
    }

    return TRUE;
}

static int serv_activity(SERV * serv, int isbusy, time_t now)
{
    int nbytes;
    int status;

    misTrc(T_MGR,"Handling server activity... (FD: %d/PID: %d)", serv->pipefd, 
	                                                         serv->pid);

    /*
     * Read the incoming descriptor for data.  Result data should always be
     * an integer, which corresponds to the result status.
     */
    nbytes = read(serv->pipefd, &status, sizeof status);
    if (-1 == nbytes || 0 == nbytes)
    {
	misTrc(T_MGR,"Server lost connection (FD: %d/PID: %d)", serv->pipefd, 
		                                                serv->pid);
	
	/* Close the (now-dead) pipe descriptor */
	osSockClose(serv->pipefd);
	FD_CLR(serv->pipefd, &readfds);

	/*
	 * If this is a busy server, we have to deal with the connection
	 * associated with it.
	 */
	if (isbusy)
	{
	    /*
	     * Deal with the connection associated with the server.  We
	     * shouldn't have to take it out of our FD_SET because it was
	     * taken out when the connection was sent to the server.
	     */
	    if (serv->connection)
	    {
	        osSockShutdown(serv->connection->fd);
		osSockClose(serv->connection->fd);
		MoveConnIdle(serv->connection);
		DelConnIdle(serv->connection);
	    }
	    DelServBusy(serv);
	}
	else
	{
	    DelServFree(serv);
	}
    }
    else
    {
	/*
	 * If it didn't die, we're only expecting to get pipe activity
	 * from the server when it's got a connection.  Otherwise, it's
	 * an error.
	 */
	if (!isbusy)
	{
	    misLogError("Recevied an unexpected status from server (Status: %d)", status);
	}
	else
	{
	    /*
	     * We're only expecting to get one status back.  Otherwise, 
	     * it's an error.
	     */
	    if (status != SRV_STATUS_DONE)
		misLogError("Got unexpected status from server (Status: %d)", status);

	    /*
	     * If there's a connection associated with this server, we need
	     * to put it back into the idle connection pool.
	     */
	    if (serv->connection)
	    {
		misTrc(T_MGR,"Marking connection as idle... (FD: %d)", 
		       serv->connection->fd);
		serv->connection->info.idle = now;
		MoveConnIdle(serv->connection);
		FD_SET(serv->connection->fd, &readfds);
	    }

	    /*
	     * Now we need to put this server back into the idle server pool.
	     */
	    misTrc(T_MGR,"Marking server as free... (FD: %d/PID: %d)", 
		   serv->pipefd, 
		   serv->pid);
	    serv->connection = NULL;
	    MoveServFree(serv);

	    /*
	     * Look to see if there's a pending connection.  A pending
	     * connection is one that was held up from sending to a server due
	     * to too much activity.  Since a server has freed up, we send the
	     * pending connection to the newly freed server immediately.
	     */
	    if (ConnPendingFirst)
	    {
		CONN *conn = ConnPendingFirst;

		misTrc(T_MGR, "Sending pending request...");

		DetachConnPending(conn);
		if (SEND_OK == send_server(conn))
		{
		    if (conn->closing)
		    {
	                osSockShutdown(conn->fd);
			osSockClose(conn->fd);
			MoveConnIdle(conn);
			DelConnIdle(conn);
		    }
                    else
                    {
                        conn->info.busy = now;
                        conn->info.commands++;
                    }
		}
		else
		{
		    misLogError("Could not to send a pending request: %s",
			        osError( ));
	            osSockShutdown(conn->fd);
		    osSockClose(conn->fd);
		    MoveConnIdle(conn);
		    DelConnIdle(conn);
		}
	    }
	    else
	    {
		/*
		 * If there's no pending connection, check to see if this
		 * process puts us over our limit of idle servers.  If so, shut
		 * the server down.
		 */

                if (param.max_commands > 0 &&
		         serv->info.count >= param.max_commands)
                {
                    misTrc(T_MGR, "Server exceeded max commands: (%d >= %d)",
                           serv->info.count, param.max_commands);
                    serv->closing = 1;
                }

		if (serv->closing)
		{
		    misTrc(T_MGR,"Sending shutdown request to server... (FD: %d/PID: %d)", 
			   serv->pipefd, serv->pid);
		    shutdown_server(serv);
		}
	    }
	}
    }

    return TRUE;
}
