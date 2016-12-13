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
 *  FUNCTION: mocaAddXMLAttribute
 *
 *  PURPOSE:  Command interface to the mxmlAddAttribute( ) function.
 */

LIBEXPORT 
RETURN_STRUCT *mocaAddXMLAttribute(mxmlCtxt **inCtxt, mxmlNode **inParent,
	                           char *name, char *value)
{
    mxmlNode *node;

    mxmlNode *parent = inParent ? *inParent : NULL;
    mxmlCtxt *ctxt   = inCtxt   ? *inCtxt   : NULL;

    /* Validate the arguments. */
    if (! ctxt  || ! parent || ! name  || ! strlen(name) ||
	! value || ! strlen(value))
    {
        return srvErrorResults(eINVALID_ARGS, "Invalid arguments", NULL);
    }

    /* Add the attribute. */
    if ((node = mxmlAddAttribute(ctxt, parent, name, value)) == NULL)
        return srvErrorResults(eERROR, mxmlErrorText(ctxt), NULL);

    return srvResults(eOK,
	              "mxml_ctxt", COMTYP_GENERIC, sizeof(mxmlCtxt *), ctxt, 
                      "mxml_node", COMTYP_GENERIC, sizeof(mxmlNode *), node,
		      NULL);
}