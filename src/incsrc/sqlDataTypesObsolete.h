/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file containing obsolete SQL 
 *               datatype definitions.
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

#ifndef SQLDATATYPESOBSOLETE_H
#define SQLDATATYPESOBSOLETE_H

/* 
 *  Common Definitions
 */

#define FLAG_COL(table, col, def) \
	col FLAG_TY default def \
        constraint table##_##col##_nl not null \
        constraint table##_##col##_ck check (col in (1, 0))

#define FLAG_CONSTRAINT(name, col) \
	constraint name check (col in (1, 0))

#define PRIMARY_KEY_FIELD(name) \
	constraint name not null

#define NOT_NULL_CONSTRAINT(name) \
        not null

#define ADD_NOT_NULL_CONSTRAINT(name, col) \
	add constraint name check (col is not null)

#define ADD_FLAG_CONSTRAINT(name, col) \
	add constraint name check (col in (1, 0))

/* 
 *  Database Engine Specific Definitions
 */

#ifdef ORACLE

#   define RUN_SQL /
#   define RUN_DDL /

#   define STRING_TY(x)      varchar2(x)
#   define LONG_TY           long
#   define INTEGER_TY        number(10)
#   define SMALLINT_TY       smallint
#   define FLAG_TY           number(1)
#   define DATE_TY           date 
#   define REAL_TY           number
#   define FIXED_INT_TY(x)   number(x)
#   define NUMERIC_TY(x,y)   number(x,y)
#   define MONEY_TY          number(19,4)
#   define LINEAR_TY         number(19,4)
#   define WEIGHT_TY         number(23,8)
#   define OLD_WEIGHT_TY     number(19,4)
#   define PERCENTAGE_TY     number(19,4)
#   define VOLUME_TY         number(23,8)
#   define OLD_VOLUME_TY     number(19,4)
#   define QVL_TY            number(19,4)
#   define FLOAT_TY          number(19,4)
#   define CONV_FACTOR_TY    number(28,10)
#   define DURATION_TY       number(10,2)
#   define LARGECHAR_TY      varchar2(4000)
#   define BINARY_OBJECT_TY  long raw 

#   define DBANSI_CHAR(n)              char(n)
#   define DBANSI_CHARACTER(n)         char(n)
#   define DBANSI_CHAR_VARYING(n)      varchar2(n)
#   define DBANSI_CHARACTER_VARYING(n) varchar2(n)
#   define DBANSI_NUMERIC(p,s)         decimal(p,s)
#   define DBANSI_DECIMAL(p,s)         decimal(p,s)
#   define DBANSI_INT                  int 
#   define DBANSI_INTEGER              int 
#   define DBANSI_SMALLINT             smallint 
#   define DBANSI_REAL                 float
#   define DBANSI_FLOAT	               float
#   define DBANSI_DOUBLE_PRECISION     double precision	
#   define DBANSI_DATE                 date 

#   define BEGIN_CONSTRAINT  (
#   define END_CONSTRAINT    )

#   define SYSDATE_VAL       sysdate

#   define CREATE_TABLE(x) create table x

#   define DELETE_VIEW(x)
#   define CREATE_VIEW(x)    create or replace view x as

#   define CREATE_SEQ(SEQ, SEED, INC, MAX) \
	   create sequence SEQ \
           start with SEED \
           increment by INC \
           maxvalue MAX \
           cycle cache 10 \
           order

#   define ALTER_TABLE(table, column, datatype) \
           alter table x add (column datatype)

#   define ALTER_TABLE_TABLE_INFO(x) \
	   alter table x

#   define ALTER_TABLE_MOD_COLUMN_START(x) modify ( x
#   define ALTER_TABLE_MOD_COLUMN_END      )

#   define ALTER_TABLE_ADD_COLUMN_START(x) add ( x
#   define ALTER_TABLE_ADD_COLUMN_END      )

#   ifdef USE_DEFAULT_TABLESPACE
#     define TABLESPACE_INFO(x, y)
#     define CONSTRAINT_TABLESPACE_INFO(x, y)
#   else
#     define TABLESPACE_INFO(x, y)             tablespace x y
#     define CONSTRAINT_TABLESPACE_INFO(x, y)  using index tablespace x y
#   endif

#   define CREATE_PK_PRE(tabname,name) \
           alter table tabname add (constraint name primary key 
#   define CREATE_PK_INDEX_INFO using index
#   define CREATE_PK_POST       )

#   define CREATE_INDEX(tabname, name) \
	   create index name on tabname

#   define CREATE_UNIQUE_INDEX(tabname, name) \
	   create unique index name on tabname

#   define CREATE_CLUSTERED_INDEX(tabname, name) \
	   create index name on tabname

#   define CREATE_UNIQUE_CLUSTERED_INDEX(tabname, name) \
	   create unique index name on tabname

#   define CREATE_FK_PRE(tabname,name)  \
           alter table tabname add (constraint name foreign key 
#   define CREATE_FK_POST )

#   define CREATE_FK_ON_DELETE_CASCADE on delete cascade

#   define ALTER_TABLE_DROP_COLUMN_INFO(col) drop column col

#   define DROP_INDEX(tab,ind)   drop index ind

#   define ALTER_TABLE_DROP_CONSTRAINT_INFO(cons) drop constraint cons drop index
#   define COL_DOC(tab,col,doc)  comment on column tab.col is 'doc'
#   define TBL_DOC(tab,doc)      comment on table tab is 'doc'

#   define RENAME_PK_FIELD_CONSTRAINT(tabname, old, new) \
	   alter table tabname rename constraint old to new

#   define RENAME_NOT_NULL_CONSTRAINT(tabname, old, new) \
	   alter table tabname rename constraint old to new

#   define RENAME_FLAG_CONSTRAINT(tabname, old, new) \
	   alter table tabname rename constraint old to new

#   define RENAME_CHECK_CONSTRAINT(tabname, old, new) \
	   alter table tabname rename constraint old to new

#   define RENAME_PK_CONSTRAINT(tabname, old, new) \
	   alter table tabname rename constraint old to new

#   define RENAME_FK_CONSTRAINT(tabname, old, new) \
	   alter table tabname rename constraint old to new

#   define RENAME_INDEX(tabname, old, new) \
	   alter index old rename to new

#   define RENAME_TABLE(old, new) \
	   alter table old rename to new

#endif  /* Oracle */

#ifdef SQL_SERVER

#   define RUN_SQL /
#   define RUN_DDL /\
COMMIT\
/

#   define STRING_TY(x)      varchar(x)
#   define LONG_TY           text
#   define INTEGER_TY        int
#   define SMALLINT_TY       smallint
#   define FLAG_TY           int
#   define DATE_TY           datetime
#   define REAL_TY           float
#   define FIXED_INT_TY(x)   numeric(x)
#   define NUMERIC_TY(x,y)   numeric(x,y)
#   define MONEY_TY          numeric(19,4)
#   define LINEAR_TY         numeric(19,4)
#   define WEIGHT_TY         numeric(23,8)
#   define OLD_WEIGHT_TY     numeric(19,4)
#   define PERCENTAGE_TY     numeric(19,4)
#   define VOLUME_TY         numeric(23,8)
#   define OLD_VOLUME_TY     numeric(19,4)
#   define QVL_TY            numeric(19,4)
#   define FLOAT_TY          numeric(19,4)
#   define CONV_FACTOR_TY    numeric(28,10)
#   define DURATION_TY       numeric(10,2)
#   define LARGECHAR_TY      varchar(8000)
#   define BINARY_OBJECT_TY  image

#   define DBANSI_CHAR(n)              char(n)
#   define DBANSI_CHARACTER(n)         char(n)
#   define DBANSI_CHAR_VARYING(n)      varchar(n)
#   define DBANSI_CHARACTER_VARYING(n) varchar(n)
#   define DBANSI_NUMERIC(p,s)         decimal(p,s)
#   define DBANSI_DECIMAL(p,s)         decimal(p,s)
#   define DBANSI_INT                  int
#   define DBANSI_INTEGER              int
#   define DBANSI_SMALLINT             smallint
#   define DBANSI_REAL                 float
#   define DBANSI_FLOAT                float
#   define DBANSI_DOUBLE_PRECISION     double precision 
#   define DBANSI_DATE                 datetime

#   define SYSDATE_VAL       getdate()

#   define BEGIN_CONSTRAINT
#   define END_CONSTRAINT

#   define CREATE_TABLE(x) create table dbo.x

#   define DELETE_VIEW(x) \
	   if exists (select TABLE_NAME \
	                from INFORMATION_SCHEMA.VIEWS \
                       where TABLE_NAME = 'x') \
	       drop view x

#   define CREATE_VIEW(x) create view dbo.x as

#   define CREATE_SEQ(SEQ, SEED, INC, MAX) \
	   create table dbo.SEQ \
	   ( \
	       currval numeric(28) identity (SEED, INC) primary key, \
	       nextval numeric(28), \
	       seedval numeric(28), \
	       incval  numeric(28), \
	       maxval  numeric(28) \
	   ) \
           commit \
           set identity_insert dbo.SEQ on \
           insert into dbo.SEQ (currval, nextval, seedval, incval, maxval) values (-1, -1, SEED, INC, MAX) \
           set identity_insert dbo.SEQ off

#   define TABLESPACE_INFO(x, y) 
#   define CONSTRAINT_TABLESPACE_INFO(x, y)

#   define CREATE_PK_PRE(tabname,name) \
           alter table tabname add constraint name primary key 
#   define CREATE_PK_INDEX_INFO
#   define CREATE_PK_POST

#   define CREATE_INDEX(tabname, name) \
	   create index name on tabname

#   define CREATE_UNIQUE_INDEX(tabname, name) \
	   create unique index name on tabname

#   define CREATE_CLUSTERED_INDEX(tabname, name) \
	   create clustered index name on tabname

#   define CREATE_UNIQUE_CLUSTERED_INDEX(tabname, name) \
	   create unique clustered index name on tabname

#   define CREATE_FK_PRE(tabname,name)  \
           alter table tabname add constraint name foreign key 
#   define CREATE_FK_POST 

#   define CREATE_FK_ON_DELETE_CASCADE     on delete cascade

#   define ALTER_TABLE_TABLE_INFO(x)       alter table x

#   define ALTER_TABLE_MOD_COLUMN_START(x) alter column x
#   define ALTER_TABLE_MOD_COLUMN_END 

#   define ALTER_TABLE_ADD_COLUMN_START(x) add x
#   define ALTER_TABLE_ADD_COLUMN_END 

#   define ALTER_TABLE_DROP_COLUMN_INFO(col) drop column col

#   define DROP_INDEX(tab,ind)             drop index tab.ind

#   define ALTER_TABLE_DROP_CONSTRAINT_INFO(cons) drop constraint cons

#   define COL_DOC(tab,col,doc)  \
           sp_addextendedproperty 'MS_Description', 'doc', \
	   'user', dbo, \
	   'table', tab, \
	   'column', col

#   define TBL_DOC(tab,col,doc)  \
           sp_addextendedproperty 'MS_Description', 'doc', \
	   'user', dbo, \
	   'table', tab

#   define RENAME_PK_FIELD_CONSTRAINT(tabname, old, new) \
           if exists (select NAME \
                        from SYSOBJECTS \
                       where NAME = 'old' \
                         and TYPE = 'C') \
               exec sp_rename 'old', 'new', 'OBJECT' \
           else \
               select 'n/a' from dual

#   define RENAME_NOT_NULL_CONSTRAINT(tabname, old, new) \
           if exists (select NAME \
                        from SYSOBJECTS \
                       where NAME = 'old' \
                         and TYPE = 'C') \
               exec sp_rename 'old', 'new', 'OBJECT' \
           else \
               select 'n/a' from dual

#   define RENAME_FLAG_CONSTRAINT(tabname, old, new) \
          sp_rename 'old', 'new', 'OBJECT'

#   define RENAME_CHECK_CONSTRAINT(tabname, old, new) \
          sp_rename 'old', 'new', 'OBJECT'

#   define RENAME_PK_CONSTRAINT(tabname, old, new) \
          select 'RENAME_INDEX should be called after this.' from dual

#   define RENAME_FK_CONSTRAINT(tabname, old, new) \
          sp_rename 'old', 'new', 'OBJECT'

#   define RENAME_INDEX(tabname, old, new) \
          sp_rename 'tabname.old', 'new', 'INDEX'

#   define RENAME_TABLE(old, new) \
          sp_rename 'old', 'new', 'OBJECT'

#endif /* SQL server */

#endif
