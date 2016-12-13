static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions used by the server and server applications
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
#include <stdlib.h>

#include <mocaerr.h>
#include <mocagendef.h>

#include "srvprivate.h"

static int gRecursiveAfterXaction;

typedef struct FuncList
{
    void (*function)(void *data);
    void *data;
    struct FuncList *next;
} FUNC_LIST;

static FUNC_LIST *headCommitFuncList,
		 *headRollbackFuncList;

void srvExecuteAfterCommit(void (*function)(void *data), void *data)
{
    if (function)
    {
	FUNC_LIST *this;

	this = malloc(sizeof(FUNC_LIST));

	this->function = function;
	this->data     = data;
	this->next     = headCommitFuncList;

	headCommitFuncList = this;
    }
}

void srv_ExecuteAfterCommit(int callFunction)
{
    FUNC_LIST *this, 
              *next;

    /* If we're being called recursively, just return. */
    if (gRecursiveAfterXaction)
	return;

    /* Set the recursive flag. */
    gRecursiveAfterXaction = 1;

    for (this=headCommitFuncList; this; this=next)
    {
	if (callFunction && this->function)
	    (*this->function)(this->data);

	next = this->next;

	free(this);
    }

    headCommitFuncList = NULL;

    /* Clear the recursive flag. */
    gRecursiveAfterXaction = 0;
}

void srvExecuteAfterRollback(void (*function)(void *data), void *data)
{
    if (function)
    {
        FUNC_LIST *this;

        this = malloc(sizeof(FUNC_LIST));

        this->function = function;
        this->data     = data;
        this->next     = headRollbackFuncList;

        headRollbackFuncList = this;
    }
}

void srv_ExecuteAfterRollback(int callFunction)
{
    FUNC_LIST *this,
              *next;

    /* If we're being called recursively, just return. */
    if (gRecursiveAfterXaction)
        return;

    /* Set the recursive flag. */
    gRecursiveAfterXaction = 1;

    for (this=headRollbackFuncList; this; this=next)
    {
        if (callFunction && this->function)
            (*this->function)(this->data);

        next = this->next;

        free(this);
    }

    headRollbackFuncList = NULL;

    /* Clear the recursive flag. */
    gRecursiveAfterXaction = 0;
}
