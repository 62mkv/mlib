static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Hooks into the MocaClientAdapter class.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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
#include <string.h>
#include <jni.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <jnilib.h>
#include <mislib.h>

#include "jniprivate.h"
#include "jnidefs.h"

static char *gMessage;

static void sHandleException(JNIEnv *env, char *message)
{
    char *errorText;

    jthrowable exc;

    /* Free the current message. */
    free(gMessage);
    gMessage = NULL;

    /* Deal with any exception that may have been raised. */
    exc = (*env)->ExceptionOccurred(env);
    if (exc)
    {
        /* Clear the exception from the current JNI environment. */
        (*env)->ExceptionClear(env);

        /* Grab the exception error text. */
        errorText = jniToString(env, exc);

        /* Make a copy of the exception error text. */
        misDynSprintf(&gMessage, "%s\n", errorText);

	free(errorText);

        /*
         * We don't know if we're being called from a temporary JNI context,
         * so let's explicitly delete any references we've produced.
         */
        (*env)->DeleteLocalRef(env, exc);
    }

    /* Append the caller's error message to the exception error text. */
    if (message)
    {
        misDynStrcat(&gMessage, message);
        misDynStrcat(&gMessage, "\n");
    }
}

/*
 * Wrapper around MocaClientAdapter()
 *
 * MocaClientAdapter() can raise a MocaException.
 */

void *jni_mccInit(char *url, char *envString)
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;

    jstring jUrl = NULL,
	    jEnvString = NULL;

    jobject tempObject,
	    mocaClientAdapter = NULL;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return NULL;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    if (url) jUrl = (*env)->NewStringUTF(env, url);
    if (envString) jEnvString = (*env)->NewStringUTF(env, envString);

    /* Call the Java method. */
    tempObject = (*env)->NewObject(env,
                                   MocaClientAdapterClass,
                                   MocaClientAdapter_constructor,
                                   jUrl, 
				   jEnvString);
    if ((*env)->ExceptionCheck(env))
    {
	status = eMCC_CONSTRUCT_CLIENT;
        sHandleException(env, "Could not construct MocaClientAdapter");
        goto cleanup;
    }

    /* Create a global reference of this object. */
    mocaClientAdapter = (*env)->NewGlobalRef(env, tempObject);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (void *) mocaClientAdapter;
}

/*
 * Wrapper around MocaClientAdapter.close()
 *
 * MocaClientAdapter.close() doesn't raise any exceptions so we don't 
 * need to check them below.
 */

void jni_mccClose(void *mocaClientAdapter)
{
    JNIEnv *env = jniGetEnv( );

    /* Only call the Java method if we have an object and method to call. */
    if (mocaClientAdapter && MocaClientAdapter_close)
        (*env)->CallVoidMethod(env, 
			       (jobject) mocaClientAdapter, 
			       MocaClientAdapter_close);
}

/*
 * Wrapper around MocaClientAdapter.executeCommand()
 *
 * MocaClientAdapter.executeCommand() can raise a MocaException.
 */

long jni_mccExecStr(void *mocaClientAdapter, char *cmd, mocaDataRes **res)
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;
    RETURN_STRUCT *ret = NULL;

    jstring jCmd;
    jobject jRes;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jCmd = (*env)->NewStringUTF(env, cmd);

    /* Call the Java method. */
    jRes = (*env)->CallObjectMethod(env,
                                    (jobject) mocaClientAdapter,
                                    MocaClientAdapter_executeCommand,
                                    jCmd);


    /* Get a RETURN_STRUCT associated with this WrappedResults object.
     * This also does exception checking for us
     */
    status = jni_ExtractReturnStruct(env, jRes, &ret);

    /* 
     * It's possible that we weren't given a mocaDataRes. 
     *
     * If we were, we then extract the mocaDataRes from the RETURN_STRUCT 
     * and remove the reference to the mocaDataRes from the RETURN_STRUCT 
     * so that we can free everything associated with the RETURN_STRUCT 
     * w/out and still keep our own copy of the mocaDataRes.
     */
    if (res)
    {
        *res = ret->ReturnedData;
        ret->ReturnedData = NULL;

        if (ret->Error.DefaultText != NULL)
        {
            (*res)->Message = malloc(strlen(ret->Error.DefaultText) + 1);
            strcpy((*res)->Message, ret->Error.DefaultText);
        }
    }

    srvFreeMemory(SRVRET_STRUCT, ret);

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return status;
}

/*
 * Wrapper around MocaClientAdapter.login()
 *
 * MocaClientAdapter.login() can raise a LoginFailedException.
 */

long jni_mccLogin(void *mocaClientAdapter, char *userid, char *password, char *clientKey)
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;

    jstring jUserid = NULL,
	    jPassword = NULL,
	    jClientKey = NULL;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    if (userid) jUserid   = (*env)->NewStringUTF(env, userid);
    if (password) jPassword = (*env)->NewStringUTF(env, password);
    if (clientKey) jClientKey = (*env)->NewStringUTF(env, clientKey);

    /* Call the Java method. */
    (*env)->CallVoidMethod(env,
			   (jobject) mocaClientAdapter,
			   MocaClientAdapter_login,
                           jUserid,
                           jPassword,
			   jClientKey);
    if ((*env)->ExceptionCheck(env))
    {
	status = eMCC_LOGIN;
        sHandleException(env, "Could not login user");
        goto cleanup;
    }

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return status;
}

/*
 * Wrapper around MocaClientAdapter.logout()
 *
 * MocaClientAdapter.logout() can raise a LogoutFailedException.
 */
 
long jni_mccLogout(void *mocaClientAdapter)
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;

    /* Call the Java method. */
    (*env)->CallVoidMethod(env,
                           (jobject) mocaClientAdapter,
			   MocaClientAdapter_logout);
    if ((*env)->ExceptionCheck(env))
    {
	status = eMCC_LOGOUT;
        sHandleException(env, "Could not logout user");
        goto cleanup;
    }

cleanup:

    return status;
}

/*
 * Wrapper around MocaClientAdapter.setAutoCommit()
 *
 * MocaClientAdapter.setAutoCommit() doesn't raise any exceptions 
 * so we don't need to check them below.
 */

void jni_mccSetAutoCommit(void *mocaClientAdapter, int flag)
{
    JNIEnv *env = jniGetEnv( );

    /* Call the Java method. */
    (*env)->CallVoidMethod(env,
                           (jobject) mocaClientAdapter,
                           MocaClientAdapter_setAutoCommit,
			   (jboolean) flag);
}

/*
 * Wrapper around MocaClientAdapter.setApplicationId()
 *
 * MocaClientAdapter.setApplicationId() doesn't raise any exceptions 
 * so we don't need to check them below.
 */

void jni_mccSetApplicationId(void *mocaClientAdapter, char *appId)
{
    JNIEnv *env = jniGetEnv( );

    jstring jAppId = NULL;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    if (appId) jAppId = (*env)->NewStringUTF(env, appId);

    /* Call the Java method. */
    (*env)->CallVoidMethod(env,
                           (jobject) mocaClientAdapter,
                           MocaClientAdapter_setApplicationId,
                           jAppId);
    if ((*env)->ExceptionCheck(env))
    {
        sHandleException(env, "Could not set application id");
        goto cleanup;
    }

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return;
}

/*
 * Wrapper around MocaClientAdapter.setEnvironment()
 *
 * MocaClientAdapter.setEnvironment() doesn't raise any exceptions 
 * so we don't need to check them below.
 */

void jni_mccSetupEnvironment(void *mocaClientAdapter, char *envString)
{
    JNIEnv *env = jniGetEnv( );

    jstring jEnvString = NULL;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    if (envString) jEnvString = (*env)->NewStringUTF(env, envString);

    /* Call the Java method. */
    (*env)->CallVoidMethod(env,
                           (jobject) mocaClientAdapter,
                           MocaClientAdapter_setEnvironment,
                           jEnvString);
    if ((*env)->ExceptionCheck(env))
    {
        sHandleException(env, "Could not set environment");
        goto cleanup;
    }

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return;
}

/*
 * Simple function to return the last error message.
 */

char *jni_mccErrorMessage( )
{
    return gMessage ? gMessage : "";
}
