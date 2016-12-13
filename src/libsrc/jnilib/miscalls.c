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
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <jnilib.h>
#include <srvlib.h>

#include "jniprivate.h"
#include "jnidefs.h"

void jni_misTrace(int level, char *msg)
{
    JNIEnv *env = jniGetEnv();

    jobject mocaServerAdapter;
    jstring jMsg;
    jint jLevel;
    long status;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return;
    
    /* If tracing is enabled then we actually trace it */
    if (gTraceEnabled && gTraceEnabled == JNI_TRUE)
    {
        /* we need to obtain the moca server adapter */
        mocaServerAdapter = jniGetServerAdapter( );
        if (!mocaServerAdapter) 
	    return;

        jMsg = jniNewStringFromBytes(env, msg);
        jLevel = (jint)level;

        /* Call Java trace method */
        (*env)->CallVoidMethod(env, 
			       mocaServerAdapter, 
			       MocaServerAdapter_trace, 
			       jLevel, 
			       jMsg);

        (*env)->DeleteLocalRef(env, jMsg);
    }

    return;
}

/*
 * This will log a message to either DEBUG (1), INFO (2), WARN(3) or ERROR(4)
 * level in log4j for java.
 */
void jni_misLog(int level, char *msg)
{
    JNIEnv *env = jniGetEnv();

    jobject mocaServerAdapter;
    jstring jMsg;
    jint jLevel;
    long status;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return;

    /* we need to obtain the moca server adapter */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter) 
        return;

    jMsg = jniNewStringFromBytes(env, msg);
    jLevel = (jint)level;

    /* Call Java log method */
    (*env)->CallVoidMethod(env, 
		           mocaServerAdapter, 
			   MocaServerAdapter_log, 
			   jLevel, 
			   jMsg);

    (*env)->DeleteLocalRef(env, jMsg);

    return;
}

void jni_misSetTraceFileName(char *name, char *mode)
{
    JNIEnv *env = jniGetEnv();

    jobject mocaServerAdapter;
    jstring jName;
    jboolean jAppend = JNI_FALSE;

    /* Initialize class and method ids. */
    if (jni_Initialize(env) != eOK)
        return;

    /* we need to obtain the moca server adapter */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter) 
        return;

    jName = jniNewStringFromBytes(env, name);

    /* If the mode has an a then we are in append */
    if (strstr(mode, "a") != NULL)
        jAppend = JNI_TRUE;

    /* Call Java setTraceFileName method */
    (*env)->CallVoidMethod(env, 
		           mocaServerAdapter, 
			   MocaServerAdapter_setTraceFileName, 
			   jName, 
			   jAppend);

    (*env)->DeleteLocalRef(env, jName);

    return;
}

void jni_misSetTraceLevel(int level)
{
    JNIEnv *env = jniGetEnv();

    jobject mocaServerAdapter;

    /* Initialize class and method ids. */
    if (jni_Initialize(env) != eOK)
        return;

    /* we need to obtain the moca server adapter */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter) 
        return;

    /* If the level is greater than 0, then we have to set our C flag saying tracing is on */
    if (level > 0)
        gTraceEnabled = JNI_TRUE;

    /* Call Java setTraceLevel method */
    (*env)->CallVoidMethod(env, 
		           mocaServerAdapter, 
			   MocaServerAdapter_setTraceLevel, 
			   (jint) level);

    return;
}

int jni_misGetTraceLevel()
{
    JNIEnv *env = jniGetEnv();

    long status;
    jobject mocaServerAdapter;
    jint jTraceLevel;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return status;

    /* we need to obtain the moca server adapter */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter) 
        return eJNI_SERVER_ADAPTER;

    /* If trace is enabled, ask the server what the level is */
    if (gTraceEnabled && gTraceEnabled == JNI_TRUE)
        /* Call Java getTraceLevel method */
        jTraceLevel = (*env)->CallIntMethod(env, 
		                            mocaServerAdapter, 
					    MocaServerAdapter_getTraceLevel);
    /* If not then just default to nothing */
    else 
        jTraceLevel = 0;

    return (int) jTraceLevel;
}
