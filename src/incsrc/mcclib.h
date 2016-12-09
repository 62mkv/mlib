/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file for mcclib.
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

#ifndef MCCLIB_H
#define MCCLIB_H

#include <moca.h>
#include <common.h>
#include <oslib.h>

/*
 *  Client Information Type Definition
 */

typedef struct mcc_ClientInfo mccClientInfo;

/*
 *  Function Prototypes
 */

#if defined (__cplusplus)
extern "C" {
#endif

/* mccClose.c */
long MOCAEXPORT mccClose(mccClientInfo *client);

/* mccDisableAutoCommit.c */
long MOCAEXPORT mccDisableAutoCommit(mccClientInfo *client);

/* mccEnableAutoCommit.c */
long MOCAEXPORT mccEnableAutoCommit(mccClientInfo *client);

/* mccError.c */
char *MOCAEXPORT mccErrorMessage(mccClientInfo *client);

/* mccExecStr.c */
long MOCAEXPORT mccExecStr(mccClientInfo *client, char *cmd, mocaDataRes **res);
long MOCAEXPORT mccExecStrWithApp(mccClientInfo *client, char *cmd,
                           char *applicationID, mocaDataRes **res);

/* mccInit.c */
mccClientInfo * MOCAEXPORT mccInit(char *host, unsigned short port, char *env);

/* mccLogin.c */
long MOCAEXPORT mccLogin(mccClientInfo *client, char *userid, char *password);
long MOCAEXPORT mccLoginWithClientKey(mccClientInfo *client, char *userid, char *password, char *clientKey);

/* mccLogout.c */
long MOCAEXPORT mccLogout(mccClientInfo *client);

/* mccSetupEnvironment.c */
long MOCAEXPORT mccSetupEnvironment(mccClientInfo *client, char *env);

/* mccSetApplicationID.c */
long MOCAEXPORT mccSetApplicationID(mccClientInfo *client, char *applicationID);

#if defined (__cplusplus)
}
#endif

#endif
