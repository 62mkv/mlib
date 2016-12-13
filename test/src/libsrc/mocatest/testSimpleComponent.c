static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: 
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

#include <common.h>
#include <mocaerr.h>
#include <srvlib.h>
#include <sqllib.h>
#include <mislib.h>

LIBEXPORT RETURN_STRUCT *testSimpleComponent(char *arg)
{
    RETURN_STRUCT *results = NULL;

    results = srvResults(eOK, "foo", COMTYP_CHAR, 100, "zzz", 
                              "bar", COMTYP_CHAR, 1000, arg, NULL);

    return results;
}