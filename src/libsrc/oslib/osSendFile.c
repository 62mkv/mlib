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

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#ifdef USE_NSPIPE
# include <unistd.h>
# include <errno.h>
# include <fcntl.h>
# include <sys/types.h>
# include <sys/stream.h>
# include <stropts.h>
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
 * Pass a file descriptor to another process.
 * We also pass a control word, to tell the other process if a file descriptor
 * is following.
 */
long osSendFile(PIPE_FD pipefd, long controlword, SOCKET_FD fd,
	        OS_TCP_ADDR *tcp_addr)
{
    struct iovec iov[2];
    int nbytes;
    struct msghdr msg;
#ifdef HAVE_MSGHDR_CMSGHDR
    struct
    {
	struct cmsghdr hdr;
	int fd;
    } cmsg;
#endif

    iov[0].iov_base = (void *)&controlword;
    iov[0].iov_len = sizeof controlword;
    if (tcp_addr)
    {
	iov[1].iov_base = (void *)tcp_addr;
	iov[1].iov_len = sizeof *tcp_addr;
	msg.msg_iovlen = 2;
    }
    else
    {
	msg.msg_iovlen = 1;
    }
    msg.msg_iov = iov;
    msg.msg_name = NULL;
    msg.msg_namelen = 0;

    if (controlword == OS_SF_SENDFILE)
    {
#ifdef HAVE_MSGHDR_CLASSIC
        msg.msg_accrights = (caddr_t) & fd;     /* addr of descriptor */
        msg.msg_accrightslen = sizeof(int);     /* pass 1 descriptor */
#else
	cmsg.hdr.cmsg_len = sizeof cmsg;
	cmsg.hdr.cmsg_level = SOL_SOCKET;
	cmsg.hdr.cmsg_type = SCM_RIGHTS;
	cmsg.fd = fd;

	msg.msg_control = (void *) &cmsg;
	msg.msg_controllen = sizeof cmsg;
#endif
    }
    else
    {
#ifdef HAVE_MSGHDR_CLASSIC
        msg.msg_accrights = NULL;
        msg.msg_accrightslen = 0;
#else
	msg.msg_control = NULL;
	msg.msg_controllen = 0;
#endif
    }

    if ((nbytes = sendmsg(pipefd, &msg, 0)) !=
	(sizeof controlword) + (tcp_addr?(sizeof *tcp_addr):0))
    {
	return (eERROR);
    }

    return (eOK);
}

#elif defined(USE_NSPIPE)

/*
 * Pass a file descriptor to another process.
 * We also pass a control word, to tell the other process if a file descriptor
 * is following.
 */
long osSendFile(PIPE_FD pipefd, long controlword, SOCKET_FD fd,
	        OS_TCP_ADDR *tcp_addr)
{
    char buf[2];		/* send_fd()/recv_fd() 2-byte protocol */

    if (write(pipefd, &controlword, sizeof controlword) != sizeof controlword)
    {
	fprintf(stderr, "Write Failure:%s\n", strerror(errno));
	return (eERROR);
    }

    if (controlword == OS_SF_SENDFILE)
    {
	if (ioctl(pipefd, I_SENDFD, fd) < 0)
	{
	    fprintf(stderr, "Failied on ioctl:%s\n", strerror(errno));
	    return (eERROR);
	}
    }
    else if (controlword == OS_SF_CLOSETCP)
    {
	if (write(pipefd, tcp_addr, sizeof *tcp_addr) != sizeof *tcp_addr)
	{
	    fprintf(stderr, "Write Failure:%s\n", strerror(errno));
	    return (eERROR);
	}

    }

    return (eOK);
}
#endif

#ifdef WIN32
/*
 * This function has a slightly different prototype under WIN32, because
 * we need to know something about the process we're sending to.  That's
 * because the duplication of the handle and the sending across a pipe are
 * separate steps in WIN32.  Thus the added hPid.  Also, we have to signal
 * an event when the fd is ready to be ready because osRecvFile( ) uses
 * a message loop to play nicely with COM STA's.
 */
long osSendFile(PIPE_FD hPipe, long controlword, SOCKET_FD hSend,
                OS_TCP_ADDR *tcp_addr, DWORD dwPid, HANDLE hEvent)
{
    int nbytes;
    BOOL status;
    OS__SENDFILE_STRUCT sf;

    sf.controlword = controlword;
    if (controlword == OS_SF_SENDFILE)
    {
        /*
         * Duplicate the handle into the "handlespace" of the other process.
         * The other process doesn't get any indication that this happened,so
         * we need to use other methods, such as writing the handle to a pipe
         * and signalling an event.
         */
#if 0
        status = DuplicateHandle(GetCurrentProcess(), (HANDLE) hSend, hPid,
                                 &sf.handle, 0, FALSE, 0);
#endif
	status = WSADuplicateSocket(hSend, dwPid, &sf.data.info);
        if (status != 0)
        {
	    printf("error duplicating socket: %d\n", WSAGetLastError());
            return eERROR;
        }
    }
    else if (controlword == OS_SF_CLOSETCP)
    {
        sf.data.tcp_addr = *tcp_addr;
    }

    /*
     * First write out the control word, and then, if the control word
     * is OS_SF_SENDFILE, send the handle.
     */
    status = WriteFile(hPipe, &sf, sizeof sf, &nbytes, NULL);

    if (!status)
    {
	printf("error writing socket info");
        return eERROR;
    }

    /*
     * Signal our peer via an event that the fd is ready to be read.
     */
    SetEvent(hEvent);

    return eOK;
}

#endif
