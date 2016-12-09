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
#include <stdarg.h>

#include <mocaerr.h>
#include <mislib.h>
#include <sqllib.h>

#include "srvprivate.h"

/*
 *  Build a return structure for data to be returned to
 *  the client.
 */
RETURN_STRUCT *srvErrorResults(long status, char *fmt, ...)
{
    char *varnam,
         *tmpString;

    va_list args;

    RETURN_STRUCT *ret;

    SRV_ERROR_ARG *tmpArg;

    ret = calloc (1, sizeof(RETURN_STRUCT));
    if (ret == NULL) return (NULL);

    ret->DataTypes = calloc(1,1);
    if (ret->DataTypes == NULL) return (NULL);

    ret->ReturnedData = sql_AllocateResultHdr(0);

    if (ret->ReturnedData == NULL) return (NULL);

    ret->Error.Code       = status;
    /*
     * SERIOUS HACK.
     *
     * As a bridge to the next release, we'll be using the obsolete
     * "Header" attribute to populate the caught error code.
     *
    ret->Error.CaughtCode = status;
    */
    ret->Header = (char *) status;

    if (fmt)
    {
    ret->Error.DefaultText = malloc(strlen(fmt) + 1);
    strcpy(ret->Error.DefaultText, fmt);
    }

    va_start(args, fmt);

    while ((varnam = va_arg(args, char *)) != 0)
    {
    tmpArg = malloc(sizeof *tmpArg);
    tmpArg->next = ret->Error.Args;
    ret->Error.Args = tmpArg;

    strncpy(tmpArg->varnam, varnam, ARGNAM_LEN);
    tmpArg->type = (char) va_arg(args, int);

    switch (tmpArg->type)
    {
    case COMTYP_STRING:
    case COMTYP_DATTIM:
        tmpString = va_arg(args, char *);
        tmpArg->data.cdata = tmpString?malloc(strlen(tmpString) + 1):NULL;
        if (tmpString)
        strcpy(tmpArg->data.cdata, tmpString);
        break;

    case COMTYP_BOOLEAN:
    case COMTYP_INT:
    case COMTYP_LONG:
        tmpArg->data.ldata = va_arg(args, long);
        break;

    case COMTYP_FLOAT:
        tmpArg->data.fdata = va_arg(args, double);
        break;
    }

        tmpArg->lookup = va_arg(args, int);
    }

    va_end(args);

    return ret;
}

/*
 *  Add an error argument to an already-created error results return
 *  structure.
 */
void srvErrorResultsAddArg(RETURN_STRUCT *ret, char *name, int type, ...)
{
    char *tmpString;

    va_list args;

    SRV_ERROR_ARG *tmpArg;

    va_start(args, type);

    tmpArg = malloc(sizeof *tmpArg);
    tmpArg->next = ret->Error.Args;
    ret->Error.Args = tmpArg;

    strncpy(tmpArg->varnam, name, ARGNAM_LEN);
    tmpArg->type = type;

    switch (tmpArg->type)
    {
    case COMTYP_STRING:
    case COMTYP_DATTIM:
        tmpString = va_arg(args, char *);
        tmpArg->data.cdata = tmpString?malloc(strlen(tmpString) + 1):NULL;
        if (tmpString)
            strcpy(tmpArg->data.cdata, tmpString);

        tmpArg->lookup = va_arg(args, int);
        break;

    case COMTYP_BOOLEAN:
    case COMTYP_INT:
    case COMTYP_LONG:
        tmpArg->data.ldata = va_arg(args, long);
        tmpArg->lookup = 0;
        break;

    case COMTYP_FLOAT:
        tmpArg->data.fdata = va_arg(args, double);
        tmpArg->lookup = 0;
        break;
    }

    va_end(args);

    return;
}

/*
 *  Build a return structure for data to be returned to
 *  the client.
 */
long srvErrorResultsAdd(RETURN_STRUCT *ret, long status, char *fmt, ...)
{
    char *varnam,
         *tmpString;

    va_list args;

    SRV_ERROR_ARG *tmpArg,
          *nextArg;

    /* Validate our arguments. */
    if (ret == NULL)
    return eINVALID_ARGS;

    /*
     *  We need to make sure we free up any previous resources.
     */

    /* Free previous error arguments. */
    for (tmpArg = ret->Error.Args; tmpArg; tmpArg = nextArg)
    {
        if (tmpArg->type == COMTYP_STRING || tmpArg->type == COMTYP_DATTIM)
            free(tmpArg->data.cdata);

        nextArg = tmpArg->next;

        free(tmpArg);
    }

    /* Free the previous default error text. */
    free(ret->Error.DefaultText);

    /*
     *  Now we can populate the new error information.
     */

    ret->Error.Code       = status;
    /*
     * SERIOUS HACK.
     *
     * As a bridge to the next release, we'll be using the obsolete
     * "Header" attribute to populate the caught error code.
     *
    ret->Error.CaughtCode = status;
    */
    ret->Header = (char *) status;

    if (fmt)
    {
    ret->Error.DefaultText = malloc(strlen(fmt) + 1);
    strcpy(ret->Error.DefaultText, fmt);
    }

    va_start(args, fmt);

    while ((varnam = va_arg(args, char *)) != 0)
    {
    tmpArg = malloc(sizeof *tmpArg);
    tmpArg->next = ret->Error.Args;
    ret->Error.Args = tmpArg;

    strncpy(tmpArg->varnam, varnam, ARGNAM_LEN);
    tmpArg->type = (char) va_arg(args, int);

    switch (tmpArg->type)
    {
    case COMTYP_STRING:
    case COMTYP_DATTIM:
        tmpString = va_arg(args, char *);
        tmpArg->data.cdata = tmpString?malloc(strlen(tmpString) + 1):NULL;
        if (tmpString)
        strcpy(tmpArg->data.cdata, tmpString);
        break;

    case COMTYP_BOOLEAN:
    case COMTYP_INT:
    case COMTYP_LONG:
        tmpArg->data.ldata = va_arg(args, long);
        break;

    case COMTYP_FLOAT:
        tmpArg->data.fdata = va_arg(args, double);
        break;
    }

        tmpArg->lookup = va_arg(args, int);
    }

    va_end(args);

    return eOK;
}
