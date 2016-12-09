static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions used by the server and server applications
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
 *#END*************************************************************************/

#include <moca.h>

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <mocaerr.h>
#include <sqllib.h>
#include "srvprivate.h"


long srvFreeMemory(int type, void *Ptr)
{
    RETURN_STRUCT *ReturnStruct;
    SRV_COMPILED_COMMAND *compiled;
    SRV_ERROR_ARG *tmpArg, *nextArg;

    switch (type)
    {
    case SRVRET_STRUCT:

	ReturnStruct = (RETURN_STRUCT *) Ptr;
	if (ReturnStruct == NULL)
	    return (eOK);

	if (ReturnStruct)
	{
	    if (ReturnStruct->DataTypes != NULL)
	    {
		free(ReturnStruct->DataTypes);
		ReturnStruct->DataTypes = NULL;
	    }

            /*
             * SERIOUS HACK.
             *
             * As a bridge to the next release, we'll be using the obsolete
             * "Header" attribute to populate the caught error code.
             *
	    if (ReturnStruct->Header != NULL)
	    {
		free(ReturnStruct->Header);
		ReturnStruct->Header = NULL;
	    }
            */

	    if (ReturnStruct->ReturnedData != NULL)
	    {
		sqlFreeResults(ReturnStruct->ReturnedData);
		ReturnStruct->ReturnedData = NULL;
	    }

	    for (tmpArg=ReturnStruct->Error.Args; tmpArg; tmpArg = nextArg)
	    {
		if (tmpArg->type == COMTYP_STRING || tmpArg->type == COMTYP_DATTIM)
		{
		    free(tmpArg->data.cdata);
		}
		nextArg = tmpArg->next;
		free(tmpArg);
	    }

	    if (ReturnStruct->Error.DefaultText)
		free(ReturnStruct->Error.DefaultText);
	    free((char *) ReturnStruct);
	    ReturnStruct = NULL;
	}
	break;

    case SRVCMP_STRUCT:
        compiled = (SRV_COMPILED_COMMAND *) Ptr;
        free(compiled->command_text);
        free(compiled);
	break;
    }
    return (eOK);
}
