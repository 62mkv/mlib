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

#include <mislib.h>

#include "msql.h"

static char *sReadFileContents(char *file)
{
    char *ptr;
    char temp[256];

    MIS_DYNBUF *buffer;

    FILE *fp;

    fp = fopen(file, "r");
    if (fp == NULL)
        return NULL;

    buffer = misDynBufInit(0);

    while (fgets(temp, sizeof(temp), fp))
	misDynBufAppendString(buffer, temp);

    fclose(fp);

    /* Get the string from the dynamic buffer. */
    ptr = misDynBufClose(buffer);

    /* Trim any trailing space or non-printables. */
    misTrim(ptr);

    return ptr;
}

int EditCommand(int index)
{
    char *cmd    = NULL,
	 *buffer = NULL;
	int stat = 0;

    FILE *fp;

    /*
     * Create the temporary file. 
     */
    fp = fopen("mocaedt.buf", "w");
    if (fp == NULL)
    {
        Print("Could not open mocaedt.buf for editing\n");
		stat = 1;
        goto cleanup;
    }

    if (GetHistory(index))
        fprintf(fp, "%s", GetHistory(index));

    fclose(fp);

    /*
     * Pull the temporary file up in an editor.
     */
    misDynSprintf(&cmd, "%s mocaedt.buf", osGetVar("EDITOR")
                                          ? osGetVar("EDITOR")
                                          : OS_DEFAULT_EDITOR);
    system(cmd);

    free(cmd);

    /*
     * Read the contents of the temporary file back in.
     */
    buffer = sReadFileContents("mocaedt.buf");
    if (!buffer)
    {
        Print("Count not open mocaedt.buf for reading\n");
		stat = 1;
        goto cleanup;                     
    }

    PutHistory(buffer);
    ListHistory(1, 1);

cleanup:

    free(buffer);
    remove("mocaedt.buf");

    return stat;
}
