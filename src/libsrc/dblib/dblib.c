static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Hooks into the MOCA JDBC DB library.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005-2008
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

#include <mocaerr.h>
#include <mocagendef.h>
#include <dblib.h>
#include <mislib.h>
#include <oslib.h>
#include <sqllib.h>

#include "../jnilib/dbcalls.h"

static long gErrorCode;
static char gMessage[1024];

static long sExecStr(char *sqlStmt,
                     mocaDataRes **res,
                     mocaBindList *bindList,
                     int parseFlag)
{
    static int parseOverride = -1;
    static FILE *parseLog = NULL;

    long status;

    /* Check for override of parse logic if we haven't yet. */
    if (parseOverride < 0)
    {
        char *InhibitVar,
             *ParseLog;

        InhibitVar = osGetVar(ENV_PREFIX "SQLPARSE_INHIBIT");
        if (InhibitVar != NULL &&
            (0 == misCiStrcmp(InhibitVar, "yes") ||
             0 == misCiStrcmp(InhibitVar, "true") ||
             0 == misCiStrcmp(InhibitVar, "on")))
        {
            parseOverride = 1;
        }
        else
        {
            parseOverride = 0;
        }

        ParseLog = osGetVar(ENV_PREFIX "SQLPARSE_LOGFILE");
        if (ParseLog)
            parseLog = fopen(ParseLog, "a");
    }

    /* Change the parse flag if it's supposed to be overriden. */
    if (parseOverride && parseFlag)
        parseFlag = 0;

    /* Handle tracing. */
    misTrc(T_SQL, "==> %s", sqlStmt);

    if (bindList && (misGetTraceLevel() & T_SQL))
    {
        char *tmpstr;
        tmpstr = sqlUnbind(sqlStmt, bindList);
        misTrc(T_SQL, "==> %s", tmpstr);
        free(tmpstr);
    }

    /* First attempt at parsing and executing the SQL statement. */
    status = jni_dbExecute(sqlStmt, res, bindList, parseFlag);

    /* Handle tracing. */
    misTrc(T_SQL,"*** SQL Status: %ld", status);

    return status;
}

long dbExecBindParse(char *sql, 
	              mocaDataRes **res, 
                      mocaBindList *inBindList, 
		      ...)
{
    long status;
    va_list args;
    mocaBindList *bindList = NULL;

    /* Set our bind list from the user's bind list or arguments. */
    if (inBindList)
    {
        bindList = inBindList;
    }
    else
    {
        va_start(args, inBindList);
        sqlBuildBindListFromArgs(&bindList, args);
        va_end(args);
    }

    /* Update the status of this command. */
    misUpdateStatus(MOCASTAT_EXEC_SQL, "%.999s", sql);

    /* Execute the SQL statement with our bind list. */
    status = sExecStr(sql, res, bindList, 1);

    /* Free our bind list only if we created it. */
    if (!inBindList)
        sqlFreeBindList(bindList);

    return status;
}

long dbExecBind(char *sql, 
	         mocaDataRes **res, 
                 mocaBindList *inBindList, 
		 ...)
{
    long status;
    va_list args;
    mocaBindList *bindList = NULL;

    /* Set our bind list from the user's bind list or arguments. */
    if (inBindList)
    {
        bindList = inBindList;
    }
    else
    {
        va_start(args, inBindList);
        sqlBuildBindListFromArgs(&bindList, args);
        va_end(args);
    }

    /* Update the status of this command. */
    misUpdateStatus(MOCASTAT_EXEC_SQL, "%.999s", sql);

    /* Execute the SQL statement with our bind list. */
    status = sExecStr(sql, res, bindList, 0);

    /* Free our bind list only if we created it. */
    if (!inBindList)
        sqlFreeBindList(bindList);

    return status;
}

long dbExecStr(char *sql, mocaDataRes **res)
{
    return sExecStr(sql, res, NULL, 1);
}

long dbExecStrFormat(mocaDataRes **res, char *fmt, ...)
{
    long status,
	 length;

    char *sql;

    va_list args;

    va_start(args, fmt);

    /* Determine what the length of the sql will be. */
    if ((length = misSprintfLen(fmt, args)) < 0)
        return length;

    /* Allocate space for the sql. */
    if ((sql = malloc(length + 1)) == NULL)
        return eNO_MEMORY;

    /* Build the sql. */
    vsprintf(sql, fmt, args);

    va_end(args);

    /* Actually execute the sql. */
    status = dbExecStr(sql, res);

    /* Free the memory. */
    free(sql);

    return status;
}

long dbCommit(void)
{
    return jni_dbCommit( );
}

long dbRollback(void)
{
    return jni_dbRollback( );
}

long dbRollbackToSavepoint(char *savepoint)
{
    return jni_dbRollbackToSavepoint(savepoint);
}

long dbSetSavepoint(char *savepoint)
{
    return jni_dbSetSavepoint(savepoint);
}

char *dbGetNextVal(char *name)
{
    return jni_dbGetNextVal(name);
}

long dbPing(void)
{
    return jni_dbPing( );
}

long dbInfo(int *dbtype)
{
    return jni_dbInfo(dbtype);
}

void dbLogPerformance(void)
{
    return;
}

long dbErrorNumber(void)
{
    return jni_dbErrorNumber( );
}

char *dbErrorText(void)
{
    return jni_dbErrorText( );
}
