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
 *  Copyright (c) 2002-2009
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

#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <math.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <sqllib.h>
#include <srvlib.h>

#include "msql.h"

#define DIVIDER "------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
#define SPACES  "                                                                                                                                                                                                                  "
#define DATTIM_DEFAULT_WIDTH 19 /* xxxx-xx-xx xx:xx:xx */

static int Widths[500];

void Print(char *format, ...)
{
    long len;
    char *ptr;
    va_list args;

    /* We always write output to stdout. */
    va_start(args, format);
    len = vprintf(format, args);
    va_end(args);
    fflush(stdout);

    /* We may write to the spool as well. */
    if (IsSpooling( ))
    {
	ptr = (char *) calloc(1, len + 1);

	va_start(args, format);
	vsprintf(ptr, format, args);
	Spool(ptr);
	va_end(args);

	free(ptr);
    }

    return;
}

void PrintPrompt(long linenum)
{
    if (gInstallMode)
        return;

    if (linenum == 0)
    {
        if (gAutoCommit)
            Print("MSQL> ");
        else
            Print("(autocommit off) MSQL> ");
    }
    else
    {
        if (gAutoCommit)
            Print("%4d  ", linenum);
        else
            Print("(autocommit off) %4d  ", linenum);
    }
}

void PrintStartBanner(void)
{
    Print(misGetStartBanner(APPNAME));
}

void PrintStatusStart(void)
{
    Print("\nExecuting... ");
}

void PrintStatusDone(RETURN_STRUCT *ret)
{
    long status;

    status = srvGetReturnStatus(ret);

    if (status == eOK)
	Print("Success!\n\n");
    else
	Print("Error!\n\n");
}

void PrintHeadings(RETURN_STRUCT *ret)
{
    int i;
    long status;
    char *ldesc, *sdesc, *desc;
    char heading[3000] = "";
    char divider[3000] = "";

    mocaDataRes *res = ret->ReturnedData;

    /* We don't print any results if an error occurred. */
    status = srvGetReturnStatus(ret);
    if (status != eOK)
        return;

    /* Cycle through each column in this result set. */
    for (i = 0; res && i < res->NumOfColumns; i++)
    {
	if (res->LongDescription)
	    ldesc = res->LongDescription[i];
	else
	    ldesc = NULL;

	if (res->ShortDescription)
	    sdesc = res->ShortDescription[i];
	else
	    sdesc = NULL;

	if (!ldesc || (int) strlen(ldesc) > res->ActualMaxLen[i])
	    desc = sdesc;
	else
	    desc = ldesc;

	if (!desc)
	    desc = res->ColName[i];
	if (!desc)
	    desc = "???";

	Widths[i] = desc ? (int) strlen(desc) : 3;
	if (Widths[i] < res->ActualMaxLen[i])
	    Widths[i] = MIN(res->ActualMaxLen[i], (long) strlen(DIVIDER));

	if (Widths[i] < 3)
	    Widths[i] = 3;

	if (res->DataType[i] == COMTYP_DATTIM)
	    Widths[i] = DATTIM_DEFAULT_WIDTH;

	strncat(heading, desc, Widths[i]);
	if (Widths[i] > (int) strlen(desc))
	    strncat(heading, SPACES, Widths[i] - (int) strlen(desc));
	strcat(heading, "  ");
	strncat(divider, DIVIDER, Widths[i]);
	strcat(divider, "  ");
    }

    if (i > 0)
	Print("%s\n%s\n", heading, divider);
}

void PrintResults(RETURN_STRUCT *ret)
{
    long i,
	 status;

    char *dp;

    char temp[10000],
         temp_str[10000],
         temp_date[30],
	 results[10000];

    mocaDataRow *row;
    mocaDataRes *res = ret->ReturnedData;

    /* We don't print any results if an error occurred. */
    status = srvGetReturnStatus(ret);
    if (status != eOK)
	return;

    /* Cycle through each row in the result set. */
    for (row = sqlGetRow(res); row; row = sqlGetNextRow(row))
    {
	/* This is where we'll put our results that we'll be printing out. */
	memset(results, 0, sizeof results);

        /* Cycle through each column in this row. */
	for (i = 0; i < res->NumOfColumns; i++)
	{
	    switch (res->DataType[i])
	    {
	    case COMTYP_DATTIM:
		/* 
		 * This should catch all situations where a NULL is returned.
		 */
		if (row->DataPtr[i] && 
		    !row->NullInd[i] && 
		    *(char *)(row->DataPtr[i]))
		{
                    dp = (char *)row->DataPtr[i];
		    if (strlen(dp) != strlen("YYYYMMDDHHMISS"))
		    {
			sprintf(temp_str, "%-*.*s  ",
				Widths[i], Widths[i], "BAD-DATE!");
		    }
		    else
		    {
			sprintf(temp_date, "%.4s-%.2s-%.2s %.2s:%.2s:%.2s",
				dp, dp+4, dp+6, dp+8, dp+10,dp+12);
			sprintf(temp_str, "%-*.*s  ",
				Widths[i],
				Widths[i],
				temp_date);
		    }
		}
		else
                {

		    sprintf(temp_str, "%-*.*s  ",
			    Widths[i], Widths[i], "(null)");
                }

		strcat(results, temp_str);
		break;

	    case COMTYP_CHAR:
		if (row->DataPtr[i] && !row->NullInd[i])
		{
		    sprintf(temp_str, "%-*.*s  ",
			    Widths[i],
			    Widths[i],
			    (char *) row->DataPtr[i]);

                }
		else
		{
		    sprintf(temp_str, "%-*.*s  ",
			    Widths[i], Widths[i], "(null)");
                }
		
		strcat(results, temp_str);
		break;

	    case COMTYP_INT:
	    case COMTYP_LONG:
		if (row->DataPtr[i] && !row->NullInd[i])
		    sprintf(temp, "%ld", *(long *) row->DataPtr[i]);
		else
		    sprintf(temp, "(null)");

		sprintf(temp_str, "%-*.*s  ",
			Widths[i],
			Widths[i],
			temp);

		strcat(results, temp_str);
		break;

	    case COMTYP_BOOLEAN:
		if (row->DataPtr[i] && !row->NullInd[i])
		{
		    sprintf(temp, "%s", 
			    * (long *) row->DataPtr[i] ? "TRUE" : "FALSE");
		}
		else
		{
		    sprintf(temp, "(null)");
                }

		sprintf(temp_str, "%-*.*s  ",
			Widths[i],
			Widths[i],
			temp);

		strcat(results, temp_str);
		break;

	    case COMTYP_FLOAT:
		if (row->DataPtr[i] && !row->NullInd[i])
		    sprintf(temp, "%.15g", *(double *) row->DataPtr[i]);
		else
		    sprintf(temp, "(null)");

		sprintf(temp_str, "%-*.*s  ",
			Widths[i],
			Widths[i], temp);

		strcat(results, temp_str);
		break;

	    case COMTYP_RESULTS:
		if (row->DataPtr[i] && !row->NullInd[i])
		    sprintf(temp, "%-*.*s  ", Widths[i], Widths[i], "(res)");
		else
		    sprintf(temp, "%-*.*s  ", Widths[i], Widths[i], "(null)");
		strcat(results, temp);
		break;

	    case COMTYP_GENERIC:
		if (row->DataPtr[i] && !row->NullInd[i])
		    sprintf(temp, "%-*.*s  ", Widths[i], Widths[i], "(gen)");
		else
		    sprintf(temp, "%-*.*s  ", Widths[i], Widths[i], "(null)");
		strcat(results, temp);
		break;
	    }
	}

	Print("%s\n", results);
    }

    Print("\n");
}

void PrintRowsAffected(RETURN_STRUCT *ret)
{
    long status;

    if (gInstallMode)
        return;

    status = srvGetReturnStatus(ret);
    if (status == eOK ||
        status == eDB_NO_ROWS_AFFECTED || 
        status == eSRV_NO_ROWS_AFFECTED)
    {
        long rowcount;

        /* Get the result set. */
        mocaDataRes *res = ret->ReturnedData;

        /* Get the number of rows in the result set. */
        rowcount = sqlGetNumRows(res);

        /* Provide nice messages with the number of rows. */
        if (status == eOK)
            Print("(%ld Rows Affected)\n\n", sqlGetNumRows(res));
        else
            Print("Command affected no rows\n\n");
    }
    else  
    {
        Print("ERROR: %d", status);
        Print(" - %s\n\n", srvResultsMessage(ret));
    }
}
