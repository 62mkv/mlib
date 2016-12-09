static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Abstraction of the access(3) system call.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2003
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

#ifdef UNIX
# include <unistd.h>
#else
# include <io.h>
#endif

int osAccess(char *pathname, int mode)
{
    int status;

#ifdef UNIX
    status = access(pathname, mode);
#else
    status = _access(pathname, mode);
#endif

    return status;
}
