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

#include <stddef.h>

#ifdef UNIX

# include <sys/time.h>
# include <sys/types.h>

#ifdef HAVE_SYS_SELECT_H
# include <sys/select.h>
#endif 

# include <unistd.h>
# include <errno.h>

#endif

#include "osprivate.h"

void osSleep(int sec, int msec)
{
#ifdef WIN32   
   Sleep((DWORD) (sec * 1000) + msec);
#else
   int nfds;
   struct timeval tv;

   tv.tv_sec  = sec + msec / 1000;
   tv.tv_usec = (msec % 1000) * 1000;

   select(0, NULL, NULL, NULL, &tv);
   if (osSockErrno( ) == OS_EINTR)
       exit(-1);

#endif
}
