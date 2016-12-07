static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: XML library C API.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <jnilib.h>
#include <mxmllib.h>
#include <oslib.h>

#include "mxmlprivate.h"

/*
 *  FUNCTION: mxmlStartParser
 *
 *  PURPOSE:  Start a new document creation or parsing session.
 *
 *  RETURNS:  eOK
 *            MOCA/MSXML error number
 */

long mxmlStartParser(mxmlCtxt **ctxt)
{
    /* Call the driver. */
    return jni_StartParser(ctxt);
}

/*
 *  FUNCTION: mxmlStopParser
 *
 *  PURPOSE:  Stop a document creation or parsing session.
 *
 *  RETURNS:  void
 */

void mxmlStopParser(mxmlCtxt *ctxt)
{
    /* Validate arguments. */
    if (!ctxt)
	return;

    /* Call the driver. */
    jni_StopParser(ctxt);

    return;
}

/*
 *  FUNCTION: mxmlConfigureParser
 *
 *  PURPOSE:  Set configuration of this parsing session.
 *
 *  RETURNS:  eOK
 *            MOCA/MSXML error number
 */

long mxmlConfigureParser(mxmlCtxt *ctxt, 
	                 short validate, 
			 short preserveWhitespace)
{
    /* Call the driver. */
    return jni_ConfigureParser(ctxt, validate, preserveWhitespace);
}

/*
 *  FUNCTION: mxmlInfo
 *
 *  PURPOSE:  Determine the parser being used.
 *
 *  RETURNS:  Code representing parser type.
 *            MOCA/MSXML error number
 */

long mxmlInfo(int *parserType)
{
    if (parserType)
        *parserType = MOCA_XML_JAXP;

    return eOK;
}

/*
 *  FUNCTION: mxmlDumpContext
 *
 *  PURPOSE:  Dump internal context information for debugging.
 *
 *  RETURNS:  void
 */

void mxmlDumpContext(mxmlCtxt *ctxt)
{

    /* Validate arguments. */
    if (!ctxt)
        return;

    /* Call the driver. */
    jni_DumpContext(ctxt);

    return;
}

/*
 *  FUNCTION: mxmlError
 *
 *  PURPOSE:  Handle an error condition.
 *
 *  RETURNS:  void
 */

void mxmlError(mxmlCtxt *ctxt, long errorNumber, char *msg)
{
    /* Validate arguments. */
    if (!ctxt)
        return;

    /* Call the driver. */
    jni_Error(ctxt, errorNumber, msg);

    return;
}

/*
 *  FUNCTION: mxmlErrorNumber
 *
 *  PURPOSE:  Get the latest error number.
 *
 *  RETURNS:  Latest error number.
 */

long mxmlErrorNumber(mxmlCtxt *ctxt)
{
    /* Validate arguments. */
    if (!ctxt)
        return eINVALID_ARGS;

    /* Call the driver. */
    return jni_ErrorNumber(ctxt);
}

/*
 *  FUNCTION: mxmlErrorText
 *
 *  PURPOSE:  Get the latest error text.
 *
 *  RETURNS:  Latest error text.
 */

char *mxmlErrorText(mxmlCtxt *ctxt)
{
    /* Validate arguments. */
    if (!ctxt)
        return NULL;

    /* Call the driver. */
    return jni_ErrorText(ctxt);
}

/*
 *  FUNCTION: mxmlParseString
 *
 *  PURPOSE:  Parse the given string.
 *
 *  RETURNS:  Pointer to the root document element.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlParseString(mxmlCtxt *ctxt, char *xml)
{
    /* Validate arguments. */
    if (!ctxt || !xml || !strlen(xml))
        return NULL;

    /* Call the driver. */
    return jni_ParseString(ctxt, xml);
}

/*
 *  FUNCTION: mxmlParseFile
 *
 *  PURPOSE:  Parse the given file.
 *
 *  RETURNS:  Pointer to the root document element.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlParseFile(mxmlCtxt *ctxt, char *filename)
{
    /* Validate arguments. */
    if (!ctxt || !filename || !strlen(filename))
        return NULL;

    /* Call the driver. */
    return jni_ParseFile(ctxt, filename);
}

/*
 *  FUNCTION: mxmlString
 *
 *  PURPOSE:  Create a string from this XML node.
 *
 *  RETURNS:  Pointer to the string.
 *            NULL - An error occurred.
 */

char *mxmlString(mxmlCtxt *ctxt, mxmlNode *node)
{
    char *xml;

    OS_TIME mytime; osGetTime(&mytime);

    /* Work with the entire document if a node wasn't given. */
    node = node ? node : mxmlGetDocument(ctxt);

    /* Validate arguments. */
    if (!ctxt || !node)
        return NULL;

    /* Call the driver. */
    xml = jni_String(ctxt, node);

    /*
    AccumulateTime(&TotalTime_String, &mytime);
    */

    return xml;
}

/*
 *  FUNCTION: mxmlRawString
 *
 *  PURPOSE:  Create a raw string using the XML document.
 *
 *  RETURNS:  Pointer to the string.
 *            NULL - An error occurred.
 */

char *mxmlRawString(mxmlCtxt *ctxt, mxmlNode *node)
{
    /* Work with the entire document if a node wasn't given. */
    node = node ? node : mxmlGetDocument(ctxt);

    /* Validate arguments. */
    if (!ctxt || !node)
        return NULL;

    /* Call the driver. */
    return jni_RawString(ctxt, node);
}

/*
 *  FUNCTION: mxmlWriteFile
 *
 *  PURPOSE:  Write the document to filename.
 *
 *  RETURNS:  eOK
 *            MOCA/MSXML error number
 */

long mxmlWriteFile(mxmlCtxt *ctxt, mxmlNode *node, char *filename)
{
    /* Work with the entire document if a node wasn't given. */
    node = node ? node : mxmlGetDocument(ctxt);

    /* Validate arguments. */
    if (!ctxt || !node || !filename || !strlen(filename))
        return eINVALID_ARGS;

    return jni_WriteFile(ctxt, node, filename);
}

/*
 *  FUNCTION: mxmlRawWriteFile
 *
 *  PURPOSE:  Write the raw document to filename.
 *
 *  RETURNS:  eOK
 *            MOCA/MSXML error number
 */

long mxmlRawWriteFile(mxmlCtxt *ctxt, mxmlNode *node, char *filename)
{
    /* Work with the entire document if a node wasn't given. */
    node = node ? node : mxmlGetDocument(ctxt);

    /* Validate arguments. */
    if (!ctxt || !node || !filename || !strlen(filename))
        return eINVALID_ARGS;

    return jni_RawWriteFile(ctxt, node, filename);
}

/*
 *  FUNCTION: mxmlAddElement
 *
 *  PURPOSE:  Create a new element and add it to the parent.
 *
 *  RETURNS:  Pointer to the new element.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlAddElement(mxmlCtxt *ctxt, mxmlNode *parent, char *name)
{
    /* Work with the entire document if a parent wasn't given. */
    parent = parent ? parent : mxmlGetDocumentElement(ctxt);
    parent = parent ? parent : mxmlGetDocument(ctxt);

    /* Validate arguments. */
    if (!ctxt || !parent || !name || !strlen(name))
        return NULL;

    /* Call the driver. */
    return jni_AddElement(ctxt, parent, name);
}

/*
 *  FUNCTION: mxmlAddTextNode
 *
 *  PURPOSE:  Create a new text node and add it to the parent.
 *
 *  RETURNS:  Pointer to the new text node.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlAddTextNode(mxmlCtxt *ctxt, mxmlNode *parent, char *data)
{
    /* Work with the entire document if a parent wasn't given. */
    parent = parent ? parent : mxmlGetDocumentElement(ctxt);
    parent = parent ? parent : mxmlGetDocument(ctxt);

    /* Validate arguments. */
    if (!ctxt || !parent || !data)
        return NULL;

    /* Call the driver. */
    return jni_AddTextNode(ctxt, parent, data);
}

/*
 *  FUNCTION: mxmlAddComment
 *
 *  PURPOSE:  Create a new comment and add it to the parent.
 *
 *  RETURNS:  Pointer to the new comment.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlAddComment(mxmlCtxt *ctxt, mxmlNode *parent, char *data)
{
    /* Work with the entire document if a parent wasn't given. */
    parent = parent ? parent : mxmlGetDocumentElement(ctxt);
    parent = parent ? parent : mxmlGetDocument(ctxt);

    /* Validate arguments. */
    if (!ctxt || !parent || !data || !strlen(data))
        return NULL;

    /* Call the driver. */
    return jni_AddComment(ctxt, parent, data);
}

/*
 *  FUNCTION: mxmlAddPI
 *
 *  PURPOSE:  Create a new processing instruction and add it to the parent.
 *
 *  RETURNS:  Pointer to the new processing instruction.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlAddPI(mxmlCtxt *ctxt, mxmlNode *parent, char *target, char *data)
{
    /* Work with the entire document if a parent wasn't given. */
    parent = parent ? parent : mxmlGetDocumentElement(ctxt);
    parent = parent ? parent : mxmlGetDocument(ctxt);

    /* Validate arguments. */
    if (!ctxt || !parent || !target || !strlen(target) || !data || !strlen(data))
        return NULL;

    /* Call the driver. */
    return jni_AddPI(ctxt, parent, target, data);
}

/*
 *  FUNCTION: mxmlAddAttribute
 *
 *  PURPOSE:  Create a new attribute and add it to the parent.
 *
 *  RETURNS:  Pointer to the new attribute.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlAddAttribute(mxmlCtxt *ctxt, 
	                   mxmlNode *parent, 
			   char     *name, 
	                   char     *value)
{
    /* Work with the entire document if a parent wasn't given. */
    parent = parent ? parent : mxmlGetDocumentElement(ctxt);

    /* Validate arguments. */
    if (!ctxt || !parent || !name || !strlen(name) || !value)
    {
	mxmlError(ctxt, eINVALID_ARGS, "mxmlAddAttribute: Invalid arguments");
        return NULL;
    }

    /* Call the driver. */
    return jni_AddAttribute(ctxt, parent, name, value);
}

/*
 *  FUNCTION: mxmlApplyStylesheetFromFile
 *
 *  PURPOSE:  Apply the given stylesheet to the given document.
 *
 *  RETURNS:  Pointer to the transformed document.
 *            NULL - An error occurred.
 */

mxmlCtxt *mxmlApplyStylesheetFromFile(mxmlCtxt *xmlCtxt, char *pathname)
{
    /* Validate arguments. */
    if (!xmlCtxt || !pathname || ! strlen(pathname))
        return NULL;

    /* Call the driver. */
    return jni_ApplyStylesheetFromFile(xmlCtxt, pathname);
}

/*
 *  FUNCTION: mxmlApplyStylesheetFromString
 *
 *  PURPOSE:  Apply the given stylesheet to the given XSL string.
 *
 *  RETURNS:  Pointer to the transformed document.
 *            NULL - An error occurred.
 */

mxmlCtxt *mxmlApplyStylesheetFromString(mxmlCtxt *xmlCtxt, char *xsl)
{
    /* Validate arguments. */
    if (!xmlCtxt || !xsl || ! strlen(xsl))
        return NULL;

    /* Call the driver. */
    return jni_ApplyStylesheetFromString(xmlCtxt, xsl);
}

/*
 *  FUNCTION: mxmlCreateDocument
 *
 *  PURPOSE:  Creates an XML document object.
 *
 *  RETURNS:  Pointer to the new document.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlCreateDocument(mxmlCtxt *ctxt)
{
    /* Validate arguments. */
    if (!ctxt)
        return NULL;

    /* Call the driver. */
    return jni_CreateDocument(ctxt);
}

/*
 *  FUNCTION: mxmlGetDocument
 *
 *  PURPOSE:  Gets an XML document object.
 *
 *  RETURNS:  Pointer to the document.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlGetDocument(mxmlCtxt *ctxt)
{
    /* Validate arguments. */
    if (!ctxt)
        return NULL;

    /* Call the driver. */
    return jni_GetDocument(ctxt);
}

/*
 *  FUNCTION: mxmlInsertBefore
 *
 *  PURPOSE:  Inserts the node newChild before the existing node refChild.
 *            If refChild is null, insert newChild at the end of the list of
 *            children.  If newChild is a document fragment, all of its 
 *            children are inserted, in the same order, before refChild.  If 
 *            the newChild is already in the tree, it is first removed.
 *
 *  RETURNS:  Pointer to the new child node.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlInsertBefore(mxmlCtxt *ctxt,
	                   mxmlNode *parent,
		           mxmlNode *newChild,
		           mxmlNode *refChild)
{
    /* Get the parent if one wasn't passed. */
    parent = parent ? parent : mxmlGetParentNode(ctxt, refChild);

    /* Validate arguments. */
    if (!ctxt || !parent || !newChild)
        return NULL;

    /* Call the driver. */
    return jni_InsertBefore(ctxt, parent, newChild, refChild);
}

/*
 *  FUNCTION: mxmlReplaceChild
 *
 *  PURPOSE:  Replaces the child node oldChild with the child node newChild in
 *            the list of children.  If newChild is a document fragment, 
 *            oldChild is replaced by all of the document fragment children, 
 *            which are inserted in the same order.  If newChild is already in
 *            the tree, it is first removed.
 *
 *  RETURNS:  Pointer to the old child node.
 *            NULL - An error occurred.
 */

long mxmlReplaceChild(mxmlCtxt *ctxt, 
	              mxmlNode *parent, 
		      mxmlNode *newChild,
	              mxmlNode *oldChild)
{
    /* Get the parent if one wasn't passed. */
    parent = parent ? parent : mxmlGetParentNode(ctxt, oldChild);

    /* Validate arguments. */
    if (!ctxt || !parent || !newChild || !oldChild)
        return eINVALID_ARGS;

    /* Call the driver. */
    return jni_ReplaceChild(ctxt, parent, newChild, oldChild);
}

/*
 *  FUNCTION: mxmlRemoveChild
 *
 *  PURPOSE:  Removes the child node oldChild from the list of children.
 *
 *  RETURNS:  Pointer to the old child node.
 *            NULL - An error occurred.
 */

long mxmlRemoveChild(mxmlCtxt *ctxt, mxmlNode *parent, mxmlNode *oldChild)
{
    /* Get the parent if one wasn't passed. */
    parent = parent ? parent : mxmlGetParentNode(ctxt, oldChild);

    /* Validate arguments. */
    if (!ctxt || !parent || !oldChild)
        return eINVALID_ARGS;

    return jni_RemoveChild(ctxt, parent, oldChild);
}

/*
 *  FUNCTION: mxmlAppendChild
 *
 *  PURPOSE:  Adds the node newChild to the end of the list of children of 
 *            the node parent.  If the newChild is already in the tree,
 *            it is first removed.
 *
 *  RETURNS:  Pointer to the new child node.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlAppendChild(mxmlCtxt *ctxt, mxmlNode *parent, mxmlNode *newChild)
{
    /* Work with the entire document if a node wasn't given. */
    parent = parent ? parent : mxmlGetDocumentElement(ctxt);
    parent = parent ? parent : mxmlGetDocument(ctxt);

    /* Validate arguments. */
    if (!ctxt || !parent || !newChild)
        return NULL;

    /* Call the driver. */
    return jni_AppendChild(ctxt, parent, newChild);
}

/*
 *  FUNCTION: mxmlHasChildNodes
 *
 *  PURPOSE:  Determines whether this node has any children.
 *
 *  RETURNS:  1 - The node has child nodes.
 *            0 - The node does not have child nodes.
 */

long mxmlHasChildNodes(mxmlCtxt *ctxt, mxmlNode *node)
{
    /* Validate arguments. */
    if (!ctxt || !node)
        return eINVALID_ARGS;

    return jni_HasChildNodes(ctxt, node);
}

/*
 *  FUNCTION: mxmlCloneNode
 *
 *  PURPOSE:  Clones the given node.  The cloned node has no parent.  Cloning
 *            an element copies all attributes and their values, including 
 *            those generated by the XML processor to represent defaulted
 *            attributes, but this method does not copy any text it contains
 *            unless it is a deep clone, since the text is contained in a 
 *            child text node.  Cloning an attribute directly, as opposed to 
 *            cloning it as part of an element cloning operation, returns a 
 *            specified attribute (specified is true).  Cloning any other 
 *            type of node simpley clones a copy of the node.
 *
 *  RETURNS:  Pointer to the cloned node.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlCloneNode(mxmlCtxt *ctxt, mxmlNode *node, long deep)
{
    /* Validate arguments. */
    if (!ctxt || !node || (deep != MOCA_TRUE && deep != MOCA_FALSE))
        return NULL;

    return jni_CloneNode(ctxt, node, deep);
}

/*
 *  FUNCTION: mxmlImportNode
 *
 *  PURPOSE:  Imports the given node from one document to this document. 
 *            The imported node has no parent.  The source node is not altered 
 *            or removed from the original document; this method creates a new 
 *            copy of the source node.  For all nodes, importing a node creates
 *            a node object owned by the importing document, with attribute 
 *            values identical to the source node's nodeName and nodeType, plus
 *            the attributes related to namespaces (prefix, localName, and 
 *            namespaceURI). As in the cloneNode operation on a Node, the 
 *            source node is not altered.  
 *
 *  RETURNS:  Pointer to the imported node.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlImportNode(mxmlCtxt *ctxt, mxmlNode *node, long deep)
{
    /* Validate arguments. */
    if (!ctxt || !node || (deep != MOCA_TRUE && deep != MOCA_FALSE))
        return NULL;

    return jni_ImportNode(ctxt, node, deep);
}

/*
 *  FUNCTION: mxmlGetNodeName
 *
 *  PURPOSE:  Gets the name of this node.
 *
 *  RETURNS:  Pointer to the node name.
 *            NULL - An error occurred.
 */

char *mxmlGetNodeName(mxmlCtxt *ctxt, mxmlNode *node)
{
    /* Validate arguments. */
    if (!ctxt || !node)
        return NULL;

    return jni_GetNodeName(ctxt, node);
}

/*
 *  FUNCTION: mxmlGetNodeValue
 *
 *  PURPOSE:  Gets the value of this node.
 *
 *  RETURNS:  Pointer to the node value.
 *            NULL - An error occurred.
 */

char *mxmlGetNodeValue(mxmlCtxt *ctxt, mxmlNode *node)
{
    /* Validate arguments. */
    if (!ctxt || !node)
        return NULL;

    return jni_GetNodeValue(ctxt, node);
}

/*
 *  FUNCTION: mxmlSetNodeValue
 *
 *  PURPOSE:  Sets the value of this node.
 *
 *  RETURNS:  eOK - All ok.
 *            MOCA/MSXML error number.
 */

long mxmlSetNodeValue(mxmlCtxt *ctxt, mxmlNode *node, char *value)
{
    /* Validate arguments. */
    if (!ctxt || !node || !value || !strlen(value))
        return eINVALID_ARGS;

    return jni_SetNodeValue(ctxt, node, value);
}

/*
 *  FUNCTION: mxmlGetNodeType
 *
 *  PURPOSE:  Gets the node type of this node.
 *
 *  RETURNS:  Code representing node type.
 *            MOCA/MSXML error number
 */

mxmlNodeType mxmlGetNodeType(mxmlCtxt *ctxt, mxmlNode *node)
{
    /* Validate arguments. */
    if (!ctxt || !node)
        return MXML_NODE_TYPE_ERROR;

    return jni_GetNodeType(ctxt, node);
}

/*
 *  FUNCTION: mxmlGetParentNode
 *
 *  PURPOSE:  Gets the parent node of this node.
 *
 *  RETURNS:  Pointer to the parent node.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlGetParentNode(mxmlCtxt *ctxt, mxmlNode *node)
{
    /* Validate arguments. */
    if (!ctxt || !node)
        return NULL;

    return jni_GetParentNode(ctxt, node);
}

/*
 *  FUNCTION: mxmlGetChildNodes
 *
 *  PURPOSE:  Gets the list of child nodes for this node.
 *
 *  RETURNS:  Pointer to the list of child nodes.
 *            NULL - An error occurred.
 */

mxmlNodeList *mxmlGetChildNodes(mxmlCtxt *ctxt, mxmlNode *node)
{
    /* Validate arguments. */
    if (!ctxt || !node)
        return NULL;

    return jni_GetChildNodes(ctxt, node);
}

/*
 *  FUNCTION: mxmlGetFirstChild
 *
 *  PURPOSE:  Gets the first child of this node.
 *
 *  RETURNS:  Pointer to the first child.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlGetFirstChild(mxmlCtxt *ctxt, mxmlNode *parent)
{
    /* Work with the entire document if a parent wasn't given. */
    parent = parent ? parent : mxmlGetDocumentElement(ctxt);
    parent = parent ? parent : mxmlGetDocument(ctxt);

    /* Validate arguments. */
    if (!ctxt || !parent)
        return NULL;

    return jni_GetFirstChild(ctxt, parent);
}

/*
 *  FUNCTION: mxmlGetLastChild
 *
 *  PURPOSE:  Gets the last child of this node.
 *
 *  RETURNS:  Pointer to the last child.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlGetLastChild(mxmlCtxt *ctxt, mxmlNode *parent)
{
    /* Work with the entire document if a parent wasn't given. */
    parent = parent ? parent : mxmlGetDocumentElement(ctxt);
    parent = parent ? parent : mxmlGetDocument(ctxt);

    /* Validate arguments. */
    if (!ctxt || !parent)
        return NULL;

    return jni_GetLastChild(ctxt, parent);
}

/*
 *  FUNCTION: mxmlGetPreviousSibling
 *
 *  PURPOSE:  Gets the previous sibling of this node in the parent's child 
 *            list.
 *
 *  RETURNS:  Pointer to the previous sibling.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlGetPreviousSibling(mxmlCtxt *ctxt, mxmlNode *node)
{
    /* Validate arguments. */
    if (!ctxt || !node)
        return NULL;

    return jni_GetPreviousSibling(ctxt, node);
}

/*
 *  FUNCTION: mxmlGetNextSibling
 *
 *  PURPOSE:  Gets the next sibling of this node in the parent's child list.
 *
 *  RETURNS:  Pointer to the next sibling.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlGetNextSibling(mxmlCtxt *ctxt, mxmlNode *node)
{
    /* Validate arguments. */
    if (!ctxt || !node)
        return NULL;

    return jni_GetNextSibling(ctxt, node);
}

/*
 *  FUNCTION: mxmlGetAttributes
 *
 *  PURPOSE:  Gets the list of attributes for this node.
 *
 *  RETURNS:  Pointer to the list of attributes.
 *            NULL - An error occurred.
 */

mxmlNodeList *mxmlGetAttributes(mxmlCtxt *ctxt, mxmlNode *node)
{
    /* Validate arguments. */
    if (!ctxt || !node)
        return NULL;

    return jni_GetAttributes(ctxt, node);
}

/*
 *  FUNCTION: mxmlGetAttribute
 *
 *  PURPOSE:  Gets an attribute value by name.
 *
 *  RETURNS:  Pointer to the attribute.
 *            NULL - An error occurred.
 */

char *mxmlGetAttribute(mxmlCtxt *ctxt, mxmlNode *node, char *name)
{
    /* Validate arguments. */
    if (!ctxt || !node || !name || !strlen(name))
        return NULL;

    return jni_GetAttribute(ctxt, node, name);
}

/*
 *  FUNCTION: mxmlSetAttribute
 *
 *  PURPOSE:  Adds a new attribute.  If an attribute with that name is
 *            already present in the element, its value is changed to be that
 *            of the value parameter.
 *
 *  RETURNS:  eOK
 *            MOCA/MSXML error number
 */

long mxmlSetAttribute(mxmlCtxt *ctxt, 
	              mxmlNode *element, 
		      char *name, 
		      char *value)
{
    /* Validate arguments. */
    if (!ctxt || !element || !name || !strlen(name) || !value)
        return eINVALID_ARGS;

    return jni_SetAttribute(ctxt, element, name, value);
}

/*
 *  FUNCTION: mxmlRemoveAttribute
 *
 *  PURPOSE:  Removes an attribute by name.
 *
 *  RETURNS:  eOK
 *            MOCA/MSXML error number
 */

long mxmlRemoveAttribute(mxmlCtxt *ctxt, mxmlNode *element, char *name)
{
    /* Validate arguments. */
    if (!ctxt || !element || !name || !strlen(name))
        return eINVALID_ARGS;

    return jni_RemoveAttribute(ctxt, element, name);
}

/*
 *  FUNCTION: mxmlGetAttributeNode
 *
 *  PURPOSE:  Gets an attribute node by name.
 *
 *  RETURNS:  Pointer to the attribute node.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlGetAttributeNode(mxmlCtxt *ctxt, mxmlNode *element, char *name)
{
    /* Validate arguments. */
    if (!ctxt || !element || !name || !strlen(name))
        return NULL;

    return jni_GetAttributeNode(ctxt, element, name);
}

/*
 *  FUNCTION: mxmlSetAttributeNode
 *
 *  PURPOSE:  Adds a new attribute node.  If an attribute with that name is
 *            already present in the element, it is replaced with the new one.
 *
 *  RETURNS:  eOK
 *            MOCA/MSXML error number
 */

long mxmlSetAttributeNode(mxmlCtxt *ctxt, 
	                  mxmlNode *element, 
		          mxmlNode *attribute)
{
    /* Validate arguments. */
    if (!ctxt || !element || !attribute)
        return eINVALID_ARGS;

    return jni_SetAttributeNode(ctxt, element, attribute);
}

/*
 *  FUNCTION: mxmlRemoveAttributeNode
 *
 *  PURPOSE:  Removes an attribute node.
 *
 *  RETURNS:  Pointer to the attribute node.
 */

long mxmlRemoveAttributeNode(mxmlCtxt *ctxt, 
	                     mxmlNode *element, 
	                     mxmlNode *attribute)
{
    /* Validate arguments. */
    if (!ctxt || !element || !attribute)
        return eINVALID_ARGS;

    return jni_RemoveAttributeNode(ctxt, element, attribute);
}
/*
 *  FUNCTION: mxmlGetElementsByTagName
 *
 *  PURPOSE:  Gets a node list of all descendant elements with of the
 *            given tag name, in the order in which they are encountered
 *            in a preorder traveral of this element tree.
 *
 *  RETURNS:  Pointer to the list of elements.
 *            NULL - An error occurred.
 */

mxmlNodeList *mxmlGetElementsByTagName(mxmlCtxt *ctxt, 
	                               mxmlNode *parent, 
				       char     *name)
{
    /* Work with the entire document if a node wasn't given. */
    parent = parent ? parent : mxmlGetDocumentElement(ctxt);
    parent = parent ? parent : mxmlGetDocument(ctxt);

    /* Validate arguments. */
    if (!ctxt || !parent || !name || !strlen(name))
        return NULL;

    return jni_GetElementsByTagName(ctxt, parent, name);
}

/*
 *  FUNCTION: mxmlGetTagName
 *
 *  PURPOSE:  Gets the tag name of this element.  This is currently the
 *            same as the node name, but the DOM Working Group considers
 *            it worthwhile to support both for now.
 *
 *  RETURNS:  Pointer to the tag name.
 *            NULL - An error occurred.
 */

char *mxmlGetTagName(mxmlCtxt *ctxt, mxmlNode *element)
{
    /* Validate arguments. */
    if (!ctxt || !element)
        return NULL;

    return jni_GetTagName(ctxt, element);
}

/*
 *  FUNCTION: mxmlCreateElement
 *
 *  PURPOSE:  Creates an element of the type specified.  Note that the 
 *            instance returned implements the Element interface, so 
 *            attributes can be specified directly on the returned object.  
 *            In addition, if there are known attributes with default values, 
 *            attribute nodes representing them are automatically created and 
 *            attached to the element.
 *
 *  RETURNS:  Pointer to the new element.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlCreateElement(mxmlCtxt *ctxt, char *name)
{
    /* Validate arguments. */
    if (!ctxt || !name || !strlen(name))
        return NULL;

    /* Call the driver. */
    return jni_CreateElement(ctxt, name);
}

/*
 *  FUNCTION: mxmlCreateDocumentFragment
 *
 *  PURPOSE:  Create an empty document fragment object.
 *
 *  RETURNS:  Pointer to the new document fragment.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlCreateDocumentFragment(mxmlCtxt *ctxt)
{
    /* Validate arguments. */
    if (!ctxt)
        return NULL;

    /* Call the driver. */
    return jni_CreateDocumentFragment(ctxt);
}

/*
 *  FUNCTION: mxmlCreateTextNode
 *
 *  PURPOSE:  Creates a text node given the specified string.
 *
 *  RETURNS:  Pointer to the new text node.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlCreateTextNode(mxmlCtxt *ctxt, char *data)
{
    /* Validate arguments. */
    if (!ctxt || !data)
        return NULL;

    /* Call the driver. */
    return jni_CreateTextNode(ctxt, data);
}

/*
 *  FUNCTION: mxmlCreateComment
 *
 *  PURPOSE:  Creates a comment node given the specified string.
 *
 *  RETURNS:  Pointer to the new comment.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlCreateComment(mxmlCtxt *ctxt, char *data)
{
    /* Validate arguments. */
    if (!ctxt || !data || !strlen(data))
        return NULL;

    /* Call the driver. */
    return jni_CreateComment(ctxt, data);
}

/*
 *  FUNCTION: mxmlCreateCDATASection
 *
 *  PURPOSE:  Create a CDATA section node whose value is the specified string.
 *
 *  RETURNS:  Pointer to the new CDATA section.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlCreateCDATASection(mxmlCtxt *ctxt, char *data)
{
    /* Validate arguments. */
    if (!ctxt || !data || !strlen(data))
        return NULL;

    /* Call the driver. */
    return jni_CreateCDATASection(ctxt, data);
}

/*
 *  FUNCTION: mxmlCreatePI
 *
 *  PURPOSE:  Create a processing instruction node given the specified
 *            target and data strings.
 *
 *  RETURNS:  Pointer to the new processing instruction.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlCreatePI(mxmlCtxt *ctxt, char *target, char *data)
{
    /* Validate arguments. */
    if (!ctxt || !target || !strlen(target) || !data || !strlen(data))
        return NULL;

    /* Call the driver. */
    return jni_CreatePI(ctxt, target, data);
}

/*
 *  FUNCTION: mxmlCreateAttribute
 *
 *  PURPOSE:  Create an attribute of the given name.  
 *
 *  RETURNS:  Pointer to the new attribute.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlCreateAttribute(mxmlCtxt *ctxt, char *name)
{
    /* Validate arguments. */
    if (!ctxt || !name || !strlen(name))
        return NULL;

    /* Call the driver. */
    return jni_CreateAttribute(ctxt, name);
}

/*
 *  FUNCTION: mxmlGetDocumentElement
 *
 *  PURPOSE:  Get the root element of the document.
 *
 *  RETURNS:  Pointer to the root element.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlGetDocumentElement(mxmlCtxt *ctxt)
{
    /* Validate arguments. */
    if (!ctxt)
        return NULL;

    /* Call the driver. */
    return jni_GetDocumentElement(ctxt);
}

/*
 *  FUNCTION: mxmlGetAttrName
 *
 *  PURPOSE:  Get the name of the given attribute.
 *
 *  RETURNS:  Pointer to the attribute name.
 *            NULL - An error occurred.
 */

char *mxmlGetAttrName(mxmlCtxt *ctxt, mxmlNode *attr)
{
    /* Validate arguments. */
    if (!ctxt || !attr)
        return NULL;

    /* Call the driver. */
    return jni_GetAttrName(ctxt, attr);
}

/*
 *  FUNCTION: mxmlGetAttrSpecified
 *
 *  PURPOSE:  Get whether or not the given attribute's value was explicitly
 *            specifed in the orginal document.
 *
 *  RETURNS:  1 - The attribute's value was explicity specified.
 *            0 - The attribute's value was not explicitly specified.
 *            MOCA/MSXML error number
 */

long mxmlGetAttrSpecified(mxmlCtxt *ctxt, mxmlNode *attr)
{
    /* Validate arguments. */
    if (!ctxt || !attr)
        return eINVALID_ARGS;

    /* Call the driver. */
    return jni_GetAttrSpecified(ctxt, attr);
}

/*
 *  FUNCTION: mxmlGetAttrValue
 *
 *  PURPOSE:  Get the value of the given attribute.
 *
 *  RETURNS:  Pointer to the attribute value.
 *            NULL - An error occurred.
 */

char *mxmlGetAttrValue(mxmlCtxt *ctxt, mxmlNode *attr)
{
    /* Validate arguments. */
    if (!ctxt || !attr)
        return NULL;

    /* Call the driver. */
    return jni_GetAttrValue(ctxt, attr);
}

/*
 *  FUNCTION: mxmlSetAttrValue
 *
 *  PURPOSE:  Set the value of the given attribute.
 *
 *  RETURNS:  eOK
 *            MOCA/MSXML error number
 */

long mxmlSetAttrValue(mxmlCtxt *ctxt, mxmlNode *attr, char *value) 
{
    /* Validate arguments. */
    if (!ctxt || !attr || !value || !strlen(value))
        return eINVALID_ARGS;

    /* Call the driver. */
    return jni_SetAttrValue(ctxt, attr, value);
}

/*
 *  FUNCTION: mxmlGetPITarget
 *
 *  PURPOSE:  Gets the target for this processing instruction.
 *
 *  RETURNS:  Pointer to the processing instruction target.
 *            NULL - An error occurred.
 */

char *mxmlGetPITarget(mxmlCtxt *ctxt, mxmlNode *pi)
{
    /* Validate arguments. */
    if (!ctxt || !pi)
        return NULL;

    return jni_GetPITarget(ctxt, pi);
}

/*
 *  FUNCTION: mxmlGetPIData
 *
 *  PURPOSE:  Gets the data for this processing instruction.
 *
 *  RETURNS:  Pointer to the processing instruction data.
 *            NULL - An error occurred.
 */

char *mxmlGetPIData(mxmlCtxt *ctxt, mxmlNode *pi)
{
    /* Validate arguments. */
    if (!ctxt || !pi)
        return NULL;

    return jni_GetPIData(ctxt, pi);
}

/*
 *  FUNCTION: mxmlSetPIData
 *
 *  PURPOSE:  Sets the data for this processing instruction.
 *
 *  RETURNS:  eOK
 *            MOCA/MSXML error number
 */

long mxmlSetPIData(mxmlCtxt *ctxt, mxmlNode *pi, char *data)
{
    /* Validate arguments. */
    if (!ctxt || !pi || !data || !strlen(data))
        return eINVALID_ARGS;

    return jni_SetPIData(ctxt, pi, data);
}

/*
 *  FUNCTION: mxmlGetCharacterData
 *
 *  PURPOSE:  Gets the character data for this CDATA or text node.
 *
 *  RETURNS:  Pointer to the character data.
 *            NULL - An error occurred.
 */

char *mxmlGetCharacterData(mxmlCtxt *ctxt, mxmlNode *node)
{
    /* Validate arguments. */
    if (!ctxt || !node)
        return NULL;

    return jni_GetCharacterData(ctxt, node);
}

/*
 *  FUNCTION: mxmlSetCharacterData
 *
 *  PURPOSE:  Sets the character data for this CDATA or text node.
 *
 *  RETURNS:  eOK
 *            MOCA/MSXML error number
 */

long mxmlSetCharacterData(mxmlCtxt *ctxt, mxmlNode *node, char *data)
{
    /* Validate arguments. */
    if (!ctxt || !node || !data || !strlen(data))
        return eINVALID_ARGS;

    return jni_SetCharacterData(ctxt, node, data);
}

/*
 *  FUNCTION: mxmlGetCharacterDataLength
 *
 *  PURPOSE:  Gets the length of this character data.
 *
 *  RETURNS:  Length of character data.
 *            MOCA/MSXML error number
 */

long mxmlGetCharacterDataLength(mxmlCtxt *ctxt, mxmlNode *node)
{
    /* Validate arguments. */
    if (!ctxt || !node)
        return eINVALID_ARGS;

    return jni_GetCharacterDataLength(ctxt, node);
}

/*
 *  FUNCTION: mxmlGetNodeListLength
 *
 *  PURPOSE:  Gets the length of this node list.
 *
 *  RETURNS:  Length of node list.
 *            MOCA/MSXML error number
 */

long mxmlGetNodeListLength(mxmlCtxt *ctxt, mxmlNodeList *nodeList)
{
    /* Validate arguments. */
    if (!ctxt || !nodeList)
        return eINVALID_ARGS;

    return jni_GetNodeListLength(ctxt, nodeList);
}

/*
 *  FUNCTION: mxmlGetItem
 *
 *  PURPOSE:  Gets the indexed node from this node list.
 *
 *  RETURNS:  Pointer to the node.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlGetItem(mxmlCtxt*ctxt, mxmlNodeList *nodeList, long index)
{
    /* Validate arguments. */
    if (!ctxt || !nodeList || index < 0)
        return NULL;

    return jni_GetItem(ctxt, nodeList, index);
}

/*
 *  FUNCTION: mxmlGetNamedItem
 *
 *  PURPOSE:  Gets the named node from this node list.
 *
 *  RETURNS:  Pointer to the node.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlGetNamedItem(mxmlCtxt *ctxt, mxmlNodeList *nodeList, char *name)
{
    /* Validate arguments. */
    if (!ctxt || !nodeList || !name || !strlen(name))
        return NULL;

    return jni_GetNamedItem(ctxt, nodeList, name);
}

/*
 *  FUNCTION: mxmlSetNamedItem
 *
 *  PURPOSE:  Adds this node to this node list.
 *
 *  RETURNS:  eOK
 *            MOCA/MSXML error number
 */

long mxmlSetNamedItem(mxmlCtxt *ctxt, mxmlNodeList *nodeList, mxmlNode *node)
{
    /* Validate arguments. */
    if (!ctxt || !nodeList || !node)
        return eINVALID_ARGS;

    return jni_SetNamedItem(ctxt, nodeList, node);
}
/*
 *  FUNCTION: mxmlRemoveNamedItem
 *
 *  PURPOSE:  Removes this named node from this node list.
 *
 *  RETURNS:  Pointer to the node.
 *            NULL - An error occurred.
 */

long mxmlRemoveNamedItem(mxmlCtxt *ctxt, mxmlNodeList *nodeList, char *name)
{
    /* Validate arguments. */
    if (!ctxt || !nodeList || !name || !strlen(name))
        return eINVALID_ARGS;

    return jni_RemoveNamedItem(ctxt, nodeList, name);
}
