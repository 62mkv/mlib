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

#include <stdlib.h>
#include <string.h>

#include "mad_byte_buffer.h"
#include "mad_msg.h"

/*
 * Append a header to MadByteBuffer +buf+.
 */ 
static void setHeader(MadByteBuffer *buf, 
                      MadMsgType type) 
{
    MadHeader hdr;

    hdr.sig = htonl(MAD_SIG);
    hdr.type = htonl(type);
    hdr.size = 0;

    madByteBufferWrite(buf, &hdr, sizeof(hdr));
}

/*
 * Append a MadName to MadButeBuffer +buf+.
 */
static void writeName(MadByteBuffer *buf,
                      const char *group,
                      const char *type,
                      const char *name,
                      const char *scope)
{
    madByteBufferWriteString(buf, group);
    madByteBufferWriteString(buf, type);
    madByteBufferWriteString(buf, name);
    madByteBufferWriteString(buf, scope);
}

/*
 * Set the size field of the MadMsg and return the buffer's contents.
 * At this point +buf+ will have a header, a MadName and contents.
 * However, size was initialized to 0.
 * We set the size as size of the MadName + contents and return the message.
 */
MadMsg *setSizeAndReturn(MadByteBuffer *buf)
{
    MadMsg *msg = NULL;

    msg = (MadMsg *)buf->bytes;

    msg->hdr.size = htonl(buf->nbytes - sizeof(MadHeader));

    return msg;
}

/*
 * Construct new boolean guage message.
 *
 */
MadMsg *madMsgBooleanNew(const char *group,
                         const char *type,
                         const char *name,
                         const char *scope,
                         int value) 
{
    MadByteBuffer buf;

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_BOOLEAN_GAUGE);

    writeName(&buf, group, type, name, scope);

    madByteBufferWriteInt(&buf, value);

    return setSizeAndReturn(&buf);
}

/*
 * Construct new integer gauge message.
 */
MadMsg *madMsgIntegerNew(const char *group,
                         const char *type,
                         const char *name,
                         const char *scope,
                         int value) 
{
    MadByteBuffer buf;

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_INTEGER_GAUGE);

    writeName(&buf, group, type, name, scope);

    madByteBufferWriteInt(&buf, value);

    return setSizeAndReturn(&buf);
}

/*
 * Construct new float gauge message.
 */
MadMsg *madMsgFloatNew(const char *group,
                       const char *type,
                       const char *name,
                       const char *scope,
                       float value) 
{
    MadByteBuffer buf;
    int ivalue = *((int *)(&value));

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_FLOAT_GAUGE);

    writeName(&buf, group, type, name, scope);

    madByteBufferWriteInt(&buf, ivalue);

    return setSizeAndReturn(&buf);
}

/*
 * Construct new string gauge message.
 */
MadMsg *madMsgStringNew(const char *group,
                        const char *type,
                        const char *name,
                        const char *scope,
                        const char *value) 
{
    MadByteBuffer buf;

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_STRING_GAUGE);

    writeName(&buf, group, type, name, scope);

    madByteBufferWriteString(&buf, value);

    return setSizeAndReturn(&buf);
}

/*
 * Construct new counter increment message.
 */
MadMsg *madMsgCounterIncNew(const char *group,
                            const char *type,
                            const char *name,
                            const char *scope,
                            int value) 
{
    MadByteBuffer buf;

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_COUNTER_INC);

    writeName(&buf, group, type, name, scope);

    madByteBufferWriteInt(&buf, value);

    return setSizeAndReturn(&buf);
}

/*
 * Construct new counter decrement message.
 */
MadMsg *madMsgCounterDecNew(const char *group,
                            const char *type,
                            const char *name,
                            const char *scope,
                            int value) 
{
    MadByteBuffer buf;

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_COUNTER_DEC);

    writeName(&buf, group, type, name, scope);

    madByteBufferWriteInt(&buf, value);

    return setSizeAndReturn(&buf);
}

/*
 * Constructe new histogram message.
 */
MadMsg *madMsgHistogramNew(const char *group,
                           const char *type,
                           const char *name,
                           const char *scope,
                           int biased,
                           int value) {
    MadByteBuffer buf;

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_HISTOGRAM);

    writeName(&buf, group, type, name, scope);

    madByteBufferWriteInt(&buf, biased);
    madByteBufferWriteInt(&buf, value);

    return setSizeAndReturn(&buf);
}

/*
 * Construct new histogram clear message.
 */ 
MadMsg *madMsgHistogramClear(const char *group,
                             const char *type,
                             const char *name,
                             const char *scope)
{
    MadByteBuffer buf;

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_HISTOGRAM_CLEAR);

    writeName(&buf, group, type, name, scope);

    return setSizeAndReturn(&buf);
}

/*
 * Construct new meter message.
 */
MadMsg *madMsgMeterNew(const char *group,
                       const char *type,
                       const char *name,
                       const char *scope,
                       int unit,
                       int value) {
    MadByteBuffer buf;

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_METER);

    writeName(&buf, group, type, name, scope);

    madByteBufferWriteInt(&buf, unit);
    madByteBufferWriteInt(&buf, value);

    return setSizeAndReturn(&buf);
}

/*
 * Construct new timer message.
 */
MadMsg *madMsgTimerNew(const char *group,
                       const char *type,
                       const char *name,
                       const char *scope,
                       int rateUnit,
                       int durationUnit,
                       int seconds,
                       int nanoseconds)
{
    MadByteBuffer buf;

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_TIMER);

    writeName(&buf, group, type, name, scope);

    madByteBufferWriteInt(&buf, rateUnit);
    madByteBufferWriteInt(&buf, durationUnit);
    madByteBufferWriteInt(&buf, seconds);
    madByteBufferWriteInt(&buf, nanoseconds);

    return setSizeAndReturn(&buf);
}

/*
 * Construct new timer clear message.
 */
MadMsg *madMsgTimerClear(const char *group,
                         const char *type,
                         const char *name,
                         const char *scope)
{
    MadByteBuffer buf;

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_TIMER_CLEAR);

    writeName(&buf, group, type, name, scope);

    return setSizeAndReturn(&buf);
}

/*
 * Construct new histogram with context message.
 */
MadMsg *madMsgHistogramContextNew(const char *group,
                                  const char *type,
                                  const char *name,
                                  const char *scope,
                                  int biased,
                                  int value,
                                  const char *context)
{
    MadByteBuffer buf;

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_HISTOGRAM_CONTEXT);

    writeName(&buf, group, type, name, scope);

    madByteBufferWriteInt(&buf, biased);
    madByteBufferWriteInt(&buf, value);

    madByteBufferWriteString(&buf, context);

    return setSizeAndReturn(&buf);
}

/*
 * Construct new histogram with context clear message.
 */
MadMsg *madMsgHistogramContextClear(const char *group,
                                    const char *type,
                                    const char *name,
                                    const char *scope)
{
    MadByteBuffer buf;

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_HISTOGRAM_CONTEXT_CLEAR);

    writeName(&buf, group, type, name, scope);

    return setSizeAndReturn(&buf);
}

/*
 * Construct new timer with context message.
 */
MadMsg *madMsgTimerContextNew(const char *group,
                              const char *type,
                              const char *name,
                              const char *scope,
                              int rateUnit,
                              int durationUnit,
                              int seconds,
                              int nanoseconds,
                              const char *context)
{
    MadByteBuffer buf;

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_TIMER_CONTEXT);

    writeName(&buf, group, type, name, scope);

    madByteBufferWriteInt(&buf, rateUnit);
    madByteBufferWriteInt(&buf, durationUnit);
    madByteBufferWriteInt(&buf, seconds);
    madByteBufferWriteInt(&buf, nanoseconds);

    madByteBufferWriteString(&buf, context);

    return setSizeAndReturn(&buf);
}

/*
 * Construct new message to clear a timer's context
 */
MadMsg *madMsgTimerContextClear(const char *group,
                                const char *type,
                                const char *name,
                                const char *scope)
{
    MadByteBuffer buf;

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_TIMER_CONTEXT_CLEAR);

    writeName(&buf, group, type, name, scope);

    return setSizeAndReturn(&buf);
}

/*
 * Construct new notification message.
 */
MadMsg *madMsgNotify(const char *key, const char *notify)
{
    MadByteBuffer buf;

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_NOTIFY);

    madByteBufferWriteString(&buf, key);
    madByteBufferWriteString(&buf, notify);

    return setSizeAndReturn(&buf);
}

/*
 * Construct new delete message.
 */
MadMsg *madMsgDeleteNew(const char *group,
                        const char *type,
                        const char *name,
                        const char *scope) 
{
    MadByteBuffer buf;

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_DELETE);

    writeName(&buf, group, type, name, scope);

    return setSizeAndReturn(&buf);
}

/*
 * Construct new delete multiple message. 
 */
MadMsg *madMsgDeleteTreeNew(const char *group,
                            const char *type,
                            const char *scope) 
{
    MadByteBuffer buf;

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_DELETE_TREE);

    writeName(&buf, group, type, "", scope);

    return setSizeAndReturn(&buf);
}

/*
 * Construct new asynchronous executor status set message.
 */
MadMsg *madMsgAsyncStatusSet(const char *sessionId,
                             const char *status)
{
    MadByteBuffer buf;

    madByteBufferNew(&buf);
    
    setHeader(&buf, MAD_MSG_CUSTOM);

    madByteBufferWriteInt(&buf, 1001);
    madByteBufferWriteString(&buf, sessionId);
    madByteBufferWriteString(&buf, status);

    return setSizeAndReturn(&buf);
}

/*
 * Free space used by MadMsg.
 */
void madMsgFree(MadMsg *msg) 
{
    free(msg);
}

