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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <oslib.h>
#include <srvlib.h>

static void *ptr = NULL;
static int registered = 0;

static void freeConvertedPointer(void)
{
    if (ptr)
        free(ptr);
}

LIBEXPORT 
RETURN_STRUCT *testConvertToBinaryPointer(char * value_i)
{
    RETURN_STRUCT *results = NULL;
    long status = eOK;
    int size = 0;

    size = strlen(value_i);

    if (ptr)
    {
        free(ptr);
	ptr = NULL;
    }
    if (!registered)
    {
        registered = 1;
	osAtexit(freeConvertedPointer);
    }
    ptr = malloc(size + 1);

    sprintf(ptr, "%s", value_i);
    
    results = srvResults(eOK, "data_ptr", COMTYP_GENERIC, size, ptr,
                              "data_len", COMTYP_INT, sizeof(size), size,
                              NULL); 

    return results;
}
