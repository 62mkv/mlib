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

#ifndef JAVAPRIVATE_H
#define JAVAPRIVATE_H

#include <jni.h>

#include <common.h>
#include <srvlib.h>

#include <jni.h>

#ifdef MAIN
#  define STORAGE_CLASS
#else
#  define STORAGE_CLASS extern
#endif

/*
 * Globals
 */

STORAGE_CLASS jstring gCharset;
STORAGE_CLASS jboolean gTraceEnabled;

/*
 * Function Prototypes
 */

/* argmap.c */
jobject jni_ArgMap(JNIEnv *env, mocaBindList *bind);

/* executeCOM.c */
#if defined (__cplusplus)
extern "C" {
#endif
RETURN_STRUCT *jni_ExecuteCOM(char *progid, 
	                      char *method,
	                      int argCount, 
			      char argTypes[], 
			      void *args[]);
#if defined (__cplusplus)
}
#endif

/* extract.c */
long jni_ExtractResults(JNIEnv *env, jobject jRes, mocaDataRes **res);
long jni_ExtractReturnStruct(JNIEnv *env, jobject jRes, RETURN_STRUCT **ret);
long jni_ExtractNativeReturnStruct(JNIEnv *env, jobject jRetStruct, RETURN_STRUCT **ret, jobject *outBindList);

/* initialize.c */
long jni_Initialize(JNIEnv *env);

/* jnilib.c */
#if defined (__cplusplus)
extern "C" {
#endif

    JNIEnv *jni_GetEnv(int *needToDetach);
    void    jni_ReleaseEnv(void);

#if defined (__cplusplus)
}
#endif

void jni_SetAppName(char *appName);
long jni_CheckForErrors(JNIEnv *env);

jobject jni_NewBoolean(JNIEnv *env, int value);
jobject jni_NewDouble(JNIEnv *env, double value);
jobject jni_NewInteger(JNIEnv *env, int value);
jobject jni_NewPointer(JNIEnv *env, void *value);

int    jni_BooleanValue(JNIEnv *env, jobject obj);
double jni_DoubleValue(JNIEnv *env, jobject obj);
int    jni_IntValue(JNIEnv *env, jobject obj);
long   jni_LongValue(JNIEnv *env, jobject obj);
void  *jni_PointerValue(JNIEnv *env, jobject obj);

/* native.c */
void jni_AddAccumulatedArg(void *ptr, void (*destructor)(void *));

/* oscalls.c */
void jni_AddToVarList(char *ptr);
void jni_FreeVarList(void);

/* results.c */
mocaObjectRef *jni_ObjectRefValue(JNIEnv *env, jobject obj);

mocaDataRes *jni_ResultsValue(JNIEnv *env, jobject obj);

#endif
