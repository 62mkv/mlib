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
 *#END*************************************************************************/

#define MOCA_ALL_SOURCE

#include <moca.h>

#ifdef UNIX
# include <fcntl.h>
# include <unistd.h>
# include <sys/types.h>
# include <sys/mman.h>
#endif

#include <mocagendef.h>
#include <mocaerr.h>
#include <common.h>
#include "osprivate.h"

long osUnmapFile(void *mem)
{
    unsigned long size;
    long mode;

#ifdef UNIX /* { */
    int fd;

    os_GetMapInfo(mem, &size, &fd, &mode);

    close(fd);

    if (munmap((caddr_t)mem, (size_t)size) <0)
    {
	return eERROR;
    }
#else /* WIN32 */ /* }{ */
    HANDLE hMutex;

    os_GetMapInfo(mem, &size, &hMutex, &mode);

    if (!UnmapViewOfFile(mem))
    {
	return eERROR;
    }

    CloseHandle(hMutex);
#endif /* } */

    os_DelMapInfo(mem);

    return eOK;
}
