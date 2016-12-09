static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Generate OS-independent SOCKET error text.
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
#include <errno.h>
#include <string.h>

#include "osprivate.h"

#ifdef WIN32 /*{*/
static struct {
    int errnum;
    char *text;
} err_text[] =
{
    {WSAEINTR, "Interrupted system call"},
    {WSAEBADF, "Bad file number"},
    {WSAEACCES, "Permission denied"},
    {WSAEFAULT, "Bad address"},
    {WSAEINVAL, "Invalid argument"},
    {WSAEMFILE, "Too many open files"},
    {WSAEWOULDBLOCK, "Operation would block"},
    {WSAEINPROGRESS, "Operation now in progress"},
    {WSAEALREADY, "Operation already in progress"},
    {WSAENOTSOCK, "Socket operation on non-socket"},
    {WSAEDESTADDRREQ, "Destination address required"},
    {WSAEMSGSIZE, "Message too long"},
    {WSAEPROTOTYPE, "Protocol wrong type for socket"},
    {WSAENOPROTOOPT, "Protocol not available"},
    {WSAEPROTONOSUPPORT, "Protocol not supported"},
    {WSAESOCKTNOSUPPORT, "Socket type not supported"},
    {WSAEOPNOTSUPP, "Operation not supported"},
    {WSAEPFNOSUPPORT, "Protocol family not supported"},
    {WSAEAFNOSUPPORT, "Address family not supported by protocol family"},
    {WSAEADDRINUSE, "Address already in use"},
    {WSAEADDRNOTAVAIL, "Can't assign requested address"},
    {WSAENETDOWN, "Network is down"},
    {WSAENETUNREACH, "Network is unreachable"},
    {WSAENETRESET, "Network dropped connection on reset"},
    {WSAECONNABORTED, "Software caused connection abort"},
    {WSAECONNRESET, "Connection reset by peer"},
    {WSAENOBUFS, "No buffer space available"},
    {WSAEISCONN, "Socket is already connected"},
    {WSAENOTCONN, "Socket is not connected"},
    {WSAESHUTDOWN, "Can't send after socket shutdown"},
    {WSAETOOMANYREFS, "Too many references: can't splice"},
    {WSAETIMEDOUT, "Connection timed out"},
    {WSAECONNREFUSED, "Connection refused"},
    {WSAELOOP, "Too many levels of symbolic links"},
    {WSAENAMETOOLONG, "File name too long"},
    {WSAEHOSTDOWN, "Host is down"},
    {WSAEHOSTUNREACH, "No route to host"},
    {WSAENOTEMPTY, "Directory not empty"},
    {WSAEUSERS, "Unknown error"},
    {WSAEDQUOT, "Disk quota exceeded"},
    {WSAESTALE, "Stale NFS file handle"},
    {WSAEREMOTE, "Too many levels of remote in path"},
    {WSAEPROCLIM, "Limit Exceeded"},
    {WSASYSNOTREADY, "Network subsystem not ready"},
    {WSAVERNOTSUPPORTED, "Version not supported"},
    {WSANOTINITIALISED, "Not initialized"},
    {WSAHOST_NOT_FOUND, "Host not found"},
    {WSATRY_AGAIN, "Try Again"},
    {WSANO_RECOVERY, "Non-recoverable Error"},
    {WSANO_DATA, "No data"},
    {WSANO_ADDRESS, "No address"},
    {0, "Unknown Error"}
};

static char *sockstrerror(int num)
{
    register int ii;

    for (ii=0; err_text[ii].errnum;ii++)
    {
	if (num == err_text[ii].errnum)
	    return err_text[ii].text;
    }
    return err_text[ii].text;
}
#endif /*}*/

char *osSockError(void)
{
#ifdef WIN32 /*{*/
   return sockstrerror(WSAGetLastError());
#else /*}{*/
   return strerror(errno);
#endif /*}*/
}

long osSockErrno(void)
{
#ifdef WIN32 /*{*/
   switch(WSAGetLastError())
   {
   case WSAECONNRESET:
       return OS_ECONNRESET;
   case WSAEINTR:
       return OS_EINTR;
   case WSAEWOULDBLOCK:
   case WSAEINPROGRESS:
       return OS_EAGAIN;
   case WSAENOTCONN:
       return OS_ENOTCONN;
   }
#else /*}{*/
   switch(errno)
   {
   case ECONNRESET:
       return OS_ECONNRESET;
   case EPIPE:
       return OS_EPIPE;
   case EINTR:
       return OS_EINTR;
   case EAGAIN:
#if EAGAIN != EWOULDBLOCK
   case EWOULDBLOCK:
#endif
   case EINPROGRESS:
       return OS_EAGAIN;
   case ENOTCONN:
       return OS_ENOTCONN;
   }
#endif /*}*/
   return OS_EERROR;
}
