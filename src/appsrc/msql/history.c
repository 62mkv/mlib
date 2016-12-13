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

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>

#include "msql.h"

#define MAX_HIST_ENTRIES 50

#define next_spot ((current_pos + 1) % MAX_HIST_ENTRIES)
#define last_spot ((current_pos + MAX_HIST_ENTRIES - 1) % MAX_HIST_ENTRIES)
#define spot(num) ((current_pos + MAX_HIST_ENTRIES - (num) + 1) % MAX_HIST_ENTRIES)

static int current_pos;

static char *command_history[MAX_HIST_ENTRIES];

static void sPrintCommand(int pos, char *command)
{
    char *ptr = command;

    while (*ptr)
    {
	/* Print the history position for the first line, but indent others. */
        if (ptr == command)
            Print("%3d :  ", pos);
        else
            Print("       ");

	/* Print each character for this line. */
        while (*ptr && *ptr != '\r' && *ptr != '\n')
            Print("%c", *ptr++);

        Print("\n");

	/* Skip over carriage returns and line feeds. */
        while (*ptr && (*ptr == '\r' || *ptr == '\n'))
	    ptr++;
    }
}

char *GetHistory(int pos)
{
    return command_history[spot(pos)];
}

void PutHistory(char *command)
{
    static int done_atexit;

    current_pos = next_spot;

    misDynStrcpy(&command_history[current_pos], command);

    if (!done_atexit)
    {
	done_atexit = 1;
	osAtexit(FreeHistory);
    }
}

void ListHistory(int start, int count)
{
    int ii;

    if (!count)
	count = MAX_HIST_ENTRIES;

    if (command_history[spot(start)])
        Print("\n");

    for (ii = start; ii <= MAX_HIST_ENTRIES && ii < (start + count); ii++)
    {
	if (!command_history[spot(ii)])
	    break;

        sPrintCommand(ii, command_history[spot(ii)]);
    }

    Print("\n");
}

void FreeHistory(void)
{
    int i;

    for (i=0; i< MAX_HIST_ENTRIES; i++)
    {
	if (command_history[i])
        {
	    free(command_history[i]);
            command_history[i] = NULL;
        }
    }
}
