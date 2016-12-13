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

/*
 * The idea here is that we need to build a "environment strings" string to 
 * pass to CreateProcess( ) for the given process name.  The "environment 
 * strings" string is literally a "string of strings" with a trailing 
 * NULL on the tail end.  (e.g. VAR1=VAL1\0VAR2=VAL2\0\0)
 *
 * We do this via the following steps:
 *
 *   1. Get the current process's environment strings.
 *   2. Parse each name/value pair and put them into a hash.
 *   3. Look through the registry for any environment variables that
 *      are for the given process.
 *   4. If this environment variable already exists in the hash, remove it.
 *   5. Put this environment variable name/value pair into the hash.
 *   6. Enumerate through the hash creating the new environment strings.
 */

/*
 * Populate the hash from this process's environment strings.
 */

static void sPopulateEnvironmentHash(MISHASH *envhash)
{
    char *ptr,
	 *envstr;

    /* Get the environment strings for this process. */
    envstr = GetEnvironmentStringsA( );

    /* Point to the environment strings. */
    ptr = envstr;

    /* Cycle through each environment variable. */
    while (*ptr)
    {
        char *eq,
             *name,
             *value;
        char *payload = NULL;

        eq = strchr(ptr, '=');

	/*
         * Get the name and value of this environment variable.
         * If the variable name starts with '=', ignore it.
         */
        if (eq && eq != ptr)
        {
            name = malloc (eq - ptr + 1);
            strncpy(name, ptr, (eq - ptr));
            name[eq - ptr] = '\0';

            value = eq + 1;

            /* Make a copy of the value as the payload for the hash. */
            misDynStrcpy(&payload, value);

            /* Put this environment variable into the hash. */
            if (misHashPut(envhash, name, payload))
                OS_PANIC;

            free(name);
        }

	/* Move on to the next environment variable. */
	ptr += strlen(ptr) + 1;
    } 

    /* Free the environment strings. */
    FreeEnvironmentStrings(envstr);

    return;
}

/*
 * Populate the hash using any environment variables for the given process 
 * that are in the registry, removing environment variables from the hash if 
 * they are already there.
 */

static void sRepopulateEnvironmentHash(MISHASH *envhash, char *process)
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
        char *payload = NULL;

        /* Don't bother if this environment variable isn't for this process. */
        if (misCiStrncmp(name, prefix, strlen(prefix)) != 0)
            continue;

        /* Get the name by skipping past the prefix. */
        name = name + strlen(prefix);

	/* Let's be nice and make the cases consistently uppercase. */
	misToUpper(name);

	/* Make a copy of the value as the payload for the hash. */
        misDynStrcpy(&payload, value);

	/* Remove this environment variable from the hash if necessary. */
	if (misHashGet(envhash, name))
	{
            char *payload;

	    payload = misHashDelete(envhash, name);
	    free(payload);
	}

        /* Put this environment variable into the hash. */
	misHashPut(envhash, name, payload);
    }

    free(prefix);

    return;
}

/*
 * Free everything associated with the has.
 */

static void sFreeEnvironmentHash(MISHASH *envhash)
{
    char *payload;

    /* Cycle through each environment variable in the hash. */
    while (misHashEnum(envhash, &payload))
    {
        free(payload);
    }

    misHashFree(envhash);
}

/*
 * Build an "environment strings" string from the hash.
 */

static char *sGetChildEnvironmentStrings(MISHASH *envhash)
{
    char *name   = NULL,
	 *value  = NULL,
	 *envstr = NULL;

    MIS_DYNBUF *buffer;

    /* Initialize the dynamic buffer. */
    buffer = misDynBufInit(32);

    /* Cycle through each environment variable in the hash. */
    while ((name = misHashEnum(envhash, &value)) != NULL)
    {
        misDynBufAppendString(buffer, name); 
        misDynBufAppendChar(buffer, '='); 
        misDynBufAppendString(buffer, value); 
        misDynBufAppendChar(buffer, '\0'); 
    }

    /* Append the trailing null byte. */
    misDynBufAppendChar(buffer, '\0'); 

    /* Close the dynamic buffer and get the environment strings. */
    envstr = misDynBufClose(buffer);

    return envstr;
}

char *get_environment_strings(char *process)
{
    char *envstr;

    MISHASH *envhash;

    /* Initialize the environment variable hash. */
    envhash = misCiHashInit(256);

    /* Populate the hash from this process's environment strings. */
    sPopulateEnvironmentHash(envhash);
    
    /* Repopulate the hash from the registry. */
    sRepopulateEnvironmentHash(envhash, process);

    /* Get the "environment strings" string. */
    envstr = sGetChildEnvironmentStrings(envhash);

    /* Free the hash. */
    sFreeEnvironmentHash(envhash);

    return envstr;
}
