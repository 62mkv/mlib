/*#START***********************************************************************
 *
 *  $URL: https://athena.redprairie.com/svn/prod/moca/branches/vs2005/config/mocaconfig.h.linux-x86 $
 *  $Revision: 120395 $
 *  $Id: mocaconfig.h.linux-x86 120395 2007-02-13 16:03:23Z mlange $
 *
 *  Description: Linux configuration header file.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2007
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

#ifndef LINUX
#define LINUX
#endif

#define MOCAEXPORT
#define LIBEXPORT
#define LIBIMPORT

#ifndef HAVE_GETTIMEOFDAY	/**/
#define HAVE_GETTIMEOFDAY	/**/
#endif				/**/
/*#ifndef HAVE_FTIME		  */
/*#define HAVE_FTIME		  */
/*#endif			  */

#ifndef USE_FCNTL_FOR_ASYNC	/**/
#define USE_FCNTL_FOR_ASYNC	/**/
#endif				/**/
/*#ifndef USE_IOCTL_FOR_ASYNC	*/
/*#define USE_IOCTL_FOR_ASYNC	*/
/*#endif			*/

#ifndef USE_MSGHDR 		/**/
#define USE_MSGHDR 		/**/
#endif				/**/
/*#ifndef USE_NSPIPE 		  */
/*#define USE_NSPIPE 		  */
/*#endif			  */

/*#ifndef HAVE_MSGHDR_CLASSIC 	  */
/*#define HAVE_MSGHDR_CLASSIC 	  */
/*#endif			  */
#ifndef HAVE_MSGHDR_CMSGHDR 	/**/
#define HAVE_MSGHDR_CMSGHDR 	/**/
#endif				/**/

#ifndef SELECT_NEEDS_SYS_TIME_H		/**/
#define SELECT_NEEDS_SYS_TIME_H		/**/
#endif					/**/
#ifndef SELECT_NEEDS_SYS_TYPES_H	/**/
#define SELECT_NEEDS_SYS_TYPES_H	/**/
#endif					/**/
/*#ifndef SELECT_NEEDS_SYS_SELECT_H	  */
/*#define SELECT_NEEDS_SYS_SELECT_H	  */
/*#endif				  */
#ifndef SELECT_NEEDS_UNISTD_H		/**/
#define SELECT_NEEDS_UNISTD_H		/**/
#endif					/**/

#ifndef MSGHDR_NEEDS_SYS_TYPES_H	/**/
#define MSGHDR_NEEDS_SYS_TYPES_H	/**/
#endif					/**/

#ifndef STAT_NEEDS_SYS_TYPES_H	/**/
#define STAT_NEEDS_SYS_TYPES_H	/**/
#endif				/**/
#ifndef STAT_NEEDS_SYS_STAT_H	/**/
#define STAT_NEEDS_SYS_STAT_H	/**/
#endif				/**/

#ifndef HAVE_GLOB_H		/**/
#define HAVE_GLOB_H		/**/
#endif				/**/
#ifndef HAVE_SYS_STAT_H		/**/
#define HAVE_SYS_STAT_H		/**/
#endif				/**/
#ifndef HAVE_SYS_TYPES_H	/**/
#define HAVE_SYS_TYPES_H	/**/
#endif				/**/
#ifndef HAVE_SYS_TIME_H		/**/
#define HAVE_SYS_TIME_H		/**/
#endif				/**/
#ifndef HAVE_SYS_UIO_H		/**/
#define HAVE_SYS_UIO_H		/**/
#endif				/**/
#ifndef HAVE_SYS_UTSNAME_H	/**/
#define HAVE_SYS_UTSNAME_H	/**/
#endif				/**/
#ifndef HAVE_SYS_SELECT_H	/**/
#define HAVE_SYS_SELECT_H	/**/
#endif				/**/
/*#ifndef HAVE_SYS_SYSTEMINFO_H   */
/*#define HAVE_SYS_SYSTEMINFO_H   */
/*#endif			  */

/*#ifndef HAVE_BROKEN_GLOB_SLASH  */
/*#define HAVE_BROKEN_GLOB_SLASH  */
/*#endif                          */

#ifndef HAVE_DLOPEN		/**/
#define HAVE_DLOPEN		/**/
#endif				/**/
/*#ifndef HAVE_SHL_LOAD           */
/*#define HAVE_SHL_LOAD           */
/*#endif                          */

#define LPCOMMAND                "/usr/bin/lp -c"

#define PATH_SEPARATOR           '/'
#define PATH_SEPARATOR_STR       "/"
#define PATH_LIST_SEPARATOR      ':'
#define PATH_LIST_SEPARATOR_STR  ":"

#define MOCA_DEBUG_MALLOC

/*#define MOCA_MAX_TRACING 	  */

#endif


