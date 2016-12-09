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
#include <time.h>

#include <mocaerr.h>
#include <mislib.h>

/* NOTE:  You should place yyyy before yy, mmm before mm, etc. because */
/* the routine checks for those strings and we want to check for the */
/* longer ones first.  Otherwise we will get dates like 03/12/yy92 */

static char *date_literals[] =
{"yyyy", "yy", "mmm", "mm", "ddd", "dd", 0};
static char *date_formats[] =
{"%Y", "%y", "%b", "%m", "%j", "%d", 0};
static size_t date_sizes[] =
{4, 2, 3, 2, 3, 2, 0};

static char *time_literals[] =
{"hh", "mm", "ss", 0};
static char *time_formats[] =
{"%H", "%M", "%S", 0};
static size_t time_sizes[] =
{2, 2, 2, 0};

static char *format_models[] =
{
    "YYYY", "YYY", "YY", "Y",
    "Q",
    "MM", "MONTH",
    "WW", "IW",
    "DAY", "DDD", "DD", "D", "J",
    "HH24", "MI", "SS",
    "-", "/", ".", ":", " ",
    0
};

char *formattime(char *time_str_i, struct tm *times)
{
    long idx;
    char *tmp;
    char tmp_str[15];
    char tmp_text[10];
    static char time_str[100];
    
    memset(time_str, 0, sizeof(time_str));
    strcpy(time_str, time_str_i);
    
    idx = 0;
    while (time_formats[idx] != 0)
    {
	strncpy(tmp_str, time_str, 15);
	tmp_str[14] = '\0';

	if ((tmp = strstr(tmp_str, time_literals[idx])) != NULL)
	{
	    strftime(tmp_text, 9, time_formats[idx], times);
	    strncpy(&time_str[tmp - tmp_str], tmp_text, time_sizes[idx]);
	}

	idx++;
    }

    return (time_str);
}

char *formatdate(char *date_str_i, struct tm *times)
{
    long idx;
    char *tmp;
    char tmp_str[15];
    char tmp_text[10];
    static char date_str[100];

    memset(date_str, 0, sizeof(date_str));
    strcpy(date_str, date_str_i);

    idx = 0;
    while (date_formats[idx] != 0)
    {
	strncpy(tmp_str, date_str, 15);
	tmp_str[14] = '\0';

	if ((tmp = strstr(tmp_str, date_literals[idx])) != NULL)
	{
	    strftime(tmp_text, 9, date_formats[idx], times);
	    strncpy(&date_str[tmp - tmp_str], tmp_text, date_sizes[idx]);
	}

	idx++;
    }

    return date_str;
}


char *misFormatDate(char *date_str_i)
{
    struct tm *times;
    time_t *temp_time;
    time_t tmp_time;
    char *date_str;

    tmp_time = time(&tmp_time);
    temp_time = &tmp_time;
    times = localtime(temp_time);

    date_str = formatdate(date_str_i, times);

    return (date_str);
}

char *misFormatTime(char *time_str_i)
{
    struct tm *times;
    time_t *temp_time;
    time_t tmp_time;
    char *time_str;

    tmp_time = time(&tmp_time);
    temp_time = &tmp_time;
    times = localtime(temp_time);

    time_str = formattime(time_str_i, times);

    return (time_str);
}

char *misFormatDateTime(char *date_str_i, char *time_str_i)
{
    struct tm *times;
    time_t *temp_time;
    time_t tmp_time;
    char *date_str;
    char *time_str;
    static char date_time_str[100];

    tmp_time = time(&tmp_time);
    temp_time = &tmp_time;
    times = localtime(temp_time);

    date_str = formatdate(date_str_i, times);
    time_str = formattime(time_str_i, times);

    /* concat date a ndtime */
    sprintf(date_time_str, "%s %s", date_str, time_str); 

    return (date_time_str);

}

char *misFormatMOCADate(char *moca_dt, char *date_fmt_i)
{
    struct tm tm_date;
    time_t base_time;
   
    char *date_str;

    char   year[5];
    char   month[3];
    char   day[3];

    memset(&tm_date, 0, sizeof(struct tm));

    /* get year, month and day */
    strncpy(year, moca_dt, 4);
    year[4] = 0; /*don't forget the null terminator*/

    strncpy(month, &moca_dt[4], 2);
    month[2] = 0;

    strncpy(day, &moca_dt[6], 2);
    day[2] = 0;

    tm_date.tm_year  = atoi(year) - 1900;
    tm_date.tm_mday  = atoi(day);
    tm_date.tm_mon   = atoi(month) - 1;
    tm_date.tm_isdst = -1;

    base_time = mktime(&tm_date);

    if (base_time == -1)
    {
       return NULL;
    }
    /* Call the following to fill in the other struct tm fields
     ** we will need */
    memcpy(&tm_date, localtime(&base_time), sizeof(struct tm));

    date_str = formatdate(date_fmt_i, &tm_date);

    return (date_str);
}

char *misFormatMOCATime(char *moca_dt, char *time_fmt_i)
{
    struct tm tm_date;
    time_t base_time;
   
    char *time_str;

    char   year[5];
    char   month[3];
    char   day[3];
    char   hour[3];
    char   min[3];
    char   sec[3];

    memset(&tm_date, 0, sizeof(struct tm));

    /* get year, month and day */
    strncpy(year, moca_dt, 4);
    year[4] = 0; /*don't forget the null terminator*/

    strncpy(month, &moca_dt[4], 2);
    month[2] = 0;

    strncpy(day, &moca_dt[6], 2);
    day[2] = 0;
    
    strncpy(hour, &moca_dt[8], 2);
    hour[2] = 0;

    strncpy(min, &moca_dt[10], 2);
    min[2] = 0;

    strncpy(sec, &moca_dt[12], 2);
    sec[2] = 0;

    tm_date.tm_year  = atoi(year) - 1900;
    tm_date.tm_mday  = atoi(day);
    tm_date.tm_mon   = atoi(month) - 1;
    tm_date.tm_hour  = atoi(hour);
    tm_date.tm_min   = atoi(min);
    tm_date.tm_sec   = atoi(sec);
    tm_date.tm_isdst = -1;

    base_time = mktime(&tm_date);

    if (base_time == -1)
    {
       return NULL;
    }
    /* Call the following to fill in the other struct tm fields
     ** we will need */
    memcpy(&tm_date, localtime(&base_time), sizeof(struct tm));

    time_str = formattime(time_fmt_i, &tm_date);

    return (time_str);
}

char *misFormatMOCADateTime(char *moca_dt, char *date_fmt_i, char *time_fmt_i)
{

    char *date_str;
    char *time_str;
    static char date_time_str[100];

    date_str = misFormatMOCADate(moca_dt, date_fmt_i);
    time_str = misFormatMOCATime(moca_dt, time_fmt_i);

    /* concat the two with a space between */
    sprintf(date_time_str, "%s %s", date_str, time_str); 
    return (date_time_str);
}

static int sIsLeapYear(int yyyy)
{
    return yyyy % 400 == 0 || (yyyy % 4 == 0 && yyyy % 100 != 0);
}

long misValidateDatetime(char *yyyymmddhhmiss, int fullyValidateYear)
{
    char  temp[10],
         *ptr;

    struct tm mytime;

    int days[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    /* Make sure we at least have something to work with. */
    if (!yyyymmddhhmiss || strlen(yyyymmddhhmiss) != 14)
	return eINVALID_ARGS;

    /* Make sure it's all numbers. */
    ptr = yyyymmddhhmiss;
    while (ptr && *ptr)
    {
        if (!isdigit(*ptr))
            return eINVALID_ARGS;
 
        ptr++;
    }

    /* Validate the year. */
    memset(temp, 0, sizeof(temp));
    strncpy(temp, &yyyymmddhhmiss[0], 4);
    mytime.tm_year = atoi(temp);
    if (fullyValidateYear)
    {
        if (mytime.tm_year < 1900 || mytime.tm_year > 2038)
            return eINVALID_ARGS;
    }
    else
    {
        if (mytime.tm_year < 0 || mytime.tm_year > 9999)
            return eINVALID_ARGS;
    }

    /* Validate the month. */
    memset(temp, 0, sizeof(temp));
    strncpy(temp, &yyyymmddhhmiss[4], 2);
    mytime.tm_mon = atoi(temp);
    if (mytime.tm_mon < 1 || mytime.tm_mon > 12)
        return eINVALID_ARGS;

    /* Validate the day. */
    memset(temp, 0, sizeof(temp));
    strncpy(temp, &yyyymmddhhmiss[6], 2);
    mytime.tm_mday = atoi(temp);
    if (mytime.tm_mday < 1 || mytime.tm_mday > (days[mytime.tm_mon - 1] + (2 == mytime.tm_mon && sIsLeapYear(mytime.tm_year))))
         return eINVALID_ARGS;

    /* Validate the hour. */
    memset(temp, 0, sizeof(temp));
    strncpy(temp, &yyyymmddhhmiss[8], 2);
    mytime.tm_hour = atoi(temp);
    if (mytime.tm_hour < 0 || mytime.tm_hour > 23)
        return eINVALID_ARGS;

    /* Validate the minute. */
    memset(temp, 0, sizeof(temp));
    strncpy(temp, &yyyymmddhhmiss[10], 2);
    mytime.tm_min = atoi(temp);
    if (mytime.tm_min < 0 || mytime.tm_min > 59)
        return eINVALID_ARGS;

    /* Validate the second. */
    memset(temp, 0, sizeof(temp));
    strncpy(temp, &yyyymmddhhmiss[12], 2);
    mytime.tm_sec = atoi(temp);
    if (mytime.tm_sec < 0 || mytime.tm_sec > 59)
        return eINVALID_ARGS;

    return eOK;
}

/*
 * FUNCTION: misValidateDatetimeFormat
 *
 * PURPOSE:  Validate the given date/time format.
 *
 * NOTE(S):  The given format can contain any of the placeholders that
 *           we support.  It's important to note that because we support
 *           multiple database engines, this is a subset of what Oracle
 *           supports rather than every placeholder that they support.
 *
 * RETURNS:  0 - The date/time format is valid.
 */

long misValidateDatetimeFormat(char *fmt)
{
    int ii,
        length,
        status;

    char *ptr,
         *buffer = NULL;

    /* Make a copy of the given format that we can play with. */
    misDynStrcpy(&buffer, fmt);
    misToUpper(buffer);

    /* Remove all the format models from the given format. */
    for (ii = 0; format_models[ii]; ii++)
    {
        while ((ptr = strstr(buffer, format_models[ii])) != NULL)
        {
            length = strlen(format_models[ii]);
            memmove(ptr, ptr+length, strlen(ptr+length)+1);
        }
    }

    /* The status is essentially the string length. */
    status = strlen(buffer);

    free(buffer);

    return status;
}
