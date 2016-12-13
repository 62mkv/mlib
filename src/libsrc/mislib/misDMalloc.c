static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: MOCA Version of heap debugging malloc wrapper.
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


/******************************************************************************
 *
 *                       D O C U M E N T A T I O N
 *
 * Depending on the runtime options specified in the MOCA_DMALLOC environment
 * variable, this function will report each of the currently allocated memory
 * blocks including their addresses, sizes, times of allocation, and whether or
 * not they have been overwritten.
 *
 * OPTIONS
 *
 *     Turning Options On and Off
 *
 *         Runtime options to the dmalloc package should be specified in an
 *         environment variable called MOCA_DMALLOC.
 *         The options should be comma seperated.
 *
 *         Examples:
 *
 *             unix> export MOCA_DMALLOC=off
 *             unix> export MOCA_DMALLOC=dynalloc
 *             unix> export MOCA_DMALLOC=nomemset
 *             unix> export MOCA_DMALLOC=nomemset,deadbeeflen=100
 *             unix> export MOCA_DMALLOC=trackmalloc=100
 *             unix> export MOCA_DMALLOC=outfile=dmalloc_%d.log
 *
 *     off
 *
 *         This option turns off dmalloc at runtime so that it need not be
 *         compiled out.  Basically, the dmalloc package will do nothing more
 *         than call the native allocation routines.  With this option, the
 *         application should run nearly as fast as with dmalloc compiled out.
 *         The complement of this option is "on", which is the default.
 *
 *     nomemset
 *
 *         This option keeps dmalloc from memsetting malloced and freed memory 
 *         blocks using the "dead beef" string.  The disadvantage of using this 
 *         option is that it prevents dmalloc from interfering with a program 
 *         that assumes malloc regions are initialized or that use information 
 *         stored in a block after it has been freed.  If a program bombs only 
 *         without this option, it is probably inadvertently using an 
 *         uninitialized or freed memory block.  The advantage of using this 
 *         option is that the application should run faster if it uses dynamic 
 *         allocation heavily.  The complement of this option is "memset", which
 *         is the default.
 *
 *     deadbeeflen
 *
 *         This option specifies the length of the "dead beef" value, which is 
 *         written at the end of each allocated memory block.  The greater this 
 *         value, the more likely memory overwrites will be detected.  A value 
 *         of 0 will prevent dmalloc from being able to detect any memory 
 *         overwrites. The default value is 4.  The disadvantage of setting 
 *         this option to a greater value is that it increases the size of 
 *         every dynamically allocated memory block by that many bytes, however,
 *         increasing the value dramatically (such as deadbeeflen=512) can be 
 *         useful for detecting particularly nasty overwrites (the sort in 
 *         which the overwriting doesn't occur immediately at the end of a 
 *         memory block, but rather skips into middle of the next memory block).
 *
 *     time
 *
 *         This option causes dmalloc to track the allocation time of each 
 *         memory block rather than a call sequence number for the given 
 *         allocation function.  The disadvantage of using this option is that 
 *         the application will run slower if it uses dynamic allocation 
 *         heavily.  The advantage of specifying this option is only that, 
 *         while debugging, the time stamps might help to cross-referencing 
 *         allocations with events specified in a time-stamped log file.  The 
 *         complement of this option is "notime", which is the default.
 *
 *     nofreenull
 *
 *         This option causes dmalloc to complain if the application passes 
 *         NULL to free.  This is different from the ANSI-C defined behavior 
 *         which is to allow NULL as the argument to free with no effect.  The 
 *         complement of this option is "freenull", which is the default.
 *
 *     noabort
 *
 *         This option causes dmalloc to not abort and create a core dump on
 *         UNIX or crash dump on Win32.  By default, if a malloc( ),
 *         calloc( ) or realloc( ) call fails, the application immediately
 *         exits, creating a core/crash dump.
 *
 *     maxalloc
 *
 *         This option sets up the maximum (static) number of allocations
 *         that dmalloc will support.  Once there have been more than this
 *         many memory allocations, it is assumed that the memory has been 
 *         used up.
 *
 *     dynalloc
 *
 *         This option tells dmalloc to support dynamic memory allocation.
 *         The size of the allocation list to track will grow dynamically
 *         as memory is allocated.  This may cause problems if significant
 *         heap corruption occurs (it uses the heap).  If problems occur
 *         with this option enabled, run in static (maxalloc) allocation
 *         mode, instead.
 *
 * 
 *     trackmalloc
 *     trackcalloc
 *     trackrealloc
 *          
 *         These options cause dmalloc to perform an abort on the n'th call
 *         to malloc, calloc or realloc.  This can aid in debugging memory
 *         overwrites by forcing a core dump on the exact call to malloc,
 *         calloc, realloc that you know causes an overwrite.
 *
 *     outfile
 *
 *         This option redirects any messages to be written to the given
 *         output filename, which will be written to the current working 
 *         directory. Any references to %d in the output file will be replaced
 *         with the processes' PID.
 *
 * DEBUGGER
 *
 *     The function misDebugReport is usually invoked automatically at exit.
 *     However, it may be useful to call it from within within a source level
 *     debugger (such as dbx) in this way: p misDebugReport("", 0)
 *
 * WARNING
 *
 *     To accurately report the status of dynamic allocations, all code
 *     that calls calloc, malloc, realloc or free should be compiled with
 *     MOCA_DEBUG_MALLOC defined in mocaconfig.h.
 *
 ******************************************************************************/


#ifndef TRUE
#define TRUE  1
#define FALSE 0
#endif

/*
 * This defines how many individual dynamically allocated memory blocks that 
 * dmalloc can track.  Some applications may be incompatible with this package
 * (ie. allocation functions will return NULL prematurely) unless this
 * maximum is increased.
 */

#ifndef DEFAULT_MAXALLOC
#define DEFAULT_MAXALLOC 16276
#endif

#ifndef HASH_TARGET
#define HASH_TARGET 20
#endif

/*
 * This makes sure the malloc, calloc, realloc and free functions don't map
 * back to the debugging functions, creating a recursive condition.
 */

#define MOCA_DEBUG_MALLOC_IGNORE

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <time.h>
#include <string.h>
#include <limits.h>

#include <mislib.h>
#include <oslib.h>

#include "llist.h"


/*
 *  Type Definitions
 */

typedef struct
{
    LNODE link;
    void *addr;
    size_t size;
    time_t time;
    char *function;
    char *args[2];
    char *file;
    int line;
} MEMNODE;

typedef struct memcache {
    OSFPTR function;
    void *address;
    struct memcache *next;
} MEMCACHE;


/*
 *  Static Variables
 */

static char *achtung     = "MOCA_DMALLOC";
static char *report_from = "atexit";

static int report_line        = 0;
static int initialized        = 0;

static int malloc_call_count  = 0;
static int calloc_call_count  = 0;
static int realloc_call_count = 0;

static unsigned int DEAD_BEEF = 0xDEADBEEF;

static LLIST *memhash,
	     freelist;

static MEMNODE *nodes;

static MEMCACHE *cacheHead;

static struct
{
    int off;
    int nomemset;
    int deadbeeflen;
    int notime;
    int nofreenull;
    int noabort;
    int maxalloc;
    int trackmalloc;
    int trackcalloc;
    int trackrealloc;
    int hashsize;
    int dynalloc;
    FILE *outfile;
} opt =
{
    TRUE,
    FALSE,
    sizeof DEAD_BEEF,
    TRUE,
    FALSE,
    FALSE,
    DEFAULT_MAXALLOC,
    0,
    0,
    0,
    1,
    TRUE,
    NULL
};


static int hashval(void *addr)
{
    unsigned int intaddr = (int) addr;
    /* ignore the low-order 3 bits */
    unsigned int hash = (intaddr >> 3) * 37 + (intaddr >> 11);
    return hash % opt.hashsize;
}

/*
 *  FUNCTION: ParseOptions
 *
 *  PURPOSE:  Parse options from the MOCA_DMALLOC environment variable.
 *
 *  RETURNS:  void
 */

static void ParseOptions(void)
{
    char *option;
    char *unconverted;
    static char buffer[256];
    char *context;

    if (((option = getenv(achtung)) != NULL) && 
	(strlen(option) < sizeof buffer))
    {
	strncpy(buffer, option, sizeof buffer);
	context = buffer;
	option = misStrsep(&context, ",=");
	while (option != NULL)
	{
	    if (misCiStrcmp(option, "off") == 0)
	    {
		opt.off = TRUE;
	    }
	    else if (misCiStrcmp(option, "on") == 0)
	    {
		opt.off = FALSE;
	    }
	    else if (misCiStrcmp(option, "nomemset") == 0)
	    {
		opt.nomemset = TRUE;
	    }
	    else if (misCiStrcmp(option, "memset") == 0)
	    {
		opt.nomemset = FALSE;
	    }
	    else if (misCiStrcmp(option, "maxalloc") == 0)
	    {
		if ((option = misStrsep(&context, ",")) == NULL)
		{
		    fprintf(opt.outfile,
			    "%-6d %s - %s - %d: missing value for option "
			    "maxalloc set in environment variable %s.\n",
			    osGetProcessId(), achtung, "options", 0, achtung);
		    break;
		}

		opt.maxalloc = strtoul(option, &unconverted, 10);
		if (*unconverted != '\0')
		{
		    opt.maxalloc = DEFAULT_MAXALLOC;
		    fprintf(opt.outfile,
			    "%-6d %s - %s - %d: invalid maxalloc %s set in "
			    "environment variable %s.\n", 
                            osGetProcessId(), achtung, "options", 0, 
                            option, achtung);
		}
                opt.dynalloc = FALSE;
	    }
	    else if (misCiStrcmp(option, "deadbeeflen") == 0)
	    {
		if ((option = misStrsep(&context, ",")) == NULL)
		{
		    fprintf(opt.outfile,
			    "%-6d %s - %s - %d: missing value for option "
			    "deadbeeflen set in environment variable %s.\n",
			    osGetProcessId(), achtung, "options", 0, achtung);
		    break;
		}

		opt.deadbeeflen = strtoul(option, &unconverted, 10);
		if (*unconverted != '\0')
		{
		    opt.deadbeeflen = sizeof DEAD_BEEF;
		    fprintf(opt.outfile,
			    "%-6d %s - %s - %d: invalid deadbeeflen %s set in "
			    "environment variable %s.\n", 
                            osGetProcessId(), achtung, "options", 0, 
                            option, achtung);
		}
	    }
	    else if (misCiStrcmp(option, "trackmalloc") == 0)
	    {
		if ((option = misStrsep(&context, ",")) == NULL)
		{
		    fprintf(opt.outfile,
			    "%-6d %s - %s - %d: missing value for option "
			    "trackmalloc set in environment variable %s.\n",
			    osGetProcessId(), achtung, "options", 0, achtung);
		    break;
		}

		opt.trackmalloc = strtoul(option, &unconverted, 10);
		if (*unconverted != '\0')
		{
		    opt.trackmalloc = 0;
		    fprintf(opt.outfile,
			    "%-6d %s - %s - %d: invalid trackmalloc %s set in "
			    "environment variable %s.\n", 
                            osGetProcessId(), achtung, "options", 0, 
                            option, achtung);
		}
	    }
	    else if (misCiStrcmp(option, "trackcalloc") == 0)
	    {
		if ((option = misStrsep(&context, ",")) == NULL)
		{
		    fprintf(opt.outfile,
			    "%-6d %s - %s - %d: missing value for option "
			    "trackcalloc set in environment variable %s.\n",
			    osGetProcessId(), achtung, "options", 0, achtung);
		    break;
		}

		opt.trackcalloc = strtoul(option, &unconverted, 10);
		if (*unconverted != '\0')
		{
		    opt.trackcalloc = 0;
		    fprintf(opt.outfile,
			    "%-6d %s - %s - %d: invalid trackcalloc %s set in "
			    "environment variable %s.\n", 
                            osGetProcessId(), achtung, "options", 0, 
                            option, achtung);
		}
	    }

	    else if (misCiStrcmp(option, "trackrealloc") == 0)
	    {
		if ((option = misStrsep(&context, ",")) == NULL)
		{
		    fprintf(opt.outfile,
			    "%-6d %s - %s - %d: missing value for option "
			    "trackrealloc set in environment variable %s.\n",
			    osGetProcessId(), achtung, "options", 0, achtung);
		    break;
		}

		opt.trackrealloc = strtoul(option, &unconverted, 10);
		if (*unconverted != '\0')
		{
		    opt.trackrealloc = 0;
		    fprintf(opt.outfile,
			    "%-6d %s - %s - %d: invalid trackrealloc %s set in "
			    "environment variable %s.\n", 
                            osGetProcessId(), achtung, "options", 0, 
                            option, achtung);
		}
	    }

	    else if (misCiStrcmp(option, "notime") == 0)
	    {
		opt.notime = TRUE;
	    }
	    else if (misCiStrcmp(option, "time") == 0)
	    {
		opt.notime = FALSE;
	    }
	    else if (misCiStrcmp(option, "nofreenull") == 0)
	    {
		opt.nofreenull = TRUE;
	    }
	    else if (misCiStrcmp(option, "freenull") == 0)
	    {
		opt.nofreenull = FALSE;
	    }
	    else if (misCiStrcmp(option, "noabort") == 0)
	    {
		opt.noabort = TRUE;
	    }
	    else if (misCiStrcmp(option, "dynalloc") == 0)
	    {
		opt.dynalloc = TRUE;
	    }
	    else if (misCiStrcmp(option, "outfile") == 0)
	    {
		if ((option = misStrsep(&context, ",")) == NULL)
		{
		    fprintf(opt.outfile,
			    "%-6d %s - %s - %d: missing value for option "
			    "trackrealloc set in environment variable %s.\n",
			    osGetProcessId(), achtung, "options", 0, achtung);
		    break;
		}

		opt.outfile = fopen(option, "w+");
		if (!opt.outfile)
		{
		    opt.outfile = stdout;
		    fprintf(opt.outfile,
			    "%-6d %s - %s - %d: could not open outfile %s: %s"
			    "environment variable %s.\n", 
                            osGetProcessId(), achtung, "options", 0, 
                            option, osError());
		}
	    }
	    else
	    {
		fprintf(opt.outfile,
			"%-6d %s - %s - %d: unknown option %s set in "
			"environment variable %s.\n", 
                        osGetProcessId(), achtung, "options", 0, 
                        option, achtung);
	    }

	    option = misStrsep(&context, ",=");
	}
    }

    opt.hashsize = opt.maxalloc / HASH_TARGET;
}


/*
 *  FUNCTION: memset_pattern
 *
 *  PURPOSE:
 *
 *  RETURNS:  
 */

static void *memset_pattern(void *addr, void *pattern, size_t pat_len,
			    size_t len)
{
    register size_t plen;
    register unsigned char *s,
                           *p;

    for (plen=pat_len, s=(unsigned char *) addr, p=(unsigned char *) pattern;
	 0 < len; s++, 
	 p++, len--, plen--)
    {
	if (0 == plen)
	{
	    plen = pat_len;
	    p = (unsigned char *) pattern;
	}
	*s = *p;
    }

    return addr;
}


/*
 *  FUNCTION: memcmp_pattern
 *
 *  PURPOSE:
 *
 *  RETURNS:  
 */

static int memcmp_pattern(void *addr, void *pattern, size_t pat_len, size_t len)
{
    register size_t plen;

    register unsigned char *s,
                           *p;

    for (plen=pat_len, s=(unsigned char *) addr, p=(unsigned char *) pattern;
	 0 < len; s++, 
	 p++, len--, plen--)
    {
	if (0 == plen)
	{
	    plen = pat_len;
	    p = (unsigned char *) pattern;
	}
	if (*s != *p)
	    return ((*s < *p) ? -1 : 1);
    }

    return 0;
}


/*
 *  FUNCTION: offset_pattern
 *
 *  PURPOSE:
 *
 *  RETURNS:  
 */

static int offset_pattern(void *addr, void *pattern, size_t pat_len, size_t len)
{
    int last = 0;

    register size_t plen;

    register unsigned char *s,
                           *p;

    for (plen=pat_len, s=(unsigned char *) addr, p=(unsigned char *) pattern;
	 len > 0; 
	 s++, p++, len--, plen--)
    {
	if (0 == plen)
	{
	    plen = pat_len;
	    p = (unsigned char *) pattern;
	}
	if (*s != *p)
	    last = s - (unsigned char *) addr;
    }

    return last;
}


/*
 *  FUNCTION: local_ctime
 *
 *  PURPOSE:
 *
 *  RETURNS:  
 */

static char *local_ctime(time_t *timer)
{
    static char scount[(sizeof(time_t)) * CHAR_BIT / 3 + sizeof "on the xx call"];
    static char stime[sizeof "Mon Jan 01 12:59:59"];

    long modval1, 
	 modval2;

    if (!opt.notime)
    {
	strncpy(stime, ctime(timer), (sizeof stime) - 1);
	return stime;
    }

    modval1 = (long) *timer % 100;
    modval2 = (long) *timer % 10;

    switch (((modval1 == (10 + modval2)) ? modval1 : modval2))
    {
    case 1:
	sprintf(scount, "on the %ldst call", (long) *timer);
	break;
    case 2:
	sprintf(scount, "on the %ldnd call", (long) *timer);
	break;
    case 3:
	sprintf(scount, "on the %ldrd call", (long) *timer);
	break;
    default:
	sprintf(scount, "on the %ldth call", (long) *timer);
    }

    return scount;
}


/*
 *  FUNCTION: function
 *
 *  PURPOSE:
 *
 *  RETURNS:  
 */

static char *function(MEMNODE * node)
{
    static char call[1024];

    if (node->args[1])
    {
	sprintf(call, "%s(%s%s%s)", node->function, 
				    node->args[0] ? node->args[0] : "", 
				    node->args[0] ? ", "          : "", 
				    node->args[1]);
    }
    else
    {
	sprintf(call, "%s(%s)", node->function, node->args[0]);
    }

    return call;
}


static MEMNODE *nextfreenode()
{
    MEMNODE *node;
    node = (MEMNODE *) ll_rem_head(&freelist);

    if (!node && opt.dynalloc) {
	int newmaxalloc = opt.maxalloc * 2;
	int newhashsize = newmaxalloc / HASH_TARGET;
	int oldmaxalloc = opt.maxalloc;
	int oldhashsize = opt.hashsize;
	int i;

	fprintf(opt.outfile,
		"%-6d %s - resizing allocation: %d --> %d, (hash: %d --> %d)\n",
		osGetProcessId(), achtung, oldmaxalloc, newmaxalloc, 
                oldhashsize, newhashsize);

	/*
	 * Before we reallocate the node list, we'll need to remove
	 * all nodes from the existing hash lists.
	 */
	for (i = 0; i < oldmaxalloc; i++)
	{
	    MEMNODE *node = &nodes[i];
	    ll_remove(&memhash[hashval(node->addr)], (LNODE *) node);
	}

	/* We double maxalloc and produce more free nodes. */
	if ((nodes = realloc(nodes, newmaxalloc * sizeof(MEMNODE))) == NULL)
	    OS_PANIC;

	/* initialize those new free nodes (only!) to null */
	memset(nodes + oldmaxalloc, 0, sizeof(MEMNODE) * (newmaxalloc - oldmaxalloc));

	/* Allocate a new hash table */
        memhash = realloc(memhash, newhashsize * sizeof *memhash);
	memset(memhash, 0, newhashsize * sizeof *memhash);

        for (i = 0; i < newhashsize; i++)
        {
            ll_init(&memhash[i], 0);
        }

	/* set the global hashsize so our hashval function will work */
	opt.hashsize = newhashsize;

	/* Give all the old nodes a home in their new hash heaven */
	for (i = 0; i < oldmaxalloc; i++)
	{
	    MEMNODE *node = &nodes[i];
	    ll_add_head(&memhash[hashval(node->addr)], (LNODE *) node);
	}

	/* Put the rest of the nodes into the free list, which had been empty */
	for (i = oldmaxalloc; i < newmaxalloc; i++)
	{
	    ll_add_tail(&freelist, (LNODE *) &nodes[i]);
	    nodes[i].link.type = 0;
	}

	/* Reset the max allocation size for next time. */
	opt.maxalloc = newmaxalloc;
	
	/* Now that we've done all that, this should return a value */
	node = (MEMNODE *) ll_rem_head(&freelist);
    }

    return node;
}

/*
 *  FUNCTION: ll_insert
 *
 *  PURPOSE:  Insert a node into a linked list.  The node is inserted
 *            following prev.  If prev is NULL, node will be inserted at 
 *            head.  
 *         
 *  RETURNS:  Pointer to the node.
 */

static LNODE *ll_insert(LLIST *list, LNODE *prev, LNODE *node)
{
    /* Validate the arguments. */
    if (NULL == list)
	return NULL;
    if (NULL == node)
	return NULL;
    if (list->type != node->type)
	return NULL;
    if (NULL == prev)
	prev = &list->head;

    /* Insert the node. */
    node->next       = prev->next;
    node->next->prev = node;
    node->prev       = prev;
    prev->next       = node;

    /* Increment the number of nodes in the list. */
    list->nnodes++;

    return node;
}


/*
 *  FUNCTION: ll_remove
 *
 *  PURPOSE:  Remove a node from a linked list. 
 *         
 *  RETURNS:  Pointer to the node.
 */

static LNODE *ll_remove(LLIST *list, LNODE *node)
{
    /* Validate the arguments. */
    if (NULL == list)
	return NULL;
    if (0 == list->nnodes)
	return NULL;
    if (NULL == node)
	node = list->head.next;
    if (list->type != node->type)
	return NULL;

    /* Remove the node. */
    node->prev->next = node->next;
    node->next->prev = node->prev;
    node->prev       = NULL;
    node->next       = NULL;

    /* Decrement the number of nodes in the list. */
    list->nnodes--;

    return node;
}


/*
 *  FUNCTION: TrapMalloc
 *
 *  PURPOSE:  Trap the n'th call to malloc by forcing a core dump.
 *
 *  RETURNS:  void
 */

static void TrapMalloc(int call)
{
    if (call == opt.trackmalloc)
	osDumpCore( );

    return;
}


/*
 *  FUNCTION: TrapCalloc
 *
 *  PURPOSE:  Trap the n'th call to calloc by forcing a core dump.
 *
 *  RETURNS:  void
 */

static void TrapCalloc(int call)
{
    if (call == opt.trackcalloc)
	osDumpCore( );

    return;
}


/*
 *  FUNCTION: TrapRealloc
 *
 *  PURPOSE:  Trap the n'th call to realloc by forcing a core dump.
 *
 *  RETURNS:  void
 */

static void TrapRealloc(int call)
{
    if (call == opt.trackrealloc)
	osDumpCore( );

    return;
}


/*
 *  FUNCTION: misDebugExit
 *
 *  PURPOSE:  
 *
 *  RETURNS:  void
 */

static void misDebugExit(void)
{
    MEMNODE *node;
    int i;

    if (! initialized)
	return;

    for (i = 0; i < opt.hashsize; i++)
    {
        for (node = (MEMNODE *) ll_tail(&memhash[i]);
             node && node->link.prev;
             node = (MEMNODE *) node->link.prev)
        {
            if (memcmp_pattern(((char *) node->addr) + node->size, &DEAD_BEEF, sizeof DEAD_BEEF, opt.deadbeeflen) != 0)
            {
                fprintf(opt.outfile,
                        "%-6d %s - %s - %d: the block at address %p (%d "
                        "bytes requested %s) returned by %s at line "
                        "%d of file %s, that hasn't been freed, "
                        "appears to have been overwritten (%d "
                        "bytes). The arena may be trashed!\n",
                        osGetProcessId(), "MEMORY LEAK & OVERWRITE",
                        report_from, report_line, node->addr,
                        (int) node->size, local_ctime(&node->time),
                        function(node), node->line, node->file,
                        offset_pattern(((char *) node->addr) + node->size,
                        &DEAD_BEEF, sizeof DEAD_BEEF,
                        opt.deadbeeflen));
            }
            else
            {
                fprintf(opt.outfile,
                        "%-6d %s - %s - %d: the block at address %p (%d "
                        "bytes requested %s) returned by %s at line "
                        "%d of file %s hasn't been freed.\n",
                        osGetProcessId(), "MEMORY LEAK",
                        report_from, report_line, node->addr, 
                        (int) node->size, local_ctime(&node->time), 
                        function(node), node->line, node->file);
            }

        }
    }


    return;
}


/*
 *  FUNCTION: misDebugReport
 *
 *  PURPOSE:  Called at exit.
 *
 *  RETURNS:  void
 */

void misDebugReport(char *file, const int line)
{
    int saved_line;

    char *saved_from;

    if (initialized)
    {
        saved_from = report_from;
        saved_line = report_line;

        report_from = file;
        report_line = line;

        misDebugExit();

        report_line = saved_line;
        report_from = saved_from;
    }

    return;
}


/*
 *  FUNCTION: misDebugInitialize
 *
 *  PURPOSE:  Initialize the debugging functions.
 *
 *  RETURNS:  void
 */

static void misDebugInitialize(void)
{
    register int i;

    opt.outfile = stdout;

    ParseOptions( );

    if (!opt.off)
    {
	/*
	 * This variable is technically unnecessary, but
	 * this is a kludge to prevent some compilers
	 * from realizing that the shift below is for 32 bits
	 * where ints are 32 bits and whining about it.
	 */
	int deadbeeflen = 4;

	if (sizeof DEAD_BEEF > deadbeeflen)
	{
	    DEAD_BEEF = (DEAD_BEEF << deadbeeflen * CHAR_BIT) + DEAD_BEEF;
	}

        memhash = calloc(opt.hashsize, sizeof *memhash);

        for (i = 0; i < opt.hashsize; i++)
        {
            ll_init(&memhash[i], 0);
        }

        ll_init(&freelist, 0);

	if ((nodes = calloc(opt.maxalloc, sizeof(MEMNODE))) == NULL)
	    OS_PANIC;

	for (i = 0; i < opt.maxalloc; i++)
	{
	    ll_add_tail(&freelist, (LNODE *) &nodes[i]);
	    nodes[i].link.type = 0;
	}

	if (osAtexit(misDebugExit) != 0)
	    OS_PANIC;
    }

    initialized = 1;

    return;
}


/*
 *  FUNCTION: misDebugMalloc
 *
 *  PURPOSE:  Allocate a memory block.
 *
 *  RETURNS:  Pointer to the memory block.
 */

void *misDebugMalloc(char *call, char *file, int line, size_t size, ...)
{
    register MEMNODE *node = (MEMNODE *) 0;
    register void *addr = (void *) 0;

    if (!initialized)
	misDebugInitialize( );

    if (opt.off)
    {
        addr = malloc(size);
        if (!addr && !opt.noabort)
            OS_PANIC;

	return addr;
    }

    if (size == 0)
    {
	fprintf(opt.outfile,
	        "%-6d %s - %s - %d: 0 size was passed to malloc.\n",
		osGetProcessId(), "IMPLEMENTATION-DEFINED BEHAVIOR", 
                file, line);
	return (void *) 0;
    }

    if ((addr = malloc(size + opt.deadbeeflen)))
    {
	memset_pattern(((char *) addr) + size, &DEAD_BEEF, sizeof DEAD_BEEF, opt.deadbeeflen);

	for (node = (MEMNODE *) ll_head(&memhash[hashval(addr)]); node && node->link.next;
	     node = (MEMNODE *) node->link.next)
	    if (addr == node->addr)
	    {
		fprintf(opt.outfile,
			"%-6d %s - %s - %d: the block at address %p (%d bytes "
			"requested %s) returned by %s at line %d of file "
			"%s has been freed outside the scope of %s "
			"(allocations may not be tracked correctly).\n",
			osGetProcessId(), achtung,
			report_from, report_line, node->addr, (int) node->size,
                        local_ctime(&node->time), function(node), node->line, 
                        node->file, __FILE__);
		break;
	    }

	if ((node && node->link.next) ||
	    ((node = nextfreenode()) &&
	     ll_add_head(&memhash[hashval(addr)], (LNODE *) node)))
	{
	    node->addr = addr;
	    node->size = size;
	    {
		va_list args;
		va_start(args, size);
		node->args[0] = va_arg(args, char *);
		node->args[1] = va_arg(args, char *);
		va_end(args);
	    }
	    if (!opt.notime)
		node->time = time((time_t *) 0);
	    else
	    {
		if (node->args[0] && node->args[1])
		{
		    node->time = ++realloc_call_count;
                    TrapRealloc(realloc_call_count);
                }
		else
		{
		    node->time = ++malloc_call_count;
                    TrapMalloc(malloc_call_count);
                }
	    }
	    node->function = call;
	    node->file = file;
	    node->line = line;

	    if (!opt.nomemset)
		memset_pattern(addr, &DEAD_BEEF, sizeof DEAD_BEEF, size);
	}
	else
	{
	    free(addr);
	    addr = (void *) 0;
	    if (node)
	    {
		ll_add_head(&freelist, (LNODE *) node);
	    }
	    else
	    {
		fprintf(opt.outfile,
			"%-6d %s - %s - %d: %d nodes used - if this isn't due "
                        "to a memory leak, set MAXALLOC to a greater "
			"maximum, then relink.\n",
			osGetProcessId(), "MEMORY LEAK?", file, line, 
                        opt.maxalloc);
	    }
	}
    }
    else
    {
        if (!opt.noabort)
            OS_PANIC;
    }

    return addr;
}


/*
 *  FUNCTION: misDebugCalloc
 *
 *  PURPOSE:  Allocate a cleared memory block.
 *
 *  RETURNS:  Pointer to the memory block.
 */

void *misDebugCalloc(char *call, char *file, int line,
		     int nel, size_t size,...)
{
    register MEMNODE *node = (MEMNODE *) 0;
    register void *addr = (void *) 0;
    static int track_call = -1;

    if (!initialized)
	misDebugInitialize( );

    if (opt.off)
    {
	addr = calloc(nel, size);
        if (!addr && !opt.noabort)
            OS_PANIC;

	return addr;
    }

    if ((nel * size) == 0)
    {
	fprintf(opt.outfile,
	        "%-6d %s - %s - %d: 0 size was passed to calloc.\n",
		osGetProcessId(), "IMPLEMENTATION-DEFINED BEHAVIOR", 
                file, line);
	return (void *) 0;
    }

    if ((addr = malloc(nel * size + opt.deadbeeflen)))
    {
	memset(addr, '\0', nel * size);
	memset_pattern(((char *) addr) + nel * size, &DEAD_BEEF, sizeof DEAD_BEEF, opt.deadbeeflen);

	for (node = (MEMNODE *) ll_head(&memhash[hashval(addr)]); node && node->link.next;
	     node = (MEMNODE *) node->link.next)
	    if (addr == node->addr)
	    {
		fprintf(opt.outfile,
			"%-6d %s - %s - %d: the block at address %p (%d bytes requested %s) returned by %s at line %d of file %s has been freed outside the scope of %s (allocations may be tracked incorrectly).\n",
			osGetProcessId(), achtung,
			report_from, report_line, node->addr, (int) node->size,
                        local_ctime(&node->time), function(node), node->line, 
                        node->file, __FILE__);
		break;
	    }

	if ((node && node->link.next) ||
	    ((node = nextfreenode()) &&
	     ll_add_head(&memhash[hashval(addr)], (LNODE *) node)))
	{
	    node->addr = addr;
	    node->size = nel * size;
	    if (!opt.notime)
		node->time = time((time_t *) 0);
	    else
		node->time = ++calloc_call_count;

            TrapCalloc(calloc_call_count);

	    node->function = call;
	    {
		va_list args;
		va_start(args, size);
		node->args[0] = va_arg(args, char *);
		node->args[1] = va_arg(args, char *);
		va_end(args);
	    }
	    node->file = file;
	    node->line = line;
	}
	else
	{
	    free(addr);
	    addr = (void *) 0;
	    if (node)
	    {
		ll_add_head(&freelist, (LNODE *) node);
	    }
	    else
	    {
		fprintf(opt.outfile,
			"%-6d %s - %s - %d: %d nodes used - if this isn't due "
		        "to a memory leak, set MAXALLOC to a greater "
			"maximum, then relink.\n",
			osGetProcessId(), "MEMORY LEAK?", 
                        file, line, opt.maxalloc);
	    }
	}
    }
    else
    {
        if (!opt.noabort)
            OS_PANIC;
    }

    return addr;
}


/*
 *  FUNCTION: misDebugRealloc
 *
 *  PURPOSE:  Resize an allocated memory block.
 *
 *  RETURNS:  Pointer to the memory block.
 */

void *misDebugRealloc(char *call, char *file, int line,
		      void *ptr, size_t size,...)
{
    register MEMNODE *node;
    register MEMNODE *node2;
    register void *addr = (void *) 0;

    if (!initialized)
	misDebugInitialize( );

    if (opt.off)
    {
	addr = realloc(ptr, size);
        if (!addr && size && !opt.noabort)
            OS_PANIC;

	return addr;
    }

    if (size == 0)
    {
	fprintf(opt.outfile,
	        "%-6d %s - %s - %d: 0 size was passed to realloc.\n",
		osGetProcessId(), "IMPLEMENTATION-DEFINED BEHAVIOR", 
                file, line);
	return NULL;
    }

    if (ptr == NULL)
    {
	char *arg1;
	char *arg2;
	va_list args;

	va_start(args, size);
	arg1 = va_arg(args, char *);
	arg2 = va_arg(args, char *);
	va_end(args);

	return misDebugMalloc(call, file, line, size, arg1, arg2);
    }

    for (node = (MEMNODE *) ll_head(&memhash[hashval(ptr)]); node && node->link.next;
	 node = (MEMNODE *) node->link.next)
	if (ptr == node->addr)
	    break;

    if (node && node->link.next)
    {
	if (memcmp_pattern(((char *) node->addr) + node->size, &DEAD_BEEF, sizeof DEAD_BEEF, opt.deadbeeflen) != 0)
	{
	    fprintf(opt.outfile,
		    "%-6d %s - %s - %d: the block at address %p (%d bytes "
		    "requested %s) returned by %s at line %d of file "
		    "%s appears to have been overwritten(%d bytes). The "
		    "arena may be trashed!\n",
		    osGetProcessId(), "MEMORY OVERWRITE", 
                    report_from, report_line,
		    node->addr, (int) node->size,
		    local_ctime(&node->time), function(node),
		    node->line, node->file,
		    offset_pattern(((char *) node->addr) + node->size,
			           &DEAD_BEEF, sizeof DEAD_BEEF,
				   opt.deadbeeflen));
	}

	if ((addr = realloc(ptr, size + opt.deadbeeflen)))
	{
	    memset_pattern(((char *) addr) + size, &DEAD_BEEF, sizeof DEAD_BEEF, opt.deadbeeflen);

	    if (addr != node->addr)
	    {
		for (node2 = (MEMNODE *) ll_head(&memhash[hashval(addr)]);
		     node2 && node2->link.next;
		     node2 = (MEMNODE *) node2->link.next)
		    if (addr == node2->addr)
		    {
			fprintf(opt.outfile,
				"%-6d %s - %s - %d: the block at address %p (%d "
				"bytes requested %s) returned by %s at line "
				"%d of file %s has been freed outside the "
				"scope of %s (allocations may be tracked "
				"incorrectly).\n",
				osGetProcessId(), achtung,
				report_from, report_line, node2->addr,
				(int) node2->size, local_ctime(&node2->time),
			function(node2), node2->line, node2->file, __FILE__);
			ll_add_head(&freelist, (LNODE *) node2);
			break;
		    }

                /*
                 * We may have jumped hash values
                 */
                ll_remove(&memhash[hashval(node->addr)], (LNODE *) node);
                ll_add_head(&memhash[hashval(addr)], (LNODE *) node);
		node->addr = addr;
	    }

	    node->size = size;
	    if (!opt.notime)
		node->time = time((time_t *) 0);
	    else
		node->time = ++realloc_call_count;

            TrapRealloc(node->time);

	    node->function = call;
	    {
		va_list args;
		va_start(args, size);
		node->args[0] = va_arg(args, char *);
		node->args[1] = va_arg(args, char *);
		va_end(args);
	    }
	    node->file = file;
	    node->line = line;
	}
        else
        {
            if (!opt.noabort)
                OS_PANIC;
        }
    }
    else
    {
	fprintf(opt.outfile,
		"%-6d %s - %s - %d: %p passed to realloc wasn't allocated "
		"in scope of %s.\n",
		osGetProcessId(), "BAD ADDRESS?", file, line, ptr, __FILE__);
    }

    return addr;
}


/*
 *  FUNCTION: misDebugFree
 *
 *  PURPOSE:  Free an allocated memory block.
 *
 *  RETURNS:  void
 */

void misDebugFree(char *call, char *file, int line, void *addr,...)
{
    register MEMNODE *node;

    if (!initialized)
	misDebugInitialize( );

    if (opt.off || (addr == NULL && !opt.nofreenull))
    {
	free(addr);
	return;			/* dev_dp - Thu Nov 4 1993 */
    }

    for (node = (MEMNODE *) ll_head(&memhash[hashval(addr)]); 
	 node && node->link.next;
	 node = (MEMNODE *) node->link.next)
    {
	if (addr == node->addr)
	    break;
    }

    if (node && node->link.next)
    {
	if (memcmp_pattern(((char *) node->addr) + node->size, &DEAD_BEEF, sizeof DEAD_BEEF, opt.deadbeeflen) != 0)
	{
	    fprintf(opt.outfile,
		    "%-6d %s - %s - %d: the block at address %p (%d bytes "
		    "requested %s) returned by %s at line %d of file "
		    "%s appears to have been overwritten (%d bytes). "
		    "The arena may be trashed!\n",
		    osGetProcessId(), "MEMORY OVERWRITE", 
                    file, line, node->addr,
		    (int) node->size, local_ctime(&node->time),
		    function(node), node->line, node->file,
		    offset_pattern(((char *) node->addr) + node->size,
			           &DEAD_BEEF, sizeof DEAD_BEEF,
				   opt.deadbeeflen));
	}

	if (!opt.nomemset)
	    memset_pattern(addr, &DEAD_BEEF, opt.deadbeeflen, node->size);
	free(addr);
	ll_remove(&memhash[hashval(addr)], (LNODE *) node);
	ll_add_tail(&freelist, (LNODE *) node);
    }
    else
    {
	va_list args;
	va_start(args, addr);
	fprintf(opt.outfile,
		"%-6d %s - %s - %d: %p passed%s%s to free wasn't allocated "
		"in scope of %s.\n", 
                osGetProcessId(), "BAD ADDRESS?", file, line, addr,
		" as ", va_arg(args, char *),
		__FILE__);
	va_end(args);
    }
}



/*
 * The following functions are used to allow components to flag cached 
 * memory elements.  A single atexit handler can then be used to free 
 * all cached data elements.
 *
 * These functions do nothing if MOCA_DMALLOC is turned off.
 */


/*
 *  FUNCTION: misFlagCachedMemory
 *
 *  PURPOSE:  Flag memory that is to be cached by the component.
 *            The arguments provided allow the component to specify both 
 *            a function address as well as a pointer to an object to be
 *            freed by the specified function.
 *
 *  RETURNS:  void
 */

void misFlagCachedMemory(OSFPTR function, void *address)
{
    MEMCACHE *ptr;
    static int first_time = -1;

    if (!initialized)
	misDebugInitialize( );

    if (opt.off)
	return;
    
    if (first_time < 0)
    {
	osAtexit(misReleaseCachedMemory);
	first_time = 1;
    }

    ptr = (MEMCACHE *) calloc(1, sizeof(MEMCACHE));

    ptr->function = function;
    ptr->address  = address;
    
    ptr->next = cacheHead;
    cacheHead = ptr;

    return;
}


/*
 *  FUNCTION: misReleasedCachedMemory
 *
 *  PURPOSE:  This function is designed to be called as a atexit( ) handler.  
 *            It will traverse through the linked list of cached memory 
 *            elements and call the memory free function for each node.
 *
 *  RETURNS:  void
 */

void misReleaseCachedMemory( )
{
    MEMCACHE *ptr, 
	     *last;

    if (!initialized)
	misDebugInitialize( );

    if (opt.off)
	return;

    last = NULL;
    for (ptr = cacheHead; ptr; last = ptr, ptr = ptr->next)
    {
	if (last)
	    free(last);
	
	if (ptr->address)
	{
	    /* call the function - with args */
	    if (ptr->function)
	        (ptr->function) (ptr->address);
            else
		misDebugFree("free", __FILE__, __LINE__, ptr->address, "ptr->address");
	}
	else
	{
	    /* call the function */
	    (ptr->function)( );
	}
    }
    if (last)
	free(last);

    cacheHead = NULL;

    return;
}	


/*
 *  FUNCTION: misRemovedCachedMemoryEntry
 *
 *  PURPOSE:  Remove an element from the linked list of cached memory elements.
 *
 *  RETURNS:   void
 */

void misRemoveCachedMemoryEntry(void *address)
{
    MEMCACHE *ptr, 
	     *last;

    if (!initialized)
	misDebugInitialize( );

    if (opt.off)
	return;

    last = NULL;
    for (ptr = cacheHead; ptr; last = ptr, ptr = ptr->next)
    {
	if (ptr->address == address)
	{
	    if (last)
		last->next = ptr->next;
	    else
		cacheHead = ptr->next;
	
	    free(ptr);

	    return;
	}
    }

    return;
}
