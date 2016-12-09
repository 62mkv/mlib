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
 *  Copyright (c) 2005
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
#include "srvprivate.h"


/****************************************************************************
 * This is used to do a srvFreeMemory but assumes that the structure will
 * be SRVRET_STRUCT (which I think it always is). This is important for the
 * situation where we have some results that remains static or otherwise exists
 * outside of the normal command context and needs to be freed by some function
 * that runs later on.  The current functions only allow you to define the
 * pointer to free and the function to be used to free it.  So this does not
 * allow you to pass the structure type.
 ****************************************************************************/

long srvFreeReturnStruct(RETURN_STRUCT *RetPtr_i)
{
    return (srvFreeMemory(SRVRET_STRUCT, RetPtr_i));
}

