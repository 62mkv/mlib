/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: MOCA tablespace name definitions.
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

#ifndef MOCATBLDEF_H
#define MOCATBLDEF_H

#include <usrtbldef.h>
#include <vartbldef.h>

#ifndef MOCA_TBS_DEFS
#define MOCA_TBS_DEFS
#define MOCA_DATA_TBS_01   GEN_D_01
#define MOCA_INDEX_TBS_01  GEN_X_01
#endif

#ifndef COMP_VER_TBL
#define COMP_VER_PK_TBLSPC  MOCA_INDEX_TBS_01
#define COMP_VER_PK_STORAGE /* STORAGE (INITIAL 500K) */
#define COMP_VER_TBL_TBLSPC  MOCA_DATA_TBS_01
#define COMP_VER_TBL_STORAGE /* STORAGE (INITIAL 500K) */
#define COMP_VER_TBL
#endif

#ifndef MOCA_DATASET_TBL
#define MOCA_DATASET_TBL_TBLSPC  MOCA_DATA_TBS_01
#define MOCA_DATASET_TBL_STORAGE /* STORAGE (INITIAL 1K) */
#define MOCA_DATASET_TBL
#endif

#ifndef MOCA_DBVERSION_TBL
#define MOCA_DBVERSION_TBL_TBLSPC  MOCA_DATA_TBS_01
#define MOCA_DBVERSION_TBL_STORAGE /* STORAGE (INITIAL 1K) */
#define MOCA_DBVERSION_TBL
#endif

#ifndef TASK_DEFINITION_TBL
#define TASK_DEFINITION_PK_TBLSPC  MOCA_INDEX_TBS_01
#define TASK_DEFINITION_PK_STORAGE /* STORAGE (INITIAL 500K) */
#define TASK_DEFINITION_TBL_TBLSPC  MOCA_DATA_TBS_01
#define TASK_DEFINITION_TBL_STORAGE /* STORAGE (INITIAL 500K) */
#define TASK_DEFINITION_TBL
#endif

#ifndef JOB_DEFINITION_TBL
#define JOB_DEFINITION_PK_TBLSPC  MOCA_INDEX_TBS_01
#define JOB_DEFINITION_PK_STORAGE /* STORAGE (INITIAL 500K) */
#define JOB_DEFINITION_TBL_TBLSPC  MOCA_DATA_TBS_01
#define JOB_DEFINITION_TBL_STORAGE /* STORAGE (INITIAL 500K) */
#define JOB_DEFINITION_TBL
#endif

#ifndef TASK_ENV_DEFINITION_TBL
#define TASK_ENV_DEFINITION_PK_TBLSPC  MOCA_INDEX_TBS_01
#define TASK_ENV_DEFINITION_PK_STORAGE /* STORAGE (INITIAL 500K) */
#define TASK_ENV_DEFINITION_TBL_TBLSPC  MOCA_DATA_TBS_01
#define TASK_ENV_DEFINITION_TBL_STORAGE /* STORAGE (INITIAL 500K) */
#define TASK_ENV_DEFINITION_TBL
#endif

#ifndef JOB_ENV_DEFINITION_TBL
#define JOB_ENV_DEFINITION_PK_TBLSPC  MOCA_INDEX_TBS_01
#define JOB_ENV_DEFINITION_PK_STORAGE /* STORAGE (INITIAL 500K) */
#define JOB_ENV_DEFINITION_TBL_TBLSPC  MOCA_DATA_TBS_01
#define JOB_ENV_DEFINITION_TBL_STORAGE /* STORAGE (INITIAL 500K) */
#define JOB_ENV_DEFINITION_TBL
#endif

#ifndef TASK_DEFINITION_EXEC_TBL
#define TASK_DEFINITION_EXEC_PK_TBLSPC  MOCA_INDEX_TBS_01
#define TASK_DEFINITION_EXEC_PK_STORAGE /* STORAGE (INITIAL 500K) */
#define TASK_DEFINITION_EXEC_IDX1_TBLSPC  MOCA_INDEX_TBS_01
#define TASK_DEFINITION_EXEC_IDX1_STORAGE /* STORAGE (INITIAL 500K) */
#define TASK_DEFINITION_EXEC_TBL_TBLSPC  MOCA_DATA_TBS_01
#define TASK_DEFINITION_EXEC_TBL_STORAGE /* STORAGE (INITIAL 500K) */
#define TASK_DEFINITION_EXEC_TBL
#endif

#ifndef JOB_DEFINITION_EXEC_TBL
#define JOB_DEFINITION_EXEC_PK_TBLSPC  MOCA_INDEX_TBS_01
#define JOB_DEFINITION_EXEC_PK_STORAGE /* STORAGE (INITIAL 500K) */
#define JOB_DEFINITION_EXEC_IDX1_TBLSPC  MOCA_INDEX_TBS_01
#define JOB_DEFINITION_EXEC_IDX1_STORAGE /* STORAGE (INITIAL 500K) */
#define JOB_DEFINITION_EXEC_TBL_TBLSPC  MOCA_DATA_TBS_01
#define JOB_DEFINITION_EXEC_TBL_STORAGE /* STORAGE (INITIAL 500K) */
#define JOB_DEFINITION_EXEC_TBL
#endif

#endif
