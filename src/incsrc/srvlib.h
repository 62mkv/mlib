/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file for srvlib.
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

#ifndef SRVLIB_H
#define SRVLIB_H

#include <moca.h>

#include <mocagendef.h>
#include <common.h>
#include <oslib.h>

/*
 * Argument Type Definitions
 */

#define ARGTYP_FLAG     'O'         /* COMTYP_BOOLEAN */
#define ARGTYP_INT      'I'         /* COMTYP_INT     */
#define ARGTYP_FLOAT    'F'         /* COMTYP_FLOAT   */
#define ARGTYP_STR      'S'         /* COMTYP_STRING  */
#define ARGTYP_POINTER  'P'         /* COMTYP_POINTER */
#define ARGTYP_RESULTS  'R'         /* COMTYP_RESULTS */
#define ARGTYP_JAVAOBJ  'J'         /* COMTYP_JAVAOBJ */
#define ARGTYP_BINARY   'V'         /* COMTYP_BINARY */

/*
 *  Definition for srvFreeMemory( )
 */

#define SRVRET_STRUCT  1
#define SRVCMP_STRUCT  2

/*
 * Where Clause Operator Definitions
 */

#define OPR_NONE       0
#define OPR_NOTNULL    1
#define OPR_ISNULL     2
#define OPR_EQ         3
#define OPR_NE         4
#define OPR_LT         5
#define OPR_LE         6
#define OPR_GT         7
#define OPR_GE         8
#define OPR_LIKE       9
#define OPR_RAWCLAUSE  10
#define OPR_REFALL     11
#define OPR_REFONE     12
#define OPR_REFLIKE    13
#define OPR_NOTLIKE    14
#define OPR_NAMECLAUSE 15

/*
 *  Expression Operator Definitions
 */

#ifdef MOCA_PRIVATE
# define OPR_PLUS    20
# define OPR_MINUS   21
# define OPR_TIMES   22
# define OPR_DIV     23
# define OPR_CONCAT  24
# define OPR_MOD     25

# define OPR_AND     30
# define OPR_OR      31
# define OPR_NOT     32
#endif /* MOCA_PRIVATE */

/*
 *  Server Error Argument Type Definition
 */

typedef struct srv__ErrorArg SRV_ERROR_ARG;

/*
 *  Server Argument List Type Definition
 */

typedef struct srv__argslist SRV_ARGSLIST;

/*
 *  Return Structure Type Definition
 */

typedef struct srv__ReturnStruct RETURN_STRUCT;

/*
 * Compiled Command Type Definition
 */

typedef struct srv__CompiledCommand SRV_COMPILED_COMMAND;

#ifdef MOCA_PRIVATE

struct srv__ReturnStruct
{
    struct
    {
    long Code;
    /*
     *  SERIOUS HACK.
     *
     *  As a bridge to the next release, we'll be using the obsolete
     *  "Header" attribute to populate the caught error code.
     *
    long CaughtCode;
    */
    char *DefaultText;
    SRV_ERROR_ARG *Args;
    } Error;
    char *DataTypes;
    long rows;
    char *Header;
    mocaDataRes *ReturnedData;
};

/*
 *  Error Argument Type Definition
 */

struct srv__ErrorArg
{
    char varnam[ARGNAM_LEN + 1];
    char type;
    int  lookup;
    union
    {
    long ldata;
    double fdata;
    char *cdata;
    } data;
    struct srv__ErrorArg *next;
};

#endif /* MOCA_PRIVATE */

/*
 *  Results List Type Definition
 */

typedef struct srv__ResultsList SRV_RESULTS_LIST;

#ifdef MOCA_PRIVATE

struct srv__ResultsList
{
    long numcols;

    /* First the data about the column */
    char colname[ARGNAM_LEN+1];
    long size;
    char type;

    /* Then the data about the data */
    char nullind;
    union
    {
    long ldata;
    double fdata;
    char *cdata;
    void *vdata;
    } data;
};

/*
 *  Function Pointer Definition
 */

typedef RETURN_STRUCT *(*PFVP) ();

#endif

/*
 *  Function Prototypes
 */

#if defined (__cplusplus)
extern "C" {
#endif

/* srvlib.c */
void srvRequestKeepalive(char *id, void (*func)(void));
void srvReleaseKeepalive(char *id);
void srvResetKeepalive(void);

/* srvAddColumn.c */
long srvAddColumn(RETURN_STRUCT *ret, char *column, char datatype, long length);

/* srvAddColumnWithValue.c */
long srvAddColumnWithValue(RETURN_STRUCT *ret,
               char *column,
               char datatype,
                       long length,
               void *value);

/* srvAddRow.c */
long srvAddRow(RETURN_STRUCT *ret);

/* srvAddSQLResults.c */
RETURN_STRUCT *srvAddSQLResults(mocaDataRes *res, long status);

/* srvBuildCompareString.c */
long srvBuildCompareString(char *in, char *out);

/* srvBuildResultsList.c */
long srvBuildResultsList(SRV_RESULTS_LIST *reslist,
                         long colNum,
                     char *name,
             char dtype,
             long len,
             int nullind,
             ...);
void srvFreeResultsList(SRV_RESULTS_LIST *list);

SRV_RESULTS_LIST *srvCreateResultsList(long ncols);

/* srvCombineMocaDataRes.c */
long srvCombineMocaDataRes(mocaDataRes **res1, mocaDataRes **res2);

/* srvCombineResults.c */
long srvCombineResults(RETURN_STRUCT **ret1, RETURN_STRUCT **ret2);

/* srvCommit.c */
long srvCommit(void);

/* srvCompileCommand.c */
long srvCompileCommand(char *command, SRV_COMPILED_COMMAND **exec);

/* srvConcatenateResults.c */
long srvConcatenateResults(RETURN_STRUCT **target, mocaDataRes *source);

/* srvConcatenateRow.c */
long srvConcatenateRow(RETURN_STRUCT **ret, mocaDataRes *res, mocaDataRow *row);

/* srvEnumerateArgList.c */
long srvEnumerateArgList(SRV_ARGSLIST **ctx,
             char *name,
             int *oper,
                     void **data,
             char *dtype);
long srvEnumerateAllArgs(SRV_ARGSLIST **ctx,
             char *name,
             int *oper,
                     void **data,
             char *dtype);
void srvFreeArgList(SRV_ARGSLIST *ctx);

/* srvErrorMessage.c */
char *srvErrorMessage(int ErrorNumber);

/* srvErrorResults.c */
RETURN_STRUCT *srvErrorResults(long status, char *fmt, ...);
void srvErrorResultsAddArg(RETURN_STRUCT *ret, char *name, int type, ...);
long srvErrorResultsAdd(RETURN_STRUCT *ret, long status, char *fmt, ...);

/* srvExecuteAfterCommitRollback.c */
void srvExecuteAfterCommit(void (*function)(void *data), void *data);
void srvExecuteAfterRollback(void (*function)(void *data), void *data);

/* srvFreeMemory.c */
long srvFreeMemory(int type, void *ptr);

/* srvFreeReturnStruct.c */
long srvFreeReturnStruct(RETURN_STRUCT *ptr);

/* srvGetNeededElement.c */
long srvGetNeededElement(char *name, char *alias, char *dtype, void **data);

long srvGetNeededElementWithLength(char *name,
                   char *alias,
                   char *dtype,
                                   void **data,
                   long *length);
long srvGetNeededElementWithOper(char *name,
                   char *alias,
                   char *dtype,
                                   void **data,
                   int *oper);

/* Note, this is used by RPWriter from VB. Needs to be MOCAEXPORT */
long MOCAEXPORT srvGetNeededElementOper(char *name,
                             char *alias, int *oper);

/* srvInitialize.c */
long srvInitialize(char *name, long singleThreaded);

/* srvInitiateCommand.c */
long srvInitiateCommand(char *cmd, RETURN_STRUCT **ret);
long srvInitiateCommandFormat(RETURN_STRUCT **ret, char *format, ...);

/* srvInitiateCompiled */
long srvInitiateCompiled(SRV_COMPILED_COMMAND *compiled, mocaBindList *bindList,
                         RETURN_STRUCT **ret, short useContext);

/* srvInitiateInline.c */
long srvInitiateInline(char *cmd, RETURN_STRUCT **ret);
long srvInitiateInlineFormat(RETURN_STRUCT **ret, char *format, ...);

/* srvMakeSessionKey.c */
char *srvMakeSessionKey(char *userid, char *output, long size);

/* srvMoca.c */
long srvMoca_InitConnection(char *host, char *port, char *env);
long srvMoca_Execute(char *cmd, mocaDataRes **res);
void srvMoca_CloseConnection(void);

/* srvRegisterData.c */
void srvRegisterData(void *ptr, void (*destructor)(void *));
void srvUnregisterData(void *ptr);

/* srvResults.c */
RETURN_STRUCT *srvResults(long status, ...);

/* srvResultsAdd.c */
long srvResultsAdd(RETURN_STRUCT *ret, ...);



/* srvResultsAddList.c */
long srvResultsAddList(RETURN_STRUCT *ret, SRV_RESULTS_LIST *list);

/* srvResultsErrorArg.c */
long srvResultsErrorArg(RETURN_STRUCT *ret, char *varnam, char type, ...);

/* srvResultsInit.c */
RETURN_STRUCT *srvResultsInit(long status, ...);

/* srvResultsInitList.c */
RETURN_STRUCT *srvResultsInitList(long status,
                  int ncols,
                  SRV_RESULTS_LIST *list);

/* srvResultsList.c */
RETURN_STRUCT *srvResultsList(long status, int ncols, SRV_RESULTS_LIST *list);

/* srvResultsMessage.c */
char *srvResultsMessage(RETURN_STRUCT *ret);

/* srvResultsNull.c */
long srvResultsNull(RETURN_STRUCT *ret, ...);

/* srvReturn.c */
RETURN_STRUCT *srvSetupReturn(long status, char *dtypes, ...);

void srvSetReturnStatus(RETURN_STRUCT *ret, long status);
long srvGetReturnStatus(RETURN_STRUCT *ret);

mocaDataRes *srvGetResults(RETURN_STRUCT *ret);
mocaDataRow *srvAddToReturn(RETURN_STRUCT *ret,
                mocaDataRow *row,
                char *dtypes,
                ...);

long srvSetupColumns(long ncols);
char srvSetColName(long colnum, char *name, char dtype, long len);
long srvCopyColInfo(mocaDataRes *res);

char *srvTrim(char *string);
long  srvTrimLen(char *string, long maxlen);

long srvGetServerTrim(void);
void srvSetServerTrimOn(void);
void srvSetServerTrimOff(void);

/* srvRollback.c */
long srvRollback(void);

/* srvSortResultSet.c */
long srvSortResultSet(mocaDataRes *res, char *sortList);

/* srvSwapColumns.c */
long srvSwapColumns(RETURN_STRUCT *ret, long src, long dest);

/* srv_AppHooks.c */
void srvHookInterpretEnvironment(void (*f)(char *));
void srvHookAuthenticateSession(int (*f)(void));
void srvHookUpdateActivity(void (*f)(void));
void srvHookLogUsage(void (*f)(char *, char *, char *, long));
void srvHookGetColumnDesc(void (*f)(long, char **, char **, char **));
void srvHookErrorMessage(char *(*f)(long));
void srvHookTranslateMessage(char *(*f)(char *));
void srvHookCheckLicense(long (*f)(void));
void srvHookCleanupConnection(void (*f)(void));
void srvHookPrecommit(long (*f)(void));
void srvHookPostcommit(long (*f)(void));
void srvHookRollback(long (*f)(void));
void srvHookPreReturnResults(long (*f)(char *,RETURN_STRUCT *));

#if defined (__cplusplus)
}
#endif

#endif

