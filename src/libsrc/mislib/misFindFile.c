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

#include <string.h> /* strcpy */
#include <stdlib.h> /* malloc */

#include <mocaerr.h>
#include <mocagendef.h>
#include <oslib.h>
#include <mislib.h>

long misFindFile (char *Filespec, char **ReturnedFilename, long *Context)
{
    long RetCode;
    char buf[500];

    /* To preserve legacy path-trimming behavior, pass FileFlags=0 . */

    RetCode = osFindFile(Filespec, buf, (OS_FF_CONTEXT *)Context, 0);
    if (RetCode != eOK) return RetCode;

    *ReturnedFilename = malloc(strlen(buf)+1);
    strcpy (*ReturnedFilename, buf);

    return eOK;
}

/* misEndFindFile - deallocate context for file list */
/*                  deallocate each filename string */
/*                  deallocate the list structure */
long misEndFindFile (long *inContext)
{
   return osEndFindFile((OS_FF_CONTEXT *)inContext);
}
