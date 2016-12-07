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
 *  Copyright (c) 2005-2009
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
#include <stdarg.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <jnilib.h>
#include <mislib.h>
#include <oslib.h>
#include <sqllib.h>

#ifdef WIN32
#include <io.h>
#include <fcntl.h>
#include <windows.h>
#endif

#include "jniprivate.h"
#include "jnidefs.h"

#define CREATE_JVM_SYM "JNI_CreateJavaVM"

static JavaVM *jvm;
static JNIEnv *sEnv;
static char sAppName[256];

static jint JNICALL sLocalPrintf(FILE *fptr, char *format, va_list args)
{
    return vfprintf(stdout, format, args);
}

static void sAppendClasspath(MIS_DYNBUF *buffer, char *pathname)
{
    /* Apend the jar file to the buffer. */
    misDynBufAppendString(buffer, pathname);
    misDynBufAppendString(buffer, PATH_LIST_SEPARATOR_STR);
}

static void sAppendWildcardClasspath(MIS_DYNBUF *buffer, char *pathname)
{
    char found[2048];

    OS_FF_CONTEXT ctxt = 0;

    /* Find every file in the given directory. */
    while (osFindFile(pathname, found, &ctxt, OS_FF_NOSORT) == eOK)
    {
        char *foundpath;

        /* Skip it if it isn't a jar file. */
        if ((strstr(found, ".jar") == 0) && (strstr(found, ".JAR") == 0))
            continue;

        /* Build the file pathname of the jar file. */
        foundpath = misStrReplace(pathname, "*", found);

        /* Apend the jar file to the buffer. */
        misDynBufAppendString(buffer, foundpath);
        misDynBufAppendString(buffer, PATH_LIST_SEPARATOR_STR);

        free(foundpath);
    }

    osEndFindFile(&ctxt);
}

static void sShutdownVM(void)
{
    if (jvm)
    {
        (*jvm)->DestroyJavaVM(jvm);
    }
}

static char *sBuildClasspath(void)
{
    char *ptr       = NULL,
         *temp      = NULL,
         *pathname  = NULL,
         *classpath = NULL;

    MIS_DYNBUF *buffer;

    /* Get the CLASSPATH from the environment. */
    classpath = getenv("CLASSPATH");

    /* Make a copy of the CLASSPATH that we can play with. */
    misDynStrcpy(&temp, classpath);

    /* Handle debugging. */
    if (getenv("_JAVA_LAUNCHER_DEBUG") != 0)
    {
        printf("Original CLASSPATH\n");
        printf("%s\n", temp);
    }

    /* Initialize the dynamic buffer. */
    buffer = misDynBufInit(0);

    misDynBufAppendString(buffer, "-Djava.class.path=");

    ptr = temp;

    /* Cycle through every pathname in the CLASSPATH. */
    while ((pathname = misStrsep(&ptr, PATH_LIST_SEPARATOR_STR)) != NULL)
    {
        /* Skip over empty pathnames. */
        if (strlen(pathname) == 0)
            continue;

        /* Append pathnames to the CLASSPATH. */
        if (strchr(pathname, '*'))
            sAppendWildcardClasspath(buffer, pathname);
        else
            sAppendClasspath(buffer, pathname);
    }

    /* Get the CLASSPATH from the buffer. */
    classpath = misDynBufClose(buffer);

    /* Handle debugging. */
    if (getenv("_JAVA_LAUNCHER_DEBUG") != 0)
    {
        printf("Expanded CLASSPATH\n");
        printf("%s\n", classpath);
    }

    free(temp);

    return classpath;
}

void jni_SetAppName(char *appName)
{
   strncpy(sAppName, appName, sizeof(sAppName) - 1);
}

JNIEnv *jniGetEnv(void)
{
    int needToDetach;

    return jni_GetEnv(&needToDetach);
}

void jni_ReleaseEnv(void)
{
    if (!jvm)
        return;

    (*jvm)->DetachCurrentThread(jvm);
}

#define JVM_MAX_ARGS 32
JNIEnv *jni_GetEnv(int *needToDetach)
{
    JNIEnv *env = NULL;

    *needToDetach = 0;

    if (!jvm)
    {
        /* Initialize JVM if not already available */
        jint jstatus;
        JavaVMInitArgs vm_args;
        JavaVMOption options[JVM_MAX_ARGS];
        int nopts = 0;
        jint (*newvm)(JavaVM **, void **, void *);
#ifndef STATIC_JAVA_VM
        char *libname;
        void *libhandle;
#endif
        char *classpath = NULL;
        char *libpath = NULL;
        char *optionString = NULL;
        char *envVMArgs = osGetVar(ENV_JAVA_VMARGS);

        options[nopts].optionString = "vfprintf";
        options[nopts++].extraInfo = (void *)sLocalPrintf;

        classpath = sBuildClasspath( );

        options[nopts++].optionString = classpath;

#ifdef WIN32
        /*
         * We need to reduce the signal set for versions of
         * Windows prior to Windows 7 and Windows Server 2008
         * so we don't exit on console logout events.
         */
        {
        OSVERSIONINFOEX lpVersionInfo;

        ZeroMemory(&lpVersionInfo, sizeof(OSVERSIONINFOEX));
        lpVersionInfo.dwOSVersionInfoSize = sizeof(OSVERSIONINFOEX);

        if (GetVersionEx((OSVERSIONINFOEX *) &lpVersionInfo)) 
        {
            if (lpVersionInfo.dwMajorVersion < 6)
                options[nopts++].optionString = "-Xrs";
        }
        }
#endif

        if (!envVMArgs && sAppName[0])
	{
	    char appKey[512];
	    sprintf(appKey, "%s.%s", REGKEY_JAVA_VMARGS, sAppName);
	    envVMArgs = osGetRegistryValue(REGSEC_JAVA, appKey);
	}

        if (!envVMArgs) 
	{
	    envVMArgs = osGetRegistryValueNotExpanded(REGSEC_JAVA, REGKEY_JAVA_VMARGS32);
	}

        /* Use the regular vm args if app name or 32 isn't supplied */
        if (!envVMArgs)
	{
	    envVMArgs = osGetRegistryValueNotExpanded(REGSEC_JAVA, REGKEY_JAVA_VMARGS);
	}

        if (envVMArgs)
	{
            char *bufptr;
            char *arg = NULL;

            optionString = malloc(strlen(envVMArgs) + 1);
            strcpy(optionString, envVMArgs);
            bufptr = optionString;

            while (nopts < JVM_MAX_ARGS && (arg = misParseArgument(&bufptr)))
            {
                if (*arg != '\0')
		{
                    options[nopts++].optionString = arg;
		    misFlagCachedMemory(NULL, arg);
		}    
            }
	}

        /*
         * As of now, we're only supporting JNI version 1.2 and higher.
         * Since that version, the JNI spec has solidified the meaning of
         * the third argument to JNI_CreateJavaVM.
         */
        vm_args.version = JNI_VERSION_1_2;
        vm_args.options = options;
        vm_args.nOptions = nopts;
        vm_args.ignoreUnrecognized = JNI_TRUE;

#ifndef STATIC_JAVA_VM
        libname = osGetVar(ENV_PREFIX "JAVA_VM");
        if (libname == NULL || misCiStrcmp(libname, "none") == 0)
        {
#ifdef WIN32
            libname = "jvm";
#else
            libname = "libjvm";
#endif
        }

        libhandle = osLibraryOpen(libname);
        if (!libhandle)
        {
            fprintf(stderr, "Can't find JVM shared library: %s\n", 
			    osLibraryError());
	    goto cleanup;
        }

        newvm = (jint (*)(JavaVM **, void **, void *))osLibraryLookupFunction(libhandle, CREATE_JVM_SYM);

        if (!newvm)
        {
            fprintf(stderr, "Can't find CreateJavaVM function: %s\n", 
			    osError());
	    goto cleanup;
        }

#else
        newvm = JNI_CreateJavaVM;
#endif

        jstatus = (*newvm)(&jvm, (void **)&env, &vm_args);
        if (jstatus < 0)
        {
            fprintf(stderr, "Unable to create Java VM: %s\n", osError());
	    goto cleanup;
        }

        osAtexitClean(sShutdownVM);

#ifdef WIN32
        _setmode(_fileno(stdout), _O_TEXT);
        _setmode(_fileno(stderr), _O_TEXT);
#endif
       
cleanup:	
        free(classpath);
        free(libpath);
        free(optionString);
    }
    else
    {
        jint getEnvStatus;
        getEnvStatus = (*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_2);
        if (getEnvStatus == JNI_EDETACHED)
        {
            JavaVMAttachArgs thread_args;
            thread_args.version = JNI_VERSION_1_2;
            thread_args.name = "subthread";
            thread_args.group = NULL;
               
            *needToDetach = 1;
 
            (*jvm)->AttachCurrentThread(jvm, (void **)&env, &thread_args);
        }
    }

    return env;
}

JNIEnv *jniSetEnv(JNIEnv *env)
{
    JNIEnv *tmp = sEnv;
    sEnv = env;
    if (!jvm)
    {
        (*env)->GetJavaVM(env, &jvm);
    }

    return tmp;
}

/*
 * Take the value of a java Boolean wrapper object.
 */
int jni_BooleanValue(JNIEnv *env, jobject obj)
{
    return (int) (*env)->CallBooleanMethod(env, obj, Boolean_booleanValue);
}

/*
 * Take the value of a java Integer wrapper object.
 */
int jni_IntValue(JNIEnv *env, jobject obj)
{
    return (*env)->CallIntMethod(env, obj, Integer_intValue);
}

/*
 * Take the value of a MOCA Pointer object.
 */
void *jni_PointerValue(JNIEnv *env, jobject obj)
{
    if ((*env)->IsInstanceOf(env, obj, GenericPointerClass)) 
    {
	return (void *)(*env)->CallIntMethod(env, obj, GenericPointer_32bitValue);
    }

    return NULL;
}

/*
 * Take the value of a java Long wrapper object.
 */
long jni_LongValue(JNIEnv *env, jobject obj)
{
    return (long) (*env)->CallLongMethod(env, obj, Long_longValue);
}

/*
 * Take the value of a java Double wrapper object.
 */
double jni_DoubleValue(JNIEnv *env, jobject obj)
{
    return (*env)->CallDoubleMethod(env, obj, Double_doubleValue);
}

/*
 * Remove global reference to java object.  This function is intended to be
 * passed as a pointer to a call to sql_ObjectRef.
 */
static void removeRef(void *ref)
{
    JNIEnv *env = jniGetEnv();
    (*env)->DeleteGlobalRef(env, (jobject)ref);
}

/*
 * Get a MOCA-friendly object reference "object" from a raw java object
 * reference.  Wow, that sentence barely makes sense.
 */
mocaObjectRef *jni_ObjectRefValue(JNIEnv *env, jobject obj)
{
    jobject ref = (*env)->NewGlobalRef(env, obj);
    return sql_ObjectRef(ref, removeRef);
}

/*
 * Create a new wrapper object for java.lang.Boolean
 */
jobject jni_NewBoolean(JNIEnv *env, int value)
{
    return (*env)->NewObject(env, BooleanClass, Boolean_constructor, value);
}

/*
 * Create a new wrapper object for java.lang.Integer
 */
jobject jni_NewInteger(JNIEnv *env, int value)
{
    return (*env)->NewObject(env, IntegerClass, Integer_constructor, value);
}

/*
 * Create a new wrapper object for java.lang.Double
 */
jobject jni_NewDouble(JNIEnv *env, double value)
{
    return (*env)->NewObject(env, DoubleClass, Double_constructor, value);
}

jobject jni_NewPointer(JNIEnv *env, void *value)
{
    return (*env)->NewObject(env, GenericPointerClass, GenericPointer_constructor, (jint)value);
}

/*
 * Create a new wrapper object for java.lang.String
 */
jstring jniNewStringFromBytes(JNIEnv *env, char *value)
{
    if (!value) return NULL;
    return (*env)->NewStringUTF(env, value);
}

/*
 * Returns the result of the "toString" method on any object.
 * The string returned from this function must be freed.
 */
char *jniToString(JNIEnv *env, jobject obj)
{
    jstring objString = NULL;
    const char *utfString = NULL;
    char *result = NULL;

    jni_Initialize(env);

    /*
     * Call Object.toString().  It is possible that we had trouble
     * looking up the java.lang.Object class, so this method may not
     * be available.
     */
    if (Object_toString)
    {
        objString = (jstring) (*env)->CallObjectMethod(env, 
		                                       obj, 
						       Object_toString);
        utfString = (*env)->GetStringUTFChars(env, objString, NULL);
        misDynStrcpy(&result, (char *)utfString);
        (*env)->ReleaseStringUTFChars(env, objString, utfString);
        (*env)->DeleteLocalRef(env, objString);
    }
    else
    {
        misDynStrcpy(&result, "(Error)");
    }

    return result;
}

/*
 * Returns the result of the "toString" method on any object.
 * The string returned from this function must be freed.
 */
char *jniDecodeString(JNIEnv *env, jstring str, int *length)
{
    char *result = NULL;
    jbyteArray array;
    char *bytes;
    int tmplength;

    /* cannot call CallObjectMethod with a null object ...will abort VM */
    if (str == NULL)
    {
        if (length)
        {
            *length =0;
        }
        return NULL;
    }

    jni_Initialize(env);
    
    /*
     * Call String.getBytes().  It is possible that we had trouble
     * looking up the java.lang.String class, so this method may not
     * be available.
     */
    if (String_getBytes)
    {
        array = (jbyteArray) (*env)->CallObjectMethod(env, 
			                              str,
                                                      String_getBytes, 
						      gCharset);

        tmplength = (*env)->GetArrayLength(env, array);
        bytes = (char *)(*env)->GetByteArrayElements(env, array, NULL);
        result = malloc(tmplength + 1);

        /* If the getBytes call fails, don't blow up */
        if (bytes)
        {
            memcpy(result, bytes, tmplength);
        }

        result[tmplength] = '\0';
        (*env)->ReleaseByteArrayElements(env, array, (jbyte *)bytes, 0);
        (*env)->DeleteLocalRef(env, array);

        if (length)
        {
            *length = tmplength;
        }
    }
    else
    {
        misDynStrcpy(&result, "(Error)");
        if (length)
        {
          *length = strlen(result);
        }
    }

    return result;
}


jstring jni_Charset(JNIEnv *env)
{
    return gCharset;
}

/*
 * Check for any exceptions.  If no Exception occurred, return eOK.  Otherwise,
 * write the message to the log and return a generic error code.
 */
long jni_CheckForErrors(JNIEnv *env)
{
    long status = eOK;
    jthrowable exc = NULL;

    exc = (*env)->ExceptionOccurred(env);

    if (exc)
    {
        char *errorText = NULL;

        /*
         * We're handling the exception, so clear it from our current JVM
         * thread context.  This helps with other calls that might fail
         * if the thread is in an exception condition. (e.g. toString or
         * getMessage)
         */
        (*env)->ExceptionClear(env);

        errorText = jniToString(env, exc);
        misLogError("Java Error: %s", errorText);
        if (errorText) free(errorText);
        (*env)->DeleteLocalRef(env, exc);

        /*
         * Something bad happened.  Let's get the exception as a
         * string and just call this a generic DB error.
         */
        status = eERROR;
    }

    return status;
}
