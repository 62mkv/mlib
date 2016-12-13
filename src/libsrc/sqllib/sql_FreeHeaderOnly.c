static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: This routine is used by the srvlib to only free the SQL
 *               header structure.  It is passed a mocaDataRes pointer.
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

#include <sqllib.h>

void sql_FreeHeaderOnly(mocaDataRes *res)
{
    long i;

    if (!res)
        return;
     
    if (res->ColName) 
    {
        for (i = 0; i < res->NumOfColumns; i++)
            if (res->ColName[i])
                free(res->ColName[i]);
    }
     
    if (res->ShortDescription) 
    {
        for (i = 0; i < res->NumOfColumns; i++)
            if (res->ShortDescription[i])
                free(res->ShortDescription[i]);
    }

    if (res->LongDescription) 
    {
        for (i = 0; i < res->NumOfColumns; i++)
            if (res->LongDescription[i])
                free(res->LongDescription[i]);
    }
    
    if (res->ColName)
        free(res->ColName);
    if (res->ShortDescription)
        free(res->ShortDescription);
    if (res->LongDescription)
        free(res->LongDescription);
    if (res->DataType)
        free(res->DataType);
    if (res->Nullable)
        free(res->Nullable);
    if (res->DefinedMaxLen)
        free(res->DefinedMaxLen);
    if (res->ActualMaxLen)
        free(res->ActualMaxLen);
    
    free(res);
}
