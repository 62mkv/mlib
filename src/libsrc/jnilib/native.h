/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_redprairie_moca_server_legacy_InternalNativeProcess */

#ifndef _Included_com_redprairie_moca_server_legacy_InternalNativeProcess
#define _Included_com_redprairie_moca_server_legacy_InternalNativeProcess
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _loadLibrary
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_redprairie_moca_server_legacy_InternalNativeProcess__1loadLibrary
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _initializeLibrary
 * Signature: (I)Lcom/redprairie/moca/MocaLibInfo;
 */
JNIEXPORT jobject JNICALL Java_com_redprairie_moca_server_legacy_InternalNativeProcess__1initializeLibrary
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _initializeCOMLibrary
 * Signature: (Ljava/lang/String;)Lcom/redprairie/moca/MocaLibInfo;
 */
JNIEXPORT jobject JNICALL Java_com_redprairie_moca_server_legacy_InternalNativeProcess__1initializeCOMLibrary
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _initializeAppLibrary
 * Signature: (Lcom/redprairie/moca/server/legacy/MocaServerAdapter;I)V
 */
JNIEXPORT void JNICALL Java_com_redprairie_moca_server_legacy_InternalNativeProcess__1initializeAppLibrary
  (JNIEnv *, jobject, jobject, jint);

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _findCFunction
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_redprairie_moca_server_legacy_InternalNativeProcess__1findCFunction
  (JNIEnv *, jobject, jint, jstring);

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _callCFunction
 * Signature: (Lcom/redprairie/moca/server/legacy/MocaServerAdapter;I[C[Ljava/lang/Object;ZZ)Lcom/redprairie/moca/server/legacy/NativeReturnStruct;
 */
JNIEXPORT jobject JNICALL Java_com_redprairie_moca_server_legacy_InternalNativeProcess__1callCFunction
  (JNIEnv *, jobject, jobject, jint, jcharArray, jobjectArray, jboolean, jboolean);

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _callCOMMethod
 * Signature: (Lcom/redprairie/moca/server/legacy/MocaServerAdapter;Ljava/lang/String;Ljava/lang/String;[C[Ljava/lang/Object;Z)Lcom/redprairie/moca/server/legacy/NativeReturnStruct;
 */
JNIEXPORT jobject JNICALL Java_com_redprairie_moca_server_legacy_InternalNativeProcess__1callCOMMethod
  (JNIEnv *, jobject, jobject, jstring, jstring, jcharArray, jobjectArray, jboolean);

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _preCommit
 * Signature: (Lcom/redprairie/moca/server/legacy/MocaServerAdapter;)V
 */
JNIEXPORT void JNICALL Java_com_redprairie_moca_server_legacy_InternalNativeProcess__1preCommit
  (JNIEnv *, jobject, jobject);

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _postTransaction
 * Signature: (Lcom/redprairie/moca/server/legacy/MocaServerAdapter;Z)V
 */
JNIEXPORT void JNICALL Java_com_redprairie_moca_server_legacy_InternalNativeProcess__1postTransaction
  (JNIEnv *, jobject, jobject, jboolean);

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _getKeepaliveCounter
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_redprairie_moca_server_legacy_InternalNativeProcess__1getKeepaliveCounter
  (JNIEnv *, jobject);

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _release
 * Signature: (Lcom/redprairie/moca/server/legacy/MocaServerAdapter;)V
 */
JNIEXPORT void JNICALL Java_com_redprairie_moca_server_legacy_InternalNativeProcess__1release
  (JNIEnv *, jobject, jobject);

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _setEnvironment
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_redprairie_moca_server_legacy_InternalNativeProcess__1setEnvironment
  (JNIEnv *, jobject, jstring, jstring);

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _initIDs
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_redprairie_moca_server_legacy_InternalNativeProcess__1initIDs
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
/* Header for class com_redprairie_moca_server_legacy_InternalNativeProcess__LibraryDescriptor */

#ifndef _Included_com_redprairie_moca_server_legacy_InternalNativeProcess__LibraryDescriptor
#define _Included_com_redprairie_moca_server_legacy_InternalNativeProcess__LibraryDescriptor
#ifdef __cplusplus
extern "C" {
#endif
#ifdef __cplusplus
}
#endif
#endif