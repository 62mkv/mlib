static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: 
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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
 *#END************************************************************************/

#include <moca.h>

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <common.h>
#include <mocaerr.h>
#include <srvlib.h>
#include <sqllib.h>
#include <mislib.h>

LIBEXPORT RETURN_STRUCT *testSQLBindDate(char *arg)
{
    mocaDataRes *res = NULL;
    long status;

    status = dbExecBind("select to_date(:arg, 'YYYYMMDDHH24MISS') from dual",
			&res, NULL, 
			"arg", COMTYP_DATTIM, 14, arg, 0,
			NULL);

    return srvAddSQLResults(res, status);
}

LIBEXPORT RETURN_STRUCT *testSQLBindReference(char *arg)
{
    mocaDataRes *res = NULL;
    long status;
    int dbtype;
    char buffer[100];

    dbInfo(&dbtype);

    strcpy(buffer, "BAR");
    
    if (dbtype == MOCA_DB_ORACLE)
    {
	status = dbExecBind("begin select 'FOO' into :arg from dual;end;",
			    NULL, NULL, 
			    "arg", COMTYP_CHARPTR, sizeof buffer, buffer, 0,
			    NULL);
    }
    else if (dbtype == MOCA_DB_MSSQL)
    {
        strcpy(buffer, "FOO");
    }

    return srvResults(eOK, "result", COMTYP_CHAR, sizeof(buffer), buffer, NULL);
}

LIBEXPORT RETURN_STRUCT *testSQLBindValue(char *name, char *value)
{
    mocaDataRes *res = NULL;
    long status;
    int dbtype;
    char buffer[500];

    dbInfo(&dbtype);

    sprintf(buffer, "select :%s as result from dual", name);
    
    status = dbExecBind(buffer, &res, NULL,
                        name, COMTYP_CHAR, strlen(value), value, 0,
                        NULL);

    return srvAddSQLResults(res, status);
}
