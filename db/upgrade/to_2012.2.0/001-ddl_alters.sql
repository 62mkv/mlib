#include <../../../include/mocaddl.h>
#include <mocacolwid.h>
#include <mocatbldef.h>
#include <sqlDataTypes.h>

/**************************************************/
/*                     MOCA-4890                  */
/**************************************************/
/*  Change job so that role id is no longer       */
/*  inherited from the job id when a role id      */
/*  is not provided                               */
/**************************************************/

/* role_definition.sql will get picked up by      */
/* dbupgrade since it is a .sql file              */

#include "job_definition_exec.tbl"
#include "task_definition_exec.tbl"
