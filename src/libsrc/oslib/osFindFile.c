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
 *#END*************************************************************************/

#define MOCA_ALL_SOURCE

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#ifdef HAVE_GLOB_H
# include <glob.h>
#endif

#ifdef HAVE_SYS_STAT_H
# include <sys/stat.h>
#endif

#ifdef HAVE_SYS_TYPES_H
# include <sys/types.h>
#endif

#include <mocaerr.h>
#include <mocagendef.h>
#include "osprivate.h"

typedef struct
{
    char           *fname;
#ifdef UNIX
    time_t          mtime;
#else
    FILETIME        mtime;
#endif
} fileinfo;

typedef struct
{
    fileinfo       *flist;
    int             current;
    int             max;
} FindFile_t;

/* date sort function for reverse date of modification sorting */
/* uses sortstuff buffers */
/* input: (fileinfo *)a, (fileinfo *)b */
/* if a>b return 1 */
/* if a=b return 0 */
/* if a<b return -1 */

static int datechk(const void *a, const void *b)
{
#ifdef UNIX
    return ((fileinfo *) a)->mtime - ((fileinfo *) b)->mtime;
#else
    return (int) CompareFileTime(&((fileinfo *) a)->mtime, &((fileinfo *) b)->mtime);
#endif
}

/* name sort function for sorting */
/* input: (fileinfo *)a, (fileinfo *)b */

static int namechk(const void *a, const void *b)
{
    return strcmp(((fileinfo *) a)->fname, ((fileinfo *) b)->fname);
}

#ifdef UNIX /* { */
static long FillContext(char *Filespec_in, FindFile_t * Context, int FindFlags)
{
    glob_t          pglob;
    int             glob_status;
    int             glob_flags = 0;
    struct stat     stat_buf;
    int             ii;
    char           *tmp_ptr;
    char           *Filespec;

    /* If we're not sorting by name, why sort at all? */
    if (!(FindFlags & OS_FF_NAMESORT))
	glob_flags = GLOB_NOSORT;

#ifdef HAVE_BROKEN_GLOB_SLASH
    /*
     * This is mostly for AIX.  For some reason, if a file has multiple
     * slashes in it, glob breaks.  We need to handle the doubled slashes
     * in this code.  Only do this for platforms with a broken glob, though.
     */
    { /* For Scope */
	char *s, *d;
	Filespec = malloc(strlen(Filespec_in)+1);
	for (d=Filespec, s=Filespec_in; *s; s++,d++)
	{
	    while (*s == '/' && *(s+1) == '/')
		s++;
	    *d = *s;
	}
	*d = '\0';
    }
#else
    Filespec = Filespec_in;
#endif


    glob_status = glob(Filespec, glob_flags, NULL, &pglob);

#ifdef HAVE_BROKEN_GLOB_SLASH
    free(Filespec);
    Filespec = Filespec_in;
#endif

    switch (glob_status)
    {
        case 0:
        {
	    Context->max = pglob.gl_pathc;
	    Context->current = 0;
	    Context->flist = NULL;

	    if (pglob.gl_pathc)
	    {
		Context->flist = malloc(pglob.gl_pathc * sizeof(fileinfo));
		for (ii = 0; ii < pglob.gl_pathc; ii++)
		{

		    if (FindFlags & OS_FF_FULLPATH)
		    {
			tmp_ptr = pglob.gl_pathv[ii];
		    }
		    else
		    {
			/* Just get the filename portion */
			tmp_ptr = osBaseFile(pglob.gl_pathv[ii]);
		    }

		    Context->flist[ii].fname = 
		    strcpy(malloc(strlen(tmp_ptr) + 1), tmp_ptr);

		    stat(pglob.gl_pathv[ii], &stat_buf);
		    Context->flist[ii].mtime = stat_buf.st_mtime;
		}
	    }
            break;
        }
#ifdef GLOB_NOMATCH
        case GLOB_NOMATCH:
        {
            /* 
             * Returned when no matches are found and (flags & GLOB_NOCHECK).
             */
            Context->flist = NULL;
            Context->current = 0;
            Context->max = 0;
            break;
        }
#endif
        default:
        {
            return eERROR;
        }
    }

    globfree(&pglob);
    return eOK;
}

#else /* }{ */

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

static char *sConvertToUTF8(wchar_t *in)
{
    int outsize;

    char *out;

    /* Determine how much space we'll need for the UTF-16 string. */
    outsize = WideCharToMultiByte(CP_UTF8, 0, in, -1, 0, 0, 0, 0);
    if (!outsize)
    {
        misLogError("WideCharToMultiByte: %s", osError( ));
        misLogError("Could not get filename byte length");
        return NULL;
    }

    /* Allocate space for the UTF-16 string. */
    out = malloc(outsize);

    /* Convert the given UTF-16 string to a UTF-8 string. */
    outsize = WideCharToMultiByte(CP_UTF8, 0, in, -1, out, outsize, 0, 0);
    if (!outsize)
    {
        misLogError("WideCharToMultiByte: %s", osError( ));
        misLogError("Could not convert filename to UTF-8");
        free(out);
        return NULL;
    }

    return out;
}

static long FillContext(char *Filespec, FindFile_t * Context, int FindFlags)
{
    WIN32_FIND_DATAW FindData;
    HANDLE           hFindFile;
    wchar_t         *wFilespec;
    char            *tmpptr, *baseptr, *sepptr;
    int              ii;

    /* Convert the given pathname to UTF-16. */
    wFilespec = sConvertToUTF16(Filespec);
    if (!wFilespec)
    {
	Context->max = 0;
	Context->current = 0;
        return eERROR;
    }

    hFindFile = FindFirstFileW(wFilespec, &FindData);
    if (hFindFile == INVALID_HANDLE_VALUE)
    {
	Context->max = 0;
	Context->current = 0;
        free(wFilespec);
        return eOK;
    }

    ii = 0;
    sepptr = strrchr(Filespec, PATH_SEPARATOR);
    do
    {
	/* realloc w/ NULL is the same as malloc, so this will work */
	Context->flist = realloc(Context->flist, (ii + 1) * sizeof(fileinfo));

        if (!(FindFlags & OS_FF_FULLPATH))
	{
            /* Convert the given pathname to UTF-16. */
            tmpptr = sConvertToUTF8(FindData.cFileName);
            if (!tmpptr)
            {
	        Context->max = 0;
	        Context->current = 0;
                return eERROR;
            }

	    /* Just get the filename portion */
	    baseptr = osBaseFile(tmpptr);

	    Context->flist[ii].fname = strcpy(malloc(strlen(baseptr) + 1), baseptr);

	    free(tmpptr);
	}
	else
	{
            /* Convert the given pathname to UTF-16. */
            tmpptr = sConvertToUTF8(FindData.cFileName);
            if (!tmpptr)
            {
	        Context->max = 0;
	        Context->current = 0;
                return eERROR;
            }

	    Context->flist[ii].fname = malloc(strlen(tmpptr) + 
					      (sepptr ? (sepptr - Filespec + 1)
					              : 0) + 1);
	    sprintf(Context->flist[ii].fname, "%.*s%s",
		    sepptr?(sepptr - Filespec + 1):0, Filespec, tmpptr);

	    free(tmpptr);
	}

	Context->flist[ii].mtime = FindData.ftLastWriteTime;
	ii++;
    } while (FindNextFileW(hFindFile, &FindData));

    Context->max = ii;
    Context->current = 0;

    FindClose(hFindFile);

    /* Sort the filenames by name if necessary. */
    if (!(FindFlags & OS_FF_NOSORT) && (FindFlags & OS_FF_NAMESORT))
        qsort(Context->flist, Context->max, sizeof(fileinfo), namechk);

    free(wFilespec);

    return eOK;
}
#endif /* } */

long osFindFile(char *Filespec, char *Filename, OS_FF_CONTEXT * PassedContext, 
                int FindFlags)
{
    FindFile_t    **Context;

    Context = (FindFile_t **) PassedContext;

    if (!*Context)
    {
        *Context = calloc(1, sizeof(FindFile_t));
        if (eOK != FillContext(Filespec, *Context, FindFlags))
        {
            free(*Context);
            *Context = NULL;
            return eERROR;
        }

        if ((*Context)->max)
        {
	    /* Only do the date sort if necessary. */
	    if (!(FindFlags & OS_FF_NOSORT) && !(FindFlags & OS_FF_NAMESORT))
		qsort((*Context)->flist, (*Context)->max, sizeof(fileinfo), datechk);
        }
    }

    if ((*Context)->current >= (*Context)->max)
        return eNO_MORE_FILENAMES;

    strcpy(Filename, (*Context)->flist[(*Context)->current].fname);
        (*Context)->current++;

    return eOK;
}

/*  osEndFindFile - deallocate context for file list */
/*                  deallocate each filename string */
/*                  deallocate the list structure */
long osEndFindFile(OS_FF_CONTEXT * inContext)
{
    int             ii;
    FindFile_t     *Context;

    Context = (FindFile_t *) * inContext;
    if (Context)
    {
	if (Context->flist)
	{
	    for (ii = 0; ii < Context->max; ii++)
	    {
		free(Context->flist[ii].fname);
	    }
	    free(Context->flist);
	}
	free(Context);

	*inContext = (OS_FF_CONTEXT) 0;
    }
    return eOK;
}
