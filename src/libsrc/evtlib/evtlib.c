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
 *#END*************************************************************************/

#define MOCA_ALL_SOURCE

#include <moca.h>

#include <stdio.h>
#include <stdio.h>
#include <stdlib.h>

#include <mocaerr.h>
#include <stddef.h>
#include <string.h>
#include <ctype.h>
#include <time.h>

#ifdef HAVE_SYS_TYPES_H
# include <sys/types.h>
#endif

#ifdef HAVE_SYS_TIME_H
# include <sys/time.h>
#endif

#ifdef HAVE_SYS_SELECT_H
# include <sys/select.h>
#endif

#include <common.h>
#include <evtlib.h>
#include <oslib.h>

#ifdef UNIX /* { */

#include <unistd.h>
#include <fcntl.h>

/* The EVENT_STR defines data used Internally within the event handling
   functions.  The application SHOULD NOT attempt to use or manipulate
   any data within this structure dynamically.  If the application needs
   to alter any of the data, it should be done between the evtRequest and
   evtRealize calls. */

#define TIMER_UNSET   0
#define TIMER_SET     1
#define TIMER_EXPIRED 2

union events_union
{
    struct
    {
	int iterations;
	OSMBX *mbx;
    } mbx;
    struct
    {
	int iterations;
        int status;
	struct timeval timeout;
    } timer;
    struct
    {
	int fd;
	int nread;
	char *scratch;
    } device;
};

/*
 * SetTimer - Set a timer based on the current time and the value of
 *            the timer event.
 */
static long SetTimer(MOCA_EVENT *event, struct timeval *now)
{
    event->event->timer.timeout.tv_sec = now->tv_sec + event->time/100;
    event->event->timer.timeout.tv_usec = now->tv_usec + 10000 * (event->time%100);
    while (event->event->timer.timeout.tv_usec > 1000000)
    {
	event->event->timer.timeout.tv_usec -= 1000000;
	event->event->timer.timeout.tv_sec++;
    }

    event->event->timer.status = TIMER_SET;

    return eOK;
}

static int CompareTime(struct timeval *tv1, struct timeval *tv2)
{
    long diff;
    diff = tv1->tv_sec - tv2->tv_sec;
    if (diff)
	return diff;
    else
    {
	diff = tv1->tv_usec - tv2->tv_usec;
	return diff;
    }
}

static struct timeval *SubTime(struct timeval *tv1, struct timeval *tv2)
{
    static struct timeval ret_tv;

    ret_tv.tv_sec = 0;
    ret_tv.tv_usec = 0;

    if (CompareTime(tv1, tv2) > 0)
    {
	ret_tv.tv_sec = tv1->tv_sec - tv2->tv_sec;
	ret_tv.tv_usec = tv1->tv_usec - tv2->tv_usec;
	while (ret_tv.tv_usec < 0)
	{
	    ret_tv.tv_usec += 1000000;
	    ret_tv.tv_sec--;
	}
    }
    return &ret_tv;
}

/*
 * CheckTimer - Check to see if a timer has expired, given the current time.
 */
static long CheckTimer(MOCA_EVENT *event, struct timeval *now)
{
    if (CompareTime(&event->event->timer.timeout,now) <= 0)
    {
	event->event->timer.status = TIMER_EXPIRED;
	return 1;
    }

    return 0;
}


/****************************************************************/

long evtFreeEvents(MOCA_EVENT *events)
{
    MOCA_EVENT *work;

    /* Set the work pointer to the incoming event base pointer and
     * loop thru the event structure array until we hit an EVT_EVENT_END
     */

    for (work = events; work->type != EVT_EVENT_END; work++)
    {
	if (work->event != NULL)
	{
	    if (work->type == EVT_MSG_READ && work->event->mbx.mbx != NULL)
	    {
		osMBXClose(work->event->mbx.mbx);
	    }
	    else if (work->type == EVT_DEV_READ && work->event->device.fd >= 0)
	    {
		close(work->event->device.fd);
		free(work->event->device.scratch);
	    }
	    free((char *) work->event);
	}
    }

    return (eOK);
}

/*   Event Request Routine   */
/*   evtRequest         Request the setup of any number of events,  read
   thru array elements until we hit an EVT_EVENT_END,
   setting up the appropriate structures
 */

long evtRequest(MOCA_EVENT * events)
{
    MOCA_EVENT *work;

    /*
     * Set the work pointer to the incoming event base pointer and
     * loop thru the event structure array until we hit an EVT_EVENT_END
     */
    for (work = events; work->type != EVT_EVENT_END; work++)
    {
	work->event = (EVENT_STR *) calloc(1, sizeof(EVENT_STR));

	if (work->event == NULL)
	{
	    return (eNO_MEMORY);
	}

	switch (work->type)
	{
	/* Timers */
	case EVT_TIMER:
	    /* Time must be a longword greater than 0  */
	    if (work->time < 1)
	    {
		return (eEVT_INVALID_PARAMS);
	    }

	    work->event->timer.status = TIMER_UNSET;

	    break;

	/* Message queue reads (asynchronous) */
	case EVT_MSG_READ:

	    if (!work->data)
		return (eEVT_INVALID_PARAMS);

	    work->event->mbx.mbx = osMBXCreate(work->name, work->max_size);
	    if (work->event->mbx.mbx == NULL)
	    {
		return (eEVT_MBX_FAILURE);
	    }

	    break;

	/* Device reads */
	case EVT_DEV_READ:
	    if (!work->data)
		return (eEVT_INVALID_PARAMS);

	    work->event->device.fd = open(work->name, O_RDWR|O_NONBLOCK);

	    if (work->event->device.fd < 0)
	    {
		return eEVT_SYSERR;
	    }

	    work->event->device.scratch = malloc(work->max_size);
	    work->event->device.nread = 0;

	    /* Expose the FD so users can see it */
	    work->fd = work->event->device.fd;

	    break;

	default:
	    return (eEVT_INVALID_PARAMS);

	}			/* End switch statement */

    }				/* End while loop */
    return (eOK);
}

/* 
 * EvtRealize iterates the events indicated in the MOCA_EVENT structure array
 * until all the events are done.  Typically, events are iterated forever and
 * the function exits via a callback routine.
 */
long evtRealize(MOCA_EVENT * events)
{
    MOCA_EVENT *work;
    int expired;
    struct timeval now;
    struct timezone zone;
    struct timeval timeout, *tptr;
    fd_set rfds;
    int nready;
    int max_fd;
    int nevents;
    long ret_status;
    int nread;
    int do_callback;


    /*
     * We need to establish the current time only at specific times, so
     * we don't hit a race condition.
     */
    gettimeofday(&now, &zone);

    /*
     * Set up timers so they're all valid.
     */
    for (work = events; work->type != EVT_EVENT_END; work++)
    {
	if (work->type == EVT_TIMER)
	{
	    if (work->event->timer.status == TIMER_UNSET)
	    {
		SetTimer(work, &now);
	    }

	    break;
	}
    }

    for (;;)
    {
	FD_ZERO(&rfds);
	
	expired = 0;
	tptr = NULL;
	max_fd = 0;
	nevents = 0;

	/*
	 * We MUST have a constant "now" time so we don't enter into
	 * a race condition.
	 */
	gettimeofday(&now, &zone);

	for (work = events; work->type != EVT_EVENT_END; work++)
	{
	    switch(work->type)
	    {
	    case EVT_TIMER:
		nevents++;
		CheckTimer(work, &now);
		if (work->event->timer.status == TIMER_EXPIRED)
		{
		    expired++;
		}
		else if (work->event->timer.status == TIMER_SET)
		{
		   /*
		    * If we haven't got a timeout yet, figure out our timeout
		    * based on the current time and the expire time of the 
		    * timer in question.
		    */
		   if (!tptr)
		   {
		       timeout = *(SubTime(&work->event->timer.timeout, &now));
		       tptr = &timeout;
		   }
		   else
		   {
		       /*
			* We've already got a timeout, so let's see if this
			* one is set to expire before or after the one we've
			* already got.
			*/
		       if (CompareTime(SubTime(&work->event->timer.timeout,
					       &now), &timeout) < 0)
		       timeout = *(SubTime(&work->event->timer.timeout,&now));
		   }
		}
		break;

	    case EVT_MSG_READ:
		nevents++;
		FD_SET(work->event->mbx.mbx->pipefd, &rfds);
		if (max_fd < work->event->mbx.mbx->pipefd)
		    max_fd = work->event->mbx.mbx->pipefd;
		break;

	    case EVT_DEV_READ:
		do
		{
		    int n;
		    /*
		     * Start out by assuming we don't want to call
		     * our function.
		     */
		    do_callback = 0;

		    /*
		     * Check for reads terminated with a NUL or Newline
		     */
		    for (n=0;n<work->event->device.nread;n++)
		    {
			if (work->event->device.scratch[n] == '\0' ||
			    work->event->device.scratch[n] == '\r')
			{
			    n++; /* We want to include this character */
			    memcpy(work->data, work->event->device.scratch, n);
			    memmove(work->event->device.scratch,
				    work->event->device.scratch+n+1,
				    work->event->device.nread - n);
			    work->event->device.nread -= n;
			    do_callback = 1;
			    break;
			}
		    }

		    /*
		     * Check for reads that went all the way.
		     */
		    if (!do_callback && 
			work->event->device.nread == work->max_size)
		    {
			memcpy(work->data, work->event->device.scratch, work->max_size);
			work->event->device.nread = 0;
			do_callback = 1;
		    }

		    /*
		     * If any of the triggering conditions have been met,
		     * we've done the read, and the callback function
		     * should be called.
		     */
		    if (do_callback)
		    {
			ret_status = (work->callback(work));
			if (ret_status != eOK)
			{
			    return ret_status;
			}
		    }
		} while(do_callback && work->event->device.nread > 0);

		nevents++;
		FD_SET(work->event->device.fd, &rfds);
		if (max_fd < work->event->device.fd)
		    max_fd = work->event->device.fd;
	    }
	}

	if (nevents)
	{
	    if (expired)
	    {
		/*
		 * No need to call select() if we know there's at least one
		 * event ready to go.
		 */
	    }
	    else
	    {
		/*
		 * At this point, we have an fd_set and a timeout value...all
		 * set to call select.
		 */
		nready = select(max_fd+1, &rfds, NULL, NULL, tptr);
		if (nready > 0)
		{
		    for (work = events; work->type != EVT_EVENT_END; work++)
		    {
			switch(work->type)
			{
			case EVT_MSG_READ:
			    if (FD_ISSET(work->event->mbx.mbx->pipefd, &rfds))
			    {
				long msgsize;
				ret_status = osMBXRecv(work->event->mbx.mbx,
						       work->data,
						       work->max_size,
						       &msgsize, 0);
				if (ret_status != eOK)
				{
				    return ret_status;
				}

				ret_status = (work->callback(work));
				if (ret_status != eOK)
				{
				    return ret_status;
				}
			    }
			    break;

			case EVT_DEV_READ:
			    if (FD_ISSET(work->event->device.fd, &rfds))
			    {
				nread = read(work->event->device.fd,
					     work->event->device.scratch +
					     work->event->device.nread,
					     work->max_size - work->event->device.nread);
				if (nread <= 0)
				{
				    return eEVT_SYSERR;
				}

				work->event->device.nread += nread;

				/* This should never happen */
				if (work->event->device.nread > work->max_size)
				{
				    return(eEVT_SYSERR);
				}
			    }
			    break;
			}
		    }
		}
	    }

	    /*
	     * Check Timers.
	     */
	    gettimeofday(&now, &zone);
	    for (work = events; work->type != EVT_EVENT_END; work++)
	    {
		switch(work->type)
		{
		case EVT_TIMER:
		    CheckTimer(work, &now);
		    if (work->event->timer.status == TIMER_EXPIRED)
		    {
			ret_status = (work->callback(work));
			if (ret_status != eOK)
			{
			    return ret_status;
			}

			if (work->iterate != EVT_FOREVER &&
			    ++work->event->timer.iterations >= work->iterate)
			{
			    work->type = EVT_EVENT_DONE;
			}
			else
			{
			    SetTimer(work, &now);
			}
		    }
		    break;
		}
	    }
	}
	else
	{
	    /*
	     * No events to wait for, so we're getting out.
	     */
	    break;
	}
    }

    return eOK;
}

#else /* WIN32 */ /* }{ */

/* The EVENT_STR defines data used Internally within the event handling
   functions.  The application SHOULD NOT attempt to use or manipulate
   any data within this structure dynamically.  If the application needs
   to alter any of the data, it should be done between the evtRequest and
   evtRealize calls. */

#define TIMER_UNSET   0
#define TIMER_SET     1
#define TIMER_EXPIRED 2

#define MBX_NEEDTHREAD 0
#define MBX_NEEDREAD   1
#define MBX_INPROG     2

union events_union
{
    struct
    {
	int iterations;
        int status;
	OSMBX *mbx;
	HANDLE hThread;
	HANDLE hReadyEvent;
	HANDLE hContinueEvent;
    } mbx;
    struct
    {
	int iterations;
        int status;
	HANDLE hTimer;
    } timer;
};


static DWORD WINAPI MBXThread(MOCA_EVENT *eptr)
{
    long msgsize;
    long ret_status;

    /*
     * Receive on a mailbox forever.
     */
    while (1)
    {
	/*
	 * Wait for our "continue" event to become signalled. It should
	 * start out that way from when we create it.
	 */
	WaitForSingleObject(eptr->event->mbx.hContinueEvent, INFINITE);

	/*
	 * Read from the mailbox. We're reading INTO the eptr->data
	 * member now, so we need to be careful that nobody else is
	 * accessing it at the same time.
	 */
	ret_status = osMBXRecv(eptr->event->mbx.mbx, eptr->data,
			       eptr->max_size, &msgsize, 0);
	if (ret_status != eOK)
	{
	    fprintf(stderr, "Error from osMBXRecv():%s\n", osError());
	    return ret_status;
	}

	/*
	 * Signal to the main thread that we've got a good read. We'll also
	 * signal to ourself to wait for the main thread to be done 
	 * processing the message.
	 */
	ResetEvent(eptr->event->mbx.hContinueEvent);
	SetEvent(eptr->event->mbx.hReadyEvent);
    }
    return eOK;
}

/*
 * Use LARGE_INTEGER for SetWaitableTimer().
 */
static void SetupTimerVal(LARGE_INTEGER *ft, long hsec)
{
    /*
     * Relative timers are negative...thus the negative multiplier
     */
    ft->QuadPart = ((LONGLONG) hsec) * (LONGLONG) -100000;
}


/*
 * evtFreeEvents - Free allocated events and handles associated with the 
 * current MOCA_EVENT array.
 */
long evtFreeEvents(MOCA_EVENT *events)
{
    MOCA_EVENT *work;

    /* Set the work pointer to the incoming event base pointer and
     * loop thru the event structure array until we hit an EVT_EVENT_END
     */
    for (work = events; work->type != EVT_EVENT_END; work++)
    {
	if (work->event)
	{
	    switch(work->type)
	    {
	    case EVT_MSG_READ:
		if (work->event->mbx.hThread)
		    TerminateThread(work->event->mbx.hThread, 0);
		if (work->event->mbx.hReadyEvent)
		    CloseHandle(work->event->mbx.hReadyEvent);
		if (work->event->mbx.hContinueEvent)
		    CloseHandle(work->event->mbx.hContinueEvent);
		if (work->event->mbx.mbx)
		    osMBXClose(work->event->mbx.mbx);
		break;

	    case EVT_TIMER:
		if (work->event->timer.hTimer)
		    CloseHandle(work->event->timer.hTimer);
		break;
	    }
	    free((char *) work->event);
	    work->event = NULL;
	}
    }

    return (eOK);
}

/*  evtRequest - Request the setup of any number of events, read
 *  thru array elements until we hit an EVT_EVENT_END,
 *  setting up the appropriate structures
 */

long evtRequest(MOCA_EVENT * events)
{

    MOCA_EVENT *work;

    /*
     * Set the work pointer to the incoming event base pointer and
     * loop thru the event structure array until we hit an EVT_EVENT_END
     */
    for (work = events; work->type != EVT_EVENT_END; work++)
    {
	/* Allocate an event structure */
	work->event = (EVENT_STR *) calloc(1, sizeof(EVENT_STR));

	if (work->event == NULL)
	{
	    return (eNO_MEMORY);
	}

	switch (work->type)
	{
	/* Timers */
	case EVT_TIMER:
	    /* Time must be a longword greater than 0  */
	    if (work->time < 1)
	    {
		return (eEVT_INVALID_PARAMS);
	    }

	    /*
	     * Create a timer that we can wait on.  Set the initial status to
	     * "UNSET", which will be our clue later on to set the timer.
	     */
	    work->event->timer.hTimer = CreateWaitableTimer(NULL, TRUE, NULL);
	    work->event->timer.status = TIMER_UNSET;

	    break;

	/* Mailbox reads (asynchronous) */
	case EVT_MSG_READ:

	    if (!work->data)
		return (eEVT_INVALID_PARAMS);

	    /*
	     * Allocate a mailbox for this read.  This will fail if a
	     * mailbox already exists.
	     */
	    work->event->mbx.mbx = osMBXCreate(work->name, work->max_size);

	    if (work->event->mbx.mbx == NULL)
	    {
		return (eEVT_MBX_FAILURE);
	    }

	    /*
	     * Allocate two Windows event handles. One for us to wait for,
	     * and one for the child thread to wait for.
	     */
	    work->event->mbx.hReadyEvent = CreateEvent(NULL, TRUE, FALSE, NULL);

	    if (!work->event->mbx.hReadyEvent)
	    {
		fprintf(stderr, "Error from CreateEvent():%s\n", osError());
		return(eEVT_SYSERR);
	    }

	    work->event->mbx.hContinueEvent = CreateEvent(NULL, TRUE, TRUE, NULL);
	    if (!work->event->mbx.hContinueEvent)
	    {
		fprintf(stderr, "Error from CreateEvent():%s\n", osError());
		return(eEVT_SYSERR);
	    }

	    /*
	     * The thread  isn't yet spawned, so we need to set a status
	     * so we spawn it later.
	     */
	    work->event->mbx.status = MBX_NEEDTHREAD;

	    break;

	/* Device reads, not implemented yet */
	case EVT_DEV_READ:
	    return (eEVT_INVALID_PARAMS);

	default:
	    return (eEVT_INVALID_PARAMS);

	}			/* End switch statement */

    }				/* End while loop */
    return (eOK);
}

/* 
 * EvtRealize iterates the events indicated in the MOCA_EVENT structure array
 * until all the events are done.  Typically, events are iterated forever and
 * the function exits via a callback routine.
 */
long evtRealize(MOCA_EVENT * events)
{

    MOCA_EVENT *work;
    int nevents;
    DWORD threadid;
    long ret_status;
    LARGE_INTEGER duetime;
    HANDLE hlist[200];
    MOCA_EVENT *evlist[200];

    while (1)
    {
	nevents = 0;

	/*
	 * Keep track of which Windows events/timers/etc. we'll be
	 * waiting for.  Also spawn any threads, set any events and reset
	 * any timers that need to be spawned, set and reset.
	 */
	for (work = events; work->type != EVT_EVENT_END; work++)
	{
	    switch(work->type)
	    {
	    case EVT_TIMER:
		/* 
		 * If the timer is unset, set it
		 */
		if (work->event->timer.status == TIMER_UNSET)
		{
		    SetupTimerVal(&duetime, work->time);
		    SetWaitableTimer(work->event->timer.hTimer, &duetime, 0,
				     NULL, NULL, FALSE);

		    work->event->timer.status = TIMER_SET;
		}

		/*
		 * Add the timer to our list of synchronization objects
		 * we're waiting for.
		 */
		evlist[nevents] = work;
		hlist[nevents++] = work->event->timer.hTimer;
		break;

	    case EVT_MSG_READ:
		/*
		 * If the thread isn't spawned, spawn it.
		 */
		if (work->event->mbx.status == MBX_NEEDTHREAD)
		{
		    work->event->mbx.hThread = CreateThread(NULL, 0, MBXThread,
			                                    work, 0, &threadid);
		    if (!work->event->mbx.hThread)
		    {
			fprintf(stderr, "Error from CreateThread():%s\n", osError());
			return(eEVT_SYSERR);
		    }
		    work->event->mbx.status = MBX_INPROG;
		}

		/*
		 * If a read is needed, set the "continue" event. (It's really
		 * more of an "OK to read" event.
		 */
		if (work->event->mbx.status == MBX_NEEDREAD)
		{
		    SetEvent(work->event->mbx.hContinueEvent);
		}

		/*
		 * Watch both event and thread in our list of synchronization
		 * objects.  The thread handle will only become signalled if
		 * there's a serious problem causing the thread to terminate.
		 * Since we're only going to exit in that case, we can ignore
		 * the MOCA_EVENT associated with that thread's death.
		 */
		evlist[nevents] = work;
		hlist[nevents++] = work->event->mbx.hReadyEvent;
		evlist[nevents] = NULL;
		hlist[nevents++] = work->event->mbx.hThread;
		break;
	    }
	}

	/*
	 * Only go on if there's more to do
	 */
	if (nevents)
	{
	    DWORD curobj;

	    /*
	     * We have a valid non-empty list of Win32 synchronization
	     * objects to wait on.
	     */
	    curobj = WaitForMultipleObjects(nevents, hlist, FALSE, INFINITE);

	    if (curobj == WAIT_FAILED || curobj == WAIT_TIMEOUT)
	    {
		fprintf(stderr, "Error from WaitForMultipleObjects():%s\n", osError());
		return(eEVT_SYSERR);
	    }
	    else
	    {
		work = evlist[curobj - WAIT_OBJECT_0];

		/*
		 * If we don't have a valid MOCA_EVENT pointer, it must be
		 * because a thread died.  That's a bad thing.
		 */
		if (!work)
		{
		    fprintf(stderr, "Error: a thread died prematurely\n");
		    return eEVT_SYSERR;
		}

		switch(work->type)
		{
		case EVT_TIMER:
		    work->event->timer.iterations++;

		    /*
		     * Call our event callback routine.  This should 
		     * return eOK unless it wants the program to end.
		     */
		    ret_status = (work->callback(work));
		    if (ret_status != eOK)
		    {
			return ret_status;
		    }

		    /*
		     * Have we come to the end of our iterations? If so,
		     * we'll need to clean up after ourselves.
		     */
		    if (work->iterate != EVT_FOREVER &&
			work->event->timer.iterations >= work->iterate)
		    {
			if (work->event->timer.hTimer)
			    CloseHandle(work->event->timer.hTimer);
			work->event->timer.hTimer = NULL;
			work->type = EVT_EVENT_DONE;
		    }
		    else
		    {
			/*
			 * Reset the timer status so the handle 
			 * gets reset above.
			 */
			work->event->timer.status = TIMER_UNSET;
		    }

		    break;

		case EVT_MSG_READ:
		    work->event->mbx.iterations++;

		    /*
		     * Call our event callback routine.  This should 
		     * return eOK unless it wants the program to end.
		     */
		    ret_status = (work->callback(work));
		    if (ret_status != eOK)
		    {
			return ret_status;
		    }

		    /*
		     * Have we come to the end of our iterations? If so,
		     * we'll need to clean up after ourselves.
		     */
		    if (work->iterate != EVT_FOREVER &&
			work->event->mbx.iterations >= work->iterate)
		    {
			if (work->event->mbx.hThread)
			    TerminateThread(work->event->mbx.hThread, 0);
			if (work->event->mbx.hReadyEvent)
			    CloseHandle(work->event->mbx.hReadyEvent);
			if (work->event->mbx.hContinueEvent)
			    CloseHandle(work->event->mbx.hContinueEvent);
			if (work->event->mbx.mbx)
			    osMBXClose(work->event->mbx.mbx);
			work->event->mbx.hThread = NULL;
			work->event->mbx.hReadyEvent = NULL;
			work->event->mbx.hContinueEvent = NULL;

			work->type = EVT_EVENT_DONE;
		    }
		    else
		    {
			/*
			 * Reset the "Ready" event, so we dont spin
			 * through this again.
			 */
			ResetEvent(work->event->mbx.hReadyEvent);
			work->event->mbx.status = MBX_NEEDREAD;
		    }

		    break;
		}
	    }
	}
	else
	{
	    /*
	     * If we are iterating for only a short number of iterations,
	     * we may come to the end of our list.  If so, head on out.
	     */
	    break;
	}
    }
    return eOK;
}
#endif /* } */
