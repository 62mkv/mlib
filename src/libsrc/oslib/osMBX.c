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
#include <string.h>
#include <stdlib.h>
#include <errno.h>

#ifdef UNIX
# include <signal.h>
# include <unistd.h>
# include <time.h>
# include <sys/time.h>
# include <sys/types.h>
# include <sys/wait.h>
# include <sys/msg.h>
#endif

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include "osprivate.h"

#ifdef WIN32 /* { */

/*
 * os_MBXRecv - Receive a mailbox message.
 */
long os_MBXRecv(OSMBX *mbx, void *buffer, long size, long *msgsize, int flags, int timeout)
{
    long tmp_msgsize;

    if (flags & OS_MBX_NOWAIT)
	SetMailslotInfo(mbx->hPipe, timeout);

    if (!ReadFile(mbx->hPipe, buffer, mbx->max_msg, &tmp_msgsize, NULL))
    {
	SetMailslotInfo(mbx->hPipe, MAILSLOT_WAIT_FOREVER);

	if ((flags & OS_MBX_NOWAIT) && GetLastError() == ERROR_TIMEOUT)
	    return eOS_TIMEOUT;

	return eOS_MBX_READ_FILE;
    }

    SetMailslotInfo(mbx->hPipe, MAILSLOT_WAIT_FOREVER);

    if (msgsize) 
	*msgsize = tmp_msgsize;

    return eOK;
}

/*
 * osMBXCreate - Create a mailbox and return a structure to be used 
 *               with subsequent calls to osMBX* functions.
 */
OSMBX *osMBXCreate(char *mbxname, long max_msg)
{
    OSMBX tmp, *ptr;
    char *envname;

    envname = osGetVar(ENV_ENVNAME);
    sprintf(tmp.path, "\\\\.\\mailslot\\%s_%s", envname?envname:"UNK", mbxname);
    tmp.max_msg = max_msg;
    tmp.hPipe = CreateMailslot(tmp.path, max_msg, MAILSLOT_WAIT_FOREVER, NULL);

    if (tmp.hPipe == INVALID_HANDLE_VALUE)
	return NULL;

    ptr = calloc(1, sizeof (OSMBX));
    if (! ptr)
	OS_PANIC;
 
    *ptr = tmp;

    return ptr;
}

/*
 * osMBXClose - Deallocate all resources associated with a mailbox.  This
 *              includes the handle to the named pipe (which should make it
 *              go away, and the memory associated with the mailbox.
 */
long osMBXClose(OSMBX *mbx)
{
    CloseHandle(mbx->hPipe);
    free(mbx);

    return eOK;
}

/*
 * osMBXRecv - Receive a mailbox message.
 */
long osMBXRecv(OSMBX *mbx, void *buffer, long size, long *msgsize, int flags)
{
    return os_MBXRecv(mbx, buffer, size, msgsize, flags, 0);
}

/*
 * osMBXRecvTimeout - Receive a mailbox message, handling a timeout argument.
 */

long osMBXRecvTimeout(OSMBX *mbx, void *buffer, long size, long *msgsize, int timeout)
{
    return os_MBXRecv(mbx, buffer, size, msgsize, OS_MBX_NOWAIT, timeout);
}

/*
 * osMBXSend - Send a message to a mailbox.
 */
long osMBXSend(char *mbxname, void *buffer, long msgsize, int flags)
{
    long status;
    char tmp_path[256];
    char *envname;
    HANDLE hMailslot;
    DWORD nbytes;

    envname = osGetVar(ENV_ENVNAME);
    sprintf(tmp_path, "\\\\.\\mailslot\\%s_%s", envname?envname:"UNK", mbxname);
    hMailslot = CreateFile(tmp_path, GENERIC_WRITE,
	                   FILE_SHARE_READ | FILE_SHARE_WRITE, NULL,
			   OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);

    if (hMailslot == INVALID_HANDLE_VALUE)
	return eOS_MBX_CREATE_FILE;

    if (!WriteFile(hMailslot, buffer, msgsize, &nbytes, NULL))
        status = eOS_MBX_WRITE_FILE;
    else if (nbytes < (DWORD) msgsize)
        status = (flags & OS_MBX_NOWAIT) ? eOS_TIMEOUT : eOS_MBX_WRITE_FILE;
    else
	status = eOK;

    if (status != eOK && status != eOS_TIMEOUT)
	return status;

    CloseHandle(hMailslot);

    return status;
}

long osMBXKill(char *mbxname)
{
    long status = eOK;

    /*
     * We can't remove the mailbox while someone's got it open, so
     * just retun eOK.
     */
    return status;
}

#else /* UNIX */ /* }{ */

typedef struct
{
    char mbxname[OS_MBXNAME_LEN+1];
    char queue_id[OS_QUEUEID_LEN+1];
    char newline;
} OSMBX_IDS;

static OSMBX **MBXList;
static int MBXCount;

static struct sigaction sigactINT,
			sigactTERM;

#define READ 1
#define WRITE 2
#define CREATE 4

static OSMBX_IDS *os_MBXmem = NULL;
int os_MBXreadonly = 0;

static OSMBX_IDS *os_MapMBXFile(int mode)
{
    if (os_MBXmem && os_MBXreadonly && (mode & WRITE))
    {
	osUnmapFile(os_MBXmem);
	os_MBXmem = NULL;
    }

    if (!os_MBXmem)
    {
	char *ptr;
	char mbx_file[1000];

	ptr = osGetRegistryValue(REGSEC_SERVER, REGKEY_SERVER_MAILBOX_FILE);
	strncpy(mbx_file, ptr?ptr:"", sizeof mbx_file);

	/* Read Only mode */
	if (!(mode & WRITE))
	{
	    if (eOK != osMapFile(mbx_file, (void **) &os_MBXmem, OS_MAPMODE_RO))
		os_MBXmem = NULL;
	    os_MBXreadonly = 1;
	}
	else
	{
	    /*
	     * First try to map it read/write.  Then, if we want to create it,
	     * create it and then map it read/write.
	     */
	    if (eOK != osMapFile(mbx_file, (void **) &os_MBXmem, OS_MAPMODE_RW))
	    {
		if (mode & CREATE)
		{
		    /*
		     * First create, then map the files. osCreateMapFile
		     * should create the file zero-filled, so we don't need
		     * to initialize the data.  If we did, this would be
		     * the place.
		     */
		    if (eOK != osCreateMapFile(mbx_file,
					       NUM_MBX * sizeof(OSMBX_IDS)) ||
		        eOK != osMapFile(mbx_file, (void **) &os_MBXmem,
					 OS_MAPMODE_RW))
		    {
			os_MBXmem = NULL;
		    }
		}
		else
		{
		    os_MBXmem = NULL;
		}
	    }

	    os_MBXreadonly = 0;
	}
    }

    return os_MBXmem;
}

static void os_UnmapMBXFile(void)
{
   osUnmapFile(os_MBXmem);
   os_MBXmem = NULL;
}

/*
 * os_MBXCleanUp() - Clean up list of mailboxes.
 */
static void os_MBXCleanup(void)
{
    int count;

    for (count = MBXCount; count; count--)
    {
	/* 
	 * osMBXClose affects MBXCount, unless something is drastically
	 * wrong, but we can't be looping, so clear out the mailboxes
	 * backwards.
	 */
	if (osMBXClose(MBXList[count - 1]) != eOK)
	    perror("osMBXClose");
    }

    os_UnmapMBXFile();
}

/*
 * Clean up on receipt of a signal.  
 * 
 * We don't handle all possible signals, just SIGINT and SIGTERM.
 */
static void os_MBXSigCleanup(int signo)
{
    /* Send this signal to the initial thread if necessary. */
    if (!osIsInitialThread( ))
    {
        osSignalInitialThread(signo);
	return;
    }

    fprintf(stderr, "MBX: Caught signal %d - cleaning up mailboxes...\n", signo);

    /* Do our own signal handling. */
    os_MBXCleanup( );

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
     *  If main( ) installed its own signal handler for any of the
     *  above signal handlers, we'll never get to here and we'll be 
     *  just fine because the lower level signal handler can handle
     *  calling exit( ).  We just want to make sure we clean up our
     *  messages queues.
     *
     *  If main( ) was too lazy to install its own signal handler, 
     *  however, then we have to call exit so that execution actually
     *  stops.
     */

    exit(EXIT_SUCCESS);

    return;
}

/*
 * os_MBXInit() - Initialize function for Mailbox handling.  Set up
 * signal handlers, cleanup routines, etc.
 */
static void os_MBXInit(void)
{
    static int done_atexit;

    struct sigaction sigact;

    /*
     *  SIGINT / SIGTERM
     */

    sigemptyset(&sigact.sa_mask);
    sigact.sa_handler = (void (*)()) os_MBXSigCleanup;
    sigact.sa_flags = 0;

    sigaction(SIGINT,  &sigact, &sigactINT);
    sigaction(SIGTERM, &sigact, &sigactTERM);

    /*
     * atexit routines can't be dequeued, so we should make sure to only
     * queue this puppy once.
     */
    if (!done_atexit)
    {
	done_atexit++;
	osAtexit(os_MBXCleanup);
    }
}

/*
 * os_MBXDeInit() - Undoes what os_MBXInit does.
 */
static void os_MBXDeInit(void)
{
    struct sigaction default_action;

    /*
     * Ideally, we'd make a dynamic sigaction stack, but it gets tricky
     * if os_MBXInit gets called more than once per mailbox. (i.e. if an
     * osMBXCreate failed for some reason and then succeeded.  For now, 
     * using the default action will have to do.
     */
    default_action.sa_handler = SIG_DFL;
    sigemptyset(&default_action.sa_mask);

    sigaction(SIGTERM, &default_action, NULL);
    sigaction(SIGINT, &default_action, NULL);
}

/*
 * os_ExecSubproc() - Run the subprocess.
 */
static void os_ExecSubproc(char *mbxname, int queue_id, int pipefd, long max_msg)
{
    char queue_id_str[20];
    char pipefd_str[20];
    char max_msg_str[20];
    char subproc_exec[1024];
    sprintf(subproc_exec, "%s/bin/%s", osGetVar("MOCADIR"), "mbxrcvprc");
    sprintf(queue_id_str, "%d", queue_id);
    sprintf(pipefd_str, "%d", pipefd);
    sprintf(max_msg_str, "%ld", max_msg);

    execl(subproc_exec, 
	  "mbxrcv", mbxname, pipefd_str, queue_id_str, max_msg_str, (char *)0);
    return;
}

/*
 * closeall() - close all FDs >= a specified value (courtesy Andrew 
 * Gierth--andrew@erlenstar.demon.co.uk)
 */
static void os_CloseAll(int fd)
{
    int fdlimit = sysconf(_SC_OPEN_MAX);

    while (fd < fdlimit)
	close(fd++);
}

/*
 * LookupMBXIdentifier - Lookup by name our mailbox name to get
 * the queue_id (message queue) associated with it.
 */
static long os_LookupMBXIdentifier(char *name, int *queue_id)
{
    OSMBX_IDS *mem;
    long ret_status = eOK;
    char testname[OS_MBXNAME_LEN+1];
    int i,slot,found;

    /*
     * Map the mailboxes file read-only.  
     */
    mem = os_MapMBXFile(READ|WRITE);
    if (!mem)
	return eOS_MBX_MAP_FILE;

    /*
     * Keep others out while we're writing the shared memory.
     */
    osLockMem(mem);

    memset(testname, ' ', OS_MBXNAME_LEN);
    testname[OS_MBXNAME_LEN] = '\0';
    strncpy(testname, name, strlen(name));

    slot = -1;
    found = 0;
    for(i=0;i<NUM_MBX;i++)
    {
	/*
	 * If a slot is still untouched, it's never been used, so it's safe
	 * to stop looking here.
	 */
	if (!mem[i].mbxname[0])
	    break;

	/*
	 * If we've found a match to our name, break out.
	 */
	if (0 == strncmp(testname, mem[i].mbxname, OS_MBXNAME_LEN))
	{
	    found = 1;
	    break;
	}
    }

    if (!found)
    {
	ret_status = eOS_MBX_NOT_FOUND;
    }
    else
    {
	char tmp[20];
	memset(tmp, '\0', sizeof tmp);
	strncpy(tmp, mem[i].queue_id, sizeof mem[i].queue_id);
	*queue_id = atoi(tmp);
    }

    osUnlockMem(mem);
    
    return ret_status;
}

/*
 * CreateMBXIdentifier - Create a new entry in the Mailboxes memory region,
 * and create the message queue associated with it.
 */
static long os_CreateMBXIdentifier(char *name, int *queue_id)
{
    OSMBX_IDS *mem = NULL;
    long ret_status = eOK;
    char testname[OS_MBXNAME_LEN+1];
    int i,slot,found;


    mem = os_MapMBXFile(READ|WRITE|CREATE);
    if (!mem)
	return eOS_MBX_MAP_FILE;

    /*
     * Keep others out while we're writing the shared memory.
     */
    osLockMem(mem);

    memset(testname, ' ', OS_MBXNAME_LEN);
    testname[OS_MBXNAME_LEN] = '\0';
    strncpy(testname, name, strlen(name));

    slot = -1;
    found = 0;
    for(i=0;i<NUM_MBX;i++)
    {
	/*
	 * If a slot is still untouched, it's never been used, so it's safe
	 * to stop looking here.
	 */
	if (!mem[i].mbxname[0])
	{
	    if (slot == -1)
		slot = i;
	    break;
	}

	/*
	 * If we've found a match to our name, break out with an error.
	 */
	if (0 == strncmp(testname, mem[i].mbxname, OS_MBXNAME_LEN))
	{
	    found = 1;
	    break;
	}

	/*
	 * If there's an open slot (mbxname cleared) keep track of it for
	 * later.
	 */
	if (mem[i].mbxname[0] == ' ' && slot == -1)
	{
	    slot = i;
	}
    }

    if (found)
    {
	/* Oops.  Someone's already using the name we want. */
	ret_status = eOS_MBX_EXISTS;
    }
    else if (slot < 0)
    {
	/* What happened?  The memory block is full! */
	ret_status = eOS_MBX_NOMEM;
    }
    else if (-1 == (*queue_id = msgget(IPC_PRIVATE, 0660 | IPC_CREAT)))
    {
	/* What happened?  We can't create the message queue! */
	ret_status = eOS_MBX_MSGGET;
    }
    else
    {
	char tmp[20];

	memset(mem[slot].mbxname, ' ', sizeof(mem[slot].mbxname));
	strncpy(mem[slot].mbxname, testname, OS_MBXNAME_LEN);

	memset(mem[slot].queue_id, ' ', sizeof(mem[slot].queue_id));
	sprintf(tmp, "%d", *queue_id);
	strncpy(mem[slot].queue_id, tmp, strlen(tmp));

	mem[slot].newline = '\n';
    }

    osUnlockMem(mem);

    return ret_status;
}

/*
 * os_BXRecv - 
 */

long os_MBXRecv(OSMBX *mbx, void *buffer, long size, long *msgsize, int flags, int timeout)
{
    char msgsize_str[9];
    char *unconverted = NULL;
    long nbytes;
    long readlen, dumplen;
    long total_read;
    
    /* Check to see if there's a message waiting */
    if (flags & OS_MBX_NOWAIT)
    {
	fd_set rfds;
	struct timeval peek;

	FD_ZERO(&rfds);
	FD_SET(mbx->pipefd, &rfds);

	peek.tv_sec = timeout;
	peek.tv_usec= 0;

	if (0 == select(mbx->pipefd + 1, &rfds, NULL, NULL, &peek))
	    return eOS_TIMEOUT;
    }

    /*
     * The format of messages coming from the child process (over the pipe)
     * is: xxxxxxxx|message
     * where 'x' represents a hex digit encoding the size of 'message' in
     * bytes.
     */
    if ((nbytes = read(mbx->pipefd, msgsize_str, 9)) <= 0)
    {
	/* Got an error! */
	eOS_MBX_READ;
    }

    if (nbytes != 9 || msgsize_str[8] != MBX_SEPARATOR)
    {
	/* Protocol error! */
	eOS_MBX_PROTOCOL;
    }

    msgsize_str[8] = '\0';
    readlen = strtol(msgsize_str, &unconverted, 16);

    if (unconverted && *unconverted)
    {
	/* Protocol error! */
	return eOS_MBX_PROTOCOL;
    }

    total_read = 0;

    /*
     * OK.  There's a chance (hopefully small) that we're getting a large
     * message we weren't expecting.  Since we're message oriented here,
     * we'll just discard the excess and return OK.
     */
    dumplen = 0;

    if (readlen > size)
    {
	dumplen = readlen - size;
	readlen = size;
    }

    /*
     * Read readlen bytes, looping if necessary.  On error, bomb out.
     */
    while (total_read < readlen)
    {
	nbytes = read(mbx->pipefd, ((char *)buffer) + total_read,
		      readlen - total_read);
	if (nbytes <= 0)
	{
	    /* Protocol Error...EOF or Error on pipe. */
	    return eOS_MBX_READ;
	}

	total_read += nbytes;
    }

    /*
     * Now we have to read the pipe again, dumping out the results, in case
     * we got a message that was too big.
     */
    while (total_read < (dumplen + readlen))
    {
	char blackhole[256];
	long holesize;

	holesize = dumplen + readlen - total_read;
	if (holesize > sizeof blackhole) 
	    holesize = sizeof blackhole;

	nbytes = read(mbx->pipefd, blackhole, holesize);
	if (nbytes <= 0)
	{
	    /* Protocol Error...EOF or Error on pipe. */
	    return eOS_MBX_READ;
	}

	total_read += nbytes;
    }

    if (msgsize) *msgsize = readlen;

    return eOK;
}

/*
 * RemoveMBXIdentifier - 
 */
static long os_RemoveMBXIdentifier(char *name, int queue_id)
{
    OSMBX_IDS *mem = NULL;
    long ret_status = eOK;
    char testname[OS_MBXNAME_LEN+1];
    int i,found;

    /*
     * Map the mailboxes file.
     */
    if ((mem = os_MapMBXFile(READ|WRITE)))
    {
	/*
	 * Keep others out while we're writing the shared memory.
	 */
	osLockMem(mem);

	memset(testname, ' ', OS_MBXNAME_LEN);
	strncpy(testname, name, strlen(name));
	testname[OS_MBXNAME_LEN] = '\0';

	found = 0;
	for(i=0;i<NUM_MBX;i++)
	{
	    /*
	     * If a slot is still untouched, it's never been used, so it's safe
	     * to stop looking here.
	     */
	    if (!mem[i].mbxname[0])
		break;

	    /*
	     * We've found a match to our name.
	     */
	    if (0 == strncmp(testname, mem[i].mbxname, OS_MBXNAME_LEN))
	    {
		char tmp[20];
		memset(tmp, '\0', sizeof tmp);
		strncpy(tmp, mem[i].queue_id, sizeof mem[i].queue_id);
		queue_id = atoi(tmp);
		memset(&mem[i], ' ', sizeof(mem[i]));
		mem[i].newline = '\0';
		break;
	    }
	}

	osUnlockMem(mem);
    }

    if (queue_id >= 0)
    {
	if (0 != msgctl(queue_id, IPC_RMID, NULL))
	{
	    ret_status = eOS_MBX_MSGCTL;
	}
    }
    else
    {
	ret_status = eOS_MBX_QUEUE_ID;
    }
    
    return ret_status;
}

/******************************************************************
 * Beginning of public functions.
 *****************************************************************/

/*
 * osMBXCreate - Create a mailbox and return a structure to be used 
 *               with subsequent calls to osMBX* functions.
 */
OSMBX *osMBXCreate(char *mbxname, long max_msg)
{
    OSMBX *ptr;
    int pipes[2];
    long ret_status;
    pid_t pid;

    /*
     * Install signal handlers and register an atexit function
     * to clean up after ourselves.
     */
    if (!MBXCount)
	os_MBXInit();

    /* Build up a new mailbox entry . */
    ptr = calloc(1, sizeof(OSMBX));
    ptr->max_msg = max_msg;
    strncpy(ptr->mbxname, mbxname, sizeof ptr->mbxname - 1);
    ptr->mbxname[sizeof ptr->mbxname - 1] = '\0';

    /* Add the new mailbox entry to the mailbox list. */
    MBXCount++;
    MBXList = realloc(MBXList, sizeof (OSMBX *) * MBXCount);
    MBXList[MBXCount-1] = ptr;

    /*
     * Copy the mailbox name into the mailboxes file and create 
     * a message queue for the new mailbox.
     */
    ret_status = os_CreateMBXIdentifier(ptr->mbxname, &ptr->queue_id);
    if (ret_status != eOK)
    {
        osMBXClose(ptr);
	return NULL;
    }

    /*
     * Ok, we need to create a pipe so we can talk to the child process.
     */
    if (0 != pipe(pipes))
    {
        osMBXClose(ptr);
	return NULL;
    }

    switch(pid = fork())
    {
    case -1: 
    {
        osMBXClose(ptr);
	return NULL;
    }

    case 0:
	/* Child */
	close(pipes[0]);
	dup2(pipes[1],3);
	os_CloseAll(4);
	os_ExecSubproc(mbxname, ptr->queue_id, 3, max_msg);
	exit(EXIT_SUCCESS);

    default:
	/* Parent */
	close(pipes[1]);
	ptr->pipefd = pipes[0];
	ptr->subproc_pid = pid;
	break;
    }

    return ptr;
}

/*
 * osMBXClose - Close a mailbox, and deallocate all the resources
 *              associated with it.  This includes the subproc
 *              spawned to read the messages, allocated memory and
 *              pipes.
 */
long osMBXClose(OSMBX *mbx)
{
    long ret_status;
    int i;

    /* Close the pipe to the subproc */
    if (mbx->pipefd)
        close(mbx->pipefd);

    /* Kill the subproc */
    if (mbx->subproc_pid)
        kill(mbx->subproc_pid, SIGTERM);

    /* Wait for the subproc to die */
    if (mbx->subproc_pid)
        waitpid(mbx->subproc_pid, NULL, 0);

    /* Remove the mailbox */
    ret_status = os_RemoveMBXIdentifier(mbx->mbxname, mbx->queue_id);

    /*
     * Free the associated memory.  We do this even if the Remove
     * failed (e.g. there's a problem with shared memory).
     */
    free(mbx);

    for (i=0; i<MBXCount; i++)
    {
	if (MBXList[i] == mbx)
	{
	    if (i+1 < MBXCount)
		memmove(MBXList+i, MBXList+i+1, (MBXCount-i) * sizeof(OSMBX *));

	    MBXCount--;

	    if (MBXCount)
	    {
		MBXList = realloc(MBXList, sizeof (OSMBX *) * MBXCount) ;
	    }
	    else
	    {
		free(MBXList);
	        MBXList = NULL;
	    }
	    break;
	}
    }

    /* Remove signal handlers if we don't have a mailbox anymore */
    if (!MBXCount)
	os_MBXDeInit();

    return ret_status;
}

/*
 * osMBXRecv - Receive a mailbox message.
 */

long osMBXRecv(OSMBX *mbx, void *buffer, long size, long *msgsize, int flags)
{
    return os_MBXRecv(mbx, buffer, size, msgsize, flags, 0);
}

/*
 * osMBXRecvTimeout - Receive a mailbox message, handling a timeout argument.
 */

long osMBXRecvTimeout(OSMBX *mbx, void *buffer, long size, long *msgsize, int timeout)
{
    return os_MBXRecv(mbx, buffer, size, msgsize, OS_MBX_NOWAIT, timeout);
}

/* This structure is used for message queue sends */
typedef struct { long mtype; char mbuff[1]; } MY_MSGBUF;

/*
 * osMBXSend - Send a message to a mailbox.
 */
long osMBXSend(char *mbxname, void *buffer, long size, int flags)
{
    int myflags;
    long ret_status;
    MY_MSGBUF *sendstruct;
    int queue_id;

    /*
     * Find the message queue, given the mailbox name.
     */
    ret_status = os_LookupMBXIdentifier(mbxname, &queue_id);
    if (ret_status != eOK)
	return ret_status;

    /*
     * We either have to do this or have a static-sized send buffer because
     * of the annoying way that msgsnd does it's "type-classification".
     */
    sendstruct = malloc(sizeof(MY_MSGBUF) + size - 1);
    if (!sendstruct)
	OS_PANIC;
    
    sendstruct->mtype = 1;
    memcpy(sendstruct->mbuff, buffer, size);

    myflags = (flags & OS_MBX_NOWAIT) ? IPC_NOWAIT : 0;

    ret_status = msgsnd(queue_id, sendstruct, size, myflags);
    if (ret_status != 0)
    {
	if ((flags & OS_MBX_NOWAIT) && errno == EAGAIN)
	    ret_status = eOS_TIMEOUT;
        else
	    ret_status = eOS_MBX_MSGSND;
    }

    free(sendstruct);

    return ret_status;
}

long osMBXKill(char *mbxname)
{
    long ret_status;

    /* Remove the mailbox */
    ret_status = os_RemoveMBXIdentifier(mbxname, -1);

    return ret_status;
}

/*
 * osMBXGetList - Get a list of the current mailbox names.
 */

OS_MBX_LIST *osMBXGetList(void)
{
    int ii,
        jj;
    static OS_MBX_LIST mbxList[NUM_MBX];
    OSMBX_IDS *mem;

    /* Map the mailboxes file read-only. */
    mem = os_MapMBXFile(READ|WRITE);
    if (!mem)
	return(NULL);

    /* Lock the mailboxes file. */
    osLockMem(mem);

    /* Cycle through the mailboxes list. */
    for(ii=0, jj=0; ii<NUM_MBX; ii++)
    {

	/* Skip this entry if it's empty. */
	if (mem[ii].mbxname[0] == ' ')
	    continue;

	/* Are we at the end of the mailboxes list? */
	if (mem[ii].mbxname[0] == '\0')
	    break;

	/* Add this mailbox to our list. */
	strncpy(mbxList[jj].mbxname, mem[ii].mbxname, OS_MBXNAME_LEN);
	misTrim(mbxList[jj].mbxname);
	strncpy(mbxList[jj].queue_id, mem[ii].queue_id, OS_QUEUEID_LEN);
	misTrim(mbxList[jj].queue_id);

	jj++;

    }

    /* Make sure out last entry is null'd out. */
    mbxList[jj].mbxname[0] = '\0';

    /* Unlock the mailboxes file. */ 
    osUnlockMem(mem);

    return(mbxList);
}

#endif /* } */
