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
 *  FUNCTION: mocaRemoveXMLChild
 *
 *  PURPOSE:  Command interface to the mxmlRemoveChild( ) function.
 */

LIBEXPORT 
RETURN_STRUCT *mocaRemoveXMLChild(mxmlCtxt **inCtxt, mxmlNode **inParent,
	                          mxmlNode **inChild)
{
    mxmlNode *child  = inChild  ? *inChild  : NULL;
    mxmlNode *parent = inParent ? *inParent : NULL;
    mxmlCtxt *ctxt   = inCtxt   ? *inCtxt   : NULL;

    /* Validate the arguments. */
    if (! ctxt || ! child)
        return srvErrorResults(eINVALID_ARGS, "Invalid arguments", NULL);

    /* Remove the child node. */
    if (mxmlRemoveChild(ctxt, parent, child) != eOK)
        return srvErrorResults(mxmlErrorNumber(ctxt), mxmlErrorText(ctxt), NULL);

    return srvResults(eOK,
	              "mxml_ctxt", COMTYP_GENERIC, sizeof(mxmlCtxt *), ctxt, 
		      NULL);
}
