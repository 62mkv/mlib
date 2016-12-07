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
 *  Copyright (c) 2002
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

#define MOCA_ALL_SOURCE

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <string.h>
#include <errno.h>

#ifdef UNIX
# include <sys/types.h>
# include <sys/socket.h>
# include <netinet/in.h>
# include <arpa/inet.h>
# include <sys/un.h>
# include <fcntl.h>
# include <netdb.h>
# include <unistd.h>
# include <signal.h>
# include <time.h>
#ifdef HAVE_SYS_TIME_H
# include <sys/time.h>
#endif
#endif


#include <mocaerr.h>

#include "osprivate.h"

int os_SocketInited = 0;
long osSockInit(void)
{
#ifdef WIN32
    WSADATA wsaData;

    /*
     * Initialize Winsock 2.
     */
    if (0 != WSAStartup(2, &wsaData))
    {
	return eERROR;
    }
#else
    signal(SIGPIPE, SIG_IGN);
#endif
    os_SocketInited = 1;
    return eOK;
}

long osSockAccept(SOCKET_FD fd, SOCKET_FD *newfd, int flags)
{
    struct sockaddr addr;
#ifdef AIX
    size_t len;
#else
    int len;
#endif
#ifdef UNIX
    int fdflags, origflags;
#else
    int save_err;
#endif

    len = sizeof addr;

    if (!os_SocketInited) osSockInit();

    if (flags & OS_SOCK_NOWAIT)
    {
#ifdef UNIX
	if ((origflags = fcntl(fd, F_GETFL, 0)) != -1)
	{
	    fdflags = origflags;
	    fdflags |= O_NONBLOCK;
	    fcntl(fd, F_SETFL, fdflags);
	}
#else
	int nonblock = 1;
        ioctlsocket(fd, FIONBIO, &nonblock);
#endif
    }

    *newfd = accept(fd, &addr, &len);

#ifdef WIN32
    save_err = WSAGetLastError();
#endif

    if (flags & OS_SOCK_NOWAIT)
    {
#ifdef UNIX
	if (origflags != -1)
	{
	    fcntl(fd, F_SETFL, origflags);
	}
#else
	int nonblock = 0;
        ioctlsocket(fd, FIONBIO, &nonblock);
#endif
    }

#ifdef WIN32
    WSASetLastError(save_err);
#endif

    if (INVALID_SOCKET == *newfd)
    {
	return eERROR;
    }

#ifdef WIN32
    SetHandleInformation((HANDLE)*newfd, HANDLE_FLAG_INHERIT, 0);
#endif 

    return eOK;
}

long osSockBlocking(SOCKET_FD fd, int blocking)
{
#ifdef UNIX
    int fdflags, origflags;
    if ((origflags = fcntl(fd, F_GETFL, 0)) != -1)
    {
	fdflags = origflags;
	if (blocking)
	{
	    fdflags &= ~(O_NONBLOCK);
	}
	else
	{
	    fdflags |= O_NONBLOCK;
	}
	fcntl(fd, F_SETFL, fdflags);
    }
#else
    unsigned long nonblock = (blocking?0:1);
    ioctlsocket(fd, FIONBIO, &nonblock);
#endif
    return eOK;
}

int osSockSend(SOCKET_FD fd, void *msg, int len, int flags)
{
    int nbytes;
    int sendflags;
#ifdef UNIX
    int fdflags, origflags;
#else
    int save_err;
#endif

    if (!os_SocketInited) osSockInit();

    sendflags = 0;

    if (flags & OS_SOCK_OOB) sendflags |= MSG_OOB;

    if (flags & OS_SOCK_NOWAIT)
    {
#ifdef UNIX
	if ((origflags = fcntl(fd, F_GETFL, 0)) != -1)
	{
	    fdflags = origflags;
	    fdflags |= O_NONBLOCK;
	    fcntl(fd, F_SETFL, fdflags);
	}
#else
	int nonblock = 1;
        ioctlsocket(fd, FIONBIO, &nonblock);
#endif
    }

    nbytes = send(fd, msg, len, sendflags);

#ifdef WIN32
    save_err = WSAGetLastError();
#endif

    if (flags & OS_SOCK_NOWAIT)
    {
#ifdef UNIX
	if (origflags != -1)
	{
	    fcntl(fd, F_SETFL, origflags);
	}
#else
	int nonblock = 0;
        ioctlsocket(fd, FIONBIO, &nonblock);
#endif
    }

#ifdef WIN32
    WSASetLastError(save_err);
#endif

    return nbytes;
}

int osSockRecv(SOCKET_FD fd, void *msg, int len, int flags)
{
    return osSockRecvTimeout(fd, msg, len, flags, 0);
}

int osSockRecvTimeout(SOCKET_FD fd, void *msg, int len, int flags, int timeout)
{
    int nbytes;
    int recvflags;
#ifdef UNIX
    int fdflags, origflags;
#else
    int save_err;
#endif

    if (!os_SocketInited) osSockInit();

    recvflags = 0;

    if (flags & OS_SOCK_PEEK) recvflags |= MSG_PEEK;
    if (flags & OS_SOCK_OOB)  recvflags |= MSG_OOB;

    if (flags & OS_SOCK_NOWAIT && !timeout)
    {
#ifdef UNIX
	if ((origflags = fcntl(fd, F_GETFL, 0)) != -1)
	{
	    fdflags = origflags;
	    fdflags |= O_NONBLOCK;
	    fcntl(fd, F_SETFL, fdflags);
	}
#else
	int nonblock = 1;
        ioctlsocket(fd, FIONBIO, &nonblock);
#endif
    }

    /* We use a select(3c) to support timeouts. */
    if (timeout)
    {
	int nfds, maxfd;
	fd_set readfds, errorfds;
	struct timeval mytimeout;

	/* Setup our read fds. */
	FD_ZERO(&readfds);
	FD_SET(fd, &readfds);

	/* Setup our error fds. */
	FD_ZERO(&errorfds);
	FD_SET(fd, &errorfds);

	/* Setup our max fd. */
	maxfd = fd;

	/* Setup our timeout. */
	mytimeout.tv_sec  = timeout;
	mytimeout.tv_usec = 0;

	/* Wait for activity or a timeout. */
	nfds = select(maxfd+1, &readfds, NULL, &errorfds, &mytimeout);

	/* A 0 means we timed out and a -1 means we've got an error. */
	switch (nfds)
	{
	    case 0:                                    /* Timeout */
#ifdef WIN32
                WSASetLastError(WSAEWOULDBLOCK);
#else
                errno = EWOULDBLOCK;
#endif
		nbytes = -1;
		goto cleanup;
	    case -1:                                   /* Error   */
                if (osSockErrno( ) == OS_EINTR)
		    exit(-1);
		break;
	    default:                                   /* Ready   */
		break;
	}
    }

    nbytes = recv(fd, msg, len, recvflags);

cleanup:

#ifdef WIN32
    save_err = WSAGetLastError();
#endif

    if (flags & OS_SOCK_NOWAIT && !timeout)
    {
#ifdef UNIX
	if (origflags != -1)
	{
	    fcntl(fd, F_SETFL, origflags);
	}
#else
	int nonblock = 0;
        ioctlsocket(fd, FIONBIO, &nonblock);
#endif
    }

#ifdef WIN32
    WSASetLastError(save_err);
#endif

    return nbytes;
}

long osSockAddress(SOCKET_FD fd, char *outstr, long size, unsigned short *port)
{
    union {
	struct sockaddr_in sin;
#ifdef UNIX
	struct sockaddr_un sock_un;
#endif
	struct sockaddr sock;
    } addr;

#ifdef AIX
    size_t bufsize = sizeof addr;
#else
    int bufsize = sizeof addr;
#endif

    if (!os_SocketInited) osSockInit();

    /*
     * Get the remote socket address.
     */
    if (getpeername(fd, &addr.sock, &bufsize) < 0)
    {
	misLogError("getpeername: %s", osSockError( ));
	return eERROR;
    }

    switch(addr.sock.sa_family)
    {
#ifdef UNIX
    case AF_UNIX:
	strncpy(outstr, addr.sock_un.sun_path, size);
	if (port) *port = 0;
	break;
#endif
    case AF_INET:
	strncpy(outstr, inet_ntoa(addr.sin.sin_addr), size);
	if (port) *port = ntohs(addr.sin.sin_port);
	break;
    }

    if (size)
        outstr[size-1] = 0;

    return eOK;
}

long osSockLocalAddress(SOCKET_FD fd, 
			char *outstr, 
			long size, 
			unsigned short *port)
{
    union {
	struct sockaddr_in sin;
#ifdef UNIX
	struct sockaddr_un sock_un;
#endif
	struct sockaddr sock;
    } addr;

#ifdef AIX
    size_t bufsize = sizeof addr;
#else
    int bufsize = sizeof addr;
#endif

    if (!os_SocketInited) osSockInit();

    /*
     * Get the local socket address.
     */
    if (getsockname(fd, &addr.sock, &bufsize) < 0)
    {
        misLogError("sockname: %s", osSockError( ));
        return eERROR;
    }

    switch(addr.sock.sa_family)
    {
#ifdef UNIX
    case AF_UNIX:
	strncpy(outstr, addr.sock_un.sun_path, size);
	if (port) *port = 0;
	break;
#endif
    case AF_INET:
	strncpy(outstr, inet_ntoa(addr.sin.sin_addr), size);
	if (port) *port = ntohs(addr.sin.sin_port);
	break;
    }

    if (size)
        outstr[size-1] = 0;

    return eOK;
}

long osSockShutdown(SOCKET_FD fd)
{
    if (!os_SocketInited) osSockInit();

#ifdef UNIX
    shutdown(fd, 2);
#else
    shutdown(fd, SD_BOTH);
#endif

    return eOK;
}

long osSockClose(SOCKET_FD fd)
{
    if (!os_SocketInited) osSockInit();

#ifdef UNIX
    close(fd);
#else
    closesocket(fd);
#endif

    return eOK;
}


static long sSockWait(SOCKET_FD fd, int do_read, int timeout)
{
#ifdef WIN32
    WSAEVENT hEvent;
    MSG msg;
    DWORD dwStatus;
    unsigned long nonblock = 0L;
    char tmp;

    /*
     * Check ahead to see if the socket has any outstanding data on it.
     * If so, don't even bother with the event.
     */
    if (do_read &&
        osSockRecv(fd, &tmp, sizeof tmp, OS_SOCK_PEEK|OS_SOCK_NOWAIT) >= 0)
    {
        return eOK;
    }

    hEvent = WSACreateEvent();
    /* Only wait for follow event to occur: FD_READ, RF_ACCEPT, FD_CLOSE 
     * NOTE: If data already existed on socket event is not triggered 
     */
    WSAEventSelect(fd, hEvent, FD_READ|FD_ACCEPT|FD_CLOSE);

    /*
     * Handle the message loop.  This is needed because of concerns over
     * using COM single threaded apartments.  We NEED to handle
     * all windows messages, on our thread, if we want to play nicely
     * with COM STAs.
     */
    while(1)
    {
        dwStatus = MsgWaitForMultipleObjects(1, &hEvent, FALSE, 
                   (timeout>0)?timeout*1000:INFINITE, QS_ALLINPUT);

        if ((timeout>0) && dwStatus == WAIT_TIMEOUT)
        {
            WSAEventSelect(fd, (WSAEVENT) NULL, 0);
            ioctlsocket(fd, FIONBIO, &nonblock);
            WSACloseEvent(hEvent);
            return eOS_TIMEOUT;
        }
        if (dwStatus == WAIT_OBJECT_0)
        {
            break;
        }
        else if (dwStatus == WAIT_OBJECT_0 + 1)
        {
            while(PeekMessage(&msg, NULL, 0, 0, PM_REMOVE))
            {
                TranslateMessage(&msg);
                DispatchMessage(&msg);
            }
        }
        else
        {
            WSAEventSelect(fd, (WSAEVENT) NULL, 0);
            ioctlsocket(fd, FIONBIO, &nonblock);
            WSACloseEvent(hEvent);
            return eERROR;
        }
    }
    WSAEventSelect(fd, (WSAEVENT) NULL, 0);
    ioctlsocket(fd, FIONBIO, &nonblock);
    WSACloseEvent(hEvent);

#else /* UNIX */
    /* If no timeout is passed in, it is a noop 
     *    backward compatiblity for osSockWait 
     */
    if(timeout > 0)
    {
	int nfds, maxfd;
	fd_set readfds, errorfds;
	struct timeval mytimeout;

	/* Setup our read fds. */
	FD_ZERO(&readfds);
	FD_SET(fd, &readfds);

	/* Setup our error fds. */
	FD_ZERO(&errorfds);
	FD_SET(fd, &errorfds);

	/* Setup our max fd. */
	maxfd = fd;

	/* Setup our timeout. */
	mytimeout.tv_sec  = timeout;
	mytimeout.tv_usec = 0;

	/* Wait for activity or a timeout. */
	nfds = select(maxfd+1, &readfds, NULL, &errorfds, 
                     (timeout>0)?&mytimeout:NULL);

	/* A 0 means we timed out and a -1 means we've got an error. */
	switch (nfds)
	{
	    case 0:                                    /* Timeout */
               return eOS_TIMEOUT;
               break;
	    case -1:                                   /* Error   */
                if (osSockErrno( ) == OS_EINTR)
		    exit(-1);
                return eERROR;
		break;
	    default:                                   /* Ready   */
		break;
	}
    }
#endif
    return eOK;
}

long osSockWait(SOCKET_FD fd, int do_read)
{
   return sSockWait(fd, do_read, -1);
}

long osSockWaitTimeout(SOCKET_FD fd, int timeout)
{
   return sSockWait(fd, 1, timeout);
}
