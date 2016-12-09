static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to support a memory mapped status file.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2004
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
#include <stdarg.h>
#include <string.h>
#include <time.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <common.h>
#include <mislib.h>
#include <oslib.h>

typedef struct
{
    long pid;
    char incoming_cmd[1000];
    char incoming_flags[50];
    char executing_cmd[1000];
    char executing_sql[1000];
    char keepalive_text[1000];
    char comment[200];
    char state[100];
    char environment[1000];
    OS_TIME last_in;
    OS_TIME last_cmd;
    OS_TIME last_sql;
} MIS__STATUS_RECORD;

static char statusFileName[1024];
static int statusPos = -1;
static MIS__STATUS_RECORD *statusMap;
static int status_TriedMap;

static void LockStatusMap(void)
{
    long status;

    /* Lock the memory associated with the status map. */
    status = osLockMem(statusMap);
    if (status != eOK)
    {
        misLogError("osLockMem: %s", osError( ));
        misLogError("LockStatusMap: Could not lock the status map file");
    }

    return;
}

static void UnlockStatusMap(void)
{
    long status;

    /* Lock the memory associated with the status map. */
    status = osUnlockMem(statusMap);
    if (status != eOK)
    {
        misLogError("osUnlockMem: %s", osError( ));
        misLogError("UnlockStatusMap: Could not unlock the status map file");
    }

    return;
}

void misSetStatusFile(char *fileName)
{
    if (fileName)
	strncpy(statusFileName, fileName, sizeof statusFileName);
}

void misSetStatusPos(int filepos)
{
    statusPos = filepos;
}

int misGetStatusPos(void)
{
    return statusPos;
}

static void ReleaseStatusRecord(int slot)
{
    return;
}

long misCreateStatusFile(int slots)
{
    long os_status;

    if (statusFileName[0])
    {
	os_status = osCreateMapFile(statusFileName, slots * sizeof (MIS__STATUS_RECORD));

	if (os_status != eOK)
	{
	    misLogWarning("Unable to create status file %s: %s",
			  statusFileName, osError());
	}
    }
    else
    {
	os_status = eERROR;
    }

    return os_status;
}

static MIS__STATUS_RECORD *mis_GetStatusRecord(int slot)
{
    long os_status;

    if (slot < 0)
	return NULL;

    /*
     * Only try to map the file once.  If it fails, don't even bother from here on in.
     */
    if (!statusMap && !status_TriedMap)
    {
	status_TriedMap = 1;

	/*
	 * If there's a file, map it read-write
	 */
	if (statusFileName[0])
	{
	    os_status = osMapFile(statusFileName, (void **)&statusMap, OS_MAPMODE_RW);
	    if (os_status != eOK)
	    {
		misLogWarning("Unable to open status file %s: %s",
			      statusFileName, osError());
	    }
	}
    }

    if (statusMap)
	return statusMap + slot;
    else
	return NULL;
}

void misUpdateStatus(int field, ...)
{
    va_list a;
    char *cfield;
    int keepalive_counter;
    int keepalive_value;
    struct mis_Keepalive *keepalive_list;
    MIS__STATUS_RECORD *myrec;
    char tmpbuf[1024];
    int ii;

    /* Get the actual slot within the status map file. */
    myrec = mis_GetStatusRecord(statusPos);
    if (! myrec)
        return;

    LockStatusMap( );

    va_start(a, field);

    switch (field)
    {
	case MOCASTAT_INCOMING_CMD:
	    cfield = va_arg(a, char *);
	    vsprintf(tmpbuf, cfield, a);
	    strncpy(myrec->incoming_cmd, tmpbuf, sizeof myrec->incoming_cmd - 1);
	    osGetTime(&myrec->last_in);
	    break;
	case MOCASTAT_EXEC_CMD:
	    cfield = va_arg(a, char *);
	    vsprintf(tmpbuf, cfield, a);
	    strncpy(myrec->executing_cmd, tmpbuf, sizeof myrec->executing_cmd - 1);
	    osGetTime(&myrec->last_cmd);
	    break;
	case MOCASTAT_EXEC_SQL:
	    cfield = va_arg(a, char *);
	    vsprintf(tmpbuf, cfield, a);
	    strncpy(myrec->executing_sql, tmpbuf, sizeof myrec->executing_sql - 1);
	    osGetTime(&myrec->last_sql);
	    break;
	case MOCASTAT_COMMENT:
	    cfield = va_arg(a, char *);
	    vsprintf(tmpbuf, cfield, a);
	    strncpy(myrec->comment, tmpbuf, sizeof myrec->comment - 1);
	    break;
	case MOCASTAT_ENVIRONMENT:
	    cfield = va_arg(a, char *);
	    vsprintf(tmpbuf, cfield, a);
	    strncpy(myrec->environment, tmpbuf, sizeof myrec->environment - 1);
	    break;
	case MOCASTAT_KEEPALIVE:
	    keepalive_value = va_arg(a, int);
	    keepalive_counter = va_arg(a, int);
            keepalive_list = va_arg(a, struct mis_Keepalive *);
            sprintf(tmpbuf, "(%d) ", keepalive_value);
            for (ii = 0; ii < keepalive_counter; ii++)
            {
                sprintf(tmpbuf + strlen(tmpbuf), "%s%s(%d)", 
                        (ii == 0) ? "" : ", ",
                        keepalive_list[ii].id,
                        keepalive_list[ii].counter);
            }
	    strncpy(myrec->keepalive_text, tmpbuf, sizeof myrec->keepalive_text - 1);
	    break;
	case MOCASTAT_STATE:
	    cfield = va_arg(a, char *);
	    vsprintf(tmpbuf, cfield, a);
	    strncpy(myrec->state, tmpbuf, sizeof myrec->state - 1);
	    break;
    }

    myrec->pid = osGetProcessId();

    UnlockStatusMap( );
}

long misGetStatus(int slot, int field, void **value, void *timestamp)
{
    MIS__STATUS_RECORD *myrec;

    /* Reset the passed in value. */
    *value = NULL;

    /* Get the actual slot within the status map file. */
    myrec = mis_GetStatusRecord(slot);
    if (! myrec)
        return eERROR;

    LockStatusMap( );

    switch (field)
    {
	case MOCASTAT_INCOMING_CMD:
            misTrc(T_MGR, "    Incoming command (%s)", myrec->incoming_cmd);
            misDynStrcpy((char **) value, myrec->incoming_cmd);
            * (OS_TIME *) timestamp = myrec->last_in;
            break;
        case MOCASTAT_EXEC_CMD:
            misTrc(T_MGR, "    Executing command (%s)", myrec->executing_cmd);
            misDynStrcpy((char **) value, myrec->executing_cmd);
            * (OS_TIME *) timestamp = myrec->last_cmd;
            break;
        case MOCASTAT_EXEC_SQL:
            misTrc(T_MGR, "    Executing SQL (%s)", myrec->executing_sql);
            misDynStrcpy((char **) value, myrec->executing_sql);
            * (OS_TIME *) timestamp = myrec->last_sql;
            break;
        case MOCASTAT_COMMENT:
            misTrc(T_MGR, "    Comment (%s)", myrec->comment);
            misDynStrcpy((char **) value, myrec->comment);
            break;
        case MOCASTAT_ENVIRONMENT:
            misTrc(T_MGR, "    Environment (%s)", myrec->environment);
            misDynStrcpy((char **) value, myrec->environment);
            break;
        case MOCASTAT_KEEPALIVE:
            misTrc(T_MGR, "    Keepalive (%s)", myrec->keepalive_text);
            misDynStrcpy((char **) value, myrec->keepalive_text);
            break;
        case MOCASTAT_STATE:
            misTrc(T_MGR, "    State (%s)", myrec->state);
            misDynStrcpy((char **) value, myrec->state);
            break;
    }

    UnlockStatusMap( );

    return eOK;
}
