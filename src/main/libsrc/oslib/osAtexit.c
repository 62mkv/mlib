static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to handle atexit( ) calls, which may be done
 *               from an external procedure.
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

#define MOCA_DEBUG_MALLOC_IGNORE

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>

#include <oslib.h>

typedef struct FunctionList
{
    OSFPTR function;
    int clean;
    struct FunctionList *next;
} FunctionList;

static FunctionList *gFunctionList;

static short gCalledAtexit = 0;

static void sAddToCleanupList(void (*function)(void), int clean)
{
    FunctionList *ptr;

    /* Allocate space for the new function list node. */
    ptr = (FunctionList *) calloc(1, sizeof(FunctionList));
    if (!ptr)
        OS_PANIC;

    /* Populate the new function list node. */
    ptr->function = function;
    ptr->clean    = clean;

    /* Push this node onto the front of the list. */
    ptr->next = gFunctionList;

    /* Point the function list to this new node. */
    gFunctionList = ptr;

    return;
}

static void sExecuteCleanupList(int clean)
{
    FunctionList *ptr, *next;

    /* Cycle through each of the registered functions. */
    /* We only want to free the pointers on the non clean run, since we know
     * that will happen after the clean one.  So on a clean run we only
     * run the functions and don't free other stuff.
     */
    for (ptr = gFunctionList; ptr; ptr = next)
    {
        if (clean == ptr->clean)
            (ptr->function)( );
        next = ptr->next;
	if (!clean)
            free(ptr);
    }
    if (!clean)
        gFunctionList = NULL;
}

int osAtexit(void (*function)(void))
{
    /* Register our cleanup function if we haven't yet. */
    if (!gCalledAtexit)
    {
        gCalledAtexit++;
        atexit(os_CleanupAtexit);
    }

    /* Add this function pointer to the cleanup list. */
    sAddToCleanupList(function, 0);

    return 0;
}

int osAtexitClean(void (*function)(void))
{
    /*
     * We purposely don't register our cleanup function here.  Instead
     * we rely on the caller of this function to call osExit( ) to force
     * the "clean" functions that were registered to be called.
     */

    /* Add this function pointer to the cleanup list. */
    sAddToCleanupList(function, 1);

    return 0;
}

void os_CleanupAtexit(void)
{
    sExecuteCleanupList(0);
}

void os_CleanupAtexitClean(void)
{
    sExecuteCleanupList(1);
}

int osExit(int code)
{
    /* 
     * Call the "clean" functions that were registered via osAtexitClean( ). 
     *
     * This will call functions that may exist in other shared libraries
     * before they are unloaded.  On UNIX this isn't an issue, but on 
     * Windows platforms there is no guarantee that a function will still 
     * be available to be called.  In fact, you're only supposed to register
     * functions via atexit( ) if they exist within your library.  In our 
     * case we have at least one case where we want to call a function that
     * sits outside our library - the DestroyJavaVM( ) function.
     */
    os_CleanupAtexitClean( );

    /* Call the "other" functions that were registered via osAtexit( ). */
    exit(code);
}
