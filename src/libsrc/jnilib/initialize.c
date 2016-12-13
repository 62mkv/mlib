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

#define MAIN

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <jni.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <jnilib.h>

#include "jniprivate.h"
#include "jnidefs.h"
#include "jninames.h"

static jclass sFindClass(JNIEnv *env, const char *name)
{
    jclass clazz     = NULL,
           tempClazz = NULL;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Find this class. */
    tempClazz = (*env)->FindClass(env, name);
    if (tempClazz == NULL)
    {
        fprintf(stderr, "Could not find class \"%s\"\n", name);
        goto cleanup;
    }

    /* Create a global reference of this class. */
    clazz = (*env)->NewGlobalRef(env, tempClazz);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return clazz;
}

static jmethodID sGetMethodID(JNIEnv *env, 
                              jclass clazz, 
                              const char *name, 
                              const char *signature)
{
    jmethodID methodID = NULL;

    /* Find this method. */
    methodID = (*env)->GetMethodID(env, clazz, name, signature);
    if (methodID == NULL)
    {
        fprintf(stderr, "%s%s\n", name, signature);
        fprintf(stderr, "Could not get method id\n");
    }

    return methodID;
}

static jmethodID sGetStaticMethodID(JNIEnv *env, 
                                    jclass clazz, 
                                    const char *name, 
                                    const char *signature)
{
    jmethodID methodID = NULL;

    /* Find this method. */
    methodID = (*env)->GetStaticMethodID(env, clazz, name, signature);
    if (methodID == NULL)
    {
        fprintf(stderr, "%s%s\n", name, signature);
        fprintf(stderr, "Could not get static method id\n");
    }

    return methodID;
}

static jfieldID sGetFieldID(JNIEnv *env, 
                            jclass clazz, 
                            const char *name, 
                            const char *dtype)
{
    jfieldID fieldID = NULL;

    /* Find this field. */
    fieldID = (*env)->GetFieldID(env, clazz, name, dtype);
    if (fieldID == NULL)
    {
        fprintf(stderr, "%s (%s)\n", name, dtype);
        fprintf(stderr, "Could not get field id\n");
    }

    return fieldID;
}

/*
 * java.lang.Boolean
 */

static long sInitialize_Boolean(JNIEnv *env)
{
    /* Get the Boolean class. */
    BooleanClass = sFindClass(env, BooleanClassName);
    if (BooleanClass == NULL)
        return eJNI_FIND_CLASS;

    /* Boolean.constructor */
    Boolean_constructor = 
	    sGetMethodID(env,
		         BooleanClass, 
		         Boolean_constructorName, 
		         Boolean_constructorSig);
    if (Boolean_constructor == NULL)
	return eJNI_FIND_METHOD;

    /* Boolean.booleanValue */
    Boolean_booleanValue = 
	    sGetMethodID(env,
		         BooleanClass, 
		         Boolean_booleanValueName, 
		         Boolean_booleanValueSig);
    if (Boolean_booleanValue == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * java.lang.Double
 */

static long sInitialize_Double(JNIEnv *env)
{
    /* Get the Double class. */
    DoubleClass = sFindClass(env, DoubleClassName);
    if (DoubleClass == NULL)
        return eJNI_FIND_CLASS;

    /* Double.constructor */
    Double_constructor = 
	    sGetMethodID(env,
		         DoubleClass, 
		         Double_constructorName, 
		         Double_constructorSig);
    if (Double_constructor == NULL)
	return eJNI_FIND_METHOD;

    /* Double.booleanValue */
    Double_doubleValue = 
	    sGetMethodID(env,
		         DoubleClass, 
		         Double_doubleValueName, 
		         Double_doubleValueSig);
    if (Double_doubleValue == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * java.lang.Integer
 */

static long sInitialize_Integer(JNIEnv *env)
{
    /* Get the Integer class. */
    IntegerClass = sFindClass(env, IntegerClassName);
    if (IntegerClass == NULL)
        return eJNI_FIND_CLASS;

    /* Integer.constructor */
    Integer_constructor = 
	    sGetMethodID(env,
		         IntegerClass, 
		         Integer_constructorName, 
		         Integer_constructorSig);
    if (Integer_constructor == NULL)
	return eJNI_FIND_METHOD;

    /* Integer.intValue */
    Integer_intValue = 
	    sGetMethodID(env,
		         IntegerClass, 
		         Integer_intValueName, 
		         Integer_intValueSig);
    if (Integer_intValue == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * java.lang.Long
 */

static long sInitialize_Long(JNIEnv *env)
{
    /* Get the Long class. */
    LongClass = sFindClass(env, LongClassName);
    if (LongClass == NULL)
        return eJNI_FIND_CLASS;

    /* Long.constructor */
    Long_constructor = 
	    sGetMethodID(env,
		         LongClass, 
		         Long_constructorName, 
		         Long_constructorSig);
    if (Long_constructor == NULL)
	return eJNI_FIND_METHOD;

    /* Long.longValue */
    Long_longValue = 
	    sGetMethodID(env,
		         LongClass, 
		         Long_longValueName, 
		         Long_longValueSig);
    if (Long_longValue == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * java.lang.Number
 */

static long sInitialize_Number(JNIEnv *env)
{
    /* Get the Number class. */
    NumberClass = sFindClass(env, NumberClassName);
    if (NumberClass == NULL)
        return eJNI_FIND_CLASS;

    /* Number.intValue */
    Number_intValue = 
	    sGetMethodID(env,
		         NumberClass, 
		         Number_intValueName, 
		         Number_intValueSig);
    if (Number_intValue == NULL)
	return eJNI_FIND_METHOD;

    /* Number.doubleValue */
    Number_doubleValue = 
	    sGetMethodID(env,
		         NumberClass, 
		         Number_doubleValueName, 
		         Number_doubleValueSig);
    if (Number_doubleValue == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * java.lang.Object
 */

static long sInitialize_Object(JNIEnv *env)
{
    /* Get the Object class. */
    ObjectClass = sFindClass(env, ObjectClassName);
    if (ObjectClass == NULL)
        return eJNI_FIND_CLASS;

    /* Object.toString */
    Object_toString = 
	    sGetMethodID(env,
		         ObjectClass, 
		         Object_toStringName, 
		         Object_toStringSig);
    if (Object_toString == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * java.lang.String
 */

static long sInitialize_String(JNIEnv *env)
{
    /* Get the String class. */
    StringClass = sFindClass(env, StringClassName);
    if (StringClass == NULL)
        return eJNI_FIND_CLASS;

    /* String.constructor */
    String_constructor = 
	    sGetMethodID(env,
		         StringClass, 
		         String_constructorName, 
		         String_constructorSig);
    if (String_constructor == NULL)
	return eJNI_FIND_METHOD;

    /* String.getBytes */
    String_getBytes = 
	    sGetMethodID(env,
		         StringClass, 
		         String_getBytesName, 
		         String_getBytesSig);
    if (String_getBytes == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * java.lang.Exception
 */

static long sInitialize_Exception(JNIEnv *env)
{
    /* Get the Exception class. */
    ExceptionClass = sFindClass(env, ExceptionClassName);
    if (ExceptionClass == NULL)
        return eJNI_FIND_CLASS;

    /* Exception.getMessage */
    Exception_getMessage = 
	    sGetMethodID(env,
		         ExceptionClass, 
		         Exception_getMessageName, 
		         Exception_getMessageSig);
    if (Exception_getMessage == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * java.lang.RuntimeException
 */

static long sInitialize_RuntimeException(JNIEnv *env)
{
    /* Get the RuntimeException class. */
    RuntimeExceptionClass = sFindClass(env, RuntimeExceptionClassName);
    if (RuntimeExceptionClass == NULL)
        return eJNI_FIND_CLASS;

    /* RuntimeException.getMessage */
    RuntimeException_getMessage = 
	    sGetMethodID(env,
		         RuntimeExceptionClass, 
		         RuntimeException_getMessageName, 
		         RuntimeException_getMessageSig);
    if (RuntimeException_getMessage == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * java.util.Map
 */

static long sInitialize_Map(JNIEnv *env)
{
    /* Get the Map class. */
    MapClass = sFindClass(env, MapClassName);
    if (MapClass == NULL)
        return eJNI_FIND_CLASS;

    /* Map.put */
    Map_put =
	    sGetMethodID(env,
		         MapClass, 
		         Map_putName, 
		         Map_putSig);

    if (Map_put == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * java.util.HashMap
 */

static long sInitialize_HashMap(JNIEnv *env)
{
    /* Get the HashMap class. */
    HashMapClass = sFindClass(env, HashMapClassName);
    if (HashMapClass == NULL)
        return eJNI_FIND_CLASS;

    /* HashMap.constructor */
    HashMap_constructor =
	    sGetMethodID(env,
		         HashMapClass, 
		         HashMap_constructorName, 
		         HashMap_constructorSig);

    if (HashMap_constructor == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * java.sql.SQLException
 */

static long sInitialize_SQLException(JNIEnv *env)
{
    /* Get the SQLException class. */
    SQLExceptionClass = sFindClass(env, SQLExceptionClassName);
    if (SQLExceptionClass == NULL)
        return eJNI_FIND_CLASS;

    /* SQLException.getErrorCode */
    SQLException_getErrorCode = 
	    sGetMethodID(env,
		         SQLExceptionClass, 
		         SQLException_getErrorCodeName, 
		         SQLException_getErrorCodeSig);
    if (SQLException_getErrorCode == NULL)
	return eJNI_FIND_METHOD;

    /* SQLException.getMessage */
    SQLException_getMessage = 
	    sGetMethodID(env,
		         SQLExceptionClass, 
		         SQLException_getMessageName, 
		         SQLException_getMessageSig);
    if (SQLException_getMessage == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * com.redprairie.moca.server.legacy.MocaSession
 */

static long sInitialize_MocaSession(JNIEnv *env)
{
    /* Get the MocaSession class. */
    MocaSessionClass = sFindClass(env, MocaSessionClassName);
    if (MocaSessionClass == NULL)
        return eJNI_FIND_CLASS;

    /* MocaSession.newSessionKey */
    MocaSession_newSessionKey = 
	    sGetStaticMethodID(env,
		               MocaSessionClass, 
		               MocaSession_newSessionKeyName, 
		               MocaSession_newSessionKeySig);
    if (MocaSession_newSessionKey == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * com.redprairie.moca.server.legacy.MocaClientAdapter
 */

static long sInitialize_MocaClientAdapter(JNIEnv *env)
{
    /* Get the MocaClientAdapter class. */
    MocaClientAdapterClass = sFindClass(env, MocaClientAdapterClassName);
    if (MocaClientAdapterClass == NULL)
        return eJNI_FIND_CLASS;

    /* MocaClientAdapter.constructor */
    MocaClientAdapter_constructor = 
	    sGetMethodID(env,
		         MocaClientAdapterClass, 
		         MocaClientAdapter_constructorName, 
		         MocaClientAdapter_constructorSig);
    if (MocaClientAdapter_constructor == NULL)
	return eJNI_FIND_METHOD;

    /* MocaClientAdapter.close */
    MocaClientAdapter_close = 
	    sGetMethodID(env,
		         MocaClientAdapterClass, 
		         MocaClientAdapter_closeName, 
		         MocaClientAdapter_closeSig);
    if (MocaClientAdapter_close == NULL)
	return eJNI_FIND_METHOD;

    /* MocaClientAdapter.executeCommand */
    MocaClientAdapter_executeCommand = 
	    sGetMethodID(env,
		         MocaClientAdapterClass, 
		         MocaClientAdapter_executeCommandName, 
		         MocaClientAdapter_executeCommandSig);
    if (MocaClientAdapter_executeCommand == NULL)
	return eJNI_FIND_METHOD;

    /* MocaClientAdapter.login */
    MocaClientAdapter_login = 
	    sGetMethodID(env,
		         MocaClientAdapterClass, 
		         MocaClientAdapter_loginName, 
		         MocaClientAdapter_loginSig);
    if (MocaClientAdapter_login == NULL)
        return eJNI_FIND_METHOD;

    /* MocaClientAdapter.logout */
    MocaClientAdapter_logout = 
	    sGetMethodID(env,
		         MocaClientAdapterClass, 
		         MocaClientAdapter_logoutName, 
		         MocaClientAdapter_logoutSig);
    if (MocaClientAdapter_logout == NULL)
        return eJNI_FIND_METHOD;

    /* MocaClientAdapter.setAutoCommit */
    MocaClientAdapter_setAutoCommit = 
	    sGetMethodID(env,
		         MocaClientAdapterClass, 
		         MocaClientAdapter_setAutoCommitName, 
		         MocaClientAdapter_setAutoCommitSig);
    if (MocaClientAdapter_setAutoCommit == NULL)
	return eJNI_FIND_METHOD;

    /* MocaClientAdapter.setApplicationId */
    MocaClientAdapter_setApplicationId = 
	    sGetMethodID(env,
		         MocaClientAdapterClass, 
		         MocaClientAdapter_setApplicationIdName, 
		         MocaClientAdapter_setApplicationIdSig);
    if (MocaClientAdapter_setApplicationId == NULL)
	return eJNI_FIND_METHOD;

    /* MocaClientAdapter.setEnvironment */
    MocaClientAdapter_setEnvironment = 
	    sGetMethodID(env,
		         MocaClientAdapterClass, 
		         MocaClientAdapter_setEnvironmentName, 
		         MocaClientAdapter_setEnvironmentSig);
    if (MocaClientAdapter_setEnvironment == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * com.redprairie.moca.server.legacy.MocaServerAdapter
 */

static long sInitialize_MocaServerAdapter(JNIEnv *env)
{
    /* Get the MocaServerAdapter class. */
    MocaServerAdapterClass = sFindClass(env, MocaServerAdapterClassName);
    if (MocaServerAdapterClass == NULL)
        return eJNI_FIND_CLASS;

    /* MocaServerAdapter.getEnvironment */
    MocaServerAdapter_getEnvironment = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_getEnvironmentName, 
		         MocaServerAdapter_getEnvironmentSig);
    if (MocaServerAdapter_getEnvironment == NULL)
	return eJNI_FIND_METHOD;

    /* MocaServerAdapter.putEnvironment */
    MocaServerAdapter_putEnvironment = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_putEnvironmentName, 
		         MocaServerAdapter_putEnvironmentSig);
    if (MocaServerAdapter_putEnvironment == NULL)
	return eJNI_FIND_METHOD;

    /* MocaServerAdapter.removeEnvironment */
    MocaServerAdapter_removeEnvironment = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_removeEnvironmentName, 
		         MocaServerAdapter_removeEnvironmentSig);
    if (MocaServerAdapter_removeEnvironment == NULL)
	return eJNI_FIND_METHOD;

    /* MocaServerAdapter.commitTx */
    MocaServerAdapter_commitTx = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_commitTxName, 
		         MocaServerAdapter_commitTxSig);
    if (MocaServerAdapter_commitTx == NULL)
	return eJNI_FIND_METHOD;

    /* MocaServerAdapter.rollbackTx */
    MocaServerAdapter_rollbackTx = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_rollbackTxName, 
		         MocaServerAdapter_rollbackTxSig);
    if (MocaServerAdapter_rollbackTx == NULL)
	return eJNI_FIND_METHOD;

    /* MocaServerAdapter.commitDB */
    MocaServerAdapter_commitDB = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_commitDBName, 
		         MocaServerAdapter_commitDBSig);
    if (MocaServerAdapter_commitDB == NULL)
	return eJNI_FIND_METHOD;

    /* MocaServerAdapter.rollbackDB */
    MocaServerAdapter_rollbackDB = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_rollbackDBName, 
		         MocaServerAdapter_rollbackDBSig);
    if (MocaServerAdapter_rollbackDB == NULL)
	return eJNI_FIND_METHOD;

    /* MocaServerAdapter.rollbackDBToSavepoint */
    MocaServerAdapter_rollbackDBToSavepoint = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_rollbackDBToSavepointName, 
		         MocaServerAdapter_rollbackDBToSavepointSig);
    if (MocaServerAdapter_rollbackDBToSavepoint == NULL)
	return eJNI_FIND_METHOD;

    /* MocaServerAdapter.setSavepoint */
    MocaServerAdapter_setSavepoint = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_setSavepointName, 
		         MocaServerAdapter_setSavepointSig);
    if (MocaServerAdapter_setSavepoint == NULL)
	return eJNI_FIND_METHOD;

    /* MocaServerAdapter.executeCommand */
    MocaServerAdapter_executeCommand = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_executeCommandName, 
		         MocaServerAdapter_executeCommandSig);
    if (MocaServerAdapter_executeCommand == NULL)
	return eJNI_FIND_METHOD;

    /* MocaServerAdapter.executeSQL */
    MocaServerAdapter_executeSQL = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_executeSQLName, 
		         MocaServerAdapter_executeSQLSig);
    if (MocaServerAdapter_executeSQL == NULL)
	return eJNI_FIND_METHOD;

    /* MocaServerAdapter.getStackElement */
    MocaServerAdapter_getStackElement = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_getStackElementName, 
		         MocaServerAdapter_getStackElementSig);
    if (MocaServerAdapter_getStackElement == NULL)
	return eJNI_FIND_METHOD;

    /* MocaServerAdapter.getStackArgs */
    MocaServerAdapter_getStackArgs = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_getStackArgsName, 
		         MocaServerAdapter_getStackArgsSig);
    if (MocaServerAdapter_getStackArgs == NULL)
	return eJNI_FIND_METHOD;

    /* MocaServerAdapter.getDBType */
    MocaServerAdapter_getDBType = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_getDBTypeName, 
		         MocaServerAdapter_getDBTypeSig);
    if (MocaServerAdapter_getDBType == NULL)
	return eJNI_FIND_METHOD;

    /* MocaServerAdapter.getNextSequenceValue */
    MocaServerAdapter_getNextSequenceValue = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_getNextSequenceValueName, 
		         MocaServerAdapter_getNextSequenceValueSig);
    if (MocaServerAdapter_getNextSequenceValue == NULL)
	return eJNI_FIND_METHOD;
    
    /* MocaServerAdapter.trace */
    MocaServerAdapter_trace = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_traceName, 
		         MocaServerAdapter_traceSig);
    if (MocaServerAdapter_trace == NULL)
	return eJNI_FIND_METHOD;
	
     /* MocaServerAdapter.log */
    MocaServerAdapter_log = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_logName, 
		         MocaServerAdapter_logSig);
    if (MocaServerAdapter_log == NULL)
	return eJNI_FIND_METHOD;
    
    /* MocaServerAdapter.setTraceFileName */
    MocaServerAdapter_setTraceFileName = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_setTraceFileNameName, 
		         MocaServerAdapter_setTraceFileNameSig);
    if (MocaServerAdapter_setTraceFileName == NULL)
	return eJNI_FIND_METHOD;
    
    /* MocaServerAdapter.setTraceLevel */
    MocaServerAdapter_setTraceLevel = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_setTraceLevelName, 
		         MocaServerAdapter_setTraceLevelSig);
    if (MocaServerAdapter_setTraceLevel == NULL)
	return eJNI_FIND_METHOD;
    
    /* MocaServerAdapter.getTraceLevel */
    MocaServerAdapter_getTraceLevel = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_getTraceLevelName, 
		         MocaServerAdapter_getTraceLevelSig);
    if (MocaServerAdapter_getTraceLevel == NULL)
	return eJNI_FIND_METHOD;

    /* MocaServerAdapter.translateMessage */
    MocaServerAdapter_translateMessage = 
	    sGetMethodID(env,
		         MocaServerAdapterClass, 
		         MocaServerAdapter_translateMessageName, 
		         MocaServerAdapter_translateMessageSig);
    if (MocaServerAdapter_translateMessage == NULL)
	return eJNI_FIND_METHOD;
    return eOK;
}

/*
 * com.redprairie.moca.server.legacy.InProcessMocaServerAdapter
 */

static long sInitialize_InProcessMocaServerAdapter(JNIEnv *env)
{
    /* Get the InProcessMocaServerAdapter class. */
    InProcessMocaServerAdapterClass = 
            sFindClass(env, InProcessMocaServerAdapterClassName);
    if (InProcessMocaServerAdapterClass == NULL)
        return eJNI_FIND_CLASS;

    /* InProcessMocaServerAdapter.constructor */
    InProcessMocaServerAdapter_constructor = 
	    sGetMethodID(env,
		         InProcessMocaServerAdapterClass, 
		         InProcessMocaServerAdapter_constructorName, 
		         InProcessMocaServerAdapter_constructorSig);
    if (InProcessMocaServerAdapter_constructor == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * com.redprairie.moca.server.legacy.WrappedResults
 */

static long sInitialize_WrappedResults(JNIEnv *env)
{

    /* Get the WrappedResults class. */
    WrappedResultsClass = sFindClass(env, WrappedResultsClassName);
    if (WrappedResultsClass == NULL)
        return eJNI_FIND_CLASS;

    /* WrappedResults.constructor */
    WrappedResults_constructor = 
	    sGetMethodID(env,
		         WrappedResultsClass, 
		         WrappedResults_constructorName, 
		         WrappedResults_constructorSig);
    if (WrappedResults_constructor == NULL)
	return eJNI_FIND_METHOD;

    /* WrappedResults._interalRes */
    WrappedResults_internalRes = 
	    sGetFieldID(env,
		        WrappedResultsClass, 
		        WrappedResults_internalResName, 
		        WrappedResults_internalResType);
    if (WrappedResults_internalRes == NULL)
	return eJNI_FIND_FIELD;

    /* WrappedResults._interalRow */
    WrappedResults_internalRow = 
	    sGetFieldID(env,
		        WrappedResultsClass, 
		        WrappedResults_internalRowName, 
		        WrappedResults_internalRowType);
    if (WrappedResults_internalRow == NULL)
	return eJNI_FIND_FIELD;

    /* WrappedResults._nextRow */
    WrappedResults_nextRow = 
	    sGetFieldID(env,
		        WrappedResultsClass, 
		        WrappedResults_nextRowName, 
		        WrappedResults_nextRowType);
    if (WrappedResults_nextRow == NULL)
	return eJNI_FIND_FIELD;

    /* WrappedResults._editRow */
    WrappedResults_editRow = 
	    sGetFieldID(env,
		        WrappedResultsClass, 
		        WrappedResults_editRowName, 
		        WrappedResults_editRowType);
    if (WrappedResults_editRow == NULL)
	return eJNI_FIND_FIELD;

    /* WrappedResults._allocateNulls */
    WrappedResults_allocateNulls = 
	    sGetFieldID(env,
		        WrappedResultsClass, 
		        WrappedResults_allocateNullsName, 
		        WrappedResults_allocateNullsType);
    if (WrappedResults_allocateNulls == NULL)
	return eJNI_FIND_FIELD;

    return eOK;
}

/*
 * com.redprairie.moca.server.legacy.CommandInvocationException
 */

static long sInitialize_CommandInvocationException(JNIEnv *env)
{

    /* Get the CommandInvocationException class. */
    CommandInvocationExceptionClass = 
	    sFindClass(env, CommandInvocationExceptionClassName);
    if (CommandInvocationExceptionClass == NULL)
        return eJNI_FIND_CLASS;

    /* CommandInvocationException.constructor */
    CommandInvocationException_constructor = 
	    sGetMethodID(env,
		         CommandInvocationExceptionClass, 
		         CommandInvocationException_constructorName, 
		         CommandInvocationException_constructorSig);
    if (CommandInvocationException_constructor == NULL)
	return eJNI_FIND_METHOD;

    /* CommandInvocationException.addArg */
    CommandInvocationException_addArg = 
	    sGetMethodID(env,
		         CommandInvocationExceptionClass, 
		         CommandInvocationException_addArgName, 
		         CommandInvocationException_addArgSig);
    if (CommandInvocationException_addArg == NULL)
	return eJNI_FIND_METHOD;

    /* CommandInvocationException.addLookupArg */
    CommandInvocationException_addLookupArg = 
	    sGetMethodID(env,
		         CommandInvocationExceptionClass, 
		         CommandInvocationException_addLookupArgName, 
		         CommandInvocationException_addLookupArgSig);
    if (CommandInvocationException_addLookupArg == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * com.redprairie.moca.server.legacy.MocaNativeException
 */

static long sInitialize_MocaNativeException(JNIEnv *env)
{

    /* Get the MocaNativeException class. */
    MocaNativeExceptionClass = 
	    sFindClass(env, MocaNativeExceptionClassName);
    if (MocaNativeExceptionClass == NULL)
        return eJNI_FIND_CLASS;

    /* MocaNativeException.constructor */
    MocaNativeException_constructor = 
	    sGetMethodID(env,
		         MocaNativeExceptionClass, 
		         MocaNativeException_constructorName, 
		         MocaNativeException_constructorSig);
    if (MocaNativeException_constructor == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * com.redprairie.moca.MocaLibInfo
 */

static long sInitialize_MocaLibInfo(JNIEnv *env)
{
    /* Get the MocaLibInfo class. */
    MocaLibInfoClass = sFindClass(env, MocaLibInfoClassName);
    if (MocaLibInfoClass == NULL)
        return eJNI_FIND_CLASS;

    /* MocaLibInfo.constructor */
    MocaLibInfo_constructor = 
	    sGetMethodID(env,
		         MocaLibInfoClass, 
		         MocaLibInfo_constructorName, 
		         MocaLibInfo_constructorSig);
    if (MocaLibInfo_constructor == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * com.redprairie.moca.MocaException
 */

static long sInitialize_MocaException(JNIEnv *env)
{
    /* Get the MocaException class. */
    MocaExceptionClass = sFindClass(env, MocaExceptionClassName);
    if (MocaExceptionClass == NULL)
        return eJNI_FIND_CLASS;

    /* MocaException.getErrorCode */
    MocaException_getErrorCode =
	    sGetMethodID(env,
		         MocaExceptionClass, 
		         MocaException_getErrorCodeName, 
		         MocaException_getErrorCodeSig);
    if (MocaException_getErrorCode == NULL)
	return eJNI_FIND_METHOD;

    /* MocaException.getMessage */
    MocaException_getMessage =
	    sGetMethodID(env,
		         MocaExceptionClass, 
		         MocaException_getMessageName, 
		         MocaException_getMessageSig);
    if (MocaException_getMessage == NULL)
	return eJNI_FIND_METHOD;

    /* MocaException.getArgList */
    MocaException_getArgList =
	    sGetMethodID(env,
		         MocaExceptionClass, 
		         MocaException_getArgListName, 
		         MocaException_getArgListSig);
    if (MocaException_getArgList == NULL)
	return eJNI_FIND_METHOD;

    /* MocaException.getResults */
    MocaException_getResults =
	    sGetMethodID(env,
		         MocaExceptionClass, 
		         MocaException_getResultsName, 
		         MocaException_getResultsSig);
    if (MocaException_getResults == NULL)
	return eJNI_FIND_METHOD;

    /* MocaException.isReolved */
    MocaException_isResolved =
	    sGetMethodID(env,
		         MocaExceptionClass, 
		         MocaException_isResolvedName, 
		         MocaException_isResolvedSig);
    if (MocaException_isResolved == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * com.redprairie.moca.MocaRuntimeException
 */

static long sInitialize_MocaRuntimeException(JNIEnv *env)
{
    /* Get the MocaRuntimeException class. */
    MocaRuntimeExceptionClass = sFindClass(env, MocaRuntimeExceptionClassName);
    if (MocaRuntimeExceptionClass == NULL)
        return eJNI_FIND_CLASS;

    /* MocaRuntimeException.getErrorCode */
    MocaRuntimeException_getErrorCode =
	    sGetMethodID(env,
		         MocaRuntimeExceptionClass, 
		         MocaRuntimeException_getErrorCodeName, 
		         MocaRuntimeException_getErrorCodeSig);
    if (MocaRuntimeException_getErrorCode == NULL)
	return eJNI_FIND_METHOD;

    /* MocaRuntimeException.getMessage */
    MocaRuntimeException_getMessage =
	    sGetMethodID(env,
		         MocaRuntimeExceptionClass, 
		         MocaRuntimeException_getMessageName, 
		         MocaRuntimeException_getMessageSig);
    if (MocaRuntimeException_getMessage == NULL)
	return eJNI_FIND_METHOD;

    /* MocaRuntimeException.getArgList */
    MocaRuntimeException_getArgList =
	    sGetMethodID(env,
		         MocaRuntimeExceptionClass, 
		         MocaRuntimeException_getArgListName, 
		         MocaRuntimeException_getArgListSig);
    if (MocaRuntimeException_getArgList == NULL)
	return eJNI_FIND_METHOD;

    /* MocaRuntimeException.getResults */
    MocaRuntimeException_getResults =
	    sGetMethodID(env,
		         MocaRuntimeExceptionClass, 
		         MocaRuntimeException_getResultsName, 
		         MocaRuntimeException_getResultsSig);
    if (MocaRuntimeException_getResults == NULL)
	return eJNI_FIND_METHOD;

    /* MocaException.isReolved */
    MocaRuntimeException_isResolved =
	    sGetMethodID(env,
		         MocaRuntimeExceptionClass, 
		         MocaRuntimeException_isResolvedName, 
		         MocaRuntimeException_isResolvedSig);
    if (MocaRuntimeException_isResolved == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * com.redprairie.moca.MocaException$Args
 */

static long sInitialize_MocaExceptionArgs(JNIEnv *env)
{
    /* Get the MocaException$Args class. */
    MocaExceptionArgsClass = sFindClass(env, MocaExceptionArgsClassName);
    if (MocaExceptionArgsClass == NULL)
        return eJNI_FIND_CLASS;

    /* MocaException$Args.getName */
    MocaExceptionArgs_getName =
	    sGetMethodID(env,
		         MocaExceptionArgsClass, 
		         MocaExceptionArgs_getNameName, 
		         MocaExceptionArgs_getNameSig);
    if (MocaExceptionArgs_getName == NULL)
	return eJNI_FIND_METHOD;

    /* MocaException$Args.getValue */
    MocaExceptionArgs_getValue =
	    sGetMethodID(env,
		         MocaExceptionArgsClass, 
		         MocaExceptionArgs_getValueName, 
		         MocaExceptionArgs_getValueSig);
    if (MocaExceptionArgs_getValue == NULL)
	return eJNI_FIND_METHOD;

    /* MocaException$Args.isLookup */
    MocaExceptionArgs_isLookup =
	    sGetMethodID(env,
		         MocaExceptionArgsClass, 
		         MocaExceptionArgs_isLookupName, 
		         MocaExceptionArgs_isLookupSig);
    if (MocaExceptionArgs_isLookup == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * com.redprairie.moca.server.db.BindList
 */

static long sInitialize_BindList(JNIEnv *env)
{
    /* Get the BindList class. */
    BindListClass = sFindClass(env, BindListClassName);
    if (BindListClass == NULL)
        return eJNI_FIND_CLASS;

    /* BindList.constructor */
    BindList_constructor = 
	    sGetMethodID(env,
		         BindListClass, 
		         BindList_constructorName, 
		         BindList_constructorSig);
    if (BindList_constructor == NULL)
	return eJNI_FIND_METHOD;

    /* BindList.addInt */
    BindList_addInt = 
	    sGetMethodID(env,
		         BindListClass, 
		         BindList_addIntName, 
		         BindList_addIntSig);
    if (BindList_addInt == NULL)
	return eJNI_FIND_METHOD;

    /* BindList.addDouble */
    BindList_addDouble = 
	    sGetMethodID(env,
		         BindListClass, 
		         BindList_addDoubleName, 
		         BindList_addDoubleSig);
    if (BindList_addDouble == NULL)
	return eJNI_FIND_METHOD;

    /* BindList.addBoolean */
    BindList_addBoolean = 
	    sGetMethodID(env,
		         BindListClass, 
		         BindList_addBooleanName, 
		         BindList_addBooleanSig);
    if (BindList_addBoolean == NULL)
	return eJNI_FIND_METHOD;

    /* BindList.addObject */
    BindList_addObject = 
	    sGetMethodID(env,
		         BindListClass, 
		         BindList_addObjectName, 
		         BindList_addObjectSig);
    if (BindList_addObject == NULL)
	return eJNI_FIND_METHOD;

    /* BindList.addObjectWithSize */
    BindList_addObjectWithSize = 
	    sGetMethodID(env,
		         BindListClass, 
		         BindList_addObjectWithSizeName, 
		         BindList_addObjectWithSizeSig);
    if (BindList_addObjectWithSize == NULL)
	return eJNI_FIND_METHOD;

    /* BindList.getValue */
    BindList_getValue = 
	    sGetMethodID(env,
		         BindListClass, 
		         BindList_getValueName, 
		         BindList_getValueSig);
    if (BindList_getValue == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

/*
 * com.redprairie.moca.server.legacy.NativeReturnStruct
 */

static long sInitialize_NativeReturnStruct(JNIEnv *env)
{
    /* Get the NativeReturnStruct class. */
    NativeReturnStructClass = sFindClass(env, NativeReturnStructName);
    if (NativeReturnStructClass == NULL)
        return eJNI_FIND_CLASS;

    /* NativeReturnStruct.exceptionConstructor */
    NativeReturnStruct_exceptionConstructor = 
	    sGetMethodID(env,
		         NativeReturnStructClass, 
		         NativeReturnStruct_constructorName, 
		         NativeReturnStruct_exceptionConstructorSig);
    if (NativeReturnStruct_exceptionConstructor == NULL)
	return eJNI_FIND_METHOD;

    /* NativeReturnStruct.resultConstructor */
    NativeReturnStruct_resultsConstructor = 
	    sGetMethodID(env,
		         NativeReturnStructClass, 
		         NativeReturnStruct_constructorName, 
		         NativeReturnStruct_resultsConstructorSig);
    if (NativeReturnStruct_resultsConstructor == NULL)
	return eJNI_FIND_METHOD;

    /* NativeReturnStruct.getErrorCode */
    NativeReturnStruct_getErrorCode = 
	    sGetMethodID(env,
		         NativeReturnStructClass, 
		         NativeReturnStruct_getErrorCodeName, 
		         NativeReturnStruct_getErrorCodeSig);
    if (NativeReturnStruct_getErrorCode == NULL)
	return eJNI_FIND_METHOD;

    /* NativeReturnStruct.getResults */
    NativeReturnStruct_getResults = 
	    sGetMethodID(env,
		         NativeReturnStructClass, 
		         NativeReturnStruct_getResultsName, 
		         NativeReturnStruct_getResultsSig);
    if (NativeReturnStruct_getResults == NULL)
	return eJNI_FIND_METHOD;

    /* NativeReturnStruct.getMessage */
    NativeReturnStruct_getMessage = 
	    sGetMethodID(env,
		         NativeReturnStructClass, 
		         NativeReturnStruct_getMessageName, 
		         NativeReturnStruct_getMessageSig);
    if (NativeReturnStruct_getMessage == NULL)
	return eJNI_FIND_METHOD;

    /* NativeReturnStruct.getArgs */
    NativeReturnStruct_getArgs = 
	    sGetMethodID(env,
		         NativeReturnStructClass, 
		         NativeReturnStruct_getArgsName, 
		         NativeReturnStruct_getArgsSig);
    if (NativeReturnStruct_getArgs == NULL)
	return eJNI_FIND_METHOD;

    /* NativeReturnStruct.getArgs */
    NativeReturnStruct_getBindList = 
	    sGetMethodID(env,
		         NativeReturnStructClass, 
		         NativeReturnStruct_getBindListName, 
		         NativeReturnStruct_getBindListSig);
    if (NativeReturnStruct_getBindList == NULL)
	return eJNI_FIND_METHOD;

    /* NativeReturnStruct.isResolved */
    NativeReturnStruct_isResolved = 
	    sGetMethodID(env,
		         NativeReturnStructClass, 
		         NativeReturnStruct_isResolvedName, 
		         NativeReturnStruct_isResolvedSig);
    if (NativeReturnStruct_isResolved == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}


/*
 * com.redprairie.moca.server.legacy.GenericPointer
 */

static long sInitialize_Generic(JNIEnv *env)
{
    /* Get the GenericPointer class. */
    GenericPointerClass = sFindClass(env, GenericPointerClassName);
    if (GenericPointerClass == NULL)
        return eJNI_FIND_CLASS;

    /* GenericPointer.constructor */
    GenericPointer_constructor = 
	    sGetMethodID(env,
		         GenericPointerClass, 
		         GenericPointer_constructorName, 
		         GenericPointer_constructorSig);
    if (GenericPointer_constructor == NULL)
	return eJNI_FIND_METHOD;

    /* GenericPointer.32bitValue */
    GenericPointer_32bitValue = 
	    sGetMethodID(env,
		         GenericPointerClass, 
		         GenericPointer_32bitValueName, 
		         GenericPointer_32bitValueSig);
    if (GenericPointer_32bitValue == NULL)
	return eJNI_FIND_METHOD;

    return eOK;
}

static void sInitialize_CharacterSet(JNIEnv *env)
{
    char *charset;
    jstring jCharset;

    charset = osGetVar(ENV_JAVA_CHARSET);
    if (charset)
        jCharset = (*env)->NewStringUTF(env, charset);
    else
        jCharset = (*env)->NewStringUTF(env, "UTF-8");

    gCharset = (*env)->NewGlobalRef(env, jCharset);
}

static long sInitialize_NativeTools(JNIEnv *env)
{
    /* Get the NativeTools class. */
    NativeToolsClass = sFindClass(env, NativeToolsClassName);
    if (NativeToolsClass == NULL)
        return eJNI_FIND_CLASS;

    /* NativeTools.getArgValue */
    NativeTools_getArgValue = 
	    sGetStaticMethodID(env,
		         NativeToolsClass, 
		         NativeTools_getArgValueName, 
		         NativeTools_getArgValueSig);
    if (NativeTools_getArgValue == NULL)
	return eJNI_FIND_METHOD;

    /* NativeTools.getArgType */
    NativeTools_getArgType = 
	    sGetStaticMethodID(env,
		         NativeToolsClass, 
		         NativeTools_getArgTypeName, 
		         NativeTools_getArgTypeSig);
    if (NativeTools_getArgType == NULL)
	return eJNI_FIND_METHOD;

    /* NativeTools.getArgOper */
    NativeTools_getArgOper = 
	    sGetStaticMethodID(env,
		         NativeToolsClass, 
		         NativeTools_getArgOperName, 
		         NativeTools_getArgOperSig);
    if (NativeTools_getArgOper == NULL)
	return eJNI_FIND_METHOD;

    /* NativeTools.getArgName */
    NativeTools_getArgName = 
	    sGetStaticMethodID(env,
		         NativeToolsClass, 
		         NativeTools_getArgNameName, 
		         NativeTools_getArgNameSig);
    if (NativeTools_getArgName == NULL)
	return eJNI_FIND_METHOD;


    return eOK;

}

/*
 * FUNCTION: jni_Initialize
 *
 * PURPOSE:  Get classes and initialize method and field ids for every
 *           Java class we interface to via JNI.
 *
 * RETURNS:  eOK 
 *           Some error code
 */

long jni_Initialize(JNIEnv *env)
{
    static int initd;
    static int initializing;

    long status;

    /* Don't bother if we've already initialized ourselves. */
    if (initd || initializing)
        return eOK;

    /* Make sure we were given a JNI env. */
    if (!env)
        return eJNI_INVALID_JNIENV;

    initializing = 1;

    /* java.lang.Boolean */
    status = sInitialize_Boolean(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize Boolean ids\n");
	goto status;
    }

    /* java.lang.Double */
    status = sInitialize_Double(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize Double ids\n");
	goto status;
    }

    /* java.lang.Integer */
    status = sInitialize_Integer(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize Integer ids\n");
	goto status;
    }

    /* java.lang.Long */
    status = sInitialize_Long(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize Long ids\n");
	goto status;
    }

    /* java.lang.Number */
    status = sInitialize_Number(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize Number ids\n");
	goto status;
    }

    /* java.lang.Object */
    status = sInitialize_Object(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize Object ids\n");
	goto status;
    }

    /* java.lang.String */
    status = sInitialize_String(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize String ids\n");
	goto status;
    }

    /* java.lang.Exception */
    status = sInitialize_Exception(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize Exception ids\n");
	goto status;
    }

    /* java.lang.RuntimeException */
    status = sInitialize_RuntimeException(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize RuntimeException ids\n");
	goto status;
    }

    /* java.util.Map */
    status = sInitialize_Map(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize Map ids\n");
	goto status;
    }

    /* java.util.HashMap */
    status = sInitialize_HashMap(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize HashMap ids\n");
	goto status;
    }

    /* java.sql.SQLException */
    status = sInitialize_SQLException(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize SQLException ids\n");
	goto status;
    }

    /* com.redprairie.moca.server.legacy.MocaSession */
    status = sInitialize_MocaSession(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize MocaSession ids\n");
	goto status;
    }

    /* com.redprairie.moca.server.legacy.MocaClientAdapter */
    status = sInitialize_MocaClientAdapter(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize MocaClientAdapter ids\n");
	goto status;
    }

    /* com.redprairie.moca.server.legacy.MocaServerAdapter */
    status = sInitialize_MocaServerAdapter(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize MocaServerAdapter ids\n");
	goto status;
    }

    /* com.redprairie.moca.server.legacy.InProcessMocaServerAdapter */
    status = sInitialize_InProcessMocaServerAdapter(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize InProcessMocaServerAdapter ids -"
               " ignoring next exception as may be Version mismatch between"
               " moca-native.jar and moca-server.jar\n");
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
    }

    /* com.redprairie.moca.server.legacy.WrappedResults */
    status = sInitialize_WrappedResults(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize WrappedResults ids\n");
	goto status;
    }

    /* com.redprairie.moca.server.legacy.CommandInvocationException */
    status = sInitialize_CommandInvocationException(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize CommandInvocationException ids\n");
	goto status;
    }

    /* com.redprairie.moca.server.legacy.CommandInvocationException */
    status = sInitialize_MocaNativeException(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize MocaNativeException ids\n");
	goto status;
    }

    /* com.redprairie.moca.MocaLibInfo */
    status = sInitialize_MocaLibInfo(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize MocaLibInfo ids\n");
	goto status;
    }

    /* com.redprairie.moca.MocaException */
    status = sInitialize_MocaException(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize MocaException ids\n");
	goto status;
    }

    /* com.redprairie.moca.MocaRuntimeException */
    status = sInitialize_MocaRuntimeException(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize MocaRuntimeException ids\n");
	goto status;
    }

    /* com.redprairie.moca.MocaException$Args */
    status = sInitialize_MocaExceptionArgs(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize MocaExceptionArgs ids\n");
	goto status;
    }

    /* com.redprairie.moca.server.db.BindList */
    status = sInitialize_BindList(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize BindList ids\n");
	goto status;
    }

    /* com.redprairie.moca.server.legacy.GenericPointer */
    status = sInitialize_Generic(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize GenericPointer ids\n");
	goto status;
    }

    status = sInitialize_NativeReturnStruct(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize NativeReturnStruct ids\n");
        goto status;
    }

    status = sInitialize_NativeTools(env);
    if (status != eOK)
    {
        fprintf(stderr, "Could not initialize NativeTools ids\n");
        goto status;
    }

    /* Initialize the Java character set. */
    sInitialize_CharacterSet(env);

    initd = 1;
    initializing = 0;

    return eOK;
status:
    initializing = 0;
    return status;
}
