#include <../../../include/mocaddl.h>
#include <mocacolwid.h>
#include <mocatbldef.h>
#include <sqlDataTypes.h>

#ifdef ORACLE

mset command on

[
ALTER_TABLE_TABLE_INFO(moca_dbversion)
ALTER_TABLE_MOD_COLUMN_START(version)
      STRING_TY(DBVERSION_LEN) not null
ALTER_TABLE_MOD_COLUMN_END
] catch(-1442)
RUN_DDL

mset command off

#endif
