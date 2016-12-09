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

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <math.h>

#include <mocaerr.h>
#include <mislib.h>

typedef struct {
    int year;
    int month;
    int day;
    int hour;
    int minute;
    int second;
} TIMESTRUCT;

static int daysBeforeMonth[] = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
static int daysBeforeMonthInLeapYear[] = {0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335};

static int isleap(int year)
{
    return (year % 4 == 0 && (year % 100 !=0 || year % 400 == 0));
}

static int roundToInt(double d)
{
    return (int) ((d < 0.0) ? ceil(d - 0.5) : floor(d + 0.5));
}

static long timeStructToTimeVal(TIMESTRUCT* time, MIS_DTVALUE *val)
{
    val->seconds = time->second + time->minute * 60 + time->hour * 3600;

    val->days = (time->year - 1) * 365 + ((time->year - 1)/4) - ((time->year - 1)/100) + ((time->year - 1)/400);
    if (isleap(time->year))
        val->days += daysBeforeMonthInLeapYear[time->month -1];
    else
        val->days += daysBeforeMonth[time->month - 1];
    val->days += time->day - 1;

    return eOK;
}

static long normalizeTimeVal(MIS_DTVALUE *val)
{
    while (val->seconds < 0)
    {
        val->seconds += 86400;
        val->days -= 1;
    }

    val->days += val->seconds / 86400;
    val->seconds %= 86400;

    return eOK;
}

static long timeValToTimeStruct(MIS_DTVALUE *val, TIMESTRUCT *time)
{
    int days;
    int *daylist;
    int ii;

    normalizeTimeVal(val);

    days = val->days;
    
    time->year = 1;
    time->year += (days / 146097) * 400;
    days = days % 146097;
    if (days == 146096)
    {
        time->year += 399;
        days = 365;
    }
    else
    {
        time->year += (days / 36524) * 100;
        days = days % 36524;
        time->year += (days / 1461) * 4;
        days = days % 1461;
        if (days == 1460)
        {
            time->year += 3;
            days = 365;
        }
        else
        {
            time->year += days / 365;
            days = days % 365;
        }
    }

    if (isleap(time->year))
        daylist = daysBeforeMonthInLeapYear;
    else
        daylist = daysBeforeMonth;

    for (ii = 1; ii < 12; ii++)
    {
        if (days < daylist[ii]) break;
    }

    time->month = ii;
    time->day = (days - daylist[ii - 1]) + 1;

    time->hour = val->seconds / 3600;
    time->minute = (val->seconds % 3600) / 60;
    time->second = (val->seconds % 60);

    return eOK;
}

long misDTParse(char *str, MIS_DTVALUE *val)
{
    TIMESTRUCT tm;
    int nfields;

    /* Optional fields should be zeroed, so let's memset the puppy. */
    memset(&tm, 0, sizeof tm);

    /* Get our fields from the string. */
    nfields = sscanf(str, "%04d%02d%02d%02d%02d%02d",
	   &tm.year, &tm.month, &tm.day, &tm.hour, &tm.minute, &tm.second);
    
    if (nfields != 6)
    {
        return eINVALID_ARGS;
    }

    return timeStructToTimeVal(&tm, val);
}

long misDTFormat(char *date, long size, MIS_DTVALUE *val)
{
    TIMESTRUCT tm;
    long status = eOK;

    if (size != 15) return eINVALID_ARGS;

    status = timeValToTimeStruct(val, &tm);
    if (status == eOK) 
    {
        sprintf(date, "%04d%02d%02d%02d%02d%02d", 
                tm.year, tm.month, tm.day, tm.hour, tm.minute, tm.second);
    }
    return status;
}

double misDTCompare(MIS_DTVALUE *l, MIS_DTVALUE *r)
{
    double diff = (double)l->days - (double)r->days;
    diff = diff + (double)(l->seconds - r->seconds)/86400.0;

    return diff;
}

long misDTAddDays(MIS_DTVALUE *val, double days)
{
    val->days += (int)days;
    val->seconds += roundToInt(((days - (int)days))*86400);

    normalizeTimeVal(val);
    return eOK;
}
