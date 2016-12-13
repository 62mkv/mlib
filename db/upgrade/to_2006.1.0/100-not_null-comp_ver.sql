#include <../../../include/mocaddl.h>
#include <mocacolwid.h>
#include <mocatbldef.h>
#include <sqlDataTypes.h>

#ifdef ORACLE

mset command on

[
ALTER_TABLE_TABLE_INFO(comp_ver)
ALTER_TABLE_MOD_COLUMN_START(base_prog_id)
      STRING_TY(PROG_ID_LEN) not null
ALTER_TABLE_MOD_COLUMN_END
] catch(-1442)
RUN_DDL

[
ALTER_TABLE_TABLE_INFO(comp_ver)
ALTER_TABLE_MOD_COLUMN_START(comp_maj_ver)
      INTEGER_TY not null
ALTER_TABLE_MOD_COLUMN_END
] catch(-1442)
RUN_DDL

[
ALTER_TABLE_TABLE_INFO(comp_ver)
ALTER_TABLE_MOD_COLUMN_START(comp_min_ver)
      INTEGER_TY not null
ALTER_TABLE_MOD_COLUMN_END
] catch(-1442)
RUN_DDL

[
ALTER_TABLE_TABLE_INFO(comp_ver)
ALTER_TABLE_MOD_COLUMN_START(comp_bld_ver)
      INTEGER_TY not null
ALTER_TABLE_MOD_COLUMN_END
] catch(-1442)
RUN_DDL

[
ALTER_TABLE_TABLE_INFO(comp_ver)
ALTER_TABLE_MOD_COLUMN_START(comp_rev_ver)
      INTEGER_TY not null
ALTER_TABLE_MOD_COLUMN_END
] catch(-1442)
RUN_DDL

[
ALTER_TABLE_TABLE_INFO(comp_ver)
ALTER_TABLE_MOD_COLUMN_START(comp_file_nam)
      STRING_TY(FRM_FILE_NAM_LEN) not null
ALTER_TABLE_MOD_COLUMN_END
] catch(-1442)
RUN_DDL

[
ALTER_TABLE_TABLE_INFO(comp_ver)
ALTER_TABLE_MOD_COLUMN_START(comp_prog_id)
      STRING_TY(PROG_ID_LEN) not null
ALTER_TABLE_MOD_COLUMN_END
] catch(-1442)
RUN_DDL

[
ALTER_TABLE_TABLE_INFO(comp_ver)
ALTER_TABLE_MOD_COLUMN_START(comp_typ)
      STRING_TY(COMP_TYP_LEN) not null
ALTER_TABLE_MOD_COLUMN_END
] catch(-1442)
RUN_DDL

[
ALTER_TABLE_TABLE_INFO(comp_ver)
ALTER_TABLE_MOD_COLUMN_START(comp_file_ext)
      STRING_TY(COMP_FILE_EXT) not null
ALTER_TABLE_MOD_COLUMN_END
] catch(-1442)
RUN_DDL

[
ALTER_TABLE_TABLE_INFO(comp_ver)
ALTER_TABLE_MOD_COLUMN_START(grp_nam)
      STRING_TY(GRP_NAM_LEN) not null
ALTER_TABLE_MOD_COLUMN_END
] catch(-1442)
RUN_DDL

mset command off

#endif