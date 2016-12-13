static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: 
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

#include <stdlib.h>
#include <string.h>

#include <mocagendef.h>
#include <jnilib.h>

#include "osprivate.h"

typedef struct VarList
{
    char *tag;
    char *name;
    char *value;
    struct VarList *next;
} VarList;

static int calledAtexit;

static VarList *ListTop;

void os_FreeVarList(void)
{
    VarList *curr, 
            *next;

    /* Cycle through the variable list. */
    for (curr=ListTop; curr; curr=next)
    {
        /* Get a pointer to the next item in the list. */
        next = curr->next;

        /* Free up this item. */
        if (curr->tag)   free(curr->tag);
        if (curr->name)  free(curr->name);
        if (curr->value) free(curr->value);
        free(curr);
    }

    /* NULL out the top of the list. */
    ListTop = NULL;

    return;
}

/*
 * Get a variable from our list. If we don't have the variable, try using
 * environment.
 */
static char *os_GetEnvVar(char *name)
{
    register VarList *p;

    for (p=ListTop;p;p=p->next)
    {
        if (0==strcmp(name, p->name))
        {
            return p->value;
        }
    }

    /*
     * OK, we didn't find it in our internal list, let's look
     * it up in the real environment.
     */
    return getenv(name);

}

/*
 * This will put a variable into the local cache and update the JNI side
 * if needed.  The onJNISide variable is to passed in as 0 when
 * osPutVar is called directly since we want to update the JNI side if
 * the value has changed.  If onJNISide is passed in as something other
 * than 0 then that means that it was determined that this value was 
 * already present on the JNI side and we just need to update our cache.
 */
void os_PutVar(char *name, char *value, char *tag, int onJNISide)
{
    VarList *p,*last;
    int sameValue = 0;
    int foundValue = 0;

    /* 
     * We don't check the cache if it was deemed to be only on the JNI side.
     * This will in turn guarantee that the value was not found and that it
     * wasn't the same.
     */
    if (!onJNISide)
    {
        /* 
         * First we see if the value was already in our list, if it was then
         * we have to see if the value changed and if so then we set it to
         * the new value.
         */
        for (last = NULL, p = ListTop; p; last = p, p = p->next)
        {
            if (0 == strcmp(name, p->name) && 
                ((!tag && !p->tag) ||
                 (tag && p->tag && 0 == strcmp(tag, p->tag))))
            {
                foundValue = 1;
                /* If the values are the same then we leave it alone */
                if ((!value && !p->value) || (value && p->value && 0 == strcmp(value, p->value)))
                {
                    sameValue = 1;
                }
                else 
                {
                    free(p->value);
                    if (value)
                    {
                        p->value = malloc(strlen(value)+1);
                        strcpy(p->value, value);
                    }
                    else
                        p->value = NULL;

                    /* Push variable to the front of the list */
                    if (last)
                    {
                        last->next = p->next;
                        p->next = ListTop;
                        ListTop = p;
                    }
                }
            }
        }
    }

    /* If the value was not found then insert it to the top */
    if (!foundValue)
    {
        p = malloc(sizeof(VarList));
        p->next = ListTop;

        if (tag)
        {
            p->tag = malloc(strlen(tag)+1);
            strcpy(p->tag, tag);
        }
        else
        {
            p->tag = NULL;
        }


        p->name = malloc(strlen(name)+1);
        strcpy(p->name, name);

        if (value)
        {
            p->value = malloc(strlen(value)+1);
            strcpy(p->value, value);
        }
        else
            p->value = NULL;

        ListTop = p;
    }
    /* 
     * If we can call Jni and it was not the same value and
     * if we have a server adapter we just use that and we're done.
     */
    if (!onJNISide && !sameValue && jniGetServerAdapter( ))
    {
        jni_osPutEnvValue(name, value);
    }

    if (! calledAtexit)
    {
        calledAtexit++;
        osAtexit(os_FreeVarList);
    }

    return;
}

#define MAX_STACK 32
char *osGetVar(char *name)
{
    char *value = NULL;

    static int stackLevel;
    static char *stack[MAX_STACK];
    int foundValue = 0;

    VarList *p;

    /* First check our internal list */
    for (p=ListTop;p;p=p->next)
    {
        if (0==strcmp(name, p->name))
        {
            foundValue = 1;
            value = p->value;
        }
    }

    /* If we didn't find it in our internal list then try everything else */
    if (!foundValue)
    {
        /* 
         * If we have a server adapter, then check the jni side
         */
        if (jniGetServerAdapter( ))
	{
	    value = jni_osGetEnvValue(name);
	}
	else
	{
	    /*
	     * OK, we don't have a server adapter, so we need to simulate
	     * the behavior in C.  First, let's look it up in the real
	     * environment.
	     */
	    value = getenv(name);

	    /* Second try getting the environment variable from the registry. */
	    if (!value)
	    {
		int ii;

		/* We have to make sure osGetRegistry didn't turn around and call us. */
		for (ii = 0; ii < stackLevel; ii++)
		{
		    if (0 == misCiStrcmp(name, stack[ii]))
			goto novalue;
		}

		if (stackLevel < MAX_STACK)
		{
		    stack[stackLevel++] = name;
		    value = osGetRegistryValue(REGSEC_ENVIRONMENT, name);
		    stack[--stackLevel] = NULL;
		}
	    }
	}

novalue:
        /* Now we have to update our cache, by putting the value retrieved
         * from JNI, but we don't want to call back into JNI when we set
         * it, since we know it already had this value
         */
        os_PutVar(name, value, NULL, 1);
    }

    return value;
}

void osPutVar(char *name, char *value, char *tag)
{
    /* We call the other command forcing it to go to jni if not found */
    os_PutVar(name, value, tag, 0);
}

/*
 * Remove from the variable list.
 */
void osRemoveVar(char *name, char *tag)
{
    VarList *p, *next, *last;

    /* 
     * We need to clear the java side from the variable
     */
    if (jniGetServerAdapter( ))
    {
        jni_osRemoveEnvValue(name);
    }

    /* 
     * We want to then clear the local cache: the old way of doing environment 
     * variables.
     */
    for (last = NULL, p = ListTop; p; p = next)
    {
        next = p->next;

        if ((!name && (!tag || (p->tag && 0 == strcmp(tag, p->tag))))
            || (name && 0 == strcmp(name, p->name)))
        {
            if (p->tag)
                free(p->tag);
            free(p->name);
            free(p->value);
            free(p);

            if (last)
                last->next = next;
            else
                ListTop = next;
        }
        else
        {
            last = p;
        }
    }
}

