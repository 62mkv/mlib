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
#include "cons_command.h"

typedef long (*CONSFUNC)(CONS *c, int argc, char *argv[]);

typedef struct
{
    int listnum;
    char *text;
    int submenu;
    CONSFUNC func;
    char *helptext;
    int privlevel;
} COMMANDS;

#define NOMENU         0
#define LIST_MENU      1
#define SET_MENU       2
#define SHOW_MENU      3
#define KILL_MENU      4
#define LOAD_MENU      5
#define FILTER_MENU    6
#define START_MENU     7
#define FLUSH_MENU     8

static COMMANDS all_commands[] = {
    {NOMENU, "quit", NOMENU, cons_Quit, "Close this console connection", PRIV_LUSER},
    {NOMENU, "exit", NOMENU, cons_Quit, "Close this console connection", PRIV_LUSER},
    {NOMENU, "echo", NOMENU, cons_Echo, "Turn echo mode on/off", PRIV_LUSER},
    {NOMENU, "password", NOMENU, cons_Password, "Enter privileged mode", PRIV_LUSER},
    {NOMENU, "shutdown", NOMENU, cons_Shutdown, "Shutdown", PRIV_ADMIN},

    {NOMENU, "list", LIST_MENU, cons_ListAll, "List various options", PRIV_ADMIN},
    {LIST_MENU, "connections", NOMENU, cons_ListConnections, "List client connections", PRIV_ADMIN},
    {LIST_MENU, "servers", NOMENU, cons_ListServers, "List server processes", PRIV_ADMIN},
    {LIST_MENU, "xref", NOMENU, cons_ListXref, "List client/server cross-referenced", PRIV_ADMIN},

    {NOMENU, "show", SHOW_MENU, (CONSFUNC)NULL, "Show various options", PRIV_ADMIN},
    {SHOW_MENU, "options", NOMENU, cons_ShowOptions, "Show options", PRIV_ADMIN},

    {NOMENU, "set",  SET_MENU, (CONSFUNC)NULL, "Set options", PRIV_ADMIN},
    {SET_MENU, "trace",  NOMENU, cons_SetTraceLevel, "Set trace level", PRIV_ADMIN},
    {SET_MENU, "min-servers",  NOMENU, cons_SetMinServers, "Set minimum number of servers", PRIV_ADMIN},
    {SET_MENU, "max-servers",  NOMENU, cons_SetMaxServers, "Set maximum number of servers", PRIV_ADMIN},
    {SET_MENU, "max-commands",  NOMENU, cons_SetMaxCommands, "Set maximum commands per server", PRIV_ADMIN},

    {NOMENU, "kill", KILL_MENU, (CONSFUNC)NULL, "Kill a client connection or server process", PRIV_ADMIN},
    {KILL_MENU, "connection", NOMENU, cons_KillConn, "Close a client connection", PRIV_ADMIN},
    {KILL_MENU, "server", NOMENU, cons_KillServer, "Kill a server process", PRIV_ADMIN},
    {KILL_MENU, "allservers", NOMENU, cons_KillAllServers, "Kill all server processes", PRIV_ADMIN},

    {-1}
};

static int sorthelp(const void *a, const void *b)
{
    return strcmp((*(COMMANDS **)a)->text, (*(COMMANDS **)b)->text);
}

static void LogCommand(CONSFUNC fptr, char **command, int argc, char **argv)
{
    int ii;

    /* There are some commands we don't want to log. */
    if (fptr == cons_Password)
	return;

    /* Build the command string from the argument vector. */
    for (ii=0; ii<argc; ii++)
    {
	misDynStrcat(command, argv[ii]);
	misDynStrcat(command, " ");
    }

    misLogInfo("Console Command: %s", *command);
}

static void StepThroughList(int submenu, 
	                    CONS *c, 
			    char **command,
			    int argc, 
			    char *argv[])
{
    COMMANDS *tmp;
    COMMANDS *matching[20];
    int nmatching = 0;
    int help = 0;
    int ii;

    if (argv[0][0] == '?')
	help = 1;

    for (tmp=all_commands; tmp->listnum != -1; tmp++)
    {
	if (tmp->listnum == submenu &&
	    (tmp->privlevel <= c->privlevel) &&
	    (help || 0 == misCiStrncmp(tmp->text, argv[0], strlen(argv[0]))))
	{
	    matching[nmatching++] = tmp;
	    if (nmatching > 20) break;
	}
    }

    if (help)
    {
	cons_printf(c, "Available options:\n");
	qsort(matching, nmatching, sizeof (COMMANDS *), sorthelp);
	for (ii=0;ii<nmatching;ii++)
	{
	    cons_printf(c,"%-20s - %s\n",matching[ii]->text,
			matching[ii]->helptext);
	}
    }

    else if (nmatching > 1)
    {
	cons_printf(c, "Ambiguous command\n");
    }

    else if (0 == nmatching)
    {
	cons_printf(c, "Unrecognized command\n");
	c->status = eUNKNOWN_CONS_COMMAND;
    }
    else
    {
	if (NOMENU != matching[0]->submenu && argc > 1)
	{
	    misDynStrcat(command, argv[0]);
	    misDynStrcat(command, " ");
	    StepThroughList(matching[0]->submenu, c, command, argc-1, argv+1);
	}
	else if (matching[0]->func)
	{
	    LogCommand(matching[0]->func, command, argc, argv);
	    c->status = (*matching[0]->func)(c, argc-1, argv+1);
	}
	else
	{
	    cons_printf(c,"Unrecognized command\n");
	    c->status = eUNKNOWN_CONS_COMMAND;
	}
    }
}

long cons_command(CONS *c, int argc, char *argv[])
{
    char *command = NULL;

    if (argc)
	StepThroughList(NOMENU, c, &command, argc, argv);

    free(command);

    return eOK;
}
