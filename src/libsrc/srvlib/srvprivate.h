/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Private header file for srvlib.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2002
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

#ifndef SRVPRIVATE_H
#define SRVPRIVATE_H

#include <mislib.h>
#include <oslib.h>
#include <srvlib.h>

#include "../jnilib/srvcalls.h"

/*
 *  Helpful MIN/MAX Definitions
 */

#ifndef MIN
#define MIN(x,y) ((x) > (y) ? (y) : (x))
#endif

#ifndef MAX
#define MAX(x,y) ((x) > (y) ? (x) : (y))
#endif

struct srv__CompiledCommand
{
    char *command_text;
};

#if defined (__cplusplus)
extern "C" {
#endif

/*
 *  Function Prototypes
 */

/* srv_AppHooks.c */
void  srv_AppInterpretEnvironment(char *);
int   srv_AppAuthenticateSession(void);
void  srv_AppUpdateActivity(void);
void  srv_AppLogUsage(char *, char *, char *, long);
void  srv_AppGetColumnDesc(long, char **, char **, char **);
char *srv_AppErrorMessage(long);
char *srv_AppTranslateMessage(char *);
long  srv_AppCheckLicense(void);
void  srv_AppCleanupConnection(void);
long  srv_AppPrecommit(void);
long  srv_AppPostcommit(void);
long  srv_AppRollback(void);
long  srv_AppLogRollback(RETURN_STRUCT *);
long  srv_AppPreReturnResults(char *, RETURN_STRUCT *);
long  srv_HaveAppLogRollbackHooks(void);
long  srv_HaveAppPreReturnResultsHooks();

/* srvExecuteAfterCommit.c */
void srv_ExecuteAfterCommit(int callFunction);

/* srvExecuteAfterRollback.c */
void srv_ExecuteAfterRollback(int callFunction);

/* srv_FormatCommand.c */
char *srv_FormatCommand(char *fmt, va_list args);

/* srvGetNeededElement.c */
long srv_GetContextVar(char *element1, 
		       char *element2, 
		       int inOper,
		       char *dtype, 
		       void **data, 
		       long *length,
                       int *outOper, 
		       int markAsUsed);

/* srvRegisterData.c */
void srv_DestroyRegisteredData(void);

/* srvResultsMessage.c */
char *srv_ResultsMessageStatus(RETURN_STRUCT *ret, long status);

/* srvlib.c */
int srv_KeepaliveLevel(void);

#if defined (__cplusplus)
}
#endif

#endif
