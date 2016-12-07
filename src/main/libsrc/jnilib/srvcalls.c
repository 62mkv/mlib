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
#include <string.h>
#include <stdlib.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <jnilib.h>
#include <srvlib.h>
#include <sqllib.h>

#include "jniprivate.h"
#include "jnidefs.h"

long jni_srvInitialize(char *process, long singleThreaded)
{
    long status = eOK;
    JNIEnv *env;
    jobject serverAdapter;

    /* set the app name, to allow custom JVM configurations */
    jni_SetAppName(process);

    /* Make sure we were passed a process name. */
    if (!process || !strlen(process))
        return eINVALID_ARGS;

    /* Now, get the JNIEnv pointer.  This may create a Java VM */
    env = jniGetEnv();
    if (env == NULL)
    {
        fprintf(stderr, "Unable to initialize JVM\n");
	return eERROR;
    }

    /* If we've alrady got a server adapter we can just return. */
    serverAdapter = jniGetServerAdapter( );
    if (serverAdapter)
    {
        misTrc(T_SQL, "Using existing database connection...");
        return eOK;
    }

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Create a server adapter for this process. */    
    serverAdapter = jniCreateServerAdapter(env, process, singleThreaded);
    if (!serverAdapter)
    {
        status = eERROR;
        goto cleanup;
    }

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return status;
}

long jni_srvCommit(void)
{
    JNIEnv *env = jniGetEnv();

    long status;
    jobject mocaServerAdapter;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return status;

    /* Get the current MocaServerAdapter. */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter)
	return eJNI_SERVER_ADAPTER;

    /* Call Java commit method */
    (*env)->CallVoidMethod(env, mocaServerAdapter, MocaServerAdapter_commitTx);

    status = jni_CheckForErrors(env);

    return status;
}

long jni_srvRollback(void)
{
    JNIEnv *env = jniGetEnv();

    long status;
    jobject mocaServerAdapter;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return status;

    /* Get the current MocaServerAdapter. */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter)
	return eJNI_SERVER_ADAPTER;

    /* Call Java commit method */
    (*env)->CallVoidMethod(env, mocaServerAdapter, MocaServerAdapter_rollbackTx);

    status = jni_CheckForErrors(env);

    return status;
}

long jni_srvInitiateExecute(char *command, RETURN_STRUCT **ret,
                            mocaBindList *args, int keepCtx)
{
    JNIEnv *env = jniGetEnv();

    long status;

    jstring commandStr;
    jobject mocaServerAdapter;
    jobject argMap = NULL;
    jobject result;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return status;

    /* Get the current MocaServerAdapter. */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter)
	return eJNI_SERVER_ADAPTER;

    commandStr = jniNewStringFromBytes(env, command);

    if (args)
        argMap = jni_ArgMap(env, args);

    /* Call Java execute method */
    result = (*env)->CallObjectMethod(env, 
	                              mocaServerAdapter, 
				      MocaServerAdapter_executeCommand,
                                      commandStr, 
                                      argMap,
				      keepCtx ? JNI_FALSE : JNI_TRUE);

    /* 
     * Get the status and result set. 
     * This also does exception handling for us, so we don't need
     * to explicitly check for exceptions being raised above.
     */
    status = jni_ExtractNativeReturnStruct(env, result, ret, NULL);

    if (result)
        (*env)->DeleteLocalRef(env, result);
    (*env)->DeleteLocalRef(env, commandStr);
    (*env)->DeleteLocalRef(env, argMap);

    return status;
}

char *jni_srvMakeSessionKey(char *userid, char *output, long outsize)
{
    JNIEnv *env = jniGetEnv();

    long status;

    char *domain,
         *sessionKey;

    jstring jDomain,
            jUserid,
            jSessionKey;

    jobject mocaServerAdapter;

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

    /* Get the security domain from the registry file. */
    domain = osGetReg(REGSEC_SECURITY, REGKEY_SECURITY_DOMAIN);

    /* Get a Java string. */
    jDomain = jniNewStringFromBytes(env, domain);
    jUserid = jniNewStringFromBytes(env, userid);

    /* Call the Java method */
    jSessionKey = (*env)->CallStaticObjectMethod(env,
                                                 MocaSessionClass,
                                                 MocaSession_newSessionKey,
                                                 jUserid,
                                                 jDomain);

    /* Get a C string. */
    sessionKey = jniDecodeString(env, jSessionKey, NULL);

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    memset(output, '\0', outsize);
    memcpy(output, sessionKey, outsize-1);

    return sessionKey;
}

static void sFreeResults(void *data)
{
    sqlFreeResults(data);
}

static void sGetArgData(JNIEnv *env, 
	                jobject arg, 
			char *name, 
			char *dtype, 
			void **value, 
			long *length, 
			int *oper)
{
    jchar typecode;
    int byteLength;

    typecode = (*env)->CallStaticCharMethod(env, 
	                              NativeToolsClass,
                                      NativeTools_getArgType, 
				      arg);

    if (dtype)
    {
        *dtype = (char)typecode;
    }

    if (length)
    {
        *length = 0;
    }

    if (value)
    {
        jobject objValue;
        void (*destructor)(void *) = NULL;
        *value = NULL;

        objValue = (*env)->CallStaticObjectMethod(env, 
		                            NativeToolsClass,
                                            NativeTools_getArgValue, 
					    arg);

        if (objValue) {
            switch(typecode) {
            case COMTYP_BOOLEAN:
                *value = malloc(sizeof(int));
                *((int *)*value) = jni_BooleanValue(env, objValue);
                if (length) *length = sizeof(int);
                break;
            case COMTYP_INT:
                *value = malloc(sizeof(long));
                *((long *)*value) = jni_IntValue(env, objValue);
                if (length) *length = sizeof(int);
                break;
            case COMTYP_FLOAT:
                *value = malloc(sizeof(double));
                *((double *)*value) = jni_DoubleValue(env, objValue);
                if (length) *length = sizeof(double);
                break;
            case COMTYP_DATTIM:
            case COMTYP_CHAR:
                *value = jniToString(env, objValue);
                if (length && *value) *length = strlen(*value);
                break;
            case COMTYP_JAVAOBJ:
                break;
            case COMTYP_GENERIC:
                *value = malloc(sizeof(void *));
                *((void **)*value) = jni_PointerValue(env, objValue);
                if (length) *length = sizeof(void *);
                break;
            case COMTYP_RESULTS:
                jni_ExtractResults(env, objValue, (mocaDataRes **)value);
                destructor = sFreeResults;
                if (length) *length = sizeof(void *);
                break;
            case COMTYP_BINARY:
                byteLength = (*env)->GetArrayLength(env, (jbyteArray)objValue);
                /* We allocate it 8 bytes longer, since that is where we store
                 * the length
                 */
                *value = malloc(byteLength + 8);
                /* We put the length as base 16 first */
                sprintf(*value, "%*.*X", 8, 8, byteLength);
                (*env)->GetByteArrayRegion(env, objValue, (jint)0, 
                                           (jint)byteLength, 
                                           (jbyte*)*value + 8);
                if (length) *length = byteLength + 8;
                break;
            }

            /* Save allocated pointer to be freed later */
            jni_AddAccumulatedArg(*value, destructor);
        }
    }

    if (oper)
    {
        *oper = (*env)->CallStaticIntMethod(env, 
		                      NativeToolsClass,
                                      NativeTools_getArgOper,
				      arg);
    }

    if (name)
    {
        jstring strName = (*env)->CallStaticObjectMethod(env, 
		                                   NativeToolsClass,
                                                   NativeTools_getArgName,
						   arg);
        char *tmpName = jniDecodeString(env, strName, NULL);
        strncpy(name, tmpName, ARGNAM_LEN);
        free(tmpName);
    }

}

long jni_srvGetContextVar(char *name, char *alias, int inOper,
                          char *dtype, void **value, long *length,
                          int *outOper, int markused)
{
    JNIEnv *env = jniGetEnv();

    long status;

    jstring jName;
    jstring jAlias;
    jobject mocaServerAdapter;
    jobject result;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return status;

    /* Get the current MocaServerAdapter. */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter)
	return eJNI_SERVER_ADAPTER;

    jName = jniNewStringFromBytes(env, name);
    jAlias = jniNewStringFromBytes(env, alias);

    result = (*env)->CallObjectMethod(env, 
	                              mocaServerAdapter, 
				      MocaServerAdapter_getStackElement,
                                      jName, 
				      jAlias, 
				      markused ? JNI_TRUE : JNI_FALSE,
				      inOper == OPR_EQ ? JNI_TRUE : JNI_FALSE);

    (*env)->DeleteLocalRef(env, jName);
    (*env)->DeleteLocalRef(env, jAlias);

    if (!result) 
    {
        return eDB_NO_ROWS_AFFECTED;
    }

    sGetArgData(env, result, NULL, dtype, value, length, outOper);

    return eOK;
}

typedef struct
{
    int size;
    int pos;
    jobjectArray argsArray;
} ARGLIST;

long jni_srvEnumerateArgList(void **list, char *name, int *oper,
                             void **value, char *dtype, int getAll)
{
    JNIEnv *env = jniGetEnv();

    long status;

    ARGLIST *argList;

    jobject mocaServerAdapter;
    jobjectArray arg;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return status;

    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter)
	return eJNI_SERVER_ADAPTER;

    if (!list)
        return eERROR;

    if (!*list)
    {
        argList = malloc(sizeof(ARGLIST));
        argList->pos = 0;
        argList->argsArray = 
	    (jobjectArray) (*env)->CallObjectMethod(env,
                                                mocaServerAdapter, 
						MocaServerAdapter_getStackArgs,
						getAll ? JNI_TRUE : JNI_FALSE);

        if (argList->argsArray) 
        {
            argList->size = (*env)->GetArrayLength(env, argList->argsArray);
        }
        else
        {
            argList->size = 0;
        }
        *list = argList;
    }
    else
    {
        argList = *list;
    }

    /* Check for end of list */
    if (argList->pos >= argList->size)
    {
        return eERROR;
    }

    /* Get the next argument in the array */
    arg = (*env)->GetObjectArrayElement(env, argList->argsArray, argList->pos);
    if (!arg)
    {
        return eERROR;
    }

    /* Pull out the argument data */
    sGetArgData(env, arg, name, dtype, value, NULL, oper);

    argList->pos++;

    return eOK;
}


void jni_srvFreeArgList(void *list)
{
    JNIEnv *env = jniGetEnv();

    if (list)
    {
        ARGLIST *argList = list;
        (*env)->DeleteLocalRef(env, argList->argsArray);
        free(argList);
    }
}

char *jni_srvTranslateMessage(char *lookup)
{
    JNIEnv *env = jniGetEnv();
    jstring msg = NULL;
    jobject mocaServerAdapter;
    jstring jLookup = NULL;

    /* Get the current MocaServerAdapter. */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter)
	return NULL;
    
    jLookup = (*env)->NewStringUTF(env, lookup);
	
    /* Now we just call the translateErorrMessage method on the ServerAdapter */
    msg = (jstring) (*env)->CallObjectMethod(env,
                                   mocaServerAdapter,
				   MocaServerAdapter_translateMessage,
				   jLookup);

    if (jLookup)      (*env)->DeleteLocalRef(env, jLookup);

    if (msg)
    {
        /* Convert the UTF-16 string into a UTF-8 string. */
        return jniDecodeString(env, msg, NULL);
    }
    else
    {
        return NULL;
    }
}
