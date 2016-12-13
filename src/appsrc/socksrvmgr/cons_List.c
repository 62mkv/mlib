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
#include <ctype.h>

#define TIMEFMT "%m/%d/%y %H:%M:%S"
#define NONINT_TIMEFMT "%Y%m%d%H%M%S"

static void encode_str(CONS *c, char *data)
{
    char *newptr;
    register char *p,*d;
    size_t len;

    for (p=data, len=0; *p; p++)
    {
	if (!isalnum(*p) && (*p != '+') && (*p != '_') && (*p != '-') && (*p != '.') && (*p != ' '))
	    len += 3;
	else
	    len += 1;
    }

    newptr = malloc(len+1);

    for (p=data,d=newptr; *p; p++)
    {
	if (!isalnum(*p) && (*p != '+') && (*p != '_') && (*p != '-') && (*p != '.') && (*p != ' '))
	{
	    sprintf(d,"%%%02X", (unsigned char)*p);
	    d+=3;
	}
	else
	{
	    *d++ = *p;
	}
    }
    *d = '\0';
    
    cons_printf(c, "%s", newptr);
    free(newptr);
}

static void encode_time(CONS *c, OS_TIME *time)
{
    char tmp[100];
    strftime(tmp, sizeof tmp, NONINT_TIMEFMT, localtime((time_t *)&time->sec));
    cons_printf(c, "%s", tmp);
}

long cons_ListConnections(CONS *c, int argc, char *argv[])
{
    CONN *conn;
    SERV *s;
    char startbuf[100];
    char lastbuf[100];
    char addrbuf[100];
    unsigned short portno;
    int shownames=0;
    char *fmt;

    cons_printf(c, "Listing client connections...\n\n");
    
    if (argc > 0 && 0 == strcmp(argv[0], "-n"))
	shownames=1;

    fmt = TIMEFMT;
    cons_printf(c, 
            "FD  Client Address  Port   Time Connected    Last Command      Count   PID\n"
	    "--- --------------- ------ ----------------- ----------------- ------- -----\n");

    for (s=ServBusyFirst;s;s=s->next)
    {
	if (NULL != (conn = s->connection))
	{
	    strftime(startbuf, sizeof startbuf, fmt,
		     localtime(&conn->info.connected));
	    strftime(lastbuf, sizeof lastbuf, fmt,
		     localtime(&conn->info.busy));
	    addrbuf[0] = '\0';
	    osSockAddress(conn->fd, addrbuf, sizeof addrbuf, &portno);
            if (shownames)
            {
                char *name;
                name = osTCPAddrToName(addrbuf);
                if (name && misTrimLen(name, 10))
                {
                    strncpy(addrbuf, name, sizeof addrbuf);
                    addrbuf[sizeof addrbuf - 1] = '\0';
                }
            }

	    cons_printf(c, "%-3d %-15.15s %-6d %-17s %-17s %-7d %d\n",
			    conn->fd, addrbuf, (int) portno, startbuf, lastbuf,
			    conn->info.commands, s->pid);
	}
    }

    for (conn=ConnIdleTop;conn;conn=conn->next)
    {
	strftime(startbuf, sizeof startbuf, fmt,
		 localtime(&conn->info.connected));
	strftime(lastbuf, sizeof lastbuf, fmt,
		 localtime(&conn->info.busy));
	addrbuf[0] = '\0';
	osSockAddress(conn->fd, addrbuf, sizeof addrbuf, &portno);
	if (shownames)
	{
	    char *name;
	    name = osTCPAddrToName(addrbuf);
	    if (name && misTrimLen(name, 10))
	    {
		strncpy(addrbuf, name, sizeof addrbuf);
		addrbuf[sizeof addrbuf - 1] = '\0';
	    }
	}

	cons_printf(c, "%-3d %-15.15s %-6d %-17s %-17s %-7d %s\n",
			conn->fd, addrbuf, (int) portno, startbuf, lastbuf,
			conn->info.commands, "Idle");
    }

    for (conn=ConnPendingFirst;conn;conn=conn->next)
    {
	strftime(startbuf, sizeof startbuf, fmt,
		 localtime(&conn->info.connected));
	strftime(lastbuf, sizeof lastbuf, fmt,
		 localtime(&conn->info.busy));
	addrbuf[0] = '\0';
	osSockAddress(conn->fd, addrbuf, sizeof addrbuf, &portno);
	if (shownames)
	{
	    char *name;
	    name = osTCPAddrToName(addrbuf);
	    if (name && misTrimLen(name, 10))
	    {
		strncpy(addrbuf, name, sizeof addrbuf);
		addrbuf[sizeof addrbuf - 1] = '\0';
	    }
	}
	cons_printf(c, "%-3d %-15.15s %-6d %-17s %-17s %-7d %s\n",
			conn->fd, addrbuf, (int) portno, startbuf, lastbuf,
			conn->info.commands, "Wait");
    }

    return eOK;
}

long cons_ListServers(CONS *c, int argc, char *argv[])
{
    SERV *s;
    char startbuf[100];
    char lastbuf[100];
    char *fmt;
    OS_TIME ts;
    char *value;

    cons_printf(c, "Listing servers...\n\n");

    fmt = TIMEFMT;
    cons_printf(c,
	    "PID     Time Started      Last Command      Count   Conn \n"
	    "------- ----------------- ----------------- ------- -----\n");

    for (s=ServFreeTop;s;s=s->next)
    {
	strftime(startbuf,sizeof startbuf,fmt,localtime(&s->info.created));
	strftime(lastbuf,sizeof lastbuf,fmt,localtime(&s->info.busy));
	cons_printf(c, "%-7d %-17s %-17s %-7d %d\n",
			s->pid, startbuf, lastbuf, s->info.count,
			s->connection?s->connection->fd:0);
    }

    for (s=ServBusyFirst;s;s=s->next)
    {
	strftime(startbuf,sizeof startbuf,fmt,localtime(&s->info.created));
	strftime(lastbuf,sizeof lastbuf,fmt,localtime(&s->info.busy));
	cons_printf(c, "%-7d %-17s %-17s %-7d %d\n",
			s->pid, startbuf, lastbuf, s->info.count,
			s->connection?s->connection->fd:0);
    }

    return eOK;
}


long cons_ListXref(CONS *c, int argc, char *argv[])
{
    CONN *conn;
    SERV *s;
    char startbuf[100];
    char lastbuf[100];
    char addrbuf[100];
    unsigned short portno;
    int shownames=0;
    char *fmt;
    OS_TIME ts;
    char *value;

    if (argc > 0 && 0 == strcmp(argv[0], "-n"))
	shownames=1;

    fmt = TIMEFMT;
    cons_printf(c, "Listing client/server cross-reference...\n\n");

    cons_printf(c, 
	    "PID     Time Started      Last Command      Count  \n"
	    "-->FD  Client Address  Port   Time Connected    Last Command      Count  \n"
	    "-------------------------------------------------------------------------\n");

    for (s=ServBusyFirst;s;s=s->next)
    {
	strftime(startbuf,sizeof startbuf,fmt,localtime(&s->info.created));
	strftime(lastbuf,sizeof lastbuf,fmt,localtime(&s->info.busy));
	cons_printf(c, "%-7d %-17s %-17s %-7d\n",
			s->pid, startbuf, lastbuf, s->info.count);

	if (NULL != (conn = s->connection))
	{
	    strftime(startbuf, sizeof startbuf, fmt,
		     localtime(&conn->info.connected));
	    strftime(lastbuf, sizeof lastbuf, fmt,
		     localtime(&conn->info.busy));
	    addrbuf[0] = '\0';
	    osSockAddress(conn->fd, addrbuf, sizeof addrbuf, &portno);
            if (shownames)
            {
                char *name;
                name = osTCPAddrToName(addrbuf);
                if (name && misTrimLen(name, 10))
                {
                    strncpy(addrbuf, name, sizeof addrbuf);
                    addrbuf[sizeof addrbuf - 1] = '\0';
                }
            }
            cons_printf(c, "-->%-3d %-15.15s %-6d %-17s %-17s %-7d\n",
			    conn->fd, addrbuf, (int) portno, startbuf, lastbuf,
			    conn->info.commands);
	}
    }

    return eOK;
}

long cons_ListAll(CONS *c, int argc, char *argv[])
{
    cons_ListServers(c, argc, argv);
    cons_printf(c, "\n");
    cons_ListConnections(c, argc, argv);

    return eOK;
}

