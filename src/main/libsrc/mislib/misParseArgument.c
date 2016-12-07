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
 *  Copyright (c) 2010
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
#include <ctype.h>

char *misParseArgument(char **line)
{
    char *c   = NULL,
	 *arg = NULL;
	 
    char found   = 0,
	 inquote = 0;

    if (*line == NULL)
        return NULL;

    for (c = *line; *c; c++)
    {
        /* 
	 * If we're not in a quoted string and found some whitespace 
	 * we either skip over it if we haven't found an argument yet
	 * or we're at the end of the current argument and can return it.
	 */
	if (!inquote && isspace(*c))
	{
	    *line = c + 1;

	    /* If we haven't found an argument yet just continue on. */
	    if (!found)
		continue;

	    /* We need to handle quoted empty arguments. */
	    if (!arg)
	        misDynCharcat(&arg, '\0');

	    return arg;
	}

	/* Handle a single or double qoute. */
	else if (!inquote && (*c == '\'' || *c == '"')) 
	{
	    found = 1;
	    inquote = *c;
	}

	/* Handle finding an ending quote or single quote. */
	else if (*c == inquote)
	{
	    inquote = 0;
	}

	/* Handle a real character from the argument. */
	else
	{
	    found = 1;
	    misDynCharcat(&arg, *c);
	}
    }

    /* If we're still in a quote we've got unmatches quotes. */
    if (inquote)
    {
	free(arg);
        *line = NULL;
	return NULL;
    }

    *line = c;

    return arg;
}
