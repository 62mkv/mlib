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

#include <mislib.h>
#include <sqllib.h>

void sqlFreeBindList(mocaBindList *Head)
{
    mocaBindList *bp, *bpnext;

    for (bp = Head; bp; bp = bpnext)
    {
	switch(bp->dtype)
	{
	case COMTYP_LONG:
	case COMTYP_INT:
	case COMTYP_BOOLEAN:
	case COMTYP_FLOAT:
	case COMTYP_STRING:
	case COMTYP_DATTIM:
	    free(bp->data);
	    break;
        case COMTYP_RESULTS:
            if (bp->data)
            {
                sqlFreeResults(*(mocaDataRes **)bp->data);
                free(bp->data);
            }
            break;
        case COMTYP_JAVAOBJ:
            if (bp->data)
            {
                sql_FreeObjectRef(*(mocaObjectRef **)bp->data);
                free(bp->data);
            }
            break;
	}
	bpnext = bp->next;
	free(bp);
    }

    return;
}
