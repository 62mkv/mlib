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

#include <string.h>

#include <moca.h>
#include <mocaerr.h>

#include <madlib.h>
#include <oslib.h>

#include "mad_msg.h"
#include "mad_async_send.h"

/*
 * Update boolean gauge. Create if it doesn't exist.
 */
long madBooleanSet(const char *group, 
                   const char *type, 
                   const char *name,
                   const char *scope,
                   moca_bool_t value)
{
    MadMsg *msg;
    
    if (group == NULL) return eERROR;
    if (type == NULL) return eERROR;
    if (name == NULL) return eERROR;

    msg = madMsgBooleanNew(group, type, name, scope, value);
    
    madAsyncSend(msg);

    return eOK;
}

/*
 * Update integer gauge. Create if it doesn't exist.
 */
long madIntegerSet(const char *group, 
                   const char *type, 
                   const char *name,
                   const char *scope,
                   int value)
{
    MadMsg *msg;
    
    if (group == NULL) return eERROR;
    if (type == NULL) return eERROR;
    if (name == NULL) return eERROR;

    msg = madMsgIntegerNew(group, type, name, scope, value);

    madAsyncSend(msg);

    return eOK;
}

/*
 * Update float gauge. Create if it doesn't exist.
 */
long madFloatSet(const char *group, 
                 const char *type, 
                 const char *name,
                 const char *scope,
                 float value)
{
    MadMsg *msg;
    
    if (group == NULL) return eERROR;
    if (type == NULL) return eERROR;
    if (name == NULL) return eERROR;

    msg = madMsgFloatNew(group, type, name, scope, value);

    madAsyncSend(msg);

    return eOK;
}

/*
 * Update string gauge. Create if it doesn't exist.
 */
long madStringSet(const char *group, 
                  const char *type, 
                  const char *name,
                  const char *scope,
                  const char *value)
{
    MadMsg *msg;
    
    if (group == NULL) return eERROR;
    if (type == NULL) return eERROR;
    if (name == NULL) return eERROR;

    msg = madMsgStringNew(group, type, name, scope, value);

    madAsyncSend(msg);

    return eOK;
}

/*
 * Increment counter. Create if it doesn't exist.
 */
long madCounterInc(const char *group, 
                   const char *type, 
                   const char *name,
                   const char *scope,
                   int value)
{
    MadMsg *msg;
    
    if (group == NULL) return eERROR;
    if (type == NULL) return eERROR;
    if (name == NULL) return eERROR;

    msg = madMsgCounterIncNew(group, type, name, scope, value);

    madAsyncSend(msg);

    return eOK;
}
    
/*
 * Decrement counter. Create if it doesn't exist.
 */
long madCounterDec(const char *group, 
                   const char *type, 
                   const char *name,
                   const char *scope,
                   int value)
{
    MadMsg *msg;
    
    if (group == NULL) return eERROR;
    if (type == NULL) return eERROR;
    if (name == NULL) return eERROR;

    msg = madMsgCounterDecNew(group, type, name, scope, value);

    madAsyncSend(msg);

    return eOK;
}

/*
 * Add value to histogram. Create if it doesn't exist.
 */
long madHistogramAdd(const char *group, 
                     const char *type, 
                     const char *name,
                     const char *scope,
                     moca_bool_t biased,
                     int value)
{
    MadMsg *msg;
    
    if (group == NULL) return eERROR;
    if (type == NULL) return eERROR;
    if (name == NULL) return eERROR;

    msg = madMsgHistogramNew(group, type, name, scope, biased, value);

    madAsyncSend(msg);

    return eOK;
}

/*
 * Clear histogram values. Create if it doesn't exist.
 */
long madHistogramClear(const char *group, 
                       const char *type, 
                       const char *name, 
                       const char *scope)
{
    MadMsg *msg;

    if (group == NULL) return eERROR;
    if (type == NULL) return eERROR;
    if (name == NULL) return eERROR;
    
    msg = madMsgHistogramClear(group, type, name, scope);
    
    madAsyncSend(msg);
    
    return eOK;
}

/*
 * Increment counter. Create if it doesn't exist.
 */
long madHistogramAddContext(const char *group, 
                            const char *type, 
                            const char *name,
                            const char *scope,
                            moca_bool_t biased,
                            int value,
                            const char *context)
{
    MadMsg *msg;
    
    if (group == NULL) return eERROR;
    if (type == NULL) return eERROR;
    if (name == NULL) return eERROR;

    msg = madMsgHistogramContextNew(group, type, name, scope, biased,
            value, context);

    madAsyncSend(msg);

    return eOK;
}

/*
 * Add value with context to histogram. Create if it doesn't exist.
 */
long madHistogramContextClear(const char *group, 
                              const char *type, 
                              const char *name, 
                              const char *scope)
{
    MadMsg *msg;
    
    if (group == NULL) return eERROR;
    if (type == NULL) return eERROR;
    if (name == NULL) return eERROR;

    msg = madMsgHistogramContextClear(group, type, name, scope);

    madAsyncSend(msg);

    return eOK;
}

/*
 * Mark meter. Create if it doesn't exist.
 */
long madMeterMark(const char *group, 
                  const char *type, 
                  const char *name,
                  const char *scope,
                  MadTimeUnit rateUnit,
                  int value)
{
    MadMsg *msg;
    
    if (group == NULL) return eERROR;
    if (type == NULL) return eERROR;
    if (name == NULL) return eERROR;

    msg = madMsgMeterNew(group, type, name, scope, rateUnit, value);

    madAsyncSend(msg);

    return eOK;
}

/*
 * Add time to a timer. Create if it doesn't exist.
 */
long madTimerAdd(const char *group, 
                 const char *type, 
                 const char *name,
                 const char *scope,
                 MadTimeUnit rateUnit,
                 MadTimeUnit durationUnit,
                 MadTimeInterval *interval)
{
    MadMsg *msg;
    
    if (group == NULL) return eERROR;
    if (type == NULL) return eERROR;
    if (name == NULL) return eERROR;

    msg = madMsgTimerNew(group, type, name, scope, rateUnit,
            durationUnit, 
            madTimeIntervalGetSeconds(interval),
            madTimeIntervalGetNanoseconds(interval));
    
    madAsyncSend(msg); 

    return eOK; 
}

/*
 * Clear timer. Create if it doesn't exist.
 */
long madTimerClear(const char *group,
                   const char *type,
                   const char *name,
                   const char *scope)
{
    MadMsg *msg;
    
    if (group == NULL) return eERROR;
    if (type == NULL) return eERROR;
    if (name == NULL) return eERROR;

    msg = madMsgTimerClear(group, type, name, scope);
    
    madAsyncSend(msg); 

    return eOK; 

}

/*
 * Add time to timer with context. Create if it doesn't exist.
 */
long madTimerAddContext(const char *group, 
                        const char *type, 
                        const char *name,
                        const char *scope,
                        MadTimeUnit rateUnit,
                        MadTimeUnit durationUnit,
                        MadTimeInterval *interval,
                        const char *context)
{
    MadMsg *msg;
    
    if (group == NULL) return eERROR;
    if (type == NULL) return eERROR;
    if (name == NULL) return eERROR;

    msg = madMsgTimerContextNew(group, type, name, scope, rateUnit,
            durationUnit, 
            madTimeIntervalGetSeconds(interval),
            madTimeIntervalGetNanoseconds(interval),
            context);

    madAsyncSend(msg); 

    return eOK; 
}

/*
 * Clear timer with context. Create if it doesn't exist.
 */
long madTimerContextClear(const char *group,
                          const char *type,
                          const char *name,
                          const char *scope)
{
    MadMsg *msg;
    
    if (group == NULL) return eERROR;
    if (type == NULL) return eERROR;
    if (name == NULL) return eERROR;

    msg = madMsgTimerContextClear(group, type, name, scope);
    
    madAsyncSend(msg); 

    return eOK; 

}

/*
 * Delete probe.
 */
long madDelete(const char *group, 
               const char *type, 
               const char *name,
               const char *scope)
{
    MadMsg *msg;
    
    if (group == NULL) return eERROR;
    if (type == NULL) return eERROR;
    if (name == NULL) return eERROR;

    msg = madMsgDeleteNew(group, type, name, scope);
    
    madAsyncSend(msg);

    return eOK;
}

/*
 * Delete probes matching group, type and scope.
 */
long madDeleteTree(const char *group, 
                   const char *type, 
                   const char *scope)
{
    MadMsg *msg;
    
    if (group == NULL) return eERROR;
    if (type == NULL) return eERROR;

    msg = madMsgDeleteTreeNew(group, type, scope);

    madAsyncSend(msg);

    return eOK;
}

/*
 * Send notification.
 */
long madNotify(const char *key,
               const char *value) 
{
    MadMsg *msg;

    if (key == NULL || value == NULL) return eERROR;

    msg = madMsgNotify(key, value);

    madAsyncSend(msg); 

    return eOK; 
}

/*
 * Set status of asynchronous executor.
 */
long madAsyncStatusSet(const char *status)
{
    MadMsg *msg;

    char *sessionId = osGetVar("ASYNC_SESSION_ID");

    if (sessionId == NULL || status == NULL) return eERROR;

    msg = madMsgAsyncStatusSet(sessionId, status);

    madAsyncSend(msg); 

    return eOK;
}


    


