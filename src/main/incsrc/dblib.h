/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file for dblib.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2002-2009
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

#ifndef DBLIB_H
#define DBLIB_H

#include <common.h>

/*
 *  Database Engine Definitions
 */

#define MOCA_DB_NONE   0
#define MOCA_DB_ORACLE 1
#define MOCA_DB_MSSQL  2
#define MOCA_DB_JDBC   999

#define MOCA_DB_NONE_STR   "NONE"
#define MOCA_DB_ORACLE_STR "ORACLE"
#define MOCA_DB_MSSQL_STR  "MSSQL"
#define MOCA_DB_ERROR_STR  "ERROR"
#define MOCA_DB_JDBC_STR   "JDBC"

/*
 *  Function Prototypes
 */

#if defined (__cplusplus)
extern "C" {
#endif

long  dbCommit(void);
long  dbExecBind(char *sql, mocaDataRes **res, mocaBindList *list, ...);
long  dbExecBindParse(char *sql, mocaDataRes **res, mocaBindList *list, ...);
long  dbExecStr(char *sql, mocaDataRes **res);
long  dbExecStrFormat(mocaDataRes **res, char *sql, ...);
void  dbInterrupt(void);
long  dbRollback(void);
long  dbRollbackToSavepoint(char *savepoint);
long  dbSetSavepoint(char *savepoint);
char *dbGetNextVal(char *seqname);
long  dbPing(void);
long  dbInfo(int *dbtype);
void  dbLogPerformance(void);
long  dbErrorNumber(void);
char *dbErrorText(void);

#if defined (__cplusplus)
}
#endif

#endif
