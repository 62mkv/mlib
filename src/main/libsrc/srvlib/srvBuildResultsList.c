static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Dump data to file.
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
 *#END************************************************************************/

#include <moca.h>

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdarg.h>

#include <common.h>
#include <mocaerr.h>

#include "srvprivate.h"

SRV_RESULTS_LIST *srvCreateResultsList(long NumOfColumns)
{
    long ii;
    SRV_RESULTS_LIST *reslist;

    reslist = calloc(NumOfColumns, sizeof(SRV_RESULTS_LIST));

    for (ii = 0; ii < NumOfColumns; ii++)
        reslist[ii].numcols = NumOfColumns;

    return reslist;
}

long srvBuildResultsList(SRV_RESULTS_LIST *reslist, long ColNum,
	                 char *name, char dtype, long len, int nullind, ...)
{
    va_list Arguments;

    /* If a column number wasn't given, look for a matching column. */
    if (ColNum < 0)
    {
	long found = 0;

        for (ColNum = 0; ColNum < reslist->numcols; ColNum++)
        {
	    if (strlen(reslist[ColNum].colname) == 0 ||
		strcmp(name, reslist[ColNum].colname) == 0)
            {
	        found = 1;
                break;
	    }
        }

	if (!found)
	    return eINVALID_ARGS;
    }

    if (name)
	strcpy(reslist[ColNum].colname, name);

    reslist[ColNum].type = dtype;

    va_start(Arguments, nullind);
    switch(dtype)
    {
    case COMTYP_INT:
    case COMTYP_LONG:
    case COMTYP_BOOLEAN:
	if (!nullind)
	{
	    reslist[ColNum].size = sizeof(long);
	    reslist[ColNum].data.ldata = va_arg(Arguments, long);
	}
	else
	{
	    reslist[ColNum].size = 0;
	    reslist[ColNum].data.ldata = 0;
	}
	break;

    case COMTYP_FLOAT:
	if (!nullind)
	{
	    reslist[ColNum].size = sizeof(double);
	    reslist[ColNum].data.fdata = va_arg(Arguments, double);
	}
	else
	{
	    reslist[ColNum].size = 0;
	    reslist[ColNum].data.fdata = 0;
	}
	break;

    case COMTYP_DATTIM:
    case COMTYP_STRING:
	if (!nullind)
	{
	    reslist[ColNum].data.cdata = va_arg(Arguments, char *);

	    /*
	     * Strings are a little different in that we allow people
	     * to add a string to a result set with a defined max length
	     * of 0 in cases where the value is basically a freeform
	     * text value not related to a database column.
	     *
	     * Therefore, we need to make sure we use the real size of
	     * the value, not necessarily the size that was passed in.
	     */
            if (len == 0 && reslist[ColNum].data.cdata)
            {
                reslist[ColNum].size = utf8CharLen(reslist[ColNum].data.cdata);
            }
            else
            {
                reslist[ColNum].size = len;
            }
	}
	else
	{
	    reslist[ColNum].size = 0;
	    reslist[ColNum].data.cdata = (char *)NULL;
	}

	break;

    case COMTYP_JAVAOBJ:
    case COMTYP_GENERIC:
    case COMTYP_RESULTS:
	if (!nullind)
	{
	    reslist[ColNum].size = len;
	    reslist[ColNum].data.vdata = va_arg(Arguments, void *);
	}
	else
	{
	    reslist[ColNum].size = 0;
	    reslist[ColNum].data.vdata = NULL;
	}

	break;

    case COMTYP_BINARY:
        if (!nullind)
        {
            reslist[ColNum].size = len;
            reslist[ColNum].data.vdata = va_arg(Arguments, char *);
        }
        else
        {
            reslist[ColNum].size = 0;
            reslist[ColNum].data.vdata = (char *)NULL;
        }
	break;
    }
    va_end(Arguments);

    reslist[ColNum].nullind = nullind;

    return eOK;
}

void srvFreeResultsList(SRV_RESULTS_LIST *reslist)
{
    free(reslist);
}

