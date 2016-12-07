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

char *misStrsep(char **stringp, char *delim)
{
    register char *p, *d;
    char *save;

    if (*stringp == NULL)
	return NULL;

    for (p=*stringp;*p;p++)
    {
	for (d=delim;*d;d++)
	{
	   if (*d == *p)
	   {
	       save = *stringp;
	       *p = '\0';
	       *stringp = p+1;
	       return save;
	   }
	}
    }

    save = *stringp;
    *stringp = NULL;
    return save;
}
