static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: This routine frees data associated with mocaDataRow.
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

#include <common.h>
#include <sqllib.h>

void sql_FreeObjectRef(mocaObjectRef *ref)
{
    if (ref)
    {
        ref->RefCount--;
        if (!ref->RefCount)
        {
            ref->destructor(ref->data);
            free(ref);
        }
    }
    return;
}

mocaObjectRef *sql_ObjectRef(void *obj, void (*destructor)(void *))
{
    mocaObjectRef *ref = calloc(1, sizeof(mocaObjectRef));
    ref->RefCount = 0;
    ref->data = obj;
    ref->destructor = destructor;
    return ref;
}

void *sql_GetObjectRef(mocaObjectRef *ref)
{
    if (!ref)
        return NULL;
    else
        return ref->data;
}
