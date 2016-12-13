/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file for sqllib.
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

#ifndef SQLCONVLIB_H
#define SQLCONVLIB_H

#include <moca.h>
#include <common.h>

/*
 *  Function Prototypes
 */

#if defined (__cplusplus)
extern "C" {
#endif

/* sqlconv_ConvertSQL.c */
void sqlconvConvertSQLInit(void);
long sqlconvConvertSQL(const char *sqlStmt, char **newSqlStmt, short* o_nowait, mocaBindList *bindList); 
void sqlconvGetConvertPerformance(long   *o_cnt,
                                long   *o_hits,
                                long   *o_age_out,
                                long   *o_stmtCount,
                                long   *o_stmtMax,
                                double *o_rate,
                                double *o_tot_tim,
                                long   *o_noop_cnt);
void sqlconvResetConvertPerformance(void);

#if defined (__cplusplus)
}
#endif

#endif
