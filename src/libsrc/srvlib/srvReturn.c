static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions for returning data from the server and preparing
 *               results to return
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
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <ctype.h>

#include <mocaerr.h>
#include <mislib.h>
#include <sqllib.h>

#include "srvprivate.h"

#define MAX_COL_LEN 2000

static mocaDataRes *ColInfo;

static long TrimReturnStrings = 1;

/*
 *  This routine is used to setup a column info
 *  structure.  This structure is later used
 *  by srvSetColName and by srvSetupReturn to
 *  get the correct information back to the
 *  client.
 *
 *  Notice that we will always be one "free"
 *  behind.  That is, we allocate memory for
 *  an Res structure, but never free it until
 *  the next time we come back into this routine.
 *  It is *not* the caller's responsibility
 *  to free the memory.  This isn't so bad as we
 *  will still be taking up less memory than if
 *  this were some global static array.
 *
 */
long srvSetupColumns(long NumOfColumns)
{
    if (ColInfo)
    {
        sql_FreeHeaderOnly(ColInfo);
        ColInfo = NULL;
    }

    ColInfo = sql_AllocateResultHdr(NumOfColumns);
    if (!ColInfo)
        return eNO_MEMORY;

    ColInfo->NumOfColumns = NumOfColumns;

    return eOK;
}


/*
 *  Sets the return status in the RETURN_STRUCT
 *
 *  This is a convenience routine used by routines to
 *  set the status after having called the srvSetupReturn
 *  function...
 */
void srvSetReturnStatus(RETURN_STRUCT *ret, long status)
{
    ret->Error.Code       = status;
    /*
     * SERIOUS HACK.
     *
     * As a bridge to the next release, we'll be using the obsolete
     * "Header" attribute to populate the caught error code.
     *
    ret->Error.CaughtCode = status;
    */
    ret->Header = (char *) status;

    return;
}

/*
 * Returns the return status from the RETURN_STRUCT
 */
long srvGetReturnStatus(RETURN_STRUCT *ret)
{
    if (ret)
        return ret->Error.Code;
    else
        return eERROR;
}

mocaDataRes *srvGetResults(RETURN_STRUCT *ret)
{
    if (ret)
        return ret->ReturnedData;
    else
        return NULL;
}

/*
 *  Sets the name of the column in the Module specific ColInfo
 *  mocaDataRes structure.  This will be used by srvSetupReturn.
 *
 *  This is a convenience routine for "higher level" libraries
 *  such as intlib.  In order to preserve, as close as possible,
 *  the original code in intlib, this convenience function calls
 *  the sqllib routine with a set parameter.  Other functions
 *  in srvlib actually call the sqlSetColName function directly.
 *
 */
char srvSetColName(long number, char *name, char datatype, long length)
{
    long status;

    if (ColInfo->ColName[number-1])
        free(ColInfo->ColName[number-1]);

    status = sql_SetColName(ColInfo,
                number - 1,
                name,
                datatype,
                length);


    return datatype;
}

/*
 *  Copy column information from the res structure which
 *  was setup by the caller to the res structure which is
 *  used to build the results set...
 *
 */
long srvCopyColInfo(mocaDataRes *res)
{
    int ii;
    char tempstring[80];
    long status = eOK;

    for (ii = 0; ii < res->NumOfColumns; ii++)
    {
        if (ColInfo->ColName[ii] && strlen(ColInfo->ColName[ii]))
        {
            status = sql_SetColName(res,
                                    ii,
                                    ColInfo->ColName[ii],
                                    ColInfo->DataType[ii],
                                    ColInfo->DefinedMaxLen[ii]);
        }
        else
        {
            sprintf(tempstring, "COL%d", ii);
            status = sql_SetColName(res,
                                    ii,
                                    tempstring,
                                    ColInfo->DataType[ii],
                                    ColInfo->DefinedMaxLen[ii]);
        }
    }

    return status;
}

/*
 *  Build a return structure for data to be returned to
 *  the client.
 *
 */
RETURN_STRUCT *srvSetupReturn(long status, char *DataTypes,...)
{
    va_list args;
    int       i;
    long tempLong;
    long ret_status;
    char *tempString, *ptr;
    void *tempAddr;
    double tempDouble;
    RETURN_STRUCT *ret;
    mocaDataRes *res;
    mocaDataRow *row_ptr = 0;

    va_start(args, DataTypes);

    ret = (RETURN_STRUCT *) calloc (1, sizeof(RETURN_STRUCT));
    if (ret == NULL)
        return NULL;

    if ((ret->DataTypes = calloc(strlen(DataTypes) + 1, sizeof(char))) == NULL)
        return NULL;

    strcpy(ret->DataTypes, DataTypes);

    res = sql_AllocateResultHdr(strlen(DataTypes));
    strncpy(res->DataType, DataTypes, strlen(DataTypes));

    ret->ReturnedData = res;
    if (ret->ReturnedData == NULL)
        return NULL;

    ret_status = srvCopyColInfo(res);
    if (ret_status != eOK)
        return NULL;

    ret->Error.Code       = status;
    /*
     * SERIOUS HACK.
     *
     * As a bridge to the next release, we'll be using the obsolete
     * "Header" attribute to populate the caught error code.
     *
    ret->Error.CaughtCode = status;
    */
    ret->Header = (char *) status;

    if (strlen(DataTypes))
    {
        ret->rows = 1;
        res->NumOfRows = 1;
        row_ptr = sql_AllocateRow(res);
        if (!row_ptr)
            return NULL;
    }

    for (i = 0; i < (long) strlen(DataTypes); i++)
    {
        /* Now fill in data.... */

        switch (DataTypes[i])
        {
        case COMTYP_STRING:
        case COMTYP_DATTIM:

            ptr = (char *)va_arg(args, char *);
            tempString = (char *)calloc(1, strlen(ptr)+1);
            if (!tempString)
            {
                va_end(args);
                return NULL;
            }
            strcpy(tempString, ptr);
            srvTrim(tempString);

            /* Place the value in the Res structure...  */
            ret_status = sql_AddRowItem(res,
                                        row_ptr,
                                        i,
                                        strlen(tempString),
                                        tempString);

            if (ret_status != eOK)
            {
                va_end(args);
                free(tempString);
                return NULL;
            }
            free(tempString);
            break;

        case COMTYP_BOOLEAN:
        case COMTYP_INT:
        case COMTYP_LONG:

            tempLong = va_arg(args, long);
    
            ret_status = sql_AddRowItem(res,
                                        row_ptr,
                                        i,
                    sizeof(long),
                    &tempLong);
            if (ret_status != eOK)
            {
                va_end(args);
                return NULL;
            }

            break;

        case COMTYP_FLOAT:

            tempDouble = va_arg(args, double);

            ret_status = sql_AddRowItem(res,
                                        row_ptr,
                                        i,
                                        sizeof(double),
                                        &tempDouble);
            if (ret_status != eOK)
            {
                va_end(args);
                return NULL;
            }
            break;

        case COMTYP_GENERIC:
        case COMTYP_RESULTS:
        case COMTYP_JAVAOBJ:
            tempAddr = va_arg(args, void *);

            ret_status = sql_AddRowItem(res,
                                        row_ptr, 
					i,
                                        sizeof(void *),
                                        &tempAddr);
            if (ret_status != eOK)
            {
                va_end(args);
                return NULL;
            }
        break;

        case COMTYP_BINARY:
            tempAddr = va_arg(args, void *);

            ret_status = sql_AddRowItem(res,
                                        row_ptr,
                                        i,
                                        res->DefinedMaxLen[i],
                                        tempAddr);
            if (ret_status != eOK)
            {
                va_end(args);
                return NULL;
            }
            break;
        }
    }

    va_end(args);

    return ret;
}

mocaDataRow *srvAddToReturn(RETURN_STRUCT *ret,
                            mocaDataRow *LastSet,
                            char *DataTypes,...)
{
    va_list args;
    int       i;
    long ret_status;
    long tempLong;
    char *tempString, *ptr;
    double tempDouble;
    void *tempAddr;
    static mocaDataRow *next;

    va_start(args, DataTypes);

    next = sql_AllocateRow(ret->ReturnedData);
    if (next == NULL)
        return NULL;

    LastSet->NextRow = next;
    LastSet = next;

    for (i = 0; i < (long) strlen(DataTypes); i++)
    {
        switch (DataTypes[i])
        {
        case COMTYP_STRING:
        case COMTYP_DATTIM:

            ptr = va_arg(args, char *);
            tempString = (char *)calloc(1, strlen(ptr)+1);
            if (!tempString)
            {
                va_end(args);
                return NULL;
            }
            strcpy(tempString, ptr);
            srvTrim(tempString);
            ret_status = sql_AddRowItem(ret->ReturnedData,
                                        next,
                                        i,
                                        strlen(tempString),
                                        tempString);
            if (ret_status != eOK)
            {
                free(tempString);
                va_end(args);
                return NULL;
            }
            free(tempString);
            break;

        case COMTYP_BOOLEAN:
        case COMTYP_INT:
        case COMTYP_LONG:

            tempLong = va_arg(args, long);
            ret_status = sql_AddRowItem(ret->ReturnedData,
                                        next,
                                        i,
                                        sizeof(long),
                                        &tempLong);
            if (ret_status != eOK)
            {
                va_end(args);
                return NULL;
            }
            break;

        case COMTYP_FLOAT:

            tempDouble = va_arg(args, double);
            ret_status = sql_AddRowItem(ret->ReturnedData,
                                        next,
                                        i,
                                        sizeof(double),
                                        &tempDouble);
            if (ret_status != eOK)
            {
                va_end(args);
                return NULL;
            }
            break;

        case COMTYP_GENERIC:
            tempAddr =   va_arg(args, void *);
    
            ret_status = sql_AddRowItem(ret->ReturnedData,
                                        next,
                                        i,
                                        sizeof(void *),
                                        &tempAddr);
            if (ret_status != eOK)
            {
                va_end(args);
                return NULL;
            }
            break;
    
        case COMTYP_BINARY:
            tempAddr = va_arg(args, void *);
    
            ret_status = sql_AddRowItem(ret->ReturnedData,
                                        next,
                                        i,
                                        ret->ReturnedData->DefinedMaxLen[i],
                                        tempAddr);
            if (ret_status != eOK)
            {
                va_end(args);
                return NULL;
            }
            break;
        }
    }
    ret->rows++;

    va_end(args);

    return next;
}

char *srvTrim(char *string)
{
    char *eptr, *ptr;

    if (TrimReturnStrings && string)
    {
        for (eptr = ptr = string; *ptr; ptr++)
	{
            if (!isspace((unsigned char)*ptr)) 
		eptr = ptr + 1;
        }

        *eptr = '\0';
    }

    return string;
}

long srvTrimLen(char *string, long max_len)
{
    long i, endpos;
    char *ptr;

    if (!string) return 0;

    for (i = endpos = 0, ptr = string; i < max_len && *ptr; ptr++, i++)
    {
        if (*ptr != ' ')
	    endpos = i + 1;
    }

    if (!TrimReturnStrings)
        return i;
    else
	return endpos;
}

long srvGetServerTrim(void)
{
    return TrimReturnStrings;
}

void srvSetServerTrimOn(void)
{
    TrimReturnStrings = 1;
}

void srvSetServerTrimOff(void)
{
    TrimReturnStrings = 0;
}
