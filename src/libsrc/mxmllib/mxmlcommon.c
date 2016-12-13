static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: XML library functions that are common to any driver
 *               being used at the lower level.
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

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mxmllib.h>
#include <mocaerr.h>

#include "mxmlprivate.h"

/*
 * FUNCTION:  mxmlGetNamedNode
 *
 *  PURPOSE:  Get the first occurence of the node of the given name
 *            within the XML document after the given parent node.
 *
 *  RETURNS:  Pointer to the named node.
 *            NULL - An error occurred.
 */

mxmlNode *mxmlGetNamedNode(mxmlCtxt *ctxt, mxmlNode *parent, char *name)
{
    mxmlNode *node = NULL;

    mxmlNodeList *nodeList = NULL;

    /* Get a list of matching nodes. */
    nodeList = mxmlGetElementsByTagName(ctxt, parent, name);
    if (!nodeList)
        return NULL;

    /* Get the first node in the node list. */
    node = mxmlGetItem(ctxt, nodeList, 0);
    if (!node)
        return NULL;

    return node;
}

/*
 * FUNCTION:  mxmlGetNamedNodeValue
 *
 *  PURPOSE:  Get the value of the text node of the first occurence
 *            of the node of the given name within the XML document
 *            after the given parent node.
 *
 *  RETURNS:  Pointer to the named node.
 *            NULL - An error occurred.
 */

char *mxmlGetNamedNodeValue(mxmlCtxt *ctxt, mxmlNode *parent, char *name)
{
    char *value = NULL;

    mxmlNode *node = NULL;

    mxmlNodeList *nodeList = NULL;

    /* Get a list of matching nodes. */
    nodeList = mxmlGetElementsByTagName(ctxt, parent, name);
    if (!nodeList)
        return NULL;

    /* Get the first node in the node list. */
    node = mxmlGetItem(ctxt, nodeList, 0);
    if (!node)
        return NULL;

    /* Get the value associated with the actual node. */
    value = mxmlGetChildTextNodeValue(ctxt, node);
    if (!value)
        return NULL;

    return value;
}

/*
 *  FUNCTION: mxmlHasChildTextNode
 *
 *  PURPOSE:  Determine if the given node has a child text node.
 *
 *  RETURNS:   1 - The node has a child text node.
 *             0 - The node does node have a child text node.
 *            -1 - The node does node have a child text node.
 */

long mxmlHasChildTextNode(mxmlCtxt *ctxt, mxmlNode *parent)
{
    mxmlNode *childNode;

    /* Validate arguments. */
    if (!ctxt || !parent)
        return -1;

    /* Get the node type of this parent node. */
    if (mxmlGetNodeType(ctxt, parent) != MXML_ELEMENT_NODE)
	return 0;

    /* Get this node's child node. */
    if ((childNode = mxmlGetFirstChild(ctxt, parent)) == NULL)
	return 0;

    /* Get the node type of this child node. */
    if (mxmlGetNodeType(ctxt, childNode) == MXML_TEXT_NODE)
	return 1;

    return 0;
}

/*
 *  FUNCTION: mxmlGetChildTextNodeValue
 *
 *  PURPOSE:  Get the value of this parent node's child text node.
 *
 *  RETURNS:  Pointer to the child text node value.
 *            NULL - An error occurred.
 */

char *mxmlGetChildTextNodeValue(mxmlCtxt *ctxt, mxmlNode *parent)
{
    mxmlNode *childNode;

    /* Validate arguments. */
    if (!ctxt || !parent)
        return NULL;

    /* Get this node's child node. */
    if ((childNode = mxmlGetFirstChild(ctxt, parent)) == NULL)
	return NULL;

    /* Get the node type of this child node. */
    if (mxmlGetNodeType(ctxt, childNode) != MXML_TEXT_NODE)
	return NULL;

    return mxmlGetNodeValue(ctxt, childNode);
}

/*
 *  FUNCTION: mxml_ConvertAttributesToElements
 *
 *  PURPOSE:  Convert all elements' attributes to elements with text
 *            nodes instead.  This is used to convert XML to something
 *            we can more easily deal with when doing stuff like mapping
 *            XML to a MOCA result set.
 *
 *  EXAMPLE:  <po>
 *             <desc qty="10">beer</desc>
 *            </po>
 *
 *            becomes
 *
 *            <po>
 *             <desc>beer</desc>
 *             <qty>10</qty>
 *            </po>
 *
 *  RETURNS:  Pointer to the converted context.
 *            NULL - An error occurred.
 */

mxmlCtxt *mxml_ConvertAttributesToElements(mxmlCtxt *ctxt, mxmlNode *parent)
{
    long ii,
         numChildNodes;

    mxmlNodeList *childNodes;

    /* Get the child nodes of this node. */
    if ((childNodes = mxmlGetChildNodes(ctxt, parent)) == NULL)
	return NULL;

    /* Get the number of child nodes. */
    numChildNodes = mxmlGetNodeListLength(ctxt, childNodes);

    /* Cycle through each of the child nodes. */
    for (ii = 0; ii < numChildNodes; ii++)
    {
	long jj,
	     numAttrNodes;

	char *nodeName;

	mxmlNode *childNode;

	mxmlNodeList *attrNodes;

	/* Get this child node. */
	if ((childNode = mxmlGetItem(ctxt, childNodes, ii)) == NULL)
	    return NULL;

	/* Get this node's attribute nodes. */
	if ((attrNodes = mxmlGetAttributes(ctxt, childNode)) == NULL)
	    continue;
        
	/* Get this child node's name. */
	nodeName = mxmlGetNodeName(ctxt, childNode);

	/* Get the number of attributes nodes. */
        numAttrNodes = mxmlGetNodeListLength(ctxt, attrNodes);

	/* Cycle through each of the attributes nodes. */
        for (jj = 0; jj < numAttrNodes; jj++)
	{
	    char *attrName,
	         *attrValue;

            mxmlNode *element,
	             *attrNode;

	    /* Get this attribute node. */
	    if ((attrNode = mxmlGetItem(ctxt, attrNodes, jj)) == NULL)
		continue;

	    /* Get this attribute nodes name and value. */
	    attrName  = mxmlGetNodeName(ctxt, attrNode);
	    attrValue = mxmlGetNodeValue(ctxt, attrNode);

	    /* The "isnull" attribute is a special case. */
	    if (misCiStrncmp(attrName, "isnull", strlen("isnull")) == 0)
		goto continueloop;

	    /* Some other attributes are special. */
            if (misCiStrncmp(nodeName, "resultset", strlen("resultset")) == 0)
	    {
	        if (misCiStrncmp(attrName, "rows", strlen("rows")) == 0)
		    goto continueloop;
	        if (misCiStrncmp(attrName, "columns", strlen("rows")) == 0)
		    goto continueloop;
	    }
            if (misCiStrncmp(nodeName, "row", strlen("row")) == 0)
	    {
	        if (misCiStrncmp(attrName, "number", strlen("number")) == 0)
		    goto continueloop;
	    }

            /* Add this attribute as an element. */
	    element = mxmlAddElement(ctxt, parent, attrName);
	    mxmlAddTextNode(ctxt, element, attrValue);

	    /* Remove this attribute from the current child node. */
	    mxmlRemoveAttributeNode(ctxt, childNode, attrNode);

continueloop:
	    free(attrName);
	    free(attrValue);
	}
        free(nodeName);
    }

    /* Convert this child node's children as well. */
    for (ii = 0; ii < numChildNodes; ii++)
    {
	mxmlNode *childNode;

	childNode = mxmlGetItem(ctxt, childNodes, ii);

	mxml_ConvertAttributesToElements(ctxt, childNode);
    }

    return ctxt;
}

/*
 *  FUNCTION: mxml_FixMatchingElement
 *
 *  PURPOSE:  Convert elements of the given name to children of a single
 *            element of the same name.
 *
 *  RETURNS:  Pointer to the converted context.
 *            NULL - An error occurred.
 */

mxmlCtxt *mxml_FixMatchingElement(mxmlCtxt *ctxt, mxmlNode *parent, char *name)
{
    long ii,
	 numChildNodes,
	 numDuplicates = 0;

    mxmlNodeList *childNodes;

    mxmlNode  *element,
             **duplicate;

    duplicate = NULL;

    /* Get the child nodes of this node. */
    if ((childNodes = mxmlGetChildNodes(ctxt, parent)) == NULL)
	return NULL;

    /* Get the number of child nodes. */
    numChildNodes = mxmlGetNodeListLength(ctxt, childNodes);

    /* Remove all duplicate elements. */
    for (ii = 0; ii < numChildNodes; ii++)
    {
	char *nodeName;

	mxmlNode *node;

	/* Get this node to compare. */
	node = mxmlGetItem(ctxt, childNodes, ii);

	/* Get the node name of this node to compare. */
	nodeName = mxmlGetNodeName(ctxt, node);

        /* Compare the node names. */
	if (strcmp(name, nodeName) == 0)
	{
	    numDuplicates++;
	    duplicate = realloc(duplicate, numDuplicates * sizeof(mxmlNode *));
	    duplicate[numDuplicates-1] = node;
	}
	free(nodeName);
    }

    element = mxmlAddElement(ctxt, parent, name);

    /* Add back in duplicate elements. */
    for (ii = 0; ii < numDuplicates; ii++)
    {
	mxmlNode *clone;

	clone = mxmlCloneNode(ctxt, duplicate[ii], 1);
	mxmlAppendChild(ctxt, element, clone);
	mxmlRemoveChild(ctxt, parent, duplicate[ii]);
    }

    return ctxt;
}

/*
 *  FUNCTION: mxml_ConvertMatchingElements
 *
 *  PURPOSE:  Convert elements of the same name to children of a single
 *            element of that name if there are other elements at the
 *            original level.  This is used to convert XML to something
 *            we can more easily deal with when doing stuff like mapping
 *            XML to a MOCA result set.
 *
 *  EXAMPLE:  <po>
 *             <number>10</number>
 *             <item>1</item>
 *             <item>2</item>
 *            </po>
 *
 *            becomes
 *
 *            <po>
 *             <number>10</number>
 *             <item>
 *              <item>1</item>
 *              <item>2</item>
 *             </item>
 *            </po>
 *
 *  RETURNS:  Pointer to the converted context.
 *            NULL - An error occurred.
 */

mxmlCtxt *mxml_ConvertMatchingElements(mxmlCtxt *ctxt, mxmlNode *parent)
{
    short differencesExist = 0;

    long ii,
	 numChildNodes;

    mxmlNodeList *childNodes;

    /* Get the child nodes of this node. */
    if ((childNodes = mxmlGetChildNodes(ctxt, parent)) == NULL)
	return NULL;

    /* Get the number of child nodes. */
    numChildNodes = mxmlGetNodeListLength(ctxt, childNodes);

    /* Make sure they're not all the same. */
    for (ii = 0; ii < numChildNodes-1; ii++)
    {
	long jj;

	char *nodeName1;

	mxmlNode *node1;

	/* Get the first node to compare. */
	node1 = mxmlGetItem(ctxt, childNodes, ii);

	/* Get the node name of the first node to compare. */
	nodeName1 = mxmlGetNodeName(ctxt, node1);

	for (jj = ii+1; jj < numChildNodes; jj++)
	{
	    char *nodeName2;

	    mxmlNode *node2;

	    /* Get the second node to compare. */
	    node2 = mxmlGetItem(ctxt, childNodes, jj);

	    /* Get the node name of the second node to compare. */
	    nodeName2 = mxmlGetNodeName(ctxt, node2);

	    /* Compare the node names. */
	    if (strcmp(nodeName1, nodeName2) != 0)
	    {
	        differencesExist = 1;
		break;
	    }
	}
    }

    /* Don't bother continuing if no duplicates were found. */
    if (! differencesExist)
	return ctxt;

    /* Look for a duplicate. */
    for (ii = 0; ii < numChildNodes-1; ii++)
    {
	long jj;

	char *nodeName1;

	mxmlNode *node1;

	/* Get the first node to compare. */
	node1 = mxmlGetItem(ctxt, childNodes, ii);

	/* Get the node name of the first node to compare. */
	nodeName1 = mxmlGetNodeName(ctxt, node1);

	for (jj = ii+1; jj < numChildNodes; jj++)
	{
	    char *nodeName2;

	    mxmlNode *node2;

	    /* Get the second node to compare. */
	    node2 = mxmlGetItem(ctxt, childNodes, jj);

	    /* Get the node name of the second node to compare. */
	    nodeName2 = mxmlGetNodeName(ctxt, node2);

	    /* Compare the node names. */
	    if (strcmp(nodeName1, nodeName2) == 0)
	    {
		/* Fix these duplicate elements. */
		mxml_FixMatchingElement(ctxt, parent, nodeName1);

		/* Find any other duplicate elements. */
	        mxml_ConvertMatchingElements(ctxt, parent);

		return ctxt;
	    }
	}
	free(nodeName1);
    }

    for (ii = 0; ii < numChildNodes; ii++)
    {
	mxmlNode *childNode;

	childNode = mxmlGetItem(ctxt, childNodes, ii);

	mxml_ConvertMatchingElements(ctxt, childNode);
    }

    return ctxt;
}

/*
 *  FUNCTION: mxmlNormalizeXML
 *
 *  PURPOSE:  Normalize the given XML.  This involves converting attributes
 *            to elements and converting elements with the same name to
 *            children of a single element of the same name.  We could do
 *            this all in one function, but it's a lot easier to just do it
 *            in two separate functions and call each of them serially.
 *
 *  RETURNS:  Pointer to the converted context.
 *            NULL - An error occurred.
 */

mxmlCtxt *mxmlNormalize(mxmlCtxt *ctxt, mxmlNode *node)
{
    /* Validate arguments. */
    if (!ctxt)
        return NULL;

    /* Get the document element if a node wasn't given. */
    if (! node)
    {
        if ((node = mxmlGetDocumentElement(ctxt)) == NULL)
	    return NULL;
    }

    /* Convert matching elements. */
    ctxt = mxml_ConvertMatchingElements(ctxt, node);

    /* Convert attributes to elements. */
    ctxt = mxml_ConvertAttributesToElements(ctxt, node);

    return ctxt;
}

/*
 *  FUNCTION: mxmlNodeDataListAdd
 *
 *  PURPOSE:
 *
 *  RETURNS:  eOK
 *            eNO_MEMORY = Could not allocate memory.
 *
 */

static long mxmlNodeDataListAdd(mxmlCtxt *ctxt,
                                mxmlNodeDataList *nodeDataList,
                                short look_for_dupl,
                                mxmlNode *nameNode,
                                char *value)
{
    long  ii;
    char  *name1,
          *name2;

    if (look_for_dupl && nameNode)
    {
	for (ii = 0; ii < nodeDataList->length; ii++)
	{
	    name1 = mxmlGetNodeName(ctxt, nodeDataList->data[ii].nameNode);
	    name2 = mxmlGetNodeName(ctxt, nameNode);

	    if (strcmp(name1, name2) == 0)
	    {
	        free(name1);
	        free(name2);
		free(value);
		return eOK;
	    }

	    free(name1);
	    free(name2);
	}
    }

    /* Add this item to the list. */
    nodeDataList->length++;
    nodeDataList->data = realloc(nodeDataList->data,
                                 nodeDataList->length * sizeof(mxmlNodeData));
    if (! nodeDataList->data)
        return eNO_MEMORY;

    nodeDataList->data[nodeDataList->length-1].value    = value;
    nodeDataList->data[nodeDataList->length-1].nameNode = nameNode;

    return eOK;
}

/*
 *  FUNCTION: mxmlGetNodeDataListLength
 *
 *  PURPOSE:  Get the length of the given list.
 *
 *  RETURNS:  Length of the list.
 */

long mxmlGetNodeDataListLength(mxmlCtxt *ctxt, mxmlNodeDataList *nodeDataList)
{
    if (nodeDataList)
        return nodeDataList->length;

    return 0;
}

/*
 *  FUNCTION: mxmlGetNodeDataListNameNode
 *
 *  PURPOSE:  Get the name node for the given index.
 *
 *  RETURNS:  Name of the node.
 *            NULL - The given index is invalid.
 */

mxmlNode *mxmlGetNodeDataListNameNode(mxmlCtxt *ctxt,
				      mxmlNodeDataList *nodeDataList,
				      long index)
{
    if (index < nodeDataList->length)
	return nodeDataList->data[index].nameNode;

    return NULL;
}


/*
 *  FUNCTION: mxmlGetNodeDataListData
 *
 *  PURPOSE:  Get the data for the given index.
 *
 *  RETURNS:  Name of the node.
 *            NULL - The given index is invalid.
 */

char *mxmlGetNodeDataListData(mxmlCtxt *ctxt,
			      mxmlNodeDataList *nodeDataList,
			      long index)
{
    if (index < nodeDataList->length)
        return nodeDataList->data[index].value;

    return NULL;
}

/*
 *  FUNCTION: mxmlGetNodeDataList
 *
 *  PURPOSE:  Gets the data for a node.
 *
 *  RETURNS:  Pointer to the node data list.
 *            NULL - An error occurred.
 */

mxmlNodeDataList *mxmlGetNodeDataList(mxmlCtxt *ctxt,
                                      mxmlNode *node,
                                      char *incl_sibling_node,
                                      short incl_non_text_children)
{
    long ii,
	 jj,
         num_nodes,
         num_attribs;
    long ret_siz = 0;

    short dupl_look = 0;

    mxmlNode *do_node = node;

    mxmlNodeList *nodes   = NULL;
    mxmlNodeList *attribs = NULL;

    mxmlNodeDataList *ret = NULL;

    /* Start */
    ret = calloc(1, sizeof(mxmlNodeDataList));
    if (!ret)
	return NULL;

    /*
     * We will return the following
     * -   all attributes of the node node
     * -   all text nodes within the node
     * -   same from the sibling that was passed
     */
    for (ii = 0; ii < 2; ii++)
    {
        if (ii == 1)
        {
            dupl_look = 1;
            if (incl_sibling_node && strlen(incl_sibling_node) > 0)
	    {
                if ((do_node = mxmlGetNextSibling(ctxt, node)) != NULL)
		{
		    char *nodeName = mxmlGetNodeName(ctxt, do_node);

                    if (strcmp(nodeName, incl_sibling_node) != 0)
                        do_node = NULL;

		    free(nodeName);
		}
	    }
        }

        if (do_node)
        {
            /* First get the attributes */
            if ((attribs = mxmlGetAttributes(ctxt, do_node)) != NULL)
            {
                mxmlNode *attrib = NULL;

                num_attribs = mxmlGetNodeListLength(ctxt, attribs);
                for (jj = 0; jj < num_attribs; jj++)
                {
                    attrib = mxmlGetItem(ctxt, attribs, jj);
                    mxmlNodeDataListAdd(ctxt,
                                        ret,
                                        dupl_look,
                                        attrib,
                                        mxmlGetAttrValue(ctxt, attrib));
                } /* next jj */
            } /* have some attributes */

            /* Now get all the child nodes that have text values */
            if ((nodes = mxmlGetChildNodes(ctxt, do_node)) != NULL)
            {
                mxmlNode *node = NULL;

                num_nodes = mxmlGetNodeListLength(ctxt, nodes);
                for (jj = 0; jj < num_nodes; jj++)
                {
                    mxmlNode *child_node = NULL;

                    node = mxmlGetItem(ctxt, nodes, jj);

                    if (mxmlHasChildTextNode(ctxt, node))
                    {
                        child_node = mxmlGetFirstChild(ctxt, node);
                        mxmlNodeDataListAdd(ctxt,
                                            ret,
                                            dupl_look,
                                            node,
                                            mxmlGetNodeValue(ctxt, child_node));
                    }
                    else if (mxmlGetNodeType(ctxt, node) == MXML_TEXT_NODE)
                    {
                        mxmlNodeDataListAdd(ctxt,
                                            ret,
                                            dupl_look,
                                            do_node,
                                            mxmlGetNodeValue(ctxt, node));
                    }
                    else if (incl_non_text_children)
                    {
                        mxmlNodeDataListAdd(ctxt,
                                            ret,
                                            dupl_look,
                                            node,
                                            NULL);
                    }
                }/* next jj */
            }/* have child nodes */
        }/* have a node */
    }/* next ii */

    return ret;
}

/*
 *  FUNCTION: mxmlGetNodeDataListFree
 *
 *  PURPOSE:  Frees the given node data list.
 *
 *  RETURNS:  void
 */

void mxmlGetNodeDataListFree(mxmlCtxt          *ctxt,
                             mxmlNodeDataList **nodeDataList)
{
    long ii;

    if (nodeDataList && *nodeDataList)
    {
	for (ii = 0; ii < (*nodeDataList)->length; ii++)
	{
            if ((*nodeDataList)->data[ii].value)
		free((*nodeDataList)->data[ii].value);
	}

        if ((*nodeDataList)->data)
        {
            free((*nodeDataList)->data);
            (*nodeDataList)->data = NULL;
        }

        free(*nodeDataList);
        *nodeDataList = NULL;
    }

    return;
}
