static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Get the time
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

#include <time.h>

#ifdef HAVE_GETTIMEOFDAY
#  include <sys/time.h>
#elif defined HAVE_FTIME
#  include <sys/types.h>
#  include <sys/timeb.h>
#endif

#include "osprivate.h"

OS_TIME *osGetTime(OS_TIME *tb)
{
   static OS_TIME ReturnBuf;
   OS_TIME buf;
   
#ifdef HAVE_GETTIMEOFDAY
   struct timeval timebuf;
   struct timezone zonebuf;
   gettimeofday(&timebuf, &zonebuf);
   buf.sec = timebuf.tv_sec;
   buf.msec = timebuf.tv_usec/1000;
#elif defined HAVE_FTIME
   struct timeb timebuf;
   ftime(&timebuf);
   buf.sec = timebuf.time;
   buf.msec = timebuf.millitm;
#endif

   if (tb) *tb = buf;
   ReturnBuf = buf;

   return &ReturnBuf;
}
