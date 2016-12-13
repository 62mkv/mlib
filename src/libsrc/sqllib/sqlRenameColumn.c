static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Rename the given column.
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

#include <mocaerr.h>
#include <mislib.h>
#include <sqllib.h>

long MOCAEXPORT sqlRenameColumn(mocaDataRes *res, char *oldName, char *newName)
{
    long number;

    /* Validate the result set. */
    if (! res)
        return eINVALID_ARGS;

    /* Validate the old column name. */
    if ( misTrimIsNull(oldName, 1024))
        return eINVALID_ARGS;

    /* Validate the new column name. */
    if (misTrimIsNull(newName, 1024))
        return eINVALID_ARGS;

    /* Get the column number of this column. */
    if ((number = sqlFindColumn(res, oldName)) < 0)
        return eDB_NO_ROWS_AFFECTED;

    return sqlRenameColumnByPos(res, number, newName);
}

long MOCAEXPORT sqlRenameColumnByPos(mocaDataRes *res,
                                 long number,
                                 char *newName)
{
    long length;

    /* Validate the result set. */
    if (! res)
        return eINVALID_ARGS;

    /* Validate the column number. */
    if (number < 0 || number >= res->NumOfColumns)
        return eINVALID_ARGS;

    /* Validate the new column name. */
    if ( misTrimIsNull(newName, 2014))
        return eINVALID_ARGS;

    /* Get the length of the new column name. */
    length = strlen(newName);

    /* Free the memory associated with the existing column name. */
    free(res->ColName[number]);

    /* Allocate space for the new column name. */
    res->ColName[number] = (char *) calloc(1, length+1);
    if (! res->ColName[number])
        return eNO_MEMORY;

    /* Set the new column name. */
    misTrimncpy(res->ColName[number], newName, length, length+1);

    /* Set the hash value for the new column name. */
    res->HashValue[number] = misCiHash(res->ColName[number]);

    return eOK;
}
