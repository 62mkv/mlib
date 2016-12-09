#include <../../../include/mocaddl.h>
#include <mocacolwid.h>
#include <mocatbldef.h>
#include <sqlDataTypes.h>
#use $MOCADIR/db/ddl/Tables
#use $MOCADIR/db/ddl/Indexes
#use $MOCADIR/db/ddl/Sequences
#use $MOCADIR/db/ddl/Views

/* Modify the comp_ver table  */
ALTER_TABLE_TABLE_INFO(comp_ver)
    ALTER_TABLE_ADD_COLUMN_START( ) 
    lic_key STRING_TY(LIC_KEY_LEN)
    ALTER_TABLE_ADD_COLUMN_END
RUN_SQL

