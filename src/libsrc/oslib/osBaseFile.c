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


char *osBaseFile(char *Filename)
{
    char           *ptr;

    ptr = strrchr(Filename, PATH_SEPARATOR);
    if (!ptr)		/* No PATH_SEPARATOR found */
    {
        ptr = Filename; 
    }
    else			/* We don't want that PATH_SEPARATOR either */
    {
#ifdef UNIX
        /* 
	 * Unix only: in the event the path ended with one or more 
	 * PATH_SEPARATORs, get back to the previous separator.
	 */

	if (ptr[1] == '\0')
	{
	    /* First get past this or these separators */
	    while ((*ptr == PATH_SEPARATOR) && (ptr != Filename))
	        ptr--;

            /* Second, find the previous separator */
	    while ((ptr != Filename) && (*(ptr-1) != PATH_SEPARATOR)) 
	        ptr--;

	    /*
             * ptr right now will either be at the beginning of Filename
	     * or one position after the previous separator.
	     */
	}
	else
	{
            ptr++;
        }
#else
	ptr++;
#endif
    }

    return (ptr);
}


