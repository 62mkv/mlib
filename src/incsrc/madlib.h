/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file for madlib.
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

#ifndef _MAD_H_
#define _MAD_H_

#include <moca.h>
#include <srvlib.h>

#if defined (__cplusplus)
extern "C" {
#endif

enum madTimeUnit 
{
    MAD_DAYS,
    MAD_HOURS,
    MAD_MINUTES,
    MAD_SECONDS,
    MAD_MILLISECONDS,
    MAD_MICROSECONDS,
    MAD_NANOSECONDS
};

typedef enum madTimeUnit MadTimeUnit;

struct madTime 
{
    int sec;
    int nsec;
};

typedef struct madTime MadTime;

struct madTimeInterval 
{
    MadTime start;
    MadTime end;
    MadTime diff;
};

typedef struct madTimeInterval MadTimeInterval;


/*
 * FUNCTION: madBooleanSet
 *
 * PURPOSE:  Sets a Boolean Gauge probe to value, creating it if it 
 *           does not exist.  group, type, and name must be NULL 
 *           terminated strings.  scope is optional and should be NULL 
 *           if not needed.
 *
 * RETURNS:  eOK on success.
 *           eERROR if group, type, or name is NULL.
 */
long madBooleanSet(const char *group, 
                   const char *type, 
                   const char *name, 
                   const char *scope, 
                   moca_bool_t value);

/*
 * FUNCTION: madIntegerSet
 *
 * PURPOSE:  Sets a Integer Gauge probe to value, creating it if it 
 *           does not exist.  group, type, and name must be NULL 
 *           terminated strings.  scope is optional and should be NULL 
 *           if not needed.
 *
 * RETURNS:  eOK on success.
 *           eERROR if group, type, or name is NULL.
 */
long madIntegerSet(const char *group,
                   const char *type,
                   const char *name,
                   const char *scope,
                   int value);

/*
 * FUNCTION: madFloatSet
 *
 * PURPOSE:  Sets a Float Gauge probe to value, creating it if it 
 *           does not exist.  group, type, and name must be NULL 
 *           terminated strings.  scope is optional and should be NULL 
 *           if not needed.
 *
 * RETURNS:  eOK on success.
 *           eERROR if group, type, or name is NULL.
 */
long madFloatSet(const char *group,
                 const char *type,
                 const char *name,
                 const char *scope,
                 float value);

/*
 * FUNCTION: madStringSet
 *
 * PURPOSE:  Sets a String Gauge probe to value, creating it if it 
 *           does not exist.  group, type, and name must be NULL 
 *           terminated strings.  scope is optional and should be NULL 
 *           if not needed.
 *
 * RETURNS:  eOK on success.
 *           eERROR if group, type, or name is NULL.
 */
long madStringSet(const char *group,
                  const char *type,
                  const char *name,
                  const char *scope,
                  const char *value);


/*
 * FUNCTION: madCounterInc
 *
 * PURPOSE:  Increments a Counter probe by value, creating it if it 
 *           does not exist.  group, type, and name must be NULL 
 *           terminated strings.  scope is optional and should be NULL 
 *           if not needed.
 *
 * RETURNS:  eOK on success.
 *           eERROR if group, type, or name is NULL.
 */
long madCounterInc(const char *group, 
                   const char *type, 
                   const char *name,
                   const char *scope,
                   int value);

/*
 * FUNCTION: madCounterDec
 *
 * PURPOSE:  Decrements a Counter probe by value, creating it if it 
 *           does not exist.  group, type, and name must be NULL 
 *           terminated strings.  scope is optional and should be NULL 
 *           if not needed.
 *
 * RETURNS:  eOK on success.
 *           eERROR if group, type, or name is NULL.
 */
long madCounterDec(const char *group, 
                   const char *type, 
                   const char *name,
                   const char *scope,
                   int value);

/*
 * FUNCTION: madHistogramAdd
 *
 * PURPOSE:  Add value to a Histogram probe, creating it if it 
 *           does not exist.  group, type, and name must be NULL 
 *           terminated strings.  scope is optional and should be NULL 
 *           if not needed.  If biased is true, the histogram will be
 *           biased.  It will be uniform otherwise.
 *
 * RETURNS:  eOK on success.
 *           eERROR if group, type, or name is NULL.
 */
long madHistogramAdd(const char *group, 
                     const char *type, 
                     const char *name, 
                     const char *scope,
                     moca_bool_t biased,
                     int value);
                     
/*
 * FUNCTION: madHistogramClear
 *
 * PURPOSE:  Clears a Histogram probe of its recorded values, creating 
 *           it if it does not exist.  group, type, and name must be NULL 
 *           terminated strings.  scope is optional and should be NULL 
 *           if not needed.
 *
 * RETURNS:  eOK on success.
 *           eERROR if group, type, or name is NULL.
 */
long madHistogramClear(const char *group, 
                       const char *type, 
                       const char *name, 
                       const char *scope);
                     
/*
 * FUNCTION: madHistogramAddContext
 *
 * PURPOSE:  Add value to a HistogramContext probe, creating it if it 
 *           does not exist.  group, type, and name must be NULL 
 *           terminated strings.  scope is optional and should be NULL 
 *           if not needed.  If biased is true, the histogram will be
 *           biased.  It will be uniform otherwise.  context is the
 *           context string. If the value to be added is the the maximum
 *           or minimum, it will be is published.
 *
 * RETURNS:  eOK on success.
 *           eERROR if group, type, or name is NULL.
 */
long madHistogramAddContext(const char *group, 
                            const char *type, 
                            const char *name, 
                            const char *scope,
                            moca_bool_t biased,
                            int value,
                            const char *context);

/*
 * FUNCTION: madHistogramContextClear
 *
 * PURPOSE:  Clears a HistogramContext probe of its recorded values,
 *           creating it if it does not exist.  group, type, and 
 *           name must be NULL terminated strings.  scope is optional
 *           and should be NULL if not needed.
 *
 * RETURNS:  eOK on success.
 *           eERROR if group, type, or name is NULL.
 */
long madHistogramContextClear(const char *group, 
                              const char *type, 
                              const char *name, 
                              const char *scope);
                       
/*
 * FUNCTION: madMeterMark
 *
 * PURPOSE:  Records value events in a Meter probe, creating it with a
 *           rate unit of rateUnit if it does not exist.  group, type,
 *           and name must be NULL terminated strings.  scope is optional
 *           and should be NULL if not needed.
 *
 * RETURNS:  eOK on success.
 *           eERROR if group, type, or name is NULL.
 */
long madMeterMark(const char *group, 
                  const char *type,
                  const char *name,
                  const char *scope,
                  MadTimeUnit rateUnit,
                  int value);

/*
 * FUNCTION: madTimeGet
 *
 * PURPOSE:  Fills t with the current time which can be used for
 *           comparision with another time returned by this.
 */           
void madTimeGet(MadTime *t);

/*
 * FUNCTION: madTimeIntervalClear
 *
 * PURPOSE:  Clears a time interval.
 */
void madTimeIntervalClear(MadTimeInterval *interval);

/*
 * FUNCTION: madTimeIntervalStart
 * 
 * PURPOSE:  Starts timing.
 */
void madTimeIntervalStart(MadTimeInterval *interval);

/*
 * FUNCTION: madTimeIntervalEnd
 * 
 * PURPOSE:  Ends timing.
 */
void madTimeIntervalEnd(MadTimeInterval *interval);

/*
 * FUNCTION: madTimeIntervalGetSeconds
 * 
 * PURPOSE:  Gets the number of seconds elapsed in a time interval.
 *
 * RETURNS:  The number of seconds elapsed.
 */
int madTimeIntervalGetSeconds(MadTimeInterval *split);

/*
 * FUNCTION: madTimeIntervalGetNanoseconds
 * 
 * PURPOSE:  Gets the number of nanoseconds elapsed mod 1 billion in a
 *           time interval.
 *
 * RETURNS:  The number of nanoseconds elapsed.
 */
int madTimeIntervalGetNanoseconds(MadTimeInterval *split);

/*
 * FUNCTION: madTimeIntervalSetSeconds
 * 
 * PURPOSE:  Sets the number of seconds elapsed in a time interval.
 */
void madTimeIntervalSetSeconds(MadTimeInterval *interval, int seconds);

/*
 * FUNCTION: madTimeIntervalSetNanoseconds
 * 
 * PURPOSE:  Sets the number of nanoseconds elapsed in a time interval.
 */
void madTimeIntervalSetNanoseconds(MadTimeInterval *interval, int nanoseconds);

/*
 * FUNCTION: madTimerAdd
 *
 * PURPOSE:  Adds time to a Timer probe, creating it if it does not
 *           exist.  group, type, and name must be NULL terminated
 *           strings.  scope is optional and should be NULL if not
 *           needed.  rateUnit is the unit in which rate in which times
 *           are added is report.  durationUnit is the unit in which the
 *           statistics about the time is reported.  interval is the
 *           time interval to be reported.
 *
 * RETURNS:  eOK on success.
 *           eERROR if group, type, or name is NULL.
 */
long madTimerAdd(const char *group,
                 const char *type,
                 const char *name,
                 const char *scope,
                 MadTimeUnit rateUnit,
                 MadTimeUnit durationUnit,
                 MadTimeInterval *interval);

/*
 * FUNCTION: madTimerClear
 *
 * PURPOSE:  Clears a Timer probe, creating it if it does not
 *           exist.  group, type, and name must be NULL terminated
 *           strings.  scope is optional and should be NULL if not
 *           needed.
 *
 * RETURNS:  eOK on success.
 *           eERROR if group, type, or name is NULL.
 */
long madTimerClear(const char *group,
                   const char *type,
                   const char *name,
                   const char *scope);
                 
/*
 * FUNCTION: madTimerAddContext
 *
 * PURPOSE:  Adds time to a Timer probe, creating it if it does not
 *           exist.  group, type, and name must be NULL terminated
 *           strings.  scope is optional and should be NULL if not
 *           needed.  rateUnit is the unit in which rate in which times
 *           are added is report.  durationUnit is the unit in which the
 *           statistics about the time is reported.  interval is the
 *           time interval to be reported.  context is the
 *           context string. If the value to be added is the the maximum
 *           or minimum, it will be is published.
 *
 * RETURNS:  eOK on success.
 *           eERROR if group, type, or name is NULL.
 */
long madTimerAddContext(const char *group,
                        const char *type,
                        const char *name,
                        const char *scope,
                        MadTimeUnit rateUnit,
                        MadTimeUnit durationUnit,
                        MadTimeInterval *interval,
                        const char *context);

/*
 * FUNCTION: madTimerContextClear
 *
 * PURPOSE:  Clears a TimerContext probe, creating it if it does not
 *           exist.  group, type, and name must be NULL terminated
 *           strings.  scope is optional and should be NULL if not
 *           needed.
 *
 * RETURNS:  eOK on success.
 *           eERROR if group, type, or name is NULL.
 */
long madTimerContextClear(const char *group,
                          const char *type,
                          const char *name,
                          const char *scope);
                   
/*
 * FUNCTION: madDelete
 *
 * PURPOSE:  Deletes the Metric with the specified name.
 *
 * RETURNS:  eOK on success.
 *           eERROR if group, type, or name is NULL.
 */
long madDelete(const char *group,
               const char *type,
               const char *name,
               const char *scope);

/*
 * FUNCTION: madDeleteTree
 *
 * PURPOSE:  Deletes all Metrics with the same group, type, and scope.
 *
 * RETURNS:  eOK on success.
 *           eERROR if group, type, or scope is NULL.
 */
long madDeleteTree(const char *group,
                   const char *type,
                   const char *scope);
 
/*
 * FUNCTION: madNotify
 *
 * PURPOSE:  Send notification with key and value.
 *
 * RETURNS:  eOK on success.
 *           eERROR if key or value are not.
 */
long madNotify(const char *key,
               const char *value);

/*
 * FUNCTION: madAsyncStatusSet
 *
 * PURPOSE:  Sets the status of the AsynchronousExecutor
 *           which is running this C component
 * 
 * RETURNS:  eOK on success.
 *           eERROR if not running in an AsynchronousExecutor
 *           of status is NULL
 */
long madAsyncStatusSet(const char *status);

#if defined (__cplusplus)
}
#endif

#endif /* _MAD_H_ */
