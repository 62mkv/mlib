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
#include <stdarg.h>

#include <mocagendef.h>
#include <mislib.h>

#include "servicemgr.h"

#define RPTAB_PATHNAME "%AllUsersProfile%\\Application Data\\Sam\\Server\\rptab"

static char *sBuildRegistryFilePathnameVarArgs(char *fmt, va_list args)
{
    long nbytes;
    static char msg[1024];

    nbytes = vsprintf(msg, fmt, args);

    return (nbytes > 0) ? msg : NULL;
}

static char *sBuildRegistryFilePathname(char *fmt, ...)
{
    char *msg;
    va_list args;

    va_start(args, fmt);
    msg = sBuildRegistryFilePathnameVarArgs(fmt, args);
    va_end(args);

    return msg;
}

static long sRegistryFileExists(char *pathname)
{
    char *env = NULL;
    FILE *infile;

    printf("Checking %s... ", pathname);
   
    infile = fopen(pathname, "r");
    if (infile)
    {
        fclose(infile);
        printf("found\n");
        return 1;
    }
    
    printf("not found\n");

    return 0;
}

static long sGetRptabInfo(char *lookfor, 
		          char **envname, 
			  char **lesdir, 
			  char **regfile) 
{
    long found = 0;

    char buffer[2048],
         *ptr,
         *temp,
         *pathname;

    FILE *infile;

    /* Get the expanded pathname to the rptab file. */ 
    pathname = misDynExpandVars(RPTAB_PATHNAME, NULL);

    /* Open the rptab file. */
    infile = fopen(pathname, "r");
    if (!infile)
    {
	printf("Could not find the rptab file\n");
        return 0;
    }

    printf("Looking for environment %s in rptab... ", lookfor);

    /* 
     * Cycle through each line in the rptab file. 
     *
     * Because we are only getting sizeof(line) bytes at a time we can't
     * deal with arbitrarily long pathnames, but there's potential for an
     * array boundary overwrite here and I don't think it makes sense to
     * muddy up the code for the sake of dealing with pathnames that are 
     * insanely long.
     */
    while (fgets(buffer, sizeof buffer, infile) != NULL)
    {
        /* Strip linefeeds, formfeeds and carriage returns. */
        misReplaceChars(buffer, "\n", "");
        misReplaceChars(buffer, "\r", "");
        misReplaceChars(buffer, "\f", " ");
           
        /* Get a pointer to the buffer that we can play with. */
        ptr = buffer;

        /* Skip past leading white-space. */
        while (isspace(*ptr))
            ptr++;

        /* Skip comments. */
        if (*ptr == '#')
            continue;

	/* Get the first field, which should be an environment name. */
        temp = strtok(ptr, ";");

        /* 
	 * If we found the environment name we are looking for we can make
	 * copies of the environment name, LESDIR and optional registry
	 * file pathname for the caller.
	 */
        if (temp && misCiStrcmp(temp, lookfor) == 0)
        {
	    found = 1;

	    misDynStrcpy(envname, temp);

            temp = strtok(NULL, ";");
	    misDynStrcpy(lesdir, temp);

	    temp = strtok(NULL, "\0");
	    misDynStrcpy(regfile, temp);

	    break;
        }
    }

    fclose(infile);

    free(pathname);

    if (found)
        printf("found\n");
    else
        printf("not found\n");

    return found;
}

long SetupRegistryFile(char *lookfor)
{
    long found = 0;

    char  *temp     = NULL,
	  *lesdir   = NULL,
	  *envname  = NULL,
	  *regfile  = NULL,
	  *username = NULL;

    /* 
     * Get the environment name, LESDIR and registry file pathname
     * from the rptab file. 
     */
    found = sGetRptabInfo(lookfor, &envname, &lesdir, &regfile);
    if (!found)
    {
	printf("Could not find a matching environment name\n");
	return 1;
    }

    /*
     * Our order of precedence to determine what registry file to use
     * is defined as follows:
     *
     *   1. The registry file from the rptab file.
     *   2. %LESDIR%\data\registry.%USERNAME%
     *   3. %LESDIR%\data\registry.%MOCA_ENVNAME%
     *   4. %LESDIR%\data\registry
     */

    printf("Looking for a registry file...\n");

    /* 1. Check for the registry file from the rptab file. */
    if (regfile)
    {
        found = sRegistryFileExists(regfile);
	if (found)
            goto cleanup;
    }

    /* 2. Check for %LESDIR%\data\registry.%USERNAME% */
    username = getenv("USERNAME");
    if (username)
    {
        regfile = sBuildRegistryFilePathname("%s%s.%s", lesdir, "\\data\\registry", username);

        found = sRegistryFileExists(regfile);
        if (found)
            goto cleanup;
    }

    /* 3. Check for %LESDIR%\data\registry.%MOCA_ENVNAME% */
    regfile = sBuildRegistryFilePathname("%s%s.%s", lesdir, "\\data\\registry", envname);

    found = sRegistryFileExists(regfile);
    if (found)
        goto cleanup;

    /* 4. Check for %LESDIR%\data\registry */
    regfile = sBuildRegistryFilePathname("%s%s", lesdir, "\\data\\registry");

    found = sRegistryFileExists(regfile);
    if (found)
        goto cleanup;

cleanup:

    /*
     * If we get to this point we weren't able to find a registry file. 
     */
    if (!found)
    {
	printf("Could not find a registry file\n");
    }

    /* 
     * Set the MOCA_ENVNAME and MOCA_REGISTRY environment variables.  We use
     * the environment name from the rptab file rather than the environment 
     * name the user provided so we don't have to be case-sensitive with the 
     * environment name argument. 
     */
    SetEnvironmentVariable(ENV_ENVNAME, envname);
    SetEnvironmentVariable(ENV_REGISTRY, regfile);
    
    misDynSprintf(&temp, "%s=%s", ENV_REGISTRY, regfile);
    putenv(temp);


    return found ? 0 : 1;
}
