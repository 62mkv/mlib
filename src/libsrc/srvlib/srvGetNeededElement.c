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
#include <jnilib.h>

#include "srvprivate.h"

static long sGetLength(char dtype, void *value)
{
    switch(dtype)
    {
        case COMTYP_INT:
        case COMTYP_LONG:
        case COMTYP_LONGPTR:
        case COMTYP_BOOLEAN:
            return sizeof(long);
            break;

        case COMTYP_FLOAT:
        case COMTYP_FLOATPTR:
            return sizeof(double);
            break;

        case COMTYP_CHAR:
        case COMTYP_CHARPTR:
        case COMTYP_TEXT:
        case COMTYP_DATTIM:
            return strlen((char *) value) + 1;
            break;

        /* 
         *  We don't currently support these for managed components.
         */
        case COMTYP_BINARY:
        case COMTYP_GENERIC:
        case COMTYP_JAVAOBJ:
        case COMTYP_RESULTS:
        default:
            break;
    }

    return 0;
}

long srv_GetContextVar(char *name, char *alias, int inOper,
                       char *dtype, void **value, long *length,
                       int *outOper, int markused)
{
    long status;

    /* Initialize our output variables. */
    if (dtype)   *dtype   = '\0';
    if (value)   *value   = NULL;
    if (length)  *length  = 0;
    if (outOper) *outOper = 0;


    /*
     * If an empty string is passed in, treat it as if it were NULL. We don't
     * want an empty column name to match everything.
     */
    if (name  && !*name)  name = NULL;
    if (alias && !*alias) alias = NULL;

    status = jni_srvGetContextVar(name, alias, inOper, dtype, value,
                                  length, outOper, markused);

    return status;
}

long srvGetNeededElement(char *name, char *alias, char *dtype, void **value)
{
    return srv_GetContextVar(name, alias, OPR_EQ, dtype, value, NULL, NULL, 0);
}

long srvGetNeededElementWithOper(char *name, char *alias, char *dtype, 
                                   void **value, int *oper)
{
    return srv_GetContextVar(name, alias, 0, dtype, value, NULL, oper,0);
}


/* this method is used externally by VB code so it needs to be MOCAEXPORT 
 * so it is __stdcall
 * It also doesn't return data as the void * isn't a concept avialable in VB */
long MOCAEXPORT srvGetNeededElementOper(char *name, char *alias, int *oper)
{
    char datatype[1];
    void *value;
    return srvGetNeededElementWithOper(name, alias,  
                                       datatype, &value, oper);
}

long srvGetNeededElementWithLength(char *name, char *alias, char *dtype, 
                                   void **value, long *length)
{
    return srv_GetContextVar(name, alias, OPR_EQ, dtype, value, length, NULL,0);
}

