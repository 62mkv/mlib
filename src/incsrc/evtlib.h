/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file for evtlib.
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

#ifndef EVTLIB_H
#define EVTLIB_H

#include <moca.h>
#include <oslib.h>

/*
 *  Event Attribute Definitions
 */

#define EVT_FOREVER      0	/* Forever is used to iterate forever */

#define EVT_EVENT_DONE   99	/* Event is completed                 */

/*
 *  Event Types Definitions
 */

#define EVT_EVENT_END    0	/* End, must be last type in event array */
#define EVT_TIMER        1	/* Timer, calls callback when completes  */
#define EVT_MSG_READ     2	/* Message queue read                    */
#define EVT_DEV_READ     3	/* Device read                           */

/*
 *  Internal Event Union Type Definition
 */

typedef union events_union EVENT_STR;

/*
 *  Event Structue Type Definition
 */

typedef struct
{
    short type;			  /* Must be a known event type           */
    long iterate;		  /* Iterations, 0 means forever          */
    long (*callback)();		  /* Callback function                    */
    long user_data;		  /* User defines what is here            */
    long time;			  /* Time in hundreths of a second        */
    char name[OS_MBXNAME_LEN+1];  /* Msg queue or lock name               */
    long max_size;		  /* Maximum size of a read               */
    void *data;			  /* Pointer to data when event completes */
    int fd;			  /* The fd we use to do device reads     */
    EVENT_STR *event;		  /* Internal event handling structure    */
} MOCA_EVENT;

/*
 *  Function Prototypes
 */

#if defined (__cplusplus)
extern "C" {
#endif

long evtFreeEvents(MOCA_EVENT *events);
long evtRealize(MOCA_EVENT *events);
long evtRequest(MOCA_EVENT *events);

#if defined (__cplusplus)
}
#endif

#endif
