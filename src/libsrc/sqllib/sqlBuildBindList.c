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
#include <stdarg.h>

#include <mislib.h>
#include <sqllib.h>

int sqlBuildBindListFromArgs(mocaBindList **Head, va_list args)
{
    mocaBindList *bp;
    char *name;
    int numbinds;
    char *ptr;

    mocaDataRes *tmpRes;
    mocaObjectRef *tmpObj;

    numbinds = 0;

    while ((name = va_arg(args, char *)))
    {
	bp = malloc(sizeof (mocaBindList));
	memset(bp, 0, sizeof (mocaBindList));

	strncpy(bp->name, name, sizeof (bp->name) - 1);

        bp->dtype = (char) va_arg(args, int);
	bp->size = va_arg(args, long);
	switch(bp->dtype)
	{
	case COMTYP_LONG:
	case COMTYP_INT:
	case COMTYP_BOOLEAN:
	    bp->data = malloc(sizeof(long));
	    *(long *)bp->data = va_arg(args, long);
	    bp->size = sizeof(long);
	    break;
	case COMTYP_FLOAT:
	    bp->data = malloc(sizeof(double));
	    *(double *)bp->data = va_arg(args, double);
	    bp->size = sizeof(double);
	    break;
	case COMTYP_DATTIM:
	case COMTYP_STRING:
	    bp->data = malloc(bp->size+1);
	    ptr = va_arg(args, char *);
	    strncpy((char *)bp->data, ptr?ptr:"", bp->size);
	    ((char *)bp->data)[bp->size] = '\0';
	    bp->size++;
	    break;
	case COMTYP_CHARPTR:
	    bp->data = va_arg(args, void *);
	    break;
	case COMTYP_LONGPTR:
	    bp->data = va_arg(args, void *);
	    bp->size = sizeof(long);
	    break;
	case COMTYP_FLOATPTR:
	    bp->data = va_arg(args, void *);
	    bp->size = sizeof(double);
	    break;
	case COMTYP_RESULTS:
            tmpRes = va_arg(args, mocaDataRes *);
            if (tmpRes != NULL) tmpRes->RefCount++;
	    bp->data = malloc(sizeof(mocaDataRes *));
            *((mocaDataRes **)bp->data) = tmpRes;
	    bp->size = sizeof tmpRes;
	    break;
	case COMTYP_JAVAOBJ:
            tmpObj = va_arg(args, mocaObjectRef *);
            if (tmpObj != NULL) tmpObj->RefCount++;
	    bp->data = malloc(sizeof(mocaObjectRef *));
            *((mocaObjectRef **)bp->data) = tmpObj;
	    bp->size = sizeof tmpObj;
	    break;
	}

	bp->nullind = (short) va_arg(args, int);

	bp->next = *Head;
	*Head = bp;


        numbinds++;
    }

    return(numbinds);
}

int sqlBuildBindList(mocaBindList **Head, ...)
{
    int numbinds;
    va_list args;

    va_start(args, Head);
    numbinds = sqlBuildBindListFromArgs(Head, args);
    va_end(args);

    return(numbinds);
}
