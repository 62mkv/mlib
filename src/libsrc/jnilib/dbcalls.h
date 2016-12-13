/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Header file for Java integration.
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

#ifndef DBCALLS_H
#define DBCALLS_H

#include <common.h>

/*
 * Function Prototypes
 */

long jni_dbExecute(char *sqlStmt, 
		   mocaDataRes **res, 
		   mocaBindList *bindList, 
		   int parseFlag);

long jni_dbCommit(void);

long jni_dbRollback(void);

long jni_dbRollbackToSavepoint(char *savepoint);

long jni_dbSetSavepoint(char *savepoint);

char *jni_dbGetNextVal(char *name);

long jni_dbPing(void);

long jni_dbInfo(int *dbtype);

long jni_dbErrorNumber(void);

char *jni_dbErrorText(void);

#endif
