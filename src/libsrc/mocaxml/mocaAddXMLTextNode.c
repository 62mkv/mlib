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
 *  FUNCTION: mocaAddXMLTextNode
 *
 *  PURPOSE:  Command interface to the mxmlAddTextNode( ) function.
 */

LIBEXPORT 
RETURN_STRUCT *mocaAddXMLTextNode(mxmlCtxt **inCtxt, mxmlNode **inParent, 
                                  char *tag,
	                          char *text,
                                  char *add_as_attr )
{
    mxmlNode *add_node = NULL;
    mxmlNode *tmp_node = NULL;

    mxmlNode *parent = inParent ? *inParent : NULL;
    mxmlCtxt *ctxt   = inCtxt   ? *inCtxt   : NULL;

    short add_attr;

    /* Validate the arguments. */
    if (! ctxt )
        return srvErrorResults(eINVALID_ARGS, "Invalid arguments", NULL);

    add_attr = add_as_attr && add_as_attr[0] == 'T';

    if (add_attr && tag && strlen(tag) > 0 )
    {
        add_node = mxmlAddAttribute(ctxt, parent, tag, text ? text : "");
    }
    else
    {
        if (tag && strlen(tag) > 0)
        {
            if ((tmp_node = mxmlAddElement(ctxt, parent, tag)) != NULL)
                add_node = mxmlAddTextNode(ctxt, tmp_node, text ? text : "");
        }
        else
        {
            add_node = mxmlAddTextNode(ctxt, parent, text ? text : "");
        }
    }

    if (!add_node)
        return srvErrorResults(mxmlErrorNumber(ctxt), mxmlErrorText(ctxt), NULL);

    return srvResults(eOK,
	              "mxml_ctxt", COMTYP_GENERIC, sizeof(mxmlCtxt *), ctxt, 
                      "mxml_node", COMTYP_GENERIC, sizeof(mxmlNode *), add_node,
		      NULL);
}
