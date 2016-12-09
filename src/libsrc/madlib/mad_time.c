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
 *  Copyright (c) 2012
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie
 *  Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 *
 *#END*************************************************************************/

#include <mocaconfig.h>

#ifndef WIN32

#ifndef OSX

#include <time.h>
#include <madlib.h>

void madTimeGet(MadTime *t) 
{
    struct timespec tp;

#ifdef CLOCK_MONOTONIC
    clock_gettime(CLOCK_MONOTONIC, &tp);
#else
    clock_gettime(CLOCK_REALTIME, &tp);
#endif

    t->sec = tp.tv_sec;
    t->nsec = tp.tv_nsec;
}

#else /* OSX */

#include <mach/mach.h>
#include <mach/mach_time.h>
#include <madlib.h>

void madTimeGet(MadTime *t) 
{
    uint64_t abs_time;
    uint64_t ns_time;
    static mach_timebase_info_data_t timebase_info = {0, 0};

    if (timebase_info.denom == 0) {
        mach_timebase_info(&timebase_info);
    }

    abs_time = mach_absolute_time();

    ns_time = (abs_time * timebase_info.numer) / timebase_info.denom;

    t->sec = ns_time / 1000000000;
    t->nsec = ns_time % 1000000000;
}

#endif

#else /* WIN32 */

#include <windows.h>
#include <madlib.h>

void madTimeGet(MadTime *t) 
{
    static LARGE_INTEGER perf_freq = {0};
    LARGE_INTEGER perf_time;

    if (perf_freq.QuadPart == 0)
    {
        QueryPerformanceFrequency(&perf_freq);
    }

    QueryPerformanceCounter(&perf_time);

    t->sec = perf_time.QuadPart / perf_freq.QuadPart;
    t->nsec = ((perf_time.QuadPart % perf_freq.QuadPart) * 1000000000) /
        perf_freq.QuadPart;
}

#endif

