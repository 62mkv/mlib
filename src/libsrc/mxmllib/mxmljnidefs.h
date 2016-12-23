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

#ifndef MXMLJNIDEFS_H
#define MXMLJNIDEFS_H

#include <jni.h>

/*
 * Classes
 */

static jclass xmlAdapterClass;

/*
 * Methods
 */

static jmethodID constructorMethod;
static jmethodID configureParserMethod;
static jmethodID dumpContextMethod;
static jmethodID parseStringMethod;
static jmethodID parseFileMethod;
static jmethodID toStringMethod;
static jmethodID toStringRawMethod;
static jmethodID writeFileMethod;
static jmethodID writeRawFileMethod;
static jmethodID addElementMethod;
static jmethodID addTextNodeMethod;
static jmethodID addCommentMethod;
static jmethodID addPIMethod;
static jmethodID addAttributeMethod;
static jmethodID applyStylesheetFromFileMethod;
static jmethodID applyStylesheetFromStringMethod;
static jmethodID createDocumentMethod;
static jmethodID getDocumentMethod;
static jmethodID insertBeforeMethod;
static jmethodID replaceChildMethod;
static jmethodID removeChildMethod;
static jmethodID appendChildMethod;
static jmethodID hasChildNodesMethod;
static jmethodID cloneNodeMethod;
static jmethodID importNodeMethod;
static jmethodID getNodeNameMethod;
static jmethodID getNodeValueMethod;
static jmethodID setNodeValueMethod;
static jmethodID getNodeTypeMethod;
static jmethodID getParentNodeMethod;
static jmethodID getChildNodesMethod;
static jmethodID getFirstChildMethod;
static jmethodID getLastChildMethod;
static jmethodID getPreviousSiblingMethod;
static jmethodID getNextSiblingMethod;
static jmethodID getAttributesMethod;
static jmethodID getAttributeMethod;
static jmethodID setAttributeMethod;
static jmethodID removeAttributeMethod;
static jmethodID getAttributeNodeMethod;
static jmethodID setAttributeNodeMethod;
static jmethodID removeAttributeNodeMethod;
static jmethodID getElementsByTagNameMethod;
static jmethodID getTagNameMethod;
static jmethodID createElementMethod;
static jmethodID createDocumentFragmentMethod;
static jmethodID createTextNodeMethod;
static jmethodID createCommentMethod;
static jmethodID createCDATASectionMethod;
static jmethodID createPIMethod;
static jmethodID createAttributeMethod;
static jmethodID getDocumentElementMethod;
static jmethodID getAttrNameMethod;
static jmethodID getAttrSpecifiedMethod;
static jmethodID getAttrValueMethod;
static jmethodID setAttrValueMethod;
static jmethodID getPITargetMethod;
static jmethodID getPIDataMethod;
static jmethodID setPIDataMethod;
static jmethodID getCharacterDataMethod;
static jmethodID setCharacterDataMethod;
static jmethodID getCharacterDataLengthMethod;
static jmethodID getNodeListLengthMethod;
static jmethodID getItemMethod;
static jmethodID getNamedItemMethod;
static jmethodID setNamedItemMethod;
static jmethodID removeNamedItemMethod;

/*
 * Class Names
 */

const char *xmlAdapterClassName = "com/sam/moca/xml/XMLAdapter";

/*
 * Method Names and Signatures
 */

const char *constructorName = 
	"<init>";
const char *constructorSig  = 
	"()V";

const char *configureParserName = 
	"configure";
const char *configureParserSig  = 
	"(ZZ)V";

const char *dumpContextName = 
	"dumpContext";
const char *dumpContextSig  = 
	"()V";

const char *parseStringName = 
	"parseString";
const char *parseStringSig  = 
	"(Ljava/lang/String;Ljava/lang/String;)Lorg/w3c/dom/Document;";

const char *parseFileName = 
	"parseFile";
const char *parseFileSig  = 
	"(Ljava/lang/String;)Lorg/w3c/dom/Document;";

const char *toStringName = 
	"toString";
const char *toStringSig  = 
	"(Lorg/w3c/dom/Node;)Ljava/lang/String;";

const char *toStringRawName = 
	"toStringRaw";
const char *toStringRawSig  = 
	"(Lorg/w3c/dom/Node;)Ljava/lang/String;";

const char *writeFileName = 
	"writeFile";
const char *writeFileSig  = 
	"(Lorg/w3c/dom/Node;Ljava/lang/String;)V";

const char *writeRawFileName = 
	"writeRawFile";
const char *writeRawFileSig  = 
	"(Lorg/w3c/dom/Node;Ljava/lang/String;)V";

const char *addElementName = 
	"addElement";
const char *addElementSig  = 
	"(Lorg/w3c/dom/Node;Ljava/lang/String;)Lorg/w3c/dom/Element;";

const char *addTextNodeName = 
	"addTextNode";
const char *addTextNodeSig  = 
	"(Lorg/w3c/dom/Element;Ljava/lang/String;)Lorg/w3c/dom/Text;";

const char *addCommentName = 
	"addComment";
const char *addCommentSig  =
	"(Lorg/w3c/dom/Node;Ljava/lang/String;)Lorg/w3c/dom/Comment;";

const char *addPIName =
	"addProcessingInstruction";
const char *addPISig  =
	"(Lorg/w3c/dom/Node;Ljava/lang/String;Ljava/lang/String;)Lorg/w3c/dom/ProcessingInstruction;";

const char *addAttributeName =
	"addAttribute";
const char *addAttributeSig  =
	"(Lorg/w3c/dom/Element;Ljava/lang/String;Ljava/lang/String;)Lorg/w3c/dom/Attr;";

const char *applyStylesheetFromFileName =
	"applyStylesheetFromFile";
const char *applyStylesheetFromFileSig  =
	"(Ljava/lang/String;)Lcom/sam/moca/xml/XMLAdapter;";

const char *applyStylesheetFromStringName =
	"applyStylesheetFromString";
const char *applyStylesheetFromStringSig  =
	"(Ljava/lang/String;Ljava/lang/String;)Lcom/sam/moca/xml/XMLAdapter;";

const char *createDocumentName =
	"createDocument";
const char *createDocumentSig  =
	"()Lorg/w3c/dom/Document;";

const char *getDocumentName =
	"getDocument";
const char *getDocumentSig  =
	"()Lorg/w3c/dom/Document;";

const char *insertBeforeName =
	"insertBefore";
const char *insertBeforeSig  =
	"(Lorg/w3c/dom/Node;Lorg/w3c/dom/Node;Lorg/w3c/dom/Node;)Lorg/w3c/dom/Node;";

const char *replaceChildName =
	"replaceChild";
const char *replaceChildSig  =
	"(Lorg/w3c/dom/Node;Lorg/w3c/dom/Node;Lorg/w3c/dom/Node;)V";

const char *removeChildName =
	"removeChild";
const char *removeChildSig  =
	"(Lorg/w3c/dom/Node;Lorg/w3c/dom/Node;)V";

const char *appendChildName =
	"appendChild";
const char *appendChildSig  =
	"(Lorg/w3c/dom/Node;Lorg/w3c/dom/Node;)Lorg/w3c/dom/Node;";

const char *hasChildNodesName =
	"hasChildNodes";
const char *hasChildNodesSig  =
	"(Lorg/w3c/dom/Node;)Z";

const char *cloneNodeName =
	"cloneNode";
const char *cloneNodeSig  =
	"(Lorg/w3c/dom/Node;Z)Lorg/w3c/dom/Node;";

const char *importNodeName =
	"importNode";
const char *importNodeSig  =
	"(Lorg/w3c/dom/Node;Z)Lorg/w3c/dom/Node;";

const char *getNodeNameName = 
	"getNodeName";
const char *getNodeNameSig  = 
	"(Lorg/w3c/dom/Node;)Ljava/lang/String;";

const char *getNodeValueName = 
	"getNodeValue";
const char *getNodeValueSig  = 
	"(Lorg/w3c/dom/Node;)Ljava/lang/String;";

const char *setNodeValueName =
	"setNodeValue";
const char *setNodeValueSig  =
	"(Lorg/w3c/dom/Node;Ljava/lang/String;)V";

const char *getNodeTypeName = 
	"getNodeType";
const char *getNodeTypeSig  = 
	"(Lorg/w3c/dom/Node;)I";

const char *getParentNodeName =
	"getParentNode";
const char *getParentNodeSig  =
	"(Lorg/w3c/dom/Node;)Lorg/w3c/dom/Node;";

const char *getChildNodesName =
	"getChildNodes";
const char *getChildNodesSig  =
	"(Lorg/w3c/dom/Node;)Lorg/w3c/dom/NodeList;";

const char *getFirstChildName = 
	"getFirstChild";
const char *getFirstChildSig  = 
	"(Lorg/w3c/dom/Node;)Lorg/w3c/dom/Node;";

const char *getLastChildName = 
	"getLastChild";
const char *getLastChildSig  = 
	"(Lorg/w3c/dom/Node;)Lorg/w3c/dom/Node;";

const char *getPreviousSiblingName = 
	"getPreviousSibling";
const char *getPreviousSiblingSig  = 
	"(Lorg/w3c/dom/Node;)Lorg/w3c/dom/Node;";

const char *getNextSiblingName = 
	"getNextSibling";
const char *getNextSiblingSig  = 
	"(Lorg/w3c/dom/Node;)Lorg/w3c/dom/Node;";

const char *getAttributesName = 
	"getAttributes";
const char *getAttributesSig  = 
	"(Lorg/w3c/dom/Element;)Lorg/w3c/dom/NamedNodeMap;";

const char *getAttributeName = 
	"getAttribute";
const char *getAttributeSig  = 
	"(Lorg/w3c/dom/Element;Ljava/lang/String;)Ljava/lang/String;";

const char *setAttributeName = 
	"setAttribute";
const char *setAttributeSig  =
	"(Lorg/w3c/dom/Element;Ljava/lang/String;Ljava/lang/String;)V";

const char *removeAttributeName = 
	"removeAttribute";
const char *removeAttributSig  =
	"(Lorg/w3c/dom/Element;Ljava/lang/String;)V";

const char *getAttributeNodeName =
	"getAttributeNode";
const char *getAttributeNodeSig  =
	"(Lorg/w3c/dom/Element;Ljava/lang/String;)Lorg/w3c/dom/Attr;";

const char *setAttributeNodeName =
	"setAttributeNode";
const char *setAttributeNodeSig  =
	"(Lorg/w3c/dom/Element;Lorg/w3c/dom/Attr;)V";

const char *removeAttributeNodeName =
	"removeAttributeNode";
const char *removeAttributeNodeSig  =
	"(Lorg/w3c/dom/Element;Lorg/w3c/dom/Attr;)V";

const char *getElementsByTagNameName = 
	"getElementsByTagName";
const char *getElementsByTagNameSig = 
	"(Lorg/w3c/dom/Element;Ljava/lang/String;)Lorg/w3c/dom/NodeList;";

const char *getTagNameName =
	"getTagName";
const char *getTagNameSig  =
	"(Lorg/w3c/dom/Element;)Ljava/lang/String;";

const char *createElementName =
	"createElement";
const char *createElementSig  =
	"(Ljava/lang/String;)Lorg/w3c/dom/Element;";

const char *createDocumentFragmentName =
	"createDocumentFragment";
const char *createDocumentFragmentSig  =
	"()Lorg/w3c/dom/DocumentFragment;";

const char *createTextNodeName =
	"createTextNode";
const char *createTextNodeSig  =
	"(Ljava/lang/String;)Lorg/w3c/dom/Text;";

const char *createCommentName =
	"createComment";
const char *createCommentSig  =
	"(Ljava/lang/String;)Lorg/w3c/dom/Comment;";

const char *createCDATASectionName =
	"createCDATASection";
const char *createCDATASectionSig  =
	"(Ljava/lang/String;)Lorg/w3c/dom/CDATASection;";

const char *createPIName =
	"createPI";
const char *createPISig  =
	"(Ljava/lang/String;Ljava/lang/String;)Lorg/w3c/dom/ProcessingInstruction;";

const char *createAttributeName =
	"createAttribute";
const char *createAttributeSig  =
	"(Ljava/lang/String;)Lorg/w3c/dom/Attr;";

const char *getDocumentElementName =
	"getDocumentElement";
const char *getDocumentElementSig  =
	"()Lorg/w3c/dom/Element;";

const char *getAttrNameName =
	"getAttrName";
const char *getAttrNameSig  =
	"(Lorg/w3c/dom/Attr;)Ljava/lang/String;";

const char *getAttrSpecifiedName =
	"getAttrSpecified";
const char *getAttrSpecifiedSig  =
	"(Lorg/w3c/dom/Attr;)Z";

const char *getAttrValueName =
	"getAttrValue";
const char *getAttrValueSig  =
	"(Lorg/w3c/dom/Attr;)Ljava/lang/String;";

const char *setAttrValueName =
	"setAttrValue";
const char *setAttrValueSig  =
	"(Lorg/w3c/dom/Attr;Ljava/lang/String;)V";

const char *getPITargetName =
	"getPITarget";
const char *getPITargetSig  =
	"(Lorg/w3c/dom/ProcessingInstruction;)Ljava/lang/String;";

const char *getPIDataName =
	"getPIData";
const char *getPIDataSig  =
	"(Lorg/w3c/dom/ProcessingInstruction;)Ljava/lang/String;";

const char *setPIDataName =
	"setPIData";
const char *setPIDataSig  =
	"(Lorg/w3c/dom/ProcessingInstruction;Ljava/lang/String;)V";

const char *getCharacterDataName =
	"getCharacterData";
const char *getCharacterDataSig  =
	"(Lorg/w3c/dom/CharacterData;)Ljava/lang/String;";

const char *setCharacterDataName =
	"setCharacterData";
const char *setCharacterDataSig  =
	"(Lorg/w3c/dom/CharacterData;Ljava/lang/String;)V";

const char *getCharacterDataLengthName =
	"getCharacterDataLength";
const char *getCharacterDataLengthSig  =
	"(Lorg/w3c/dom/CharacterData;)I";

const char *getNodeListLengthName = 
	"getNodeListLength";
const char *getNodeListLengthSig  = 
	"(Ljava/lang/Object;)I";

const char *getItemName = 
	"getItem";
const char *getItemSig  = 
	"(Ljava/lang/Object;I)Lorg/w3c/dom/Node;";

const char *getNamedItemName =
	"getNamedItem";
const char *getNamedItemSig  =
	"(Lorg/w3c/dom/NamedNodeMap;Ljava/lang/String;)Lorg/w3c/dom/Node;";

const char *setNamedItemName =
	"setNamedItem";
const char *setNamedItemSig  =
	"(Lorg/w3c/dom/NamedNodeMap;Lorg/w3c/dom/Node;)V";

const char *removeNamedItemName =
	"removeNamedItem";
const char *removeNamedItemSig  =
	"(Lorg/w3c/dom/NamedNodeMap;Ljava/lang/String;)V";

#endif
