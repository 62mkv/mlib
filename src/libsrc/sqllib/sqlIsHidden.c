static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: This routine returns TRUE if a specified column is hidden.
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

#include <sqllib.h>

long MOCAEXPORT sqlIsHidden(mocaDataRes *res, mocaDataRow *row, char *name)
{
    long col;

    /* Get the column number. */
    col = sqlFindColumn(res, name);

    return sqlIsHiddenByPos(res, row, col);
}

long MOCAEXPORT sqlIsHiddenByPos(mocaDataRes *res, 
			         mocaDataRow *row, 
			         long col)
{
    /* Validate the column number. */
    if (col < 0 || col >= res->NumOfColumns)
        return -1;

    return res->Hidden;
}
