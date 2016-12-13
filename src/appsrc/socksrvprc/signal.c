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
#include <signal.h>

#ifdef UNIX
# include <unistd.h>
#endif

#include <mocaerr.h>
#include <mocagendef.h>
#include <oslib.h>

#include "socksrvprc.h"

#ifdef UNIX

static struct sigaction sigactINT, sigactTERM;

/*
 * This sets the Shutdown flag so that when we get hit with a
 * signal, things go down in an orderly fashion.
 */

static void HandleSignal(int signo)
{
    /* Send this signal to the initial thread if necessary. */
    if (!osIsInitialThread( ))
    {
        osSignalInitialThread(signo);
        return;
    }

    fprintf(stderr, "socksrvprc: Caught signal %d - exiting...\n", signo);
    fflush(stderr);

    /* Set the shutdown flag. */
    gShutdown = 1;

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

    /*
     *  We're safe to just return here because our code is just going
     *  to shutdown right away because we set the do shutdown flag
     *  in the code above.
     */

    return;
}

void InitializeSignalHandling(void)
{
    struct sigaction sigact;

    /*
     *  SIGPIPE
     */

    sigemptyset(&sigact.sa_mask);
    sigact.sa_handler = SIG_IGN;
    sigact.sa_flags = 0;

    sigaction(SIGPIPE, &sigact, NULL);

    /*
     *  SIGINT / SIGTERM
     */

    sigemptyset(&sigact.sa_mask);
    sigact.sa_handler = (void (*)(int)) HandleSignal;
    sigact.sa_flags = 0;

    sigaction(SIGINT,  &sigact, &sigactINT);
    sigaction(SIGTERM, &sigact, &sigactTERM);
}

#else

void InitializeSignalHandling(void)
{
    return;
}

#endif


