/*#START************************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: XML library private header file.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2002-2008
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

#ifndef MXMLPRIVATE_H
#define MXMLPRIVATE_H

#include <jni.h>
#include <mxmllib.h>

/*
 * Type Definitions
 */

typedef struct mxml_GlobalRef_s mxml_GlobalRef;

/*
 * Structures
 */

struct mxml_GlobalRef_s
{
    jobject globalRef;

    struct mxml_GlobalRef_s *next;
};

/*
 * Opaque Structures
 */

struct mxmlCtxt_s
{
    long errorNumber;
    char *errorText;

    short validate;
    short preserveWhitespace;

    jobject xmlAdapterObject;

    mxml_GlobalRef *globalRefList;
};

/*
 * Function Prototypes
 */

#if defined (__cplusplus)
extern "C" {
#endif

long      jni_StartParser(mxmlCtxt **ctxt);
long      jni_ConfigureParser(mxmlCtxt *ctxt, 
	                      short     validate,
		              short     preserveWhitespace);
void      jni_StopParser(mxmlCtxt *ctxt);

void      jni_ParserInfo(mxmlCtxt *ctxt);
void      jni_DumpContext(mxmlCtxt *ctxt);

void      jni_Error(mxmlCtxt *ctxt,
	            long      errorNumber,
		    char     *msg);
long      jni_ErrorNumber(mxmlCtxt *ctxt);
char     *jni_ErrorText(mxmlCtxt *ctxt);

mxmlNode *jni_ParseString(mxmlCtxt *ctxt, 
	                  char     *xmlString);
mxmlNode *jni_ParseFile(mxmlCtxt *ctxt, 
	                char     *filename);

char     *jni_String(mxmlCtxt *ctxt, mxmlNode *node);
char     *jni_RawString(mxmlCtxt *ctxt, mxmlNode *node);

long      jni_WriteFile(mxmlCtxt *ctxt,
	                mxmlNode *node,
	                char     *filename);

long      jni_RawWriteFile(mxmlCtxt *ctxt,
	                   mxmlNode *node,
	                   char     *filename);

mxmlNode *jni_AddElement(mxmlCtxt *ctxt,
	                 mxmlNode *parent,
		         char     *data);
mxmlNode *jni_AddTextNode(mxmlCtxt *ctxt,
	                  mxmlNode *parent,
		          char     *data);
mxmlNode *jni_AddComment(mxmlCtxt *ctxt,
	                 mxmlNode *parent,
		         char     *data);
mxmlNode *jni_AddPI(mxmlCtxt *ctxt,
	            mxmlNode *parent,
	            char     *target,
	            char     *data);
mxmlNode *jni_AddAttribute(mxmlCtxt *ctxt,
	                   mxmlNode *parent,
	                   char     *name,
	                   char     *value);

mxmlCtxt *jni_ApplyStylesheetFromFile(mxmlCtxt *xmlCtxt,
	                              char     *pathname);
mxmlCtxt *jni_ApplyStylesheetFromString(mxmlCtxt *xmlCtxt,
	                                char     *xsl);

mxmlNode *jni_CreateDocument(mxmlCtxt *ctxt);
mxmlNode *jni_GetDocument(mxmlCtxt *ctxt);

mxmlNode *jni_InsertBefore(mxmlCtxt *ctxt,
	                   mxmlNode *parent, 
	                   mxmlNode *newChild, 
	                   mxmlNode *refChild);
long      jni_ReplaceChild(mxmlCtxt *ctxt,
	                   mxmlNode *parent, 
	                   mxmlNode *newChild, 
		           mxmlNode *oldChild);
long      jni_RemoveChild(mxmlCtxt *ctxt,
	                  mxmlNode *parent, 
	                  mxmlNode *child);
mxmlNode *jni_AppendChild(mxmlCtxt *ctxt,
	                  mxmlNode *parent, 
		          mxmlNode *newChild);
long      jni_HasChildNodes(mxmlCtxt *ctxt,
	                    mxmlNode *parent);
mxmlNode *jni_CloneNode(mxmlCtxt *ctxt,
	                mxmlNode *node, 
	                long      deep);
mxmlNode *jni_ImportNode(mxmlCtxt *ctxt,
	                 mxmlNode *node, 
	                 long      deep);
char     *jni_GetNodeName(mxmlCtxt *ctxt,
	                  mxmlNode *node);
char     *jni_GetNodeValue(mxmlCtxt *ctxt,
	                   mxmlNode *node);
long      jni_SetNodeValue(mxmlCtxt *ctxt,
	                   mxmlNode *node, 
	                   char     *value);
mxmlNodeType
          jni_GetNodeType(mxmlCtxt *ctxt,
	                  mxmlNode *node);
mxmlNode *jni_GetParentNode(mxmlCtxt *ctxt,
	                    mxmlNode *child);
mxmlNodeList 
         *jni_GetChildNodes(mxmlCtxt *ctxt,
	                    mxmlNode *parent);
mxmlNode *jni_GetFirstChild(mxmlCtxt *ctxt,
	                    mxmlNode *parent);
mxmlNode *jni_GetLastChild(mxmlCtxt *ctxt,
	                   mxmlNode *parent);
mxmlNode *jni_GetPreviousSibling(mxmlCtxt *ctxt,
	                         mxmlNode *sibling);
mxmlNode *jni_GetNextSibling(mxmlCtxt *ctxt,
	                     mxmlNode *sibling);
mxmlNodeList 
         *jni_GetAttributes(mxmlCtxt *ctxt,
	                    mxmlNode *node);
char     *jni_GetAttribute(mxmlCtxt *ctxt,
	                   mxmlNode *element, 
	                   char     *name);
long      jni_SetAttribute(mxmlCtxt *ctxt,
	                   mxmlNode *element, 
	                   char     *name, 
			   char     *value);
long      jni_RemoveAttribute(mxmlCtxt *ctxt,
	                      mxmlNode *element, 
	                      char     *name);
mxmlNode *jni_GetAttributeNode(mxmlCtxt *ctxt,
	                       mxmlNode *element, 
	                       char     *name);
long      jni_SetAttributeNode(mxmlCtxt *ctxt,
	                       mxmlNode *element, 
	                       mxmlNode *attr);
long      jni_RemoveAttributeNode(mxmlCtxt *ctxt,
	                          mxmlNode *element, 
	                          mxmlNode *attr);
mxmlNodeList 
         *jni_GetElementsByTagName(mxmlCtxt *ctxt,
	                           mxmlNode *element, 
	                           char     *name);
char     *jni_GetTagName(mxmlCtxt *ctxt,
	                 mxmlNode *element);
mxmlNode *jni_CreateElement(mxmlCtxt *ctxt,
	                    char     *name);
mxmlNode *jni_CreateDocumentFragment(mxmlCtxt *ctxt);
mxmlNode *jni_CreateTextNode(mxmlCtxt *ctxt,
	                     char     *data);
mxmlNode *jni_CreateComment(mxmlCtxt *ctxt,
	                    char     *data);
mxmlNode *jni_CreateCDATASection(mxmlCtxt *ctxt,
	                         char     *data);
mxmlNode *jni_CreatePI(mxmlCtxt *ctxt,
	               char     *target, 
		       char     *data);
mxmlNode *jni_CreateAttribute(mxmlCtxt *ctxt,
	                      char     *name);
mxmlNode *jni_GetDocumentElement(mxmlCtxt *ctxt);
char     *jni_GetAttrName(mxmlCtxt *ctxt,
	                  mxmlNode *attr);
long      jni_GetAttrSpecified(mxmlCtxt *ctxt,
	                       mxmlNode *attr);
char     *jni_GetAttrValue(mxmlCtxt *ctxt,
	                   mxmlNode *attr);
long      jni_SetAttrValue(mxmlCtxt *ctxt,
	                   mxmlNode *attr, 
			   char     *value);
char     *jni_GetPITarget(mxmlCtxt *ctxt,
			  mxmlNode *pi);
char     *jni_GetPIData(mxmlCtxt *ctxt,
			mxmlNode *pi);
long      jni_SetPIData(mxmlCtxt *ctxt,
			mxmlNode *pi,
			char     *data);
char     *jni_GetCharacterData(mxmlCtxt *ctxt,
			       mxmlNode *node);
long      jni_SetCharacterData(mxmlCtxt *ctxt,
			       mxmlNode *node, 
			       char     *data);
long      jni_GetCharacterDataLength(mxmlCtxt *ctxt,
				     mxmlNode *node);
long      jni_GetNodeListLength(mxmlCtxt     *ctxt,
	                        mxmlNodeList *nodeList);
mxmlNode *jni_GetItem(mxmlCtxt     *ctxt,
	              mxmlNodeList *nodeList, 
		      long          index);
mxmlNode *jni_GetNamedItem(mxmlCtxt     *ctxt,
	                   mxmlNodeList *nodeList, 
			   char         *name);
long      jni_SetNamedItem(mxmlCtxt     *ctxt,
	                   mxmlNodeList *nodeList, 
			   mxmlNode     *newNode);
long      jni_RemoveNamedItem(mxmlCtxt     *ctxt,
	                      mxmlNodeList *nodeList, 
			      char         *name);

#if defined (__cplusplus)
}
#endif

#endif
