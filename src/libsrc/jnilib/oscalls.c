static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Hooks into the InProcessMocaServerAdapter class.
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
#include <jnilib.h>

#include "jniprivate.h"
#include "jnidefs.h"

typedef struct VarList
{
    char *ptr;
    struct VarList *next;
} VarList;

static VarList *VarListTop;

void jni_AddToVarList(char *ptr)
{
    VarList *node;

    node = calloc(1, sizeof(VarList));

    node->ptr = ptr;
    node->next = VarListTop;

    VarListTop = node;
}

void jni_FreeVarList(void)
{
    VarList *curr,
              *next;

    /* Point to the first item in the glohal reference list. */
    curr = VarListTop;

    /* Delete the global references to other objects. */
    while (curr != NULL)
    {
        next = curr->next;

        free(curr->ptr);
        curr->ptr = NULL;
        free(curr);

        curr = next;
    }

    VarListTop = NULL;
}

char *jni_osGetEnvValue(char *name)
{
    JNIEnv *env = jniGetEnv();

    static int calledAtexit;

    long status;

    char *value;

    jstring jName,
	    jValue;

    jobject mocaServerAdapter;

    if (!calledAtexit)
    {
        calledAtexit++;
	osAtexit(jni_FreeVarList);
    }

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return NULL;

    /* Get the current MocaServerAdapter object. */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter)
	return NULL;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string. */
    jName = jniNewStringFromBytes(env, name);

    /* Call the Java method */
    jValue = (*env)->CallObjectMethod(env, 
	                              mocaServerAdapter, 
				      MocaServerAdapter_getEnvironment,
				      jName);

    /* Get a C string. */
    value = jniDecodeString(env, jValue, NULL);

    /* Add this to the list of environment variables to free later on. */
    jni_AddToVarList(value);

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return value;
}

void jni_osPutEnvValue(char *name, char *value)
{
    JNIEnv *env = jniGetEnv();

    long status;

    jstring jName,
	    jValue;

    jobject mocaServerAdapter;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return;

    /* Get the current MocaServerAdapter object. */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter)
	return;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string. */
    jName  = jniNewStringFromBytes(env, name);
    jValue = jniNewStringFromBytes(env, value);

    /* Call the Java method */
    (*env)->CallVoidMethod(env, 
	                   mocaServerAdapter, 
			   MocaServerAdapter_putEnvironment,
			   jName,
			   jValue);

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return;
}

void jni_osRemoveEnvValue(char *name)
{
    JNIEnv *env = jniGetEnv();

    long status;

    jstring jName;

    jobject mocaServerAdapter;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return;

    /* Get the current MocaServerAdapter object. */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter)
	return;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string. */
    jName = jniNewStringFromBytes(env, name);

    /* Call the Java method */
    (*env)->CallVoidMethod(env, 
	                   mocaServerAdapter, 
			   MocaServerAdapter_removeEnvironment,
			   jName);

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return;
}
