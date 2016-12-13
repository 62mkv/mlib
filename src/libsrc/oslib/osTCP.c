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

#define MOCA_ALL_SOURCE

#include <moca.h>

#ifdef UNIX
# include <sys/types.h>
# include <sys/socket.h>
# include <netinet/in.h>
# include <netinet/tcp.h>
# include <arpa/inet.h>
# include <fcntl.h>
# include <unistd.h>
# include <netdb.h>
#endif

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include "osprivate.h"

extern int os_SocketInited;

long osTCPListen(SOCKET_FD *desc, unsigned short port, int backlog, int reuse)
{
    struct sockaddr_in addr;
    int trueval = 1;

    if (!os_SocketInited) osSockInit();

    /* Open socket for communication endpoint */
    if ((*desc = socket(AF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET)
    {
        misLogError("socket: %s", osSockError( ));
        return eERROR;
    }

#ifdef WIN32
    SetHandleInformation((HANDLE)*desc, HANDLE_FLAG_INHERIT, 0);
#endif 

    if (reuse)
    {
        /* Set SO_REUSEADDR on socket so we can re-bind quickly */
        if (0 != setsockopt(*desc, SOL_SOCKET, SO_REUSEADDR,
                            (const char *)&trueval, sizeof trueval))
        {
            osSockShutdown(*desc);
            osSockClose(*desc);
            return eERROR;
        }
    }

    /*
     * Bind the socket to our expected port.
     */
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = INADDR_ANY;
    addr.sin_port = htons(port);

    if (SOCKET_ERROR == bind(*desc, (struct sockaddr *) &addr, sizeof addr))
    {
        osSockShutdown(*desc);
        osSockClose(*desc);
        return eERROR;
    }

    if (backlog == OS_MAX_BACKLOG)
    {
        /* Defined in sys/socket.h under Unix, wsock32.h under NT */
        backlog = MIN(OS_SOMAXCONN, SOMAXCONN);
    }

    if (SOCKET_ERROR == listen(*desc, backlog))
    {
        osSockShutdown(*desc);
        osSockClose(*desc);
        return eERROR;
    }

    return eOK;
}

long osTCPConnect(SOCKET_FD *desc, char *host, unsigned short port)
{
    struct sockaddr_in remote_addr;
    struct hostent *remote_host;

    if (!os_SocketInited) osSockInit();

    /* Open socket for communication endpoint */
    if ((*desc = socket(AF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET)
    {
        misLogError("socket: %s", osSockError( ));
        return eERROR;
    }

#ifdef WIN32
    SetHandleInformation((HANDLE)*desc, HANDLE_FLAG_INHERIT, 0);
#endif

    remote_addr.sin_family = AF_INET;
    remote_addr.sin_port = htons(port);

    if (SOCKET_ERROR == (remote_addr.sin_addr.s_addr = inet_addr(host)))
    {
        if (NULL == (remote_host = gethostbyname(host)))
        {
            osSockShutdown(*desc);
            osSockClose(*desc);
            *desc = INVALID_SOCKET;
            return eERROR;
        }

        memcpy(&remote_addr.sin_addr, remote_host->h_addr_list[0],
               remote_host->h_length);
    }

    if (SOCKET_ERROR == connect(*desc, (struct sockaddr *)&remote_addr, sizeof remote_addr))
    {
        osSockShutdown(*desc);
        osSockClose(*desc);
        *desc = INVALID_SOCKET;
        return eERROR;
    }

    return eOK;
}

long osTCPConnectTimeout(SOCKET_FD *desc, 
                         char *host, 
                         unsigned short port,
                         long timeout)
{
    int nfds, 
        max_fd;

    long status;

    fd_set tmpwfds, 
           writefds;

    struct timeval timeval;

    struct sockaddr_in remote_addr;
    struct hostent *remote_host;

    if (!os_SocketInited) osSockInit();

    /* Open socket for communication endpoint */
    if ((*desc = socket(AF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET)
    {
        misLogError("socket: %s", osSockError( ));
        return eERROR;
    }

    /* Turn blocking off. */
    osSockBlocking(*desc, 0);

#ifdef WIN32
    SetHandleInformation((HANDLE) *desc, HANDLE_FLAG_INHERIT, 0);
#endif 

    remote_addr.sin_family = AF_INET;
    remote_addr.sin_port = htons(port);

    if (SOCKET_ERROR == (remote_addr.sin_addr.s_addr = inet_addr(host)))
    {
        if (NULL == (remote_host = gethostbyname(host)))
        {
            osSockShutdown(*desc);
            osSockClose(*desc);
            *desc = INVALID_SOCKET;
            return eERROR;
        }

        memcpy(&remote_addr.sin_addr, remote_host->h_addr_list[0],
               remote_host->h_length);
    }

    /*
     * We would usually check the return value from connect( ), but we'll
     * rely on the select( ) call below to handle errors in the case.  We
     * do this because we want to be able to call this function from
     * multiple threads and the global errno gets messed up if we rely on
     * checking the return value and then examining errno to see if the
     * error was a legitimate error or just that the connection is still
     * in progress.
     */
    status = connect(*desc, (struct sockaddr *) &remote_addr, sizeof remote_addr);
    if (status == SOCKET_ERROR && osSockErrno( ) != OS_EAGAIN)
    {
        osSockShutdown(*desc);
        osSockClose(*desc);
        *desc = INVALID_SOCKET;
        return status;
    }

    FD_ZERO(&writefds);

    FD_SET(*desc, &writefds);
    max_fd = *desc;

    tmpwfds = writefds;

    timeval.tv_sec = timeout; timeval.tv_usec = 0;

    /* If the connect has completed, we'll be able to write to the fd. */
    nfds = select(max_fd+1, NULL, &tmpwfds, NULL, &timeval);

    switch (nfds)
    {
        case -1:
            misTrc(T_FLOW, "select: %s", osSockError( ));
            misTrc(T_FLOW, "Could not select fds for activity");
            osSockShutdown(*desc);
            osSockClose(*desc);
            *desc = INVALID_SOCKET;
            return eERROR;
        case 0:
            misTrc(T_FLOW, "Timer expired before connection established");
            osSockShutdown(*desc);
            osSockClose(*desc);
            *desc = INVALID_SOCKET;
            return eOS_TIMEOUT;
        default:
            break;
    }

    /* Turn blocking back on. */
    osSockBlocking(*desc, 1);

    return eOK;
}

long osTCPConnectWithBind(SOCKET_FD *desc,
                          char *host,
                          unsigned short port,
                          SOCKET_FD my_desc)
{
    struct sockaddr_in my_addr,
                       remote_addr;

    struct hostent *remote_host;

    if (!os_SocketInited) osSockInit();

    /* Get socket information of my local socket address. */
    if (my_desc)
    {
        size_t my_addrlen = sizeof my_addr;

        if (SOCKET_ERROR == getsockname(my_desc, (struct sockaddr *)&my_addr, &my_addrlen))
        {
            *desc = INVALID_SOCKET;
            return eERROR;
        }

        my_addr.sin_port = 0;
    }
    else
    {
        my_addr.sin_family      = AF_INET;
        my_addr.sin_port        = 0;
        my_addr.sin_addr.s_addr = INADDR_ANY;
    }

    /* Open socket for communication endpoint */
    if ((*desc = socket(AF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET)
        return eERROR;

#ifdef WIN32
    SetHandleInformation((HANDLE)*desc, HANDLE_FLAG_INHERIT, 0);
#endif

    remote_addr.sin_family = AF_INET;
    remote_addr.sin_port = htons(port);

    if (SOCKET_ERROR == (remote_addr.sin_addr.s_addr = inet_addr(host)))
    {
        if (NULL == (remote_host = gethostbyname(host)))
        {
            osSockShutdown(*desc);
            osSockClose(*desc);
            *desc = INVALID_SOCKET;
            return eERROR;
        }

        memcpy(&remote_addr.sin_addr, remote_host->h_addr_list[0],
               remote_host->h_length);
    }

    if (SOCKET_ERROR == bind(*desc, (struct sockaddr *)&my_addr, sizeof my_addr))
    {
        osSockShutdown(*desc);
        osSockClose(*desc);
        *desc = INVALID_SOCKET;
        return eERROR;
    }

    if (SOCKET_ERROR == connect(*desc, (struct sockaddr *)&remote_addr, sizeof remote_addr))
    {
        osSockShutdown(*desc);
        osSockClose(*desc);
        *desc = INVALID_SOCKET;
        return eERROR;
    }

    return eOK;
}

long osTCPHostname(char *hostname, long hostsize)
{
    if (!os_SocketInited) osSockInit();

    gethostname(hostname, hostsize);

    return eOK;
}

long osTCPHostAddr(char *ipaddr, long addrsize)
{
    char hostname[100];
    struct hostent *hp;

    if (!os_SocketInited) osSockInit();

    gethostname(hostname, sizeof hostname);
    hp = gethostbyname(hostname);
    if (hp)
        strncpy(ipaddr, inet_ntoa(*(struct in_addr*)hp->h_addr_list[0]), addrsize);

    return eOK;
}

char *osTCPAddrToName(char *hostip)
{
    struct hostent *hp;
    struct in_addr in;
    static char namebuf[100];

    memset(namebuf, '\0', sizeof namebuf);

    if (!os_SocketInited) osSockInit();

    in.s_addr = inet_addr(hostip);
    hp = gethostbyaddr((const char *)&in, sizeof in, AF_INET);
    if (hp)
        strncpy(namebuf, hp->h_name, sizeof namebuf - 1);

    return namebuf;
}

char *osTCPNameToAddr(char *hostname)
{
    static char ipaddress[20];
    struct hostent *hp;

    memset(ipaddress, '\0', sizeof ipaddress);

    if (!os_SocketInited) osSockInit();

    hp = gethostbyname(hostname);
    if (hp)
    {
        strncpy(ipaddress, 
                inet_ntoa(*(struct in_addr*)hp->h_addr_list[0]), 
                sizeof ipaddress - 1);
    }

    return ipaddress;
}

long osTCPKeepalive(SOCKET_FD fd, int onoff)
{
    /* Set SO_KEEPALIVE on socket so dead connections will really die. */
    if (0 != setsockopt(fd, SOL_SOCKET, SO_KEEPALIVE,
                        (const char *)&onoff, sizeof onoff))
    {
        return eERROR;
    }

    return eOK;
}

long osTCPNodelay(SOCKET_FD fd, int onoff)
{
    /* Set TCP_NODELAY on socket so we can send multiple outstanding packets */
    if (0 != setsockopt(fd, IPPROTO_TCP, TCP_NODELAY,
                        (const char *)&onoff, sizeof onoff))
    {
        return eERROR;
    }

    return eOK;
}
