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
#include <stdlib.h>

#include <mocaerr.h>
#include "osprivate.h"

long osEncodeDescriptor(PIPE_FD descriptor, char *text)
{
#ifdef WIN32
    sprintf(text,"%p", (void *) descriptor);
#else
    sprintf(text,"%d", descriptor);
#endif
    return eOK;
}

long osDecodeDescriptor(char *text, PIPE_FD *descriptor)
{
#ifdef WIN32
    void *tmpptr;
    sscanf(text,"%p", &tmpptr);
    *descriptor = (PIPE_FD) tmpptr;;
#else
    *descriptor = atoi(text);
#endif
    return eOK;
}
