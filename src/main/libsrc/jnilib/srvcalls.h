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
 *  Copyright (c) 2009
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

#ifndef SRVCALLS_H
#define SRVCALLS_H

#include <common.h>
#include <srvlib.h>

/*
 * Function Prototypes
 */

long jni_srvInitialize(char *process, long singleThreaded);

long jni_srvCommit(void);

long jni_srvRollback(void);

long jni_srvInitiateExecute(char *command, 
	                    RETURN_STRUCT **res, 
                            mocaBindList *args,
			    int keepCtx);

char *jni_srvMakeSessionKey(char *userid, char *output, long outsize);

long jni_srvGetContextVar(char *name, 
	                  char *alias, 
			  int inOper,
	                  char *dtype, 
			  void **value, 
			  long *length,
			  int *outOper, 
			  int markused);

long jni_srvEnumerateArgList(void **list, 
	                     char *name, 
			     int *oper,
	                     void **value, 
			     char *dtype, 
			     int getAll);

void jni_srvFreeArgList(void *list);

char *jni_srvTranslateMessage(char *lookup);

#endif
