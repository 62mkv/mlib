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
 *#END************************************************************************/

/* console.c */
HANDLE console_thread(SOCKET fd);
long cons_read(CONS *c);
long cons_printf(CONS *c, char *format, ...);
long cons_sync(CONS *c);
int cons_process(CONS *c);
long cons_command(CONS *c, int argc, char *argv[]);
void cons_prompt(CONS *c);
int cons_line(char *buf);
int read_config(char *cfgfile);

/* environment.c */
char *get_environment_strings(char *process);

/* init.c */
void InitializeParameters(int argc, char *argv[]);

/* list.c */
void AddConnIdle(CONN *conn);
void DelConnIdle(CONN *conn);
void MoveConnIdle(CONN *conn);
void DetachConnIdle(CONN *conn);
void AddServFree(SERV *serv);
void DelServFree(SERV *serv);
void DelServBusy(SERV *serv);
void MoveServFree(SERV *serv);
void MoveServBusy(SERV *serv);
void MoveConnPending(CONN *conn);
void DetachConnPending(CONN *conn);

/* server.c */
HANDLE serv_thread(int threadno, HANDLE hEvent);
BOOL fork_servers(int num_servers);
BOOL send_server(CONN *conn);
BOOL shutdown_server(SERV *serv);
BOOL shutdown_idle(void);
BOOL shutdown_busy(void);

/* signal.c */
void InitializeSignalHandling(void);
