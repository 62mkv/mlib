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
 *#END*************************************************************************/

#include <moca.h>
#include <mocagendef.h>

#include <string.h>

char *osDirName(char *pathname)
{
    char *ptr;

    static char dot[] = ".";
    static char dirname[PATHNAME_LEN + 1];

    /* Expand environment variables in the given pathname. */
    memset(dirname, 0, sizeof dirname);
    misExpandVars(dirname, pathname, PATHNAME_LEN, NULL);

    /* Remove duplicate path separators and fix them. */
    misFixFilePath(dirname);

    /* An empty pathname or one with no path separators is a dot. */
    if ((dirname[0] == '\0') || (strchr(dirname, PATH_SEPARATOR) == NULL))
    {
	strcpy(dirname, dot);
	return dirname;
    }

    /* Set a pointer to the end of the directory name. */
    ptr = &dirname[strlen(dirname) - 1];

    /* Move backward past any path separators. */
    while ((*ptr == PATH_SEPARATOR) && (ptr != dirname))
	*ptr-- = '\0';

    /* Move backward past the current directory or file. */
    while ((*ptr != PATH_SEPARATOR) && (ptr != dirname))
	*ptr-- = '\0';

    /* Move backward past any path separators. */
    while ((*ptr == PATH_SEPARATOR) && (ptr != dirname))
	*ptr-- = '\0';

    return dirname;
}
