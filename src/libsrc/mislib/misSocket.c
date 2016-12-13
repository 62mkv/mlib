static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to support a "socket buffer".
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

#ifdef UNIX
# include <sys/time.h>
# include <sys/types.h>
# include <unistd.h>
#endif

#include <mocaerr.h>
#include <mislib.h>
#include <oslib.h>


#define BUFSIZE 1024


/*
 *  Socket Buffer Structure
 */

struct mis_Socket
{
    SOCKET_FD fd;

    int eof,
        error,
        errorTimeout,
        inOffset,
	inPosition, 
	outOffset;

    long timeout;

    unsigned char *inBuffer,
                  *outBuffer;
};


/*
 *  FUNCTION: misSockOpen
 *
 *  PURPOSE:  Open a socket buffer.
 *
 *  RETURNS:  Pointer to the socket buffer.
 *            NULL - An error occurred.
 */

misSocket *misSockOpen(char *host, unsigned short port)
{
    long status;

    SOCKET_FD fd;

    misSocket *sock;

    /* Connect to the remote host/port. */
    status = osTCPConnect(&fd, host, port);
    if (status != eOK)
	return NULL;

    /* Allocate space for the socket buffer structure. */
    sock = malloc(sizeof(misSocket));
    if (! sock)
	return NULL;

    /* Initialize the socket buffer structure. */
    sock->fd           = fd;
    sock->inBuffer     = malloc(BUFSIZE);
    sock->inOffset     = 0;
    sock->inPosition   = 0;
    sock->outBuffer    = malloc(BUFSIZE);
    sock->outOffset    = 0;
    sock->eof          = 0;
    sock->error        = 0;
    sock->errorTimeout = 0;
    sock->timeout      = 240;

    return sock;
}


/*
 *  FUNCTION: misSockOpenTimeout
 *
 *  PURPOSE:  Open a socket buffer.
 *
 *  RETURNS:  Pointer to the socket buffer.
 *            NULL - An error occurred.
 */

misSocket *misSockOpenTimeout(char *host, 
			      unsigned short port, 
			      long timeout)
{
    long status;

    SOCKET_FD fd;

    misSocket *sock;

    /* Connect to the remote host/port. */
    status = osTCPConnectTimeout(&fd, host, port, timeout);
    if (status != eOK)
	return NULL;

    /* Allocate space for the socket buffer structure. */
    sock = malloc(sizeof(misSocket));
    if (! sock)
	return NULL;

    /* Initialize the socket buffer structure. */
    sock->fd           = fd;
    sock->inBuffer     = malloc(BUFSIZE);
    sock->inOffset     = 0;
    sock->inPosition   = 0;
    sock->outBuffer    = malloc(BUFSIZE);
    sock->outOffset    = 0;
    sock->eof          = 0;
    sock->error        = 0;
    sock->errorTimeout = 0;
    sock->timeout      = 240;

    return sock;
}


/*
 *  FUNCTION: misSockOpenWithBind
 *
 *  PURPOSE:  Open a socket buffer.
 *
 *  RETURNS:  Pointer to the socket buffer.
 *            NULL - An error occurred.
 */

misSocket *misSockOpenWithBind(char *host,
                               unsigned short port,
                               misSocket *my_sock)
{
    long status;

    SOCKET_FD fd;

    misSocket *sock;

    /* Connect to the remote host/port. */
    status = osTCPConnectWithBind(&fd, host, port, my_sock->fd);
    if (status != eOK)
        return NULL;

    /* Allocate space for the socket buffer structure. */
    sock = malloc(sizeof(misSocket));
    if (! sock)
        return NULL;

    /* Initialize the socket buffer structure. */
    sock->fd           = fd;
    sock->inBuffer     = malloc(BUFSIZE);
    sock->inOffset     = 0;
    sock->inPosition   = 0;
    sock->outBuffer    = malloc(BUFSIZE);
    sock->outOffset    = 0;
    sock->eof          = 0;
    sock->error        = 0;
    sock->errorTimeout = 0;
    sock->timeout      = 240;

    return sock;
}


/*
 *  FUNCTION: misSockOpenFD
 *
 *  PURPOSE:  Open a socket buffer for the given socket fd.
 *
 *  RETURNS:  Pointer to the socket buffer.
 *            NULL - An error occurred.
 */

misSocket *misSockOpenFD(int fd)
{

    misSocket *sock;

    /* Allocate space for the socket buffer structure. */
    sock = malloc(sizeof(misSocket));
    if (! sock)
	return NULL;

    /* Initialize the socket buffer structure. */
    sock->fd           = (SOCKET_FD) fd;
    sock->inBuffer     = malloc(BUFSIZE);
    sock->inOffset     = 0;
    sock->inPosition   = 0;
    sock->outBuffer    = malloc(BUFSIZE);
    sock->outOffset    = 0;
    sock->eof          = 0;
    sock->error        = 0;
    sock->errorTimeout = 0;
    sock->timeout      = 240;

    return sock;
}


/*
 *  FUNCTION: misSockClose
 *
 *  PURPOSE:  Close a socket buffer.
 *
 *  RETURNS:  void
 */

void misSockClose(misSocket *sock)
{
    /* Don't bother if we don't have a socket. */
    if (!sock)
	return;

    /* Shutdown and close the socket. */
    osSockShutdown(sock->fd);
    osSockClose(sock->fd);

    /* Free memory associated with the socket buffer structure. */
    free(sock->inBuffer);
    free(sock->outBuffer);
    free(sock);

    return;
}


/*
 *  FUNCTION: misSockCheck
 *
 *  PURPOSE:  Check the connection to the given socket.
 *
 *  RETURNS:  eOK
 *            eMIS_SOCKET_DISCONNECTED
 */

long misSockCheck(misSocket *sock)
{
    long nbytes;

    char buffer[10];

    /* Don't bother if we don't have a socket to work with. */
    if (!sock)
	return eINVALID_ARGS;

    /* Make sure we still have a connection to the server. */
    nbytes = osSockRecv(sock->fd, buffer, sizeof buffer,
                        OS_SOCK_PEEK|OS_SOCK_NOWAIT);
    if (0 == nbytes || (nbytes < 0 && osSockErrno( ) != OS_EAGAIN))
    {
        return eMIS_SOCKET_DISCONNECTED;
    }

    return eOK;
}


/*
 *  FUNCTION: misSockTimeout
 *
 *  PURPOSE:  Set the timeout to use for the given socket.
 *
 *  RETURNS:  void to the socket buffer.
 */

void misSockTimeout(misSocket *sock, long timeout)
{
    if (timeout >= 0)
	sock->timeout = timeout;

    return;
}


/*
 *  FUNCTION: misSockPutc
 *
 *  PURPOSE:  Put a character into the buffer and flush the buffer if we've
 *            hit the maximum buffer size.
 *
 *  RETURNS:  The character put into the buffer.
 *            -1 - An error occurred.
 */

int misSockPutc(misSocket *sock, int c)
{
    /* Add this character to our buffer. */
    sock->outBuffer[sock->outOffset++] = c;

    /* Flush our buffer if we've filled it up. */
    if (sock->outOffset == BUFSIZE)
    {
	misSockFlush(sock);

        if (sock->error)
	    return -1;
    }

    return c;
}


/*
 *  FUNCTION: misSockPuts
 *
 *  PURPOSE:  Write a line at a time to the socket, given a separate 
 *            end-of-line string.
 *
 *  RETURNS:  The number of bytes written.
 */

int misSockPuts(char *string, misSocket *sock, char *eol)
{
    long nwritten;

    /* Write the actual string. */
    nwritten = misSockWrite(sock, string, strlen(string));

    /* Write the end-of-line string if necessary. */
    if (nwritten != MIS_SOCK_EOF && nwritten != MIS_SOCK_ERROR && eol)
    {
	nwritten += misSockWrite(sock, eol, strlen(eol));
    }

    return nwritten;
}


/*
 *  FUNCTION: misSockWrite
 *
 *  PURPOSE:  Write N bytes of data to the socket.  This is a raw write 
 *            that pays no attention to lines and other boundaries.  Any 
 *            size buffer can be written with this interface, and it's 
 *            always guaranteed to write the whole amount.
 *
 *  RETURNS:  The number of bytes written.
 */

int misSockWrite(misSocket *sock, char *buffer, long size)
{
    long pos,
         status;

    /* Initialize where we are in the given buffer. */
    pos = 0;

    /* 
     *  While the remainder of the given buffer is large enough
     *  to fill up our buffer, we can copy it into our buffer
     *  and flush it right away to the socket.
     */
    while ((size - pos) >= (BUFSIZE - sock->outOffset))
    {
	/* Copy the given buffer into our buffer. */
	memcpy(&sock->outBuffer[sock->outOffset], 
	       &buffer[pos], BUFSIZE - sock->outOffset);

	/* Determine where in the given buffer we are now. */
	pos += (BUFSIZE - sock->outOffset);

	/* Set the new offset into our buffer. */
	sock->outOffset = BUFSIZE;

	/* Flush our buffer. */
	status = misSockFlush(sock);
	if (status == MIS_SOCK_ERROR)
	    return status;
    }

    /* Anything left in the given buffer can just be copied into our buffer. */
    if (size > pos)
    {
	memcpy(&sock->outBuffer[sock->outOffset], &buffer[pos], size - pos);
	sock->outOffset += (size - pos);
    }

    return size;
}


/*
 *  FUNCTION: misSockFlush
 *
 *  PURPOSE:  Flush the buffer we're holding for the outgoing data.
 *
 *  RETURNS:  The number of bytes flushed.
 */

int misSockFlush(misSocket *sock)
{
    int nbytes, 
        nflushed;

    /* Initialize the number of bytes we've flushed. */
    nflushed = 0;

    /* Continue flushing our buffer until it's all gone. */
    while (nflushed < sock->outOffset)
    {
	nbytes = osSockSend(sock->fd, sock->outBuffer + nflushed, 
		            sock->outOffset - nflushed, 0);
	if (nbytes < 0)
	{
	    sock->error = 1;
	    return MIS_SOCK_ERROR;
	}

	/* Increment the number of bytes we've flushed. */
	nflushed += nbytes;
    }

    /* Reset the offset into our buffer. */
    sock->outOffset = 0;

    return nflushed;
}


/*
 *  FUNCTION: sDataOnSocket
 *
 *  PURPOSE:  Check for data on the given socket.  We timeout after a
 *            certain time so we don't freeze up our caller.
 *
 *  RETURNS:  0 - There is no data on the socket.
 *            1 - There is data on the socket.
 */

static int sDataOnSocket(misSocket *sock)
{
    fd_set fds;
    struct timeval timeout;

    /* Populate the timeout structure. */
    timeout.tv_sec  = sock->timeout;
    timeout.tv_usec = 0;

    /* Populate the fd set. */
    FD_ZERO(&fds);
    FD_SET(sock->fd, &fds);

    /* Check the socket for data. */
    if (select(sock->fd + 1, &fds, NULL, NULL, &timeout) > 0)
	return 1;

    return 0;
}


/*
 *  FUNCTION: sFillBuffer
 *
 *  PURPOSE:  Fill the buffer for reading.  This will read up to the full
 *            buffer size.  Since it's only called when the buffer is empty, 
 *            it's safe to fill it at the buffer boundary.
 *
 *  RETURNS:  void
 */

static void sFillBuffer(misSocket *sock)
{
    int nbytes;

    /* Make sure there's data on the socket. */
    if (! sDataOnSocket(sock))
    {
	sock->errorTimeout = 1;
	sock->inOffset     = 0;
	return;
    }

    /* Read as many bytes as we can into our buffer. */
    nbytes = osSockRecv(sock->fd, sock->inBuffer, BUFSIZE, 0);

    /* Set our attributes accordingly. */
    if (nbytes < 0)
    {
	sock->error    = 1;
	sock->inOffset = 0;
    }
    else if (nbytes == 0)
    {
	sock->eof      = 1;
	sock->inOffset = 0;
    }
    else
    {
	sock->inOffset = nbytes;
    }

    sock->inPosition = 0;
}


/*
 *  FUNCTION: misSockGetc
 *
 *  PURPOSE:  Read a character from the buffer. If we're at the end of 
 *            our buffered space, block until another character comes 
 *            across.
 *
 *  RETURNS:  The character read from the socket.
 */

int misSockGetc(misSocket *sock)
{
    /* If we've already got a character in the buffer, just return it. */
    if (sock->inPosition < sock->inOffset)
	return (int) sock->inBuffer[sock->inPosition++];

    /* Fill up the buffer if it's empty. */
    sFillBuffer(sock);

    /* Handle end-of-file errors. */
    if (sock->eof)
	return MIS_SOCK_EOF;

    /* Handle miscellaneous socket errors. */
    if (sock->error)
	return MIS_SOCK_ERROR;

    /* Handle timeout errors. */
    if (sock->errorTimeout)
	return MIS_SOCK_TIMEOUT;

    return (int) sock->inBuffer[sock->inPosition++];
}


/*
 *  FUNCTION: misSockGets
 *
 *  PURPOSE:  Get a whole string, (up to and including a specified EOL 
 *            character) blocking as necessary.  The string is always 
 *            null-terminated.
 *
 *  RETURNS:  The string read from the socket. 
 */

char *misSockGets(char *buffer, long size, misSocket *sock, char eol)
{
    int c;
    long ii;

    /* Get the number of bytes requested. */
    for (ii = 0; ii < (size - 1); ii++)
    {
	c = misSockGetc(sock);
	if (c == MIS_SOCK_EOF || c == MIS_SOCK_ERROR || c == MIS_SOCK_TIMEOUT ) 
        {
	    buffer[ii] = '\0';
	    return NULL;
        }
	
	/* Append this byte to the buffer. */
	buffer[ii] = c;

	/* Have we reached an end-of-file? */
	if (c == eol)
	{
	    ii++;
	    break;
	}
    }

    /* Null-terminate the buffer. */
    buffer[ii] = '\0';

    return buffer;
}


/*
 *  FUNCTION: misSockRead
 *
 *  PURPOSE:  Read N bytes of data from the socket.  This is a raw read 
 *            that pays no attention to lines and other boundaries.  Any 
 *            size buffer can be read with this interface, and it's 
 *            always guaranteed to read the whole amount.
 *
 *
 *  RETURNS:  The string read from the socket. 
 */

char *misSockRead(misSocket *sock, char *buffer, long size)
{
    int c;
    long ii;

    /* Get the number of bytes requested. */
    for (ii = 0; ii < size; ii++)
    {
	c = misSockGetc(sock);
	if (c == MIS_SOCK_EOF || c == MIS_SOCK_ERROR || c == MIS_SOCK_TIMEOUT )
	{
	    buffer[ii] = '\0';
	    return NULL;
        }

	/* Append this byte to the buffer. */
	buffer[ii] = c;
    }

    return buffer;
}


/*
 *  FUNCTION: misSockFD
 *
 *  PURPOSE:  Get the fd associated with the socket.
 */

int misSockFD(misSocket *sock)
{
    return sock->fd;
}

/*
 *  FUNCTION: misSockError
 *
 *  PURPOSE:  Determine if an EOF has been reached.
 *
 *  RETURNS:  1 - EOF has been reached.
 *            0 - EOF has not yet been reached.
 */

int misSockEOF(misSocket *sock)
{
    return sock->eof;
}


/*
 *  FUNCTION: misSockError
 *
 *  PURPOSE:  Determine if an error has occurred.
 *
 *  RETURNS:  1 - An error has occurred.
 *            0 - An error has not occurred.
 */

int misSockError(misSocket *sock)
{
    return sock->error;
}
