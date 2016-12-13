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

#define MOCA_POSIX_SOURCE

#include <moca.h>

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#ifdef UNIX
# include <sys/types.h>
# include <fcntl.h>
# include <unistd.h>
#endif

#include <mocagendef.h>
#include <mocaerr.h>
#include <common.h>
#include "osprivate.h"

#define UNLOCK     F_UNLCK;
#define WRITE_LOCK F_WRLCK;
#define READ_LOCK  F_RDLCK;

#ifdef WIN32 /* { */

long osLockMem(void *mem)
{
    unsigned long size;
    long mode;
    HANDLE hMutex;

    os_GetMapInfo(mem, &size, &hMutex, &mode);

    if (WAIT_OBJECT_0 != WaitForSingleObject(hMutex, INFINITE))
    {
	return eERROR;
    }

    return eOK;
}

long osUnlockMem(void *mem)
{
    unsigned long size;
    long mode;
    HANDLE hMutex;

    os_GetMapInfo(mem, &size, &hMutex, &mode);

    if (!ReleaseMutex(hMutex))
    {
	return eERROR;
    }

    return eOK;
}

#else /* UNIX */ /* }{ */

static int LockFile(int fd, off_t pos, off_t len, int type)
{
   struct flock fl;

   fl.l_type = type;
   fl.l_whence = SEEK_SET;
   fl.l_start = pos;
   fl.l_len = len;

   if (-1 == fcntl(fd, F_SETLKW, &fl))
   {
      return eERROR;
   }

   return eOK;
}

long osLockMem(void *mem)
{
    unsigned long size;
    long ret_status;
    int locktype;
    long mode;
    int fd;

    ret_status = os_GetMapInfo(mem, &size, &fd, &mode);

    /*
     * Don't let this screw us up
     */
    if (ret_status != eOK)
	return ret_status;

    if (mode == OS_MAPMODE_RW)
	locktype = F_WRLCK;
    else
	locktype = F_RDLCK;

    return LockFile(fd, 0, 0, locktype);
}

long osUnlockMem(void *mem)
{
    unsigned long size;
    long mode;
    int fd;

    os_GetMapInfo(mem, &size, &fd, &mode);

    return LockFile(fd, 0, 0, F_UNLCK);
}

#endif /* } */
