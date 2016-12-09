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
 *  Copyright (c) 2009
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
#include <stdlib.h>

#ifdef UNIX
# include <unistd.h>
#else
# include <windows.h>
# include <wtypes.h>
# include <winnls.h>
# include <mbctype.h>
#endif

#include <mocaerr.h>
#include <mocagendef.h>
#include <oslib.h>

#ifdef UNIX

FILE *osFopen(char *path, char *mode)
{
    return fopen(path, mode);
}

#else

static wchar_t *sConvertToUTF16(char *in)
{
    int outsize;

    wchar_t *out;

    /* Determine how much space we'll need for the UTF-16 string. */
    outsize = 2 * (strlen(in) + 1);

    /* Allocate space for the UTF-16 string. */
    out = (wchar_t *) malloc(outsize);

    /* Convert the given UTF-8 string to a UTF-16 string. */
    outsize = MultiByteToWideChar(CP_UTF8, 0, in, -1, out, outsize);
    if (!outsize)
    {
        misLogError("MultiByteToWideChar: %s", osError( ));
        free(out);
        return NULL;
    }

    return out;
}

FILE *osFopen(char *path, char *mode)
{
    wchar_t *wpath = NULL, 
	    *wmode = NULL;

    FILE *fp = NULL;

    /* Convert the given pathname to UTF-16. */
    wpath = sConvertToUTF16(path);
    if (!wpath)
    {
        misLogError("osFopen: Could not convert pathname to UTF-16");
	goto cleanup;
    }

    /* Convert the given mode to UTF-16. */
    wmode = sConvertToUTF16(mode);
    if (!wmode)
    {
        misLogError("osFopen: Could not convert mode to UTF-16");
	goto cleanup;
    }

    /* Open the given pathname. */
    fp = _wfopen(wpath, wmode);

cleanup:

    free(wpath);
    free(wmode);

    return fp;
}

#endif
