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
 *  Copyright (c) 20168
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

#define MOCA_ALL_SOURCE

#include <moca.h>

#include <stdio.h>
#include <stddef.h>
#include <signal.h>
#include <sys/types.h>

#ifdef UNIX
# include <unistd.h>
# include <pthread.h>
#endif

static char *sig_names[] =  {
#if defined AIX
    "0  SIG0",      "1  SIGHUP",    "2  SIGINT",     "3  SIGQUIT",
    "4  SIGILL",    "5  SIGTRAP",   "6  SIGABRT",    "7  SIGEMT",
    "8  SIGFPE",    "9  SIGKILL",   "10 SIGBUS",     "11 SIGSEGV",
    "12 SIGSYS",    "13 SIGPIPE",   "14 SIGALRM",    "15 SIGTERM",
    "16 SIGURG",    "17 SIGSTOP",   "18 SIGTSTP",    "19 SIGCONT",
    "20 SIGCHLD",   "21 SIGTTIN",   "22 SIGTTOU",    "23 SIGIO",
    "24 SIGXCPU",   "25 SIGXFSZ",   "27 SIGMSG",     "28 SIGWINCH",
    "29 SIGPWR",    "30 SIGUSR1",   "31 SIGUSR2",    "32 SIGPROF",
    "33 SIGDANGER", "34 SIGVTALRM", "35 SIGMIGRATE", NULL
#elif defined SUNOS
    "0  SIGO",     "1  SIGHUP",  "2  SIGINT",    "3  SIGQUIT",
    "4  SIGILL",   "5  SIGTRAP", "6  SIGABRT",   "7  SIGEMT",
    "8  SIGFPE",   "9  SIGKILL", "10 SIGBUS",    "11 SIGSEGV",
    "12 SIGSYS",   "13 SIGPIPE", "14 SIGALRM",   "15 SIGTERM",
    "16 SIGUSR1",  "17 SIGUSR2", "18 SIGCHLD",   "19 SIGPWR",
    "20 SIGWINCH", "21 SIGURG",  "22 SIGPOLL",   "23 SIGSTOP",
    "24 SIGTSTP",  "25 SIGCONT", "26 SIGTTIN",   "27 SIGTTOU",
    "28 SIGVTALRM", NULL
#elif defined HPUX
    "0  SIGO",      "1  SIGHUP",  "2  SIGINT",  "3  SIGQUIT",
    "4  SIGILL",    "5  SIGTRAP", "6  SIGABRT", "7  SIGEMT",
    "8  SIGFPE",    "9  SIGKILL", "10 SIGBUS",  "11 SIGSEGV",
    "12 SIGSYS",    "13 SIGPIPE", "14 SIGALRM", "15 SIGTERM",
    "16 SIGUSR1",   "17 SIGUSR2", "18 SIGCHLD", "19 SIGPWR",
    "20 SIGVTALRM", "21 SIGPROF", "22 SIGIO",   "23 SIGWINCH",
    "24 SIGSTOP",   "25 SIGTSTP", "26 SIGCONT", "27 SIGTTIN",
    "28 SIGTTOU",   "29 SIGURG",   NULL
#else
    NULL
#endif
};

#ifdef UNIX

static struct sigaction actions[(sizeof sig_names) / sizeof *sig_names];

static void handler(int sig)  
{
    static int in_handler;

    in_handler = 1;

    if (!in_handler)
    {
        fprintf(stderr, __FILE__ " - %d: caught %s\n", __LINE__, sig_names[sig]);
        fflush(stderr);

        /* Call the original signal handler if the original action     */
        /* wasn't SIG_DFL and an actual handler exists for the signal. */
        if ((actions[sig].sa_handler != actions[sig].sa_handler) &&
                         (actions[sig].sa_handler != (void (*)()) -1))
        {
            (*actions[sig].sa_handler)(sig);
        }

        in_handler = 0;
    }

    return;
}

void osDumpSignalMask(void)  {

    int col, sig;

    static struct sigaction handler_action = { handler, 0L, 0 };
    sigset_t threadmask;
    sigset_t procmask;

    /* Empty our own action set. */
    sigemptyset(&handler_action.sa_mask);

    /* Check the thread mask. */
#ifdef AIX
    sigthreadmask(0, NULL, &threadmask);
#else
    pthread_sigmask(0, NULL, &threadmask);
#endif

    /* Check the process mask. */
    sigprocmask(0, NULL, &procmask);

    /* Cycle through every signal we have. */
    for (sig = 0, col = 1; sig_names[sig]; sig++)
    {
        /* Replace the current action with our own. */
        /* This will fail on SIGKILL and SIGSTOP.   */
        if (sigaction(sig, &handler_action, &actions[sig]) == 0)
        {
            switch ((int)actions[sig].sa_handler)
            {
                case (int) SIG_DFL:
                    fprintf(stderr, "%-13s - SIG_DFL     ",
                                    sig_names[sig]);
                    break;

                case (int) SIG_IGN:
                    fprintf(stderr, "%-13s - SIG_IGN  *  ",
                                    sig_names[sig]);
                    break;

                default:
                    fprintf(stderr, "%-13s - %-8p *  ",
                                    sig_names[sig],
                                    actions[sig].sa_handler);

            }  /* switch */

	    fprintf(stderr, "%c%c%c ", 
	        (actions[sig].sa_flags) ? 'F' : ' ',
	        sigismember(&threadmask, sig) ? 'm' : ' ',
	        sigismember(&procmask, sig) ? 'M' : ' ');

	    if (col % 2 == 0)
		fprintf(stderr, "\n");

            /* Increment our column counter. */
            col++;

        }  /* if */

    }  /* for */

    /* Print a linefeed if necessary. */
    if (col % 2 == 0)
        fprintf(stderr, "\n");

    /* Restore the original signal action. */
    for(sig = 0; sig_names[sig]; sig++)
        sigaction(sig, &actions[sig], (struct sigaction *)0);

    fflush(stderr);

    return;
}

#else

void osDumpSignalMask(void)  {

    fprintf(stderr, "The signal mask can only be dumped on UNIX-based systems\n");
    fflush(stderr);

    return;
}

#endif
