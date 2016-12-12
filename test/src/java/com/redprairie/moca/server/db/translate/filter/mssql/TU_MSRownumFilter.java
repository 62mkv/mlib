/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2006
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 */

package com.redprairie.moca.server.db.translate.filter.mssql;

import com.redprairie.moca.MocaType;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.TranslationException;
import com.redprairie.moca.server.db.translate.filter.TU_AbstractFilterTest;
import com.redprairie.moca.server.db.translate.filter.TranslationFilter;


/**
 * Unit tests for MSRownumFilter
 * 
 * <b><pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_MSRownumFilter extends TU_AbstractFilterTest {
    public void testNoRownum() {
        _testTranslation(
                "select * from foo.bar xyz123 where xyz123.x = 'test'",
                "select * from foo.bar xyz123 where xyz123.x = 'test'");
    }
    
    public void testSimpleRownumQuery() {
        _testTranslation(
                "select * from foo where x = 10*2-3.4/-2.123412 and rownum < 100",
                "select TOP 99 * from foo where x = 10*2-3.4/-2.123412 and 1=1");
    }

    public void testSimpleRownumQueryWithComments() {
        _testTranslation(
                "select 1 from sl_msg_log where msg_log_dt < sysdate - /*=moca_util.days(*/480/*=)*/ and rownum < 10",
                "select TOP 9 1 from sl_msg_log where msg_log_dt < sysdate - /*=moca_util.days(*/480/*=)*/ and 1=1");
    }

    public void testSimpleParenRownumQueryWithComments() {
      _testTranslation(
            "select 1 from sl_msg_log where msg_log_dt < sysdate - /*=moca_util.days(*/480/*=)*/ and (rownum < 10)",
            "select TOP 9 1 from  sl_msg_log where msg_log_dt < sysdate - /*=moca_util.days(*/480/*=)*/ and ( 1=1)");
    }
    
    public void testNestedFunctions() {
        _testTranslation(
                "select n, (case when c is null then  cast(:value as VARCHAR(2000)) else  c || cast(:value as VARCHAR(2000)) end) blah from foo where ROWNUM < 10 for update WITH RS",
                "select TOP 9 n, (case when c is null then  cast(:value as VARCHAR(2000)) else  c || cast(:value as VARCHAR(2000)) end) blah from foo where 1=1 for update WITH RS");
    }
    
    public void testNestedFunctionsNoRownum() {
       _testTranslation(
                "select * from foo where x = cast(foo as varchar) and lower(y) <> lower(substr(foo, 10, 20))",
                "select * from foo where x = cast(foo as varchar) and lower(y) <> lower(substr(foo, 10, 20))");
    }
    
    public void testInlineView() {
        _testTranslation(
                "select * from (" +
                 "  select x blah, y blah2, z as blah3" +
                 "    from foo" +
                 "   where xxx < 999" +
                 "     and yyy = 'yyy'" +
                 "     and rownum < 999999999)subsel" +
                 " where rownum = 1",
                 "select TOP 1 * from (" +
                 "  select TOP 999999998 x blah, y blah2, z as blah3" +
                 "    from foo" +
                 "   where xxx < 999" +
                 "     and yyy = 'yyy'" +
                 "     and 1=1)subsel" +
                 " where 1=1"
        );
    }
    
    public void testBindArgumnet() {
       _testTranslation(
                "select * from (" +
                 "  select x blah, y blah2, z as blah3" +
                 "    from foo" +
                 "   where xxx < 999" +
                 "     and yyy = 'yyy'" +
                 "     and rownum <= :i003)subsel" +
                 " where rownum = :i001",
                 "select TOP 1 * from (" +
                 "  select TOP 3 x blah, y blah2, z as blah3" +
                 "    from foo" +
                 "   where xxx < 999" +
                 "     and yyy = 'yyy'" +
                 "     and 1=1)subsel" +
                 " where 1=1"
        );
    }
    
    public void testFloatArgumnet() {
        _testTranslation(
                "select * from (" +
                 "  select x blah, y blah2, z as blah3" +
                 "    from foo" +
                 "   where xxx < 999" +
                 "     and yyy = 'yyy'" +
                 "     and rownum <= 999.00000)subsel" +
                 " where rownum = 1.0000",
                 "select TOP 1 * from (" +
                 "  select TOP 999 x blah, y blah2, z as blah3" +
                 "    from foo" +
                 "   where xxx < 999" +
                 "     and yyy = 'yyy'" +
                 "     and 1=1)subsel" +
                 " where 1=1"
        );
    }
    
    public void testFloatBindArgumnet() {
        _testTranslation(
                "select * from (" +
                 "  select x blah, y blah2, z as blah3" +
                 "    from foo" +
                 "   where xxx < 999" +
                 "     and yyy = 'yyy'" +
                 "     and rownum <= :f003)subsel" +
                 " where rownum = :f001",
                 "select TOP 1 * from (" +
                 "  select TOP 3 x blah, y blah2, z as blah3" +
                 "    from foo" +
                 "   where xxx < 999" +
                 "     and yyy = 'yyy'" +
                 "     and 1=1)subsel" +
                 " where 1=1"
        );
    }
    
    public void testStringBindArgumnet() {
       BindList args = new BindList();
        args.add("c003", MocaType.STRING, "3");
        args.add("c001", MocaType.STRING, "1");
        try {
            String result = _performTranslation(
                    "select * from (" +
                     "  select x blah, y blah2, z as blah3" +
                     "    from foo" +
                     "   where xxx < 999" +
                     "     and yyy = 'yyy'" +
                     "     and rownum <= :c003)subsel" +
                     " where rownum = :c001", args);
            fail("Expected error, successful translation: " + result);
        }
        catch (TranslationException e) {
            // Normal
        }
    }

    public void testDoubledParenthesesInSubquery() {
        _testTranslation(
                "select 'x' " +
                " from dual " +
                "where exists " +
                "(select 'x' " +
                "   from sl_retr_mthd_impl_def rmid, " +
                "        sl_retr_mthd_def rmd, " +
                "  sl_eo_seg es, " +
                "        sl_eo_def eo, " +
                "        sl_ifd_def ifd " +
                "  where exists (select 'x' " +
                "                  from sl_eo_col ec " +
                "                 where ec.eo_id = es.eo_id " +
                "                   and ec.eo_ver = es.eo_ver " +
                "                   and ec.eo_seg_id = es.eo_seg_id " +
                "                   and ec.retr_mthd_impl_genid = rmid.retr_mthd_impl_genid " +
                "                   and ec.retr_mthd_id = rmid.retr_mthd_id)  " +
                "    and ((rmid.sys_id = :trg_sys_id) or " +
                "          (rmid.ifd_id = :trg_ifd_id and rmid.ifd_ver = :trg_ifd_ver))  " +
                "    and rmid.retr_mthd_id = rmd.retr_mthd_id " +
                "    and rmd.retr_mthd_id = es.retr_mthd_id " +
                "    and es.eo_id = eo.eo_id " +
                "    and es.eo_ver  = eo.eo_ver " +
                "    and eo.eo_id = ifd.eo_id " +
                "    and eo.eo_ver = ifd.eo_ver " +
                "    and ifd.ifd_id = :dest_ifd_id " +
                "    and ifd.ifd_ver = :dest_ifd_ver " +
                " ) ",
                "select 'x' " +
                " from dual " +
                "where exists " +
                "(select 'x' " +
                "   from sl_retr_mthd_impl_def rmid, " +
                "        sl_retr_mthd_def rmd, " +
                "  sl_eo_seg es, " +
                "        sl_eo_def eo, " +
                "        sl_ifd_def ifd " +
                "  where exists (select 'x' " +
                "                  from sl_eo_col ec " +
                "                 where ec.eo_id = es.eo_id " +
                "                   and ec.eo_ver = es.eo_ver " +
                "                   and ec.eo_seg_id = es.eo_seg_id " +
                "                   and ec.retr_mthd_impl_genid = rmid.retr_mthd_impl_genid " +
                "                   and ec.retr_mthd_id = rmid.retr_mthd_id)  " +
                "    and ((rmid.sys_id = :trg_sys_id) or " +
                "          (rmid.ifd_id = :trg_ifd_id and rmid.ifd_ver = :trg_ifd_ver))  " +
                "    and rmid.retr_mthd_id = rmd.retr_mthd_id " +
                "    and rmd.retr_mthd_id = es.retr_mthd_id " +
                "    and es.eo_id = eo.eo_id " +
                "    and es.eo_ver  = eo.eo_ver " +
                "    and eo.eo_id = ifd.eo_id " +
                "    and eo.eo_ver = ifd.eo_ver " +
                "    and ifd.ifd_id = :dest_ifd_id " +
                "    and ifd.ifd_ver = :dest_ifd_ver " +
                " ) "
        );
    }
    
    public void testParenthisesRownum() {
        _testTranslation("select x from sl_msg_log where " + "x='1' "
                + "and (rownum < 100)", "select TOP 99 x from sl_msg_log "
                + "where x='1' " + "and ( 1=1)");
    }

    public void testInlineRowNumInParenView() {
        _testTranslation("select * from ("
                + "  select x blah, y blah2, z as blah3" + "    from foo"
                + "   where xxx < 999" + "     and yyy = 'yyy'"
                + "     and (rownum < 999999999))subsel"
                + " where (rownum = 1)", "select TOP 1 * from ("
                + "  select TOP 999999998 x blah, y blah2, z as blah3"
                + "    from foo" + "   where xxx < 999"
                + "     and yyy = 'yyy'" + "     and ( 1=1))subsel" + " where ( 1=1)");
    }

    public void testSimpleRowNumInParenView() {
        _testTranslation(
            "select * from foo where x = 'foo' and (rownum < 100) and y = 'bar'",
            "select TOP 99 * from foo where x = 'foo' and ( 1=1) and y = 'bar'");
    }

    public void testSimpleRowNumInMultiConditionalParenView() {
        _testTranslation(
            "select * from foo where x = 'foo' and (rownum < 100 and y = 'bar')",
            "select TOP 99 * from foo where x = 'foo' and ( 1=1 and y = 'bar')");
    }

    public void testSimpleRowNumInMultiConditionalDoubleParenView() {
        _testTranslation(
            "select * from foo where x = 'foo' and ((rownum < 100) and y = 'bar')",
            "select TOP 99 * from foo where x = 'foo' and (( 1=1) and y = 'bar')");
    }

    public void testSimpleRowNumInMiddleMultiConditionalDoubleParenView() {
        _testTranslation(
            "select * from foo where x = 'foo' and (z='fooey' and (rownum < 100) and y = 'bar')",
            "select TOP 99 * from foo where x = 'foo' and (z='fooey' and ( 1=1) and y = 'bar')");
    }

    public void testSimpleRowNumAtEndMultiConditionalDoubleParenView() {
        _testTranslation(
            "select * from foo where x = 'foo' and (z='fooey' and y = 'bar' and (rownum < 100) )",
            "select TOP 99 * from foo where x = 'foo' and (z='fooey' and y = 'bar' and ( 1=1) )");
    }

    public void testSimpleRowNumAtEndMultipleSubSelect() {
       _testTranslation(
            "select x from ( select x from bar ),(select * from foo where x = 'foo' and (z='fooey' and y = 'bar' and (rownum < 100)))",
            "select x from ( select x from bar ),(select TOP 99 * from foo where x = 'foo' and (z='fooey' and y = 'bar' and ( 1=1)))");
    }

    public void testFloatBindArgumnetWithRowNumInParen() {
        _testTranslation(
            "select * from (" + "  select x blah, y blah2, z as blah3"
                    + "    from foo" + "   where xxx < 999"
                    + "     and yyy = 'yyy'"
                    + "     and (rownum <= :f003))subsel"
                    + " where rownum = :f001", "select TOP 1 * from ("
                    + "  select TOP 3 x blah, y blah2, z as blah3"
                    + "    from foo" + "   where xxx < 999"
                    + "     and yyy = 'yyy'" + "     and ( 1=1))subsel"
                    + " where 1=1");
    }

    public void testFloatBindArgumnetWithRowNumInParenUnMatched() {
        BindList args = new BindList();
        args.add("f003", MocaType.DOUBLE, 3.0f);
        args.add("f001", MocaType.DOUBLE, 1.0f);
        try {
            String result = _performTranslation("select * from ("
                    + "  select x blah, y blah2, z as blah3" + "    from foo"
                    + "   where xxx < 999" + "     and yyy = 'yyy'"
                    + "     and (rownum <= :f003)subsel"
                    + " where rownum = :f001", args);
            fail("Expected error, however got successful translation: "
                    + result);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        catch (TranslationException e) {
            // Normal
        }
    }

    public void testStringBindArgumnetWithRowNumInParen() {
        BindList args = new BindList();
        args.add("c003", MocaType.STRING, "3");
        args.add("c001", MocaType.STRING, "1");
        try {
            String result = _performTranslation("select * from ("
                    + "  select x blah, y blah2, z as blah3" + "    from foo"
                    + "   where xxx < 999" + "     and yyy = 'yyy'"
                    + "     and (rownum <= :c003))subsel"
                    + " where (rownum = :c001)", args);
            fail("Expected error, successful translation: " + result);
        }
        catch (TranslationException e) {
            // Normal
        }
    }

    public void testStringBindArgumnetWithMultiRowNumError() {
       BindList args = new BindList();
        args.add("c003", MocaType.STRING, "3");
        args.add("c001", MocaType.STRING, "1");
        try {
            String result = _performTranslation("select * from ("
                    + "  select x blah, y blah2, z as blah3" + "    from foo"
                    + "   where xxx < 999"
                    + "     and (rownum < 3 ) and yyy = 'yyy'"
                    + "     and (rownum <= :c003))subsel"
                    + " where (rownum = :c001)", args);
            fail("Expected error, successful translation: " + result);
        }
        catch (TranslationException e) {
            // Normal
        }
    }

    public void testDistinct() {
        _testTranslation(
                "select distinct * from foo where rownum < 2",
                 "select distinct TOP 1 * from foo where 1=1"
        );
    }

    public void testDistinctSubselect() {
        _testTranslation(
                "select distinct * from (" +
                 "  select x blah, y blah2, z as blah3" +
                 "    from foo" +
                 "   where xxx < 999" +
                 "     and yyy = 'yyy'" +
                 "     and rownum < 999999999) subsel" +
                 " where rownum < 2",
                 "select distinct TOP 1 * from (" +
                 "  select TOP 999999998 x blah, y blah2, z as blah3" +
                 "    from foo" +
                 "   where xxx < 999" +
                 "     and yyy = 'yyy'" +
                 "     and 1=1 ) subsel" +
                 " where 1=1"
        );
    }
    
    public void testDistinctSubselectParentheses() {
       _testTranslation(
            "select distinct * from (" + "  select x blah, y blah2, z as blah3"
                    + "    from foo" + "   where xxx < 999"
                    + "     and yyy = 'yyy'"
                    + "     and (rownum < 999999999)) subsel"
                    + " where rownum < 2", "select distinct TOP 1 * from ("
                    + "  select TOP 999999998 x blah, y blah2, z as blah3"
                    + "    from foo" + "   where xxx < 999"
                    + "     and yyy = 'yyy'" + "     and ( 1=1)) subsel"
                    + " where 1=1"
        );
    }

    public void testDistinctMultiConditionParen() {
        _testTranslation(
            "select distinct * from foo where (rownum < 2  and y = 1)",
            "select distinct TOP 1 * from foo where ( 1=1 and y = 1)");
    }

    public void testDistinctBadOrder() {
      _testTranslation(
            "select distinct * from foo where rownum < 2 and y = 1",
            "select distinct TOP 1 * from foo where 1=1 and y = 1");
    }

    public void testDistinctBadOrderParen() {
        _testTranslation(
            "select distinct * from foo where (rownum < 2) and y = 1",
            "select distinct TOP 1 * from foo where ( 1=1) and y = 1");
    }

    public void testDistinctParen() {
        _testTranslation(
                "select distinct * from foo where (rownum < 2)",
                "select distinct TOP 1 * from foo where (1=1)");
    }
    public void testUnionWithRowNum() {
        _testTranslation("select * from foo where id=5 and rownum < 5 UNION ALL select * from foo where id=6 and rownum < 15",
        "select TOP 4 * from foo where id=5 and 1=1 UNION ALL select TOP 14 * from foo where id=6 and 1=1");
    }
    
    public void testSubselectWithUnion() {
        _testTranslation(
                "select * from (select * from foo union select * from bar) where rownum < 10",
                "select TOP 9 * from (select * from foo union select * from bar) where 1=1");
    }

    public void testUnionWithParenRowNum() {
        _testTranslation("select * from foo where id=5 and (rownum < 5) UNION ALL select * from foo where id=6 and rownum < 15",
        "select TOP 4 * from foo where id=5 and ( 1=1) UNION ALL select TOP 14 * from foo where id=6 and 1=1");

    }
    public void testUnionWithMultiParenRowNum() {
        _testTranslation("select * from foo where (id=5 and rownum < 5) UNION ALL select * from foo where id=6 and rownum < 15",
        "select TOP 4 * from foo where (id=5 and 1=1) UNION ALL select TOP 14 * from foo where id=6 and 1=1");

    }
    public void testUnionLatterRowNum() {
        _testTranslation("select * from foo where id=5 UNION ALL select * from foo where id=6 and rownum < 15",
        "select * from foo where id=5 UNION ALL select TOP 14 * from foo where id=6 and 1=1");

    }
    public void testUnionWithInitialRowNum() {
        _testTranslation("select * from foo where id=5 and rownum < 5 UNION ALL select * from foo where id=6",
        "select TOP 4 * from foo where id=5 and 1=1 UNION ALL select * from foo where id=6");

    }
    public void testComplexUnionwithSubQueryRowNum() {
        _testTranslation("select jobcodeintid from ( " +
                              "select jobcodeintid " +
                               "from ( " +
                              "select 3 as mapping_order, jobcodeintid from job_code_mapping " +
                               "where  cstnum is NULL and client_id is NULL and aislearea_int_id = 21 and " + 
                               "worktype_id = 'SEL' " +
                               "union " +
                               "select 4 as mapping_order, jobcodeintid from job_code_mapping " +
                               "where  aislearea_int_id is NULL and cstnum is NULL and client_id is NULL and worktype_id = 'SEL' " + 
                               ") standards " +
                              "where rownum < 2147483647 order by mapping_order) subsel " + 
                              "where rownum = 1",
                              "select TOP 1 jobcodeintid from ( " +
                              "select TOP 2147483646 jobcodeintid " +
                              "from ( " +
                              "select 3 as mapping_order, jobcodeintid from job_code_mapping " +
                              "where  cstnum is NULL and client_id is NULL and aislearea_int_id = 21 and " + 
                              "worktype_id = 'SEL' " +
                              "union " +
                              "select 4 as mapping_order, jobcodeintid from job_code_mapping " +
                              "where  aislearea_int_id is NULL and cstnum is NULL and client_id is NULL and worktype_id = 'SEL' " + 
                              ") standards " +
                              "where 1=1 order by mapping_order) subsel " + 
                              "where 1=1");
    }
                        
    public void testQueryMultiple() {
        _testTranslation("set nocount on;begin  insert into sl_ifd_sys_map_data ( evt_data_seq,    ifd_id, ifd_ver, sys_id, ena_flg, snd_ordr, blkd_flg, blk_alg_id,   blk_alg_eval_cd, comm_mode_cd, cre_dt, snd_dt, cre_ts, snd_ts,    grp_ordr, comm_mthd_id, head_evt_id, tail_evt_id, succ_evt_id, fail_evt_id,    ins_dt, ins_user_id, blk_reason ) values ( 4915,'SL_TEST_OUT_FROM_EO_IFD','V1.0','SL_TEST_DEST','F',100,'T',NULL,   NULL,'SYNCD', to_date('19691231180000','YYYYMMDDHH24MISS'),   to_date('19691231180000','YYYYMMDDHH24MISS'),   decode(0, -1, CAST(NULL AS FLOAT), 0), decode(0, -1, CAST(NULL AS FLOAT), 0),    0, 'FILE', NULL, NULL, NULL, NULL,   sysdate, 'SUPER', 'OSIMD' );  end;set nocount off;",
                         "set nocount on;begin  insert into sl_ifd_sys_map_data ( evt_data_seq,    ifd_id, ifd_ver, sys_id, ena_flg, snd_ordr, blkd_flg, blk_alg_id,   blk_alg_eval_cd, comm_mode_cd, cre_dt, snd_dt, cre_ts, snd_ts,    grp_ordr, comm_mthd_id, head_evt_id, tail_evt_id, succ_evt_id, fail_evt_id,    ins_dt, ins_user_id, blk_reason ) values ( 4915,'SL_TEST_OUT_FROM_EO_IFD','V1.0','SL_TEST_DEST','F',100,'T',NULL,   NULL,'SYNCD', to_date('19691231180000','YYYYMMDDHH24MISS'),   to_date('19691231180000','YYYYMMDDHH24MISS'),   decode(0, -1, CAST(NULL AS FLOAT), 0), decode(0, -1, CAST(NULL AS FLOAT), 0),    0, 'FILE', NULL, NULL, NULL, NULL,   sysdate, 'SUPER', 'OSIMD' );  end;set nocount off;");
    }
    public void testQueryMultipleWithRownum() {
        _testTranslation("set nocount on;select * from foo where id=5 and rownum < 5;set nocount off;",
                         "set nocount on;select TOP 4 * from foo where id=5 and 1=1 ; set nocount off;");
    }
    public void testQueryMultipleWithSubSelectRownum() {
        _testTranslation("select x from (set nocount on;select * from foo where id=5 and rownum < 5;set nocount off); ",
                         "select x from (set nocount on;select TOP 4 * from foo where id=5 and 1=1 ; set nocount off);");
    }

    // @see com.redprairie.moca.db.translate.filter.TU_AbstractFilterTest#_getFilter()
    @Override
    protected TranslationFilter _getFilter() {
        return new MSRownumFilter();
    }
}
