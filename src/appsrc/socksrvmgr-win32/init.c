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

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <oslib.h>

#include "socksrvmgr.h"

static void PrintUsage(void)
{
    fprintf(stderr,
            "Usage: socksrvmgr -S <section> [ -p <port> ] \n"
                "\t-S <section>           Registry section name\n"
                "\t-p <port>              Listen on port \n"
                "\t-o <trace file>        Trace file pathname\n"
                "\t-t <trace levels>      Trace level switches\n"
                "\t-h                     Show help\n"
                "\t-v                     Show version information\n"
                "%s",
                misGetTraceOptionsString());
}

static void GetRegistryValues(void)
{
    char *ptr;

    char *section = param.console_section;

    /* Get the listen port number. */
    ptr = osGetReg(section, REGKEY_SOCKMGR_PORT);
    param.port = ptr ? (unsigned short) atol(ptr) : DEFAULT_SOCKMGR_PORT;
    param.console_port = param.port + 1;

    /* Get the console password. */
    ptr = osGetRegNotExpanded(section, REGKEY_SOCKMGR_CONSOLE_PASSWORD);
    param.console_password = ptr ? ptr : DEFAULT_SOCKMGR_CONSOLE_PASSWORD;

    /* Get the server command. */
    ptr = osGetReg(section, REGKEY_SOCKMGR_SERVER_COMMAND);
    param.server_command = ptr;

    /* Get the min number of servers. */
    ptr = osGetReg(section, REGKEY_SOCKMGR_MIN_POOL_SIZE);
    param.min_servers = ptr ? atol(ptr) : DEFAULT_SOCKMGR_MIN_POOL_SIZE;

    /* Get the max number of servers. */
    ptr = osGetReg(section, REGKEY_SOCKMGR_MAX_POOL_SIZE);
    param.max_servers = ptr ? atol(ptr) : DEFAULT_SOCKMGR_MAX_POOL_SIZE;

    /* Get the max commands. */
    ptr = osGetReg(section, REGKEY_SOCKMGR_MAX_COMMANDS);
    param.max_commands = ptr ? atol(ptr) : DEFAULT_SOCKMGR_MAX_COMMANDS;

    /* Get the client timeout value in seconds. */
    ptr = osGetReg(section, REGKEY_SOCKMGR_CLIENT_TIMEOUT);
    param.conn_timeout = ptr ? atol(ptr) : DEFAULT_SOCKMGR_CLIENT_TIMEOUT;
}

void InitializeParameters(int argc, char *argv[])
{
    int c;

    extern char *osOptarg;

    /*
     * We need to get the registry section name before doing anything.
     */
    while ((c = osGetopt(argc, argv, "S:p:t:o:vh?")) != -1)
    {
        switch (c)
        {
        case 'S':
            param.console_section = osOptarg;
            break;
        }
    }

    if (!param.console_section)
    {
        PrintUsage( );
        exit(EXIT_FAILURE);
    }

    osOptind=1; 

    /*
     * First get run-time parameters from the registry.
     */
    GetRegistryValues( );

    /* We can get ourselves into trouble if a command isn't defined. */
    if (!param.server_command || !strlen(param.server_command))
    {
        fprintf(stderr, "A command must be defined in the registry - exiting...\n");
        exit(EXIT_FAILURE);
    }

    /* We should make sure the server pool size is kosher. */
    if (param.min_servers > param.max_servers)
    {
        fprintf(stderr, "ERROR: min_pool_size must be greater than max_pool_size\n");
        exit(EXIT_FAILURE);
    }

    /*
     * Now get command line parameters, which can override registry values.
     */
    while ((c = osGetopt(argc, argv, "S:p:t:o:vh?")) != -1)
    {
        switch (c)
        {
        case 'S':
            /* Handled above */
            break;
        case 'p':
            param.port = (unsigned short) atoi(osOptarg);
            param.console_port = param.port + 1;
            break;
        case 't':
            misSetTraceLevelFromArg(osOptarg);
            strcpy(param.trace_level, osOptarg);
            break;
        case 'o':
            misSetTraceFile(osOptarg, "a+");
            sprintf(param.opts[param.nopts++], "-o%s", osOptarg);
            break;
        case 'v':
            printf(misGetVersionBanner(APPNAME));
            exit(EXIT_SUCCESS);
        case 'h':
        case '?':
        default:
            PrintUsage( );
            exit(EXIT_FAILURE);
        }
    }
}
