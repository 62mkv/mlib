#include <../../../include/mocaddl.h>
#include <mocacolwid.h>
#include <mocatbldef.h>
#include <sqlDataTypes.h>

/**************************************************/
/*                     MOCA-4359                  */
/**************************************************/
/*  Rename node_id to role_id on task_definition  */
/**************************************************/

mset command on
[select 1 from dual where exists (select role_id from task_definition)]
catch (ERR_INVALID_COLUMN_NAME)
|
if (@? = ERR_INVALID_COLUMN_NAME)
{
    [RENAME_TABLE(task_definition, task_definition_tmp)]
    |
    [DROP_PK_CONSTRAINT(task_definition_tmp, task_definition_pk)]
}
RUN_DDL
mset command off

#include "task_definition.tbl"

mset command on
[select a.node_id role_id, a.* from task_definition_tmp a]
catch (ERR_OBJECT_NOT_FOUND, ERR_NO_ROWS_AFFECTED)
|
if (@? = 0) 
{
    create record where table = 'task_definition'
}
RUN_DDL

[DROP_TABLE(task_definition_tmp)]
catch (ERR_CANNOT_DROP_TABLE_NOT_FOUND)
RUN_DDL
mset command off

/**************************************************/
/*                     MOCA-4359                  */
/**************************************************/
/*  Rename node_id to role_id on job_definition   */
/**************************************************/

mset command on
[select 1 from dual where exists (select role_id from job_definition)]
catch (ERR_INVALID_COLUMN_NAME)
|
if (@? = ERR_INVALID_COLUMN_NAME)
{
    [RENAME_TABLE(job_definition, job_definition_tmp)]
    |
    [DROP_PK_CONSTRAINT(job_definition_tmp, job_definition_pk)]
}
RUN_DDL
mset command off

#include "job_definition.tbl"

mset command on
[select a.node_id role_id, a.* from job_definition_tmp a]
catch (ERR_OBJECT_NOT_FOUND, ERR_NO_ROWS_AFFECTED)
|
if (@? = 0) 
{
    create record where table = 'job_definition'
}
RUN_DDL

[DROP_TABLE(job_definition_tmp)]
catch (ERR_CANNOT_DROP_TABLE_NOT_FOUND)
RUN_DDL
mset command off

/**************************************************/
/*                     MOCA-4359                  */
/**************************************************/
/*       Install our role_definition view         */
/**************************************************/

#include "role_definition.sql"
