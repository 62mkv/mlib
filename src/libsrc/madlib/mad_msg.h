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

#ifndef MAD_MSG_H
#define MAD_MSG_H

#include "madlib.h"

#ifdef __cplusplus
extern "C" {
#endif

#define MAD_SIG 0x4D614300

enum madMsgType 
{
    MAD_MSG_UNDEFINED = 0,
    MAD_MSG_BOOLEAN_GAUGE,
    MAD_MSG_INTEGER_GAUGE,
    MAD_MSG_FLOAT_GAUGE,
    MAD_MSG_STRING_GAUGE,
    MAD_MSG_COUNTER_INC,
    MAD_MSG_COUNTER_DEC,
    MAD_MSG_HISTOGRAM,
    MAD_MSG_METER,
    MAD_MSG_TIMER,
    MAD_MSG_HISTOGRAM_CONTEXT,
    MAD_MSG_TIMER_CONTEXT,
    MAD_MSG_NOTIFY,
    MAD_MSG_DELETE,
    MAD_MSG_DELETE_TREE,
    MAD_MSG_GAUGE,
    MAD_MSG_DISCONNECT,
    MAD_MSG_CUSTOM,
    MAD_MSG_HISTOGRAM_CLEAR,
    MAD_MSG_HISTOGRAM_CONTEXT_CLEAR,
    MAD_MSG_TIMER_CLEAR,
    MAD_MSG_TIMER_CONTEXT_CLEAR
};

typedef enum madMsgType MadMsgType;

struct madHeader {
    int sig;
    int type;
    int size;
};

typedef struct madHeader MadHeader;

struct madMsg 
{
    MadHeader hdr;
};

typedef struct madMsg MadMsg;

MadMsg *madMsgBooleanNew(const char *group,
                         const char *type,
                         const char *name,
                         const char *scope,
                         int value);

MadMsg *madMsgIntegerNew(const char *group,
                         const char *type,
                         const char *name,
                         const char *scope,
                         int value);

MadMsg *madMsgFloatNew(const char *group,
                       const char *type,
                       const char *name,
                       const char *scope,
                       float value);

MadMsg *madMsgStringNew(const char *group,
                        const char *type,
                        const char *name,
                        const char *scope,
                        const char *value);

MadMsg *madMsgCounterIncNew(const char *group,
                            const char *type,
                            const char *name,
                            const char *scope,
                            int value);

MadMsg *madMsgCounterDecNew(const char *group,
                            const char *type,
                            const char *name,
                            const char *scope,
                            int value);

MadMsg *madMsgHistogramNew(const char *group,
                           const char *type,
                           const char *name,
                           const char *scope,
                           int biased,
                           int value);

MadMsg *madMsgHistogramClear(const char *group,
                             const char *type,
                             const char *name,
                             const char *scope);
                           
MadMsg *madMsgMeterNew(const char *group,
                       const char *type,
                       const char *name,
                       const char *scope,
                       int unit,
                       int value);

MadMsg *madMsgTimerNew(const char *group,
                       const char *type,
                       const char *name,
                       const char *scope,
                       int rateUnit,
                       int durationUnit,
                       int seconds,
                       int nanoseconds);
                       
MadMsg *madMsgTimerClear(const char *group,
                         const char *type,
                         const char *name,
                         const char *scope);

MadMsg *madMsgHistogramContextNew(const char *group,
                                  const char *type,
                                  const char *name,
                                  const char *scope,
                                  int biased,
                                  int value,
                                  const char *context);

MadMsg *madMsgHistogramContextClear(const char *group,
                                    const char *type,
                                    const char *name,
                                    const char *scope);
                                  
MadMsg *madMsgTimerContextNew(const char *group,
                              const char *type,
                              const char *name,
                              const char *scope,
                              int rateUnit,
                              int durationUnit,
                              int seconds,
                              int nanoseconds,
                              const char *context);

MadMsg *madMsgTimerContextClear(const char *group,
                                const char *type,
                                const char *name,
                                const char *scope);
                              
MadMsg *madMsgNotify(const char *key, const char *notify);

MadMsg *madMsgDeleteNew(const char *group,
                        const char *type,
                        const char *name,
                        const char *scope);

MadMsg *madMsgDeleteTreeNew(const char *group,
                            const char *type,
                            const char *scope);

MadMsg *madMsgAsyncStatusSet(const char *sessionId,
                             const char *status);

void madMsgFree(MadMsg *msg);

#ifdef __cplusplus
}
#endif

#endif /* MAD_MSG_H */
