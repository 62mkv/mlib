/*#START***********************************************************************
 *
 *  $URL: https://athena.redprairie.com/svn/prod/moca/branches/vs2005/config/mocaconfig.h.win32-x86 $
 *  $Revision: 120395 $
 *  $Id: mocaconfig.h.win32-x86 120395 2007-02-13 16:03:23Z mlange $
 *
 *  Description: Win32 configuration header file.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2002-2007
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


#ifndef MOCACONFIG_H
#define MOCACONFIG_H

#define MOCAEXPORT __stdcall

#define LIBEXPORT  __declspec(dllexport)
#define LIBIMPORT  __declspec(dllimport)

#define _USE_RTM_VERSION
#define _USE_32BIT_TIME_T

#define _CRT_SECURE_NO_DEPRECATE
#define _CRT_NONSTDC_NO_DEPRECATE

#define HAVE_FTIME

#ifndef HAVE_MALLOC_H  /**/
#define HAVE_MALLOC_H  /**/
#endif                 /**/

#define PATH_SEPARATOR           '\\'
#define PATH_SEPARATOR_STR       "\\"
#define PATH_LIST_SEPARATOR      ';'
#define PATH_LIST_SEPARATOR_STR  ";"

#define MOCA_DEBUG_MALLOC

/* #define MOCA_MAX_TRACING  */

#endif
