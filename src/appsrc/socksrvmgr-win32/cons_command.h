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

/*
 * These should always be available, no matter whether the
 * password's been entered.
 */
long cons_Echo(CONS *c, int argc, char *argv[]);
long cons_Quit(CONS *c, int argc, char *argv[]);
long cons_Password(CONS *c, int argc, char *argv[]);

/*
 * Kill Servers
 */
long cons_KillConn(CONS *c, int argc, char *argv[]);
long cons_KillServer(CONS *c, int argc, char *argv[]);
long cons_KillAllServers(CONS *c, int argc, char *argv[]);

/*
 * List Servers and Connections
 */
long cons_ListConnections(CONS *c, int argc, char *argv[]);
long cons_ListServers(CONS *c, int argc, char *argv[]);
long cons_ListAll(CONS *c, int argc, char *argv[]);
long cons_ListXref(CONS *c, int argc, char *argv[]);

/*
 * Set various options and flags.
 */
long cons_SetMinServers(CONS *c, int argc, char *argv[]);
long cons_SetMaxServers(CONS *c, int argc, char *argv[]);
long cons_SetMaxCommands(CONS *c, int argc, char *argv[]);
long cons_SetTraceLevel(CONS *c, int argc, char *argv[]);

/*
 * Miscellaneous commands.
 */
long cons_ShowOptions(CONS *c, int argc, char *argv[]);
long cons_Shutdown(CONS *c, int argc, char *argv[]);
