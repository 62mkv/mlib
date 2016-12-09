#include <../../../include/mocaddl.h>
#include <mocacolwid.h>
#include <mocatbldef.h>
#include <sqlDataTypes.h>

mset command on

[select lic_key
   from comp_ver 
  where 1 = 0 ] catch (-207, -904, -1403)
|
if (@? = -207 or @? = -904)
{
    [ ALTER_TABLE_TABLE_INFO(comp_ver)
      ALTER_TABLE_ADD_COLUMN_START( ) 
          lic_key STRING_TY(LIC_KEY_LEN)
      ALTER_TABLE_ADD_COLUMN_END
    ]

}
/

mset command off
