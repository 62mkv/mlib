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

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include <time.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <oslib.h>
#include <srvlib.h>

#define APPNAME "Socket Server Manager"

#define SERVER_PROGRAM "socksrvprc"

#define PRIV_LUSER 0
#define PRIV_ADMIN 1

#define SEND_OK    0
#define SEND_WAIT  1
#define SEND_ERROR 2

typedef struct serv SERV;
typedef struct conn CONN;
typedef struct cons CONS;

/* Connection Info Structure */
struct conn
{
   int closing;
   SOCKET_FD fd;
   OS_TCP_ADDR addr;
   SERV *server;
   struct
   {
       time_t connected;
       time_t busy;
       time_t idle;
       long commands;
   } info;
   CONN *next, *prev;
};

/* Server Info Structure */
struct serv
{
   int closing:1;
   int shutdown:1;
   int id;
   DWORD dwPid;
   HANDLE hPipe;
   HANDLE hProcess;
   HANDLE hThread;
   HANDLE hEvent;
   HANDLE hServerEvent;
   HANDLE hThreadEvent;
   int threadno;
   CONN *connection;
   time_t busytime;
   struct
   {
       time_t created;
       time_t busy;
       long count;
   } info;
   SERV *next, *prev;
};

/* Console Info Structure */
struct cons
{
   SOCKET_FD fd;
   char *ibuf,*obuf;
   int ibytes,obytes;
   int closing;
   int echo;
   int privlevel;
   int status;
   int interactive;
   CONS *next, *prev;
};

struct params
{
   unsigned short port;
   unsigned short console_port;
   unsigned long conn_timeout;
   int min_servers;
   int max_servers;
   int max_commands;
   char trace_level[20];
   char *console_section;
   char *console_password;
   char *server_command;
   int nopts;
   char opts[20][100];
};

#ifdef MAIN
  #define STORAGE_CLASS
#else
  #define STORAGE_CLASS extern
#endif

/* Set up our global variables */
STORAGE_CLASS struct params param;

STORAGE_CLASS CONN *ConnIdleTop;
STORAGE_CLASS CONN *ConnPendingFirst, *ConnPendingLast;
STORAGE_CLASS SERV *ServFreeTop;
STORAGE_CLASS int ServFreeCount;
STORAGE_CLASS int ServBusyCount;
STORAGE_CLASS SERV *ServBusyFirst,*ServBusyLast;
STORAGE_CLASS WSAEVENT socket_event, pending_event;

STORAGE_CLASS CRITICAL_SECTION listCrit;

#include "proto.h"
