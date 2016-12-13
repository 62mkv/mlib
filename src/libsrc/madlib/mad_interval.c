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
 *  Sam Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by Sam
 *  Corporation.
 *
 *  Sam Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by Sam Corporation.
 *
 *  $Copyright-End$
 *
 *#END*************************************************************************/

#include <string.h>
#include <madlib.h>
#include <mocaerr.h>

/*
 * Clear interval.
 */
void madTimeIntervalClear(MadTimeInterval *split) 
{
    memset(split, 0, sizeof(MadTimeInterval));
}

/*
 * Record start time for interval.
 */
void madTimeIntervalStart(MadTimeInterval *split) 
{
    madTimeGet(&split->start);
} 

/*
 * Record end time for interval and calculate difference.
 */
void madTimeIntervalEnd(MadTimeInterval *split) 
{
    madTimeGet(&split->end);

    if (split->end.nsec >= split->start.nsec) 
    {
        split->diff.sec = split->end.sec - split->start.sec;
        split->diff.nsec = split->end.nsec - split->start.nsec;
    } 
    else 
    { 
        split->diff.sec = (split->end.sec - split->start.sec) - 1;
        split->diff.nsec = (split->end.nsec - split->start.nsec)
            + 1000000000;
    }
}

/*
 * Return seconds difference. 
 */
int madTimeIntervalGetSeconds(MadTimeInterval *split) 
{
    return split->diff.sec;
}

/*
 * Return nanoseconds difference.
 */
int madTimeIntervalGetNanoseconds(MadTimeInterval *split) 
{
    return split->diff.nsec;
}

/*
 * Set seconds difference.
 */
void madTimeIntervalSetSeconds(MadTimeInterval *split, int seconds)
{
    split->diff.sec = seconds;
}

/*
 * Set nanoseconds difference.
 */
void madTimeIntervalSetNanoseconds(MadTimeInterval *split, int nanoseconds)
{
    split->diff.nsec = nanoseconds;
}
