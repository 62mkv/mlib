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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <jnilib.h>
#include <mislib.h>
#include <sqllib.h>
#include <srvlib.h>

#include "jniprivate.h"
#include "jnidefs.h"

/*
 * Find out the likely MOCA data type of the given object.
 */

static int sGetMocaType(JNIEnv *env, jobject obj)
{
    if ((*env)->IsInstanceOf(env, obj, StringClass))
        return COMTYP_CHAR;
    else if ((*env)->IsInstanceOf(env, obj, IntegerClass))
        return COMTYP_LONG;
    else if ((*env)->IsInstanceOf(env, obj, DoubleClass))
        return COMTYP_FLOAT;
    else if ((*env)->IsInstanceOf(env, obj, BooleanClass))
        return COMTYP_BOOLEAN;
    else
        return -1;
}

static void sExtractExceptionInformation(JNIEnv *env, RETURN_STRUCT **ret, jobject exc, long status, jmethodID getResultsMethod, jmethodID getMessageMethod, jmethodID getArgsListMethod, jmethodID isResolvedMethod)
{
    int size;
    int i;
    jstring jmessage;
    const char *utfmsg;
    jobjectArray argsArray;

    /*
     * Only call the other exception methods if a result object 
     * is needed.
     */
    if (ret)
    {
        jobject result;

        *ret = NULL;

        /*
         * It's possible for an exception to include results.
         * This generally only makes sense for errors like
         * NO_ROWS_AFFECTED, but by existing practice, can
         * occur anywhere.
         */
        result = (*env)->CallObjectMethod(env, exc, getResultsMethod);

        if (result)
        {
            /*
             * If a result was returned, it should be an instance of
             * WrappedResults.  If not, it's an error.
             */
            if (!(*env)->IsInstanceOf(env, result, WrappedResultsClass))
            {
                misLogError("Received invalid results object in exception");
            }
            else
            {
                mocaDataRes *res = (mocaDataRes *)(*env)->GetIntField(env,
                        result, WrappedResults_internalRes);
                (*env)->SetIntField(env, result, WrappedResults_internalRes, 0);
                *ret = srvAddSQLResults(res, status);
                (*env)->DeleteLocalRef(env, result);
            }
        }

        /* If the results are null, create a new results object */
        if (*ret == NULL)
        {
            *ret = srvResults(status, NULL);
        }

        /* Check for an error message */
        jmessage = (*env)->CallObjectMethod(env, exc, getMessageMethod);

        if (jmessage)
        {
            jboolean isResolved = JNI_FALSE;

            /*
             * Convert the Unicode message to UTF-8 and copy it into
             * a known spot.
             */
            utfmsg = (*env)->GetStringUTFChars(env, jmessage, NULL);

            isResolved = (*env)->CallBooleanMethod(env, exc, isResolvedMethod);

            if (isResolved)
            {
                (*ret)->ReturnedData->Message = malloc(strlen(utfmsg) + 1);
                strcpy((*ret)->ReturnedData->Message, utfmsg);
            }
            else 
            {
                srvErrorResultsAdd(*ret, status, (char *)utfmsg, NULL);
            }

            (*env)->ReleaseStringUTFChars(env, jmessage, utfmsg);

            (*env)->DeleteLocalRef(env, jmessage);
        }

        /*
         * Go through the exception arguments and add them into
         * the error results.
         */
        argsArray = (jobjectArray) (*env)->CallObjectMethod(env, exc, getArgsListMethod);
        size = (*env)->GetArrayLength(env, argsArray);

        for (i = 0; i < size; i++)
        {
            jobject arg;
            jstring name;
            jobject value;
            jboolean lookup;
            const char *utfName;
            char *utfValue;
            long longValue;
            double doubleValue;
            int datatype;

            arg = (*env)->GetObjectArrayElement(env, argsArray, i);
            name = (*env)->CallObjectMethod(env, arg, MocaExceptionArgs_getName);
            value = (*env)->CallObjectMethod(env, arg, MocaExceptionArgs_getValue);
            lookup = (*env)->CallBooleanMethod(env, arg, MocaExceptionArgs_isLookup);

            utfName = (*env)->GetStringUTFChars(env, name, NULL);
            datatype = sGetMocaType(env, value);

            switch(datatype)
            {
            case COMTYP_CHAR:
            case COMTYP_CHARPTR:
                if (value) 
                {
                    utfValue = jniDecodeString(env, value, NULL);
                     
                    srvErrorResultsAddArg(*ret,
                            (char *)utfName, datatype,
                            utfValue, lookup);
                    free(utfValue);
                }
                else
                {
                    srvErrorResultsAddArg(*ret,
                            (char *)utfName, datatype,
                            NULL, lookup);
                }
                break;

            case COMTYP_INT:
            case COMTYP_LONG:
            case COMTYP_LONGPTR:
                if (value)
                    longValue = jni_IntValue(env, value);
                else
                    longValue = 0L;
                srvErrorResultsAddArg(*ret, (char *)utfName,
                        datatype, longValue, lookup);
                break;

            case COMTYP_FLOAT:
            case COMTYP_FLOATPTR:
                if (value)
                    doubleValue = jni_DoubleValue(env, value);
                else
                    doubleValue = 0.0;
                srvErrorResultsAddArg(*ret, (char *)utfName,
                        datatype, doubleValue, lookup);
                break;
            }
            (*env)->ReleaseStringUTFChars(env, name, utfName);
        }
    }
}

/*
 * Check for any exceptions.  If a MocaException occurred, get the
 * error code and message from it.  If the exception was something else,
 * or if the Exception didn't have a specific error code, return a generic
 * error code, and do a toString() on the exception object.
 */

static long sExtractException(JNIEnv *env, RETURN_STRUCT **ret)
{
    jthrowable exc;
    long status = eOK;
    
    exc = (*env)->ExceptionOccurred(env);

    /*
     * If an exception occurred, find out what kind.
     */
    if (exc)
    {
        char *message = NULL;

        /* 
	 * Print out the exception before we translate so we can get the
	 * exact stack trace.  This will print to standard error on the native
	 * process.
	 */
        (*env)->ExceptionDescribe(env);
        /*
         * We're handling the exception, so clear it from our current JVM
         * thread context.  This helps with other calls that might fail
         * if the thread is in an exception condition. (e.g. toString or
         * getMessage)
         */
        (*env)->ExceptionClear(env);

        /*
         * If it's a MocaException, we can expect certain things, including
         * an error code and a default error message.
         */
        if ((*env)->IsInstanceOf(env, exc, MocaExceptionClass) ||
            (*env)->IsInstanceOf(env, exc, MocaRuntimeExceptionClass))
        {
            jmethodID getErrorCodeMethod, 
		      getMessageMethod, 
		      getArgsListMethod, 
		      getResultsMethod,
                      isResolvedMethod;

            if ((*env)->IsInstanceOf(env, exc, MocaExceptionClass))
            {
                getErrorCodeMethod = MocaException_getErrorCode;
                getMessageMethod   = MocaException_getMessage;
                getArgsListMethod  = MocaException_getArgList;
                getResultsMethod   = MocaException_getResults;
                isResolvedMethod   = MocaException_isResolved;
            }
            else
            {
                getErrorCodeMethod = MocaRuntimeException_getErrorCode;
                getMessageMethod   = MocaRuntimeException_getMessage;
                getArgsListMethod  = MocaRuntimeException_getArgList;
                getResultsMethod   = MocaRuntimeException_getResults;
                isResolvedMethod   = MocaRuntimeException_isResolved;
            }

            status = (*env)->CallIntMethod(env, exc, getErrorCodeMethod);

            sExtractExceptionInformation(env, ret, exc, status, getResultsMethod, getMessageMethod, getArgsListMethod, isResolvedMethod);
        }
	else if ((*env)->IsInstanceOf(env, exc, SQLExceptionClass))
        {
            jstring jmessage;
            const char *utfmsg;

            /* Get the error code for this exception. */
            status = (*env)->CallIntMethod(env, exc, SQLException_getErrorCode);
	    if (status == 0)
	        status = eDB_JDBC_EXCEPTION;
	    else if (status > 0)
	        status = -status;

            *ret = srvResults(status, NULL);

            /* Get the message for this exception. */
            jmessage = (*env)->CallObjectMethod(env, exc, SQLException_getMessage);
            if (jmessage)
            {
                    /*
                     * Convert the Unicode message to UTF-8 and copy it into
                     * a known spot.
                     */
                utfmsg = (*env)->GetStringUTFChars(env, jmessage, NULL);

                srvErrorResultsAdd(*ret, status, (char *)utfmsg, NULL);

                (*env)->ReleaseStringUTFChars(env, jmessage, utfmsg);

                (*env)->DeleteLocalRef(env, jmessage);
            }
	}
        else
        {
            /*
             * Something else happened.  Let's get the exception as a
             * string and just call this a generic DB error.
             */
            status = eSRV_UNEXPECTED_ERROR;

            message = jniToString(env, exc);
            misLogError("Unexpected Java Error: %s", message);

            if (ret)
                *ret = srvErrorResults(status, message, NULL);

            if (message) free(message);
        }

        (*env)->DeleteLocalRef(env, exc);
    }

    return status;
}


long jni_ExtractNativeReturnStruct(JNIEnv *env, jobject jRetStruct, 
     RETURN_STRUCT **ret, jobject *outBindList)
{
    long status;
    jobject jRes;

    if (outBindList)
    {
	if (jRetStruct)
	{
	    *outBindList = (*env)->CallObjectMethod(
	            env, jRetStruct, NativeReturnStruct_getBindList);
	}
	else 
	{
	    *outBindList = NULL;
	}
    }

    /* 
     * Check for exceptions. 
     *
     * If one did occur this will get the status from it, extract
     * the RETURN_STRUCT from the exception, and populate the given
     * RETURN_STRUCT for us.
     *
     * We can then just return to the caller.
     */
    status = sExtractException(env, ret);
    if (status != eOK)
        return status;

    if (!jRetStruct)
        return eOK;

    status = (*env)->CallIntMethod(env, jRetStruct, NativeReturnStruct_getErrorCode);

    /* If we have an okay status then just extract the result set */
    if (status == eOK)
    {
        jRes = (*env)->CallObjectMethod(env, jRetStruct, NativeReturnStruct_getResults);
        return jni_ExtractReturnStruct(env, jRes, ret);
    }
    else 
    {
        sExtractExceptionInformation(env, ret, jRetStruct, status, NativeReturnStruct_getResults, NativeReturnStruct_getMessage, NativeReturnStruct_getArgs, NativeReturnStruct_isResolved);
    }

    return status;
}

/*
 * Get a C RETURN_STRUCT from a Java WrappedResults.
 */

long jni_ExtractReturnStruct(JNIEnv *env, jobject jRes, RETURN_STRUCT **ret)
{
    long status;

    /* 
     * Check for exceptions. 
     *
     * If one did occur this will get the status from it, extract
     * the RETURN_STRUCT from the exception, and populate the given
     * RETURN_STRUCT for us.
     *
     * We can then just return to the caller.
     */
    status = sExtractException(env, ret);
    if (status != eOK)
	    return status;

    /*
     * If we get to here an exception did not occur, but that does not
     * imply that we have a Java result set.  
     */
    if (jRes)
    {
        mocaDataRes *res = 
	    (mocaDataRes *)(*env)->GetIntField(env, 
		                               jRes, 
					       WrappedResults_internalRes);
        (*env)->SetIntField(env, jRes, WrappedResults_internalRes, 0);
    
	/* It's possible that we weren't given a RETURN STRUCT. */
	if (ret)
            *ret = srvAddSQLResults(res, eOK);
    }
    else
    {
        if (ret)
            *ret = srvResults(eOK, NULL);
    }

    return status;
}

/*
 * Get a C mocaDataRes from a Java WrappedResults 
 */

long jni_ExtractResults(JNIEnv *env, jobject jRes, mocaDataRes **res)
{
    long status;

    RETURN_STRUCT *ret = NULL;

    /* Get a RETURN_STRUCT associated with this WrappedResults object. */
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
    }

    srvFreeMemory(SRVRET_STRUCT, ret);

    return status;
}
