/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file containing Oracle datatype definitions.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016-2009
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

#ifndef SQLDATATYPESH2_H
#define SQLDATATYPESH2_H

/*
 * Error Codes
 *
 * NOTE: Some of these are duplicate codes because they are different
 *       in a different database.
 */

#define ERR_CANNOT_DROP_COLUMN_NOT_FOUND    -904
#define ERR_CANNOT_DROP_TABLE_NOT_FOUND     -942
#define ERR_CANNOT_DROP_INDEX_NOT_FOUND    -1418
#define ERR_CANNOT_DROP_SEQUENCE_NOT_FOUND -2289
#define ERR_COLUMN_ALREADY_EXISTS          -1430
#define ERR_COLUMN_ALREADY_NOT_NULL        -1442
#define ERR_COLUMN_ALREADY_HAS_DEFAULT         0
#define ERR_CONSTRAINT_NOT_FOUND           -2443
#define ERR_INDEX_ALREADY_EXISTS      	  -42111
#define ERR_INVALID_COLUMN_NAME             -904
#define ERR_NO_ROWS_AFFECTED               -1403
#define ERR_OBJECT_NOT_FOUND                -942
#define ERR_PRIMARY_KEY_ALREADY_EXISTS    -90017
#define ERR_TABLE_ALREADY_EXISTS          -42101
#define ERR_VIEW_ALREADY_EXISTS           -42101
#define ERR_CONSTRAINT_ALREADY_EXISTS      -2264
#define ERR_COLUMN_ALREADY_ALLOWS_NULLS    -1451
#define ERR_CANNOT_VALIDATE_PK_VIOLATED    -2437

/*
 * Data Types
 */
#define STRING_TY(len)               varchar(len CHAR)

#ifndef DB_USE_NTYPES
#define NSTRING_TY(len)  varchar(len CHAR)
#define NCLOB_TY                     clob
#define NLONG_TY                     clob
#define NLARGECHAR_TY                varchar(4000) 
#else
#define NSTRING_TY(len)  nvarchar(__MOCA([[len > 2000 ? 2000 : len]]))
#define NCLOB_TY                     nclob
#define NLONG_TY                     nclob
#define NLARGECHAR_TY                nvarchar(4000) 
#endif

#define LONG_TY                      long
#define INTEGER_TY                   number(10)
#define SMALLINT_TY                  smallint
#define FLAG_TY                      number(1)
#define DATE_TY                      timestamp 
#define REAL_TY                      number
#define FIXED_INT_TY(x)              number(x)
#define NUMERIC_TY(x,y)              number(x,y)
#define MONEY_TY                     number(19,4)
#define LINEAR_TY                    number(19,4)
#define WEIGHT_TY                    number(23,8)
#define OLD_WEIGHT_TY                number(19,4)
#define PERCENTAGE_TY                number(19,4)
#define VOLUME_TY                    number(23,8)
#define OLD_VOLUME_TY                number(19,4)
#define QVL_TY                       number(19,4)
#define FLOAT_TY                     number(19,4)
#define CONV_FACTOR_TY               number(28,10)
#define DURATION_TY                  number(10,2)
#define LARGECHAR_TY                 varchar(4000)
#define BINARY_OBJECT_TY             long raw 
#define BLOB_TY                      blob
#define CLOB_TY                      clob

#define DBANSI_CHAR(n)               char(n)
#define DBANSI_CHARACTER(n)          char(n)
#define DBANSI_CHAR_VARYING(n)       varchar(n)
#define DBANSI_CHARACTER_VARYING(n)  varchar(n)
#define DBANSI_NUMERIC(p,s)          decimal(p,s)
#define DBANSI_DECIMAL(p,s)          decimal(p,s)
#define DBANSI_INT                   int 
#define DBANSI_INTEGER               int 
#define DBANSI_SMALLINT              smallint 
#define DBANSI_REAL                  float
#define DBANSI_FLOAT	             float
#define DBANSI_DOUBLE_PRECISION      double precision	
#define DBANSI_DATE                  date 

/*
 * Tablespaces
 */

# define CREATE_TABLE_TABLESPACE(name, storage)
# define CREATE_INDEX_TABLESPACE(name, storage)
# define CREATE_UNIQUE_INDEX_TABLESPACE(name, storage)
# define CREATE_CLUSTERED_INDEX_TABLESPACE(name, storage)
# define CREATE_UNIQUE_CLUSTERED_INDEX_TABLESPACE(name, storage)
# define CREATE_PK_CONSTRAINT_TABLESPACE(name, storage)

/*
 * Tables
 */

#define CREATE_TABLE(tblname)  create table tblname

#define DROP_TABLE(tblname)    drop table tblname

/*
 * Column Constraints
 */
	   
#define FLAG_CONSTRAINT(cnsnam, colnam) \
	  constraint cnsnam check (colnam in (1, 0))

/*
 * Indexes
 */

#define CREATE_INDEX_BEGIN(tblname, idxname) \
	  create index idxname on tblname
#define CREATE_INDEX_END

#define CREATE_UNIQUE_INDEX_BEGIN(tblname, idxname) \
          create unique index idxname on tblname
#define CREATE_UNIQUE_INDEX_END

#define CREATE_CLUSTERED_INDEX_BEGIN(tblname, idxname) \
	  create index idxname on tblname
#define CREATE_CLUSTERED_INDEX_CLUSTER
#define CREATE_CLUSTERED_INDEX_END

#define CREATE_UNIQUE_CLUSTERED_INDEX_BEGIN(tblname, idxname) \
	  create unique index idxname on tblname
#define CREATE_UNIQUE_CLUSTERED_INDEX_CLUSTER
#define CREATE_UNIQUE_CLUSTERED_INDEX_END

#define DROP_INDEX(tblname, idxname)                   drop index idxname
#define DROP_UNIQUE_INDEX(tblname, idxname)            drop index idxname
#define DROP_CLUSTERED_INDEX(tblname, idxname)         drop index idxname
#define DROP_UNIQUE_CLUSTERED_INDEX(tblname, idxname)  drop index idxname

/*
 * Primary Keys
 */

#define CREATE_PK_CONSTRAINT_BEGIN(tblname, cnsname) \
          alter table tblname add primary key 
#define CREATE_PK_CONSTRAINT_USING_INDEX 
#define CREATE_PK_CONSTRAINT_END 

#define DROP_PK_CONSTRAINT(tblname, cnsname) \
	  alter table tblname drop primary key

/*
 * Foreign Keys
 */

#define CREATE_FK_CONSTRAINT_BEGIN(tblname, cnsname) \
          alter table tblname add constraint cnsname foreign key 
#define CREATE_FK_CONSTRAINT_REFERENCES(tblname) \
	  references tblname
#define CREATE_FK_CONSTRAINT_ON_DELETE_CASCADE \
	  on delete cascade
#define CREATE_FK_CONSTRAINT_END 

#define DROP_FK_CONSTRAINT(tblname, cnsname) \
	  alter table tblname drop constraint cnsname

/*
 * Views
 */

#define CREATE_VIEW(x)  create or replace view x as

#define DROP_VIEW(x)    drop view x

/*
 * Sequences
 */

#define CREATE_SEQUENCE(w, x, y, z) \
	  create sequence w \
          start with x \
          increment by y \
          cache 10

#define DROP_SEQUENCE(x) \
	  drop sequence x

/*
 * Alters
 */

#define ALTER_TABLE_ADD_COLUMN_BEGIN(tblnam, colnam) \
          alter table tblnam add (colnam
#define ALTER_TABLE_ADD_COLUMN_END \
	  )

#define ALTER_TABLE_DROP_COLUMN_BEGIN(tblnam, colnam) \
          alter table tblnam drop column colnam
#define ALTER_TABLE_DROP_COLUMN_END 

#define ALTER_TABLE_MODIFY_COLUMN_BEGIN(tblnam, colnam) \
          alter table tblnam modify (colnam
#define ALTER_TABLE_MODIFY_COLUMN_END \
	  )

#define ALTER_TABLE_MODIFY_COLUMN_DATATYPE_BEGIN(tblnam, colnam) \
          alter table tblnam modify (colnam
#define ALTER_TABLE_MODIFY_COLUMN_DATATYPE_END \
	  )

#define ALTER_TABLE_ADD_FLAG_CONSTRAINT_BEGIN(tblnam, cnsname, colnam) \
	  alter table tblnam add (constraint cnsname check (colnam in (1, 0))
#define ALTER_TABLE_ADD_FLAG_CONSTRAINT_END \
	  )

#define ALTER_TABLE_DROP_FLAG_CONSTRAINT_BEGIN(tblnam, cnsname, colnam) \
	  alter table tblnam drop constraint cnsname
#define ALTER_TABLE_DROP_FLAG_CONSTRAINT_END

#define ALTER_TABLE_ADD_CHECK_CONSTRAINT_BEGIN(tblnam, cnsname) \
	  alter table tblnam add (constraint cnsname check (
#define ALTER_TABLE_ADD_CHECK_CONSTRAINT_END \
	  ))

#define ALTER_TABLE_DROP_CHECK_CONSTRAINT_BEGIN(tblnam, cnsname) \
	  alter table tblnam drop constraint cnsname
#define ALTER_TABLE_DROP_CHECK_CONSTRAINT_END

#define ALTER_TABLE_DROP_NOT_NULL_CONSTRAINT_BEGIN(tblnam, colnam) \
	  alter table tblnam modify (colnam
#define ALTER_TABLE_DROP_NOT_NULL_CONSTRAINT_END \
	  null)

#define ALTER_TABLE_ADD_DEFAULT_BEGIN(tblnam, colnam, value) \
          alter table tblnam modify colnam default value 
#define ALTER_TABLE_ADD_DEFAULT_END

#define ALTER_TABLE_ADD_DEFAULT_QUOTED_BEGIN(tblnam, colnam, value) \
          alter table tblnam modify colnam default 'value' 
#define ALTER_TABLE_ADD_DEFAULT_QUOTED_END

/*
 * Renames
 */

#define RENAME_TABLE(old, new)          alter table old rename to new
#define RENAME_INDEX(tblnam, old, new)  alter index old rename to new
#define RENAME_COLUMN(tblnam, old, new) alter table tblnam alter column old rename to new


/*
 * Current Date/Time
 */

#define SYSDATE_VAL  sysdate

/*
 * Execution
 */

#define RUN_SQL /
#define RUN_DDL /

#endif
