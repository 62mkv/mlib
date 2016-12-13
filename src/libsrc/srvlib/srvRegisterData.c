static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions used by the server and server applications
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

#include <mislib.h>
#include <mocaerr.h>
#include <mocagendef.h>
#include "srvprivate.h"

typedef struct dest_list
{
    void *pointer;
    void (*destructor)(void *);
    struct dest_list *next;
} DEST_LIST;

static DEST_LIST *Head;

void srvRegisterData(void *ptr, void (*destructor)(void *))
{
    if (ptr)
    {
	DEST_LIST *p;

	p=malloc(sizeof *p);
	p->pointer = ptr;
	p->destructor = destructor;
	p->next = Head;
	Head = p;
    }
}

void srvUnregisterData(void *ptr)
{
    DEST_LIST *p, *p_next, *p_prev;

    for(p=Head;p;p=p_next)
    {
	if (p->pointer == ptr)
	{
	    if (p == Head)
		Head = p->next;
            else
                p_prev->next = p->next;

            free(p);

	    break;
	}
	p_prev = p;
	p_next = p->next;
    }
}

void srv_DestroyRegisteredData(void)
{
    DEST_LIST *p, *p_next;

    for(p=Head;p;p=p_next)
    {
	misTrc(T_SERVER, "Freeing registered data (Address: %p)", p->pointer);

	if (p->destructor)
	    (*p->destructor)(p->pointer);
	else
	    free(p->pointer);
	p_next = p->next;
	free(p);
    }
    Head = NULL;
}
