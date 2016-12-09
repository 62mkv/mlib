static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$ 
 *  $Revision$
 *  $Author$
 *
 *  Description: Get a value from the registry.
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
 *#END************************************************************************/

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <srvlib.h>

LIBEXPORT 
RETURN_STRUCT *mocaGetTraceLevels(void)
{
    int  ii,
	 count;

    char *args,
         **levels;

    char  thisArg[2],
	 *thisLevel;

    RETURN_STRUCT *ret;

    /* Clear out the array so the second element will always be null. */
    memset(thisArg, 0, sizeof(thisArg));

    /* Get the number of trace levels. */
    count = misGetTraceLevelsString(&levels, &args);

    /* Initialize the return structure. */
    ret = srvResultsInit(eOK,
                         "trc_lvl", COMTYP_CHAR, 0,
                         "trc_arg", COMTYP_CHAR, 0,
                         NULL);

    /* Cycle through each trace level. */
    for (ii = 0; ii < count; ii++)
    {
	thisLevel  = levels[ii];
	thisArg[0] = args[ii];

        srvResultsAdd(ret, thisLevel, thisArg);
    }

    return ret;
}
