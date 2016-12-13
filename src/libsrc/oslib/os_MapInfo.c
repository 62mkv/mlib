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

#include <moca.h>

#include <stdlib.h>

#include <mocagendef.h>
#include <mocaerr.h>
#include <common.h>
#include "osprivate.h"

#define LIST_SIZE 10
typedef struct mapinfo
{
   void *memaddr;
   unsigned long size;
   long mode;
#ifdef UNIX
   int fd;
#else
   HANDLE hMutex;
#endif
} MAPINFO;

static MAPINFO list[LIST_SIZE];

#ifdef UNIX
long os_PutMapInfo(void *mem, unsigned long size, int fd, long mode)
#else
long os_PutMapInfo(void *mem, unsigned long size, HANDLE hMutex, long mode)
#endif
{
    int i;

    for (i=0;i<LIST_SIZE;i++)
    {
        if (!list[i].memaddr)
        {
	    list[i].memaddr = mem;
	    list[i].size = size;
	    list[i].mode = mode;
#ifdef UNIX
	    list[i].fd = fd;
#else
	    list[i].hMutex = hMutex;
#endif
	    return eOK;
        }
    }

    return eERROR;
}

#ifdef UNIX
long os_GetMapInfo(void *mem, unsigned long *size, int *fd, long *mode)
#else
long os_GetMapInfo(void *mem, unsigned long *size, HANDLE *hMutex, long *mode)
#endif
{
    int i;

    for(i=0;i<LIST_SIZE;i++)
    {
	if (list[i].memaddr == mem)
	{
	    *size = list[i].size;
	    *mode = list[i].mode;
#ifdef UNIX
	    *fd = list[i].fd;
#else
	    *hMutex = list[i].hMutex;
#endif
	    return eOK;
	}
    }
    *size = 0;
    return eERROR;
}

long os_DelMapInfo(void *mem)
{
    int i;
    for(i=0;i<LIST_SIZE;i++)
    {
	if (list[i].memaddr == mem)
	{
	    list[i].memaddr = NULL;
	    return eOK;
	}
    }

    return eOK;
}
