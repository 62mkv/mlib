/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file for jnilib.
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

#ifndef JNILIB_H
#define JNILIB_H

#include <jni.h>
#include <srvlib.h>

/*
 *  Function Prototypes
 */
#if defined (__cplusplus)
extern "C" {
#endif

/*
 * Get and set the current JNI context.
 */

JNIEnv *jniGetEnv(void);
JNIEnv *jniSetEnv(JNIEnv *);

/* Server Adapter Interfaces */
jobject jniCreateServerAdapter(JNIEnv *env, char *name, long singleThreaded);
jobject jniGetServerAdapter(void);
jobject jniSetServerAdapter(JNIEnv *env, jobject obj);

/* Extract a results from a Java results object. */

long jniExtractResults(JNIEnv *env, jobject jRes, mocaDataRes **res);
long jniExtractReturnStruct(JNIEnv *env, jobject jRes, RETURN_STRUCT **ret);

/*
 * String routines that use the default character set
 */

char *jniDecodeString(JNIEnv *env, jstring value, int *length);
char *jniToString(JNIEnv *env, jobject obj);
jstring jniNewStringFromBytes(JNIEnv *env, char *value);

jstring jni_Charset(JNIEnv *env);

#if defined (__cplusplus)
}
#endif

#endif
