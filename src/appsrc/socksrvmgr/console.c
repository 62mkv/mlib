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

#include "socksrvmgr.h"

#include <stdarg.h>

long cons_read(CONS *c)
{
    char buffer[5000]; /* this isn't right, but how should we do it? */
    int i,nbytes;

    nbytes = osSockRecv(c->fd, buffer, sizeof buffer, OS_SOCK_NOWAIT);
    if (nbytes == SOCKET_ERROR)
    {
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
    int bufsize;
    va_list ap;


    if (!c || !c->fd)
    {
	va_start(ap,format);
	vprintf(format, ap);
	va_end(ap);
	return eOK;
    }
    else
    {
	va_start(ap,format);
	vsprintf(tmpbuffer, format, ap);
	va_end(ap);
	bufsize = translate_crlf(tmpbuffer, buffer);

	c->obytes += bufsize;
	misDynStrcat(&c->obuf, buffer);
	return cons_sync(c);
    }
}

long cons_sync(CONS *c)
{
   int nbytes;

   if (!c) return eOK;

   if (c->obytes)
   {
       nbytes = osSockSend(c->fd, c->obuf, c->obytes, OS_SOCK_NOWAIT);
       if (nbytes == SOCKET_ERROR)
       {
	   if (osSockErrno() != OS_EAGAIN)
	   {
	       misLogError("Error from osSockSend: %s", osSockError());
	       return eERROR;
	   }
       }
       else if (nbytes <= c->obytes)
       {
	   c->obytes -= nbytes;

	   if (c->obytes)
	   {
	       memmove(c->obuf, c->obuf+nbytes, c->obytes);
	       c->obuf = realloc(c->obuf, c->obytes);
	   }
	   else
	   {
	       free(c->obuf);
	       c->obuf = NULL;
	   }
       }
    }
    return eOK;
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
    int msglen;
    char *args[100];
    int nargs;
    char *bufptr;

    if (!c || !c->ibuf || !c->ibytes)
	return eERROR;

    misTrc(T_MGR, "Processing console activity...");

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
