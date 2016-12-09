static char RCS_Id[] = "$Id: servicemgr.c 393281 2011-11-08 15:25:31Z mlange $";
/*#START***********************************************************************
 *
 *  $URL: https://athena.redprairie.com/svn/prod/moca/branches/2011.2-dev/src/appsrc/servicemgr/servicemgr.c $
 *  $Revision: 393281 $
 *  $Author: mlange $
 *
 *  Description: Launcher for servicemgr.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>

#include <mocagendef.h>
#include <oslib.h>

#include "servicemgr.h"

#define APPNAME    "servicemgr"
#define CLASSNAME  "com.redprairie.moca.server.service.ServiceManager"

/*
 * The Service Manager code below is a little different in that we don't 
 * rely on the Java code to handle the entire application.  In order to not
 * require any boostrapping of the environment (PATH and CLASSPATH) to generate
 * an env.bat file we do that in C code.
 *
 * Therefore, the command line option processing below is done only to see
 * if we're being invoked to dump an env.bat.  
 */

void PrintUsage(void)
{
    printf("Usage: servicemgr [-a <application name>]\n"
           "                  [-e <environment name>]\n" 
           "                  [-s auto|manual]\n"
           "                  [-u <username> -p <password>]\n" 
           "                  [-t <timeout(seconds)>]\n"
           "                  <action>\n"
           "\twhere \"action\" must be one of: \n"
           "\t       install   - to install as a service\n"
           "\t       uninstall - to uninstall a service\n"
           "\t       start     - to start a service\n"
           "\t       stop      - to stop a service\n"
           "\t       dump      - to dump a batch file of all environment settings\n");
}

int main(int argc, char **argv)
{
    long c,
         status;

    char *action  = NULL, 
         *envname = NULL;

    /* Get the environment name from the command line. */
    while ((c = osGetopt(argc, argv, "a:e:s:u:p:t:h?")) != -1)
    {
        switch (c)
        {
            case 'e':
                envname = osOptarg;
                break;
            case 'h':
            case '?':
                PrintUsage();
                exit(0);
        }
    }

    /* Get the environment name from the environment if necessary. */
    if (!envname)
        envname = getenv(ENV_ENVNAME);

    /* Get the action from the command line if we have an argument left. */
    if (osOptind == (argc - 1)) 
        action = argv[argc-1];

    /* Dump the env.bat file or launch the Java service manager. */
    if (envname && action && misCiStrcmp(action, "dump") == 0)
    {
        status = DumpEnvironment(envname);
        exit(status);
    }

    /*
     * At this point we're about to spin up the Java code so we want to
     * do our best to ensure that the environment is actually boostrapped
     * enough to allow us to find the ServiceManager class.
     *
     * This means we need to make some assumptions.  We'll assume that
     * the environment is bootstrapped if the following environment variables
     * are set:
     *
     *     MOCA_ENVNAME
     *     MOCA_REGISTRY
     */
    if (!getenv(ENV_ENVNAME) || !getenv(ENV_REGISTRY))
    {
        printf("The environment does not appear to be bootstrapped yet.\n\n");
        PrintUsage( );
        exit(1);
    }

    exit(osLaunchJavaApplication(FALSE, APPNAME, CLASSNAME, argc, argv));
}
