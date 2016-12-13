static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Used to all a value to a mocaDataRow
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

#include <moca.h>

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <mocagendef.h>
#include <mocaerr.h>
#include <sqllib.h>

/*
 * These two functions are used to calculate the size of an integer or floating point
 * number. 
 */
static int intlen(long longval)
{
    register int len;
    register long tmpval;
    len = (longval <= 0L) ? 1 : 0;

    for (tmpval = longval; tmpval; tmpval /= 10, len++)
        ;
    return len;
}

static int floatlen(double floatval)
{   
    char buf[100];
    return sprintf(buf, MOCA_FLT_FMT, floatval);
}

/*
 * sqlAddRowItem - this function is used to add a value to
 *                 an mocaDataRow.  Additionally, it will 
 *                 update the ActualMaxLen on the parent
 *                 Ifc if needed.  Memory is allocated to
 *                 hold the data value...
 *
 */
long sql_AddRowItem(mocaDataRes *res,
		    mocaDataRow *row, 
		    long ColNum, 
		    long v_size,
		    void *v_addr)
{
    long  len;

    if (row->DataPtr[ColNum]) 
	free(row->DataPtr[ColNum]);

    row->DataPtr[ColNum] = (void *) calloc(1, v_size+1);
    if (!row->DataPtr[ColNum])
        return(eNO_MEMORY);

    switch(res->DataType[ColNum]) 
    {
    case COMTYP_LONG:
    case COMTYP_INT:
	len = v_addr ? intlen(*(long *)v_addr) : 0;

        if (len > (long) res->ActualMaxLen[ColNum]) 
	{
            res->ActualMaxLen[ColNum] = len;
            res->DefinedMaxLen[ColNum] = res->ActualMaxLen[ColNum];
        }

        break;

    case COMTYP_FLOAT:
	len = v_addr ? floatlen(*(double *)v_addr) : 0;

        if (len > (long) res->ActualMaxLen[ColNum]) 
	{
            res->ActualMaxLen[ColNum] = len;
            res->DefinedMaxLen[ColNum] = res->ActualMaxLen[ColNum];
        }
        
	break; 
	
    case COMTYP_BOOLEAN: 
	if (1 > res->ActualMaxLen[ColNum]) 
	{ 
	    res->ActualMaxLen[ColNum] = 1; 
	    res->DefinedMaxLen[ColNum] = res->ActualMaxLen[ColNum]; 
	}

        break;
        
    case COMTYP_RESULTS:
	res->ActualMaxLen[ColNum] = v_size;
	res->DefinedMaxLen[ColNum] = v_size;

	if (v_addr)
	    (*(mocaDataRes **)v_addr)->RefCount++;

	break;

    case COMTYP_JAVAOBJ:
	res->ActualMaxLen[ColNum] = v_size;
	res->DefinedMaxLen[ColNum] = v_size;

	if (v_addr)
	    (*(mocaObjectRef **)v_addr)->RefCount++;

	break;

    default:
        if (v_size > res->ActualMaxLen[ColNum])
            res->ActualMaxLen[ColNum] = v_size;
        if (v_size > res->DefinedMaxLen[ColNum]
            && res->DefinedMaxLen[ColNum] > 0)
            res->DefinedMaxLen[ColNum] = v_size;
    }

    /* Deal with a null value correctly. */
    if (v_addr)
    {
        memcpy(row->DataPtr[ColNum], v_addr, v_size);
	row->NullInd[ColNum] = 0;
    }
    else
    {
	free(row->DataPtr[ColNum]);
	row->DataPtr[ColNum] = NULL;
	row->NullInd[ColNum] = 1;
    }

    return(eOK);
}
