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
 *  FUNCTION: mocaNormalizeXML
 *
 *  PURPOSE:  Normalize the given XML.
 */

LIBEXPORT 
RETURN_STRUCT *mocaNormalizeXML(mxmlCtxt **inCtxt, mxmlNode **inNode)
{
    mxmlCtxt *ctxt = inCtxt ? *inCtxt : NULL;
    mxmlNode *node = inNode ? *inNode : NULL;

    /* Validate the arguments. */
    if (! ctxt)
        return srvErrorResults(eINVALID_ARGS, "Invalid arguments", NULL);

    /* Get the document element if a node wasn't given. */
    if (! node)
    {
	if ((node = mxmlGetDocumentElement(ctxt)) == NULL)
            return srvResults(eERROR, NULL);
    }

    /* Normalize the XML. */
    ctxt = mxmlNormalize(ctxt, node);

    return srvResults(eOK,
	              "mxml_ctxt", COMTYP_GENERIC, sizeof(mxmlCtxt *), ctxt, 
		      NULL);
}