static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Build application startup and version banners to display.
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
#include <time.h>

#include <copyright.h>

#include <mislib.h>
#include <mocaversion.h>


/*
 *  FUNCTION: misGetStartBanner
 *
 *  PURPOSE:  Build a banner message appropriate for application startup.
 *
 *  RETURNS:  Pointer to banner.
 */

char *misGetStartBanner(char *appName)
{
    static char banner[1024];
    time_t timenow;

    time(&timenow);

    /* Build the banner to return to the caller. */
    sprintf(banner, "\n%s %s - %s\n"
		    "%s\n\n", appName, 
			      RELEASE_VERSION,
			      ctime(&timenow),
			      COPYRIGHT_STRING);

    return(banner);
}

char *misGetVersion(void)
{
    return RELEASE_VERSION;
}

/*
 *  FUNCTION: misGetVersionBanner
 *
 *  PURPOSE:  Build a banner message appropriate for printing the version.
 *
 *  RETURNS:  Pointer to banner.
 */
char *misGetVersionBanner(char *appName)
{
    static char banner[1024];

    /* Build the banner to return to the caller. */
    sprintf(banner, "\n%s %s\n\n", appName, misGetVersion());

    return(banner);
}
