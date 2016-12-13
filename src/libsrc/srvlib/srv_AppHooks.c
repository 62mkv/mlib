static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Default Application Hooks
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

#include <moca.h>

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <oslib.h>
#include <mocaerr.h>
#include "srvprivate.h"

/*
 * Lifecycle Hooks.  These can be used to affect the lifecycle of a
 * MOCA server process or session.  These hooks can be chained together
 * to allow multiple libraries to affect the lifecycle.
 */
typedef void (*InterpretEnvironmentHook)(char *);
typedef int (*AuthenticateSessionHook)(void);
typedef void (*UpdateActivityHook)(void);
typedef void (*LogUsageHook)(char *, char *, char *, long);
typedef long (*CheckLicenseHook)(void);
typedef void (*CleanupConnectionHook)(void);
typedef long (*PrecommitHook)(void);
typedef long (*PostcommitHook)(void);
typedef long (*RollbackHook)(void);
typedef long (*LogRollbackHook)(RETURN_STRUCT *);
typedef long (*PreReturnResultsHook)(char *, RETURN_STRUCT *);

/*
 * Translation Hooks.  These are used to translate messages and error
 * codes.  It never really makes sense to have multiples of these.
 */
typedef void (*GetColumnDescHook)(long, char **, char **, char **);
typedef char *(*ErrorMessageHook)(long);
typedef char *(*TranslateMessageHook)(char *);

typedef struct sHookList
{
    OSFPTR fp;
    struct sHookList *next;
} HOOK_LIST;

static HOOK_LIST *InterpretEnvironmentHookList;
static HOOK_LIST *AuthenticateSessionHookList;
static HOOK_LIST *UpdateActivityHookList;
static HOOK_LIST *LogUsageHookList;
static HOOK_LIST *CheckLicenseHookList;
static HOOK_LIST *CleanupConnectionHookList;
static HOOK_LIST *PrecommitHookList;
static HOOK_LIST *PostcommitHookList;
static HOOK_LIST *RollbackHookList;
static HOOK_LIST *LogRollbackHookList;
static HOOK_LIST *PreReturnResultsHookList;

static GetColumnDescHook GetColumnDescHookFunction = NULL;
static ErrorMessageHook ErrorMessageHookFunction = NULL;
static TranslateMessageHook TranslateMessageHookFunction = NULL;

static int calledAtexit = 0;

/*
 * Library-private functions to perform hooked functions.
 */
void srv_AppInterpretEnvironment(char *string)
{
    HOOK_LIST *hook;
    for (hook = InterpretEnvironmentHookList; hook; hook = hook->next)
    {
        if (hook->fp)
            (*(InterpretEnvironmentHook)hook->fp)(string);
    }
    return;
}

int srv_AppAuthenticateSession(void)
{
    HOOK_LIST *hook;
    for (hook = AuthenticateSessionHookList; hook; hook = hook->next)
    {
        if (hook->fp)
            if (!(*(AuthenticateSessionHook)hook->fp)())
                return 0;
    }
    return 1;
}

void srv_AppUpdateActivity(void)
{
    HOOK_LIST *hook;
    for (hook = UpdateActivityHookList; hook; hook = hook->next)
    {
        if (hook->fp)
            (*(UpdateActivityHook)hook->fp)();
    }
    return;
}

void srv_AppLogUsage(char *verb, char *noun, char *cmplvl, long exectime)
{
    HOOK_LIST *hook;
    for (hook = LogUsageHookList; hook; hook = hook->next)
    {
        if (hook->fp)
            (*(LogUsageHook)hook->fp)(verb, noun, cmplvl, exectime);
    }
    return;
}


void srv_AppCleanupConnection(void)
{
    HOOK_LIST *hook;
    for (hook = CleanupConnectionHookList; hook; hook = hook->next)
    {
        if (hook->fp)
            (*(CleanupConnectionHook)hook->fp)();
    }
    return;
}


long srv_AppCheckLicense(void)
{
    HOOK_LIST *hook;
    for (hook = CheckLicenseHookList; hook; hook = hook->next)
    {
        if (hook->fp)
        {
            long status = (*(CheckLicenseHook)hook->fp)();
            if (status != eOK) return status;
        }
    }
    return eOK;
}


long srv_AppPrecommit(void)
{
    HOOK_LIST *hook;
    for (hook = PrecommitHookList; hook; hook = hook->next)
    {
        if (hook->fp)
        {
            long status = (*(PrecommitHook)hook->fp)();
            if (status != eOK) return status;
        }
    }
    return eOK;
}


long srv_AppPostcommit(void)
{
    long status = eOK;
    HOOK_LIST *hook;
    for (hook = PostcommitHookList; hook; hook = hook->next)
    {
        if (hook->fp)
        {
            status = (*(PostcommitHook)hook->fp)();
        }
    }
    return status;
}


long srv_AppRollback(void)
{
    long status = eOK;
    HOOK_LIST *hook;
    for (hook = RollbackHookList; hook; hook = hook->next)
    {
        if (hook->fp)
        {
            status = (*(RollbackHook)hook->fp)();
        }
    }
    return status;
}

long srv_HaveAppLogRollbackHooks()
{
    if (LogRollbackHookList == NULL)
        return MOCA_FALSE;
    else
        return MOCA_TRUE;
    
}

long srv_AppLogRollback(RETURN_STRUCT *ret)
{
    long status = eOK;
    HOOK_LIST *hook;
    for (hook = LogRollbackHookList; hook; hook = hook->next)
    {
        if (hook->fp)
        {
            status = (*(LogRollbackHook)hook->fp)(ret);
        }
    }
    return status;
}

long srv_HaveAppPreReturnResultsHooks()
{
    if (PreReturnResultsHookList == NULL)
        return MOCA_FALSE;
    else
        return MOCA_TRUE;
    
}

long srv_AppPreReturnResults(char *command, RETURN_STRUCT *ret)
{
    HOOK_LIST *hook;
    long ret_status;
    for (hook = PreReturnResultsHookList; hook; hook = hook->next)
    {
        if (hook->fp)
        {
            ret_status = (*(PreReturnResultsHook)hook->fp)(command, ret);
            if (ret_status != eOK)
            {
                return ret_status;
            }
        }
    }
    return eOK;
}

/*
 * The following application hook functions are unchained:
 *     srv_AppGetColumnDesc()
 *     srv_AppErrorMessage()
 *     srv_AppTranslateMessage()
 */
void srv_AppGetColumnDesc(long ncol, char **collist,
	                          char **shtdsc, char **lngdsc)
{
    if (GetColumnDescHookFunction)
    {
        (*GetColumnDescHookFunction)(ncol, collist, shtdsc, lngdsc);
    }
}

char *srv_AppErrorMessage(long ErrorNumber)
{
    if (ErrorMessageHookFunction)
    {
        return (*ErrorMessageHookFunction)(ErrorNumber);
    }

    return NULL;
}

char *srv_AppTranslateMessage(char *message_id)
{
    if (TranslateMessageHookFunction)
    {
        return (*TranslateMessageHookFunction)(message_id);
    }

    return NULL;
}


/* Manipulate hook lists */
static void freeHooks(HOOK_LIST **list)
{
    HOOK_LIST *tmp, *next;
    for (tmp = *list; tmp; tmp = next)
    {
        next = tmp->next;
        free(tmp);
    }
    *list = NULL;
}

static void freeAllHooks(void)
{
    freeHooks(&InterpretEnvironmentHookList);
    freeHooks(&AuthenticateSessionHookList);
    freeHooks(&UpdateActivityHookList);
    freeHooks(&LogUsageHookList);
    freeHooks(&CheckLicenseHookList);
    freeHooks(&CleanupConnectionHookList);
    freeHooks(&PrecommitHookList);
    freeHooks(&PostcommitHookList);
    freeHooks(&RollbackHookList);
    freeHooks(&LogRollbackHookList);
    freeHooks(&PreReturnResultsHookList);
}

static void addHook(HOOK_LIST **list, OSFPTR fp)
{
    HOOK_LIST *tmp = malloc(sizeof (HOOK_LIST));
    tmp->fp = fp;
    tmp->next = *list;
    *list = tmp;

    /* If we're installing any chained hooks, free them at process shutdown */
    if (!calledAtexit)
    {
        osAtexit(freeAllHooks);
    }
}

/*
 * Public hook functions
 */
void srvHookInterpretEnvironment(void (*f)(char *))
{
    addHook(&InterpretEnvironmentHookList, (OSFPTR)f);
}

void srvHookAuthenticateSession(int (*f)(void))
{
    addHook(&AuthenticateSessionHookList, (OSFPTR)f);
}

void srvHookUpdateActivity(void (*f)(void))
{
    addHook(&UpdateActivityHookList, (OSFPTR)f);
}

void srvHookLogUsage(void (*f)(char *, char *, char *, long))
{
    addHook(&LogUsageHookList, (OSFPTR)f);
}

/* This hook is currently used by MCS to do interactive auditing 
 * It is fired right before we dispatch the results back to the client 
 * and exists outside of the transaction context 
 * See $MCSDIR/src/libsrc/mcsbase/baseAuditCommand.c */
void srvHookPreReturnResults(long (*f)(char *, RETURN_STRUCT *))
{
    addHook(&PreReturnResultsHookList, (OSFPTR)f);
}

void srvHookCheckLicense(long (*f)(void))
{
    addHook(&CheckLicenseHookList, (OSFPTR)f);
}

void srvHookCleanupConnection(void (*f)(void))
{
    addHook(&CleanupConnectionHookList, (OSFPTR)f);
}

void srvHookPrecommit(long (*f)(void))
{
    addHook(&PrecommitHookList, (OSFPTR)f);
}

void srvHookPostcommit(long (*f)(void))
{
    addHook(&PostcommitHookList, (OSFPTR)f);
}

void srvHookRollback(long (*f)(void))
{
    addHook(&RollbackHookList, (OSFPTR)f);
}

void srvHookLogRollbackUsingReturnStruct(long (*f)(RETURN_STRUCT *))
{
    addHook(&LogRollbackHookList, (OSFPTR)f);
}

void srvHookGetColumnDesc(void (*f)(long, char **, char **, char **))
{
    GetColumnDescHookFunction = f;
}

void srvHookErrorMessage(char *(*f)(long))
{
    ErrorMessageHookFunction = f;
}

void srvHookTranslateMessage(char *(*f)(char *))
{
    TranslateMessageHookFunction = f;
}

