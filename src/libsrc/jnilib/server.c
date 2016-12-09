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
 *  Copyright (c) 2005-2009
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

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <jnilib.h>

#include "jniprivate.h"
#include "jnidefs.h"

static jobject sInProcessMocaServerAdpater;

static void sHandleException(JNIEnv *env, char *message)
{
    char *errorText;

    jthrowable exc;

    /* Deal with any exception that may have been raised. */
    exc = (*env)->ExceptionOccurred(env);
    if (exc)
    {
        /* Clear the exception from the current JNI environment. */
        (*env)->ExceptionClear(env);

        /* Grab the exception error text. */
        errorText = jniToString(env, exc);

	/* If we don't have a server adapter we can't write trace messages. */
	if (sInProcessMocaServerAdpater)
	{
            misLogError("%s", errorText);
            misLogError("%s", message);
	}
	else
	{
            fprintf(stderr, "\n");
            fprintf(stderr, "%s\n", errorText);
            fprintf(stderr, "%s\n", message);
            fprintf(stderr, "\n");
	}

        free(errorText);

        /*
         * We don't know if we're being called from a temporary JNI context,
         * so let's explicitly delete any references we've produced.
         */
        (*env)->DeleteLocalRef(env, exc);
    }
}

jobject jniCreateServerAdapter(JNIEnv *env, char *name, long singleThreaded)
{
    long status;
    jstring jName;
    jobject tempObject;
    jboolean jSingleThreaded = JNI_FALSE;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
	return NULL;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jName = (*env)->NewStringUTF(env, name);

    if (singleThreaded)
        jSingleThreaded = JNI_TRUE;

    /* Call the Java method. */
    tempObject = (*env)->NewObject(env, 
	                           InProcessMocaServerAdapterClass,
				   InProcessMocaServerAdapter_constructor,
				   jName,
				   jSingleThreaded);
    if ((*env)->ExceptionCheck(env))
    {
        sHandleException(env, "Could not create server adapter");
	return NULL;
    }

    /* Create a global reference of this object. */
    sInProcessMocaServerAdpater = (*env)->NewGlobalRef(env, tempObject);

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return sInProcessMocaServerAdpater;
}

jobject jniGetServerAdapter(void)
{
    return sInProcessMocaServerAdpater;
}

jobject jniSetServerAdapter(JNIEnv *env, jobject obj)
{
    jobject oldAdapter = sInProcessMocaServerAdpater;

    /* Create a copy of this object. */
    sInProcessMocaServerAdpater = obj;

    return oldAdapter;
}
