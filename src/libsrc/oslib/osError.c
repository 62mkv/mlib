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
 *#END*************************************************************************/

#include <moca.h>

#include <stdlib.h>
#include <string.h>

#ifdef UNIX
# include <errno.h>
#endif

#include <mocaerr.h>
#include <oslib.h>

#include "osprivate.h"

long osErrno(void)
{
#ifdef WIN32
    return GetLastError();
#else
    return errno;
#endif
}

char *osError(void)
#ifdef WIN32
{
    return osStrError(GetLastError( ));
}
#else
{
    return osStrError(errno);
}
#endif

char *osStrError(long number)
#ifdef WIN32
{
    static char errbuf[1000];
      
    errbuf[0]='\0';


    FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM,NULL, number,
		  MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
		  errbuf,sizeof errbuf,NULL);
    return errbuf;
}
#else
{
    return strerror(number);
}
#endif
