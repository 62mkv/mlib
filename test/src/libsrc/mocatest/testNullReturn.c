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
 *#END************************************************************************/

#include <moca.h>

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <common.h>
#include <mocaerr.h>
#include <srvlib.h>
#include <sqllib.h>
#include <mislib.h>

LIBEXPORT RETURN_STRUCT *testNullReturn(char *arg)
{
    RETURN_STRUCT *results = NULL;
    mocaDataRes *res = NULL;
    mocaDataRow *row = NULL;
    char *value = NULL;
    char *copy = NULL;

    srvInitiateCommandFormat(&results, "get os var where variable = '%s'", arg);

    res = results->ReturnedData;
    row = sqlGetRow(res);

    value = sqlGetString(res, row, "value");

    if (value)
    {
        if (!strcmp("", value))
        {
            srvFreeMemory(SRVRET_STRUCT, results);
	    
            return srvResults(eOK, "value", COMTYP_CHAR, 100, "", NULL);
        }
        else
        {
            misDynStrcpy(&copy, value);

            srvFreeMemory(SRVRET_STRUCT, results);
	    
            return srvResults(eOK, "value", COMTYP_CHAR, strlen(copy), copy, NULL);
        }
    }
    else
    {
        return results;
    }
}
