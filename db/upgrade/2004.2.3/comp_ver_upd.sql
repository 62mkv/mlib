#include <../../../include/mocaddl.h>
#include <mocacolwid.h>
#include <mocatbldef.h>
#include <sqlDataTypes.h>


#ifdef ORACLE
ALTER_TABLE_TABLE_INFO(comp_ver)
ALTER_TABLE_MOD_COLUMN_START(base_prog_id)
      STRING_TY(PROG_ID_LEN)
ALTER_TABLE_MOD_COLUMN_END
RUN_DDL
#else

alter table comp_ver drop constraint comp_ver_pk

ALTER_TABLE_TABLE_INFO(comp_ver)
ALTER_TABLE_MOD_COLUMN_START(base_prog_id)
      STRING_TY(PROG_ID_LEN) NOT NULL
ALTER_TABLE_MOD_COLUMN_END
RUN_DDL

alter table comp_ver add
BEGIN_CONSTRAINT
    constraint comp_ver_pk 
    primary key (base_prog_id, 
		 comp_maj_ver, 
		 comp_min_ver, 
		 comp_bld_ver, 
		 comp_rev_ver)
    CONSTRAINT_TABLESPACE_INFO (COMP_VER_PK_TBLSPC, COMP_VER_PK_STORAGE)
END_CONSTRAINT
RUN_DDL
#endif

#ifdef ORACLE
ALTER_TABLE_TABLE_INFO(comp_ver)
ALTER_TABLE_MOD_COLUMN_START(comp_file_nam)
      STRING_TY(FRM_FILE_NAM_LEN)
ALTER_TABLE_MOD_COLUMN_END
RUN_DDL
#else
ALTER_TABLE_TABLE_INFO(comp_ver)
ALTER_TABLE_MOD_COLUMN_START(comp_file_nam)
      STRING_TY(FRM_FILE_NAM_LEN) NOT NULL
ALTER_TABLE_MOD_COLUMN_END
RUN_DDL
#endif

#ifdef ORACLE
ALTER_TABLE_TABLE_INFO(comp_ver)
ALTER_TABLE_MOD_COLUMN_START(comp_prog_id)
      STRING_TY(PROG_ID_LEN)
ALTER_TABLE_MOD_COLUMN_END
RUN_DDL
#else
ALTER_TABLE_TABLE_INFO(comp_ver)
ALTER_TABLE_MOD_COLUMN_START(comp_prog_id)
      STRING_TY(PROG_ID_LEN) NOT NULL
ALTER_TABLE_MOD_COLUMN_END
RUN_DDL
#endif
