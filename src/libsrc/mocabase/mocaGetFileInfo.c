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
 *#END************************************************************************/

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <oslib.h>
#include <srvlib.h>

LIBEXPORT 
RETURN_STRUCT *mocaGetFileInfo(char *pathname_i)
{
    long  context = 0;
    char *found = NULL;
    char  type;
    char  pathname[1024];
    char *ptr,
	 *created,
	 *accessed,
	 *modified;
    long  size;

    RETURN_STRUCT *results = NULL;

    /* Check required arguments. */
    if (! pathname_i || ! strlen(pathname_i))
	return srvResults(eINVALID_ARGS, 
			  "pathname", COMTYP_CHAR,         1024, NULL,
			  "type",     COMTYP_CHAR,            1, NULL,
			  "size",     COMTYP_INT,  sizeof(long), NULL,
			  "created",  COMTYP_CHAR,          100, NULL,
			  "accessed", COMTYP_CHAR,          100, NULL,
			  "modified", COMTYP_CHAR,          100, NULL,
			  NULL);

    /* Expand environment variables in the pathname. */
    misExpandVars(pathname, pathname_i, sizeof pathname, NULL);

    /* Fix the file path to be platform appropriate. */
    ptr = misFixFilePath(pathname);

    /* Remove the trailing pathname separator if necessary. */
    if (strlen(ptr) && ptr[strlen(ptr)-1] == PATH_SEPARATOR)
        ptr[strlen(ptr)-1] = '\0';

    /* Get all the file information. */
    osFileInfo(ptr, &type);
    osFileSize(ptr, &size);
    osFileCreated(ptr, &created);
    osFileAccessed(ptr, &accessed);
    osFileModified(ptr, &modified);

    /* Create the return structure. */
    results = srvResults(eOK, 
			 "pathname", COMTYP_CHAR,         1024,  ptr,
			 "type",     COMTYP_CHAR,            1,  &type,
			 "size",     COMTYP_INT,  sizeof(long),  size,
			 "created",  COMTYP_CHAR,          100,  created,
			 "accessed", COMTYP_CHAR,          100,  accessed,
			 "modified", COMTYP_CHAR,          100,  modified,
			 NULL);

    return results;
}
