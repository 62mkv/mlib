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

LIBEXPORT RETURN_STRUCT *testGetNeededElementPointer(char *arg)
{
    void *value = NULL;
    RETURN_STRUCT *results = NULL;
    char dtype;

    srvGetNeededElement(arg, arg, &dtype, &value);
    if (value != NULL && dtype == COMTYP_GENERIC)
    {
        void *ptr = *((void **)value);
        results = srvResults(eOK, "value", COMTYP_GENERIC, sizeof(void *), ptr, NULL);
    }
    else
    {
        results = srvResults(eOK, "value", COMTYP_GENERIC, sizeof(void *), NULL, NULL);
    }

    return results;
}
