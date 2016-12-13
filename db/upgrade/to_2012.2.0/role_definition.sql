#include <../../../include/mocaddl.h>
#include <mocatbldef.h>
#include <sqlDataTypes.h>

mset command on

[
 DROP_VIEW(role_definition)
] catch(-204, -942, -3701)
RUN_DDL

mset command off

CREATE_VIEW(role_definition)
select distinct coalesce(role_id, task_id) as role_id 
   from task_definition 
union
select distinct role_id as role_id 
   from job_definition 
     where role_id is not null
RUN_DDL