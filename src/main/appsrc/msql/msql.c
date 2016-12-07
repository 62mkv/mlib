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
 *  Copyright (c) 2002-2009
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
 *#END************************************************************************/

#define MAIN

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#ifdef UNIX
#include <unistd.h>
#else
#include <io.h>
#endif

#include <mocagendef.h>
#include <mocaerr.h>
#include <mcclib.h>
#include <oslib.h>
#include <mislib.h>
#include <sqllib.h>
#include <srvlib.h>

#include "msql.h"

static void PrintUsage()
{
    fprintf(stdout,

	    "Usage: msql [ -hv ] [ -a <url> | -S | -M ]\n"
	    "                    [ -u <user id> ] [ -w <password> ]\n"
	    "                    [ -k <client key> ]\n"
	    "                    [ @<pathname> ] \n");

    /* We purposely don't document the 'i' or 's' command line options. */    
    fprintf(stdout,
	    "\t-a <url>        URL to connect to in client mode\n"
	    "\t-u <user id>    Login user id for client mode\n"
	    "\t-w <password>   Login password for client mode\n"
	    "\t-k <client key> Client key for client mode\n"
	    "\t-S              Run in server mode\n"
	    "\t-M              Run in server mode (multi-threaded)\n"
	    "\t-h              Show help\n"
	    "\t-v              Show version information\n"
	    "\t@<pathname>     Pathname of script file to process\n");
    return;

}

int main(int argc, char **argv)
{
    int c, 
	ii;

    long status,
         errors = 0;

    char *url      = NULL,
	 *userid   = NULL, 
	 *password = NULL;

    char *clientKey = "msql";

    /* Set default behaivor. */
    gAutoCommit = 1;

    /* Handle command line arguments. */
    while ((c = osGetopt(argc, argv, "SMisa:u:w:k:vh?")) != -1)
    {
	switch (c)
	{
	case 'S':
	    gServerMode = 1;
	    break;
	case 'M':
	    gServerMode = 2;
	    break;
	case 'i':                  
	    gInstallMode = 1;  
	    gServerMode = 1;         /* Install mode implies server mode. */
	    break;
	case 's':                  
	    gSingleLineMode = 1;  
	case 'a':
	    url = osOptarg;
	    break;
	case 'u':
	    userid = osOptarg;
	    break;
	case 'w':
	    password = osOptarg;
	    break;
	case 'k':
	    clientKey = osOptarg;
	    break;
	case 'v':
	    fprintf(stdout, "%s\n", misGetVersionBanner(APPNAME));
	    osExit(EXIT_SUCCESS);
	case '?':
	case 'h':
	default:
	    PrintUsage();
	    osExit(EXIT_SUCCESS);
	}
    }

    argc -= osOptind - 1;
    argv += osOptind - 1;

    /* Try to get a URL if neither a URL nor server mode were specified. */
    if (!url && !gServerMode)
	url = osGetReg(REGSEC_SERVER, REGKEY_SERVER_URL);

    /* Validate our command line arguments. */
    if ((!url && !gServerMode) || (url && gServerMode))
    {
        PrintUsage();
	osExit(EXIT_SUCCESS);
    }

    /* Display a start banner. */
    PrintStartBanner( );

    /* Connect to the server. */
    status = Connect(url, userid, password, clientKey);
    if (status != eOK)
        osExit(EXIT_FAILURE);

    /* Deal with any scripts passed on the command line. */
    for (ii = 1; ii < argc; ii++)
    {
	if (argv[ii][0] == '@')
	{
	    if ((status = ProcessScript(argv[ii]+1)) != 0)
	    {
		errors = status;
		if (errors)
                    Print("%ld error(s) occurred executing script file\n", 
			   errors);
	    }
	}
    }

    /* Now start the interactive portion of our program. */
    if (!gQuit)
	errors += ProcessInput(stdin, url, userid, password, clientKey, 1);

    /* Commit any outstanding transactions and close any connections. */
    Close( );

    osExit(errors);
}
