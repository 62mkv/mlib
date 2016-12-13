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

#ifndef SQLLIB_H
#define SQLLIB_H

#include <moca.h>

#include <stdarg.h>

#include <common.h>
#include <dblib.h>

/*
 *  Function Remapping Definitions
 */

#define sqlCommit              dbCommit
#define sqlExecStr             dbExecStr
#define sqlExecStrFormat       dbExecStrFormat
#define sqlRollback            dbRollback
#define sqlErrorNumber         dbErrorNumber
#define sqlErrorText           dbErrorText
#define sqlExecBind            dbExecBind
#define sqlExecBindParse       dbExecBindParse
#define sqlSetSavepoint        dbSetSavepoint
#define sqlRollbackToSavepoint dbRollbackToSavepoint

/*
 *  Function Prototypes
 */

#if defined (__cplusplus)
extern "C" {
#endif

/* sqlAddColumn.c */
long sqlAddColumn(mocaDataRes *res, char *column, char datatype, long length);

/* sqlAddRow.c */
mocaDataRow *sqlAddRow(mocaDataRes *res);

/* sqlBuildBindList.c */
int sqlBuildBindListFromArgs(mocaBindList **Head, va_list args);
int sqlBuildBindList(mocaBindList **Head, ...);

/* sqlCompareTypes */
int sqlCompareTypes(char *t1, char *t2);

/* sqlCombineResults.c */
long MOCAEXPORT sqlCombineResults(mocaDataRes **res1, mocaDataRes **res2);

/* sqlDecodeBinary.c */
void *sqlDecodeBinary(void *encoded);
long  sqlDecodeBinaryLen(void *encoded);

/* sqlDecodeResults.c */
long sqlDecodeResults(char *buffer, mocaDataRes **res);

/* sqlDumpResults.c */
void sqlDumpResults(mocaDataRes *resultSet);

/* sqlEncodeBinary.c */
void *sqlEncodeBinary(void *raw, long length);
long  sqlEncodeBinaryLen(void *encoded);

/* sqlEncodeResults.c */
long sqlEncodeResultsLen(mocaDataRes *res);
long sqlEncodeResults(mocaDataRes *res, char **buffer);

/* sqlFindColumn.c */
long MOCAEXPORT sqlFindColumn(mocaDataRes *res, char *name);

/* sqlFreeBindList.c */
void sqlFreeBindList(mocaBindList *Head);

/* sqlFreeResults.c */
void MOCAEXPORT sqlFreeResults(mocaDataRes *res);

/* sqlGetActualColumnLen.c */
long MOCAEXPORT sqlGetActualColumnLen(mocaDataRes *res, char *name);
long MOCAEXPORT sqlGetActualColumnLenByPos(mocaDataRes *res, long col);

/* sqlGetBinaryData.c */
void * MOCAEXPORT sqlGetBinaryData(mocaDataRes *res, mocaDataRow *row, 
				   char *name);
void * MOCAEXPORT sqlGetBinaryDataByPos(mocaDataRes *res, mocaDataRow *row, 
					long col);

/* sqlGetBinaryDataLen.c */
long MOCAEXPORT sqlGetBinaryDataLen(mocaDataRes *res, mocaDataRow *row, 
				    char *name);
long MOCAEXPORT sqlGetBinaryDataLenByPos(mocaDataRes *res, mocaDataRow *row, 
					 long col);

/* sqlGetBoolean.c */
moca_bool_t MOCAEXPORT sqlGetBoolean(mocaDataRes *res, mocaDataRow *row, 
				     char *name);
moca_bool_t MOCAEXPORT sqlGetBooleanByPos(mocaDataRes *res, mocaDataRow *row, 
					  long col);

/* sqlGetColumnName.c */
char * MOCAEXPORT sqlGetColumnName(mocaDataRes *res, long index);

/* sqlGetDataType.c */
char MOCAEXPORT sqlGetDataType(mocaDataRes *res, char *name);
char MOCAEXPORT sqlGetDataTypeByPos(mocaDataRes *res, long col);

/* sqlGetDate.c */
char * MOCAEXPORT sqlGetDate(mocaDataRes *res, mocaDataRow *row, char *name);
char * MOCAEXPORT sqlGetDateByPos(mocaDataRes *res, mocaDataRow *row, long col);

/* sqlGetDefinedColumnLen.c */
long MOCAEXPORT sqlGetDefinedColumnLen(mocaDataRes *res, char *name);
long MOCAEXPORT sqlGetDefinedColumnLenByPos(mocaDataRes *res, long col);

/* sqlGetFloat.c */
double MOCAEXPORT sqlGetFloat(mocaDataRes *res, mocaDataRow *row, char *name);
double MOCAEXPORT sqlGetFloatByPos(mocaDataRes *res, mocaDataRow *row, 
				   long col);

/* sqlGetLastRow.c */
mocaDataRow * MOCAEXPORT sqlGetLastRow(mocaDataRes *res);

/* sqlGetLong.c */
long MOCAEXPORT sqlGetLong(mocaDataRes *res, mocaDataRow *row, char *name);
long MOCAEXPORT sqlGetLongByPos(mocaDataRes *res, mocaDataRow *row, long col);

/* sqlIsHidden.c */
long MOCAEXPORT sqlIsHidden(mocaDataRes *res, mocaDataRow *row, char *name);
long MOCAEXPORT sqlIsHiddenByPos(mocaDataRes *res, mocaDataRow *row, long col);

/* sqlGetLongDesc.c */
char * MOCAEXPORT sqlGetLongDesc(mocaDataRes *res, char *name);
char * MOCAEXPORT sqlGetLongDescByPos(mocaDataRes *res, long col);

/* sqlGetMessage.c */
char * MOCAEXPORT sqlGetMessage(mocaDataRes *res);

/* sqlGetNextRow.c */
mocaDataRow * MOCAEXPORT sqlGetNextRow(mocaDataRow *row);

/* sqlGetNumColumns.c */
long MOCAEXPORT sqlGetNumColumns(mocaDataRes *res);

/* sqlGetNumRows.c */
long MOCAEXPORT sqlGetNumRows(mocaDataRes *res);

/* sqlGetResultset.c */
mocaDataRes * MOCAEXPORT sqlGetResultset(mocaDataRes *res, mocaDataRow *row, 
					 char *name);
mocaDataRes * MOCAEXPORT sqlGetResultsetByPos(mocaDataRes *res, 
					      mocaDataRow *row, long col);

/* sqlGetRow.c */
mocaDataRow * MOCAEXPORT sqlGetRow(mocaDataRes *res);

/* sqlGetShortDesc.c */
char * MOCAEXPORT sqlGetShortDesc(mocaDataRes *res, char *name);
char * MOCAEXPORT sqlGetShortDescByPos(mocaDataRes *res, long col);

/* sqlGetString.c */
char * MOCAEXPORT sqlGetString(mocaDataRes *res, mocaDataRow *row, char *name);
char * MOCAEXPORT sqlGetStringByPos(mocaDataRes *res, mocaDataRow *row, 
				    long col);

/* sqlGetValue.c */
void * MOCAEXPORT sqlGetValue(mocaDataRes *res, mocaDataRow *row, char *name);
void * MOCAEXPORT sqlGetValueByPos(mocaDataRes *res, mocaDataRow *row, 
				   long col);

/* sqlIsNull.c */
long MOCAEXPORT sqlIsNull(mocaDataRes *res, mocaDataRow *row, char *name);
long MOCAEXPORT sqlIsNullByPos(mocaDataRes *res, mocaDataRow *row, long col);

/* sqlIsNullable.c */
long MOCAEXPORT sqlIsNullable(mocaDataRes *res, char *name);
long MOCAEXPORT sqlIsNullableByPos(mocaDataRes *res, long col);

/* sqlIsPseudoCol.c */
int MOCAEXPORT sqlIsPseudoCol(char *col);

/* sqlLengths.c */
long sqlLengthInt(long value);
long sqlLengthLong(long value);
long sqlLengthBoolean(long value);
long sqlLengthChar(char *value);
long sqlLengthString(char *value);
long sqlLengthDattim(char *value);
long sqlLengthFloat(double value);
long sqlLengthBinary(void *value);

/* sqlRemoveRow.c */
void MOCAEXPORT sqlRemoveRow(mocaDataRes *res, mocaDataRow *row);

/* sqlRenameColumn.c */
long MOCAEXPORT sqlRenameColumn(mocaDataRes *res, char *oldName, char *newName);

/* sqlRenameColumnByPos.c */
long MOCAEXPORT sqlRenameColumnByPos(mocaDataRes *res, long number, 
	                             char *newName);

/* sqlSetDate.c */
long MOCAEXPORT sqlSetDate(mocaDataRes *res, mocaDataRow *row, char *name, 
			   char *value);

/* sqlSetFloat.c */
long MOCAEXPORT sqlSetFloat(mocaDataRes *res, mocaDataRow *row, char *name, 
			    double value);

/* sqlSetLong.c */
long MOCAEXPORT sqlSetLong(mocaDataRes *res, mocaDataRow *row, char *name, 
			   long value);

/* sqlSetString.c */
long MOCAEXPORT sqlSetString(mocaDataRes *res, mocaDataRow *row, char *name, 
			     char *value);

/* sqlSwapColumns.c */
long sqlSwapColumns(mocaDataRes *res, long src, long dest);

/* sqlUnbind.c */
char *sqlUnbind(char *stmt, mocaBindList *BindList);

/*
 *  The following function prototypes are for MOCA internal use only.
 */

/* sql_AddRowItem.c */
long sql_AddRowItem(mocaDataRes *res, mocaDataRow *row, long colNum, 
		    long v_size, void *v_addr);

/* sql_AllocateResultHdr.c */
mocaDataRes *sql_AllocateResultHdr(long numColumns);

/* sql_AllocateRow.c */
mocaDataRow *sql_AllocateRow(mocaDataRes *res);

/* sql_ChangeDataType.c */
long sql_ChangeDataType(mocaDataRes *res, char *name, char dtype);
long sql_ChangeDataTypeByPos(mocaDataRes *res, long number, char dtype);

/* sql_FreeHeaderOnly.c */
void sql_FreeHeaderOnly(mocaDataRes *res);

/* sql_FreeRow.c */
void MOCAEXPORT sql_FreeRow(mocaDataRes *res, mocaDataRow *row);

/* sql_PromoteDataType.c */
long sql_PromoteDataType(mocaDataRes *res, long number, char dtype);
long sql_CompareDataTypes(char dtype1, char dtype2);

/* sql_Set.c */
long sql_SetColName(mocaDataRes *res, long colNum, char *name, char dtype, 
		    long len);
long sql_SetColNullableByPos(mocaDataRes *res,long colindex,moca_bool_t nullable);
long sql_SetColNullable(mocaDataRes *res, char *name, moca_bool_t nullable);

long sql_SetActualColumnLen(mocaDataRes *res, char *name, long length);
void sql_SetFirstRow(mocaDataRes *res, mocaDataRow *row);
void sql_SetLastRow(mocaDataRes *res, mocaDataRow *row);
void sql_SetNextRow(mocaDataRow *origRow, mocaDataRow *newRow,
		    mocaDataRes *origRes, mocaDataRes *newRes);

/* sql_ObjectRef.c */
mocaObjectRef *sql_ObjectRef(void *obj, void (*destructor)(void *));
void sql_FreeObjectRef(mocaObjectRef *ref);
void *sql_GetObjectRef(mocaObjectRef *ref);

#if defined (__cplusplus)
}
#endif

#endif
