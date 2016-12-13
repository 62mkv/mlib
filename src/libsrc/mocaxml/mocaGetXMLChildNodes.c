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
 *  FUNCTION: mocaGetXMLChildNodes
 *
 *  PURPOSE:  Command interface to the mxmlGetChildNodes( ) function.
 */

LIBEXPORT 
RETURN_STRUCT *mocaGetXMLChildNodes(mxmlCtxt **inCtxt, mxmlNode **inParent)
{
    long ii,
         numChildren;

    mxmlNodeList *children;

    mxmlNode *parent = inParent ? *inParent : NULL;
    mxmlCtxt *ctxt   = inCtxt   ? *inCtxt   : NULL;

    RETURN_STRUCT *ret;

    /* Validate the arguments. */
    if (! ctxt || ! parent)
        return srvErrorResults(eINVALID_ARGS, "Invalid arguments", NULL);

    /* Get the child nodes. */
    if ((children = mxmlGetChildNodes(ctxt, parent)) == NULL)
        return srvErrorResults(mxmlErrorNumber(ctxt), mxmlErrorText(ctxt), NULL);

    /* Get the number of children in the child nodes list. */
    numChildren = mxmlGetNodeListLength(ctxt, children);

    /* Initialize the return struct. */
    if ((ret =  srvResultsInit(eOK,
	                  "mxml_node_seq", COMTYP_INT,     sizeof(long),  
                          "mxml_node",     COMTYP_GENERIC, sizeof(mxmlNode *),
		          NULL)) == NULL)
    {
        return srvErrorResults(eERROR, "Could not initialize return struct", NULL);
    }

    /* Cycle through each child in the child nodes list. */
    for (ii = 0; ii < numChildren; ii++)
    {
	mxmlNode *child;

	/* Get this child from the child nodes list. */
	if ((child = mxmlGetItem(ctxt, children, ii)) == NULL)
            return srvErrorResults(mxmlErrorNumber(ctxt), mxmlErrorText(ctxt), NULL);

	/* Add this child to the return struct. */
	srvResultsAdd(ret, ii, child, NULL);
    }

    return ret;
}
