static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Command interface to the MOCA XML library.
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

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>

#include <common.h>
#include <mocaerr.h>
#include <mocagendef.h>
#include <mxmllib.h>
#include <sqllib.h>
#include <srvlib.h>

#include "mocaxml.h"

/*
 *  FUNCTION: moca_ConvertXMLToResultSet
 *
 *  PURPOSE:  Convert the given XML to a result set.
 */

mocaDataRes *moca_ConvertXMLToResultSet(mxmlCtxt *ctxt, mxmlNode *parent)
{
    long numRows,
         rowNumber;

    mxmlNodeList *rowNodes;

    mocaDataRes *res = NULL;

    /* Get the row nodes of this result set. */
    if ((rowNodes = mxmlGetChildNodes(ctxt, parent)) == NULL)
	return NULL;

    /* Get the number of rows. */
    numRows = mxmlGetNodeListLength(ctxt, rowNodes);

    /* Cycle through each row. */
    for (rowNumber = 0; rowNumber < numRows; rowNumber++)
    {
        long numCols,
             colNumber;

        mxmlNode     *rowNode;
        mxmlNodeList *colNodes;

        mocaDataRow *row = NULL;

	/* Get this row node. */
	rowNode = mxmlGetItem(ctxt, rowNodes, rowNumber);

        /* Get the column nodes of this row node. */
        if ((colNodes = mxmlGetChildNodes(ctxt, rowNode)) == NULL)
	    return NULL;
    
        /* Get the number of columns. */
        numCols = mxmlGetNodeListLength(ctxt, colNodes);
	
        /* Cycle through each column. */
        for (colNumber = 0; colNumber < numCols; colNumber++)
        {
            mxmlNode *colNode;
	    char     *colName,
	             *nullAttr;

	    /* Allocate the new result set if necessary. */
	    if (!res)
                res = sql_AllocateResultHdr(numCols);

	    /* Allocate the new row if necessary. */
	    if (!row)
	    {
		row = sql_AllocateRow(res);
                res->NumOfRows++;
	    }

	    /* Get this column node. */
	    colNode = mxmlGetItem(ctxt, colNodes, colNumber);

            colName = mxmlGetNodeName(ctxt, colNode);

            nullAttr = mxmlGetAttribute(ctxt, colNode, "isnull");

            if (nullAttr && strcmp(nullAttr, "true") == 0)
	    {
                /* Populate the metadata. */
	        sql_SetColName(res, colNumber, colName, COMTYP_CHAR, 4);
	    }
            /* If this column node has a text node, we've got a value. */
	    else if (mxmlHasChildTextNode(ctxt, colNode))
	    {
	        long  colLength;
	        char  colType,
	             *colValue;
    
                mxmlNode *textNode;
    
	        /* Get the child text node of this column node. */
	        textNode = mxmlGetFirstChild(ctxt, colNode);
    
                /* Get nicer aliases to work with. */
	        colValue  = mxmlGetNodeValue(ctxt, textNode);
	        colLength = strlen(colValue) + 1;
	        colType   = COMTYP_CHAR;

	        /* Populate the metadata. */
	        sql_SetColName(res, colNumber, colName, colType, colLength);
    
	        /* Populate the actual data. */
	        sql_AddRowItem(res, row, colNumber, colLength, colValue);
                free(colValue);
	    }
	    else
	    {
	        long  colLength;
	        char  colType;
                void *colValue;
   
		mxmlNode *childNode;

	        mocaDataRes *childRes;
    
	        /* Get the child result set node of this column node. */
	        childNode = mxmlGetFirstChild(ctxt, colNode);

	        /* Get the child result set of this column node. */
	        childRes = moca_ConvertXMLToResultSet(ctxt, childNode);
    
                /* Get nicer aliases to work with. */
                colName   = mxmlGetNodeName(ctxt, colNode);
	        colValue  = (void *) childRes;
	        colLength = sizeof(void *);
	        colType   = COMTYP_RESULTS;
    
	        /* Populate the metadata. */
	        sql_SetColName(res, colNumber, colName, colType, colLength);
  
  		if (childRes != NULL)  
	            /* populate the actual data. */
       	            sql_AddRowItem(res, row, colNumber, colLength, &colValue);
            } 
	    free(nullAttr);
            free(colName);
	}
    }

    return res;
}


/*
 *  FUNCTION: mocaConvertXMLToResultSet
 *
 *  PURPOSE:  Convert the given XML to a result set.
 */

LIBEXPORT 
RETURN_STRUCT *mocaConvertXMLToResultSet(mxmlCtxt **inCtxt)
{
    mxmlCtxt *ctxt = inCtxt ? *inCtxt : NULL;

    mxmlNode *node;

    mocaDataRes *res;

    RETURN_STRUCT *ret;

    /* Validate the arguments. */
    if (! ctxt)
        return srvErrorResults(eINVALID_ARGS, "Invalid arguments", NULL);

    /* Get the document element. */
    if ((node = mxmlGetDocumentElement(ctxt)) == NULL)
        return srvErrorResults(mxmlErrorNumber(ctxt), mxmlErrorText(ctxt), NULL);

    /* Normalize the XML. */
    ctxt = mxmlNormalize(ctxt, node);

    /* Convert the XML to a result set. */
    res = moca_ConvertXMLToResultSet(ctxt, node);

    /* Populate the return struct. */
    ret = srvAddSQLResults(res, eOK);

    return ret;
}
