static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Function to send a file to a printer.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2004
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

#include "mocabase.h"

LIBEXPORT
RETURN_STRUCT *mocaPrintFile(char *printer_i, 
			     char *pathname_i, 
			     long *copies_i,
			     long *rawFlag_i,
			     long *removeFlag_i)
{
    long status,
	 copies,
         rawFlag,
	 removeFlag;

    char *printer  = NULL,
	 *pathname = NULL;

    RETURN_STRUCT *ret = NULL;

    /* Make sure we have all our required arguments. */
    if (!printer_i || !*printer_i)
        return MOCAMissingArg("printer");
    if (!pathname_i || !*pathname_i)
        return MOCAMissingArg("pathname");

    /* Firewall our arguments. */
    copies     = (copies_i     && *copies_i)     ? *copies_i : 1;
    rawFlag    = (rawFlag_i    && *rawFlag_i)    ? 1         : 0;
    removeFlag = (removeFlag_i && *removeFlag_i) ? 1         : 0;
    misDynStrcpy(&printer, printer_i);
    misDynStrcpy(&pathname, pathname_i);

    /* Print the file. */
    status = osPrintFile(pathname, printer, copies, NULL, rawFlag);
    if (status != eOK)
    {
        misLogError("osPrintFile: Error %ld", status);
        misLogError("mocaPrintFile: Could not print file");
        misLogError("Pathname: %s", pathname_i);
	ret = srvErrorResults(status, "Could not print file", NULL);
	goto cleanup;
    }

    /* Remove the file if necessary. */
    if (removeFlag)
	remove(pathname);
   
    /* Create our return struct. */
    ret = srvResults(eOK, 
                     "printer",  COMTYP_STRING, strlen(printer),  printer,
                     "pathname", COMTYP_STRING, strlen(pathname), pathname,
                     NULL);

cleanup:

    free(printer);
    free(pathname);

    return ret;
}
