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
#include <stdlib.h>
#include <string.h>

#ifdef UNIX
# include <fcntl.h>
# include <unistd.h>
# include <sys/types.h>
# include <sys/stat.h>
# include <sys/mman.h>
#endif

#include <mocaerr.h>
#include <mocagendef.h>
#include "osprivate.h"

#define MAP_BLOCK_SIZE 1024

static char null_block[MAP_BLOCK_SIZE];

#ifdef WIN32 /* { */

#define MUTEX_NAME_SUFFIX "_mutex"

static char *translate_mapname(char *in, char *out)
{
   char *tmp = out;
   while (*in)
   {
      *out = (*in == '\\'?'_':*in);

      out++;
      in++;
   }
   *out = 0;
   return tmp;
}

long osMapFile(char *infile, void **mem, long mode)
{
    DWORD prot;
    DWORD access, map_access;
    HANDLE hMap, hFile, hMutex;
    char *mapname;
    char filename[PATHNAME_LEN];

    GetFullPathName(infile, sizeof filename, filename, NULL);

    if (mode == OS_MAPMODE_RW)
    {
       access = GENERIC_READ | GENERIC_WRITE;
       prot   = PAGE_READWRITE;
       map_access = FILE_MAP_WRITE;
    }
    else
    {
       access = GENERIC_READ;
       prot   = PAGE_READONLY;
       map_access = FILE_MAP_READ;
    }

    hFile = CreateFile(filename, access, FILE_SHARE_READ|FILE_SHARE_WRITE,
		       NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);

    if (INVALID_HANDLE_VALUE == hFile)
    {
	return eERROR;
    }

    mapname = malloc(strlen(filename)+1+strlen(MUTEX_NAME_SUFFIX));
    translate_mapname(filename, mapname);

    hMap = CreateFileMapping(hFile, NULL, prot, 0, 0, mapname);

    if (INVALID_HANDLE_VALUE == hMap)
    {
	free(mapname);
	CloseHandle(hFile);
	return eERROR;
    }

    strcat(mapname, MUTEX_NAME_SUFFIX);
    hMutex = CreateMutex(NULL, FALSE, mapname);
    if (INVALID_HANDLE_VALUE == hMutex)
    {
	free(mapname);
	CloseHandle(hFile);
	CloseHandle(hMap);
	return eERROR;
    }

    free(mapname);
    CloseHandle(hFile);

    *mem = MapViewOfFile(hMap, map_access, 0, 0, 0);

    if (!*mem)
    {
	CloseHandle(hMap);
	CloseHandle(hMutex);
	return eERROR;
    }

    CloseHandle(hMap);

    /* Keep the hMutex HANDLE for future locking */
    os_PutMapInfo(*mem, 0, hMutex, mode);
    return eOK;
}

#else /* WIN32 */ /* }{ */

long osMapFile(char *infile, void **mem, long mode)
{
    struct stat statbuf;
    int openmode, mapmode, mapflags;
    int fd;
    caddr_t addr;

    /*
     * We've got different domains for all the permission bits we need to deal
     * with, so we need to translate them.
     */
    if (mode == OS_MAPMODE_RW)
    {
	openmode = O_RDWR;
	mapmode  = PROT_READ | PROT_WRITE;
	mapflags = MAP_SHARED;
    }
    else
    {
	openmode = O_RDONLY;
	mapmode  = PROT_READ;
	mapflags = MAP_PRIVATE;
    }

    /*
     * Open the file.  mmap maps a file that's already open, so we have to 
     * open it first.
     */
    if ((fd = open(infile, openmode)) < 0)
    {
	return eERROR;
    }

    /*
     * Do this to get file size, etc. We always map the entire file, so
     * we need to get the file size.
     */
    if (0 != fstat(fd, &statbuf))
    {
	close(fd);
	return eERROR;
    }

    /*
     * Perform the mapping.
     */
    addr = mmap(NULL, statbuf.st_size, mapmode, mapflags, fd, 0);

    if (addr == (caddr_t) - 1)
    {
	close(fd);
	return eERROR;
    }

    /*
     * We don't want to close the file, now, so we can use it for mutex access
     * to the file later.
     */

    os_PutMapInfo(addr, statbuf.st_size, fd, mode);

    *mem = addr;
    return eOK;
}

#endif /* } */


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


long osCreateMapFile(char *infile, long size)
{
    FILE *fp;
    long i;

    if (NULL == (fp = fopen(infile, "wb")))
    {
        return eERROR;
    }

    for (i = 0; i < size; i += MAP_BLOCK_SIZE)
    {
        if (size - i < MAP_BLOCK_SIZE)
        {
            fwrite(null_block, size - i, 1, fp);
        }
        else
        {
            fwrite(null_block, MAP_BLOCK_SIZE, 1, fp);
        }
    }

    fclose(fp);

    return eOK;
}


long osDeleteMapFile(char *infile)
{
    if (0 != remove(infile))
    {
        return eERROR;
    }

    return eOK;
}

