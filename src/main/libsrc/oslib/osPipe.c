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

#include <stdlib.h>
#include <string.h>

#ifdef USE_NSPIPE
# include <unistd.h>
# include <fcntl.h>
# include <sys/types.h>
# include <sys/stream.h>
# include <stropts.h>
#endif

#ifdef USE_MSGHDR
# ifdef MSGHDR_NEEDS_SYS_TYPES_H
#  include <sys/types.h>
# endif
# include <sys/socket.h>
#endif

#include <mocaerr.h>
#include "osprivate.h"

#ifdef USE_MSGHDR
long osPipe(PIPE_FD fd[2])	/* two file descriptors returned in fd[0] & fd[1] */
{
   if (0 == socketpair(AF_UNIX, SOCK_STREAM, 0, fd))
      return eOK;
   else
      return eERROR;
}

#elif defined(USE_NSPIPE)
/*
 * Create an unnamed stream pipe.
 */

# define NSPIPE_DEVICE      "/dev/spx"

long osPipe(PIPE_FD fd[2]) /* two file descriptors returned through here */
{
   struct strfdinsert ins;
   queue_t *pointer;

   /*
    * First open the stream clone device "/dev/spx" twice,
    * obtaining the two file descriptors.
    */

   if ((fd[0] = open(NSPIPE_DEVICE, O_RDWR)) < 0)
      return (eERROR);

   if ((fd[1] = open(NSPIPE_DEVICE, O_RDWR)) < 0)
   {
      close(fd[0]);
      return (eERROR);
   }

   /*
    * Now link these two streams together with an I_FDINSERT ioctl.
    */

   ins.ctlbuf.buf = (char *) &pointer;	/* no ctl info, just the ptr */
   ins.ctlbuf.maxlen = sizeof(queue_t *);
   ins.ctlbuf.len = sizeof(queue_t *);

   ins.databuf.buf = (char *) 0;	/* no data to send */
   ins.databuf.len = -1;	/* magic: must be -1, not 0, for stream pipe */
   ins.databuf.maxlen = 0;

   ins.fildes = fd[1];		/* the fd to connect with fd[0] */
   ins.flags = 0;		/* nonpriority message */
   ins.offset = 0;		/* offset of pointer in control buffer */

   if (ioctl(fd[0], I_FDINSERT, (char *) &ins) < 0)
   {
      close(fd[0]);
      close(fd[1]);
      return (eERROR);
   }

   return (eOK);		/* all OK */
}

#endif

#ifdef WIN32
long osPipe(PIPE_FD fd[2]) /* two handles returned in fd[0] & fd[1] */
{
    SECURITY_ATTRIBUTES security;

    security.nLength = sizeof security;
    security.lpSecurityDescriptor = NULL;
    security.bInheritHandle = TRUE;

    if (!CreatePipe(&fd[0], &fd[1], &security, 0))
	return eERROR;
    else
	return eOK;
}
#endif
