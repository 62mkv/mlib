static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Used to get the server's OS login 
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

#define MOCA_ALL_SOURCE

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <string.h>

#ifdef UNIX 

#ifdef HAVE_SYS_UTSNAME_H
# include <sys/utsname.h>
#endif
#ifdef HAVE_SYS_SYSTEMINFO_H
# include <sys/systeminfo.h>
#endif

#endif

#include "osprivate.h"

#ifdef WIN32
#include <Lmcons.h> /* For UNLEN */
#endif

#ifdef OSX
#include <utmp.h> /* For UT_NAMESIZE */
#endif

char *osGetLogin(char * buffer, int buffer_len)
{
#ifdef WIN32
    unsigned long len = UNLEN + 1;
    char login_name[UNLEN + 1] = "";

    GetUserName(login_name, &len);
#else
# ifdef OSX
    char *temp;
    char login_name[UT_NAMESIZE + 1] = "";
    
    temp = getlogin( );
    strncpy(login_name, temp, sizeof(login_name)); 
# else
    char login_name[L_cuserid] = "";

    cuserid(login_name);
# endif
#endif

    if(login_name[0])
    {
        strncpy(buffer,login_name,buffer_len);
        buffer[buffer_len-1] = '\0';
        return buffer;
    }
    else
    {
        return NULL;
    }
}

