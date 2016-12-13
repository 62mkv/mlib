static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Hooks into the XMLAdapter class.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <jni.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <jnilib.h>
#include <mislib.h>

#include "mxmlprivate.h"
#include "mxmljnidefs.h"

/*
 * FUNCTION: sAddToGlobalRefList
 *
 * PURPOSE:  Add the given global reference to the global reference list.
 *
 * RETURNS:  void
 */

static void sAddToGlobalRefList(mxmlCtxt *ctxt, jobject globalRef)
{
    mxml_GlobalRef *node;

    node = calloc(1, sizeof(mxml_GlobalRef));

    node->globalRef = globalRef;
    node->next = ctxt->globalRefList;

    ctxt->globalRefList = node;
}

/*
 * FUNCTION: sFreeGlobalRefList
 *
 * PURPOSE:  Free up everything associated with the global reference list.
 *
 * RETURNS:  void
 */

static void sFreeGlobalRefList(JNIEnv *env, mxml_GlobalRef *globalRefList)
{
    mxml_GlobalRef *curr,
		   *next;

    /* Point to the first item in the glohal reference list. */
    curr = globalRefList;

    /* Delete the global references to other objects. */
    while (curr != NULL)
    {
        next = curr->next;

        (*env)->DeleteGlobalRef(env, curr->globalRef);

	free(curr);

	curr = next;
    }
}

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
        misLogError("Could not find class \"%s\"", name);
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
        misLogError("%s%s", name, signature);
        misLogError("Could not get method id");
    }

    return methodID;
}

/*
 * FUNCTION: sInitialize
 *
 * PURPOSE:  Intialize static varaibles for the Java classes and methods
 *           that we call throughout the remainder of the code.
 *
 * RETURNS:  eOK
 *           eERROR  - Could not initialize classes and method ids
 */

static long sInitialize(JNIEnv *env)
{
    static int initd;

    /* Don't bother if we've already initialized ourselves. */
    if (initd)
        return eOK;

    misTrc(T_FLOW, "Initializing XMLAdapter classes and method ids...");

    /* Get the XMLAdapter class. */
    xmlAdapterClass = sFindClass(env, xmlAdapterClassName);
    if (xmlAdapterClass == NULL)
        return eERROR;

    /* constructor */
    constructorMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         constructorName, 
		         constructorSig);
    if (constructorMethod == NULL)
	return eERROR;

    /* configureParser */
    configureParserMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         configureParserName, 
		         configureParserSig);
    if (configureParserMethod == NULL)
        return eERROR;

    /* dumpContext */
    dumpContextMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         dumpContextName, 
		         dumpContextSig);
    if (dumpContextMethod == NULL)
        return eERROR;

    /* parseString */
    parseStringMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         parseStringName, 
		         parseStringSig);
    if (parseStringMethod == NULL)
        return eERROR;

    /* parseFile */
    parseFileMethod = 
	    sGetMethodID(env, 
		         xmlAdapterClass, 
		         parseFileName, 
		         parseFileSig);
    if (parseFileMethod == NULL)
        return eERROR;

    /* toString */
    toStringMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         toStringName, 
		         toStringSig);
    if (toStringMethod == NULL)
        return eERROR;

    /* toStringRaw */
    toStringRawMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         toStringRawName, 
		         toStringRawSig);
    if (toStringRawMethod == NULL)
        return eERROR;

    /* writeFile */
    writeFileMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         writeFileName, 
		         writeFileSig);
    if (writeFileMethod == NULL)
        return eERROR;

    /* writeRawFile */
    writeRawFileMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         writeRawFileName, 
		         writeRawFileSig);
    if (writeRawFileMethod == NULL)
        return eERROR;

    /* addElement */
    addElementMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         addElementName, 
		         addElementSig);
    if (addElementMethod == NULL)
        return eERROR;

    /* addTextNode */
    addTextNodeMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         addTextNodeName, 
		         addTextNodeSig);
    if (addTextNodeMethod == NULL)
        return eERROR;

    /* addComment */
    addCommentMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         addCommentName, 
		         addCommentSig);
    if (addCommentMethod == NULL)
        return eERROR;

    /* addPI */
    addPIMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         addPIName, 
		         addPISig);
    if (addPIMethod == NULL)
        return eERROR;

    /* addAttribute */
    addAttributeMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         addAttributeName, 
		         addAttributeSig);
    if (addAttributeMethod == NULL)
        return eERROR;

    /* applyStylesheetFromFile */
    applyStylesheetFromFileMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         applyStylesheetFromFileName, 
		         applyStylesheetFromFileSig);
    if (applyStylesheetFromFileMethod == NULL)
        return eERROR;

    /* applyStylesheetFromString */
    applyStylesheetFromStringMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         applyStylesheetFromStringName, 
		         applyStylesheetFromStringSig);
    if (applyStylesheetFromStringMethod == NULL)
        return eERROR;

    /* createDocument */
    createDocumentMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         createDocumentName, 
		         createDocumentSig);
    if (createDocumentMethod == NULL)
        return eERROR;

    /* getDocument */
    getDocumentMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getDocumentName, 
		         getDocumentSig);
    if (getDocumentMethod == NULL)
        return eERROR;

    /* insertBefore */
    insertBeforeMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         insertBeforeName, 
		         insertBeforeSig);
    if (insertBeforeMethod == NULL)
        return eERROR;

    /* replaceChild */
    replaceChildMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         replaceChildName, 
		         replaceChildSig);
    if (replaceChildMethod == NULL)
        return eERROR;

    /* removeChild */
    removeChildMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         removeChildName, 
		         removeChildSig);
    if (removeChildMethod == NULL)
        return eERROR;

    /* appendChild */
    appendChildMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         appendChildName, 
		         appendChildSig);
    if (appendChildMethod == NULL)
        return eERROR;

    /* hasChildNodes */
    hasChildNodesMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         hasChildNodesName, 
		         hasChildNodesSig);
    if (hasChildNodesMethod == NULL)
        return eERROR;

    /* cloneNode */
    cloneNodeMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         cloneNodeName, 
		         cloneNodeSig);
    if (cloneNodeMethod == NULL)
        return eERROR;

    /* importNode */
    importNodeMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         importNodeName, 
		         importNodeSig);
    if (importNodeMethod == NULL)
        return eERROR;

    /* getNodeName */
    getNodeNameMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getNodeNameName, 
		         getNodeNameSig);
    if (getNodeNameMethod == NULL)
        return eERROR;

    /* getNodeValue */
    getNodeValueMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getNodeValueName, 
		         getNodeValueSig);
    if (getNodeValueMethod == NULL)
        return eERROR;

    /* setNodeValue */
    setNodeValueMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         setNodeValueName, 
		         setNodeValueSig);
    if (setNodeValueMethod == NULL)
        return eERROR;

    /* getNodeType */
    getNodeTypeMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getNodeTypeName, 
		         getNodeTypeSig);
    if (getNodeTypeMethod == NULL)
        return eERROR;

    /* getParentNode */
    getParentNodeMethod =
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getParentNodeName, 
		         getParentNodeSig);
    if (getParentNodeMethod == NULL)
        return eERROR;

    /* getChildNodes */
    getChildNodesMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getChildNodesName, 
		         getChildNodesSig);
    if (getChildNodesMethod == NULL)
        return eERROR;


    /* getFirstChild */
    getFirstChildMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getFirstChildName, 
		         getFirstChildSig);
    if (getFirstChildMethod == NULL)
        return eERROR;

    /* getLastChild */
    getLastChildMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getLastChildName, 
		         getLastChildSig);
    if (getLastChildMethod == NULL)
        return eERROR;

    /* getPreviousSibling */
    getPreviousSiblingMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getPreviousSiblingName, 
		         getPreviousSiblingSig);
    if (getPreviousSiblingMethod == NULL)
        return eERROR;

    /* getNextSibling */
    getNextSiblingMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getNextSiblingName, 
		         getNextSiblingSig);
    if (getNextSiblingMethod == NULL)
        return eERROR;

    /* getAttributes */
    getAttributesMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getAttributesName, 
		         getAttributesSig);
    if (getAttributesMethod == NULL)
        return eERROR;

    /* getAttribute */
    getAttributeMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getAttributeName, 
		         getAttributeSig);
    if (getAttributeMethod == NULL)
        return eERROR;

    /* setAttribute */
    setAttributeMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         setAttributeName, 
		         setAttributeSig);
    if (setAttributeMethod == NULL)
        return eERROR;

    /* removeAttribute */
    removeAttributeMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         removeAttributeName, 
		         removeAttributSig);
    if (removeAttributeMethod == NULL)
        return eERROR;

    /* getAttributeNode */
    getAttributeNodeMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getAttributeNodeName, 
		         getAttributeNodeSig);
    if (getAttributeNodeMethod == NULL)
        return eERROR;


    /* setAttributeNode */
    setAttributeNodeMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         setAttributeNodeName, 
		         setAttributeNodeSig);
    if (setAttributeNodeMethod == NULL)
        return eERROR;

    /* removeAttributeNode */
    removeAttributeNodeMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         removeAttributeNodeName, 
		         removeAttributeNodeSig);
    if (removeAttributeNodeMethod == NULL)
        return eERROR;

    /* getElementsByTagName */
    getElementsByTagNameMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getElementsByTagNameName, 
		         getElementsByTagNameSig);
    if (getElementsByTagNameMethod == NULL)
        return eERROR;

    /* getTagName */
    getTagNameMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getTagNameName, 
		         getTagNameSig);
    if (getTagNameMethod == NULL)
        return eERROR;

    /* createElement */
    createElementMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         createElementName, 
		         createElementSig);
    if (createElementMethod == NULL)
        return eERROR;

    /* createDocumentFragment */
    createDocumentFragmentMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         createDocumentFragmentName, 
		         createDocumentFragmentSig);
    if (createDocumentFragmentMethod == NULL)
        return eERROR;

    /* createTextNode */
    createTextNodeMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         createTextNodeName, 
		         createTextNodeSig);
    if (createTextNodeMethod == NULL)
        return eERROR;

    /* createComment */
    createCommentMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         createCommentName, 
		         createCommentSig);
    if (createCommentMethod == NULL)
        return eERROR;

    /* createCDATASection */
    createCDATASectionMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         createCDATASectionName, 
		         createCDATASectionSig);
    if (createCDATASectionMethod == NULL)
        return eERROR;

    /* createPI */
    createPIMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         createPIName, 
		         createPISig);
    if (createPIMethod == NULL)
        return eERROR;

    /* createAttribute */
    createAttributeMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         createAttributeName, 
		         createAttributeSig);
    if (createAttributeMethod == NULL)
        return eERROR;

    /* getDocumentElement */
    getDocumentElementMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getDocumentElementName, 
		         getDocumentElementSig);
    if (getDocumentElementMethod == NULL)
        return eERROR;

    /* getAttrName */
    getAttrNameMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getAttrNameName, 
		         getAttrNameSig);
    if (getAttrNameMethod == NULL)
        return eERROR;

    /* getAttrSpecified */
    getAttrSpecifiedMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getAttrSpecifiedName, 
		         getAttrSpecifiedSig);
    if (getAttrSpecifiedMethod == NULL)
        return eERROR;

    /* getAttrValue */
    getAttrValueMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getAttrValueName, 
		         getAttrValueSig);
    if (getAttrValueMethod == NULL)
        return eERROR;

    /* setAttrValue */
    setAttrValueMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         setAttrValueName, 
		         setAttrValueSig);
    if (setAttrValueMethod == NULL)
        return eERROR;

    /* getPITarget */
    getPITargetMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getPITargetName, 
		         getPITargetSig);
    if (getPITargetMethod == NULL)
        return eERROR;

    /* getPIData */
    getPIDataMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getPIDataName, 
		         getPIDataSig);
    if (getPIDataMethod == NULL)
        return eERROR;

    /* setPIData */
    setPIDataMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         setPIDataName, 
		         setPIDataSig);
    if (setPIDataMethod == NULL)
        return eERROR;

    /* getCharacterData */
    getCharacterDataMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getCharacterDataName, 
		         getCharacterDataSig);
    if (getCharacterDataMethod == NULL)
        return eERROR;

    /* setCharacterData */
    setCharacterDataMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         setCharacterDataName, 
		         setCharacterDataSig);
    if (setCharacterDataMethod == NULL)
        return eERROR;

    /* getCharacterDataLength */
    getCharacterDataLengthMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getCharacterDataLengthName, 
		         getCharacterDataLengthSig);
    if (getCharacterDataLengthMethod == NULL)
        return eERROR;

    /* getNodeListLength */
    getNodeListLengthMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getNodeListLengthName, 
		         getNodeListLengthSig);
    if (getNodeListLengthMethod == NULL)
        return eERROR;

    /* getItem */
    getItemMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getItemName, 
		         getItemSig);
    if (getItemMethod == NULL)
        return eERROR;

    /* getNamedItem */
    getNamedItemMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         getNamedItemName, 
		         getNamedItemSig);
    if (getNamedItemMethod == NULL)
        return eERROR;

    /* setNamedItem */
    setNamedItemMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         setNamedItemName, 
		         setNamedItemSig);
    if (setNamedItemMethod == NULL)
        return eERROR;

    /* removeNamedItem */
    removeNamedItemMethod = 
	    sGetMethodID(env,
		         xmlAdapterClass, 
		         removeNamedItemName, 
		         removeNamedItemSig);
    if (removeNamedItemMethod == NULL)
        return eERROR;

    initd = 1;

    misTrc(T_FLOW, "Initialized XMLAdapter classes and method ids");

    return eOK;
}

long jni_StartParser(mxmlCtxt **ctxt)
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;

    mxmlCtxt *tempCtxt;

    jobject tempObject,
	    xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Initialize class and method ids. */
    status = sInitialize(env);
    if (status != eOK)
    {
	status = eMXML_INITIALIZE;
        misLogError("Could not initialize XMLAdapter method ids");
	goto cleanup;
    }

    /* Instantiate a new XMLAdapter. */
    tempObject = (*env)->NewObject(env, xmlAdapterClass, constructorMethod);
    if (tempObject == NULL)
    {
	status = eMXML_CONSTRUCT;
	misLogError("Could not construct XMLAdapter");
	goto cleanup;
    }

    /* Create a global reference of this object. */
    xmlAdapterObject = (*env)->NewGlobalRef(env, tempObject);

    /* Allocate space for the context structure. */
    tempCtxt = (mxmlCtxt *) calloc(1, sizeof(mxmlCtxt));

    /* Initialize the attributes of the context structure. */
    tempCtxt->globalRefList = NULL;
    tempCtxt->xmlAdapterObject = xmlAdapterObject;
    tempCtxt->validate = 0;
    tempCtxt->preserveWhitespace = 0;

    /* Point the caller to the context structure. */
    *ctxt = tempCtxt;

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return status;
}

void jni_StopParser(mxmlCtxt *ctxt)
{
    JNIEnv *env = jniGetEnv( );

    /* Delete the global reference to the XMLAdapter object. */
    (*env)->DeleteGlobalRef(env, ctxt->xmlAdapterObject);

    /* Delete the global references to other objects. */
    sFreeGlobalRefList(env, ctxt->globalRefList);

    if (ctxt->errorText)
    {
        free(ctxt->errorText);
        ctxt->errorText = NULL;
    }

    /* Free memory associated with the context. */
    free(ctxt);

    return;
}

long jni_ConfigureParser(mxmlCtxt *ctxt, 
		         short preserveWhitespace, 
			 short validate)
{
    JNIEnv *env = jniGetEnv( );

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Save the configuration to the context. */
    ctxt->validate = validate ? 1 : 0;
    ctxt->preserveWhitespace = preserveWhitespace ? 1 : 0;

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           xmlAdapterObject, 
		           configureParserMethod, 
			   (jboolean) preserveWhitespace,
			   (jboolean) validate);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_CONFIGURE, "Could not configure parser");
        goto cleanup;
    }

cleanup:

    return eOK;
}

void jni_DumpContext(mxmlCtxt *ctxt)
{
    JNIEnv *env = jniGetEnv( );

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    misLogInfo("Dumping native-level XML parser context...");
    misLogInfo("Address               : %p", ctxt);
    misLogInfo("Global Reference List : %p", ctxt->globalRefList);
    misLogInfo("Last Error            : %ld", ctxt->errorNumber);
    misLogInfo("Validate              : %d", ctxt->validate);
    misLogInfo("Preserve Whitespace   : %d", ctxt->preserveWhitespace);
    misLogInfo("");

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           xmlAdapterObject, 
		           dumpContextMethod);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_CONFIGURE, "Could not dump parser context");
        goto cleanup;
    }

cleanup:

    return;
}

void jni_Error(mxmlCtxt *ctxt, long errorNumber, char *msg)
{
    JNIEnv *env = jniGetEnv( );

    char *errorText;

    jthrowable exc;

    /* Free the current error text. */
    if (ctxt->errorText)  
    {
        free(ctxt->errorText);
	ctxt->errorText = NULL;
    }

    /* Make a copy of the error text and number for the context. */
    ctxt->errorNumber = errorNumber;
    misDynSprintf(&ctxt->errorText, "%s\n", msg);

    /* Deal with any exception that may have been raised. */
    exc = (*env)->ExceptionOccurred(env);
    if (exc) 
    {
        /* Clear the exception from the current JNI environment. */
        (*env)->ExceptionClear(env);
    
        /* Grab the exception error text. */
        errorText = jniToString(env, exc);
    
        /* Append the exception error text to the caller's error text. */
        misDynStrcat(&ctxt->errorText, errorText);
        free(errorText);
    
        /*
         * We don't know if we're being called from a temporary JNI context,
         * so let's explicitly delete any references we've produced.
         */
        (*env)->DeleteLocalRef(env, exc);
    }

    return;
}

long jni_ErrorNumber(mxmlCtxt *ctxt)
{
    return ctxt->errorNumber;
}

char *jni_ErrorText(mxmlCtxt *ctxt)
{
    return ctxt->errorText;
}

mxmlNode *jni_ParseString(mxmlCtxt *ctxt, char *xml)
{
    JNIEnv *env = jniGetEnv( );

    jstring jXml,
	    jCharset;

    jobject node       = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Get a Java string from a UTF-8 string. */
    jXml = (*env)->NewStringUTF(env, xml);

    /* Get the character set we're using. */
    jCharset = jni_Charset(env);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          parseStringMethod,
				          jXml,
					  jCharset);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_PARSE_ERROR, "Could not parse XML string");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

mxmlNode *jni_ParseFile(mxmlCtxt *ctxt, char *filename)
{
    JNIEnv *env = jniGetEnv( );

    jstring jFilename;

    jobject node       = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jFilename = (*env)->NewStringUTF(env, filename);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          parseFileMethod,
				          jFilename);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_PARSE_ERROR, "Could not parse XML file");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

char *jni_String(mxmlCtxt *ctxt, mxmlNode *node)
{
    JNIEnv *env = jniGetEnv( );

    char *xml = NULL;

    jobject tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          toStringMethod, 
				          (jobject) node);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_STRING, "Could not serialize XML");
        goto cleanup;
    }

    /* Convert the UTF-16 string into a UTF-8 string. */
    xml = jniDecodeString(env, tempObject, NULL);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return xml;
}

char *jni_RawString(mxmlCtxt *ctxt, mxmlNode *node)
{
    JNIEnv *env = jniGetEnv( );

    char *xml = NULL;

    jobject tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          toStringRawMethod, 
				          (jobject) node);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_RAW_STRING, "Could not serialize XML raw");
        goto cleanup;
    }

    /* Convert the UTF-16 string into a UTF-8 string. */
    xml = jniDecodeString(env, tempObject, NULL);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return xml;
}

long jni_WriteFile(mxmlCtxt *ctxt, mxmlNode *node, char *filename)
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;

    jstring jFilename;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jFilename = (*env)->NewStringUTF(env, filename);

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           xmlAdapterObject, 
		           writeFileMethod, 
			   (jobject) node,
			   jFilename);
    if ((*env)->ExceptionCheck(env))
    {
	status = eMXML_WRITE_FILE;
	mxmlError(ctxt, status, "Could not write XML file");
        goto cleanup;
    }

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return status;
}

long jni_RawWriteFile(mxmlCtxt *ctxt, mxmlNode *node, char *filename)
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;

    jstring jFilename;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jFilename = (*env)->NewStringUTF(env, filename);

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           xmlAdapterObject, 
		           writeRawFileMethod, 
			   (jobject) node,
			   jFilename);
    if ((*env)->ExceptionCheck(env))
    {
	status = eMXML_RAW_WRITE_FILE;
	mxmlError(ctxt, status, "Could not write raw XML file");
        goto cleanup;
    }

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return status;
}

mxmlNode *jni_AddElement(mxmlCtxt *ctxt, mxmlNode *parent, char *name)
{
    JNIEnv *env = jniGetEnv( );

    jstring jName;

    jobject node       = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Get a Java string from a UTF-8 string. */
    jName = (*env)->NewStringUTF(env, name);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          addElementMethod, 
					  (jobject) parent,
				          jName);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_ADD_ELEMENT, "Could not add element");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

mxmlNode *jni_AddTextNode(mxmlCtxt *ctxt, mxmlNode *parent, char *text)
{
    JNIEnv *env = jniGetEnv( );

    jstring jText;

    jobject node       = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Get a Java string from a UTF-8 string. */
    jText = (*env)->NewStringUTF(env, text);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          addTextNodeMethod, 
					  (jobject) parent,
				          jText);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_ADD_TEXT_NODE, "Could not add text node");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

mxmlNode *jni_AddComment(mxmlCtxt *ctxt, mxmlNode *parent, char *comment)
{
    JNIEnv *env = jniGetEnv( );

    jstring jComment;

    jobject node       = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Get a Java string from a UTF-8 string. */
    jComment = (*env)->NewStringUTF(env, comment);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          addCommentMethod, 
					  (jobject) parent,
				          jComment);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_ADD_COMMENT, "Could not add comment");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

mxmlNode *jni_AddPI(mxmlCtxt *ctxt, mxmlNode *parent, char *target, char *data)
{
    JNIEnv *env = jniGetEnv( );

    jstring jTarget,
	    jData;

    jobject node       = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Get a Java string from a UTF-8 string. */
    jTarget = (*env)->NewStringUTF(env, target);
    jData = (*env)->NewStringUTF(env, data);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          addPIMethod, 
					  (jobject) parent,
				          jTarget,
					  jData);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_ADD_PI, "Could not add processing instruction");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

mxmlNode *jni_AddAttribute(mxmlCtxt *ctxt, 
	                   mxmlNode *parent, 
			   char     *name, 
	                   char     *value)
{
    JNIEnv *env = jniGetEnv( );

    jstring jName,
	    jValue;

    jobject node       = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Get a Java string from a UTF-8 string. */
    jName = (*env)->NewStringUTF(env, name);
    jValue = (*env)->NewStringUTF(env, value);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          addAttributeMethod, 
					  (jobject) parent,
				          jName,
					  jValue);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_ADD_ATTRIBUTE, "Could not add attribute");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

mxmlCtxt *jni_ApplyStylesheetFromFile(mxmlCtxt *ctxt, char *pathname)
{
    JNIEnv *env = jniGetEnv( );

    mxmlCtxt *newCtxt = NULL;

    jobject tempObject          = NULL,
            newXmlAdapter = NULL;

    jstring jPathname;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jPathname = (*env)->NewStringUTF(env, pathname);
    
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          applyStylesheetFromFileMethod, 
					  jPathname);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, 
		  eMXML_APPLY_XSLT_FILE, 
		  "Could not apply stylesheet from file");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    newXmlAdapter = (*env)->NewGlobalRef(env, tempObject);

    /* Allocate space for the context structure. */
    newCtxt = (mxmlCtxt *) calloc(1, sizeof(mxmlCtxt));

    /* Initialize the attributes of the context structure. */
    newCtxt->globalRefList = NULL;
    newCtxt->xmlAdapterObject = newXmlAdapter;
    newCtxt->validate = 0;
    newCtxt->preserveWhitespace = 0;

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return newCtxt;
}

mxmlCtxt *jni_ApplyStylesheetFromString(mxmlCtxt *ctxt, char *xsl)
{
    JNIEnv *env = jniGetEnv( );

    mxmlCtxt *newCtxt = NULL;

    jobject tempObject          = NULL,
            newXmlAdapter = NULL;

    jstring jXsl,
	    jCharset;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jXsl = (*env)->NewStringUTF(env, xsl);
    
    /* Get the character set we're using. */
    jCharset = jni_Charset(env);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          applyStylesheetFromStringMethod, 
					  jXsl,
					  jCharset);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, 
		  eMXML_APPLY_XSLT_STRING, 
		  "Could not apply stylesheet from string");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    newXmlAdapter = (*env)->NewGlobalRef(env, tempObject);

    /* Allocate space for the context structure. */
    newCtxt = (mxmlCtxt *) calloc(1, sizeof(mxmlCtxt));

    /* Initialize the attributes of the context structure. */
    newCtxt->globalRefList = NULL;
    newCtxt->xmlAdapterObject = newXmlAdapter;
    newCtxt->validate = 0;
    newCtxt->preserveWhitespace = 0;

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return newCtxt;
}

mxmlNode *jni_CreateDocument(mxmlCtxt *ctxt)
{
    JNIEnv *env = jniGetEnv( );

    jobject node       = NULL,
            tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          createDocumentMethod);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_CREATE_DOCUMENT, "Could not create document");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

mxmlNode *jni_GetDocument(mxmlCtxt *ctxt)
{
    JNIEnv *env = jniGetEnv( );

    jobject node       = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getDocumentMethod);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_GET_DOCUMENT, "Could not get document");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

mxmlNode *jni_InsertBefore(mxmlCtxt *ctxt,
	                   mxmlNode *parent,
		           mxmlNode *newChild,
		           mxmlNode *refChild)
{
    JNIEnv *env = jniGetEnv( );

    jobject node       = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          insertBeforeMethod, 
					  (jobject) parent,
					  (jobject) newChild,
					  (jobject) refChild);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_INSERT_BEFORE, "Could not insert node");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

long jni_ReplaceChild(mxmlCtxt *ctxt, 
	              mxmlNode *parent, 
		      mxmlNode *newChild,
	              mxmlNode *oldChild)
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           xmlAdapterObject, 
		           replaceChildMethod, 
			   (jobject) parent,
			   (jobject) newChild,
			   (jobject) oldChild);
    if ((*env)->ExceptionCheck(env))
    {
	status = eMXML_REPLACE_CHILD;
	mxmlError(ctxt, status, "Could not replace child");
        goto cleanup;
    }

cleanup:

    return status;
}

long jni_RemoveChild(mxmlCtxt *ctxt, mxmlNode *parent, mxmlNode *oldChild)
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           xmlAdapterObject, 
		           removeChildMethod, 
			   (jobject) parent,
			   (jobject) oldChild);
    if ((*env)->ExceptionCheck(env))
    {
	status = eMXML_REMOVE_CHILD;
	mxmlError(ctxt, status, "Could not remove child");
        goto cleanup;
    }

cleanup:

    return status;
}

mxmlNode *jni_AppendChild(mxmlCtxt *ctxt, mxmlNode *parent, mxmlNode *newChild)
{
    JNIEnv *env = jniGetEnv( );

    jobject node       = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          appendChildMethod, 
					  (jobject) parent,
					  (jobject) newChild);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_APPEND_CHILD, "Could not append child");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

long jni_HasChildNodes(mxmlCtxt *ctxt, mxmlNode *node)
{
    JNIEnv *env = jniGetEnv( );

    long value;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Call the Java method. */
    value = (*env)->CallIntMethod(env, 
		                  xmlAdapterObject, 
		                  hasChildNodesMethod, 
				  (jobject) node);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, 
		  eMXML_HAS_CHILD_NODES, 
		  "Could not determine if node has child nodes");
        goto cleanup;
    }

cleanup:

    return value ? 1 : 0;
}

mxmlNode *jni_CloneNode(mxmlCtxt *ctxt, mxmlNode *node, long deep)
{
    JNIEnv *env = jniGetEnv( );

    jobject clone      = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          cloneNodeMethod, 
					  (jobject) node,
				          (jint) deep);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_CLONE_NODE, "Could not clone node");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    clone = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, clone);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) clone;
}

mxmlNode *jni_ImportNode(mxmlCtxt *ctxt, mxmlNode *node, long deep)
{
    JNIEnv *env = jniGetEnv( );

    jobject imported   = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          importNodeMethod, 
					  (jobject) node,
				          (jint) deep);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_CLONE_NODE, "Could not clone node");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    imported = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, imported);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) imported;
}

char *jni_GetNodeName(mxmlCtxt *ctxt, mxmlNode *node)
{
    JNIEnv *env = jniGetEnv( );

    char *name = NULL;

    jobject tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getNodeNameMethod, 
				          (jobject) node);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_GET_NODE_NAME, "Could not get node name");
        goto cleanup;
    }

    /* Convert the UTF-16 string into a UTF-8 string. */
    name = jniDecodeString(env, tempObject, NULL);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return name;
}

char *jni_GetNodeValue(mxmlCtxt *ctxt, mxmlNode *node)
{
    JNIEnv *env = jniGetEnv( );

    char *value = NULL;

    jobject tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getNodeValueMethod, 
				          (jobject) node);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_GET_NODE_VALUE, "Could not get node value");
        goto cleanup;
    }

    /* Convert the UTF-16 string into a UTF-8 string. */
    value = jniDecodeString(env, tempObject, NULL);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return value;
}

long jni_SetNodeValue(mxmlCtxt *ctxt, mxmlNode *node, char *value)
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;

    jstring jValue;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jValue = (*env)->NewStringUTF(env, value);

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           xmlAdapterObject, 
		           setNodeValueMethod, 
			   (jobject) node,
			   jValue);
    if ((*env)->ExceptionCheck(env))
    {
	status = eMXML_SET_NODE_VALUE;
	mxmlError(ctxt, status, "Could not set node value");
        goto cleanup;
    }

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return status;
}

mxmlNodeType jni_GetNodeType(mxmlCtxt *ctxt, mxmlNode *node)
{
    JNIEnv *env = jniGetEnv( );

    long value;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Call the Java method. */
    value = (*env)->CallIntMethod(env, 
		                  xmlAdapterObject, 
		                  getNodeTypeMethod, 
				  (jobject) node);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_GET_NODE_TYPE, "Could not get node type");
        goto cleanup;
    }

cleanup:

    return value;
}

mxmlNode *jni_GetParentNode(mxmlCtxt *ctxt, mxmlNode *node)
{
    JNIEnv *env = jniGetEnv( );

    jobject parent     = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getParentNodeMethod, 
					  (jobject) node);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_GET_PARENT_NODE, "Could not get parent node");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    parent = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, parent);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) parent;
}

mxmlNodeList *jni_GetChildNodes(mxmlCtxt *ctxt, mxmlNode *node)
{
    JNIEnv *env = jniGetEnv( );

    jobject nodeList   = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getChildNodesMethod,
					  (jobject) node);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, 
	           eMXML_GET_CHILD_NODES, 
		  "Could not get child nodes");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    nodeList = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, nodeList);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNodeList *) nodeList;
}

mxmlNode *jni_GetFirstChild(mxmlCtxt *ctxt, mxmlNode *parent)
{
    JNIEnv *env = jniGetEnv( );

    jobject child      = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getFirstChildMethod, 
				          (jobject) parent);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_GET_FIRST_CHILD, "Could not get first child");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    child = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, child);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) child;
}

mxmlNode *jni_GetLastChild(mxmlCtxt *ctxt, mxmlNode *parent)
{
    JNIEnv *env = jniGetEnv( );

    jobject child      = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getLastChildMethod, 
					  (jobject) parent);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_GET_LAST_CHILD, "Could not get last child");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    child = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, child);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) child;
}

mxmlNode *jni_GetPreviousSibling(mxmlCtxt *ctxt, mxmlNode *node)
{
    JNIEnv *env = jniGetEnv( );

    jobject sibling    = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getPreviousSiblingMethod, 
					  (jobject) node);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, 
		  eMXML_GET_PREVIOUS_SIBLING, 
		  "Could not get previous sibling");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    sibling = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, sibling);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) sibling;
}

mxmlNode *jni_GetNextSibling(mxmlCtxt *ctxt, mxmlNode *node)
{
    JNIEnv *env = jniGetEnv( );

    jobject sibling    = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getNextSiblingMethod, 
					  (jobject) node);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, 
		  eMXML_GET_NEXT_SIBLING, 
		  "Could not get next sibling");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    sibling = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, sibling);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) sibling;
}

mxmlNodeList *jni_GetAttributes(mxmlCtxt *ctxt, mxmlNode *node)
{
    JNIEnv *env = jniGetEnv( );

    jobject nodeList    = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getAttributesMethod, 
					  (jobject) node);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_GET_ATTRS, "Could not get attributes");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    nodeList = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, nodeList);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNodeList *) nodeList;
}

char *jni_GetAttribute(mxmlCtxt *ctxt, mxmlNode *node, char *name)
{
    JNIEnv *env = jniGetEnv( );

    char *value = NULL;

    jstring jName;
	    
    jobject tempObject;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Get a Java string from a UTF-8 string. */
    jName = (*env)->NewStringUTF(env, name);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getAttributeMethod, 
				          (jobject) node,
				          jName);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_GET_ATTR, "Could not get attribute");
        goto cleanup;
    }

    /* Convert the UTF-16 string into a UTF-8 string. */
    value = jniDecodeString(env, tempObject, NULL);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return value;
}

long jni_SetAttribute(mxmlCtxt *ctxt, 
	              mxmlNode *element, 
		      char *name, 
		      char *value)
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;

    jstring jName,
	    jValue;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jName = (*env)->NewStringUTF(env, name);
    jValue = (*env)->NewStringUTF(env, value);

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           xmlAdapterObject, 
		           setAttributeMethod, 
			   (jobject) element,
			   jName,
			   jValue);
    if ((*env)->ExceptionCheck(env))
    {
	status = eMXML_SET_ATTR;
	mxmlError(ctxt, status, "Could not set attribute");
        goto cleanup;
    }

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return status;
}

long jni_RemoveAttribute(mxmlCtxt *ctxt, mxmlNode *element, char *name)
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;

    jstring jName;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jName = (*env)->NewStringUTF(env, name);

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           xmlAdapterObject, 
		           removeAttributeMethod, 
			   (jobject) element,
			   jName);
    if ((*env)->ExceptionCheck(env))
    {
	status = eMXML_REMOVE_ATTR;
	mxmlError(ctxt, status, "Could not remove attribute");
        goto cleanup;
    }

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return status;
}

mxmlNode *jni_GetAttributeNode(mxmlCtxt *ctxt, mxmlNode *element, char *name)
{
    JNIEnv *env = jniGetEnv( );

    jstring jName;

    jobject node       = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Get a Java string from a UTF-8 string. */
    jName = (*env)->NewStringUTF(env, name);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getAttributeNodeMethod, 
					  (jobject) element,
				          jName);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_GET_ATTR_NODE, "Could not get attribute node");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

long jni_SetAttributeNode(mxmlCtxt *ctxt, 
	                  mxmlNode *element, 
		          mxmlNode *attribute)
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           xmlAdapterObject, 
		           setAttributeNodeMethod, 
			   (jobject) element,
			   (jobject) attribute);
    if ((*env)->ExceptionCheck(env))
    {
	status = eMXML_SET_ATTR_NODE;
	mxmlError(ctxt, status, "Could not set attribute node");
        goto cleanup;
    }

cleanup:

    return status;
}

long jni_RemoveAttributeNode(mxmlCtxt *ctxt, 
	                     mxmlNode *element, 
	                     mxmlNode *attribute)
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           xmlAdapterObject, 
		           removeAttributeNodeMethod, 
			   (jobject) element,
			   (jobject) attribute);
    if ((*env)->ExceptionCheck(env))
    {
	status = eMXML_REMOVE_ATTR_NODE;
	mxmlError(ctxt, status, "Could not remove attribute node");
        goto cleanup;
    }

cleanup:

    return status;
}

mxmlNodeList *jni_GetElementsByTagName(mxmlCtxt *ctxt, 
	                               mxmlNode *parent, 
				       char     *name)
{
    JNIEnv *env = jniGetEnv( );

    jstring jName;

    jobject nodeList   = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Get a Java string from a UTF-8 string. */
    jName = (*env)->NewStringUTF(env, name);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getElementsByTagNameMethod, 
					  (jobject) parent,
				          jName);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, 
		  eMXML_GET_ELEMENTS, 
		  "Could not get elements by tag name");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    nodeList = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, nodeList);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNodeList *) nodeList;
}

char *jni_GetTagName(mxmlCtxt *ctxt, mxmlNode *element)
{
    JNIEnv *env = jniGetEnv( );

    char *name = NULL;

    jobject tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getTagNameMethod, 
				          (jobject) element);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_GET_TAG_NAME, "Could not get tag name");
        goto cleanup;
    }

    /* Convert the UTF-16 string into a UTF-8 string. */
    name = jniDecodeString(env, tempObject, NULL);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return name;
}

mxmlNode *jni_CreateElement(mxmlCtxt *ctxt, char *name)
{
    JNIEnv *env = jniGetEnv( );

    jobject node       = NULL,
	    tempObject = NULL;

    jstring jName;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jName = (*env)->NewStringUTF(env, name);
    
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          createElementMethod, 
					  jName);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_CREATE_TEXT_NODE, "Could not create text node");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

mxmlNode *jni_CreateDocumentFragment(mxmlCtxt *ctxt)
{
    JNIEnv *env = jniGetEnv( );

    jobject node       = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          createDocumentFragmentMethod);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, 
                  eMXML_CREATE_DOCFRAG, 
		  "Could not create document fragment");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

mxmlNode *jni_CreateTextNode(mxmlCtxt *ctxt, char *text)
{
    JNIEnv *env = jniGetEnv( );

    jobject node       = NULL,
	    tempObject = NULL;

    jstring jText;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jText = (*env)->NewStringUTF(env, text);
    
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          createTextNodeMethod, 
					  jText);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_CREATE_TEXT_NODE, "Could not create text node");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

mxmlNode *jni_CreateComment(mxmlCtxt *ctxt, char *comment)
{
    JNIEnv *env = jniGetEnv( );

    jobject node      = NULL,
            tempObject = NULL;

    jstring jComment;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jComment = (*env)->NewStringUTF(env, comment);
    
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          createCommentMethod, 
					  jComment);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_CREATE_COMMENT, "Could not create comment");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

mxmlNode *jni_CreateCDATASection(mxmlCtxt *ctxt, char *data)
{
    JNIEnv *env = jniGetEnv( );

    jobject node       = NULL,
            tempObject = NULL;

    jstring jData;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jData = (*env)->NewStringUTF(env, data);
    
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          createCDATASectionMethod, 
					  jData);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, 
		  eMXML_CREATE_CDATA_SECTION, 
		  "Could not create character data section");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

mxmlNode *jni_CreatePI(mxmlCtxt *ctxt, char *target, char *data)
{
    JNIEnv *env = jniGetEnv( );

    jobject node       = NULL,
            tempObject = NULL;

    jstring jTarget,
	    jData;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jTarget = (*env)->NewStringUTF(env, target);
    jData = (*env)->NewStringUTF(env, data);
    
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          createPIMethod, 
				          jTarget,
					  jData);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, 
		  eMXML_CREATE_PI, 
		  "Could not create processing instruction");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

mxmlNode *jni_CreateAttribute(mxmlCtxt *ctxt, char *name)
{
    JNIEnv *env = jniGetEnv( );

    jobject node       = NULL,
            tempObject = NULL;

    jstring jName;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jName = (*env)->NewStringUTF(env, name);
    
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          createAttributeMethod, 
				          jName);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_CREATE_ATTR, "Could not create attribute");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

mxmlNode *jni_GetDocumentElement(mxmlCtxt *ctxt)
{
    JNIEnv *env = jniGetEnv( );

    jobject node       = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getDocumentElementMethod);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, 
	           eMXML_GET_DOCUMENT_ELEMENT, 
		  "Could not get document element");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

char *jni_GetAttrName(mxmlCtxt *ctxt, mxmlNode *attr)
{
    JNIEnv *env = jniGetEnv( );

    char *name = NULL;

    jobject tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getAttrNameMethod, 
				          (jobject) attr);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_GET_ATTR_NAME, "Could not get attribute name");
        goto cleanup;
    }

    /* Convert the UTF-16 string into a UTF-8 string. */
    name = jniDecodeString(env, tempObject, NULL);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return name;
}

long jni_GetAttrSpecified(mxmlCtxt *ctxt, mxmlNode *attr)
{
    JNIEnv *env = jniGetEnv( );

    long value;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Call the Java method. */
    value = (*env)->CallIntMethod(env, 
		                  xmlAdapterObject, 
		                  getAttrSpecifiedMethod, 
				  (jobject) attr);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, 
		  eMXML_GET_ATTR_SPECIFIED,
		  "Could not get attributed specified");
        goto cleanup;
    }

cleanup:

    return value ? 1 : 0;
}

char *jni_GetAttrValue(mxmlCtxt *ctxt, mxmlNode *attr)
{
    JNIEnv *env = jniGetEnv( );

    char *value = NULL;

    jobject tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getAttrValueMethod, 
				          (jobject) attr);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_GET_ATTR_VALUE, "Could not get attribute value");
        goto cleanup;
    }

    /* Convert the UTF-16 string into a UTF-8 string. */
    value = jniDecodeString(env, tempObject, NULL);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return value;
}

long jni_SetAttrValue(mxmlCtxt *ctxt, mxmlNode *attr, char *value) 
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;

    jstring jValue;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jValue = (*env)->NewStringUTF(env, value);

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           xmlAdapterObject, 
		           setAttrValueMethod, 
			   (jobject) attr,
			   jValue);
    if ((*env)->ExceptionCheck(env))
    {
	status = eMXML_SET_ATTR_VALUE;
	mxmlError(ctxt, status, "Could not set attribute value");
        goto cleanup;
    }

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return status;
}

char *jni_GetPITarget(mxmlCtxt *ctxt, mxmlNode *pi)
{
    JNIEnv *env = jniGetEnv( );

    char *target = NULL;

    jobject tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getPITargetMethod, 
				          (jobject) pi);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, 
		  eMXML_GET_PI_TARGET, 
		  "Could not get processing instruction target");
        goto cleanup;
    }

    /* Convert the UTF-16 string into a UTF-8 string. */
    target = jniDecodeString(env, tempObject, NULL);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return target;
}

char *jni_GetPIData(mxmlCtxt *ctxt, mxmlNode *pi)
{
    JNIEnv *env = jniGetEnv( );

    char *data = NULL;

    jobject tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getPIDataMethod, 
				          (jobject) pi);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, 
		  eMXML_GET_PI_DATA, 
		  "Could not get processing instruction data");
        goto cleanup;
    }

    /* Convert the UTF-16 string into a UTF-8 string. */
    data = jniDecodeString(env, tempObject, NULL);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return data;
}

long jni_SetPIData(mxmlCtxt *ctxt, mxmlNode *pi, char *data)
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;

    jstring jData;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jData = (*env)->NewStringUTF(env, data);

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           xmlAdapterObject, 
		           setPIDataMethod, 
			   (jobject) pi,
			   jData);
    if ((*env)->ExceptionCheck(env))
    {
	status = eMXML_SET_PI_DATA;
	mxmlError(ctxt, status, "Could not set processing instruction data");
        goto cleanup;
    }

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return status;
}

char *jni_GetCharacterData(mxmlCtxt *ctxt, mxmlNode *node)
{
    JNIEnv *env = jniGetEnv( );

    char *value = NULL;

    jobject tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getCharacterDataMethod, 
				          (jobject) node);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_GET_CDATA, "Could not get character data");
        goto cleanup;
    }

    /* Convert the UTF-16 string into a UTF-8 string. */
    value = jniDecodeString(env, tempObject, NULL);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return value;
}

long jni_SetCharacterData(mxmlCtxt *ctxt, mxmlNode *node, char *data)
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;

    jstring jData;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jData = (*env)->NewStringUTF(env, data);

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           xmlAdapterObject, 
		           setCharacterDataMethod, 
			   (jobject) node,
			   jData);
    if ((*env)->ExceptionCheck(env))
    {
	status = eMXML_SET_CDATA;
	mxmlError(ctxt, status, "Could not set character data");
        goto cleanup;
    }

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return status;
}

long jni_GetCharacterDataLength(mxmlCtxt *ctxt, mxmlNode *node)
{
    JNIEnv *env = jniGetEnv( );

    long value;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Call the Java method. */
    value = (*env)->CallIntMethod(env, 
		                  xmlAdapterObject, 
		                  getCharacterDataLengthMethod, 
				  (jobject) node);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, 
		  eMXML_GET_CDATA_LENGTH, 
		  "Could not get character data length");
        goto cleanup;
    }

cleanup:

    return value;
}

long jni_GetNodeListLength(mxmlCtxt *ctxt, mxmlNodeList *nodeList)
{
    JNIEnv *env = jniGetEnv( );

    long value;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Call the Java method. */
    value = (*env)->CallIntMethod(env, 
		                  xmlAdapterObject, 
		                  getNodeListLengthMethod, 
				  (jobject) nodeList);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, 
	          eMXML_GET_LIST_LENGTH,
		  "Could not get node list length");
        goto cleanup;
    }

cleanup:

    return value;
}

mxmlNode *jni_GetItem(mxmlCtxt*ctxt, mxmlNodeList *nodeList, long index)
{
    JNIEnv *env = jniGetEnv( );

    jobject node       = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
   
    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getItemMethod, 
					  (jobject) nodeList,
				          (jint) index);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_GET_ITEM, "Could not get item");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

mxmlNode *jni_GetNamedItem(mxmlCtxt *ctxt, mxmlNodeList *nodeList, char *name)
{
    JNIEnv *env = jniGetEnv( );

    jstring jName;

    jobject node       = NULL,
	    tempObject = NULL;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);
    
    /* Get a Java string from a UTF-8 string. */
    jName = (*env)->NewStringUTF(env, name);

    /* Call the Java method. */
    tempObject = (*env)->CallObjectMethod(env, 
		                          xmlAdapterObject, 
		                          getNamedItemMethod, 
					  (jobject) nodeList,
				          jName);
    if ((*env)->ExceptionCheck(env))
    {
	mxmlError(ctxt, eMXML_GET_NAMED_ITEM, "Could not get named item");
        goto cleanup;
    }

    /* Create a new global reference of this local reference. */
    node = (*env)->NewGlobalRef(env, tempObject);

    /* Add this new global reference to the context's global reference list. */
    sAddToGlobalRefList(ctxt, node);

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return (mxmlNode *) node;
}

long jni_SetNamedItem(mxmlCtxt *ctxt, mxmlNodeList *nodeList, mxmlNode *node)
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           xmlAdapterObject, 
		           setNamedItemMethod, 
			   (jobject) nodeList,
			   (jobject) node);
    if ((*env)->ExceptionCheck(env))
    {
	status = eMXML_SET_NAMED_ITEM;
	mxmlError(ctxt, status, "Could not set named item");
        goto cleanup;
    }

cleanup:

    return status;
}

long jni_RemoveNamedItem(mxmlCtxt *ctxt, mxmlNodeList *nodeList, char *name)
{
    JNIEnv *env = jniGetEnv( );

    long status = eOK;

    jstring jName;

    jobject xmlAdapterObject = ctxt->xmlAdapterObject;

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /* Get a Java string from a UTF-8 string. */
    jName = (*env)->NewStringUTF(env, name);

    /* Call the Java method. */
    (*env)->CallVoidMethod(env, 
		           xmlAdapterObject, 
		           setNamedItemMethod, 
			   (jobject) nodeList,
			   jName);
    if ((*env)->ExceptionCheck(env))
    {
	status = eMXML_REMOVE_NAMED_ITEM;
	mxmlError(ctxt, status, "Could not remove named item");
        goto cleanup;
    }

cleanup:

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return status;
}
