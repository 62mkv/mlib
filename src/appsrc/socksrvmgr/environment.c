/*#START***********************************************************************
 *
 *  $URL$
 *  $Author$
 *
 *  Description: 
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

#include "socksrvmgr.h"

void set_environment(char *process)
{
    void *ctxt = NULL;

    char *name,
         *value,
         *prefix = NULL;

    /* Build the environment variable prefix we're looking for. */
    misDynSprintf(&prefix, "%s.", process);

    /* Cycle through every environment variable in the registry. */
    while (osEnumerateRegistry(REGSEC_ENVIRONMENT, &ctxt, &name, &value))
    {
	char *envvar = NULL;

        /* Don't bother if this environment variable isn't for this process. */
        if (misCiStrncmp(name, prefix, strlen(prefix)) != 0)
            continue;

        /* Skip past the prefix. */
        name = name + strlen(prefix);

        /* Build the environment variable name/value pair. */
        misDynSprintf(&envvar, "%s=%s", name, value);

        /* Put the variable into the environment. */
        putenv(envvar);
    }

    free(prefix);

    return;
}
