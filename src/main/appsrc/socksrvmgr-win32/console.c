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

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <oslib.h>

#include "socksrvmgr.h"

typedef struct
{
    SOCKET fd;
} CONSOLE_THREAD_ARGS;

static int num_threads;

#define STATE_PROMPT		1
#define STATE_INPUT		2
#define STATE_PROCESS_INPUT	3
#define STATE_EXIT		9

#define BLOCKSIZE 256

DWORD WINAPI console_tmain(LPVOID ptr)
{
    unsigned long nonblock = FALSE;
    char *readbuf = NULL;
    char *outbuf = NULL;
    int readsize = 0, bufsize = 0, outsize = 0;
    int threadno;
    CONS cons;

    /*
     * Keep track of which thread number we are.  It goes in the console
     * prompt.
     */
    threadno = num_threads++;

    /*
     * Initialize values of CONS structure.
     */
    cons.ibuf = cons.obuf = NULL;
    cons.ibytes = cons.obytes = 0;
    cons.closing = 0;
    cons.privlevel = PRIV_LUSER;
    cons.echo = 0;
    cons.status = 0;

    /*
     * ptr was allocated for our benefit, so we must be in charge of freeing
     * the memory associated with it.
     */
    cons.fd = ((CONSOLE_THREAD_ARGS *) ptr)->fd;
    free(ptr);

    /* Set socket to blocking */
    WSAEventSelect(cons.fd, (WSAEVENT) NULL, 0);
    ioctlsocket(cons.fd, FIONBIO, &nonblock);

    cons_prompt(&cons);
    while (!cons.closing)
    {
	cons_read(&cons);
	while (eOK == cons_process(&cons))
	{
	    if (!cons.closing) cons_prompt(&cons);
	}
    }

    if (cons.ibuf)
	free(cons.ibuf);

    osSockShutdown(cons.fd);
    osSockClose(cons.fd);

    return 0;
}

HANDLE console_thread(SOCKET fd)
{
    HANDLE hThread;
    CONSOLE_THREAD_ARGS *arg;
    DWORD threadid;

    arg = malloc(sizeof(CONSOLE_THREAD_ARGS));
    arg->fd = fd;
    hThread = CreateThread(NULL, 0, console_tmain, arg, 0, &threadid);
    CloseHandle(hThread);
    return hThread;
}

long cons_read(CONS *c)
{
    char buffer[5000]; /* this isn't right, but how should we do it? */
    int i,nbytes;

    nbytes = osSockRecv(c->fd, buffer, sizeof buffer, 0);
    if (nbytes == 0 || nbytes == SOCKET_ERROR)
    {
	c->closing = 1;
	if (nbytes == 0 || osSockErrno() == OS_ECONNRESET)
	{
	    misTrc(T_MGR,"Console disconnect");
	}
	else
	{
	    misTrc(T_MGR, "Socket Problem:%s",osSockError());
	}
	return eERROR;
    }

    /*
     * Check for backspaces
     */
    for (i=0;i<nbytes;)
    {
        if (buffer[i] == '\b')
        {
            if (c->echo && (i>0 || c->ibytes))
            {
                cons_printf(c, "\b \b");
            }
            if (i==0)
            {
                memmove(buffer, buffer+1, nbytes-1);
                nbytes--;
                if (c->ibytes)
                {
                    c->ibuf[c->ibytes - 1] = '\0';
                    c->ibytes--;
                }
            }
            else
            {
                memmove(buffer + i - 1, buffer + i + 1, nbytes - (i + 1));
                i--;
                nbytes -=2;
            }
        }
        else
        {
            i++;
        }
    }

    misDynStrncat(&c->ibuf, buffer, nbytes);
    c->ibytes += nbytes;
    if (c->echo)
        cons_printf(c, "%.*s", nbytes, buffer);
    return eOK;
}

static int translate_crlf(char *in, char *out)
{
    char *tmp = out;
    while(*in)
    {
	if (*in == '\n')
	    *out++ = '\r';
	*out++ = *in++;
    }
    *out = 0;
    return (int)(out - tmp);
}

long cons_printf(CONS *c, char *format, ...)
{
    char tmpbuffer[5000]; /* this isn't right, but how should we do it? */
    char buffer[5000]; /* this isn't right, but how should we do it? */
    va_list ap;
    int bufsize,nbytes,sent = 0;

    va_start(ap,format);
    if (!c || !c->fd)
	vprintf(format, ap);
    else
	bufsize = vsprintf(tmpbuffer, format, ap);
    va_end(ap);

    if (!c || !c->fd)
    {
	return eOK;
    }
    else
    {
	bufsize = translate_crlf(tmpbuffer, buffer);

	while (bufsize > sent)
	{
	    nbytes = osSockSend(c->fd, buffer+sent, bufsize-sent, 0);
	    if (nbytes == SOCKET_ERROR)
	    {
	         misLogError("Error from osSockSend: %s", osSockError());
	         return eERROR;
	    }
	    else
	    {
	         sent += nbytes;
	    }
	}
	return eOK;
    }
}

int cons_line(char *buf)
{
    char *args[100];
    int   nargs;
    char *tmpbuf, *bufptr;
    CONS  tmpcons;
    int   status;

    tmpbuf = malloc(strlen(buf)+1);
    strcpy(tmpbuf, buf);

    bufptr = tmpbuf;

    memset(&tmpcons, 0, sizeof tmpcons);
    tmpcons.privlevel = PRIV_ADMIN;

    for (nargs = 0; nargs < 100 && (args[nargs] = misStrsep(&bufptr, " \t\r\n"));)
	if (*args[nargs] != '\0')
	    nargs++;

    status = cons_command(&tmpcons, nargs, args);

    free(tmpbuf);

    return status;
}

int cons_process(CONS *c)
{
    char *delim;
    char *args[100];
    int nargs;
    int msglen;
    char *bufptr;

    if (!c || !c->ibuf || !c->ibytes)
	return eERROR;

    if (!((delim = memchr(c->ibuf, ';', c->ibytes)) ||
          (delim = memchr(c->ibuf, '\n', c->ibytes))))
	return eERROR;

    msglen = delim - c->ibuf + 1;
    *delim = '\0';
    bufptr = c->ibuf;

    misTrc(T_MGR, "Processing console command...");

    for (nargs = 0; nargs < 100 && (args[nargs] = misStrsep(&bufptr, " \t\r\n"));)
	if (*args[nargs] != '\0')
	    nargs++;

    cons_command(c, nargs, args);

    if (msglen < c->ibytes)
    {
	memmove(c->ibuf, c->ibuf + msglen, c->ibytes - msglen);
	c->ibytes -= msglen;
    }
    else
    {
	c->ibytes = 0;
    }
    c->ibuf[c->ibytes] = '\0';
    return eOK;
}

void cons_prompt(CONS *c)
{
    cons_printf(c, "\nConsole>");
}

