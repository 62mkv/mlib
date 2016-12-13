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

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>


/*
 * getopt - just like the "standard" Unix one. We have our own set of
 * globals, though, so we don't interfere with the various unix ones.
 */
int  osOptind = 1;
int  osOpterr = 1;
int  osOptopt;
char *osOptarg;

int osGetopt(int argc, char *argv[], char *opts)
{
    static int sp = 1;
    register char *cp;

    if (sp == 1)
    {
	if (osOptind >= argc || argv[osOptind][0] != '-' || !argv[osOptind][1])
	{
	    return EOF;
        }
	else if (!strcmp(argv[osOptind], "--"))
	{
	    osOptind++;
	    return EOF;
	}
    }

    osOptopt = argv[osOptind][sp];
    if (osOptopt == ':' || !(cp = strchr(opts, osOptopt)))
    {
	if (osOpterr && *opts != ':')
	    fprintf(stderr, "%s: illegal option -- %c\n", argv[0], osOptopt);

	if (!argv[osOptind][++sp])
	{
	    osOptind++;
	    sp = 1;
	}
	return '?';
    }
    if (*++cp == ':')
    {
	if (argv[osOptind][sp + 1])
	{
	    osOptarg = &argv[osOptind++][sp + 1];
	}
	else if (++osOptind >= argc)
	{
	    sp = 1;
	    if (':' == *opts)
		return ':';
	    if (osOpterr)
		fprintf(stderr, "%s: option requires an argument -- %c\n",
			argv[0], osOptopt);
	    return '?';
	}
	else
	{
	    osOptarg = argv[osOptind++];
	}
	sp = 1;
    }
    else
    {
	if (!argv[osOptind][++sp])
	{
	    sp = 1;
	    osOptind++;
	}
	osOptarg = 0;
    }
    return osOptopt;
}
