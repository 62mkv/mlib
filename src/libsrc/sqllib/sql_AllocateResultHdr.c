static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Allocates a result header.
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
/*
 *  Parameters:
 *         res - pointer to a result set (mocaDataRes)
 *  Returns:
 *         none
 */

#include <moca.h>

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <mislib.h>
#include <sqllib.h>

mocaDataRes *sql_AllocateResultHdr(long NumColumns)
{
    mocaDataRes *res;

    res = (mocaDataRes *) calloc(1, sizeof(mocaDataRes));
    if (!res)
        return(NULL);
        
    res->Hidden       = 0;
    res->NumOfRows    = 0;
    res->NumOfColumns = NumColumns;

    if (NumColumns > 0) 
    {
        res->DataType = (char *) calloc(NumColumns+1, sizeof(char));
        if (!res->DataType) 
	{
            free(res);
            return(NULL);
        }
        
        res->ColName = (char **) calloc(NumColumns, sizeof(char *));
        if (!res->ColName && NumColumns) 
	{
            free(res->DataType);
            free(res);
            return(NULL);
        }

        res->ShortDescription = (char **) calloc(NumColumns, sizeof(char *));
        if (!res->ShortDescription && NumColumns) 
	{
            free(res->ColName);
            free(res->DataType);
            free(res);
            return(NULL);
        }

        res->LongDescription = (char **) calloc(NumColumns, sizeof(char *));
        if (!res->LongDescription && NumColumns) 
	{
            free(res->ShortDescription);
            free(res->ColName);
            free(res->DataType);
            free(res);
            return(NULL);
        }
        
        res->Nullable = (long *) calloc(NumColumns, sizeof(long));
        if (!res->Nullable && NumColumns) 
	{
            free(res->LongDescription);
            free(res->ShortDescription);
            free(res->ColName);
            free(res->DataType);
            free(res);
            return(NULL);
        }
        
        res->ActualMaxLen = (long *) calloc(NumColumns, sizeof(long));
        if (!res->ActualMaxLen && NumColumns) 
	{
            free(res->Nullable);
            free(res->LongDescription);
            free(res->ShortDescription);
            free(res->ColName);
            free(res->DataType);
            free(res);
            return(NULL);
        }
        res->DefinedMaxLen = (long *) calloc(NumColumns, sizeof(long));
        if (!res->DefinedMaxLen && NumColumns) 
	{
            free(res->Nullable);
            free(res->ActualMaxLen);
            free(res->LongDescription);
            free(res->ShortDescription);
            free(res->ColName);
            free(res->DataType);
            free(res);
            return(NULL);
        }

        res->HashValue = (unsigned long *) calloc(NumColumns, sizeof(unsigned long));
        if (!res->HashValue && NumColumns) 
	{
            free(res->DefinedMaxLen);
            free(res->ActualMaxLen);
            free(res->Nullable);
            free(res->LongDescription);
            free(res->ShortDescription);
            free(res->ColName);
            free(res->DataType);
            free(res);
            return(NULL);
        }
    }

    return(res);
}
