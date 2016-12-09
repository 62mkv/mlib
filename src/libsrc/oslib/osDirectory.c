static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Fucntions for creating and removing directories.
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
#include <stdlib.h>

#ifdef HAVE_SYS_TYPES_H
# include <sys/types.h>
#endif
#ifdef HAVE_SYS_STAT_H
# include <sys/stat.h>
#endif
#ifdef WIN32
#include <direct.h>
#else
#include <unistd.h>
#endif

#include <mocaerr.h>
#include <mocagendef.h>
#include "osprivate.h"

static long os_CreateDirectory(char *directory)
{
#ifdef WIN32
    SECURITY_ATTRIBUTES sec_attr;

    if (directory[strlen(directory) - 1] == ':')
    {
	return eOK;
    }

    sec_attr.nLength = sizeof(sec_attr);
    sec_attr.lpSecurityDescriptor = NULL;
    sec_attr.bInheritHandle = FALSE;

    if (CreateDirectory(directory, &sec_attr))
    {
	return eOK;
    }
    else
    {
	return ePERMISSION_DENIED;
    }
#else
    return mkdir(directory, 0775);
#endif
}


/*
 *  FUNCTION: osCreateDir
 *
 *  PURPOSE:  Create the given directory using the default ownership,
 *            creating its parent directories if necessary.
 *
 *  RETURNS:  eOK
 *            Some error code.
 */

long osCreateDir(char *i_directory)
{
    char filetype;
    long status;
    char *sepptr;
    int lcv;
    char *directory;
    char *tmp_dir;

    /* TODO ExpandVars */
    /* TODO Permisions (i.e. mode) */

    directory = malloc(strlen(i_directory) + 1);
    if (!directory)
    {
	return eNO_MEMORY;
    }

    strcpy(directory, i_directory);
#ifdef WIN32
    for (; (sepptr = strchr(directory, '/')); *sepptr = PATH_SEPARATOR) ;
#endif

    if ((status = osFileInfo(directory, &filetype)) == eOK)
    {
	free(directory);
	if (filetype == OS_FILETYPE_DIR)
	{
	    return eOK;
	}
	else
	{
	    return eINVALID_OPERATION;
	}
    }
    tmp_dir = malloc(strlen(directory) + 1);
    if (tmp_dir == NULL)
    {
	free(directory);
	return eNO_MEMORY;
    }

    lcv = 0;
    while ((sepptr = strchr(directory + lcv, PATH_SEPARATOR)))
    {
	if (sepptr > directory)
	{
	    lcv = sepptr - directory;
	}
	else
	{
	    lcv = directory - sepptr;
	}
	if (lcv > 0)
	{
	    sprintf(tmp_dir, "%.*s", lcv, directory);
	    if ((status = osFileInfo(tmp_dir, &filetype)) == eOK)
	    {
		if (filetype != OS_FILETYPE_DIR)
		{
		    free(directory);
		    free(tmp_dir);
		    return eINVALID_OPERATION;
		}
	    }
	    else
	    {
		status = os_CreateDirectory(tmp_dir);
	    }
	}
	lcv++;
    }

    status = os_CreateDirectory(directory);
    free(tmp_dir);
    free(directory);
    if (status == 0)
    {
	return eOK;
    }
    else
    {
	return ePERMISSION_DENIED;
    }
}


/*
 *  FUNCTION: osRemoveDir
 *
 *  PURPOSE:  Remove the given directory.
 *
 *  RETURNS:  eOK
 *            Some error code.
 */

long osRemoveDir(char *directory)
{
#ifdef WIN32
    if (RemoveDirectory(directory))
	return eOK;
    else
	return eERROR;
#else
    return rmdir(directory);
#endif
}


/*
 *  FUNCTION: osGetCurrentDir
 *
 *  PURPOSE:  Get the current working directory.
 *
 *  RETURNS:  Pointer to the current working directory.
 *            Some error code.
 */

char *osGetCurrentDir(void)
{
    char *cwd;

    /* Allocate space for the current working directory. */
    if ((cwd = (char *) calloc(1, PATHNAME_LEN + 1)) == NULL)
	return NULL;

    return getcwd(cwd, PATHNAME_LEN);
}
