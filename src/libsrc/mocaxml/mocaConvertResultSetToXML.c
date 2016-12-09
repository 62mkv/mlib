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
 *  Copyright (c) 2002
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
 *  FUNCTION: moca_ConvertResultSetToXML
 *
 *  PURPOSE:  Convert the given result set to XML.
 */

mxmlCtxt *moca_ConvertResultSetToXML(mocaDataRes *res, mxmlCtxt *ctxt,
	                             mxmlNode *parent)
{
    long ii;
    char numRows[100],
         numColumns[100];

    mxmlNode *resElement;

    mocaDataRow *row;

    /* Initialize with the XML parser if necessary. */
    if (! ctxt)
    {
        if ((ctxt = moca_StartXMLParser( )) == NULL)
            return NULL;
    }

    /* Get nicer aliases to work with. */
    sprintf(numRows,    "%ld", sqlGetNumRows(res));
    sprintf(numColumns, "%ld", sqlGetNumColumns(res));

    /* Create the new XML document if necessary. */
    if (! parent)
        parent = mxmlCreateDocument(ctxt);

    /* Add the element for this result set. */
    resElement = mxmlAddElement(ctxt, parent, "resultset");
    mxmlAddAttribute(ctxt, resElement, "rows", numRows);
    mxmlAddAttribute(ctxt, resElement, "columns", numColumns);

    /* Cycle through each row in the result set. */
    for (ii=1, row=sqlGetRow(res); row; ii++, row=sqlGetNextRow(row))
    {
	char rowNumber[100];

	long colCount,
	     colNumber;

        mxmlNode *rowElement;

        /* Get nicer aliases to work with. */
        colCount = sqlGetNumColumns(res);

        /* Convert the row number to a string. */
	sprintf(rowNumber, "%ld", ii);

        /* Add the element for this row. */
        rowElement = mxmlAddElement(ctxt, resElement, "row");
        mxmlAddAttribute(ctxt, rowElement, "number", rowNumber);

	/* Cycle through each column in the row. */
	for (colNumber = 0; colNumber < colCount; colNumber++)
	{
	    char  colType,
	         *colName;

	    long    valLong;
	    char   *valString;
	    double  valFloat;

	    char valLongAsString[100],
	         valFloatAsString[100];

            mxmlNode *colElement;

            mocaDataRes *temp;

	    /* Get nicer aliases to work with. */
	    colName = sqlGetColumnName(res, colNumber);
	    colType = sqlGetDataTypeByPos(res, colNumber);

	    /* Add the element for this column. */
            colElement = mxmlAddElement(ctxt, rowElement, colName);

	    /* Add the isnull attribute and continue on if necessary. */
	    if (sqlIsNullByPos(res, row, colNumber))
	    {
		mxmlAddAttribute(ctxt, colElement, "isnull", "true");
		continue;
	    }

	    /* Add the value for this column. */
	    switch (colType)
	    {
	    case COMTYP_INT:
	    case COMTYP_LONG:
	    case COMTYP_BOOLEAN:
		valLong = sqlGetLongByPos(res, row, colNumber);
		sprintf(valLongAsString, "%ld", valLong);
                mxmlAddTextNode(ctxt, colElement, valLongAsString);
		break;
            case COMTYP_DATTIM:
            case COMTYP_STRING:
		valString = sqlGetStringByPos(res, row, colNumber);
                mxmlAddTextNode(ctxt, colElement, valString);
		break;
            case COMTYP_FLOAT:
		valFloat = sqlGetFloatByPos(res, row, colNumber);
                sprintf(valFloatAsString, "%lf", valFloat);
                mxmlAddTextNode(ctxt, colElement, valFloatAsString);
		break;
            case COMTYP_RESULTS:
		temp = * (void **) sqlGetValueByPos(res, row, colNumber);
                ctxt = moca_ConvertResultSetToXML(temp, ctxt, colElement);
		break;
	    }
	}
    }

    return ctxt;
}


/*
 *  FUNCTION: mocaConvertResultSetToXML
 *
 *  PURPOSE:  Convert the given result set to XML.
 */

LIBEXPORT 
RETURN_STRUCT *mocaConvertResultSetToXML(mocaDataRes **inRes)
{
    mxmlCtxt *ctxt;

    mocaDataRes *res = inRes ? *inRes : NULL;

    /* Validate the arguments. */
    if (! res)
        return srvErrorResults(eINVALID_ARGS, "Invalid arguments", NULL);

    /* Convert the result set to XML. */
    ctxt = moca_ConvertResultSetToXML(res, NULL, NULL);

    return srvResults(eOK,
	              "mxml_ctxt", COMTYP_GENERIC, sizeof(mxmlCtxt *), ctxt, 
		      NULL);
}
