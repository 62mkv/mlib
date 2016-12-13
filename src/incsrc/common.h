/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file for common definitions and types.
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

#ifndef COMMON_H
#define COMMON_H

#include <moca.h>

/*
 *  Execution Behavior Flag Definitions
 */

#define EXEC_NOCOMMIT      0x01
#define EXEC_ASCII_COMM    0x02
#define EXEC_LICENSE_CHECK 0x04
#define EXEC_KEEPALIVE     0x08
#define EXEC_REMOTE        0x10

/*
 *  Communication Datatype Definitions
 */

#define COMTYP_INT	 'I'		/* Integer            */
#define COMTYP_LONG	 'L'		/* Long Integer       */
#define COMTYP_FLOAT	 'F'		/* Floating Point     */
#define COMTYP_DATTIM	 'D'		/* Date and Time      */
#define COMTYP_CHAR	 'S'		/* String             */
#define COMTYP_STRING	 COMTYP_CHAR
#define COMTYP_TEXT	 'T'		/* Text               */
#define COMTYP_BINARY	 'V'		/* Binary Data        */
#define COMTYP_GENERIC	 'G'		/* Generic Pointer    */
#define COMTYP_POINTER	 COMTYP_GENERIC /* Generic Pointer    */
#define COMTYP_BOOLEAN   'O'            /* Boolean            */
#define COMTYP_RESULTS   'R'		/* Encoded Result Set */
#define COMTYP_JAVAOBJ	 'J'		/* Java Object Reference */

#define COMTYP_NULL_MASK 0x20

/*
 *  Bind Variable Datatype Definitions
 */

#define COMTYP_LONGPTR	 'P'		/* Pointer To Long Integer */
#define COMTYP_FLOATPTR	 'X'		/* Pointer To Double       */
#define COMTYP_CHARPTR	 'Z'		/* Pointer To String       */

/*
 *  Data Row Type Definition
 */

typedef struct moca_DataRow mocaDataRow;

#ifdef MOCA_PRIVATE
struct moca_DataRow
{
    short *NullInd;
    void **DataPtr;
    struct moca_DataRow *NextRow;
};
#endif

/*
 *  Data Result Set Type Definition
 */

typedef struct moca_DataRes mocaDataRes;

#ifdef MOCA_PRIVATE
struct moca_DataRes
{
    char **ColName;
    char **ShortDescription;
    char **LongDescription;
    char *DataType;      /* A dynamic array of chars */
    char *Message;
    long *ActualMaxLen;  /* Actual max of data in column 
				for the returned set....  */
    long *DefinedMaxLen; /* Defined maximum length in
				the database  */
    long *Nullable;      /* 1 = Column can be NULL, 0 = Not null */
    long Hidden;
    long NumOfRows;
    long NumOfColumns;
    long RefCount;
    unsigned long *HashValue;
    struct moca_DataRow *Data;
    struct moca_DataRow *LastRow;
};
#endif

/*
 *  Bind List Type Definition
 */

typedef struct moca_BindList mocaBindList;

#ifdef MOCA_PRIVATE
struct moca_BindList
{
    char name[33];
    char dtype;
    long size;
    void *data;
    short nullind;
    struct moca_BindList *next;
};
#endif

/*
 *  Object Reference Definition
 */

typedef struct moca_ObjectRef mocaObjectRef;

#ifdef MOCA_PRIVATE
struct moca_ObjectRef
{
    void *data;
    long RefCount;
    void (*destructor)(void *);
};
#endif

/*
 *  Boolean Type Definition
 */

typedef long moca_bool_t;

#endif
