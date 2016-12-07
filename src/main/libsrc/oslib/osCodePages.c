static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to handle conversions between code pages.
 *               See /usr/lib/nls/iconv/config.iconv for valid code pages.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2002-2009
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

#define MOCA_XOPEN_SOURCE

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef UNIX
# include <iconv.h>
#include <errno.h>
# include <memory.h>
# include <sys/types.h>
#else
# include <windows.h>
# include <wtypes.h>
# include <winnls.h>
# include <mbctype.h>
#endif

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <oslib.h>


/*
 *  Default Code Page
 */

#ifdef UNIX
# if defined (HPUX)
#  define DEFAULT_CODEPAGE "utf8"
# else
#  define DEFAULT_CODEPAGE "UTF-8"
#endif
#else
# define DEFAULT_CODEPAGE CP_UTF8
#endif


/* 
 *  Static Variables
 */

static long gWillConvert;

#ifdef UNIX
static char *gIntCodePage,
	    *gExtCodePage;
#else
static UINT gIntCodePage,
            gExtCodePage;
#endif

#ifdef UNIX

/*
 *  FUNCTION: sInitializeCodePages
 *
 *  PURPOSE:  Initialize the "internal" and "external" code page and 
 *            determine if conversion of code pages is going to need 
 *            to take place.
 *
 *            The external page is set using the following precedence:
 *
 *              1. Passed-in external code page
 *              2. MOCA_EXT_CODEPAGE
 *              3. Default value
 *
 *  RETURNS:  void
 */

static void sInitializeCodePages(char *i_extCodePage)
{
    static int initialized;

    static char *envExtCodePage;

    /* Get the MOCA_EXT_CODEPAGE environment variable value. */
    if (!initialized)
    {
        envExtCodePage = osGetVar(ENV_PREFIX "EXT_CODEPAGE");
        initialized = 1;
    }

    /* Set the internal code page. */
    gIntCodePage = DEFAULT_CODEPAGE;

    /* Set the external code page. */
    if (i_extCodePage)
	gExtCodePage = i_extCodePage;
    else if (envExtCodePage)
	gExtCodePage = envExtCodePage;
    else
	gExtCodePage = DEFAULT_CODEPAGE;

    /* Set the convert flag. */
    gWillConvert = strcmp(gIntCodePage, gExtCodePage) ? 1 : 0;

    return;
}


/*
 *  FUNCTION: os_ConvertArray
 *
 *  PURPOSE:  Convert the given string.
 *
 *  RETURNS:  The number of bytes that still need to be converted.
 *            -1 - An error occured.
 */

static size_t os_ConvertArray(char *inCodePage, 
			      char *inBuf,
		              char *outCodePage, 
			      char *outBuf, 
			      size_t outSize)
{
    short inPlaceFlag;
    char *inPtr,
   	 *outPtr;
    size_t status,
	   inBytesLeft, 
	   outBytesLeft;
    iconv_t cd;

    /* Initialize all our local variables. */
    inPtr  = inBuf,  inBytesLeft  = strlen(inBuf);
    outPtr = outBuf, outBytesLeft = outSize-1; /* Do not include null-termination character */

    /* 
     *  It's possible that the caller passed us the same pointer
     *  for both the input and output buffer because they want us
     *  to do the conversion in place. 
     */
    inPlaceFlag = (inBuf == outBuf) ? 1 : 0;

    /* Create a temporary output buffer that we'll use to convert the string. */
    if (inPlaceFlag)
    {
	/* Allocate space for our temporary output buffer. */
	outBuf = calloc(outSize, sizeof(char));
	if (!outBuf)
	{
	    misLogError("calloc: %s (%ld)", osError( ), osErrno( ));
	    misLogError("os_ConvertArray: Could not allocate memory");
	    return -1;
	}

	/* Reset our output pointer to our temporary output buffer. */
	outPtr = outBuf;
    }

    /* Open the conversion descriptor. */
    cd = iconv_open(outCodePage, inCodePage);
    if (cd == (iconv_t) -1)
    {
	misLogError("iconv_open: %s (%ld)", osError( ), osErrno( ));
	misLogError("os_ConvertArray: Could not open conversion descriptor");
        if (inPlaceFlag)
            free(outBuf);
        return -1;
    }

    /* Convert the string. */
#if defined(HPUX) || defined(SOLARIS)
    status = iconv(cd, (const char **) &inPtr, &inBytesLeft, &outPtr, 
		   &outBytesLeft);
#else
    status = iconv(cd, &inPtr, &inBytesLeft, &outPtr, &outBytesLeft);
#endif
    /* The rest of the buffer */
    if (status == -1)
    {
	switch (osErrno())
	{
            /* Ignore E2BIG, Output buffer is not big enough.  It's up applicaton to process 
			      the rest of the input characters. */
	    case E2BIG:
            /* Ignore EINVAL, Input buffer contains invalid multiplebyte character (HPUX). */
            /*                NOTE: Solaris does return an error status,                   */
	    /*                      instead places inPtr beyond bad characters, but may    */
	    /*                      consume characters that are not part of bad character. */
            case EINVAL:
		status = 0;
		/* NOTE: The application does not know whether 
		     1) output buffer is too small.
		     - or -
		     2) input buffer contains invalid character.

		     So the application should not loop around this call 
		 */
		break;
            default:
	        misLogError("iconv: %s (%ld)", osError( ), osErrno( ));
	        misLogError("os_ConvertArray: Could not convert code set");
                if (inPlaceFlag)
                    free(outBuf);
                return -1;
	}
    }

    /* If the output buffer still has room, null terminate the string */
    if (outBytesLeft)
    {
        *outPtr = '\0';
    } 
    else 
    {
        /* Force output buffer to be null termninated */
        outBuf[outSize-1] = '\0';
    }


    /* Close the conversion descriptor. */
    iconv_close(cd);

    /* Copy our temporary output buffer back into the caller's buffer. */
    if (inPlaceFlag)
    {
	/* Copy the string back into our caller's buffer. */
	memset(inBuf, '\0', outSize);
	memcpy(inBuf, outBuf, (outSize - outBytesLeft));

	/* Free our temporary output buffer. */
	free(outBuf);
    }

    return inBytesLeft;
}


/*
 *  FUNCTION: os_ConvertPtr
 *
 *  PURPOSE:  Convert the given string.
 *
 *  RETURNS:  The number of bytes that were converted.
 *            -1 - An error occured.
 */

static size_t os_ConvertPtr(char *inCodePage, 
			    char *inBuf,
		            char *outCodePage, 
			    char **outBuf)
{
    char *inPtr,
   	 *outPtr,
          tempPtr[1024];
    size_t status,
	   bytes,
	   inBytesLeft, 
	   outBytesLeft;
    iconv_t cd;

    /* Initialize all our local variables. */
    bytes = 0;
    *outBuf = NULL;
    inPtr  = inBuf,  inBytesLeft  = strlen(inBuf);
    outPtr = tempPtr, outBytesLeft = sizeof(tempPtr);

    /* Open the conversion descriptor. */
    cd = iconv_open(outCodePage, inCodePage);
    if (cd == (iconv_t) -1)
    {
	misLogError("iconv_open: %s (%ld)", osError( ), osErrno( ));
	misLogError("os_ConvertArray: Could not open conversion descriptor");
	return -1;
    }

    /* Convert each chunk of the string until we're done. */
    while (inBytesLeft)
    {
        /* Reinitialize the output variables on each iteration. */
        outPtr = tempPtr, outBytesLeft = sizeof(tempPtr);

        /* Convert the string. */
#if defined(HPUX) || defined(SOLARIS)
        status = iconv(cd, (const char **) &inPtr, &inBytesLeft, &outPtr, 
		       &outBytesLeft);
#else
        status = iconv(cd, &inPtr, &inBytesLeft, &outPtr, 
		       &outBytesLeft);
#endif
        if (status == -1)
        {
	    switch(osErrno())
	    {
                /* Ignore E2BIG, Output buffer is not big enough.  
	           It's up applicaton to process the rest of the input characters. */
	        case E2BIG:
		    status = 0;
		    break;
                /* Error on EINVAL, Input buffer contains invalid multiplebyte character (HPUX), */
                /*                  because MOCA does not know how far to advance, inPtr is on   */
                /*                  first byte of the invalid character.                         */
                /*                NOTE: Solaris does return an error status,                     */
	        /*                      instead places inPtr beyond bad characters, but may      */
	        /*                      consume characters that are not part of bad character.   */
                case EINVAL:
                default:
	          misLogError("iconv: %s (%ld)", osError( ), osErrno( ));
	          misLogError("os_ConvertPtr: Could not convert code set");
	          return -1;
	    }
        }

	/* Reallocate additional space for the output buffer. */
        *outBuf = realloc(*outBuf, bytes+(sizeof(tempPtr)-outBytesLeft)+1);
	if (!*outBuf)
	{
	    misLogError("realloc: %s (%ld)", osError( ), osErrno( ));
	    misLogError("os_ConvertPtr: Could not reallocate memory");
	    return -1;
	}

	/* Copy this set of characters into the output buffer. */
	memcpy(*outBuf+bytes, tempPtr, sizeof(tempPtr)-outBytesLeft);

	/* Increment the number of bytes converted. */
	bytes += sizeof(tempPtr) - outBytesLeft;
    }

    /* Null-terminate the output buffer. */
    (*outBuf)[bytes] = '\0';

    /* We want to count the null-terminator. */
    bytes += 1;

    /* Close the conversion descriptor. */
    iconv_close(cd);

    return bytes;
}


/*
 *  FUNCTION: osToIntCPArray
 *
 *  PURPOSE:  Convert the given string in the given array to the
 *            "internal" code page.  The "exteral" and "internal"
 *            arrays can be pointers to the same array.  If they
 *            are, the conversion will occur in place.
 *
 *  RETURNS:  The number of bytes that weren't converted.
 *            -1 - An error occurred.
 */

size_t osToIntCPArray(char *extStr, 
		      char *intStr, 
		      size_t intSize, 
		      char *extCodePage)
{
    size_t bytesLeft;

    /* Initialize ourselves if necessary. */
    sInitializeCodePages(extCodePage);

    /* Don't bother if the "external" string is null. */
    if (!extStr)
    {
	*intStr = '\0';
	return 0;
    }

    /* Don't bother if the "internal" string length is 0. */
    if (!intSize)
	return strlen(extStr) + 1;

    /* Don't bother if there's no need to convert. */
    if (!gWillConvert)
    {
	/* Only copy the string if the pointers are different. */
	if (intStr != extStr)
	{
	    /* We want to count the null-terminator. */
	    size_t extSize = strlen(extStr) + 1;

	    memset(intStr, '\0', intSize);
	    strncpy(intStr, extStr, intSize-1);

	    return (extSize <= intSize) ? 0 : (extSize - intSize);
        }

	return 0;
    }

    /* Convert the string to the "internal" code page. */
    bytesLeft = os_ConvertArray(gExtCodePage, extStr, 
				gIntCodePage, intStr, intSize);
    if (bytesLeft == -1)
    {
	misLogError("osToIntCPArray: Could not convert");
	return -1;
    }

    return bytesLeft;
}


/*
 *  FUNCTION: osToExtCPArray
 *
 *  PURPOSE:  Convert the given string in the given array to the
 *            "external" code page.  The "interal" and "external"
 *            arrays can be pointers to the same array.  If they
 *            are, the conversion will occur in place.
 *            
 *
 *  RETURNS:  The number of bytes that weren't converted.
 *            -1 - An error occurred.
 */

size_t osToExtCPArray(char *intStr, 
		      char *extStr, 
		      size_t extSize, 
		      char *extCodePage)
{
    size_t bytesLeft;

    /* Initialize ourselves if necessary. */
    sInitializeCodePages(extCodePage);

    /* Don't bother if the "internal" string is null. */
    if (!intStr)
    {
	*extStr = '\0';
	return 0;
    }

    /* Don't bother if the "external" string length is 0. */
    if (!extSize)
	return strlen(intStr) + 1;

    /* Don't bother if there's no need to convert. */
    if (!gWillConvert)
    {
	/* Only copy the string if the pointers are different. */
	if (extStr != intStr)
	{
	    /* We want to count the null-terminator. */
	    size_t intSize = strlen(intStr) + 1;

	    memset(extStr, '\0', extSize);
	    strncpy(extStr, intStr, extSize-1);

	    return (intSize <= extSize) ? 0 : (intSize - extSize);
        }

	return 0;
    }

    /* Convert the string to the "external" code page. */
    bytesLeft = os_ConvertArray(gIntCodePage, intStr, 
				gExtCodePage, extStr, extSize);
    if (bytesLeft == -1)
    {
	misLogError("osToExtCPArray: Could not convert");
	return -1;
    }

    return bytesLeft;
}


/*
 *  FUNCTION: osToIntCPPtr
 *
 *  PURPOSE:  Convert the string pointed to by the given "external"
 *            pointer to the "internal" code page, allocating space
 *            for the given "internal" pointer.
 *
 *  RETURNS:  The number of bytes the "internal" pointer points to.
 *            -1 - An error occurred.
 */

size_t osToIntCPPtr(char *extStr, 
                    char **intStr, 
                    char *extCodePage)
{
    size_t bytes;

    /* Initialize ourselves if necessary. */
    sInitializeCodePages(extCodePage);

    /* Point the "internal" pointer to NULL. */
    *intStr = NULL;

    /* Don't bother if the "external" string is null. */
    if (!extStr)
	return 0;

    /* Don't bother if there's no need to convert. */
    if (!gWillConvert)
    {
	/* We want to count the null-terminator. */
	size_t extSize = strlen(extStr) + 1;

	return extSize;
    }

    /* Convert the string to the "internal" code page. */
    bytes = os_ConvertPtr(gExtCodePage, extStr, gIntCodePage, intStr);
    if (bytes == -1)
    {
	misLogError("osToIntCPPtr: Could not convert");
	return -1;
    }

    return bytes;
}


/*
 *  FUNCTION: osToExtCPPtr
 *
 *  PURPOSE:  Convert the string pointed to by the given "internal"
 *            pointer to the "external" code page, allocating space
 *            for the given "external" pointer.
 *
 *  RETURNS:  The number of bytes the "external" pointer points to.
 *            -1 - An error occurred.
 */

size_t osToExtCPPtr(char *intStr, 
                    char **extStr, 
                    char *extCodePage)
{
    size_t bytes;

    /* Initialize ourselves if necessary. */
    sInitializeCodePages(extCodePage);

    /* Point the "external" pointer to NULL. */
    *extStr = NULL;

    /* Don't bother if the "internal" string is null. */
    if (!intStr)
	return 0;

    /* Don't bother if there's no need to convert. */
    if (!gWillConvert)
    {
	/* We want to count the null-terminator. */
	size_t intSize = strlen(intStr) + 1;

	return intSize;
    }

    /* Convert the string to the "external" code page. */
    bytes = os_ConvertPtr(gIntCodePage, intStr, gExtCodePage, extStr);
    if (bytes == -1)
    {
	misLogError("osToExtCPPtr: Could not convert");
	return -1;
    }

    return bytes;
}

/*
 *  FUNCTION: osGetExtCodePage
 *
 *  PURPOSE:  Returns the configured external code pages to be used for 
 *            any code page conversions done.
 *            This is mostly intended for use by functions doing their
 *            own multibytetowidechar conversions for BSTR interopability
 *            (mocasrvobj-nt)
 *
 *  RETURNS:  String representing the code page setting
 *            
 */

char *osGetExternalCodePage()
{
    /* Initialize ourselves if necessary. */
    sInitializeCodePages(NULL);

    return gExtCodePage;
}

#else

/*
 *  FUNCTION: sInitializeCodePages
 *
 *  PURPOSE:  Initialize the "internal" and "external" code page and 
 *            determine if conversion of code pages is going to need 
 *            to take place.
 *
 *            The external page is set using the following precedence:
 *
 *              1. Passed-in external code page
 *              2. MOCA_EXT_CODEPAGE
 *              3. Default value
 */

static int sInitializeCodePages(char *i_extCodePage)
{
    static int initialized;
    static char *envExtCodePage;

    CPINFO cpinfo;

    /* Get the MOCA_EXT_CODEPAGE environment variable value. */
    if (!initialized)
    {
        envExtCodePage = osGetVar(ENV_PREFIX "EXT_CODEPAGE");
        initialized = 1;
    }

    /* Set the internal code page. */
    gIntCodePage = DEFAULT_CODEPAGE;



    /* Set the external code page. */
    if (i_extCodePage)
    {
        /* Make sure the given code page is numeric. */
        if (strspn(i_extCodePage, "0123456789") != strlen(i_extCodePage))
            return -1;
	gExtCodePage = atoi(i_extCodePage);
    }
    else if (envExtCodePage)
    {
        /* Make sure the given code page is numeric. */
        if (strspn(envExtCodePage, "0123456789") != strlen(envExtCodePage))
            return -1;
	gExtCodePage = atoi(envExtCodePage);
    }
    else
    {
	gExtCodePage = DEFAULT_CODEPAGE;
    }

    /* Validate the code page. */
    if (!GetCPInfo(gExtCodePage, &cpinfo))
        return -1;

    /* Set the convert flag. */
    gWillConvert = (gIntCodePage != gExtCodePage) ? 1 : 0;

    return 0;
}


/* 
 *  FUNCTION: os_ConvertArray
 *
 *  PURPOSE:  Convert the given string.
 *
 *  RETURNS:   0 - All ok.
 *            -1 - An error occurred.
 */

static int os_ConvertArray(int inCodePage,
                           char *inBuf,
                           int outCodePage,
			   char *outBuf,
			   size_t outSize)
{
    int bytes,
        required;

    size_t wideSize;
    wchar_t *wideBuf;

    /* Determine what the max length of the wide-character string would be. */
    wideSize = 2 * (strlen(inBuf) + 1);

    /* Allocate space for the wide-character string. */
    wideBuf = (wchar_t *) malloc(wideSize);
    if (!wideBuf)
    {
        misLogError("malloc: %s (%ld)", osError( ), osErrno( ));
	misLogError("os_ConvertArray: Could not allocate memory");
	return -1;
    }

    /* Convert the given string to a wide-character string. */
    bytes = MultiByteToWideChar(inCodePage, 0, inBuf, -1, wideBuf, wideSize);
    if (!bytes)
    {
        misLogError("MultiByteToWideChar: %s (%ld)", osError( ), osErrno( ));
	misLogError("os_ConvertArray: Could not convert string");
        free(wideBuf);
	return -1;
    }

    /* Determine the number of bytes required to do the whole conversion. */
    required = WideCharToMultiByte(outCodePage, 0, wideBuf, bytes, NULL, 0,
                                   NULL, NULL);
    if (!required)
    {
        free(wideBuf);
        misLogError("WideCharToMultiByte %s (%ld)", osError( ), osErrno( ));
	misLogError("os_ConvertArray: Could not get required bytes");
	return -1;
    }

    outBuf[outSize-1]='\0';
    /* Convert the given string to the new code page. */
    bytes = WideCharToMultiByte(outCodePage, 0, wideBuf, bytes, outBuf, outSize-1,
	                        NULL, NULL);
    if (!bytes && osErrno( ) == ERROR_INSUFFICIENT_BUFFER)
    {
	bytes = strlen(outBuf) + 1;
    }
    else if (!bytes)
    {
        free(wideBuf);
        misLogError("WideCharToMultiByte %s (%ld)", osError( ), osErrno( ));
	misLogError("os_ConvertArray: Could not convert string");
	return -1;
    }

    /* Free up locally allocated memory. */
    free(wideBuf);

    return (required - bytes);
}


/*
 *  FUNCTION: os_ConvertPtr
 *
 *  PURPOSE:  Convert the given string.
 *
 *  RETURNS:  The number of bytes that were converted.
 *            -1 - An error occurred.
 */

static int os_ConvertPtr(int inCodePage,
                         char *inBuf,
                         int outCodePage,
			 char **outBuf)
{
    int bytes,
        wideSize;
    wchar_t *wideBuf;

    /* Determine what the max length of the wide-character string would be. */
    wideSize = 2 * (strlen(inBuf) + 1);

    /* Allocate space for the wide-character string. */
    wideBuf = (wchar_t *) malloc(wideSize);
    if (!wideBuf)
    {
        free(wideBuf);
        misLogError("malloc: %s (%ld)", osError( ), osErrno( ));
	misLogError("os_ConvertArray: Could not allocate memory");
	return -1;
    }

    /* Convert the given string to a wide-character string. */
    wideSize = MultiByteToWideChar(inCodePage, 0, inBuf, -1, wideBuf, wideSize);
    if (!wideSize)
    {
        free(wideBuf);
        misLogError("MultiByteToWideChar: %s (%ld)", osError( ), osErrno( ));
	misLogError("os_ConvertArray: Could not convert string");
	return -1;
    }

    /* Convert the given string to the new code page. */
    bytes = WideCharToMultiByte(outCodePage, 0, wideBuf, wideSize, NULL, 0,
                                NULL, NULL);
    if (!bytes)
    {
        free(wideBuf);
        misLogError("WideCharToMultiByte %s (%ld)", osError( ), osErrno( ));
	misLogError("os_ConvertArray: Could not convert string");
	return -1;
    }

    /* Allocate space for the output buffer. */
    *outBuf = (char *) calloc(bytes, sizeof(char));
    if (!*outBuf)
    {
        free(wideBuf);
        misLogError("calloc: %s (%ld)", osError( ), osErrno( ));
	misLogError("os_ConvertArray: Could not allocate memory");
	return -1;
    }

    /* Convert the given string to the new code page. */
    bytes = WideCharToMultiByte(outCodePage, 0, wideBuf, wideSize, *outBuf, 
                                bytes, NULL, NULL);
    if (!bytes)
    {
        free(wideBuf);
	free(*outBuf);
	*outBuf = NULL;
        misLogError("WideCharToMultiByte %s (%ld)", osError( ), osErrno( ));
	misLogError("os_ConvertArray: Could not convert string");
	return -1;
    }

    /* Free up locally allocated memory. */
    free(wideBuf);

    return bytes;
}


/*
 *  FUNCTION: osToIntCPArray
 *
 *  PURPOSE:  Convert the given string in the given array to the
 *            "internal" code page.  The "exteral" and "internal"
 *            arrays can be pointers to the same array.  If they
 *            are, the conversion will occur in place.
 *
 *  RETURNS:  The number of bytes that weren't converted.
 *            -1 - An error occurred.
 */

size_t osToIntCPArray(char *extStr,
                      char *intStr,
                      size_t intSize,
                      char *extCodePage)
{
    int status;

    /* Initialize ourselves if necessary. */
    status = sInitializeCodePages(extCodePage);
    if (status != eOK)
        return -1;

    /* Don't bother if the "external" string is null. */
    if (!extStr)
    {
        *intStr = '\0';
	return 0;
    }

    /* Don't bother if the "internal" string length is 0. */
    if (!intSize)
	return strlen(extStr) + 1;

    /* Don't bother if there's no need to convert. */
    if (!gWillConvert)
    {
        /* Only copy the string if the pointers are different. */
        if (intStr != extStr)
        {
            /* We want to count the null-terminator. */
            size_t extSize = strlen(extStr) + 1;

            memset(intStr, '\0', intSize);
            strncpy(intStr, extStr, intSize-1);

            return (extSize <= intSize) ? 0 : (extSize - intSize);
        }

        return 0;
    }

    /* Convert the string to the "internal" code page. */
    status = os_ConvertArray(gExtCodePage, extStr, gIntCodePage, intStr, intSize);
    if (status == -1)
    {
        misLogError("osToIntCPArray: Could not convert");
        return -1;
    }

    return 0;
}


/*
 *  FUNCTION: osToExtCPArray
 *
 *  PURPOSE:  Convert the given string in the given array to the
 *            "external" code page.  The "interal" and "external"
 *            arrays can be pointers to the same array.  If they
 *            are, the conversion will occur in place.
 *            
 *
 *  RETURNS:  The number of bytes that weren't converted.
 *            -1 - An error occurred.
 */

size_t osToExtCPArray(char *intStr,
                      char *extStr,
                      size_t extSize,
                      char *extCodePage)
{
    int status;

    /* Initialize ourselves if necessary. */
    status = sInitializeCodePages(extCodePage);
    if (status != eOK)
        return -1;

    /* Don't bother if the "internal" string is null. */
    if (!intStr)
    {
        *extStr = '\0';
	return 0;
    }

    /* Don't bother if the "external" string length is 0. */
    if (!extSize)
	return strlen(intStr) + 1;

    /* Don't bother if there's no need to convert. */
    if (!gWillConvert)
    {
        /* Only copy the string if the pointers are different. */
        if (extStr != intStr)
        {
            /* We want to count the null-terminator. */
            size_t intSize = strlen(intStr) + 1;

            memset(extStr, '\0', extSize);
            strncpy(extStr, intStr, extSize-1);

            return (intSize <= extSize) ? 0 : (intSize - extSize);
        }

        return 0;
    }

    /* Convert the string to the "external" code page. */
    status = os_ConvertArray(gIntCodePage, intStr, gExtCodePage, extStr, extSize);
    if (status == -1)
    {
        misLogError("osToExtCPArray: Could not convert");
        return -1;
    }

    return 0;

}


/*
 *  FUNCTION: osToIntCPPtr
 *
 *  PURPOSE:  Convert the string pointed to by the given "external"
 *            pointer to the "internal" code page, allocating space
 *            for the given "internal" pointer.
 *
 *  RETURNS:  The number of bytes the "internal" pointer points to.
 *            -1 - An error occurred.
 */

size_t osToIntCPPtr(char *extStr, char **intStr, char *extCodePage)
{
    int status;
    size_t bytes;

    /* Initialize ourselves if necessary. */
    status = sInitializeCodePages(extCodePage);
    if (status != eOK)
        return -1;

    /* Point the "internal" pointer to NULL. */
    *intStr = NULL;

    /* Don't bother if the "external" string is null. */
    if (!extStr)
        return 0;

    /* Don't bother if there's no need to convert. */
    if (!gWillConvert)
    {
        /* We want to count the null-terminator. */
        size_t extSize = strlen(extStr) + 1;

        return extSize;
    }

    /* Convert the string to the "internal" code page. */
    bytes = os_ConvertPtr(gExtCodePage, extStr, gIntCodePage, intStr);
    if (bytes == -1)
    {
        misLogError("osToIntCPPtr: Could not convert");
        return -1;
    }

    return bytes;
}


/*
 *  FUNCTION: osToExtCPPtr
 *
 *  PURPOSE:  Convert the string pointed to by the given "internal"
 *            pointer to the "external" code page, allocating space
 *            for the given "external" pointer.
 *
 *  RETURNS:  The number of bytes the "external" pointer points to.
 *            -1 - An error occurred.
 */

size_t osToExtCPPtr(char *intStr, char **extStr, char *extCodePage)
{
    int status;
    size_t bytes;

    /* Initialize ourselves if necessary. */
    status = sInitializeCodePages(extCodePage);
    if (status != eOK)
        return -1;

    /* Point the "external" pointer to NULL. */
    *extStr = NULL;

    /* Don't bother if the "internal" string is null. */
    if (!intStr)
        return 0;

    /* Don't bother if there's no need to convert. */
    if (!gWillConvert)
    {
        /* We want to count the null-terminator. */
        size_t intSize = strlen(intStr) + 1;

        return intSize;
    }

    /* Convert the string to the "external" code page. */
    bytes = os_ConvertPtr(gIntCodePage, intStr, gExtCodePage, extStr);
    if (bytes == -1)
    {
        misLogError("osToExtCPPtr: Could not convert");
        return -1;
    }

    return bytes;
}

/*
 *  FUNCTION: osGetExtCodePage
 *
 *  PURPOSE:  Returns the configured external code pages to be used for 
 *            any code page conversions done.
 *            This is mostly intended for use by functions doing their
 *            own multibytetowidechar conversions for BSTR interopability
 *            (mocasrvobj-nt)
 *
 *  RETURNS:  UINT representing the code page setting
 *            
 */

UINT osGetExternalCodePage(void)
{
    /* Initialize ourselves if necessary. */
    sInitializeCodePages(NULL);

    return gExtCodePage;
}
#endif

/*
 *  FUNCTION: osWillConvertCP
 *
 *  PURPOSE:  Determine if conversion of code pages is going to need to
 *            take place.  If an external code page is not provided one
 *            will be determined using a set of rules.
 *
 *  RETURNS:  1 - Need to convert code pages.
 *            0 - Don't need to convert code pages.
 */

long osWillConvertCP(char *extCodePage)
{
    /* Initialize ourselves if necessary. */
    sInitializeCodePages(extCodePage);

    return gWillConvert;
}

/*
 *  FUNCTION: osIsExtCodePageSet()
 *
 *  PURPOSE:  Initialize the "internal" and "external" code pages and
 *            determine if the external code page variable is in play
 *
 *  RETURNS:  1 - External code page has been set
 *            0 - Not set.  Use System code pages
 */

long osIsExtCodePageSet(void)
{
    /* Initialize ourselves if necessary. */
    sInitializeCodePages(NULL);

    return (gExtCodePage == DEFAULT_CODEPAGE) ? 0 : 1;
}

int osGetFileEncoding(FILE *fp, int stripBOM)
{
    char bomString[5];
    int encoding = ENCODING_ANSI;
    char *p;

    if (fp == NULL) return ENCODING_ANSI;
    
    memset (bomString, 0, sizeof(bomString));
    
    p = fgets(bomString, 3, fp);
    if (p )
    {
        if (bomString[0] == '\0' && bomString[1] == '\0')
        {
            /* This is possibly big endian UTF-32  (00 00 FE FF) 
             * We had to check this first because the first 
             * two bytes will be null */
            
            /* Get the next two bytes */
            p = fgets(bomString, 3, fp);

            /* make sure we got two bytes and check against the UTF32 BOM */
            if (p && strcmp(bomString, BOM_UTF32_BE +2)==0)
            {
                encoding = ENCODING_UTF16 | ENCODING_BIG_ENDIAN;
            }
        }
        else if (strncmp(bomString, BOM_UTF8, 2) == 0)
        {
            /* possible UTF-8 BOM *
             * Need one more byte to confirm */
            p = fgets(bomString, 2, fp);
            if (p && bomString[0] == BOM_UTF8[2])
            {
                encoding = ENCODING_UTF8;
            }
        }
        else if (strcmp(bomString, BOM_UTF16_BE) == 0)
        {
            /* Big Endian UTF-16 */
            encoding = ENCODING_UTF16 | ENCODING_BIG_ENDIAN;
        }
        else if (strcmp(bomString, BOM_UTF16_LE) == 0)
        {
            /* Little Endian UTF-16 */
            encoding = ENCODING_UTF16 | ENCODING_LITTLE_ENDIAN;
        }
        else if (strcmp(bomString, BOM_UTF32_LE) == 0)
        { 
            /* Little Endian UTF32 */
            encoding = ENCODING_UTF32 | ENCODING_LITTLE_ENDIAN;
        }
    }
    if (encoding == ENCODING_ANSI || !stripBOM)
    { 
        /* rewind the file if it didn't have a BOM or we are not
         * stripping the BOM per the caller */
        rewind(fp);
    }

    return encoding;   
}
