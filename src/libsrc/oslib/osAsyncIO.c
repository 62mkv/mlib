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
 *#END************************************************************************/

#define MOCA_ALL_SOURCE
#include <moca.h>

#include <string.h>
#include <stdio.h>

#ifdef UNIX
# include <unistd.h>
# include <sys/socket.h>
# include <sys/types.h>
# include <signal.h>
# ifdef USE_FCNTL_FOR_ASYNC
#  include <fcntl.h>
#  ifdef SOLARIS
#   include <sys/file.h>
#  endif
# endif
#endif

#include <mocaerr.h>
#include <common.h>
#include <oslib.h>

#ifdef UNIX /* { */

static SOCKET_FD saved_fd;
static void (*saved_handler)(int);

static void LowLevelHandler(int sig)
{
    char buf[10];
    char nbytes;

    /* Detect Closure */
    nbytes = osSockRecv(saved_fd, buf, sizeof buf,
                        OS_SOCK_PEEK|OS_SOCK_NOWAIT);

    /* Closed by peer */
    if (0 == nbytes ||
	(-1 == nbytes && (osSockErrno() == OS_ECONNRESET ||
			  osSockErrno() == OS_ENOTCONN)))
    {
        saved_handler(saved_fd);
    }
}

/*
 * Under unix systems, there are generally two ways to make
 * asynchronous notification work for socket.  Both methods
 * essentially do the same thing.
 *
 * 1. Set the owner of the socket to the current process.  This
 *    allows us to determine who gets the signal when asynchronous
 *    I/O is requested.  With the fcntl approach, this is
 *    accomplished via the fcntl(fd, F_SETOWN, getpid()) call.
 *    with the ioctl approach, this is done with the ioctl(fd,
 *    SIOCSPGRP, &pid) call.
 *
 * 2. Put the socket into async mode.  Any time the state changes
 *    (i.e. data becomes available or buffer space becomes
 *    available), the process specified in step 1 will get hit with
 *    a signal.  With the fcntl approach, this is accomplished via
 *    the fcntl(fd, F_SETFL, FASYNC) call.  With the ioctl approach,
 *    this is done with the ioctl(fd, FIOASYNC, &true) call.
 *
 * The signals are different under the two approaches.  The fcntl
 * approach causes SIGIO to be delivered, while the ioctl approach
 * causes SIGPOLL to be delivered.  Since these two signals never
 * actually appear together on any system, this seems odd.
 *
 * Similar steps are taken to undo the setting. (See osStopAsyncIO)
 */
 
#ifdef USE_FCNTL_FOR_ASYNC
# define ASYNC_SIGNAL SIGIO
#else
# define ASYNC_SIGNAL SIGPOLL
#endif

long osStartAsyncIO(SOCKET_FD fd, void (*handler)(int))
{
   struct sigaction act;
#ifdef USE_IOCTL_FOR_ASYNC
   int trueval = 1;
   pid_t pid;
#else
   int fdflags;
#endif
   int status;

   /*
    * Save off the descriptor and handler.
    */
   saved_fd = fd;
   saved_handler = handler;

   /*
    * First, install a signal handler for the async signal.
    */
   memset(&act, 0, sizeof act);
   act.sa_handler = LowLevelHandler;
   sigemptyset(&act.sa_mask);
   sigaction(ASYNC_SIGNAL, &act, NULL);

#ifdef USE_FCNTL_FOR_ASYNC
    status = fcntl(fd, F_SETOWN, getpid());
    if (status < 0)
    {
	return eERROR;
    }

    if ((fdflags = fcntl(fd, F_GETFL, 0)) == -1)
    {
	return eERROR;
    }

    fdflags |= FASYNC;

    status = fcntl(fd, F_SETFL, fdflags);
    if (status < 0)
    {
	return eERROR;
    }

#else

   status = ioctl(fd, FIOASYNC, &trueval);
   if (status < 0)
   {
      return eERROR;
   }

   pid = getpid(); /* this system call never fails */

   status = ioctl(fd, SIOCSPGRP, &pid);
   if (status < 0)
   {
      return eERROR;
   }
#endif

   return eOK;
}

long osStopAsyncIO(SOCKET_FD fd)
{
   struct sigaction act;
#ifdef USE_IOCTL_FOR_ASYNC
   int falseval = 0;
#else
   int fdflags;
#endif

   /*
    * First, ignore the Async signal.
    */
   memset(&act, 0, sizeof act);
   act.sa_handler = SIG_IGN;
   sigemptyset(&act.sa_mask);
   sigaction(ASYNC_SIGNAL, &act, NULL);

   /*
    * Then, turn off async notification for this socket.  That should
    * get things back to normal for us.
    */
#ifdef USE_FCNTL_FOR_ASYNC
    if ((fdflags = fcntl(fd, F_GETFL, 0)) == -1)
    {
	return eERROR;
    }

    fdflags &= ~(FASYNC);

    fcntl(fd, F_SETFL, fdflags);
#else
    ioctl(fd, FIOASYNC, &falseval);
#endif

   return eOK;
}
#else /* }{ */

static struct
{
    SOCKET_FD fd;
    void (*handler)(int);
    HANDLE cancelEvent;
} ThreadArgs;

static DWORD WINAPI async_tmain(LPVOID args)
{
    HANDLE events[2];
    void (*handler)(int);
    int status;
    WSAEVENT hEvent;
    unsigned long nonblock = 0L;
    SOCKET_FD fd;

    fd = ThreadArgs.fd;
    hEvent = WSACreateEvent();
    WSAEventSelect(fd, hEvent, FD_CLOSE);

    events[0] = hEvent;
    events[1] = ThreadArgs.cancelEvent;
    handler = ThreadArgs.handler;

    status = WaitForMultipleObjects(2, events, FALSE, INFINITE);

    /*
     * Deal with things if we were signalled.
     */
    if (status == WAIT_OBJECT_0)
    {
	handler(0);
    }

    WSAEventSelect(fd, (WSAEVENT) NULL, 0);
    ioctlsocket(fd, FIONBIO, &nonblock);
    WSACloseEvent(hEvent);

    return 0;
}

static HANDLE hThread;

long osStartAsyncIO(SOCKET_FD fd, void (*handler)(int))
{
    DWORD threadid;
    HANDLE hCloseEvent;

    hCloseEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
    ThreadArgs.cancelEvent = hCloseEvent;

    ThreadArgs.fd = fd;
    ThreadArgs.handler = handler;

    hThread = CreateThread(NULL, 0, async_tmain, NULL, 0, &threadid);

    return eOK;
}

long osStopAsyncIO(SOCKET_FD fd)
{
    SetEvent(ThreadArgs.cancelEvent);
    WaitForSingleObject(hThread, INFINITE);
    CloseHandle(ThreadArgs.cancelEvent);
    CloseHandle(hThread);
    hThread = NULL;

    return eOK;
}
#endif /* } */
