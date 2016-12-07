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

/* console.c */
long cons_read(CONS *c);
long cons_printf(CONS *c, char *format, ...);
long cons_sync(CONS *c);
int cons_process(CONS *c);
long cons_command(CONS *c, int argc, char *argv[]);
void cons_prompt(CONS *c);
int cons_line(char *buf);

/* environment.c */
void set_environment(char *process);

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
void AddCons(CONS *cons);
void DelCons(CONS *cons);
void MoveConnPending(CONN *conn);
void DetachConnPending(CONN *conn);

/* server.c */
int fork_servers(int num_servers);
int send_server(CONN *conn);
int shutdown_server(SERV *serv);
int shutdown_idle(void);
int shutdown_busy(void);

/* signal.c */
void InitializeSignalHandling(void);
void ResetChildSignalHandler(void);

