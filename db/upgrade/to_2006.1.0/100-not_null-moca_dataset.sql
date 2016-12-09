#include <../../../include/mocaddl.h>
#include <mocacolwid.h>
#include <mocatbldef.h>
#include <sqlDataTypes.h>

#ifdef ORACLE

mset command on

[
ALTER_TABLE_TABLE_INFO(moca_dataset)
ALTER_TABLE_MOD_COLUMN_START(ds_name)
      STRING_TY(DS_NAME_LEN) not null
ALTER_TABLE_MOD_COLUMN_END
] catch(-1442)
RUN_DDL

[
ALTER_TABLE_TABLE_INFO(moca_dataset)
ALTER_TABLE_MOD_COLUMN_START(ds_desc)
      STRING_TY(DS_DESC_LEN) not null
ALTER_TABLE_MOD_COLUMN_END
] catch(-1442)
RUN_DDL

[
ALTER_TABLE_TABLE_INFO(moca_dataset)
ALTER_TABLE_MOD_COLUMN_START(ds_dir)
      STRING_TY(DS_DIR_LEN) not null
ALTER_TABLE_MOD_COLUMN_END
] catch(-1442)
RUN_DDL

[
ALTER_TABLE_TABLE_INFO(moca_dataset)
ALTER_TABLE_MOD_COLUMN_START(ds_seq)
      INTEGER_TY not null
ALTER_TABLE_MOD_COLUMN_END
] catch(-1442)
RUN_DDL

mset command off

#endif
