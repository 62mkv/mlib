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
#ifdef UNIX
# include <termios.h>
#else
# include <oslib.h>
#endif

#include <mislib.h>

#include "msql.h"

#ifdef UNIX

void SetEchoMode(int on)
{
    struct termios tempio, origio;

    tcgetattr(0, &tempio);
    origio = tempio;
    if (on)
	tempio.c_lflag |= ECHO;
    else
	tempio.c_lflag &= ~ECHO;

    tcsetattr(0, TCSANOW, &tempio);
}

#else

void SetEchoMode(int on)
{
    HANDLE hConsole;
    DWORD Mode;

    hConsole = GetStdHandle(STD_INPUT_HANDLE);
    if (GetConsoleMode(hConsole, &Mode))
    {
	if (on)
	    SetConsoleMode(hConsole, Mode | ENABLE_ECHO_INPUT);
	else
	    SetConsoleMode(hConsole, Mode & (~ENABLE_ECHO_INPUT));
    }
}

#endif

long ReadInput(char *command, int maxlen, FILE *fp)
{
    char *ptr;

    memset(command, 0, maxlen);

    /* Get a line from the intput file. */
    ptr = fgets(command, maxlen, fp);
    if (!ptr)
	return -1;

    /* Trim any trailing space or non-printables. */
    misTrim(command);

    /* Write the command to the spool file. */
    Spool(command);

    return strlen(command);
}
