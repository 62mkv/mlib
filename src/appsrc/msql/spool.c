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
 *#END************************************************************************/

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "msql.h"

static FILE *spoolFile;
static char *spoolFilename;

void StartSpooling(char *filename)
{
    /* Don't bother if we weren't given a spool filename. */
    if (!filename || !strlen(filename))
        return;

    /* Trim whitespace from each side of the filename. */
    filename = misTrimLR(filename);

    /* Stop any existing spooling we're doing. */
    StopSpooling( );

    /* Open the spool file. */
    spoolFile = fopen(filename, "w");
    if (spoolFile == NULL)
    {
        Print("%s\n", osError( ));
        Print("Could not open spool file: %s\n", spoolFile);
        return;
    }

    /* Make a copy of the spool filename. */
    misDynStrcpy(&spoolFilename, filename);
}

void StopSpooling(void)
{
    /* Don't bother if we aren't currently spooling. */
    if (!spoolFile)
        return;

    /* Close the spool file. */
    fclose(spoolFile);
    spoolFile = NULL;

    /* Free memory associated with the spool filname. */
    free(spoolFilename);
    spoolFilename = NULL;
}

void Spool(char *buffer)
{
    /* Don't bother if we aren't spooling or there's nothing to spool. */
    if (!spoolFile || !buffer || !strlen(buffer))
        return;

    fprintf(spoolFile, "%s\n", buffer);
    fflush(spoolFile);
}

long IsSpooling(void)
{
    return spoolFile ? 1 : 0;
}
