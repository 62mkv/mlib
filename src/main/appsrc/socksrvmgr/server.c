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

#define TIMEFMT "%m/%d/%y %H:%M:%S"

static int nextId(void)
{
}
/*
 * Fork n servers and add them to our linked list.
 */
int fork_servers(int num_servers)
{
    int i;
    char port[10];
    char fdarg[50];
    char hideas[1024];
    SERV tmp;
    CONN *conn;
    SERV *serv;
    CONS *cons;
    PIPE_FD pipes[2];
    PIPE_FD newfd;
    char idlist[1024];

    memset(idlist, 0, sizeof idlist);

    for (serv = ServFreeTop; serv; serv = serv->next)
        idlist[serv->id] = 1;
    for (serv = ServBusyFirst; serv; serv = serv->next)
        idlist[serv->id] = 1;

    /* Fork each server requested. */
    for (i = 0; i < num_servers; i++)
    {
	char *arglist[50];
	int ii, jj;
        int id = -1;
        char idarg[16];

        for (ii=0; ii < param.max_servers; ii++)
        {
            if (idlist[ii] == 0)
            {
               idlist[ii] = 1;
               id = ii;
               sprintf(idarg, "%d", ii);
               break;
            }
        }

	/*
	 * Create bi-directional pipe for sending/receiving open file
	 * descriptors.
	 */
	if (osPipe(pipes) != eOK)
	{
	    misLogError("osPipe: %s", osError( ));
	    return eERROR;
	}

	/*
	 * Fork the child
	 */
	switch ((tmp.pid = fork( )))
	{
	case -1:		/* An error occurred */
	    misLogError("fork: %s", osError( ));
	    return eERROR;
	case 0:		/* Child */
	    /*
	     * Close all extra file descriptors we would otherwise inherit.
	     * First close the listening sockets, if they've already been
	     * opened.
	     */
	    if (listen_fd != INVALID_SOCKET)
		close(listen_fd);
	    if (console_fd != INVALID_SOCKET)
		close(console_fd);

	    /*
	     * Then close the various sockets and pipes.  Inbound socket 
	     * connections and consoles, pipes to backend servers.
	     *
	     * 1 - Close the socket to each client.
             * 2 - Close the socket to each console port.
	     * 3 - Close the pipe to each free server process.
	     * 4 - Close the pipe to each busy server process and
	     *     its socket to its connected client.
	     */
	    for (conn = ConnIdleTop; conn; conn = conn->next)
		close(conn->fd);
	    for (cons = ConsTop; cons; cons = cons->next)
		close(cons->fd);
	    for (serv = ServFreeTop; serv; serv = serv->next)
		close(serv->pipefd);
	    for (serv = ServBusyFirst; serv; serv = serv->next)
	    {
		close(serv->pipefd);
		if (serv->connection)
		    close(serv->connection->fd);
	    }

	    /*
	     * Close the other end of the pipe. Also dup our end of the pipe
	     * to keep the FD number down.
	     */
	    close(pipes[0]);
	    newfd = dup(pipes[1]);
	    close(pipes[1]);

	    /* 
	     * Translate the FD to a string that can be passed on the command
	     * line to the server process we're about to exec.
	     */
	    osEncodeDescriptor(newfd, fdarg);

	    /* Build the command to execute. */
            sprintf(hideas, "%s %d", SERVER_PROGRAM, param.port);

	    arglist[ii=0] = hideas;

            arglist[++ii] = "-S";
            arglist[++ii] = param.console_section;

	    arglist[++ii] = "-P";
	    arglist[++ii] = fdarg;

	    sprintf(port, "%d", param.port);
	    arglist[++ii] = "-p";
	    arglist[++ii] = port;

	    if (param.trace_level[0])
	    {
		arglist[++ii] = "-t";
		arglist[++ii] = param.trace_level;
	    }

	    for (jj = 0; jj < param.nopts; jj++)
		arglist[++ii] = param.opts[jj];

            if (id >= 0) 
            {
                arglist[++ii] = "-i";
                arglist[++ii] = idarg;
            }

	    arglist[++ii] = (char *) 0;

	    ResetChildSignalHandler( );

	    /*
	     * Set up the environment for this server process.
	     */
	    set_environment(SERVER_PROGRAM);

	    /*
	     * Exec the server process.
	     */
	    if (-1 == execvp(SERVER_PROGRAM, arglist))
	    {
		misLogError("execv: %s", osError( ));
		exit(EXIT_FAILURE);
	    }

	default:		/* Parent */
	    close(pipes[1]);
	}

	/*
	 * Build the server info. for this new server process.
	 */
	tmp.pipefd = pipes[0];
	tmp.connection = NULL;
	tmp.closing = 0;
	tmp.shutdown = 0;
	tmp.info.created = tmp.info.busy = time(NULL);
	tmp.info.count = 0;

	misTrc(T_MGR, "Created server process (PID: %d)", tmp.pid);

	/* Add it to the list */
	AddServFree(&tmp);

	/* Add this FD to the list select should check. */
	FD_SET(tmp.pipefd, &readfds);
	if (tmp.pipefd > max_fd) max_fd = tmp.pipefd;
    }

    return TRUE;
}

/*
 * Find a server and send a socket handle to it.
 */
int send_server(CONN *conn)
{
    SERV *tmp;
    long send_cmd;

    /* 
     * Look for an eligible server. If we can't find one, make one.
     */
    if (!ServFreeTop)
    {
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

    misTrc(T_MGR, "Sending connection to server... (PID: %lu)", tmp->pid);

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

    tmp->info.busy = time(NULL);
    tmp->info.count++;

    /*
     * Now our process is busy. Move it to the busy list.
     */
    MoveServBusy(tmp);

    /*
     * Send the socket to a child process.
     */
    if (eOK != osSendFile(tmp->pipefd, send_cmd, conn->fd, &conn->addr))
    {
	misLogError("osSendFile: %s", osError( ));

        tmp->connection = NULL;
        misTrc(T_MGR, "Error duplicating socket.  Closing connection to server %d", tmp->pid);
        MoveServFree(tmp);
        shutdown_server(tmp);

	return SEND_ERROR;
    }

    misTrc(T_MGR, "Sent connection to server (PID: %lu)", tmp->pid);

    return SEND_OK;
}

int shutdown_server(SERV *serv)
{
    serv->info.busy = time(NULL);
    serv->shutdown = 1;
    MoveServBusy(serv);

    if (osSendFile(serv->pipefd, OS_SF_SHUTDOWN, -1, NULL) < 0)
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


