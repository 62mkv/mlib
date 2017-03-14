#include <../../../include/mocaddl.h>
#include <mocacolwid.h>
#include <mocatbldef.h>
#include <sqlDataTypes.h>

ALTER_TABLE_TABLE_INFO(hb_buysell_data)
ALTER_TABLE_ADD_COLUMN_START(reacod)
    STRING_TY(20) null
ALTER_TABLE_ADD_COLUMN_END
RUN_DDL

mset command on
[update hb_buysell_data
    set reacod ='GoodPrice'
where reacod is null] catch(-1403)
/
mset command off

#include <hb_buysell_data.tdoc>

ALTER_TABLE_TABLE_INFO(oc_buysell_data)
ALTER_TABLE_ADD_COLUMN_START(reacod)
    STRING_TY(20) null
ALTER_TABLE_ADD_COLUMN_END
RUN_DDL

mset command on
[update oc_buysell_data
    set reacod ='GoodPrice'
where reacod is null] catch(-1403)
/
mset command off

#include <oc_buysell_data.tdoc>