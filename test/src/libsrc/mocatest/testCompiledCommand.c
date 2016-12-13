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

LIBEXPORT RETURN_STRUCT *testCompiledCommand(char *arg)
{
    SRV_COMPILED_COMMAND *compiled = NULL;
    mocaBindList  *bindList = NULL;
    RETURN_STRUCT *ret = NULL;
    long stat = eOK;
    mocaDataRes *savedRes = NULL;

    if (arg) 
    {
	stat = srvInitiateCommand(arg, &ret);
    }

    if (stat == eOK)
    {
        RETURN_STRUCT *ret2 = NULL;

	if (ret)
	{
	    savedRes = srvGetResults(ret);
	}

	sqlBuildBindList(&bindList,
		  "rs", COMTYP_RESULTS, sizeof (mocaDataRes *), savedRes, 0,
		  NULL
	       );

	stat = srvCompileCommand("[[if (rs) rs.sort(sortby)]]", &compiled);
	stat = srvInitiateCompiled(compiled, bindList, &ret2, 1);
	srvFreeMemory(SRVCMP_STRUCT, compiled);

        sqlFreeBindList(bindList);
	if (ret) srvFreeMemory(SRVRET_STRUCT, ret);

	ret = ret2;
    }

    return ret;
}
