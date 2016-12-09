/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: MOCA column width definitions.
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

#ifndef MOCACOLWID_H
#define MOCACOLWID_H

#ifndef UTF8_SIZE
#define UTF8_SIZE *4
#endif

#define COMP_FILE_EXT               3 UTF8_SIZE
#define COMP_TYP_LEN                1 UTF8_SIZE

#define DBVERSION_LEN              30 UTF8_SIZE
#define DS_DESC_LEN                80 UTF8_SIZE
#define DS_DIR_LEN                512 UTF8_SIZE
#define DS_NAME_LEN                40 UTF8_SIZE

#define FRM_FILE_NAM_LEN          256 UTF8_SIZE

#define GRP_NAM_LEN                40 UTF8_SIZE

#define JOB_ENV_NAME_LEN         256 UTF8_SIZE
#define JOB_ENV_VALUE_LEN       2000 UTF8_SIZE
#define JOB_ID_LEN                256 UTF8_SIZE
#define JOB_LOG_LEN              2000 UTF8_SIZE
#define JOB_NAME_LEN             2000 UTF8_SIZE
#define JOB_TYPE_LEN               60 UTF8_SIZE
#define JOB_TRACE_LEN              60 UTF8_SIZE
#define JOB_SCHED_LEN            2000 UTF8_SIZE
#define JOB_EXEC_MSG_LEN         2000 UTF8_SIZE

#define LIC_KEY_LEN               100

#define CLUSTER_ROLE_ID_LEN       256 UTF8_SIZE

#define PROG_ID_LEN               256 UTF8_SIZE

#define STNOUN_LEN                 60
#define STVERB_LEN                 15

#define TASK_ENV_NAME_LEN         256 UTF8_SIZE
#define TASK_ENV_VALUE_LEN       2000 UTF8_SIZE
#define TASK_ID_LEN               256 UTF8_SIZE
#define TASK_LOG_LEN             2000 UTF8_SIZE
#define TASK_TYP_LEN                1 UTF8_SIZE
#define TASK_NAME_LEN            2000 UTF8_SIZE
#define TASK_TRACE_LEN             60 UTF8_SIZE
#define TASK_RUN_DIR_LEN         2000 UTF8_SIZE
#define TASK_EXEC_STATUS_LEN     2000 UTF8_SIZE
#define TASK_EXEC_START_LEN      2000 UTF8_SIZE
#define TCPADR_LEN                 20

#define URL_LEN                   256 UTF8_SIZE

#endif
