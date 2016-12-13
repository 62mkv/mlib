/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file for mxmllib.
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

#ifndef MXMLLIB_H
#define MXMLLIB_H

#if defined (__cplusplus)
extern "C" {
#endif

/*
 *  Type Definitions
 */

typedef struct mxmlCtxt_s     mxmlCtxt;
typedef struct mxmlNode_s     mxmlNode;
typedef struct mxmlNodeList_s mxmlNodeList;

typedef mxmlCtxt     *mxmlCtxtPtr;
typedef mxmlNode     *mxmlNodePtr;
typedef mxmlNodeList *mxmlNodeListPtr;

/*
 *  XML Node Structure Definition
 */

typedef struct
{
    char     *value;
    mxmlNode *nameNode;
} mxmlNodeData;

/*
 *  XML Node Data List Structure Definition
 */

typedef struct
{
    long          length;
    mxmlNodeData *data;
} mxmlNodeDataList;

/*
 *  XML Node Type Type Definition
 */

typedef enum mxmlNodeType
{
    MXML_NODE_TYPE_ERROR             = -1,
    MXML_INVALID_NODE                =  0,
    MXML_ELEMENT_NODE                =  1,
    MXML_ATTRIBUTE_NODE              =  2,
    MXML_TEXT_NODE                   =  3,
    MXML_CDATA_SECTION_NODE          =  4,
    MXML_ENTITY_REFERENCE_NODE       =  5,
    MXML_ENTITY_NODE                 =  6,
    MXML_PROCESSING_INSTRUCTION_NODE =  7,
    MXML_COMMENT_NODE                =  8,
    MXML_DOCUMENT_NODE               =  9,
    MXML_DOCUMENT_TYPE_NODE          =  10,
    MXML_DOCUMENT_FRAGMENT_NODE      =  11,
    MXML_NOTATION_NODE               =  12
} mxmlNodeType;

/*
 *  Default Definitions
 */

#define DEFAULT_XML_VERSION  "1.0"
#define DEFAULT_XML_ENCODING "UTF-8"

/*
 *  Parser Type Definitions
 */

#define MOCA_XML_JAXP 0

#define MOCA_XML_JAXP_STR "JAXP"

/*
 *  Common Function Prototypes
 */

mxmlNode *mxmlGetNamedNode(mxmlCtxt *ctxt,
                           mxmlNode *parent,
                           char *name);
 
char     *mxmlGetNamedNodeValue(mxmlCtxt *ctxt,
                                mxmlNode *parent,
                                char *name);

long      mxmlHasChildTextNode(mxmlCtxt *ctxt,
	                       mxmlNode *node);

char     *mxmlGetChildTextNodeValue(mxmlCtxt *ctxt,
	                            mxmlNode *parent);

mxmlCtxt *mxmlNormalize(mxmlCtxt *ctxt,
	                mxmlNode *node);

mxmlNodeDataList 
	 *mxmlGetNodeDataList(mxmlCtxt *ctxt, 
                              mxmlNode *node, 
                              char     *incl_sibling_node, 
                              short     incl_non_text_children);
char     *mxmlGetNodeDataListData(mxmlCtxt *ctxt, 
				  mxmlNodeDataList *nodeDataList, 
				  long index);
mxmlNode *mxmlGetNodeDataListNameNode(mxmlCtxt *ctxt, 
				      mxmlNodeDataList *nodeDataList, 
				      long index);
long      mxmlGetNodeDataListLength(mxmlCtxt *ctxt, 
				    mxmlNodeDataList *nodeDataList);
void      mxmlGetNodeDataListFree(mxmlCtxt *ctxt, 
				  mxmlNodeDataList **nodeDataList);

/*
 *  Utility Function Prototypes
 */

char     *mxml_CopyString(mxmlCtxt *ctxt,
	                  char     *format);
char     *mxml_CopyFormattedString(mxmlCtxt *ctxt,
	                           char     *format,
			           ...);
char     *mxml_Serialize(mxmlCtxt *ctxt,
	                 mxmlNode *node,
			 short     addEntityReferences);

long      mxml_SerializeToFile(FILE     *outfile,
                               mxmlCtxt *ctxt,
                               mxmlNode *node,
                               short     addEntityReferences);

char     *mxml_ReadFile(mxmlCtxt *ctxt, char *pathname);

long      mxmlStartParser(mxmlCtxt **ctxt);
long      mxmlConfigureParser(mxmlCtxt *ctxt, 
	                      short     validate,
		              short     preserveWhitespace);
void      mxmlStopParser(mxmlCtxt *ctxt);

long      mxmlInfo(int *type);
void      mxmlDumpContext(mxmlCtxt *ctxt);

void      mxmlError(mxmlCtxt *ctxt,
	            long      errorNumber,
		    char     *msg);
long      mxmlErrorNumber(mxmlCtxt *ctxt);
char     *mxmlErrorText(mxmlCtxt *ctxt);

mxmlNode *mxmlParseString(mxmlCtxt *ctxt, 
	                  char     *xmlString);
mxmlNode *mxmlParseFile(mxmlCtxt *ctxt, 
	                char     *filename);

char     *mxmlString(mxmlCtxt *ctxt, mxmlNode *node);
char     *mxmlRawString(mxmlCtxt *ctxt, mxmlNode *node);

long      mxmlWriteFile(mxmlCtxt *ctxt,
	                mxmlNode *node,
	                char     *filename);

long      mxmlRawWriteFile(mxmlCtxt *ctxt,
	                   mxmlNode *node,
	                   char     *filename);

mxmlNode *mxmlAddElement(mxmlCtxt *ctxt,
	                 mxmlNode *parent,
		         char     *data);
mxmlNode *mxmlAddTextNode(mxmlCtxt *ctxt,
	                  mxmlNode *parent,
		          char     *data);
mxmlNode *mxmlAddComment(mxmlCtxt *ctxt,
	                 mxmlNode *parent,
		         char     *data);
mxmlNode *mxmlAddPI(mxmlCtxt *ctxt,
	            mxmlNode *parent,
	            char     *target,
	            char     *data);
mxmlNode *mxmlAddAttribute(mxmlCtxt *ctxt,
	                   mxmlNode *parent,
	                   char     *name,
	                   char     *value);

mxmlCtxt *mxmlApplyStylesheetFromFile(mxmlCtxt *xmlCtxt,
	                              char     *pathname);
mxmlCtxt *mxmlApplyStylesheetFromString(mxmlCtxt *xmlCtxt,
	                                char     *xsl);

/*
 *  DOM Function Prototypes
 */

mxmlNode *mxmlCreateDocument(mxmlCtxt *ctxt);
mxmlNode *mxmlGetDocument(mxmlCtxt *ctxt);

mxmlNode *mxmlInsertBefore(mxmlCtxt *ctxt,
	                   mxmlNode *parent, 
	                   mxmlNode *newChild, 
	                   mxmlNode *refChild);
long      mxmlReplaceChild(mxmlCtxt *ctxt,
	                   mxmlNode *parent, 
	                   mxmlNode *newChild, 
		           mxmlNode *oldChild);
long      mxmlRemoveChild(mxmlCtxt *ctxt,
	                  mxmlNode *parent, 
	                  mxmlNode *child);
mxmlNode *mxmlAppendChild(mxmlCtxt *ctxt,
	                  mxmlNode *parent, 
		          mxmlNode *newChild);
long      mxmlHasChildNodes(mxmlCtxt *ctxt,
	                    mxmlNode *parent);
mxmlNode *mxmlCloneNode(mxmlCtxt *ctxt,
	                mxmlNode *node, 
	                long      deep);
mxmlNode *mxmlImportNode(mxmlCtxt *ctxt,
	                 mxmlNode *node, 
	                 long      deep);
char     *mxmlGetNodeName(mxmlCtxt *ctxt,
	                  mxmlNode *node);
char     *mxmlGetNodeValue(mxmlCtxt *ctxt,
	                   mxmlNode *node);
long      mxmlSetNodeValue(mxmlCtxt *ctxt,
	                   mxmlNode *node, 
	                   char     *value);
mxmlNodeType
          mxmlGetNodeType(mxmlCtxt *ctxt,
	                  mxmlNode *node);
mxmlNode *mxmlGetParentNode(mxmlCtxt *ctxt,
	                    mxmlNode *child);
mxmlNodeList 
         *mxmlGetChildNodes(mxmlCtxt *ctxt,
	                    mxmlNode *parent);
mxmlNode *mxmlGetFirstChild(mxmlCtxt *ctxt,
	                    mxmlNode *parent);
mxmlNode *mxmlGetLastChild(mxmlCtxt *ctxt,
	                   mxmlNode *parent);
mxmlNode *mxmlGetPreviousSibling(mxmlCtxt *ctxt,
	                         mxmlNode *sibling);
mxmlNode *mxmlGetNextSibling(mxmlCtxt *ctxt,
	                     mxmlNode *sibling);
mxmlNodeList 
         *mxmlGetAttributes(mxmlCtxt *ctxt,
	                    mxmlNode *node);
char     *mxmlGetAttribute(mxmlCtxt *ctxt,
	                   mxmlNode *element, 
	                   char     *name);
long      mxmlSetAttribute(mxmlCtxt *ctxt,
	                   mxmlNode *element, 
	                   char     *name, 
			   char     *value);
long      mxmlRemoveAttribute(mxmlCtxt *ctxt,
	                      mxmlNode *element, 
	                      char     *name);
mxmlNode *mxmlGetAttributeNode(mxmlCtxt *ctxt,
	                       mxmlNode *element, 
	                       char     *name);
long      mxmlSetAttributeNode(mxmlCtxt *ctxt,
	                       mxmlNode *element, 
	                       mxmlNode *attr);
long      mxmlRemoveAttributeNode(mxmlCtxt *ctxt,
	                          mxmlNode *element, 
	                          mxmlNode *attr);
mxmlNodeList 
         *mxmlGetElementsByTagName(mxmlCtxt *ctxt,
	                           mxmlNode *element, 
	                           char     *name);
char     *mxmlGetTagName(mxmlCtxt *ctxt,
	                 mxmlNode *element);
mxmlNode *mxmlCreateElement(mxmlCtxt *ctxt,
	                    char     *name);
mxmlNode *mxmlCreateDocumentFragment(mxmlCtxt *ctxt);
mxmlNode *mxmlCreateTextNode(mxmlCtxt *ctxt,
	                     char     *data);
mxmlNode *mxmlCreateComment(mxmlCtxt *ctxt,
	                    char     *data);
mxmlNode *mxmlCreateCDATASection(mxmlCtxt *ctxt,
	                         char     *data);
mxmlNode *mxmlCreatePI(mxmlCtxt *ctxt,
	               char     *target, 
		       char     *data);
mxmlNode *mxmlCreateAttribute(mxmlCtxt *ctxt,
	                      char     *name);
mxmlNode *mxmlGetDocumentElement(mxmlCtxt *ctxt);
char     *mxmlGetAttrName(mxmlCtxt *ctxt,
	                  mxmlNode *attr);
long      mxmlGetAttrSpecified(mxmlCtxt *ctxt,
	                       mxmlNode *attr);
char     *mxmlGetAttrValue(mxmlCtxt *ctxt,
	                   mxmlNode *attr);
long      mxmlSetAttrValue(mxmlCtxt *ctxt,
	                   mxmlNode *attr, 
			   char     *value);
char     *mxmlGetPITarget(mxmlCtxt *ctxt,
			  mxmlNode *pi);
char     *mxmlGetPIData(mxmlCtxt *ctxt,
			mxmlNode *pi);
long      mxmlSetPIData(mxmlCtxt *ctxt,
			mxmlNode *pi,
			char     *data);
char     *mxmlGetCharacterData(mxmlCtxt *ctxt,
			       mxmlNode *node);
long      mxmlSetCharacterData(mxmlCtxt *ctxt,
			       mxmlNode *node, 
			       char     *data);
long      mxmlGetCharacterDataLength(mxmlCtxt *ctxt,
				     mxmlNode *node);
long      mxmlGetNodeListLength(mxmlCtxt     *ctxt,
	                        mxmlNodeList *nodeList);
mxmlNode *mxmlGetItem(mxmlCtxt     *ctxt,
	              mxmlNodeList *nodeList, 
		      long          index);
mxmlNode *mxmlGetNamedItem(mxmlCtxt     *ctxt,
	                   mxmlNodeList *nodeList, 
			   char         *name);
long      mxmlSetNamedItem(mxmlCtxt     *ctxt,
	                   mxmlNodeList *nodeList, 
			   mxmlNode     *newNode);
long      mxmlRemoveNamedItem(mxmlCtxt     *ctxt,
	                      mxmlNodeList *nodeList, 
			      char         *name);

#if defined (__cplusplus)
}
#endif

#endif
