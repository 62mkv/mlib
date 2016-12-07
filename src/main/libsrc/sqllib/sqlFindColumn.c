static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: This routine finds a named column in a result set.
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
/*
 *  Parameters:
 *         res - pointer to a result set (mocaDataRes)
 *         name - name of the column to retrieve value for (char *)
 *  Returns:
 *         column number if found, otherwise -1;
 */

#include <moca.h>

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <mislib.h>
#include <sqllib.h>

long MOCAEXPORT sqlFindColumn(mocaDataRes *res, char *name)
{
    long col;
    char nam[500];
    unsigned long HashValue;

    if (!res || !name)
	return -1;
    
    strcpy(nam, name);
    misTrim(nam);

    HashValue = misCiHash(nam);
	
    for (col = 0; col < res->NumOfColumns; col++) 
    {
        if (HashValue == res->HashValue[col] && 
	    misCiStrcmp(res->ColName[col], nam) == 0)
        {
	    return col;
        }
    }

    return -1;
}
