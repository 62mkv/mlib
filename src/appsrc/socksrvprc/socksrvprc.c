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

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <oslib.h>
#include <srvlib.h>

#include "socksrvprc.h"

static void PrintUsage(void)
{
    fprintf(stderr,
	        "Usage: socksrvprc -S <section> [ -p <port> ]\n"
	        "\t-S <section>           Registry section name to use\n"
	        "\t-p <port>              Listen on port \n"
	        "\t-o <trace file>        Trace file pathname\n"
	        "\t-t <trace levels>      Trace level switches\n"
	        "\t-h                     Show help\n"
	        "\t-v                     Show version information\n"
	        "%s",
	        misGetTraceOptionsString());
}

int main(int argc, char *argv[])
{
    long status;

    int c,
        gotport = 0, 
	gotpool = 0;

    char procname[30];

    unsigned short listen_port = 0;

    char *ptr        = NULL,
         *command    = NULL,
         *section    = NULL,
         *id         = "",
	 *traceFile  = NULL,
	 *traceLevel = NULL;

    PIPE_FD pipe_fd;

    SOCKET_FD client_fd,
              listen_fd;

    OS_TCP_ADDR tcp_addr;

#ifdef WIN32
    HANDLE hEvent = NULL;
    HANDLE hServerEvent = NULL;
#endif

    /* Handle command line arguments. */
    while ((c = osGetopt(argc, argv, "S:p:i:t:o:P:e:E:vh?")) != -1)
    {
	switch (c)
	{
        case 'S':
            section = osOptarg;
            break;
	case 'p':
	    listen_port = (unsigned short) atoi(osOptarg);
	    gotport++;
	    break;
        case 'i':
            id = osOptarg;
            break;
	case 't':
	    traceLevel = osOptarg;
	    break;
	case 'o':
	    traceFile = osOptarg;
	    break;
	case 'P':
	    osDecodeDescriptor(osOptarg, &pipe_fd);
	    gotpool++;
	    break;
#ifdef WIN32
	case 'e':
	    osDecodeDescriptor(osOptarg, &hEvent);
	    break;
	case 'E':
	    osDecodeDescriptor(osOptarg, &hServerEvent);
	    break;
#endif
	case 'v':
	    printf(misGetVersionBanner(APPNAME));
	    exit(EXIT_SUCCESS);
	case 'h':
	case '?':
	default:
            PrintUsage( );
	    exit(EXIT_FAILURE);
	}
    }

    /* Make sure we were given a section name. */
    if (section == NULL)
    {
	PrintUsage( );
	exit(EXIT_FAILURE);
    }

    /* Initialize as a server process. */
    sprintf(procname, "socksrvprc-%s", id);
    status = srvInitialize(procname, 1);

    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize as a server process - exiting...\n");
        exit(EXIT_FAILURE);
    }

    /* Set up tracing if necessary. */
    if (traceLevel)
        misSetTraceLevelFromArg(traceLevel);
    if (traceFile)
        misSetTraceFile(traceFile, "a+");

    /* Initialize the OS library. */
    osInit();

    /* Get a port number to listen on if one wasn't provided. */
    if (!gotport && !gotpool)
    {
	ptr = osGetReg(section, REGKEY_SOCKMGR_PORT);
	listen_port = ptr ? (unsigned short) atol(ptr) : DEFAULT_SOCKMGR_PORT;
    }

    /* Get the server command we'll be calling. */
    command = osGetReg(section, REGKEY_SOCKMGR_SERVER_COMMAND);
    if (!command || !strlen(command))
    {
        fprintf(stderr, "A command must be defined in the registry - exiting...\n");
        exit(EXIT_FAILURE);
    }

    /* Initialize signal handling. */
    InitializeSignalHandling( );

    misEnterProcess("socksrvprc");

    /* 
     * We run in one of two modes:
     *
     *    1) "Pool Mode" where we are running as a pool process of the
     *       socksrvmgr.
     *    2) "Port Mode" where we are running as a standalone server process.
     */

    if (gotpool)
    {
	while (!gShutdown)
	{
	    long controlword;

	    misTrc(T_FLOW, "Waiting in \"pool mode\" for a connection request...");

#ifdef WIN32
	    status = osRecvFile(pipe_fd, &controlword, &client_fd,
		                    &tcp_addr, hServerEvent);
#else
	    status = osRecvFile(pipe_fd, &controlword, &client_fd,
		                    &tcp_addr);
#endif
	    if (status != eOK || controlword == OS_SF_SHUTDOWN)
	    {
		misTrc(T_FLOW, "Shutting down... (Status: %d Control: %d)",
			  status, controlword);
		break;
	    }

	    /*
	     * If the connection went down, clean up after it.
	     */
	    if (controlword == OS_SF_CLOSETCP)
	    {
		misTrc(T_FLOW, "Received close notification - cleaning up...");
		status = eSRV_DISCONNECTED;
		srvCommit();
	    }
	    else if (controlword == OS_SF_SENDFILE)
	    {
		misTrc(T_FLOW, "Reading command...");

		osSockBlocking(client_fd, 1);

	        /* Execute the command. */
                status = ExecuteCommand(command, client_fd, listen_port, 0);

	        /* Close the client connection after finishing. */
		osSockClose(client_fd);
		client_fd = 0;
	    }

            /*
	     * Tell the parent we're done with our socket.
	     */
#ifdef WIN32
	    SetEvent(hEvent);
#else
            status = SRV_STATUS_DONE;
            write(pipe_fd, &status, sizeof status);
#endif
	}
    }
    else 
    {
	/* Listen for incoming requests. */
	status = osTCPListen(&listen_fd, listen_port, OS_MAX_BACKLOG, 1);
	if (status != eOK)
	{
	    misLogError("osTCPListen: %s", osSockError( ));
	    misLogError("Could not setup port for listening");
	    misExitProcess(eERROR);
	}

	misTrc(T_FLOW, "Server started up on port %d", listen_port);

	do
	{
	    /*
	     * Wait until the socket is available. This will return when
	     * further socket operations will not block.
	     */
	    osSockWait(listen_fd, 1);

	    /* Accept a connection on the listen socket. */
	    status = osSockAccept(listen_fd, &client_fd, 0);
	    if (status != eOK)
	    {
	        misLogError("osSockAccept: %s", osSockError( ));
	        misLogError("Could not accept connection request");
		goto exit;
	    }

	    osTCPKeepalive(listen_fd, 1);

	    /* Execute the command until the client disconnects or it fails. */
            ExecuteCommand(command, client_fd, listen_port, 1);

	    /* Close the client connection after finishing. */
	    osSockClose(client_fd);
	    client_fd = 0;

	} while (!gShutdown);
    }

exit:

    srvRollback();
    misExitProcess(eOK);

    return 0;
}
