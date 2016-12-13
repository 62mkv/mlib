static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions for returning data from the server and preparing
 *               results to return
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
#include <string.h>
#include <stdlib.h>
#include <stdarg.h>

#include <mocaerr.h>
#include <mislib.h>
#include <sqllib.h>

#include "srvprivate.h"

/*
 *  Add an error argument to a MOCA Results list.
 */
long srvResultsErrorArg(RETURN_STRUCT *Ret, char *varnam, char type, ...)
{
    va_list Arguments;
    char *tmpString;
    SRV_ERROR_ARG *tmpArg;

    tmpArg = malloc(sizeof *tmpArg);

    va_start(Arguments, type);
    tmpArg->next = Ret->Error.Args;
    Ret->Error.Args = tmpArg;

    strncpy(tmpArg->varnam, varnam, ARGNAM_LEN);
    tmpArg->type = type;

    switch (type)
    {
    case COMTYP_STRING:
    case COMTYP_DATTIM:
	tmpString = va_arg(Arguments, char *);

	tmpArg->data.cdata = tmpString?malloc(strlen(tmpString) + 1):NULL;
	if (tmpString)
	    strcpy(tmpArg->data.cdata, tmpString);

	break;

    case COMTYP_BOOLEAN:
    case COMTYP_INT:
    case COMTYP_LONG:
	tmpArg->data.ldata = va_arg(Arguments, long);
	break;
	
    case COMTYP_FLOAT:
	tmpArg->data.fdata = va_arg(Arguments, double);
	break;
    }

    tmpArg->lookup = va_arg(Arguments, int);
    va_end(Arguments);

    return (eOK);
}
