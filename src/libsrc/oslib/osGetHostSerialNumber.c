static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Used to get the server's OS-specific serial number.
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

#include <stddef.h>

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
char *osGetHostSerialNumber(void)
{
    HKEY hKey;
    static char value[200];
    DWORD size = sizeof value;

    if (ERROR_SUCCESS !=
	RegOpenKeyEx(HKEY_LOCAL_MACHINE,
		     "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion",
		     0, KEY_READ, &hKey))
    {
	return NULL;
    }
    if (ERROR_SUCCESS != RegQueryValueEx(hKey, "ProductId", NULL, NULL, value,
	&size))
    {
	RegCloseKey(hKey);
	return NULL;
    }
    RegCloseKey(hKey);
    return value;
}

#else

char *osGetHostSerialNumber(void)
{
    char *sp;

#if defined(AIX) || defined(HPUX)
    {
	static struct utsname buf;

	if (uname(&buf) == -1)
	    return NULL;

#ifdef AIX
	sp = (char *) buf.machine;
#else
	sp = (char *) buf.idnumber;
#endif
    }

#elif defined(SOLARIS)
    {
	static char buf[81];

	if (sysinfo(SI_HW_SERIAL, buf, sizeof(buf)) == -1)
	    return NULL;
	
	sp = buf;
    }
#else

    sp = "00000000";

#endif

    return sp;
}
#endif
