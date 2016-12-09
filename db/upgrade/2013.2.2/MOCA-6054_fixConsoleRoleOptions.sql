#include <../../../include/mocaddl.h>
#include <sqlDataTypes.h>

/* This is to remove a dependency on a MCS hotfix for MOCA console access
 * via role options which went in with MOCA-5852. This is basically manually
 * inserting the records that MCS would load so that customers can take the
 * MOCA hotfix alone without needing the MCS hotfix as well.
 */

mset command on

/* First check if the MCS tables even exist by looking for les_opt_ath,
 * if they don't actually exist there's nothing to do here as MCS isn't installed,
 * that scenario should only really be in development environments. The below
 * query should throw a ERR_NO_ROWS_AFFECTED exception when the table does actually exist
 * and throw ERR_OBJECT_NOT_FOUND when the table does not exist.
 */
[ 
  select 'x'
    from les_opt_ath
   where 1=2 
] catch(ERR_OBJECT_NOT_FOUND, ERR_NO_ROWS_AFFECTED)
|
if (@? = ERR_NO_ROWS_AFFECTED)
{
    /* Now that we know the table exists insert the role option data so the MOCA console
     * can still be logged into. For the purpose of allowing this to be executed multiple
     * times we just catch possible unique constraint violations.
     */
    [ insert into les_opt_ath (opt_nam,ath_typ,ath_id,pmsn_mask,grp_nam)
             values ('optMocaConsoleAdmin', 'R', 'SUPER', -1, 'mcs_data') ] catch(-1)
    ;
    [ insert into les_opt_ath (opt_nam,ath_typ,ath_id,pmsn_mask,grp_nam)
             values ('optMocaConsoleAdmin', 'R', 'CONSOLE_ADMIN', -1, 'mcs_data') ] catch(-1)
    ;
    [ insert into les_opt_ath (opt_nam,ath_typ,ath_id,pmsn_mask,grp_nam)
             values ('optMocaConsoleRead', 'R', 'CONSOLE_READ', -1, 'mcs_data') ] catch(-1)
    ;
    [ insert into sys_dsc_mst (colnam,colval,locale_id,cust_lvl,mls_text,short_dsc,grp_nam)
             values ('opt_nam', 'optMocaConsoleRead', 'US_ENGLISH', 0, 'MOCA Console Read Only', NULL, 'mcs_data') ] catch(-1)
    ;
    [ insert into sys_dsc_mst (colnam,colval,locale_id,cust_lvl,mls_text,short_dsc,grp_nam)
             values ('opt_nam', 'optMocaConsoleAdmin', 'US_ENGLISH', 0, 'MOCA Console Administrator', NULL, 'mcs_data') ] catch(-1)
    ;
    [ insert into les_mnu_opt (opt_nam,opt_typ,pmsn_mask,ena_flg,exec_nam,exec_parm,btn_img_id,ath_grp_nam,addon_id,grp_nam) 
             values ('optMocaConsoleRead', 'P', -1, 1, NULL, NULL, NULL, NULL, NULL, 'mcs_data') ] catch(-1)
    ;
    [ insert into les_mnu_opt (opt_nam,opt_typ,pmsn_mask,ena_flg,exec_nam,exec_parm,btn_img_id,ath_grp_nam,addon_id,grp_nam) 
             values ('optMocaConsoleAdmin', 'P', -1, 1, NULL, NULL, NULL, NULL, NULL, 'mcs_data') ] catch(-1)

}

RUN_SQL
mset command off
