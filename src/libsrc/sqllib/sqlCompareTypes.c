static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions used by the server and server applications
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

#include <moca.h>

#include <sqllib.h>

/* 
 * Compare two results, allowing for logically identical types
 * to be considered equivalent
 */

int sqlCompareTypes(char *t1, char *t2)
{
    while (*t1 && *t2)
    {
        if (*t1 != *t2 &&
	    !(*t1 == COMTYP_LONG && *t2 == COMTYP_INT) &&
	    !(*t1 == COMTYP_INT && *t2 == COMTYP_LONG))
	{
	    return 0;
	}

	t1++;
	t2++;
    }

    return 1;
}
