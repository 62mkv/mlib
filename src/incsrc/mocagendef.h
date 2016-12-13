/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file containing MOCA generic definitions.
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

#ifndef MOCAGENDEF_H
#define MOCAGENDEF_H

#define utf8Size(in) ((in) * 4)
#define utf8Chars(in) ((in) / 4)

/*
 *  Environment Variable Definitions
 */

#define ENV_PREFIX			  "MOCA_"
#define ENV_ENVNAME			  ENV_PREFIX "ENVNAME"
#define ENV_JAVA_VMARGS		          ENV_PREFIX "JAVA_VMARGS"
#define ENV_JAVA_CHARSET		  ENV_PREFIX "JAVA_CHARSET"
#define ENV_REGISTRY			  ENV_PREFIX "REGISTRY"
#define ENV_SHUTDOWN_EVENT		  ENV_PREFIX "_SHUTDOWN_EVENT"
#define ENV_PARENT_PID 			  ENV_PREFIX "_PARENT_PID"

/* Section Names */
#define REGSEC_SERVER		           "SERVER"
#define REGSEC_SERVICE		           "SERVICE"
#define REGSEC_SECURITY		           "SECURITY"
#define REGSEC_JAVA		           "JAVA"
#define REGSEC_ENVIRONMENT	           "ENVIRONMENT"

/* Server Section */
#define REGKEY_SERVER_URL 		    "url"
#define REGKEY_SERVER_MAILBOX_FILE	    "mailbox-file"

/* Service Section */
#define REGKEY_SERVICE_COMMAND              "command"
#define REGKEY_SERVICE_OUTFILE              "output"

/* Security Section */
#define REGKEY_SECURITY_DOMAIN		    "domain"

/* Java */
#define REGKEY_JAVA_VM   	            "vm"
#define REGKEY_JAVA_VM32   	            "vm32"
#define REGKEY_JAVA_VMARGS   	            "vmargs"
#define REGKEY_JAVA_VMARGS32   	            "vmargs32"

/* Socket Server Manager Section */
#define REGKEY_SOCKMGR_PORT                 "port"
#define REGKEY_SOCKMGR_CONSOLE_PASSWORD     "password"
#define REGKEY_SOCKMGR_SERVER_COMMAND       "server-command"
#define REGKEY_SOCKMGR_MIN_POOL_SIZE        "min-pool-size"
#define REGKEY_SOCKMGR_MAX_POOL_SIZE        "max-pool-size"
#define REGKEY_SOCKMGR_MAX_COMMANDS         "max-commands"
#define REGKEY_SOCKMGR_CLIENT_TIMEOUT       "client-timeout"

#define DEFAULT_SOCKMGR_PORT                8000
#define DEFAULT_SOCKMGR_CONSOLE_PASSWORD    "password"
#define DEFAULT_SOCKMGR_MIN_POOL_SIZE       5
#define DEFAULT_SOCKMGR_MAX_POOL_SIZE       10
#define DEFAULT_SOCKMGR_MAX_COMMANDS        10000
#define DEFAULT_SOCKMGR_CLIENT_TIMEOUT      0

/*
 *  Basic Macro Definitions
 */

#ifndef MIN
#   define MIN(a, b) ((a) < (b) ? (a) : (b))
#endif
#ifndef MAX
#   define MAX(a, b) ((a) > (b) ? (a) : (b))
#endif

#ifndef TRUE
#   define TRUE	 (1)
#endif
#ifndef FALSE
#   define FALSE (0)
#endif

#define MOCA_TRUE  1
#define MOCA_FALSE 0

/*
 *  MOCA Float Format Definition
 */

#define MOCA_FLT_FMT  "%.15g"

/*
 *  MOCA Standard Date Defintions
 */

#define MOCA_STD_DATE_LEN    14
#define MOCA_STD_DATE_FORMAT "YYYYMMDDHH24MISS"

/*
 *  Common Array Length Definitions
 */

#define ARGNAM_LEN           60
#define CATEGORY_LEN         100
#define CLASS_LEN            100
#define CMPLVL_LEN           100
#define COMPONENT_LEVEL_LEN  100
#define COMMAND_NAME_LEN     100
#define FIXVAL_LEN           10
#define FUNCTN_LEN           100
#define LIBNAME_LEN          100
#define NAMESPACE_LEN        100
#define PATHNAME_LEN         1024
#define PROGID_LEN           100
#define REMSYS_LEN           100
#define STNOUN_LEN           60
#define STVERB_LEN           15
#define TCPADR_LEN           20
#define TRIGGER_NAME_LEN     100
#define VERSION_LEN          100

#endif
