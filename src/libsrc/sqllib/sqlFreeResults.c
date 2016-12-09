static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: This routine frees data associated with mocaDataRes.
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

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <common.h>
#include <sqllib.h>

void MOCAEXPORT sqlFreeResults(mocaDataRes * res)
{
    mocaDataRow *next, *row;
    long i;

    if (!res)
	return;

    if (res->RefCount)
    {
	res->RefCount--;
	return;
    }

    for (row = res->Data; row;)
    {
	next = row->NextRow;
	sql_FreeRow(res, row);
	row = next;
    }

    for (i = 0; i < res->NumOfColumns; i++)
    {
	if (res->ColName && res->ColName[i])
	    free(res->ColName[i]);
	if (res->ShortDescription && res->ShortDescription[i])
	    free(res->ShortDescription[i]);
	if (res->LongDescription && res->LongDescription[i])
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
    if (res->Message)
	free(res->Message);
    if (res->DefinedMaxLen)
	free(res->DefinedMaxLen);
    if (res->ActualMaxLen)
	free(res->ActualMaxLen);
    if (res->Nullable)
	free(res->Nullable);
    if (res->HashValue)
	free(res->HashValue);

    free(res);
}
