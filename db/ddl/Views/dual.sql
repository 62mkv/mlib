#include <../../../include/mocaddl.h>
#include <mocatbldef.h>
#include <sqlDataTypes.h>

#ifdef SQL_SERVER

mset command on

[
 DROP_VIEW(dual)
] catch(-204, -942, -3701)
RUN_DDL

mset command off

CREATE_VIEW(dual)
select 'x' x
RUN_DDL

#endif
