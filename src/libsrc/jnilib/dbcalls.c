static char RCS_Id[] = "$:Id srvcalls.c 196394 2009-05-21 13:34:12Z dinksett $";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Hooks into the MocaServerAdapter class.
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
#include <sqllib.h>
#include <srvlib.h>

#include "jniprivate.h"
#include "jnidefs.h"

static long gErrorCode;
static char gMessage[1024];

static long sHandleException(JNIEnv *env, char *message)
{
    char *temp;

    jthrowable exc;

    /* Get the exception from the current JNI environment. */
    exc = (*env)->ExceptionOccurred(env);

    if (exc)
    {
	/* Clear the exception from the current JNI environment. */
	(*env)->ExceptionClear(env);

	/* Grab the exception error text. */
	temp = jniToString(env, exc);
	memset(gMessage, 0, sizeof gMessage);
	strncpy(gMessage, temp, sizeof(gMessage) - 1);
	free(temp);

	(*env)->DeleteLocalRef(env, exc);
    }

    return eOK;
}

static void sAddToBindList(JNIEnv *env, 
	                   jobject jBindList, 
			   mocaBindList *bindList)
{
    mocaBindList *tmpBp;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /*
     * Zip through the passed-in bind list, setting values on the
     * Java BindList object.
     */
    for (tmpBp = bindList; tmpBp; tmpBp= tmpBp->next)
    {
        jstring nameString = (*env)->NewStringUTF(env, tmpBp->name);
        jstring dataString = NULL;

        if (tmpBp->nullind || !tmpBp->data)
        {
            (*env)->CallVoidMethod(env, 
		                   jBindList, 
				   BindList_addObjectWithSize,
                                   nameString, 
				   (jchar) tmpBp->dtype, 
				   NULL,
                                   (jint) tmpBp->size);
        }
        else
        {
            switch (tmpBp->dtype) 
	    {
                case COMTYP_INT:
                case COMTYP_LONG:
                case COMTYP_LONGPTR:
                    (*env)->CallVoidMethod(env, 
			                   jBindList, 
					   BindList_addInt,
                                           nameString, 
					   (jchar) tmpBp->dtype,
                                           *((long *) tmpBp->data));

                    break;
                case COMTYP_FLOAT:
                case COMTYP_FLOATPTR:
                    (*env)->CallVoidMethod(env, 
			                   jBindList, 
					   BindList_addDouble,
                                           nameString, 
					   (jchar) tmpBp->dtype,
                                           *((double *) tmpBp->data));
                    break;
                case COMTYP_BOOLEAN:
                    (*env)->CallVoidMethod(env, 
			                   jBindList, 
					   BindList_addBoolean,
                                           nameString, 
					   (jchar) tmpBp->dtype,
                                           *((long *) tmpBp->data) != 0);
                    break;
                case COMTYP_CHAR:
                case COMTYP_CHARPTR:
                    dataString = 
			jniNewStringFromBytes(env, (char *) tmpBp->data);
                    (*env)->CallVoidMethod(env, 
			                   jBindList, 
					   BindList_addObjectWithSize,
                                           nameString, 
					   (jchar)tmpBp->dtype,
                                           dataString, 
					   (jint)tmpBp->size);
                    break;
                case COMTYP_DATTIM:
                    dataString = 
			(*env)->NewStringUTF(env, (char *) tmpBp->data);
                    (*env)->CallVoidMethod(env, 
			                   jBindList, 
					   BindList_addObjectWithSize,
                                           nameString, 
					   (jchar) tmpBp->dtype,
                                           dataString, 
					   (jint) tmpBp->size);
                    break;
                default:
                    break;
            }
        }
    }

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);
}

long jni_dbExecute(char *sqlStmt,
                   mocaDataRes **res,
                   mocaBindList *bindList,
                   int parseFlag)
{
    JNIEnv *env = jniGetEnv();

    long status = eOK;

    char *bindSqlStmt = NULL;
    char *convSqlStmt = NULL;

    mocaBindList *autoBindList = NULL;

    mocaDataRes *tmpRes = NULL;

    RETURN_STRUCT *ret = NULL;

    jstring jsql = NULL;

    jobject jRes      = NULL;
    jobject jBindList    = NULL;
    jobject mocaServerAdapter = NULL;

    /* Initialize the result set. */
    if (res)
        *res = NULL;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return status;

    /* Get the current MocaServerAdapter object. */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter)
        return eJNI_SERVER_ADAPTER;

    /* Add the user's bind list, if one was passed, to the local bind list. */
    if (bindList)
    {
        /* Create a BindList object. */
        jBindList = (*env)->NewObject(env,
                                      BindListClass,
                                      BindList_constructor);
        if ((*env)->ExceptionCheck(env))
        {
            status = sHandleException(env, "Could not create bind list");
	    goto cleanup;
        }

        /* Add the user's bind list to the local bind list. */
        sAddToBindList(env, jBindList, bindList);
    }

    /* Execute the given SQL */
    jsql = jniNewStringFromBytes(env, sqlStmt);

    jRes = (*env)->CallObjectMethod(env,
                                    mocaServerAdapter,
                                    MocaServerAdapter_executeSQL,
                                    jsql,
                                    jBindList,
                                    (jboolean) parseFlag,
				    (res == NULL));

    (*env)->DeleteLocalRef(env, jBindList);

    /*
     * Get the status and result set.
     * This also does exception handling for us, so we don't need
     * to explicitly check for exceptions being raised above.
     */
    status = jni_ExtractNativeReturnStruct(env, jRes, &ret, &jBindList);

    if (status != eOK && status != eDB_NO_ROWS_AFFECTED) 
    {
	char *temp;
	temp = ret ? ret->Error.DefaultText: NULL;

	gErrorCode = status;

	if (temp)
	{
	    memset(gMessage, 0, sizeof gMessage);
	    strncpy(gMessage, temp, sizeof(gMessage) - 1);
	}
	else
	{
	    gMessage[0] = '\0';
	}
    }

    if (res)	
    {
        *res = srvGetResults(ret);
        ret->ReturnedData = NULL;
    }

    srvFreeMemory(SRVRET_STRUCT, ret);

    (*env)->DeleteLocalRef(env, jsql);

    /* Now check for output parameters. */
    if (status == eOK && bindList && jBindList)
    {
        mocaBindList *tmpBp;

        for (tmpBp = bindList; tmpBp; tmpBp= tmpBp->next)
        {
            if (tmpBp->dtype == COMTYP_LONGPTR ||
                tmpBp->dtype == COMTYP_FLOATPTR ||
                tmpBp->dtype == COMTYP_CHARPTR)
            {
                jstring nameString = (*env)->NewStringUTF(env, tmpBp->name);

                jobject value = (*env)->CallObjectMethod(env,
                                                         jBindList,
                                                         BindList_getValue,
                                                         nameString);
                if (value == NULL)
                {
                    tmpBp->nullind = 1;
                }
                else if (tmpBp->dtype == COMTYP_CHARPTR)
                {
                    char *utfString = jniDecodeString(env, value, NULL);
                    strncpy(tmpBp->data, (char *)utfString, tmpBp->size);
                    free(utfString);
                }
                else if (tmpBp->dtype == COMTYP_LONGPTR)
                {
                    *((long *) tmpBp->data) =
                        (*env)->CallIntMethod(env, value, Number_intValue);
                }
                else if (tmpBp->dtype == COMTYP_FLOATPTR)
                {
                    *((double *) tmpBp->data) =
                        (*env)->CallDoubleMethod(env, value, Number_doubleValue);
                }

                (*env)->DeleteLocalRef(env, nameString);
                (*env)->DeleteLocalRef(env, value);
            }
        }
    }

cleanup:

    if (jRes)      (*env)->DeleteLocalRef(env, jRes);
    if (jBindList) (*env)->DeleteLocalRef(env, jBindList);

    if (autoBindList) sqlFreeBindList(autoBindList);

    if (convSqlStmt) free(convSqlStmt);
    if (bindSqlStmt) free(bindSqlStmt);

    return status;
}

long jni_dbCommit(void)
{
    JNIEnv *env = jniGetEnv();

    long status;

    jobject mocaServerAdapter;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return status;

    /* Get the current MocaServerAdapter object. */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter)
        return eJNI_SERVER_ADAPTER;

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           mocaServerAdapter, 
			   MocaServerAdapter_commitDB);
    if ((*env)->ExceptionCheck(env))
    {
        status = sHandleException(env, "Could not commit");
	goto cleanup;
    }

cleanup:
    
    return status;
}

long jni_dbRollback(void)
{
    JNIEnv *env = jniGetEnv();

    long status;

    jobject mocaServerAdapter;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return status;

    /* Get the current MocaServerAdapter object. */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter)
        return eJNI_SERVER_ADAPTER;

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           mocaServerAdapter, 
			   MocaServerAdapter_rollbackDB);
    if ((*env)->ExceptionCheck(env))
    {
        status = sHandleException(env, "Could not rollback");
	goto cleanup;
    }

cleanup:

    return status;
}

long jni_dbRollbackToSavepoint(char *savepoint)
{
    JNIEnv *env = jniGetEnv();

    long status;

    jstring jSavepoint;

    jobject mocaServerAdapter;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return status;

    /* Get the current MocaServerAdapter object. */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter)
        return eJNI_SERVER_ADAPTER;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jSavepoint = (*env)->NewStringUTF(env, savepoint);

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           mocaServerAdapter, 
			   MocaServerAdapter_rollbackDBToSavepoint,
                           jSavepoint);
    if ((*env)->ExceptionCheck(env))
    {
        status = sHandleException(env, "Could not rollback to savepoint");
        goto cleanup;
    }

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return status;
}

long jni_dbSetSavepoint(char *savepoint)
{
    JNIEnv *env = jniGetEnv();

    long status;

    jstring jSavepoint;

    jobject mocaServerAdapter;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return status;

    /* Get the current MocaServerAdapter object. */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter)
        return eJNI_SERVER_ADAPTER;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jSavepoint = (*env)->NewStringUTF(env, savepoint);

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           mocaServerAdapter, 
			   MocaServerAdapter_setSavepoint,
                           jSavepoint);
    if ((*env)->ExceptionCheck(env))
    {
        status = sHandleException(env, "Could not set savepoint");
        goto cleanup;
    }

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return status;
}

char *jni_dbGetNextVal(char *name)
{
    JNIEnv *env = jniGetEnv();

    static char buffer[1024];

    long status;

    char *value = NULL;

    jstring jName,
            jValue;

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

    /* Get a Java string from a UTF-8 string. */
    jName = (*env)->NewStringUTF(env, name);

    /* Call the Java method. */
    jValue = (jstring) (*env)->CallObjectMethod(env, 
		                      mocaServerAdapter, 
			              MocaServerAdapter_getNextSequenceValue,
                                      jName);
    if ((*env)->ExceptionCheck(env))
    {
        sHandleException(env, "Could not get next sequence value");
        goto cleanup;
    }

    /* Convert the UTF-16 string into a UTF-8 string. */
    value = jniDecodeString(env, jValue, NULL);

    /* Make a copy of the value for the caller. */
    strncpy(buffer, (char *) value, (sizeof buffer) - 1);
    free(value);
    value = buffer;

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return value;
}

long jni_dbPing(void)
{
    JNIEnv *env = jniGetEnv();

    long status;

    jobject mocaServerAdapter;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return status;

    /* Get the current MocaServerAdapter object. */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter)
        return eJNI_SERVER_ADAPTER;

    return eOK;
}

long jni_dbInfo(int *dbtype)
{
    JNIEnv *env = jniGetEnv();

    long status;

    jobject mocaServerAdapter;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return status;

    /* Get the current MocaServerAdapter object. */
    mocaServerAdapter = jniGetServerAdapter( );
    if (!mocaServerAdapter)
        return eJNI_SERVER_ADAPTER;

    /* Call the Java method. */
    *dbtype = (long) (*env)->CallIntMethod(env,
                                           mocaServerAdapter,
                                           MocaServerAdapter_getDBType);
    if ((*env)->ExceptionCheck(env))
    {
        status = sHandleException(env, "Could not get db type");
	goto cleanup;
    }

cleanup:

    return status;
}

long jni_dbErrorNumber(void)
{
    return gErrorCode;
}

char *jni_dbErrorText(void)
{
    return gMessage;
}
