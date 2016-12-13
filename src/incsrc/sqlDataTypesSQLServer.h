/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file containing SQL Server datatype definitions.
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

#ifndef SQLDATATYPESSQLSERVER_H
#define SQLDATATYPESSQLSERVER_H

/*
 * Error Codes
 *
 * NOTE: Some of these are duplicate codes because they are different 
 *       in a different database.
 */

#define ERR_CANNOT_DROP_COLUMN_NOT_FOUND   -4924
#define ERR_CANNOT_DROP_TABLE_NOT_FOUND    -3701
#define ERR_CANNOT_DROP_INDEX_NOT_FOUND    -3701
#define ERR_CANNOT_DROP_SEQUENCE_NOT_FOUND -3701
#define ERR_COLUMN_ALREADY_EXISTS          -2705
#define ERR_COLUMN_ALREADY_NOT_NULL            0
#define ERR_COLUMN_ALREADY_HAS_DEFAULT     -1781
#define ERR_CONSTRAINT_NOT_FOUND           -3728
#define ERR_INDEX_ALREADY_EXISTS           -1913
#define ERR_INVALID_COLUMN_NAME             -207
#define ERR_NO_ROWS_AFFECTED               -1403
#define ERR_OBJECT_NOT_FOUND                -208
#define ERR_PRIMARY_KEY_ALREADY_EXISTS     -1779
#define ERR_TABLE_ALREADY_EXISTS           -2714
#define ERR_VIEW_ALREADY_EXISTS            -2714
#define ERR_CONSTRAINT_ALREADY_EXISTS      -2714
#define ERR_COLUMN_ALREADY_ALLOWS_NULLS        0
#define ERR_CANNOT_VALIDATE_PK_VIOLATED	       0

/*
 * Data Types
 */

#define STRING_TY(x)                 nvarchar(x)
#define NSTRING_TY(x)                nvarchar(x)
#define LONG_TY                      ntext
#define NLONG_TY                     nvarchar(max)
#define INTEGER_TY                   int
#define SMALLINT_TY                  smallint
#define FLAG_TY                      int
#define DATE_TY                      datetime
#define REAL_TY                      float
#define FIXED_INT_TY(x)              numeric(x)
#define NUMERIC_TY(x,y)              numeric(x,y)
#define MONEY_TY                     numeric(19,4)
#define LINEAR_TY                    numeric(19,4)
#define WEIGHT_TY                    numeric(23,8)
#define OLD_WEIGHT_TY                numeric(19,4)
#define PERCENTAGE_TY                numeric(19,4)
#define VOLUME_TY                    numeric(23,8)
#define OLD_VOLUME_TY                numeric(19,4)
#define QVL_TY                       numeric(19,4)
#define FLOAT_TY                     numeric(19,4)
#define CONV_FACTOR_TY               numeric(28,10)
#define DURATION_TY                  numeric(10,2)
#define LARGECHAR_TY                 nvarchar(4000)
#define NLARGECHAR_TY                nvarchar(max)
#define BINARY_OBJECT_TY             image
#define BLOB_TY                      image
#define CLOB_TY                      text
#define NCLOB_TY                     nvarchar(max)

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
#define DBANSI_FLOAT                 float
#define DBANSI_DOUBLE_PRECISION      double precision
#define DBANSI_DATE                  datetime

/*
 * Tablespaces
 */

#define CREATE_TABLE_TABLESPACE(name, storage)
#define CREATE_INDEX_TABLESPACE(name, storage)
#define CREATE_UNIQUE_INDEX_TABLESPACE(name, storage)
#define CREATE_CLUSTERED_INDEX_TABLESPACE(name, storage)
#define CREATE_UNIQUE_CLUSTERED_INDEX_TABLESPACE(name, storage)
#define CREATE_PK_CONSTRAINT_TABLESPACE(name, storage)

/*
 * Tables
 */

#define CREATE_TABLE(tblname)  create table dbo.tblname

#define DROP_TABLE(tblname)    drop table dbo.tblname

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
	  create clustered index idxname on tblname
#define CREATE_CLUSTERED_INDEX_CLUSTER
#define CREATE_CLUSTERED_INDEX_END

#define CREATE_UNIQUE_CLUSTERED_INDEX_BEGIN(tblname, idxname) \
	  create unique clustered index idxname on tblname
#define CREATE_UNIQUE_CLUSTERED_INDEX_CLUSTER
#define CREATE_UNIQUE_CLUSTERED_INDEX_END

#define DROP_INDEX(tblname, idxname) \
	  drop index tblname.idxname
#define DROP_UNIQUE_INDEX(tblname, idxname) \
	  drop index tblname.idxname 
#define DROP_CLUSTERED_INDEX(tblname, idxname) \
	  drop index tblname.idxname
#define DROP_UNIQUE_CLUSTERED_INDEX(tblname, idxname) \
	  drop index tblname.idxname

/*
 * Primary Keys
 */

#define CREATE_PK_CONSTRAINT_BEGIN(tblname, cnsname) \
          alter table tblname add constraint cnsname primary key 
#define CREATE_PK_CONSTRAINT_USING_INDEX
#define CREATE_PK_CONSTRAINT_END 

#define DROP_PK_CONSTRAINT(tblname, cnsname) \
	  alter table tblname drop constraint cnsname

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

#define CREATE_VIEW(x) \
          create view dbo.x as

#define DROP_VIEW(x) \
          if exists (select TABLE_NAME \
                       from INFORMATION_SCHEMA.VIEWS \
                      where TABLE_NAME = 'x') \
              drop view x

/*
 * Sequences
 */

#define CREATE_SEQUENCE(SEQ, SEED, INC, MAX) \
          create table dbo.SEQ \
          ( \
               currval numeric(28) identity (SEED, INC) primary key, \
               nextval numeric(28), \
               seedval numeric(28), \
               incval  numeric(28), \
               maxval  numeric(28)  \
          ) \
          commit \
          set identity_insert dbo.SEQ on \
          insert into dbo.SEQ (currval, nextval, seedval, incval, maxval) \
                       values (-1, -1, SEED, INC, MAX) \
          set identity_insert dbo.SEQ off

#define DROP_SEQUENCE(SEQ) \
	  drop table dbo.SEQ

/*
 * Alters
 */
	  
#define ALTER_TABLE_ADD_COLUMN_BEGIN(tblnam, colnam) \
	  alter table tblnam add colnam
#define ALTER_TABLE_ADD_COLUMN_END
		    
#define ALTER_TABLE_DROP_COLUMN_BEGIN(tblnam, colnam) \
	  alter table tblnam drop column colnam
#define ALTER_TABLE_DROP_COLUMN_END
		    
#define ALTER_TABLE_MODIFY_COLUMN_BEGIN(tblnam, colnam) \
          alter table tblnam alter column colnam
#define ALTER_TABLE_MODIFY_COLUMN_END 
			      
#define ALTER_TABLE_MODIFY_COLUMN_DATATYPE_BEGIN(tblnam, colnam) \
          alter table tblnam alter column colnam
#define ALTER_TABLE_MODIFY_COLUMN_DATATYPE_END 
			      
#define ALTER_TABLE_ADD_FLAG_CONSTRAINT_BEGIN(tblnam, cnsname, colnam) \
	  alter table tblnam add constraint cnsname check (colnam in (1, 0))
#define ALTER_TABLE_ADD_FLAG_CONSTRAINT_END 
					
#define ALTER_TABLE_DROP_FLAG_CONSTRAINT_BEGIN(tblnam, cnsname, colnam) \
	  alter table tblnam drop constraint cnsname
#define ALTER_TABLE_DROP_FLAG_CONSTRAINT_END
						  
#define ALTER_TABLE_ADD_CHECK_CONSTRAINT_BEGIN(tblnam, cnsname) \
	  alter table tblnam add constraint cnsname check (
#define ALTER_TABLE_ADD_CHECK_CONSTRAINT_END \
          )
	  
#define ALTER_TABLE_DROP_CHECK_CONSTRAINT_BEGIN(tblnam, cnsname) \
	  alter table tblnam drop constraint cnsname
#define ALTER_TABLE_DROP_CHECK_CONSTRAINT_END

#define ALTER_TABLE_DROP_NOT_NULL_CONSTRAINT_BEGIN(tblnam, colnam) \
	  alter table tblnam alter column colnam
#define ALTER_TABLE_DROP_NOT_NULL_CONSTRAINT_END \
	  null

#define ALTER_TABLE_ADD_DEFAULT_BEGIN(tblnam, colnam, value) \
          alter table tblnam add default value for colnam 
#define ALTER_TABLE_ADD_DEFAULT_END

#define ALTER_TABLE_ADD_DEFAULT_QUOTED_BEGIN(tblnam, colnam, value) \
          alter table tblnam add default 'value' for colnam 
#define ALTER_TABLE_ADD_DEFAULT_QUOTED_END

/*
 * Renames
 */

#define RENAME_TABLE(old, new)          sp_rename 'old', 'new', 'OBJECT'
#define RENAME_INDEX(tblnam, old, new)  sp_rename 'tblnam.old', 'new', 'INDEX'
#define RENAME_COLUMN(tabname, old, new) \
              sp_rename 'tabname.old', 'new', 'COLUMN'



/*
 * Current Date/Time
 */

#define SYSDATE_VAL  getdate()

/*
 * Execution
 */

#define RUN_SQL /
#define RUN_DDL /\
COMMIT\
/

#endif
