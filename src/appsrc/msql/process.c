static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
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

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mcclib.h>
#include <mislib.h>
#include <oslib.h>
#include <sqllib.h>
#include <srvlib.h>

#include "msql.h"

static int FirstWord(char *string, char *word, char **arg)
{
    char *s, *w;

    s = string;
    w = word;
    *arg = NULL;

    while (*s && *w && tolower(*s) == tolower(*w))
    {
	s++;
	w++;
    }

    if (*w)
	return 0;

    if (!*s)
	return 1;

    if (isspace(*s))
    {
	*arg = s + strspn(s, " \t\n\r");
	return 1;
    }

    return 0;
}

long ProcessInput(FILE *fp, char *url, char *userid, char *password, char *clientKey, int prompt)
{
    int nread    = 0,
        lastRead = 0;

    long errors  = 0,
         linenum = 0,
         status  = eOK;

    char *arg         = NULL,
         *command     = NULL,
         *tempCommand = NULL;

    char buffer[10240];

    while (1)
    {
        /* Display a prompt for the user. */
        if (prompt)
            PrintPrompt(linenum);

        lastRead = nread;
	memset(buffer, '\0', sizeof buffer);

        /* Read a line from input. */
        nread = ReadInput(buffer, sizeof buffer, fp);
	if (nread < 0)
	    break;

	/* Skip this comment. */
	if (buffer[0] == '#')
	{
	    ;
	}

	/* Some commands are only valid on the first line. */
	else if (linenum == 0)
	{
	    /* Exit */
	    if (misCiStrcmp(buffer, "EXIT") == 0 ||
	        misCiStrcmp(buffer, "Q")    == 0 ||
	        misCiStrcmp(buffer, "QUIT") == 0 )
	    {
		gQuit = 1;
		goto cleanup;
	    }

	    /* MSET commands */
	    else if (FirstWord(buffer, "MSET", &arg))
	    {
		if (!misCiStrcmp(buffer, "MSET AUTOCOMMIT ON"))
		{
		    SetAutoCommit(1);
		}
		else if (!misCiStrcmp(buffer, "MSET AUTOCOMMIT OFF"))
		{
		    SetAutoCommit(0);
		}
		else if (!misCiStrncmp(buffer, "MSET ENVIRONMENT", 16))
		{
		    SetEnvironment(buffer + 17);
		}
		else if (!misCiStrcmp(buffer, "MSET SPOOL OFF"))
		{
		    StopSpooling( );
		}
		else if (!misCiStrncmp(buffer, "MSET SPOOL", 10))
		{
		    StartSpooling(buffer + 11);
		}
		else
		{
			errors++;
		    Print("\n");
		    Print("Invalid mset option, valid options are: \n");
		    Print("\n");
		    Print("     mset autocommit on | off\n");
		    Print("     mset environment <string> | off\n");
		    Print("     mset spool [ file | off ]\n");
		    Print("\n");
		}
	    }

	    /* Describe database table. */
	    else if (FirstWord(buffer, "DESC", &arg))
	    {
		DescribeTable(arg);
	    }

	    /* Edit the current command. */
	    else if ((FirstWord(buffer, "EDIT", &arg) ||
		      FirstWord(buffer, "ED", &arg)))
	    {
		errors += EditCommand(arg ? atoi(arg) : 1);
	    }

	    /* List a specific command from history. */
	    else if (FirstWord(buffer, "L", &arg))
	    {
		ListHistory(arg ? atoi(arg) : 1, 1);
	    }

	    /* List a range of commands from history. */
	    else if (FirstWord(buffer, "H", &arg) ||
		     FirstWord(buffer, "HIST", &arg) ||
		     FirstWord(buffer, "HISTORY", &arg))
	    {
		ListHistory(1, arg ? atoi(arg) : 0);
	    }

	    /* Execute the last command from history. */
	    else if (FirstWord(buffer, "/", &arg))
	    {
		/* Print the command if it's an older one. */
		if (arg && atoi(arg) != 1)
		    ListHistory(atoi(arg), 1);

		command = GetHistory(arg ? atoi(arg) : 1);
	    }

            /* Execute the given script. */
            else if (strncmp(buffer, "@", 1) == 0)
            {
                errors = ProcessScript(buffer + 1);
            }

	    /* Execute a shell command. */
	    else if (strncmp(buffer, "!", 1) == 0)
	    {
		errors += ExecuteShellCommand(buffer + 1);
	    }

	    /* Got a blank line - do nothing. */
	    else if (strlen(buffer) == 0)
	    {
		;
	    }

	    /* Got a command and we're running in single-line mode - execute. */
	    else if (gSingleLineMode)
	    {
		misDynStrcpy(&tempCommand, buffer);
	        PutHistory(tempCommand);
	        command = tempCommand;
	    }

	    /* Got something - add it to the command we're building. */
	    else
	    {
		misDynStrcpy(&tempCommand, buffer);
		linenum++;
	    }
	}

	/* Execute the current command we've built. */
	else if (strcmp(buffer, "/") == 0)
	{
	    PutHistory(tempCommand);
	    command = tempCommand;
	    linenum = 0;
	}

	/* Got a blank line - put the current command into the history. */
	else if (strlen(buffer) == 0)
	{
	    PutHistory(tempCommand);
	    free(tempCommand);
	    tempCommand = NULL;
	    linenum = 0;
	}

	/* Got something - add it to the command we're building. */
	else
	{
            if (lastRead != sizeof buffer - 1)
	        misDynStrcat(&tempCommand, "\n");
	    misDynStrcat(&tempCommand, buffer);
	    linenum++;
	}

	/* If we have a command to execute, do it. */
	if (command && strlen(command) )
	{
	    RETURN_STRUCT *ret = NULL;

	    PrintStatusStart( );

	    /* Execute the command. */
	    status = ExecuteCommand(command, &ret);
		if (status != eOK &&
			status != eDB_NO_ROWS_AFFECTED &&
			status != eSRV_NO_ROWS_AFFECTED)
			errors++;

	    /* We commit or rollback based on the status. */
            if (status == eOK)
		Commit( );
            else
		Rollback( );

	    /* Print any results from the command. */
	    PrintStatusDone(ret);
            PrintHeadings(ret);
            PrintResults(ret);
	    PrintRowsAffected(ret);

            srvFreeMemory(SRVRET_STRUCT, ret);

	    command = NULL;

	    free(tempCommand);
	    tempCommand = NULL;

        /* Sessionkey-idle-timeout was reached */
        if (status == 523 && prompt)
            Connect(url, userid, password, clientKey);

	}
    }

cleanup:

    if (tempCommand)
    {
        PutHistory(tempCommand);
        free(tempCommand);
    }

    return errors;
}
