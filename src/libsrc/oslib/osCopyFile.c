static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Function to copy a file.
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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef UNIX
# include <fcntl.h>
# include <unistd.h>
# include <sys/stat.h>
# include <sys/mman.h>
# include <sys/types.h>
#endif

#include <mocaerr.h>
#include <mocagendef.h>

#include "osprivate.h"

#ifdef UNIX			/* { */

long osCopyFile(char *srcPathname, char *dstPathname, long mode)
{
    int srcfd,
        dstfd;

    void *src,
         *dst;

    struct stat srcStatbuf,
		dstStatbuf;

    /* Long term, we'll actually support the user passing us a mode. */
    mode = S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH;

    /* Get information about the source file. */
    if (stat(srcPathname, &srcStatbuf) < 0)
    {
	misLogError("stat: %s", osError( ));
	misLogError("filename: %s", srcPathname);
	misLogError("osCopyFile: Could not get source file information");
	return eERROR;
    }

    /* 
     * Don't bother if the source and destination files are the same. 
     * We can't just rely on comparing pathnames due to symnbolic links.
     */
    if (stat(dstPathname, &dstStatbuf) == 0)
    {
        if (srcStatbuf.st_ino == dstStatbuf.st_ino)
            return eOK;
    }

    /* Open the source file. */
    srcfd = open(srcPathname, O_RDONLY);
    if (srcfd < 0)
    {
	misLogError("open: %s", osError( ));
	misLogError("filename: %s", srcPathname);
	misLogError("osCopyFile: Could not open source file");
	return eERROR;
    }

    /* Open the destination file. */
    dstfd = open(dstPathname, O_RDWR | O_CREAT | O_TRUNC, mode);
    if (dstfd < 0)
    {
	misLogError("open: %s", osError( ));
	misLogError("filename: %s", dstPathname);
	misLogError("osCopyFile: Could not open destination file");
	close(srcfd);
	return eERROR;
    }

    /* Set the size of the destination file. */
    if (lseek(dstfd, srcStatbuf.st_size - 1, SEEK_SET) == -1)
    {
	misLogError("lseek: %s", osError( ));
	misLogError("filename: %s", dstPathname);
	misLogError("osCopyFile: Could not set destination file size");
	close(srcfd);
	close(dstfd);
	return eERROR;
    }

    if (write(dstfd, "", 1) != 1)
    {
	misLogError("write: %s", osError( ));
	misLogError("filename: %s", dstPathname);
	misLogError("osCopyFile: Could not set destination file size");
	close(srcfd);
	close(dstfd);
	return eERROR;
    }

    /* Map the source file. */
    src = mmap(0, srcStatbuf.st_size, PROT_READ, MAP_SHARED, srcfd, 0);
    if (src == (caddr_t) -1)
    {
	misLogError("mmap: %s", osError( ));
	misLogError("filename: %s", srcPathname);
	misLogError("osCopyFile: Could not map the source file");
	close(srcfd);
	close(dstfd);
	return eERROR;
    }

    /* Map the destination file. */
    dst = mmap(0, srcStatbuf.st_size, PROT_READ|PROT_WRITE, MAP_SHARED, dstfd, 0);
    if (dst == (caddr_t) -1)
    {
	misLogError("mmap: %s", osError( ));
	misLogError("filename: %s", dstPathname);
	misLogError("osCopyFile: Could not map the destination file");
	close(srcfd);
	close(dstfd);
        munmap(src, srcStatbuf.st_size);
	return eERROR;
    }

    /* Copy the source file to the destination file. */
    memcpy(dst, src, srcStatbuf.st_size);

    /* Close each file. */
    close(srcfd);
    close(dstfd);

    /* Unmap each file. */
    munmap(src, srcStatbuf.st_size);
    munmap(dst, srcStatbuf.st_size);

    return eOK;
}

#else	/* WIN32 */ /* }{ */

long osCopyFile(char *srcPathname, char *dstPathname, long mode)
{
    /* Don't bother if the source and destination files are the same. */
    if (_stricmp(srcPathname, dstPathname) == 0)
        return eOK;

    /* Copy the source file to the destination file. */
    if (! CopyFile(srcPathname, dstPathname, FALSE))
	return eERROR;

    return eOK;
}

#endif /* } */
