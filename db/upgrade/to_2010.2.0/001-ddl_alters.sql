#include <../../../include/mocaddl.h>
#include <mocacolwid.h>
#include <mocatbldef.h>
#include <sqlDataTypes.h>

/**************************************************/
/*                     MOCA-4101                  */
/**************************************************/
/*    Add trace_level to task_definition table    */
/**************************************************/

mset command on
[ALTER_TABLE_ADD_COLUMN_BEGIN(task_definition, trace_level)
     STRING_TY(TASK_TRACE_LEN)
ALTER_TABLE_ADD_COLUMN_END]
catch(ERR_COLUMN_ALREADY_EXISTS)

RUN_DDL
mset command off

#include "task_definition.tdoc"
