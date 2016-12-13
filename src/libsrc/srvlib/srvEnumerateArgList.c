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
#include <string.h>
#include <stdlib.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <jnilib.h>
#include <mislib.h>
#include <oslib.h>

#include "srvprivate.h"


struct srv__argslist
{
    void *list;
};


/*
 *  FUNCTION: srvEnumerateArgList
 *
 *  PURPOSE:  Enumerate over the overstacked arguments in the
 *            where-clause of the current command.  
 *            
 *  NOTE(S):  Overstacked arguments are arguments that are not explicitly 
 *            defined in the command definition file for a command.
 *
 *  RETURNS:  eOK - An argument was returned.
 *            eERROR - Reached the end of the argument list.
 */

long srvEnumerateArgList(SRV_ARGSLIST **ctx, char *name, int *oper,
	                 void **value, char *dtype)
{
    /* Initialize the context if this is the first time we've been called. */
    if (!*ctx)
    {
	/* Allocate space for the context. */
	*ctx = malloc(sizeof(SRV_ARGSLIST));

        (*ctx)->list = NULL;
    }

    return jni_srvEnumerateArgList(&(*ctx)->list, name, oper, value, dtype, 0);
}


/*
 *  FUNCTION: srvEnumerateAllArgs
 *
 *  PURPOSE:  Enumerate over all the arguments in the where-clause 
 *            of the current command.  
 *
 *  RETURNS:  eOK - An argument was returned.
 *            eERROR - Reached the end of the argument list.
 */

long srvEnumerateAllArgs(SRV_ARGSLIST **ctx, char *name, int *oper,
	                 void **value, char *dtype)
{
    /* Initialize the context if this is the first time we've been called. */
    if (!*ctx)
    {
	/* Allocate space for the context. */
	*ctx = malloc(sizeof(SRV_ARGSLIST));

        (*ctx)->list = NULL;
    }
    return jni_srvEnumerateArgList(&(*ctx)->list, name, oper, value, dtype, 1);
}


/*
 *  FUNCTION: srvFreeArgList
 *
 *  PURPOSE:  Free up the memory associated with the argument 
 *            list used in the above functions.
 *
 *  RETURNS:  void
 */

void srvFreeArgList(SRV_ARGSLIST *ctx)
{
    jni_srvFreeArgList(ctx->list);
    free(ctx);
}
