static const char rcsid[] = "$Id$";
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
 *  Copyright (c) 2011
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
#include <stdlib.h>

#include <mocagendef.h>
#include <oslib.h>

#include "servicemgr.h"

long SetupEnvironment(char *environment)
{
    long status;
    char *variable, *value;
    void *ctx;

    status = SetupRegistryFile(environment);
    if (status != 0)
        return 1;

    /*
     * Now, go through and grab any environment variables
     * this allows for override of the default values.
     */
    ctx = NULL;
    while ((osEnumerateRegistry(REGSEC_ENVIRONMENT, &ctx, &variable, &value)))
    {
        osPutVar(variable, value, NULL);
    }

    ctx = NULL;
    while ((osEnumerateRegistry(REGSEC_ENVIRONMENT, &ctx, &variable, &value)))
    {
        SetEnvironmentVariable(variable, value);
    }
    
    return 0;
}
