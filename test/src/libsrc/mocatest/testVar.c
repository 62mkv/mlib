static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL: https://athena.redprairie.com/svn/prod/moca/trunk/src/libsrc/mislib/mi
sGetVarPassword.c $
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to deal with passwords that we encode/decode.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <oslib.h>
#include <srvlib.h>

LIBEXPORT 
RETURN_STRUCT *testGetVar(char *name)
{
    char *e1;

    RETURN_STRUCT *results = NULL;
    
    e1 = osGetVar(name);
    results = srvResults(eOK, "value", COMTYP_CHAR, e1 ? strlen(e1) : 0, e1, NULL); 

    return results;
}

LIBEXPORT 
RETURN_STRUCT *testSetVar(char *name, char *value)
{
    RETURN_STRUCT *results = NULL;
    
    osPutVar(name, value, NULL);
    results = srvResults(eOK, NULL); 

    return results;
}
