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
#include <ctype.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <oslib.h>

#include "msql.h"

long ProcessScript(char *pathname)
{
    long status = 0;
    char temp[1024];
    FILE *fp;

    /* Trim whitespace from each side of the pathname. */
    pathname = misTrimLR(pathname);

    /* 
     * We try opening the script once as the given pathname.  If we can't 
     * we'll be nice and add a ".msql" filename extension to it and then 
     * try opening it that way.
     */
    fp = fopen(pathname, "r");
    if (fp == NULL)
    {
	sprintf(temp, "%s.msql", pathname);

	fp = fopen(temp, "r");
	if (fp == NULL)
	{
	    Print("fopen: %s\n", osError( ));
	    Print("Could not open script file: %s\n", pathname);
	    return eFILE_OPENING_ERROR;
        }
    }

    /* Read and process the contents of the script. */
    status = ProcessInput(fp, "" , "" , "" , "", 0);
    /*if (status != 0)
    {
	Print("%ld error(s) occurred while executing script file\n", status);
	Print("\n");
	}*/

    fclose(fp);

    return status;
}
