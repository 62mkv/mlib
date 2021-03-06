/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_redprairie_moca_server_legacy_WrappedResults */

#ifndef _Included_com_redprairie_moca_server_legacy_WrappedResults
#define _Included_com_redprairie_moca_server_legacy_WrappedResults
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _newResults
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1newResults
  (JNIEnv *, jobject);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getEncoding
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1getEncoding
  (JNIEnv *, jobject);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _dispose
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1dispose
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _addRow
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1addRow
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _addColumn
 * Signature: (ILjava/lang/String;CIZ)V
 */
JNIEXPORT void JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1addColumn
  (JNIEnv *, jobject, jint, jstring, jchar, jint, jboolean);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _setObjectValue
 * Signature: (IIILjava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1setObjectValue
  (JNIEnv *, jobject, jint, jint, jint, jobject);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _setInt
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1setInt
  (JNIEnv *, jobject, jint, jint, jint, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _setDouble
 * Signature: (IIID)V
 */
JNIEXPORT void JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1setDouble
  (JNIEnv *, jobject, jint, jint, jint, jdouble);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _setBinary
 * Signature: (III[B)V
 */
JNIEXPORT void JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1setBinary
  (JNIEnv *, jobject, jint, jint, jint, jbyteArray);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _setBoolean
 * Signature: (IIIZ)V
 */
JNIEXPORT void JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1setBoolean
  (JNIEnv *, jobject, jint, jint, jint, jboolean);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _setString
 * Signature: (III[B)V
 */
JNIEXPORT void JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1setString
  (JNIEnv *, jobject, jint, jint, jint, jbyteArray);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _setResults
 * Signature: (IIILcom/redprairie/moca/server/legacy/WrappedResults;)V
 */
JNIEXPORT void JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1setResults
  (JNIEnv *, jobject, jint, jint, jint, jobject);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _setPointer
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1setPointer
  (JNIEnv *, jobject, jint, jint, jint, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _setNull
 * Signature: (IIIZ)V
 */
JNIEXPORT void JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1setNull
  (JNIEnv *, jobject, jint, jint, jint, jboolean);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _firstRow
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1firstRow
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _nextRow
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1nextRow
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _removeRow
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1removeRow
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _hasNextRow
 * Signature: (IIZ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1hasNextRow
  (JNIEnv *, jobject, jint, jint, jboolean);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getStringValue
 * Signature: (III)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1getStringValue
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getIntValue
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1getIntValue
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getDoubleValue
 * Signature: (III)D
 */
JNIEXPORT jdouble JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1getDoubleValue
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getBooleanValue
 * Signature: (III)Z
 */
JNIEXPORT jboolean JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1getBooleanValue
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getBinaryValue
 * Signature: (III)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1getBinaryValue
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getObjectValue
 * Signature: (III)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1getObjectValue
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getResultsValue
 * Signature: (III)Lcom/redprairie/moca/server/legacy/WrappedResults;
 */
JNIEXPORT jobject JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1getResultsValue
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getPointerValue
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1getPointerValue
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _isNull
 * Signature: (III)Z
 */
JNIEXPORT jboolean JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1isNull
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getDataTypeCode
 * Signature: (II)C
 */
JNIEXPORT jchar JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1getDataTypeCode
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _isNullable
 * Signature: (II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1isNullable
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getColumnNum
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1getColumnNum
  (JNIEnv *, jobject, jint, jstring);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getColumnName
 * Signature: (II)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1getColumnName
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getColumnCount
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1getColumnCount
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getRowCount
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1getRowCount
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _getDefinedMaxLength
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1getDefinedMaxLength
  (JNIEnv *, jobject, jint, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_WrappedResults
 * Method:    _initIDs
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_redprairie_moca_server_legacy_WrappedResults__1initIDs
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
