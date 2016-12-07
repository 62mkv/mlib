/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file containing MOCA symbols.
 *
 *               Our default behavior is to build with POSIX compliance, but 
 *               this can be manually overridden by the developer by defining 
 *               one of the macros below.
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


#ifndef MOCASYMBOLS_H
#define MOCASYMBOLS_H

#if defined (AIX)

#   define UNIX

#   if defined (MOCA_ALL_SOURCE)
#       ifndef _ALL_SOURCE
#       define _ALL_SOURCE
#       endif
#   elif defined (MOCA_XOPEN_SOURCE)
#       ifndef _XOPEN_SOURCE
#       define _XOPEN_SOURCE
#       endif
#   else 
#       ifndef _POSIX_SOURCE
#       define _POSIX_SOURCE
#       endif
#   endif

#elif defined (HPUX)

#   define UNIX

#   if defined (MOCA_ALL_SOURCE)
#       ifndef _HPUX_SOURCE
#       define _HPUX_SOURCE
#       endif
#       ifndef _POSIX_C_SOURCE
#       define _POSIX_C_SOURCE 199506L
#       endif
#   elif defined (MOCA_XOPEN_SOURCE)
#       ifndef _XOPEN_SOURCE
#       define _XOPEN_SOURCE
#       endif
#       ifndef _POSIX_C_SOURCE
#       define _POSIX_C_SOURCE 199506L
#       endif
#   else
#       ifndef _POSIX_SOURCE
#       define _POSIX_SOURCE
#       endif
#       ifndef _POSIX_C_SOURCE
#       define _POSIX_C_SOURCE 199506L
#       endif
#   endif

#elif defined (LINUX)

#   define UNIX

#   if defined (MOCA_ALL_SOURCE)
#       ifndef _GNU_SOURCE
#       define _GNU_SOURCE
#       endif
#   elif defined (MOCA_XOPEN_SOURCE)
#       ifndef _XOPEN_SOURCE
#       define _XOPEN_SOURCE
#       endif
#   else
#       ifndef _POSIX_C_SOURCE
#       define _POSIX_C_SOURCE 199309L
#       endif
#   endif

#elif defined (OSX)

#   define UNIX

#   if defined (MOCA_ALL_SOURCE)
#       ifndef _GNU_SOURCE
#       define _GNU_SOURCE
#       endif
#   elif defined (MOCA_XOPEN_SOURCE)
#       ifndef _XOPEN_SOURCE
#       define _XOPEN_SOURCE
#       endif
#   else
#       ifndef _POSIX_C_SOURCE
#       define _POSIX_C_SOURCE 199309L
#       endif
#   endif

#elif defined (SOLARIS)

#   define UNIX

#   if defined (MOCA_ALL_SOURCE)
#       ifndef __EXTENSIONS__
#       define __EXTENSIONS__
#       endif
#       ifndef _POSIX_C_SOURCE
#       define _POSIX_C_SOURCE 199506L
#       endif
#   elif defined (MOCA_XOPEN_SOURCE)
#       ifndef _XOPEN_SOURCE
#       define _XOPEN_SOURCE
#       endif
#       ifndef _POSIX_C_SOURCE
#       define _POSIX_C_SOURCE 199506L
#       endif
#   else
#       ifndef _POSIX_SOURCE
#       define _POSIX_SOURCE
#       endif
#       ifndef _POSIX_C_SOURCE
#       define _POSIX_C_SOURCE 199506L
#       endif
#   endif

#endif

#ifndef MOCA_PUBLIC
#    define MOCA_PRIVATE
#endif

#endif
