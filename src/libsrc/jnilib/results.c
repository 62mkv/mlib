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
 *#END************************************************************************/

#include <moca.h>

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <jnilib.h>
#include <sqllib.h>
#include <srvlib.h>

#include "jniprivate.h"
#include "jnidefs.h"
#include "results.h"

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _initIDs
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1initIDs(JNIEnv *env, jclass cls)
{
    long status;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return;
}

static mocaDataRes *getRes(JNIEnv *env, jobject obj)
{
    return (mocaDataRes *)(*env)->GetIntField(env, 
                                              obj, 
                                              WrappedResults_internalRes);
}

static mocaDataRow *getEditRow(JNIEnv *env, jobject obj)
{
    return (mocaDataRow *)(*env)->GetIntField(env, 
                                              obj, 
					      WrappedResults_editRow);
}

static void noRowException(JNIEnv *env)
{
    (*env)->ThrowNew(env, 
	             (*env)->FindClass(env, 
			               "java/lang/IllegalStateException"),
	             "no current row");
}

static void noResException(JNIEnv *env)
{
    (*env)->ThrowNew(env, 
	             (*env)->FindClass(env, 
			               "java/lang/IllegalStateException"),
	             "no current backing results");
}

static void invalidTypeException(JNIEnv *env)
{
    (*env)->ThrowNew(env, 
	             (*env)->FindClass(env, 
			               "java/lang/IllegalArgumentException"), 
		     "type mismatch on data assignment");
}

static void outOfMemoryError(JNIEnv *env)
{
    (*env)->ThrowNew(env, 
	             (*env)->FindClass(env, 
			               "java/lang/OutOfMemoryError"), 
		     "Could not allocate space for the result set");
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _newResults
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1newResults(JNIEnv *env, jobject obj) 
{
    mocaDataRes *res;
    res = sql_AllocateResultHdr(0);
    return (jint) res;
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _dispose
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1dispose(JNIEnv *env, jobject obj, jint intRes)
{
    mocaDataRes *res = (mocaDataRes *)intRes;

    if (res)
        sqlFreeResults(res);
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _addColumn
 * Signature: (Ljava/lang/String;CIZ)V
 */
JNIEXPORT void JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1addColumn(JNIEnv *env, jobject obj, jint intRes, jstring jColName, jchar dataType, jint maxSize, jboolean isNullable)
{
    const char *colName;
    long colNum;
    mocaDataRes *res = (mocaDataRes *)intRes;

    if (res == NULL) 
    {
        noResException(env);
        return;
    }

    colName = (*env)->GetStringUTFChars(env, jColName, NULL);
    sqlAddColumn(res, (char *)colName, (char)dataType, maxSize);
    (*env)->ReleaseStringUTFChars(env, jColName, colName);

    /* Let's assume the last column is the one we just added */
    colNum = sqlGetNumColumns(res);

    sql_SetColNullableByPos(res, colNum - 1, isNullable == JNI_TRUE);
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _addRow
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1addRow(JNIEnv *env, jobject obj, jint intRes)
{
    mocaDataRow *row;
    mocaDataRes *res; 

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return 0;
    }

    row = sqlAddRow(res);
    return (jint)row;
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _removeRow
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1removeRow(JNIEnv *env, jobject obj, jint intRes, jint intRow)
{
    mocaDataRow *row, *next;
    mocaDataRes *res; 

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return 0;
    }

    row = (mocaDataRow *)intRow;
    if (row != NULL)
    {
        next = sqlGetNextRow(row);
        sqlRemoveRow(res, row);
    }
    else
    {
        next = NULL;
    }

    return (jint) next;
}


/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _setObjectValue
 * Signature: (ILjava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1setObjectValue(JNIEnv *env, jobject obj, jint intRes, jint intRow, jint colNum, jobject objValue)
{
    mocaDataRow *row;
    mocaDataRes *res; 

    mocaObjectRef *mocaRef;

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return;
    }

    row = (mocaDataRow *)intRow;
    if (!row)
    {
        noRowException(env);
        return;
    }

    mocaRef = jni_ObjectRefValue(env, objValue);

    sql_AddRowItem(res, row, (long)colNum, sizeof mocaRef, &mocaRef);
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _setInt
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1setInt(JNIEnv *env, jobject obj, jint intRes, jint intRow, jint colNum, jint value)
{
    char typeCode;

    mocaDataRow *row;
    mocaDataRes *res; 

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return;
    }

    row = (mocaDataRow *)intRow;
    if (!row)
    {
        noRowException(env);
        return;
    }

    typeCode = sqlGetDataTypeByPos(res, (int)colNum);
    if (typeCode != COMTYP_INT && typeCode != COMTYP_LONG)
    {
        invalidTypeException(env);
        return;
    }

    sql_AddRowItem(res, row, (long)colNum, sizeof(long), (void *) &value);
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _setDouble
 * Signature: (ID)V
 */
JNIEXPORT void JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1setDouble(JNIEnv *env, jobject obj, jint intRes, jint intRow, jint colNum, jdouble value)
{
    char typeCode;
    mocaDataRow *row;
    mocaDataRes *res; 

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return;
    }

    row = (mocaDataRow *)intRow;
    if (!row)
    {
        noRowException(env);
        return;
    }

    typeCode = sqlGetDataTypeByPos(res, (int)colNum);
    if (typeCode != COMTYP_FLOAT)
    {
        invalidTypeException(env);
        return;
    }

    sql_AddRowItem(res, row, (long)colNum, sizeof(double), (void *) &value);
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _setBoolean
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1setBoolean(JNIEnv *env, jobject obj, jint intRes, jint intRow, jint colNum, jboolean value)
{
    char typeCode;

    moca_bool_t storedValue;

    mocaDataRow *row;
    mocaDataRes *res; 

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return;
    }

    row = (mocaDataRow *)intRow;
    if (!row)
    {
        noRowException(env);
        return;
    }

    typeCode = sqlGetDataTypeByPos(res, (int)colNum);
    if (typeCode != COMTYP_BOOLEAN)
    {
        invalidTypeException(env);
        return;
    }

    if (value == JNI_TRUE) 
	storedValue = MOCA_TRUE;
    else 
	storedValue = MOCA_FALSE;

    sql_AddRowItem(res, 
	           row, 
		   (long) colNum, 
		   sizeof storedValue, 
		   (void *) &storedValue);
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _setBinary
 * Signature: (I[B)V
 */
JNIEXPORT void JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1setBinary(JNIEnv *env, jobject obj, jint intRes, jint intRow, jint colNum, jbyteArray value)
{
    long encodedLength;

    char *encoded;
    char typeCode;

    mocaDataRow *row;
    mocaDataRes *res; 

    jbyte *bytes;
    jsize arrayLength;

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return;
    }

    row = (mocaDataRow *)intRow;
    if (!row)
    {
        noRowException(env);
        return;
    }

    typeCode = sqlGetDataTypeByPos(res, (int)colNum);
    if (typeCode != COMTYP_BINARY)
    {
        invalidTypeException(env);
        return;
    }

    bytes = (*env)->GetByteArrayElements(env, value, NULL);
    arrayLength = (*env)->GetArrayLength(env, value);

    encoded = sqlEncodeBinary(bytes, arrayLength);
    encodedLength = sqlEncodeBinaryLen(encoded);
    
    sql_AddRowItem(res, row, (long)colNum, encodedLength, encoded);

    free(encoded);

    (*env)->ReleaseByteArrayElements(env, value, bytes, JNI_ABORT);
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _setString
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1setString(JNIEnv *env, jobject obj, jint intRes, jint intRow, jint colNum, jbyteArray value)
{
    int length;

    char *bytes;
    char typeCode;

    mocaDataRow *row;
    mocaDataRes *res; 
    
    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return;
    }

    row = (mocaDataRow *)intRow;
    if (!row)
    {
        noRowException(env);
        return;
    }

    typeCode = sqlGetDataTypeByPos(res, (int)colNum);
    if (typeCode != COMTYP_CHAR && typeCode != COMTYP_DATTIM)
    {
        invalidTypeException(env);
        return;
    }

    length = (*env)->GetArrayLength(env, value);
    bytes = (char *)(*env)->GetByteArrayElements(env, value, NULL);

    sql_AddRowItem(res, row, (long)colNum, length, bytes);

    (*env)->ReleaseByteArrayElements(env, value, (jbyte *)bytes, 0);
}


/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _setNull
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1setNull(JNIEnv *env, jobject obj, jint intRes, jint intRow, jint colNum, jboolean allocateNulls)
{
    mocaDataRow *row;
    mocaDataRes *res; 

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return;
    }

    row = (mocaDataRow *)intRow;
    if (!row)
    {
        noRowException(env);
        return;
    }

    if (allocateNulls)
    {
        int size = 0;
        char datatype = sqlGetDataTypeByPos(res, (long)colNum);
        switch (datatype)
        {
	    case COMTYP_INT:
	    case COMTYP_LONG:
	    case COMTYP_LONGPTR:
	    case COMTYP_BOOLEAN:
		size = sizeof(long);
                break;
	    case COMTYP_FLOAT:
	    case COMTYP_FLOATPTR:
		size = sizeof(double);
                break;
	    case COMTYP_CHAR:
	    case COMTYP_TEXT:
	    case COMTYP_DATTIM:
                size = 1;
                break;
        }

        if (size) {
            row->DataPtr[colNum] = calloc(1, size);
        }
        else {
            row->DataPtr[colNum] = NULL;
        }
        row->NullInd[colNum] = 1;
    }
    else
    {
        sql_AddRowItem(res, row, (long)colNum, 0, NULL);
    }
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _firstRow
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1firstRow(JNIEnv *env, jobject obj, jint intRes)
{
    mocaDataRow *row;
    mocaDataRes *res; 

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return 0;
    }

    row = sqlGetRow(res);

    return (jint)row;
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _nextRow
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1nextRow(JNIEnv *env, jobject obj, jint intRes, jint intRow)
{
    mocaDataRes *res; 
    mocaDataRow *row, *next = NULL;

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return 0;
    }

    row = (mocaDataRow *)intRow;

    if (row != NULL)
    {
        next = sqlGetNextRow(row);
    }

    return (jint)next;
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _hasNextRow
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1hasNextRow(JNIEnv *env, jobject obj, jint intRes, jint intRow, jboolean needsFirstRow) 
{
    mocaDataRes *res; 
    mocaDataRow *row, *next = NULL;
    jboolean retVal = JNI_FALSE;

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return retVal;
    }

    if (needsFirstRow == JNI_TRUE)
    {
        /* If we need the first row, then check to see if the
         * results has a row, if so then that means we have
         * a next entry
         */
        next = sqlGetRow(res);
    }
    else if (intRow != 0)
    {
        /* If not then we have to check the current row
         * to see if it has another row after it
         */
        row = (mocaDataRow *)intRow;
        next = sqlGetNextRow(row);
    }

    if (next != NULL)
    {
        retVal = JNI_TRUE;
    }

    return retVal;
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getStringValue
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1getStringValue(JNIEnv *env, jobject obj, jint intRes, jint intRow, jint colNum)
{
    char *value;

    mocaDataRow *row;
    mocaDataRes *res; 

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return NULL;
    }

    row = (mocaDataRow *)intRow;
    if (!row)
    {
        noRowException(env);
        return NULL;
    }

    if (sqlIsNullByPos(res, row, (int)colNum))
    {
        return NULL;
    }

    value = sqlGetStringByPos(res, row, (int)colNum);

    return jniNewStringFromBytes(env, value);
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getIntValue
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1getIntValue(JNIEnv *env, jobject obj, jint intRes, jint intRow, jint colNum)
{
    long value;

    mocaDataRes *res; 
    mocaDataRow *row;

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return 0;
    }

    row = (mocaDataRow *)intRow;
    if (!row)
    {
        noRowException(env);
        return 0;
    }

    value = sqlGetLongByPos(res, row, (long)colNum);

    return (jint) value;
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getDoubleValue
 * Signature: (I)D
 */
JNIEXPORT jdouble JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1getDoubleValue(JNIEnv *env, jobject obj, jint intRes, jint intRow, jint colNum)
{
    double value;

    mocaDataRes *res; 
    mocaDataRow *row;

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return 0.0;
    }

    row = (mocaDataRow *)intRow;
    if (!row)
    {
        noRowException(env);
        return 0.0;
    }

    value = sqlGetFloatByPos(res, row, (int)colNum);

    return (jdouble) value;
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getBooleanValue
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1getBooleanValue(JNIEnv *env, jobject obj, jint intRes, jint intRow, jint colNum)
{
    mocaDataRow *row;
    mocaDataRes *res; 
    moca_bool_t value;

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return JNI_FALSE;
    }

    row = (mocaDataRow *)intRow;
    if (!row)
    {
        noRowException(env);
        return JNI_FALSE;
    }

    value = sqlGetBooleanByPos(res, row, (int)colNum);

    return (value == MOCA_TRUE) ? JNI_TRUE : JNI_FALSE;
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getBinaryValue
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1getBinaryValue(JNIEnv *env, jobject obj, jint intRes, jint intRow, jint colNum)
{
    long length;

    mocaDataRow *row;
    mocaDataRes *res; 

    jbyte *data;
    jbyteArray value;

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return NULL;
    }

    row = (mocaDataRow *)intRow;
    if (!row)
    {
        noRowException(env);
        return NULL;
    }

    if (sqlIsNullByPos(res, row, (int)colNum))
    {
        return NULL;
    }

    data = sqlGetBinaryDataByPos(res, row, (int)colNum);

    if (!data)
    {
        return NULL;
    }

    length = sqlGetBinaryDataLenByPos(res, row, (int)colNum);

    value = (*env)->NewByteArray(env, length);
    if (!value)
    {
        outOfMemoryError(env);
        return NULL;
    }

    (*env)->SetByteArrayRegion(env, value, 0, length, data);

    return value;
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getObjectValue
 * Signature: (I)O
 */
JNIEXPORT jobject JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1getObjectValue(JNIEnv *env, jobject obj, jint intRes, jint intRow, jint colNum)
{
    mocaDataRow *row;
    mocaDataRes *res; 
    mocaObjectRef **ref;

    jobject value = NULL;

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return NULL;
    }

    row = (mocaDataRow *)intRow;
    if (!row)
    {
        noRowException(env);
        return NULL;
    }

    ref = (mocaObjectRef **)sqlGetValueByPos(res, row, (int)colNum);
    if (ref)
        value = (jobject) sql_GetObjectRef(*ref);

    return value;
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _isNull
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1isNull(JNIEnv *env, jobject obj, jint intRes, jint intRow, jint colNum)
{
    mocaDataRow *row;
    mocaDataRes *res; 

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return JNI_FALSE;
    }

    row = (mocaDataRow *)intRow;
    if (!row)
    {
        noRowException(env);
        return JNI_FALSE;
    }

    return (MOCA_TRUE == sqlIsNullByPos(res, row, colNum)) ? JNI_TRUE : JNI_FALSE;
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getDataTypeCode
 * Signature: (I)C
 */
JNIEXPORT jchar JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1getDataTypeCode(JNIEnv *env, jobject obj, jint intRes, jint colNum)
{
    char typeCode;

    mocaDataRes *res; 

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return 0;
    }

    typeCode = sqlGetDataTypeByPos(res, (int)colNum);

    return (jchar) typeCode;
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _isNullable
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1isNullable(JNIEnv *env, jobject obj, jint intRes, jint colNum)
{
    mocaDataRes *res; 

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return JNI_FALSE;
    }

    return (MOCA_TRUE == sqlIsNullableByPos(res, (long)colNum)) ? JNI_TRUE : JNI_FALSE;
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getColumnNum
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1getColumnNum(JNIEnv *env, jobject obj, jint intRes, jstring jColName)
{
    const char *colName;

    long colNum;

    mocaDataRes *res; 

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return 0;
    }

    colName = (*env)->GetStringUTFChars(env, jColName, NULL);
    colNum = sqlFindColumn(res, (char *)colName);

    (*env)->ReleaseStringUTFChars(env, jColName, colName);

    return (jint) colNum;
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getColumnName
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1getColumnName(JNIEnv *env, jobject obj, jint intRes, jint colNum)
{
    char *colName;

    mocaDataRes *res; 

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return NULL;
    }

    colName = sqlGetColumnName(res, (int)colNum);

    return (*env)->NewStringUTF(env, colName);
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getColumnCount
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1getColumnCount(JNIEnv *env, jobject obj, jint intRes)
{
    mocaDataRes *res; 

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return 0;
    }

    return (jint) sqlGetNumColumns(res);
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getRowCount
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1getRowCount(JNIEnv *env, jobject obj, jint intRes)
{
    mocaDataRes *res; 

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return 0;
    }

    return (jint) sqlGetNumRows(res);
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _setResults
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1setResults(JNIEnv *env, jobject obj, jint intRes, jint intRow, jint colNum, jobject value)
{
    char typeCode;

    mocaDataRow *row;
    mocaDataRes *res, *sub; 

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return;
    }

    row = (mocaDataRow *)intRow;
    if (!row)
    {
        noRowException(env);
        return;
    }

    typeCode = sqlGetDataTypeByPos(res, (int)colNum);
    if (typeCode != COMTYP_RESULTS)
    {
        invalidTypeException(env);
        return;
    }

    sub = getRes(env, value);

    sql_AddRowItem(res, row, (long) colNum, sizeof(mocaDataRes *), &sub);
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getResultsValue
 * Signature: (I)Lcom/redprairie/moca/exec/WrappedResults
 */
JNIEXPORT jobject JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1getResultsValue(JNIEnv *env, jobject obj, jint intRes, jint intRow, jint colNum)
{
    mocaDataRow *row;
    mocaDataRes *res; 
    mocaDataRes *value;

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return NULL;
    }

    row = (mocaDataRow *)intRow;
    if (!row)
    {
        noRowException(env);
        return NULL;
    }

    value = sqlGetResultsetByPos(res, row, (int)colNum);

    if (value)
        return (*env)->NewObject(env, 
	                         WrappedResultsClass, 
			         WrappedResults_constructor, 
			         (jint) value, 
			         JNI_FALSE);
    else
        return NULL;
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getDefinedMaxLength
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1getDefinedMaxLength(JNIEnv *env, jobject obj, jint intRes, jint colNum)
{
    mocaDataRes *res; 

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return 0;
    }

    return sqlGetDefinedColumnLenByPos(res, (long)colNum);
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _setPointer
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1setPointer
  (JNIEnv *env, jobject obj, jint intRes, jint intRow, jint colNum, jint value)
{
    char typeCode;
    void *ptrValue = (void *)value;

    mocaDataRow *row;
    mocaDataRes *res; 

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return;
    }

    row = (mocaDataRow *)intRow;
    if (!row)
    {
        noRowException(env);
        return;
    }

    typeCode = sqlGetDataTypeByPos(res, (int)colNum);
    if (typeCode != COMTYP_GENERIC)
    {
        invalidTypeException(env);
        return;
    }

    sql_AddRowItem(res, row, (long)colNum, sizeof(void *), &ptrValue);
}


/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getPointerValue
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1getPointerValue
  (JNIEnv *env, jobject obj, jint intRes, jint intRow, jint colNum) 
{
    void **valuePtr;
    long value = 0L;

    mocaDataRes *res; 
    mocaDataRow *row;

    res = (mocaDataRes *)intRes;
    if (!res)
    {
        noResException(env);
        return 0;
    }

    row = (mocaDataRow *)intRow;
    if (!row)
    {
        noRowException(env);
        return 0;
    }

    valuePtr = (void **)sqlGetValueByPos(res, row, (long)colNum);

    if (valuePtr != NULL)
    {
	value = (long)(*valuePtr);
    }

    return (jint) value;
}


/*
 * Function to get a mocaDataRes pointer from an object reference.  This must
 * be used with great care.
 */
mocaDataRes *jni_ResultsValue(JNIEnv *env, jobject obj)
{
    return getRes(env, obj);
}

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getEncoding
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_sam_moca_server_legacy_WrappedResults__1getEncoding(JNIEnv *env, jobject obj) 
{
    return jni_Charset(env);
}
