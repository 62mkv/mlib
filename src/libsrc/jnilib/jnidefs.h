/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Header file for Java integration.
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

#ifndef JNIDEFS_H
#define JNIDEFS_H

#include <jni.h>

#ifdef MAIN
#  define STORAGE_CLASS
#else
#  define STORAGE_CLASS extern
#endif

/*
 * Classes
 */

STORAGE_CLASS jclass BooleanClass;
STORAGE_CLASS jclass DoubleClass;
STORAGE_CLASS jclass IntegerClass;
STORAGE_CLASS jclass LongClass;
STORAGE_CLASS jclass NumberClass;
STORAGE_CLASS jclass ObjectClass;
STORAGE_CLASS jclass StringClass;
STORAGE_CLASS jclass ExceptionClass;
STORAGE_CLASS jclass RuntimeExceptionClass;

STORAGE_CLASS jclass MapClass;
STORAGE_CLASS jclass HashMapClass;

STORAGE_CLASS jclass SQLExceptionClass;

STORAGE_CLASS jclass MocaSessionClass;
STORAGE_CLASS jclass MocaClientAdapterClass;
STORAGE_CLASS jclass MocaServerAdapterClass;
STORAGE_CLASS jclass InProcessMocaServerAdapterClass;
STORAGE_CLASS jclass WrappedResultsClass;
STORAGE_CLASS jclass CommandInvocationExceptionClass;
STORAGE_CLASS jclass MocaNativeExceptionClass;
STORAGE_CLASS jclass MocaLibInfoClass;
STORAGE_CLASS jclass MocaExceptionClass;
STORAGE_CLASS jclass MocaRuntimeExceptionClass;
STORAGE_CLASS jclass MocaExceptionArgsClass;
STORAGE_CLASS jclass BindListClass;
STORAGE_CLASS jclass GenericPointerClass;
STORAGE_CLASS jclass NativeReturnStructClass;
STORAGE_CLASS jclass NativeToolsClass;

/*
 * Methods/Fields
 */

/* java.lang.Boolean */
STORAGE_CLASS jmethodID Boolean_constructor;
STORAGE_CLASS jmethodID Boolean_booleanValue;

/* java.lang.Double */
STORAGE_CLASS jmethodID Double_constructor;
STORAGE_CLASS jmethodID Double_doubleValue;

/* java.lang.Integer */
STORAGE_CLASS jmethodID Integer_constructor;
STORAGE_CLASS jmethodID Integer_intValue;

/* java.lang.Long */
STORAGE_CLASS jmethodID Long_constructor;
STORAGE_CLASS jmethodID Long_longValue;

/* java.lang.Number */
STORAGE_CLASS jmethodID Number_intValue;
STORAGE_CLASS jmethodID Number_doubleValue;

/* java.lang.Object */
STORAGE_CLASS jmethodID Object_constructor;
STORAGE_CLASS jmethodID Object_toString;

/* java.lang.String */
STORAGE_CLASS jmethodID String_constructor;
STORAGE_CLASS jmethodID String_getBytes;

/* java.lang.Exception */
STORAGE_CLASS jmethodID Exception_getMessage;

/* java.lang.RuntimeException */
STORAGE_CLASS jmethodID RuntimeException_getMessage;

/* java.util.Map */
STORAGE_CLASS jmethodID Map_put;

/* java.util.HashMap */
STORAGE_CLASS jmethodID HashMap_constructor;

/* java.sql.SQLException */
STORAGE_CLASS jmethodID SQLException_getErrorCode;
STORAGE_CLASS jmethodID SQLException_getMessage;

/* com.redprairie.moca.server.legacy.MocaSession */
STORAGE_CLASS jmethodID MocaSession_newSessionKey;

/* com.redprairie.moca.server.legacy.MocaClientAdapter */
STORAGE_CLASS jmethodID MocaClientAdapter_constructor;
STORAGE_CLASS jmethodID MocaClientAdapter_close;
STORAGE_CLASS jmethodID MocaClientAdapter_executeCommand;
STORAGE_CLASS jmethodID MocaClientAdapter_login;
STORAGE_CLASS jmethodID MocaClientAdapter_logout;
STORAGE_CLASS jmethodID MocaClientAdapter_setAutoCommit;
STORAGE_CLASS jmethodID MocaClientAdapter_setApplicationId;
STORAGE_CLASS jmethodID MocaClientAdapter_setEnvironment;

/* com.redprairie.moca.server.legacy.MocaServerAdapter */
STORAGE_CLASS jmethodID MocaServerAdapter_getEnvironment;
STORAGE_CLASS jmethodID MocaServerAdapter_putEnvironment;
STORAGE_CLASS jmethodID MocaServerAdapter_removeEnvironment;
STORAGE_CLASS jmethodID MocaServerAdapter_commitTx;
STORAGE_CLASS jmethodID MocaServerAdapter_rollbackTx;
STORAGE_CLASS jmethodID MocaServerAdapter_commitDB;
STORAGE_CLASS jmethodID MocaServerAdapter_rollbackDB;
STORAGE_CLASS jmethodID MocaServerAdapter_rollbackDBToSavepoint;
STORAGE_CLASS jmethodID MocaServerAdapter_setSavepoint;
STORAGE_CLASS jmethodID MocaServerAdapter_executeCommand;
STORAGE_CLASS jmethodID MocaServerAdapter_executeSQL;
STORAGE_CLASS jmethodID MocaServerAdapter_getStackElement;
STORAGE_CLASS jmethodID MocaServerAdapter_getStackArgs;
STORAGE_CLASS jmethodID MocaServerAdapter_getDBType;
STORAGE_CLASS jmethodID MocaServerAdapter_getNextSequenceValue;
STORAGE_CLASS jmethodID MocaServerAdapter_trace;
STORAGE_CLASS jmethodID MocaServerAdapter_log;
STORAGE_CLASS jmethodID MocaServerAdapter_setTraceFileName;
STORAGE_CLASS jmethodID MocaServerAdapter_setTraceLevel;
STORAGE_CLASS jmethodID MocaServerAdapter_getTraceLevel;
STORAGE_CLASS jmethodID MocaServerAdapter_translateMessage;

/* com.redprairie.moca.server.legacy.InProcessMocaServerAdapter */
STORAGE_CLASS jmethodID InProcessMocaServerAdapter_constructor;

/* com.redprairie.moca.server.legacy.WrappedResults */
STORAGE_CLASS jmethodID WrappedResults_constructor;
STORAGE_CLASS jfieldID  WrappedResults_internalRes;
STORAGE_CLASS jfieldID  WrappedResults_internalRow;
STORAGE_CLASS jfieldID  WrappedResults_nextRow;
STORAGE_CLASS jfieldID  WrappedResults_editRow;
STORAGE_CLASS jfieldID  WrappedResults_allocateNulls;

/* com.redprairie.moca.server.legacy.CommandInvocationExceptionClass */
STORAGE_CLASS jmethodID CommandInvocationException_constructor;
STORAGE_CLASS jmethodID CommandInvocationException_addArg;
STORAGE_CLASS jmethodID CommandInvocationException_addLookupArg;

/* com.redprairie.moca.server.legacy.MocaNativeExceptionClass */
STORAGE_CLASS jmethodID MocaNativeException_constructor;

/* com.redprairie.moca.MocaLibInfo */
STORAGE_CLASS jmethodID MocaLibInfo_constructor;

/* com.redprairie.moca.MocaException */
STORAGE_CLASS jmethodID MocaException_getErrorCode;
STORAGE_CLASS jmethodID MocaException_getMessage;
STORAGE_CLASS jmethodID MocaException_getArgList;
STORAGE_CLASS jmethodID MocaException_getResults;
STORAGE_CLASS jmethodID MocaException_isResolved;

/* com.redprairie.moca.MocaRuntimeException */
STORAGE_CLASS jmethodID MocaRuntimeException_getErrorCode;
STORAGE_CLASS jmethodID MocaRuntimeException_getMessage;
STORAGE_CLASS jmethodID MocaRuntimeException_getArgList;
STORAGE_CLASS jmethodID MocaRuntimeException_getResults;
STORAGE_CLASS jmethodID MocaRuntimeException_isResolved;

/* com.redprairie.moca.MocaException$Args */
STORAGE_CLASS jmethodID MocaExceptionArgs_getName;
STORAGE_CLASS jmethodID MocaExceptionArgs_getValue;
STORAGE_CLASS jmethodID MocaExceptionArgs_isLookup;

/* com.redprairie.moca.server.db.BindList */
STORAGE_CLASS jmethodID BindList_constructor;
STORAGE_CLASS jmethodID BindList_addInt;
STORAGE_CLASS jmethodID BindList_addDouble;
STORAGE_CLASS jmethodID BindList_addBoolean;
STORAGE_CLASS jmethodID BindList_addObject;
STORAGE_CLASS jmethodID BindList_addObjectWithSize;
STORAGE_CLASS jmethodID BindList_getValue;

/* com.redprairie.moca.server.legacy.GenericPointer */
STORAGE_CLASS jmethodID GenericPointer_constructor;
STORAGE_CLASS jmethodID GenericPointer_32bitValue;

/* com.redprairie.moca.server.legacy.NativeReturnStruct */
STORAGE_CLASS jmethodID NativeReturnStruct_exceptionConstructor;
STORAGE_CLASS jmethodID NativeReturnStruct_resultsConstructor;
STORAGE_CLASS jmethodID NativeReturnStruct_getErrorCode;
STORAGE_CLASS jmethodID NativeReturnStruct_getResults;
STORAGE_CLASS jmethodID NativeReturnStruct_getMessage;
STORAGE_CLASS jmethodID NativeReturnStruct_getArgs;
STORAGE_CLASS jmethodID NativeReturnStruct_getBindList;
STORAGE_CLASS jmethodID NativeReturnStruct_isResolved;

/* com.redprairie.moca.server.legacy.NativeTools */
STORAGE_CLASS jmethodID NativeTools_getArgValue;
STORAGE_CLASS jmethodID NativeTools_getArgType;
STORAGE_CLASS jmethodID NativeTools_getArgOper;
STORAGE_CLASS jmethodID NativeTools_getArgName;
#endif
