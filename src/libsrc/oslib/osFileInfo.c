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

#ifdef HAVE_SYS_STAT_H
# include <sys/stat.h>
#endif

#ifdef HAVE_SYS_TYPES_H
# include <sys/types.h>
#endif

#include <mocaerr.h>
#include "osprivate.h"

#ifdef UNIX
# define FORMAT      "%Y-%m-%d %T"
#else
# define DATE_FORMAT "yyyy'-'MM'-'dd"
# define TIME_FORMAT "HH':'mm':'ss"
#endif

#ifdef UNIX /* { */

/*
 *  FUNCTION: osFileInfo
 *
 *  PURPOSE:  Get the file type of the given pathname. 
 *
 *  RETURNS:  eOK
 *            eERROR
 */

long osFileInfo(char *filename, char *filetype)
{
    int status;
    struct stat stat_buf;

    /* Get the file stat structure for this file. */
    if ((status = stat(filename, &stat_buf)) < 0)
    {
	*filetype = OS_FILETYPE_NULL;
	return eERROR;
    }

    /* Map to the correct file type. */
    if (stat_buf.st_mode & S_IFDIR)
	*filetype = OS_FILETYPE_DIR;
    else
	*filetype = OS_FILETYPE_FILE;

    return eOK;
}

/*
 *  FUNCTION: osFileSize
 *
 *  PURPOSE:  Get the size of the given pathname. 
 *
 *  RETURNS:  eOK
 *            eERROR
 */

long osFileSize(char *filename, long *size)
{
    int status;
    struct stat stat_buf;

    /* Get the file stat structure for this file. */
    if ((status = stat(filename, &stat_buf)) < 0)
    {
	*size = -1;
	return eERROR;
    }

    /* Point the caller to the file size. */
    *size = stat_buf.st_size;

    return eOK;
}

/*
 *  FUNCTION: osFileCreated
 *
 *  PURPOSE:  Get the creation time of the given pathname. 
 *
 *  RETURNS:  eOK
 *            eERROR
 */

long osFileCreated(char *filename, char **datetime)
{
    int status;

    static char buffer[100];

    struct stat  stat_buf;
    struct tm   *time_ptr;

    /* Get the file stat structure for this file. */
    if ((status = stat(filename, &stat_buf)) < 0)
    {
	*datetime = NULL;
	return eERROR;
    }

    /* Convert the time structure and format it. */
    time_ptr = localtime(&(stat_buf.st_ctime));
    strftime(buffer, sizeof buffer, FORMAT, time_ptr);

    /* Point the caller to the date string. */
    *datetime = buffer;

    return eOK;
}

/*
 *  FUNCTION: osFileAccessed
 *
 *  PURPOSE:  Get the accessed time of the given pathname. 
 *
 *  RETURNS:  eOK
 *            eERROR
 */

long osFileAccessed(char *filename, char **datetime)
{
    int status;

    static char buffer[100];

    struct stat  stat_buf;
    struct tm   *time_ptr;

    /* Get the file stat structure for this file. */
    if ((status = stat(filename, &stat_buf)) < 0)
    {
	*datetime = NULL;
	return eERROR;
    }

    /* Convert the time structure and format it. */
    time_ptr = localtime(&(stat_buf.st_atime));
    strftime(buffer, sizeof buffer, FORMAT, time_ptr);

    /* Point the caller to the date string. */
    *datetime = buffer;

    return eOK;
}

/*
 *  FUNCTION: osFileModified
 *
 *  PURPOSE:  Get the modified time of the given pathname. 
 *
 *  RETURNS:  eOK
 *            eERROR
 */

long osFileModified(char *filename, char **datetime)
{
    int status;

    static char buffer[100];

    struct stat  stat_buf;
    struct tm   *time_ptr;

    /* Get the file stat structure for this file. */
    if ((status = stat(filename, &stat_buf)) < 0)
    {
	*datetime = NULL;
	return eERROR;
    }

    /* Convert the time structure and format it. */
    time_ptr = localtime(&(stat_buf.st_mtime));
    strftime(buffer, sizeof buffer, FORMAT, time_ptr);

    /* Point the caller to the date string. */
    *datetime = buffer;

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

/*
 *  FUNCTION: osFileInfo
 *
 *  PURPOSE:  Get the file type of the given pathname. 
 *
 *  RETURNS:  eOK
 *            eERROR
 */

long osFileInfo(char *filename, char *filetype)
{
    wchar_t          *wfilename;
    HANDLE           hFindFile;
    WIN32_FIND_DATAW FindData;

    /* Convert the given pathname to UTF-16. */
    wfilename = sConvertToUTF16(filename);
    if (!wfilename)
    {
        *filetype = OS_FILETYPE_NULL;
        return eERROR;
    }

    /* Get the file data handle for this file. */
    hFindFile = FindFirstFileW(wfilename, &FindData);
    if (hFindFile == INVALID_HANDLE_VALUE)
    {
	*filetype = OS_FILETYPE_NULL;
        free(wfilename);
        return eERROR;
    }

    /* Map to the correct file type. */
    if (FindData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
	*filetype = OS_FILETYPE_DIR;
    else
	*filetype = OS_FILETYPE_FILE;

    /* Close the file data handle. */
    FindClose(hFindFile);

    free(wfilename);

    return eOK;
}

/*
 *  FUNCTION: osFileSize
 *
 *  PURPOSE:  Get the size of the given pathname. 
 *
 *  RETURNS:  eOK
 *            eERROR
 */

long osFileSize(char *filename, long *size)
{
    wchar_t          *wfilename;
    HANDLE           hFindFile;
    WIN32_FIND_DATAW FindData;

    /* Convert the given pathname to UTF-16. */
    wfilename = sConvertToUTF16(filename);
    if (!wfilename)
    {
	*size = -1;
        return eERROR;
    }

    /* Get the file data handle for this file. */
    hFindFile = FindFirstFileW(wfilename, &FindData);
    if (hFindFile == INVALID_HANDLE_VALUE)
    {
	*size = -1;
        free(wfilename);
        return eERROR;
    }

    /* Point the caller to the file size. */
    *size = FindData.nFileSizeLow;

    /* Close the file data handle. */
    FindClose(hFindFile);

    free(wfilename);

    return eOK;
}

/*
 *  FUNCTION: osFileCreated
 *
 *  PURPOSE:  Get the creation time of the given pathname. 
 *
 *  RETURNS:  eOK
 *            eERROR
 */

long osFileCreated(char *filename, char **datetime)
{
    wchar_t          *wfilename;
    HANDLE           hFindFile;
    WIN32_FIND_DATAW FindData;
    FILETIME         FileTime;
    SYSTEMTIME       SystemTime;

    static char buffer[100];

    /* Initialize the date string. */
    memset(buffer, 0, sizeof buffer);

    /* Convert the given pathname to UTF-16. */
    wfilename = sConvertToUTF16(filename);
    if (!wfilename)
    {
	*datetime = NULL;
        return eERROR;
    }

    /* Get the file data handle for this file. */
    hFindFile = FindFirstFileW(wfilename, &FindData);
    if (hFindFile == INVALID_HANDLE_VALUE)
    {
	*datetime = NULL;
        free(wfilename);
        return eERROR;
    }

    /* Get the file creation time. */
    FileTime = FindData.ftCreationTime;

    /* Convert the file time to system time. */
    FileTimeToLocalFileTime(&FileTime, &FileTime);
    FileTimeToSystemTime(&FileTime, &SystemTime);

    /* Get a copy of the formatted date. */
    GetDateFormat(LOCALE_USER_DEFAULT, 0, &SystemTime, DATE_FORMAT, 
	          buffer, strlen(DATE_FORMAT));

    /* Copy in a space. */
    strcat(buffer, " ");

    /* Get a copy of the formatted time. */
    GetTimeFormat(LOCALE_USER_DEFAULT, 0, &SystemTime, TIME_FORMAT, 
	          &buffer[11], strlen(TIME_FORMAT));

    /* Point the caller to the date string. */
    *datetime = buffer;

    /* Close the file data handle. */
    FindClose(hFindFile);

    free(wfilename);

    return eOK;
}

/*
 *  FUNCTION: osFileModified
 *
 *  PURPOSE:  Get the modified time of the given pathname. 
 *
 *  RETURNS:  eOK
 *            eERROR
 */

long osFileModified(char *filename, char **datetime)
{
    wchar_t          *wfilename;
    HANDLE           hFindFile;
    WIN32_FIND_DATAW FindData;
    FILETIME         FileTime;
    SYSTEMTIME       SystemTime;

    static char buffer[100];

    /* Initialize the date string. */
    memset(buffer, 0, sizeof buffer);

    /* Convert the given pathname to UTF-16. */
    wfilename = sConvertToUTF16(filename);
    if (!wfilename)
    {
	*datetime = NULL;
        return eERROR;
    }

    /* Get the file data handle for this file. */
    hFindFile = FindFirstFileW(wfilename, &FindData);
    if (hFindFile == INVALID_HANDLE_VALUE)
    {
	*datetime = NULL;
        free(wfilename);
        return eERROR;
    }

    /* Get the file modified time. */
    FileTime = FindData.ftLastWriteTime;

    /* Convert the file time to system time. */
    FileTimeToLocalFileTime(&FileTime, &FileTime);
    FileTimeToSystemTime(&FileTime, &SystemTime);

    /* Get a copy of the formatted date. */
    GetDateFormat(LOCALE_USER_DEFAULT, 0, &SystemTime, DATE_FORMAT, 
	          buffer, strlen(DATE_FORMAT));

    /* Copy in a space. */
    strcat(buffer, " ");

    /* Get a copy of the formatted time. */
    GetTimeFormat(LOCALE_USER_DEFAULT, 0, &SystemTime, TIME_FORMAT, 
	          &buffer[11], strlen(TIME_FORMAT));

    /* Point the caller to the date string. */
    *datetime = buffer;

    /* Close the file data handle. */
    FindClose(hFindFile);

    free(wfilename);

    return eOK;
}

/*
 *  FUNCTION: osFileAccessed
 *
 *  PURPOSE:  Get the accessed time of the given pathname. 
 *
 *  RETURNS:  eOK
 *            eERROR
 */

long osFileAccessed(char *filename, char **datetime)
{
    wchar_t          *wfilename;
    HANDLE           hFindFile;
    WIN32_FIND_DATAW FindData;
    FILETIME         FileTime;
    SYSTEMTIME       SystemTime;

    static char buffer[100];

    /* Initialize the date string. */
    memset(buffer, 0, sizeof buffer);

    /* Convert the given pathname to UTF-16. */
    wfilename = sConvertToUTF16(filename);
    if (!wfilename)
    {
	*datetime = NULL;
        return eERROR;
    }

    /* Get the file data handle for this file. */
    hFindFile = FindFirstFileW(wfilename, &FindData);
    if (hFindFile == INVALID_HANDLE_VALUE)
    {
	*datetime = NULL;
        free(wfilename);
        return eERROR;
    }

    /* Get the file accessed time. */
    FileTime = FindData.ftLastAccessTime;

    /* Convert the file time to system time. */
    FileTimeToLocalFileTime(&FileTime, &FileTime);
    FileTimeToSystemTime(&FileTime, &SystemTime);

    /* Get a copy of the formatted date. */
    GetDateFormat(LOCALE_USER_DEFAULT, 0, &SystemTime, DATE_FORMAT, 
	          buffer, strlen(DATE_FORMAT));

    /* Copy in a space. */
    strcat(buffer, " ");

    /* Get a copy of the formatted time. */
    GetTimeFormat(LOCALE_USER_DEFAULT, 0, &SystemTime, TIME_FORMAT, 
	          &buffer[11], strlen(TIME_FORMAT));

    /* Point the caller to the date string. */
    *datetime = buffer;

    /* Close the file data handle. */
    FindClose(hFindFile);

    free(wfilename);

    return eOK;
}

#endif
