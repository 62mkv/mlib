#include <../../../include/mocaddl.h>
#include <sqlDataTypes.h>

mset command on

[
 delete from comp_ver
  where base_prog_id = 'MOCADataAccess.DataAccess'
] catch (ERR_NO_ROWS_AFFECTED)
RUN_SQL

mset command off
