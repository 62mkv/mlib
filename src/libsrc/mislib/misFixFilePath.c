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

#include <string.h>
#include <stdio.h>
#include <stdlib.h>

/*
 * This function fixes a file path and returns the fixed path.  The 
 * passed in buffer is modified.
 *
 * Fixing implies:
 *
 * -   Wrong path separator is replaced with the correct one for OS
 *     e.g. on NT / changed to \
 * -   Consecutive path separators are replaced with one.  
 *     e.g. // changed to \, or /\ changed to \
 *
 * This comes in handy when full paths are constructed programmatically
 * using env vars etc. and it is not known if the src variable already 
 * has a trailing path separator or not or if it does have one what kind
 * is it ( / or \ ).
 */

char *misFixFilePath(char *io_path)
{
    char *src, 
         *dst,
	 *start;

    /* Don't bother if this is just an empty pathname. */
    if (!*io_path)
	return io_path;

    /* 
     * Fix incorrect path separators.
     */
    for (src = io_path; *src != '\0'; src++)
    {
	if ((*src == '/') || (*src == '\\'))
	    *src = PATH_SEPARATOR;
    }

    /*
     * Remove duplicate path separators.
     */

    /* 
     * We actually start one character past the first.  This insures
     * that we don't mess up an UNC pathnames, which are of the form
     * "\\server\my\path\name".
     */
    start = io_path + sizeof(char);

    /* 
     * Remove extra path separators.
     *
     * Cycle through the pathname.  We keep two pointers here, one 
     * to a source location in the string and one to a destination
     * location in the string.  The destination location is actually
     * skipped/incremented whenever we hit consecutive path separators.
     */
    for (src = start, dst = start; *src != '\0'; )
    {
	if ((*src == '/') && (*(src + sizeof(char)) == '/'))
	{
	    src++;
	}
	else if (*src == '\\' && (*(src + sizeof(char)) == '\\'))
	{
	    src++;
	}
	else
	{
	    *dst++ = *src++;
	}
    }

    /* Terminate the modified string. */
    *dst = '\0';

    return io_path;
}
