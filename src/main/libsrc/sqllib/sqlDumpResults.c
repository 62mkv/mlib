static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Function to dump a MOCA result set.
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


#include <moca.h>
#include <stdio.h>
#include <stddef.h>
#include <stdlib.h>

#include <mocagendef.h>
#include <mislib.h>
#include <sqllib.h>


/*
 *  FUNCTION: sqlDumpResults
 *
 *  PURPOSE:  Dump the given result set to the current log file.
 *
 *  RETURNS:  void
 */

void sqlDumpResults(mocaDataRes *res)
{
    long colCount,              /* Number of columns in select list */
	 colNumber,             /* Select list column number        */
	 rowCount,              /* Number of rows in result set     */
	 rowNumber;             /* Row number of result set         */

    mocaDataRow *row;		/* Pointer to the result set row    */

    /* Make sure the result set isn't null. */
    if (!res)
        return;

    misTrc(T_SQL, "---------------------------------");

    /* Give ourselves nicer aliases to work with. */
    colCount = res->NumOfColumns;
    rowCount = res->NumOfRows;

    /* Dump summary information about this result set. */
    misTrc(T_SQL, "Dumping result set...");
    misTrc(T_SQL, "Address: %p", res);
    misTrc(T_SQL, "Rows   : %d", rowCount);
    misTrc(T_SQL, "Columns: %d", colCount);
    misTrc(T_SQL, "Legend : N=Name T=Type DL=Defined-Len AL=Actual-Len X=Nullable H=Hidden");

    /* Cycle through each column in the select list. */
    for (colNumber = 0; colNumber < colCount; colNumber++)
    {
	/* Dump result set information about this column. */
        misTrc(T_SQL, "Column %d: N[%s]  T[%c]  DL[%d]  AL[%d]  X[%d]  H[%ld]", 
	       (colNumber+1),
	       res->ColName[colNumber],
	       res->DataType[colNumber],
	       res->DefinedMaxLen[colNumber],
	       res->ActualMaxLen[colNumber],
	       res->Nullable[colNumber],
	       res->Hidden);
    }

    /* 
     *  Some DDL will populate the number of rows in the result set,
     *  but there won't be any data available.
     */
    if (!res->Data)
    {
	misTrc(T_SQL, "There is no data for this result set");
	goto cleanup;
    }

    /* Cycle through each row in the result set. */
    for (rowNumber = 0, row = res->Data; 
	 rowNumber < rowCount; 
	 rowNumber++, row = row->NextRow)
    {

	misTrc(T_SQL, "*** Row %d ***", (rowNumber+1));

        /* Cycle through each column in the select list. */
        for (colNumber = 0; colNumber < colCount; colNumber++)
	{

	    /* Deal with null values here. */
	    if (! row->DataPtr[colNumber] && row->NullInd[colNumber])
	    {
	        misTrc(T_SQL, "\tColumn %d: [%s]", (colNumber+1), "(null)");
		continue;
	    }

	    /* Deal with hidden values here. */
	    if (res->Hidden)
	    {
	        misTrc(T_SQL, "\tColumn %d: [%s]", (colNumber+1), "(hidden)");
		continue;
	    }

	    /* Deal with non-null values here. */
	    switch (res->DataType[colNumber])
	    {
		case COMTYP_INT:
		case COMTYP_LONG:
		case COMTYP_LONGPTR:
		case COMTYP_BOOLEAN:
	            misTrc(T_SQL, "\tColumn %d: [%ld]", 
		           (colNumber+1), 
		           * (long *) (row->DataPtr[colNumber]));
		    break;

		case COMTYP_FLOAT:
		case COMTYP_FLOATPTR:
	            misTrc(T_SQL, "\tColumn %d: [" MOCA_FLT_FMT "] (Float)", 
		           (colNumber+1), 
		           * (double *) row->DataPtr[colNumber]);
		    break;

                case COMTYP_TEXT:
                case COMTYP_CHAR:
                case COMTYP_CHARPTR:
                case COMTYP_DATTIM:
	            misTrc(T_SQL, "\tColumn %d: [%s]", 
			   (colNumber+1), 
		           row->DataPtr[colNumber]);
		    break;

                case COMTYP_RESULTS:
	            misTrc(T_SQL, "\tColumn %d: [%p] (Results Pointer)", 
		           (colNumber+1),
			   * (mocaDataRes **) row->DataPtr[colNumber]);
                    misTrc(T_SQL, "Dumping child result set...");
		    sqlDumpResults(* (mocaDataRes **) row->DataPtr[colNumber]);
                    misTrc(T_SQL, "Dumped child result set");
		    break;

                case COMTYP_GENERIC:
	            misTrc(T_SQL, "\tColumn %d: [%p] (Pointer)", 
		           (colNumber+1),
			   * (mocaDataRes **) row->DataPtr[colNumber]);
		    break;

                default:
	            misTrc(T_SQL, "\tColumn %d: [Can't Display]", 
		           (colNumber+1));
		    break;

            }
        }

    }

cleanup:

    misTrc(T_SQL, "---------------------------------");

    return;
}
