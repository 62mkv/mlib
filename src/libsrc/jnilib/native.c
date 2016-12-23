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
#include <mislib.h>
#include <sqllib.h>
#include <srvlib.h>
#include <oslib.h>

#include "jniprivate.h"
#include "../oslib/osprivate.h"
#include "../srvlib/srvprivate.h"
#include "jnidefs.h"
#include "native.h"
#include "dbcalls.h"

#define LOGLEVEL_ERROR       1
#define LOGLEVEL_WARNING     2
#define LOGLEVEL_INFO        3
#define LOGLEVEL_DEBUG       4
#define LOGLEVEL_UPDATE      5

typedef struct
{
    void *ptr;
} ARGREF;

static MIS_DYNBUF *sAccumulatedArgs;

static jobject sWrapResults(JNIEnv *env, RETURN_STRUCT *srvres)
{
    long status;
    jobject ret = NULL;

    if (srvres)
    {
        status = srvres->Error.Code;
    }
    else
    {
        status = eERROR;
        srvres = srvResults(status, NULL);
    }

    if (status == eOK)
    {
        mocaDataRes *res;

        res = srvGetResults(srvres);
        if (res != NULL)
        {
            jobject resultSet;

            srvres->ReturnedData = NULL;
            resultSet = (*env)->NewObject(env, 
                                          WrappedResultsClass, 
                                          WrappedResults_constructor, 
                                          (jint) res, 
                                          JNI_TRUE);

            ret = (*env)->NewObject(env,  
                                    NativeReturnStructClass, 
                                    NativeReturnStruct_resultsConstructor, 
                                    resultSet);
	    }
    }
    else
    {
        jthrowable exception;
        jstring message;
        SRV_ERROR_ARG *tmpArg;
        mocaDataRes *res;
        jboolean isResolved=JNI_FALSE;
        char *messageSource = srvres->Error.DefaultText;

        res = srvGetResults(srvres);

        /* Grab the pre-formatted message, if available. */
        if (res && res->Message && strlen(res->Message))
        {
            messageSource = res->Message;
            isResolved = JNI_TRUE;
        }

        /* 
         * If no message is available, and the error code matches the
         * db error code, pass along the db error text.
         */
        if (!messageSource && status == jni_dbErrorNumber())
        {
            messageSource = jni_dbErrorText();
        }

        /* If a result set exists and has any columns, pass it along. */
        if (res != NULL && sqlGetNumColumns(res) != 0)
        {
            /* Make sure we don't double-free the low-level results object. */
            srvres->ReturnedData = NULL;
            ret = (*env)->NewObject(env, 
                                    WrappedResultsClass, 
                                    WrappedResults_constructor, 
				    (jint) res, 
				    JNI_TRUE);
        }

        message = jniNewStringFromBytes(env, messageSource);

        exception = (jthrowable) (*env)->NewObject(env,
                CommandInvocationExceptionClass, 
		CommandInvocationException_constructor,
                status, 
		message, 
                isResolved,
		ret);
        
        for (tmpArg = srvres->Error.Args; tmpArg; tmpArg = tmpArg->next)
        {
            jobject arg = NULL;
            jstring argName = (*env)->NewStringUTF(env, tmpArg->varnam);

            switch (tmpArg->type)
            {
            case COMTYP_INT:
            case COMTYP_LONG:
            case COMTYP_LONGPTR:
                arg = jni_NewInteger(env, tmpArg->data.ldata); 
                break;
            case COMTYP_BOOLEAN:
                arg = jni_NewBoolean(env, tmpArg->data.ldata);
                break;
            case COMTYP_FLOAT:
            case COMTYP_FLOATPTR:
                arg = jni_NewDouble(env, tmpArg->data.fdata);
                break;
            case COMTYP_STRING:
            case COMTYP_CHARPTR:
            case COMTYP_DATTIM:
                arg = jniNewStringFromBytes(env, tmpArg->data.cdata);
                break;
            default:
                arg = NULL;
                break;
            }

            if (tmpArg->lookup)
            {
                (*env)->CallVoidMethod(env, 
		                       exception, 
				       CommandInvocationException_addLookupArg, 
                                       argName, 
				       arg);
            }
            else
            {
                (*env)->CallVoidMethod(env, 
		                       exception, 
				       CommandInvocationException_addArg, 
                                       argName, 
				       arg);
            }
        }

        ret = (*env)->NewObject(env,
                                NativeReturnStructClass, 
                                NativeReturnStruct_exceptionConstructor, 
                                exception);
    }

    srvFreeMemory(SRVRET_STRUCT, srvres);

    return ret;
}

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _initIDs
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_sam_moca_server_legacy_InternalNativeProcess__1initIDs
  (JNIEnv *env, jclass cls)
{
    long status;

    /* Initialize class and method ids. */
    status = jni_Initialize(env);
    if (status != eOK)
        return;
}

static RETURN_STRUCT *sExecuteCFunction(void (*fptr)(), 
	                                void *args[], 
					int simple)
{
    RETURN_STRUCT *ret;

    /* Make sure we have a function pointer. */
    if (!fptr)
    {
	/* This should be caught in the Java layer */
	return srvErrorResults(eSRV_INVALID_FUNCTION,
		"Invalid function (^function^)",
		"function", COMTYP_STRING, "???", 0,
		NULL);
    }

    /* Actually execute the C function. */
    if (simple)
    {
        long (*function)() = (long (*)())fptr;

        ret = srvResults((*function)(
		 args[0], args[1], args[2], args[3], args[4],
		 args[5], args[6], args[7], args[8], args[9],
		 args[10], args[11], args[12], args[13], args[14],
		 args[15], args[16], args[17], args[18], args[19],
		 args[20], args[21], args[22], args[23], args[24],
		 args[25], args[26], args[27], args[28], args[29],
		 args[30], args[31], args[32], args[33], args[34],
		 args[35], args[36], args[37], args[38], args[39],
		 args[40], args[41], args[42], args[43], args[44],
		 args[45], args[46], args[47], args[48], args[49],
		 args[50], args[51], args[52], args[53], args[54],
		 args[55], args[56], args[57], args[58], args[59],
		 args[60], args[61], args[62], args[63], args[64],
		 args[65], args[66], args[67], args[68], args[69],
		 args[70], args[71], args[72], args[73], args[74],
		 args[75], args[76], args[77], args[78], args[79],
		 args[80], args[81], args[82], args[83], args[84],
		 args[85], args[86], args[87], args[88], args[89],
		 args[90], args[91], args[92], args[93], args[94],
		 args[95], args[96], args[97], args[98], args[99],
		 args[100], args[101], args[102], args[103], args[104],
		 args[105], args[106], args[107], args[108], args[109],
		 args[110], args[111], args[112], args[113], args[114],
		 args[115], args[116], args[117], args[118], args[119],
		 args[120], args[121], args[122], args[123], args[124],
		 args[125], args[126], args[127], args[128], args[129],
		 args[130], args[131], args[132], args[133], args[134],
		 args[135], args[136], args[137], args[138], args[139],
		 args[140], args[141], args[142], args[143], args[144],
		 args[145], args[146], args[147], args[148], args[149],
		 args[150], args[151], args[152], args[153], args[154],
		 args[155], args[156], args[157], args[158], args[159],
		 args[160], args[161], args[162], args[163], args[164],
		 args[165], args[166], args[167], args[168], args[169],
		 args[170], args[171], args[172], args[173], args[174],
		 args[175], args[176], args[177], args[178], args[179],
		 args[180], args[181], args[182], args[183], args[184],
		 args[185], args[186], args[187], args[188], args[189],
		 args[190], args[191], args[192], args[193], args[194],
		 args[195], args[196], args[197], args[198], args[199]), NULL);
    }
    else
    {
        RETURN_STRUCT *(*function)() = (RETURN_STRUCT *(*)())fptr;

        ret = (*function)(
		 args[0], args[1], args[2], args[3], args[4],
		 args[5], args[6], args[7], args[8], args[9],
		 args[10], args[11], args[12], args[13], args[14],
		 args[15], args[16], args[17], args[18], args[19],
		 args[20], args[21], args[22], args[23], args[24],
		 args[25], args[26], args[27], args[28], args[29],
		 args[30], args[31], args[32], args[33], args[34],
		 args[35], args[36], args[37], args[38], args[39],
		 args[40], args[41], args[42], args[43], args[44],
		 args[45], args[46], args[47], args[48], args[49],
		 args[50], args[51], args[52], args[53], args[54],
		 args[55], args[56], args[57], args[58], args[59],
		 args[60], args[61], args[62], args[63], args[64],
		 args[65], args[66], args[67], args[68], args[69],
		 args[70], args[71], args[72], args[73], args[74],
		 args[75], args[76], args[77], args[78], args[79],
		 args[80], args[81], args[82], args[83], args[84],
		 args[85], args[86], args[87], args[88], args[89],
		 args[90], args[91], args[92], args[93], args[94],
		 args[95], args[96], args[97], args[98], args[99],
		 args[100], args[101], args[102], args[103], args[104],
		 args[105], args[106], args[107], args[108], args[109],
		 args[110], args[111], args[112], args[113], args[114],
		 args[115], args[116], args[117], args[118], args[119],
		 args[120], args[121], args[122], args[123], args[124],
		 args[125], args[126], args[127], args[128], args[129],
		 args[130], args[131], args[132], args[133], args[134],
		 args[135], args[136], args[137], args[138], args[139],
		 args[140], args[141], args[142], args[143], args[144],
		 args[145], args[146], args[147], args[148], args[149],
		 args[150], args[151], args[152], args[153], args[154],
		 args[155], args[156], args[157], args[158], args[159],
		 args[160], args[161], args[162], args[163], args[164],
		 args[165], args[166], args[167], args[168], args[169],
		 args[170], args[171], args[172], args[173], args[174],
		 args[175], args[176], args[177], args[178], args[179],
		 args[180], args[181], args[182], args[183], args[184],
		 args[185], args[186], args[187], args[188], args[189],
		 args[190], args[191], args[192], args[193], args[194],
		 args[195], args[196], args[197], args[198], args[199]);
    }

    return ret;
}

struct arglist
{
    void *data;
    void (*destructor)(void *);
};

static void sFreeAccumulatedArgs()
{
    struct arglist *ptr;
    int size;
    int ii;

    size = misDynBufGetSize(sAccumulatedArgs)/sizeof(struct arglist);
    ptr = (struct arglist *)misDynBufClose(sAccumulatedArgs);
    sAccumulatedArgs = NULL;

    for (ii = 0; ii < size; ii++)
    {
        if (ptr[ii].data)
        {
            if (ptr[ii].destructor) 
            {
                ptr[ii].destructor((ptr[ii].data));
            }
            else
            {
                free(ptr[ii].data);
            }
        }
    }

    if (ptr) free(ptr);
}

void jni_AddAccumulatedArg(void *ptr, void (*destructor)(void *))
{
    struct arglist tmp;
    tmp.data = ptr;
    tmp.destructor = destructor;
    misDynBufAppendBytes(sAccumulatedArgs, &tmp, sizeof(struct arglist));
}

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _callCFunction
 * Signature: (J[Ljava/lang/Object;ZZ)Lcom/redprairie/moca/server/legacy/NativeReturnStruct;
 */
JNIEXPORT jobject JNICALL Java_com_sam_moca_server_legacy_InternalNativeProcess__1callCFunction
  (JNIEnv *env, jobject obj, jobject server, jint function, jcharArray types, jobjectArray args, jboolean simpleFunction, jboolean traceEnabled)
{
    RETURN_STRUCT *srvres = NULL;
    long status = eOK;
    jobject ret = NULL;
    JNIEnv *tmpEnv;
    jobject saveAdapter;
    void (*fptr)() = (void (*)())function;
    void *nativeArgs[200];
    MIS_DYNBUF *saveAccumulatedArgs;
    jsize len;
    int ii;
    int simple = 0;
    jchar *typeCodes;

    tmpEnv = jniSetEnv(env);
    
    saveAdapter = jniSetServerAdapter(env, server);

    gTraceEnabled = traceEnabled;

    saveAccumulatedArgs = sAccumulatedArgs;
    sAccumulatedArgs = misDynBufInit(0);

    /* Fill native Args array with function arguments as passed */
    memset(nativeArgs, 0, sizeof nativeArgs);
    typeCodes = (*env)->GetCharArrayElements(env, types, NULL);

    len = (*env)->GetArrayLength(env, args);
    for (ii = 0; ii < len; ii++)
    {
        jobject objValue;

        objValue = (*env)->GetObjectArrayElement(env, args, ii);

        if (objValue)
        {
            switch(typeCodes[ii]) {
            case COMTYP_BOOLEAN:
                nativeArgs[ii] = malloc(sizeof(int));
                *((int *)nativeArgs[ii]) = jni_BooleanValue(env, objValue);
                break;
            case COMTYP_INT:
                nativeArgs[ii] = malloc(sizeof(long));
                *((long *)nativeArgs[ii]) = jni_IntValue(env, objValue);
                break;
            case COMTYP_FLOAT:
                nativeArgs[ii] = malloc(sizeof(double));
                *((double *)nativeArgs[ii]) = jni_DoubleValue(env, objValue);
                break;
            case COMTYP_CHAR:
                nativeArgs[ii] = jniToString(env, objValue);
                break;
            case COMTYP_JAVAOBJ:
                break;
            case COMTYP_GENERIC:
                nativeArgs[ii] = malloc(sizeof(void *));
                *((void **)nativeArgs[ii]) = (void *)jni_PointerValue(env, objValue);
                break;
            case COMTYP_RESULTS:
                nativeArgs[ii] = malloc(sizeof(mocaDataRes *));

                *(mocaDataRes **) nativeArgs[ii] = jni_ResultsValue(env, objValue);
                break;
            }
        }
    }

    (*env)->ReleaseCharArrayElements(env, types, typeCodes, JNI_ABORT);

    /* 
     * If the function being called is simple then make sure to call it 
     * assuming that
     */
    if (simpleFunction == JNI_TRUE) 
    {
        simple = 1;
    }

    srvres = sExecuteCFunction(fptr, nativeArgs, simple);

    /* Free the native arguments */
    for (ii = 0; ii < len; ii++)
    {
        if (nativeArgs[ii] != NULL) free(nativeArgs[ii]);
    }

    sFreeAccumulatedArgs();

    ret = sWrapResults(env, srvres);

    jniSetServerAdapter(env, saveAdapter);
    jniSetEnv(tmpEnv);

    sAccumulatedArgs = saveAccumulatedArgs;

    return ret;
}

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _loadLibrary
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jint JNICALL Java_com_sam_moca_server_legacy_InternalNativeProcess__1loadLibrary
  (JNIEnv *env, jobject obj, jstring libname)
{
    void *handle;
    const char *libnameUTF;

    libnameUTF = (*env)->GetStringUTFChars(env, libname, NULL);
    handle = osLibraryOpen((char *)libnameUTF);

    if (!handle)
    {
        jthrowable exception;
        jstring functionName;
        jstring errorText;

        errorText = (*env)->NewStringUTF(env, osLibraryError());
        functionName = (*env)->NewStringUTF(env, "");

        exception = (jthrowable) (*env)->NewObject(env,
                MocaNativeExceptionClass, 
		MocaNativeException_constructor,
                libname,
		functionName, 
		errorText);
        (*env)->Throw(env, exception);
    }

    (*env)->ReleaseStringUTFChars(env, libname, libnameUTF);

    /* If handle is null, the caller will know based on library errors */
    return (jint)handle;
}

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _initializeLibrary
 * Signature: (I)Lcom/redprairie/moca/MocaLibInfo;
 */
JNIEXPORT jobject JNICALL Java_com_sam_moca_server_legacy_InternalNativeProcess__1initializeLibrary
  (JNIEnv *env, jobject obj, jint longHandle)
{
    void *handle;
    char *product = NULL;
    char *version = NULL;
    char *(*licenseFunction)(void);
    char *(*versionFunction)(void);
    jstring jproduct = NULL;
    jstring jversion = NULL;
    JNIEnv *tmpEnv;

    tmpEnv = jniSetEnv(env);

    handle = (void *)longHandle;

    licenseFunction = 
        (char *(*)(void))osLibraryLookupFunction(handle, "MOCAlicense");
    versionFunction = 
        (char *(*)(void))osLibraryLookupFunction(handle, "MOCAversion");

    /* Check for product name -- used for licensing */
    if (licenseFunction)
    {
        product = (*licenseFunction)( );
    }

    /* Look up library version */
    if (versionFunction)
    {
        version = (*versionFunction)( );
    }
    else
    {
        version = NULL;
    }

    if (product) 
        jproduct = (*env)->NewStringUTF(env, product);
    if (version)
        jversion = (*env)->NewStringUTF(env, version);

    jniSetEnv(tmpEnv);

    return (*env)->NewObject(env, 
	                     MocaLibInfoClass, 
			     MocaLibInfo_constructor, 
			     jversion, 
			     jproduct);
}

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _initializeAppLibrary
 * Signature: (Lcom/redprairie/moca/server/legacy/MocaServerAdapter;I)V
 */
JNIEXPORT void JNICALL Java_com_sam_moca_server_legacy_InternalNativeProcess__1initializeAppLibrary
  (JNIEnv *env, jobject obj, jobject server, jint longHandle)
{
    void *handle;
    void  (*initFunction)(void);
    JNIEnv *tmpEnv;
    jobject saveAdapter;

    tmpEnv = jniSetEnv(env);

    saveAdapter = jniSetServerAdapter(env, server);

    handle = (void *)longHandle;

    /* Look up our standard init functions */
    initFunction = 
        (void (*)(void))osLibraryLookupFunction(handle, "MOCAinitialize");

    /* Call the initialize function */
    if (initFunction)
    {
        (*initFunction)();
    }

    jniSetServerAdapter(env, saveAdapter);
    jniSetEnv(tmpEnv);
}


/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _findCFunction
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jint JNICALL Java_com_sam_moca_server_legacy_InternalNativeProcess__1findCFunction
  (JNIEnv *env, jobject obj, jint longLibHandle, jstring name)
{
    OSFPTR functionHandle;
    void *libHandle = (void *)longLibHandle;
    const char *nameUTF;
    JNIEnv *tmpEnv;

    tmpEnv = jniSetEnv(env);

    nameUTF = (*env)->GetStringUTFChars(env, name, NULL);
    functionHandle = osLibraryLookupFunction(libHandle, (char *)nameUTF);
    if (!functionHandle)
    {
        misLogWarning("Unable to load function %s: %s", nameUTF, osError());
    }

    (*env)->ReleaseStringUTFChars(env, name, nameUTF);

    jniSetEnv(tmpEnv);

    return (jint)functionHandle;
}

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _initializeCOMLibrary
 * Signature: (Ljava/lang/String;)Lcom/redprairie/moca/MocaLibInfo;
 */
JNIEXPORT jobject JNICALL Java_com_sam_moca_server_legacy_InternalNativeProcess__1initializeCOMLibrary
  (JNIEnv *env, jobject obj, jstring progid)
{
    return NULL;
}

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _callCOMMethod
 * Signature: (Ljava/lang/String;Ljava/lang/String;[C[Ljava/lang/Object;Z)Lcom/redprairie/moca/server/legacy/NativeReturnStruct;
 */
JNIEXPORT jobject JNICALL Java_com_sam_moca_server_legacy_InternalNativeProcess__1callCOMMethod
  (JNIEnv *env, jobject obj, jobject server, jstring progID, jstring methodName,
  jcharArray types, jobjectArray args, jboolean traceEnabled)
{
    RETURN_STRUCT *srvres = NULL;
    jobject ret = NULL;
#ifdef WIN32
    JNIEnv *tmpEnv;
    jobject saveAdapter;
    long status = eOK;
    void **comArgs;
    MIS_DYNBUF *saveAccumulatedArgs;
    jsize len;
    int ii;
    jchar *typeCodes;
    const char *progIDUTF;
    const char *methodNameUTF;
    char *argtypes;


    saveAccumulatedArgs = sAccumulatedArgs;
    sAccumulatedArgs = misDynBufInit(0);

    tmpEnv = jniSetEnv(env);
    saveAdapter = jniSetServerAdapter(env, server);

    gTraceEnabled = traceEnabled;
    
    /* Fill native Args array with function arguments as passed */
    typeCodes = (*env)->GetCharArrayElements(env, types, NULL);
    len = (*env)->GetArrayLength(env, args);

    comArgs = calloc(len, sizeof (void *));
    argtypes = malloc(len+1);

    for (ii = 0; ii < len; ii++)
    {
        jobject objValue;

        objValue = (*env)->GetObjectArrayElement(env, args, ii);

        if (objValue)
        {
            switch(typeCodes[ii]) {
            case COMTYP_BOOLEAN:
                argtypes[ii] = ARGTYP_FLAG;
                comArgs[ii] = malloc(sizeof(int));
                *((int *)comArgs[ii]) = jni_BooleanValue(env, objValue);
                break;
            case COMTYP_INT:
                argtypes[ii] = ARGTYP_INT;
                comArgs[ii] = malloc(sizeof(long));
                *((long *)comArgs[ii]) = jni_IntValue(env, objValue);
                break;
            case COMTYP_FLOAT:
                argtypes[ii] = ARGTYP_FLOAT;
                comArgs[ii] = malloc(sizeof(double));
                *((double *)comArgs[ii]) = jni_DoubleValue(env, objValue);
                break;
            case COMTYP_CHAR:
                argtypes[ii] = ARGTYP_STR;
                comArgs[ii] = jniToString(env, objValue);
                break;
            case COMTYP_JAVAOBJ:
            case COMTYP_GENERIC:
            default:
                /* no support for other data types */
                argtypes[ii] = '\0';
                break;
            }
        }
    }

    progIDUTF = (*env)->GetStringUTFChars(env, progID, NULL);
    methodNameUTF = (*env)->GetStringUTFChars(env, methodName, NULL);

    srvres = jni_ExecuteCOM((char *)progIDUTF, (char *)methodNameUTF,
                            len, argtypes, comArgs);

    (*env)->ReleaseStringUTFChars(env, progID, progIDUTF);
    (*env)->ReleaseStringUTFChars(env, methodName, methodNameUTF);

    (*env)->ReleaseCharArrayElements(env, types, typeCodes, JNI_ABORT);

    free(argtypes);

    /* Free the native arguments */
    for (ii = 0; ii < len; ii++)
    {
        if (comArgs[ii] != NULL) free(comArgs[ii]);
    }

    free(comArgs);

    sFreeAccumulatedArgs();

    ret = sWrapResults(env, srvres);

    jniSetServerAdapter(env, saveAdapter);
    jniSetEnv(tmpEnv);

    sAccumulatedArgs = saveAccumulatedArgs;

#else

    srvres = srvErrorResults(eSRV_UNKNOWN_ADAPTER_CODE,
                       "Unknown adapter code for command",
                       NULL);

    ret = sWrapResults(env, srvres);
#endif

    return ret;
}

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _preCommit
 * Signature: (Lcom/redprairie/moca/server/legacy/MocaServerAdapter;)V
 */
JNIEXPORT void JNICALL Java_com_sam_moca_server_legacy_InternalNativeProcess__1preCommit
  (JNIEnv *env, jobject obj, jobject server)
{
    RETURN_STRUCT *srvres = NULL;
    long status = eOK;
    JNIEnv *tmpEnv;
    jobject saveAdapter;

    tmpEnv = jniSetEnv(env);
    saveAdapter = jniSetServerAdapter(env, server);

    status = srv_AppPrecommit();

    jniSetServerAdapter(env, saveAdapter);
    jniSetEnv(tmpEnv);
}

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _postTransaction
 * Signature: (Lcom/redprairie/moca/server/legacy/MocaServerAdapter;Z)V
 */
JNIEXPORT void JNICALL Java_com_sam_moca_server_legacy_InternalNativeProcess__1postTransaction
  (JNIEnv *env, jobject obj, jobject server, jboolean isCommit)
{
    RETURN_STRUCT *srvres = NULL;
    long status = eOK;
    JNIEnv *tmpEnv;
    jobject saveAdapter;

    tmpEnv = jniSetEnv(env);
    saveAdapter = jniSetServerAdapter(env, server);

    if (isCommit)
    {
        srv_AppPostcommit();
	srv_ExecuteAfterCommit(1);
	srv_ExecuteAfterRollback(0);
    }
    else
    {
        srv_AppRollback();
        srv_ExecuteAfterRollback(1);
        srv_ExecuteAfterCommit(0);
    }

    srv_DestroyRegisteredData();

    jniSetServerAdapter(env, saveAdapter);
    jniSetEnv(tmpEnv);
}

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _getKeepaliveCounter
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_sam_moca_server_legacy_InternalNativeProcess__1getKeepaliveCounter
  (JNIEnv *env, jobject obj)
{
    return srv_KeepaliveLevel();
}

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _release
 * Signature: (Lcom/redprairie/moca/server/legacy/MocaServerAdapter;)I
 */
JNIEXPORT void JNICALL Java_com_sam_moca_server_legacy_InternalNativeProcess__1release
  (JNIEnv *env, jobject obj, jobject server)
{
    JNIEnv *tmpEnv;
    jobject saveAdapter;

    tmpEnv = jniSetEnv(env);
    saveAdapter = jniSetServerAdapter(env, server);

    /* Free memory for any environment variable values that were asked for. */
    jni_FreeVarList();

    /* Also free up the os variable cache */
    os_FreeVarList();

    srvResetKeepalive();

    jniSetServerAdapter(env, saveAdapter);
    jniSetEnv(tmpEnv);
}

/*
 * Class:     com_redprairie_moca_server_legacy_InternalNativeProcess
 * Method:    _setEnvironment
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_sam_moca_server_legacy_InternalNativeProcess__1setEnvironment
  (JNIEnv * env, jobject obj, jstring name, jstring value)
{
    const char *nameUTF;
    const char *valueUTF;

    nameUTF = (*env)->GetStringUTFChars(env, name, NULL);
        
    /* If the value is not null then we set that value */
    if (value != NULL)
    {
        valueUTF = (*env)->GetStringUTFChars(env, value, NULL);

        /* 
         * We don't want to go back to Java Side since this is guaranteed to
         * to come from the Java side
         */
        os_PutVar((char *) nameUTF, (char *) valueUTF, NULL, 1);

        (*env)->ReleaseStringUTFChars(env, value, valueUTF);
    }
    /* Else we remove the name */
    else
        os_PutVar((char *) nameUTF, NULL, NULL, 1); 

    (*env)->ReleaseStringUTFChars(env, name, nameUTF);
}
