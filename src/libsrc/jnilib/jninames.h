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

#ifndef JNINAMES_H
#define JNINAMES_H

/*
 * Class Names
 */

/* Java Classes */
static const char *BooleanClassName =
        "java/lang/Boolean";
static const char *DoubleClassName =
        "java/lang/Double";
static const char *IntegerClassName =
        "java/lang/Integer";
static const char *LongClassName =
        "java/lang/Long";
static const char *NumberClassName =
        "java/lang/Number";
static const char *ObjectClassName =
        "java/lang/Object";
static const char *StringClassName =
        "java/lang/String";
static const char *ExceptionClassName =
        "java/lang/Exception";
static const char *RuntimeExceptionClassName =
        "java/lang/RuntimeException";

/* Java Util Classes */
static const char *MapClassName =
        "java/util/Map";
static const char *HashMapClassName =
        "java/util/HashMap";

/* Java SQL Classes */
static const char *SQLExceptionClassName =
        "java/sql/SQLException";

/* MOCA Classes */
static const char *MocaSessionClassName = 
	"com/redprairie/moca/server/legacy/MocaSession";
static const char *MocaClientAdapterClassName = 
	"com/redprairie/moca/server/legacy/MocaClientAdapter";
static const char *MocaServerAdapterClassName =
	"com/redprairie/moca/server/legacy/MocaServerAdapter";
static const char *InProcessMocaServerAdapterClassName =
        "com/redprairie/moca/server/legacy/ServerModeInProcessMocaServerAdapter";
static const char *WrappedResultsClassName =
	"com/redprairie/moca/server/legacy/WrappedResults";
static const char *CommandInvocationExceptionClassName =
	"com/redprairie/moca/server/legacy/CommandInvocationException";
static const char *MocaLibInfoClassName =
        "com/redprairie/moca/MocaLibInfo";
static const char *MocaExceptionClassName =
	"com/redprairie/moca/MocaException";
static const char *MocaRuntimeExceptionClassName =
	"com/redprairie/moca/MocaRuntimeException";
static const char *MocaExceptionArgsClassName =
	"com/redprairie/moca/MocaException$Args";
static const char *BindListClassName =
	"com/redprairie/moca/server/db/BindList";
static const char *GenericPointerClassName =
        "com/redprairie/moca/server/legacy/GenericPointer";
static const char *NativeReturnStructName =
        "com/redprairie/moca/server/legacy/NativeReturnStruct";
static const char *NativeToolsClassName =
        "com/redprairie/moca/server/legacy/NativeTools";
static const char *MocaNativeExceptionClassName =
        "com/redprairie/moca/server/legacy/MocaNativeException";

/*
 * Method Names and Signatures
 */

/* java.lang.Boolean */
static const char *Boolean_constructorName = 
	"<init>";
static const char *Boolean_constructorSig = 
	"(Z)V";

static const char *Boolean_booleanValueName = 
	"booleanValue";
static const char *Boolean_booleanValueSig = 
	"()Z";

/* java.lang.Double */
static const char *Double_constructorName = 
	"<init>";
static const char *Double_constructorSig = 
	"(D)V";

static const char *Double_doubleValueName = 
	"doubleValue";
static const char *Double_doubleValueSig = 
	"()D";

/* java.lang.Integer */
static const char *Integer_constructorName = 
	"<init>";
static const char *Integer_constructorSig = 
	"(I)V";

static const char *Integer_intValueName = 
	"intValue";
static const char *Integer_intValueSig = 
	"()I";

/* java.lang.Long */
static const char *Long_constructorName = 
	"<init>";
static const char *Long_constructorSig = 
	"(J)V";

static const char *Long_longValueName = 
	"longValue";
static const char *Long_longValueSig = 
	"()J";

/* java.lang.Number */
static const char *Number_intValueName = 
	"intValue";
static const char *Number_intValueSig = 
	"()I";

static const char *Number_doubleValueName = 
	"doubleValue";
static const char *Number_doubleValueSig = 
	"()D";

/* java.lang.Object */
static const char *Object_toStringName = 
	"toString";
static const char *Object_toStringSig = 
	"()Ljava/lang/String;";

/* java.lang.String */
static const char *String_constructorName = 
	"<init>";
static const char *String_constructorSig = 
	"([BLjava/lang/String;)V";

static const char *String_getBytesName = 
	"getBytes";
static const char *String_getBytesSig = 
	"(Ljava/lang/String;)[B";

/* java.lang.Exception */
static const char *Exception_getMessageName =
        "getMessage";
static const char *Exception_getMessageSig =
        "()Ljava/lang/String;";

/* java.lang.RuntimeException */
static const char *RuntimeException_getMessageName =
        "getMessage";
static const char *RuntimeException_getMessageSig =
        "()Ljava/lang/String;";

/* java.util.Map */
static const char *Map_putName = 
        "put";
static const char *Map_putSig = 
        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";

/* java.util.HashMap */
static const char *HashMap_constructorName = 
        "<init>";
static const char *HashMap_constructorSig = 
        "()V";

/* java.sql.SQLException */
static const char *SQLException_getErrorCodeName = 
	"getErrorCode";
static const char *SQLException_getErrorCodeSig = 
        "()I";

static const char *SQLException_getMessageName = 
	"getMessage";
static const char *SQLException_getMessageSig = 
        "()Ljava/lang/String;";

/* com.redprairie.moca.server.legacy.MocaSession */
static const char *MocaSession_newSessionKeyName = 
	"newSessionKey";
static const char *MocaSession_newSessionKeySig = 
	"(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;";

/* com.redprairie.moca.server.legacy.MocaClientAdapter */
static const char *MocaClientAdapter_constructorName = 
	"<init>";
static const char *MocaClientAdapter_constructorSig = 
	"(Ljava/lang/String;Ljava/lang/String;)V";

static const char *MocaClientAdapter_closeName = 
	"close";
static const char *MocaClientAdapter_closeSig = 
	"()V";

static const char *MocaClientAdapter_executeCommandName = 
	"executeCommand";
static const char *MocaClientAdapter_executeCommandSig = 
	"("
	"Ljava/lang/String;"
	")"
	"Lcom/redprairie/moca/MocaResults;";

static const char *MocaClientAdapter_loginName = 
	"login";
static const char *MocaClientAdapter_loginSig = 
	"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";

static const char *MocaClientAdapter_logoutName = 
	"logout";
static const char *MocaClientAdapter_logoutSig = 
	"()V";

static const char *MocaClientAdapter_setAutoCommitName = 
	"setAutoCommit";
static const char *MocaClientAdapter_setAutoCommitSig = 
	"(Z)V";

static const char *MocaClientAdapter_setApplicationIdName = 
	"setApplicationId";
static const char *MocaClientAdapter_setApplicationIdSig = 
	"(Ljava/lang/String;)V";

static const char *MocaClientAdapter_setEnvironmentName = 
	"setEnvironment";
static const char *MocaClientAdapter_setEnvironmentSig = 
	"(Ljava/lang/String;)V";

/* com.redprairie.moca.server.legacy.MocaServerAdapter */
static const char *MocaServerAdapter_getEnvironmentName = 
	"getEnvironment";
static const char *MocaServerAdapter_getEnvironmentSig = 
	"(Ljava/lang/String;)Ljava/lang/String;";

static const char *MocaServerAdapter_putEnvironmentName = 
	"putEnvironment";
static const char *MocaServerAdapter_putEnvironmentSig = 
	"(Ljava/lang/String;Ljava/lang/String;)V";

static const char *MocaServerAdapter_removeEnvironmentName = 
	"removeEnvironment";
static const char *MocaServerAdapter_removeEnvironmentSig = 
	"(Ljava/lang/String;)V";

static const char *MocaServerAdapter_commitTxName = 
	"commitTx";
static const char *MocaServerAdapter_commitTxSig = 
	"()V";

static const char *MocaServerAdapter_rollbackTxName = 
	"rollbackTx";
static const char *MocaServerAdapter_rollbackTxSig = 
	"()V";

static const char *MocaServerAdapter_commitDBName = 
	"commitDB";
static const char *MocaServerAdapter_commitDBSig = 
	"()V";

static const char *MocaServerAdapter_rollbackDBName = 
	"rollbackDB";
static const char *MocaServerAdapter_rollbackDBSig = 
	"()V";

static const char *MocaServerAdapter_rollbackDBToSavepointName = 
	"rollbackDBToSavepoint";
static const char *MocaServerAdapter_rollbackDBToSavepointSig = 
	"(Ljava/lang/String;)V";

static const char *MocaServerAdapter_setSavepointName = 
	"setSavepoint";
static const char *MocaServerAdapter_setSavepointSig = 
	"(Ljava/lang/String;)V";

static const char *MocaServerAdapter_executeCommandName = 
	"executeCommand";
static const char *MocaServerAdapter_executeCommandSig =
	"("
        "Ljava/lang/String;"
        "Ljava/util/Map;"
        "Z"
        ")"
        "Lcom/redprairie/moca/server/legacy/NativeReturnStruct;";

static const char *MocaServerAdapter_executeSQLName = 
	"executeSQL";
static const char *MocaServerAdapter_executeSQLSig =
	"("
        "Ljava/lang/String;"
	"Lcom/redprairie/moca/server/db/BindList;"
        "ZZ"
        ")"
        "Lcom/redprairie/moca/server/legacy/NativeReturnStruct;";

static const char *MocaServerAdapter_getStackElementName = 
	"getStackElement";
static const char *MocaServerAdapter_getStackElementSig =
	"(Ljava/lang/String;Ljava/lang/String;Z)"
	"Lcom/redprairie/moca/MocaArgument;";

static const char *MocaServerAdapter_getStackArgsName = 
	"getStackArgs";
static const char *MocaServerAdapter_getStackArgsSig =
	"(Z)[Lcom/redprairie/moca/MocaArgument;";

static const char *MocaServerAdapter_getDBTypeName = 
	"getDBType";
static const char *MocaServerAdapter_getDBTypeSig =
	"()I";

static const char *MocaServerAdapter_getNextSequenceValueName = 
	"getNextSequenceValue";
static const char *MocaServerAdapter_getNextSequenceValueSig =
	"(Ljava/lang/String;)Ljava/lang/String;";

static const char *MocaServerAdapter_traceName =
        "trace";
static const char *MocaServerAdapter_traceSig =
        "(ILjava/lang/String;)V";

static const char *MocaServerAdapter_logName =
        "log";
static const char *MocaServerAdapter_logSig =
        "(ILjava/lang/String;)V";

static const char *MocaServerAdapter_setTraceFileNameName =
        "setTraceFileName";
static const char *MocaServerAdapter_setTraceFileNameSig =
        "(Ljava/lang/String;Z)V";

static const char *MocaServerAdapter_setTraceLevelName =
        "setTraceLevel";
static const char *MocaServerAdapter_setTraceLevelSig =
        "(I)V";

static const char *MocaServerAdapter_getTraceLevelName =
        "getTraceLevel";
static const char *MocaServerAdapter_getTraceLevelSig =
        "()I";

static const char *MocaServerAdapter_translateMessageName =
        "translateMessage";
static const char *MocaServerAdapter_translateMessageSig =
        "(Ljava/lang/String;)Ljava/lang/String;";

/* com.redprairie.moca.server.legacy.ServerModeInProcessMocaServerAdapter */
static const char *InProcessMocaServerAdapter_constructorName = 
	"<init>";
static const char *InProcessMocaServerAdapter_constructorSig =
	"(Ljava/lang/String;Z)V";

/* com.redprairie.moca.server.legacy.WrappedResults */
static const char *WrappedResults_constructorName = 
	"<init>";
static const char *WrappedResults_constructorSig =
	"(IZ)V";

static const char *WrappedResults_internalResName = 
	"_internalRes";
static const char *WrappedResults_internalResType = 
	"I";

static const char *WrappedResults_internalRowName = 
	"_internalRow";
static const char *WrappedResults_internalRowType = 
	"I";

static const char *WrappedResults_nextRowName = 
	"_nextRow";
static const char *WrappedResults_nextRowType = 
	"I";

static const char *WrappedResults_editRowName = 
	"_editRow";
static const char *WrappedResults_editRowType = 
	"I";

static const char *WrappedResults_allocateNullsName = 
	"_allocateNulls";
static const char *WrappedResults_allocateNullsType = 
	"Z";

/* com.redprairie.moca.server.legacy.CommandInvocationException */
static const char *CommandInvocationException_constructorName = 
	"<init>";
static const char *CommandInvocationException_constructorSig =
	"("
	"I"
	"Ljava/lang/String;"
	"Z"
	"Lcom/redprairie/moca/MocaResults;"
	")V";

static const char *CommandInvocationException_addArgName = 
	"addArg";
static const char *CommandInvocationException_addArgSig =
	"(Ljava/lang/String;Ljava/lang/Object;)V";

static const char *CommandInvocationException_addLookupArgName = 
	"addLookupArg";
static const char *CommandInvocationException_addLookupArgSig =
	"(Ljava/lang/String;Ljava/lang/String;)V";

/* com.redprairie.moca.server.legacy.MocaNativeException */
static const char *MocaNativeException_constructorName = 
	"<init>";
static const char *MocaNativeException_constructorSig =
	"("
	"Ljava/lang/String;"
	"Ljava/lang/String;"
	"Ljava/lang/String;"
	")V";
/* com.redprairie.moca.MocaLibInfo */
static const char *MocaLibInfo_constructorName = 
	"<init>";
static const char *MocaLibInfo_constructorSig =
	"(Ljava/lang/String;Ljava/lang/String;)V";

/* com.redprairie.moca.MocaException */
static const char *MocaException_getErrorCodeName = 
	"getErrorCode";
static const char *MocaException_getErrorCodeSig = 
	"()I";

static const char *MocaException_getMessageName = 
	"getMessage";
static const char *MocaException_getMessageSig = 
	"()Ljava/lang/String;";

static const char *MocaException_getArgListName = 
	"getArgList";
static const char *MocaException_getArgListSig = 
	"()[Lcom/redprairie/moca/MocaException$Args;";

static const char *MocaException_getResultsName = 
	"getResults";
static const char *MocaException_getResultsSig = 
	"()Lcom/redprairie/moca/MocaResults;";

static const char *MocaException_isResolvedName = 
	"isMessageResolved";
static const char *MocaException_isResolvedSig = 
	"()Z";

/* com.redprairie.moca.MocaRuntimeException */
static const char *MocaRuntimeException_getErrorCodeName = 
	"getErrorCode";
static const char *MocaRuntimeException_getErrorCodeSig = 
	"()I";

static const char *MocaRuntimeException_getMessageName = 
	"getMessage";
static const char *MocaRuntimeException_getMessageSig = 
	"()Ljava/lang/String;";

static const char *MocaRuntimeException_getArgListName = 
	"getArgList";
static const char *MocaRuntimeException_getArgListSig = 
	"()[Lcom/redprairie/moca/MocaException$Args;";

static const char *MocaRuntimeException_getResultsName = 
	"getResults";
static const char *MocaRuntimeException_getResultsSig = 
	"()Lcom/redprairie/moca/MocaResults;";

static const char *MocaRuntimeException_isResolvedName = 
	"isMessageResolved";
static const char *MocaRuntimeException_isResolvedSig = 
	"()Z";

/* com.redprairie.moca.MocaException$Args */
static const char *MocaExceptionArgs_getNameName = 
	"getName";
static const char *MocaExceptionArgs_getNameSig =
	"()Ljava/lang/String;";

static const char *MocaExceptionArgs_getValueName = 
	"getValue";
static const char *MocaExceptionArgs_getValueSig =
	"()Ljava/lang/Object;";

static const char *MocaExceptionArgs_isLookupName = 
	"isLookup";
static const char *MocaExceptionArgs_isLookupSig =
	"()Z";

/* com.redprairie.moca.server.db.BindList */
static const char *BindList_constructorName = 
	"<init>";
static const char *BindList_constructorSig =
	"()V";

static const char *BindList_addIntName = 
	"add";
static const char *BindList_addIntSig =
	"(Ljava/lang/String;CI)V";

static const char *BindList_addDoubleName = 
	"add";
static const char *BindList_addDoubleSig =
	"(Ljava/lang/String;CD)V";

static const char *BindList_addBooleanName = 
	"add";
static const char *BindList_addBooleanSig =
	"(Ljava/lang/String;CZ)V";

static const char *BindList_addObjectName = 
	"add";
static const char *BindList_addObjectSig =
        "(Ljava/lang/String;CLjava/lang/Object;)V";

static const char *BindList_addObjectWithSizeName = 
	"add";
static const char *BindList_addObjectWithSizeSig =
        "(Ljava/lang/String;CLjava/lang/Object;I)V";

static const char *BindList_getValueName = 
	"getValue";
static const char *BindList_getValueSig =
        "(Ljava/lang/String;)Ljava/lang/Object;";

/* com.redprairie.moca.server.legacy.GenericPointer */
static const char *GenericPointer_constructorName = 
	"<init>";
static const char *GenericPointer_constructorSig =
	"(I)V";

static const char *GenericPointer_32bitValueName = 
	"get32bitValue";
static const char *GenericPointer_32bitValueSig =
	"()I";

/* com.redprairie.moca.server.legacy.NativeReturnStruct */
static const char *NativeReturnStruct_constructorName =
        "<init>";
static const char *NativeReturnStruct_exceptionConstructorSig =
        "(Lcom/redprairie/moca/MocaException;)V";
static const char *NativeReturnStruct_resultsConstructorSig =
        "(Lcom/redprairie/moca/MocaResults;)V";

static const char *NativeReturnStruct_getErrorCodeName =
        "getErrorCode";
static const char *NativeReturnStruct_getErrorCodeSig =
        "()I";

static const char *NativeReturnStruct_getResultsName =
        "getResults";
static const char *NativeReturnStruct_getResultsSig =
        "()Lcom/redprairie/moca/MocaResults;";

static const char *NativeReturnStruct_getMessageName =
        "getMessage";
static const char *NativeReturnStruct_getMessageSig =
        "()Ljava/lang/String;";
	
static const char *NativeReturnStruct_getArgsName =
        "getArgs";
static const char *NativeReturnStruct_getArgsSig =
        "()[Lcom/redprairie/moca/MocaException$Args;";

static const char *NativeReturnStruct_getBindListName =
        "getBindList";
static const char *NativeReturnStruct_getBindListSig =
        "()Lcom/redprairie/moca/server/db/BindList;";

static const char *NativeReturnStruct_isResolvedName =
        "isMessageResolved";
static const char *NativeReturnStruct_isResolvedSig =
        "()Z";

static const char *NativeTools_getArgValueName = 
	"getArgValue";
static const char *NativeTools_getArgValueSig =
	"(Lcom/redprairie/moca/MocaArgument;)Ljava/lang/Object;";

static const char *NativeTools_getArgTypeName = 
	"getArgType";
static const char *NativeTools_getArgTypeSig =
	"(Lcom/redprairie/moca/MocaArgument;)C";

static const char *NativeTools_getArgOperName = 
	"getArgOper";
static const char *NativeTools_getArgOperSig =
	"(Lcom/redprairie/moca/MocaArgument;)I";

static const char *NativeTools_getArgNameName = 
	"getArgName";
static const char *NativeTools_getArgNameSig =
	"(Lcom/redprairie/moca/MocaArgument;)Ljava/lang/String;";

#endif
