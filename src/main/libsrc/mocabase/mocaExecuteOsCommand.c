static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Component to execute an o/s command.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2002
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

#define MOCA_ALL_SOURCE

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef UNIX
#include <signal.h>
#endif

#ifndef WIN32
#include <sys/wait.h>
#endif

#include <mocaerr.h>
#include <oslib.h>
#include <srvlib.h>

#define BUFFER_L 4096


LIBEXPORT 
RETURN_STRUCT *mocaExecuteOsCommand(char *command, char* directory)
{
    long status;
    char buffer[BUFFER_L + 1];
    char *cwd = NULL;
    FILE *inpipe;
    RETURN_STRUCT *ret;

#ifdef UNIX
    struct sigaction act,
		     oact;

    /* Make sure our sigact structs are clean. */
    memset(&act,  0, sizeof act);
    memset(&oact, 0, sizeof oact);

    /* 
     *  Make sure the signal handler for child signals is set right. 
     *
     *  We have to set the default signal handler for child signals
     *  so that the pclose( ) call will be able to get the status of
     *  the command being executed.  If we don't, the child process
     *  is ignored when it exits because the mocaconmgr code changes
     *  the signal to be ignored and the pclose( ) call fails because
     *  it thinks there was never a child process.
     */
    act.sa_handler = SIG_DFL;
    act.sa_flags = 0;
    sigemptyset(&act.sa_mask);

    sigaction(SIGCHLD, &act, &oact);
#endif

    /* If we were given a directory to run in we have to change to it first
     * and keep a backup of what directory we are in before that
     */
    if (directory)
    {
#ifdef UNIX
        cwd = getcwd(NULL, 0);
        status = chdir(directory);
#else
        cwd = _getcwd(NULL, 0);
        status = _chdir(directory);
#endif
        if (status == -1)
        {
            ret = srvErrorResults(osErrno( ), 
	                      "chdir: ^strerror^", "strerror", 
			      COMTYP_CHAR, osError( ), 0,
			      NULL);

            goto cleanup;
        }
    }

    /* Initialize our return struct. */
    ret = srvResultsInit(eOK, "result", COMTYP_CHAR, 0, NULL);

    /* Open a pipe to the command. */ 
    if ((inpipe = (FILE *) osPopen(command, "r")) == NULL)
    {
        srvFreeMemory(SRVRET_STRUCT, ret);
        ret = srvErrorResults(osErrno( ), 
	                      "osPopen: ^strerror^", "strerror", 
			      COMTYP_CHAR, osError( ), 0,
			      NULL);
        goto cleanup;
    }

    /* Cycle through each line of output from the command. */
    while (fgets(buffer, BUFFER_L, inpipe))
        srvResultsAdd(ret, buffer);

    /* Close the pipe to the command. */ 
    status = osPclose(inpipe);
    if (status == -1)
    {
        srvFreeMemory(SRVRET_STRUCT, ret);
        ret = srvErrorResults(osErrno( ), 
	                      "osPclose: ^strerror^", "strerror", 
			      COMTYP_CHAR, osError( ), 0,
			      NULL);
        goto cleanup;
    }

#ifndef UNIX
    /* Set the exit status of the command in the return struct. */
    srvSetReturnStatus(ret, status);
#else
    /* Get the exit status of the command and set it in the return struct. */
    status = WEXITSTATUS(status);
    srvSetReturnStatus(ret, status);
#endif

cleanup:

    /* If we had the current directory make sure to change back to it */
    if (cwd != 0) {
#ifdef UNIX
        status = chdir(cwd);
#else
        status = _chdir(cwd);
#endif
        /* We shouldn't have to worry about not being able to change
         * back to the directory since we were already there.
         * So all we need to do is free the memory.
         */
        free(cwd);
    }
#ifdef UNIX
    /* Put back the original signal handler. */
    sigaction(SIGCHLD, &oact, NULL);
#endif

    return ret;
}
