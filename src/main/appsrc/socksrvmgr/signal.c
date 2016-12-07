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

#include "socksrvmgr.h"

#include <signal.h>
#include <sys/types.h>
#include <sys/wait.h>

static struct sigaction sigactINT,
			sigactTERM;

static void HandleSignal(int signo)
{
    fprintf(stderr, "socksrvmgr: Caught signal %d - exiting...\n", signo);
    fflush(stderr);

    /* Call any lower level signal handler that may be registered. */
    switch (signo)
    {
        case SIGINT:
            if (sigactINT.sa_handler)
                sigactINT.sa_handler(signo);
            break;
        case SIGTERM:
            if (sigactTERM.sa_handler)
                sigactTERM.sa_handler(signo);
            break;
    }

    exit(EXIT_SUCCESS);
}

/*
 * Handle Signals.  We have to handle the SIGCHLD and SIGPIPE signals.  We
 * COULD simply ignore the SIGCLD signal, but that one isn't defined by
 * POSIX, while the SIGCHLD (note the H) is.  The SIGCLD behavior, IMHO,
 * is a crock anyway.
 */
void InitializeSignalHandling(void)
{
    struct sigaction chld_action,
                     pipe_action,
                     cleanup_action;

    /*
     * We want to reap zombies, so handle SIGCHLD.
     */
    sigemptyset(&chld_action.sa_mask);
    chld_action.sa_handler = SIG_IGN;
    chld_action.sa_flags   = SA_NOCLDSTOP;

    sigaction(SIGCHLD, &chld_action, NULL);

    /*
     * Ignore SIGPIPE.
     *
     * It's possible we could have a dead child we don't know about and
     * we'd get blown away by SIGPIPE.  Let's take steps to avoid that sort
     * of thing.
     */
    sigemptyset(&pipe_action.sa_mask);
    pipe_action.sa_handler = SIG_IGN;
    pipe_action.sa_flags   = 0;

    sigaction(SIGPIPE, &pipe_action, NULL);

    /*
     * Handle SIGTERM and SIGINT by cleaning up ourselves.
     */
    sigemptyset(&cleanup_action.sa_mask);
    cleanup_action.sa_handler = HandleSignal;
    cleanup_action.sa_flags   = 0;

    sigaction(SIGINT,  &cleanup_action, &sigactINT);
    sigaction(SIGTERM, &cleanup_action, &sigactTERM);
}

/*
 * This function is expected to be called (by the child process) after
 * calling fork(), but before the call to exec(). It is necessary to reset
 * the signal handler to SIG_DFL on Linux, due to some strange interpretation
 * of the behavior of that signal and its impact on any library function
 * that uses the wait family of calls. (e.g. system, popen/pclose)
 */
void ResetChildSignalHandler(void)
{
    struct sigaction chld_action;

    /*
     * Restore the SIGCHLD handler to SIG_DFL
     */
    sigemptyset(&chld_action.sa_mask);
    chld_action.sa_handler = SIG_DFL;
    chld_action.sa_flags   = 0L;

    sigaction(SIGCHLD, &chld_action, NULL);
}
