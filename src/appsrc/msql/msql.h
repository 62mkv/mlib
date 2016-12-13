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
#include <mocagendef.h>
#include <mcclib.h>
#include <srvlib.h>

#ifdef MAIN
#  define STORAGE_CLASS 
#else
#  define STORAGE_CLASS extern
#endif

#define APPNAME	"MSQL"

/*
 * Global Variables
 */

STORAGE_CLASS int gAutoCommit;
STORAGE_CLASS int gServerMode;
STORAGE_CLASS int gInstallMode;
STORAGE_CLASS int gSingleLineMode;
STORAGE_CLASS int gQuit;

/*
 * Function Prototypes
 */

/* commands.c */
void DescribeTable(char *name);
int ExecuteShellCommand(char *command);

/* connection.c */
long Connect(char *url, char *userid, char *password, char *clientKey);
void Close(void);
long ExecuteCommand(char *command, RETURN_STRUCT **ret);
void Commit(void);
void Rollback(void);
void SetAutoCommit(long on);
void SetEnvironment(char *env);

/* edit.c */
int EditCommand(int index);

/* history.c */
char *GetHistory(int pos);
void  PutHistory(char *command);
void  ListHistory(int start, int count);
void  FreeHistory(void);

/* print.c */
void Print(char *format, ...);
void PrintPrompt(long linenum);
void PrintStartBanner(void);
void PrintStatusStart(void);
void PrintStatusDone(RETURN_STRUCT *ret);
void PrintHeadings(RETURN_STRUCT *ret);
void PrintResults(RETURN_STRUCT *ret);
void PrintRowsAffected(RETURN_STRUCT *ret);

/* process.c */
long ProcessInput(FILE *fp, char *url, char *userid, char *password, char *clientKey, int prompt);

/* read.c */
void SetEchoMode(int on);
long ReadInput(char *command, int maxlen, FILE *fp);

/* script.c */
long ProcessScript(char *pathname);

/* spool.c */
void StartSpooling(char *filename);
void StopSpooling(void);
void Spool(char *buffer);
long IsSpooling(void);
