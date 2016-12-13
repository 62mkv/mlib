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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef USE_NSPIPE
# include <unistd.h>
# include <errno.h>
# include <fcntl.h>
# include <stropts.h>		/* defines struct strfdinsert */
# include <sys/types.h>
# include <sys/stream.h>		/* defines queue_t */
#endif

#ifdef USE_MSGHDR
# include <errno.h>
# ifdef HAVE_SYS_TYPES_H
#  include <sys/types.h>
# endif
# ifdef HAVE_SYS_UIO_H
#  include <sys/uio.h>
# endif
# include <sys/socket.h>		/* struct msghdr */
#endif

#include <mocaerr.h>
#include "osprivate.h"


#ifdef USE_MSGHDR
/*
 * Receive a file descriptor from another process (a server).
 * We have a (sizeof (long))-byte control word, which tells us if
 * a file descriptor was sent, or if the controlword was the whole
 * message.
 */
long osRecvFile(PIPE_FD pipefd, long *controlword, SOCKET_FD * fd,
	        OS_TCP_ADDR *tcp_addr)
{
#ifdef HAVE_MSGHDR_CMSGHDR
    struct
    {
	struct cmsghdr hdr;
	int fd;
    } cmsg;
#endif
    struct msghdr msg;
    int nread;
    struct iovec iov[2];

    iov[0].iov_base = (void *)controlword;
    iov[0].iov_len = sizeof *controlword;
    iov[1].iov_base = (void *)tcp_addr;
    iov[1].iov_len = sizeof *tcp_addr;
    msg.msg_iov = iov;
    msg.msg_iovlen = 2;
    msg.msg_name = NULL;
    msg.msg_namelen = 0;
#ifdef HAVE_MSGHDR_CLASSIC
    msg.msg_accrights = (caddr_t) fd;   /* addr of descriptor */
    msg.msg_accrightslen = sizeof(int);         /* receive 1 descriptor */
#elif defined(HAVE_MSGHDR_CMSGHDR)
    msg.msg_control = (void *)&cmsg;
    msg.msg_controllen = sizeof cmsg;
#endif

    if ((nread = recvmsg(pipefd, &msg, 0)) < 0)
    {
	fprintf(stderr, "recvmsg:%s\n", strerror(errno));
	return (eERROR);
    }
    else if (nread == 0)
    {
	fprintf(stderr, "Connection Closed by server\n");
	return (eERROR);
    }

    /*
     * Only attempt to read the FD if the control word is OS_SF_SENDFILE.
     */
#ifdef HAVE_MSGHDR_CLASSIC
    if (*controlword == OS_SF_SENDFILE && msg.msg_accrightslen != sizeof(int))
    {
        fprintf(stderr, "status = 0 but no fd\n");
        return (eERROR);
    }
#elif defined(HAVE_MSGHDR_CMSGHDR)
    if (*controlword == OS_SF_SENDFILE)
	*fd = cmsg.fd;
#endif
    return (eOK);
}

#elif defined(USE_NSPIPE)

/*
 * Receive a file descriptor from another process (a server).
 * We have a (sizeof (long))-byte control word, which tells us if
 * a file descriptor was sent, a socket address was sent, or if the
 * controlword was the whole message.
 */
long osRecvFile(PIPE_FD pipefd, long *controlword, SOCKET_FD * fd,
	        OS_TCP_ADDR *tcp_addr)
{
    int nread;
    struct strrecvfd recvfd;

    /*
     * First set the passed-back file descriptor to an invalid value.
     */
    *fd = -1;

    /*
     * Receive the control word.  We should receive this in one single
     * read.  Is there ever a reason this could get fragmented? XXX
     */
    if ((nread = read(pipefd, controlword, sizeof *controlword)) < 0)
    {
	fprintf(stderr, "read error:%s\n", strerror(errno));
	return (eERROR);
    }

    if (nread == 0)
    {
	fprintf(stderr, "read:connection closed\n");
	return (eERROR);
    }

    if (nread != sizeof *controlword)
    {
	fprintf(stderr, "read:Message framing error\n");
	return (eERROR);
    }

    /*
     * Only attempt to read the FD if the control word is OS_SF_SENDFILE.
     */
    if (*controlword == OS_SF_SENDFILE)
    {
	if (ioctl(pipefd, I_RECVFD, &recvfd) < 0)
	    return (eERROR);
	*fd = recvfd.fd;	/* new descriptor */
    }

    else if (*controlword == OS_SF_CLOSETCP)
    {
	nread = read(pipefd, tcp_addr, sizeof *tcp_addr);
	if (sizeof *tcp_addr != nread)
	{
	    return (eERROR);
	}
    }

    return (eOK);
}

#endif

#ifdef WIN32

extern int os_SocketInited;
/*
 * Receive the handle sent to us by send_fd().
 */
long osRecvFile(PIPE_FD hPipe, long *controlword, SOCKET_FD *fd,
                OS_TCP_ADDR *tcp_addr, HANDLE hEvent)
{
    int nbytes;
    OS__SENDFILE_STRUCT sf;

    if (!os_SocketInited) osSockInit();

    /*
     * Handle the message loop.  This is needed because of concerns
     * over using COM single threaded apartments.  We NEED to handle
     * all windows messages, on our thread, if we want to play nicely
     * with COM STA's.
     */
    while(1)
    {
        DWORD dwStatus;
        MSG msg;

        dwStatus = MsgWaitForMultipleObjects(1, &hEvent, FALSE, INFINITE,
			                     QS_ALLINPUT);
        if (dwStatus == WAIT_OBJECT_0)
        {
            ResetEvent(hEvent);
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
            return eERROR;
        }
    }

    /*
     * Now actually read from the pipe.
     */
    if (!ReadFile(hPipe, &sf, sizeof sf, &nbytes, NULL))
    {
	return eERROR;
    }

    /*
     * Only attempt to read the FD if the control word is OS_SF_SENDFILE.
     */
    if (sf.controlword == OS_SF_SENDFILE)
    {
	*fd = WSASocket(FROM_PROTOCOL_INFO,
			FROM_PROTOCOL_INFO,
			FROM_PROTOCOL_INFO,
			&sf.data.info, 0, 0);
	if (*fd == INVALID_SOCKET)
	{
	    printf("error duplicating socket: %d (%s)\n", WSAGetLastError(), osSockError());
	}
    }
    else if (sf.controlword == OS_SF_CLOSETCP)
    {
        *tcp_addr = sf.data.tcp_addr;
    }

    *controlword = sf.controlword;

    return eOK;
}

#endif /* } */
