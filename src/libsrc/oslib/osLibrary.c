static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to handle explicit loading of libraries.
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
#include <errno.h>
#include <fcntl.h>
#include <limits.h>

#if defined (HAVE_DLOPEN)
# include <dlfcn.h>
#elif defined (HAVE_SHL_LOAD)
# include <dl.h>
#endif

#include <mocaerr.h>
#include <mocagendef.h>
#include <oslib.h>



/*
 *  Function Prototypes
 */

static char *os_LibraryFilename(char *library);

char *osLibraryError(void)
{
#if defined (HAVE_DLOPEN)
    return (char *)dlerror();
#else
    return osError();
#endif

}

/*
 *  FUNCTION: osLibraryOpen
 *
 *  PURPOSE:  Open a shared library.
 *
 *  RETURNS:  Pointer to a handle.
 *            NULL - An error occurred.
 */

void *osLibraryOpen(char *libname)
{
    void *handle;	/* Shared library handle   */
    char *filename;	/* Shared library filename */

    /* Get the shared library's filename. */
#ifndef WIN32
    filename = os_LibraryFilename(libname);
#else
    filename = libname;
#endif

#if defined (HAVE_DLOPEN)

    /* Open the shared library. */
    handle = dlopen(filename, RTLD_NOW|RTLD_GLOBAL);
    if (handle == NULL)
    {
	return(NULL);
    }

#elif defined (HAVE_SHL_LOAD)

    /* Allocate space for the handle. */
    handle = (shl_t *) malloc(sizeof(shl_t));
    if (handle == NULL)
    {
	return(NULL);
    }

    /* Open the shared library. */
    * (shl_t *) handle = shl_load(filename, BIND_IMMEDIATE | BIND_VERBOSE | DYNAMIC_PATH, 0L);
    if (* (shl_t *) handle == NULL)
    {
	return(NULL);
    }

#elif defined (WIN32)

    /* Open the shared library. */
    handle = LoadLibrary(filename);

#endif

    return(handle);
}

/*
 *  FUNCTION: osLibraryOpenByMember
 *
 *  PURPOSE:  Open a shared library.
 *
 *  RETURNS:  Pointer to a handle.
 *            NULL - An error occurred.
 */

void *osLibraryOpenByMember(char *libname, char *member)
{

#ifndef AIX

    return osLibraryOpen(libname);

#else

    void *handle;	/* Shared library handle          */
    char *loadname;	/* Shared library and member name */

    /* Initialize the library and member name to load. */
    loadname = NULL;

    /* Build the library and member name to load. */
    misDynSprintf(&loadname, "%s.a(%s)", libname, member);

    /* Open the shared library. */
    handle = dlopen(loadname, RTLD_NOW|RTLD_GLOBAL|RTLD_MEMBER);

    /* Free memory Build the library and member name to load. */
    free(loadname);

    return(handle);

#endif
}

/*
 *  FUNCTION: osLibraryClose
 *
 *  PURPOSE:  Close the shared library.
 *
 *  RETURNS:  void
 */

void osLibraryClose(void *handle)
{

#if defined (HAVE_DLOPEN)

     dlclose(handle);

#elif defined (HAVE_SHL_LOAD)

    /*
     * We used to call this:
     *
     * shl_unload(* (shl_t *) handle);
     *
     * but now we don't, since shl_unload blindly unloads the library,
     * ignoring whether the process has an implicit reference to it, or
     * another explicit one.
     */
    free(handle);

#elif defined (WIN32)

    FreeLibrary(handle);

#endif

    return;
}


/*
 *  FUNCTION: osLibraryLookupFunction
 *
 *  PURPOSE:  Lookup a function in the shared library.
 *
 *  RETURNS:  Pointer to a function.
 *            NULL - An error occurred.
 */

OSFPTR osLibraryLookupFunction(void *handle, char *symname)
{
    OSFPTR function;	/* Pointer to function   */

#if defined (HAVE_DLOPEN)

    /* Lookup the symbol in the shared library. */
    function = (OSFPTR) dlsym(handle, symname);

#elif defined (HAVE_SHL_LOAD)
    long   status;	/* Status of last call   */

    /* Lookup the symbol in the shared library. */
    status = shl_findsym(handle, symname, TYPE_PROCEDURE, &function);
    if (status != 0)
	function = NULL;

#elif defined (WIN32)

    /* Lookup the symbol in the shared library. */
    function = (OSFPTR) GetProcAddress(handle, symname);

#endif

    return(function);
}


/*
 *  FUNCTION: os_LibraryFilename
 *
 *  PURPOSE:  Build the shared library's filename.
 *
 *  RETURNS:  Pointer to the shared library's filename.
 */

static char *os_LibraryFilename(char *library)
{
    static char filename[PATHNAME_LEN];

    /* Append the shared library filename suffix. */
#if defined (HAVE_DLOPEN)
# if defined (OSX)
    sprintf(filename, "%s.dylib", library);
# else
    sprintf(filename, "%s.so", library);
# endif
#elif defined (HAVE_SHL_LOAD)
    sprintf(filename, "%s.sl", library);
#endif

    return(filename);
}
