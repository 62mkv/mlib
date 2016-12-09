/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file containing SQL datatype definitions.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2002-2006
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

#ifndef SQLDATATYPES_H
#define SQLDATATYPES_H

#include <sqlDataTypesObsolete.h>

#ifdef ORACLE
# include <sqlDataTypesOracle.h>
#endif

#ifdef SQL_SERVER
# include <sqlDataTypesSQLServer.h>
#endif

#ifdef H2
# include <sqlDataTypesH2.h>
#endif

#endif
