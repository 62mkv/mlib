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

#ifndef MCCCALLS_H
#define MCCCALLS_H

#include <jni.h>
#include <common.h>

/*
 * Function Prototypes
 */

void *jni_mccInit(char *url, char *envString);
void  jni_mccClose(void *mocaClientAdapter);
long  jni_mccExecStr(void *mocaClientAdapter, char *cmd, mocaDataRes **res);
long  jni_mccLogin(void *mocaClientAdpater, char *userid, char *password, char *clientKey);
long  jni_mccLogout(void *mocaClientAdapter);
void  jni_mccSetAutoCommit(void *mocaClientAdapter, int flag);
void  jni_mccSetApplicationId(void *mocaClientAdapter, char *appId);
void  jni_mccSetupEnvironment(void *mocaClientAdapter, char *envString);
char *jni_mccErrorMessage(void);

#endif
