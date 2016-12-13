static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Determine if the given column name is a pseudo column.
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
#include <string.h>
#include <stdlib.h>

#include <mocagendef.h>
#include <sqllib.h>

/*
 * List of know pseudo-columns.
 */

static struct
{
    char *name;
    int   len;
}  pseudo_columns[] =
{
    { "currval", 7 },
    { "nextval", 7 },
    { "level",   5 },
    { "rowid",   5 },
    { "rownum",  6 },
    { "user",    4 },
    { "sysdate", 7 },
    { "null",    4 },
    { "uid",     3 },
    { NULL,      0 }
};


int MOCAEXPORT sqlIsPseudoCol(char *name)
{
    int ii,
        len;

    /* Set the length of the passed in column name. */
    len = strlen(name);

    /* Check it against our list of known pseudo columns. */
    for (ii=0; pseudo_columns[ii].name != NULL; ii++)
    {
        if ((len == pseudo_columns[ii].len) &&
            (misCiStrcmp( (char*)name, pseudo_columns[ii].name) == 0))
        {
            return MOCA_TRUE;
        }
    }

    return MOCA_FALSE;
}
