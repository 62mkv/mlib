#include <../../../include/mocaddl.h>
#include <mocacolwid.h>
#include <mocatbldef.h>
#include <sqlDataTypes.h>

#ifdef ORACLE

mset command on

try
{
    publish data where table_name = 'MOCA_DBVERSION'
    |
    [
     select constraint_name
       from user_constraints
      where table_name = @table_name
        and constraint_type = 'C'
    ]
    |
    [
     alter table @table_name:raw drop constraint @constraint_name:raw
    ]
} catch(-1403)
{
    publish data where result = 'No constraints to drop.'
}
RUN_DDL

mset command off

#endif
