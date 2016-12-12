/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

package com.redprairie.moca.server.db;

import java.util.Arrays;

import junit.framework.TestCase;

import com.redprairie.moca.MocaType;

/**
 * Unit tests for SQLBinder class.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author Derek Inksetter
 * @version $Revision$
 */
public class TU_SQLBinder extends TestCase {
    public void testNoVariables() {
        _testResults("", "", new String[] {});
        _testResults("select 'x' from dual", "select 'x' from dual", new String[] {});
    }
    
    public void testExtraColons() {
        _testResults("::::", "::::", new String[] {});
   }
    public void testSingleVariable() {
        _testResults("select :abcd from dual",
                     "select ? from dual",
                     new String[] {"abcd"});
        _testResults("select :abcd:abcd_i from dual",
                     "select ?? from dual",
                     new String[] {"abcd", "abcd_i"});
    }
    
    public void testDifferentVariables() {
        _testResults("select :a123_abc a, :b321_bca b from dual",
                     "select ? a, ? b from dual",
                     new String[] {"a123_abc", "b321_bca"});
    }
    
    public void testSameVariableTwice() {
        _testResults("select :abcd foo from dual where x = :abcd",
                     "select ? foo from dual where x = ?",
                     new String[] {"abcd", "abcd"});
    }
    
    public void testLongIdentifier() {
        _testResults("select :testveryveryveryverylongidentifier1234 foo from dual where x = :anotherveryveryverylongidentifier12345",
                     "select ? foo from dual where x = ?",
                     new String[] {"testveryveryveryverylongidentifier1234", "anotherveryveryverylongidentifier12345"});
    }
    
    public void testMixedCase() {
        _testResults("select :MixedCase foo from dual where x = :UPPERCASE",
                     "select ? foo from dual where x = ?",
                     new String[] {"MixedCase", "UPPERCASE"});
    }
    
    public void testCommentInteraction() {
        _testResults("select :abcd foo from dual where x = :wxyz /* and y = :blah */",
                     "select ? foo from dual where x = ? /* and y = :blah */",
                     new String[] {"abcd", "wxyz", "blah"}, new String[] {"abcd", "wxyz"});
        _testResults("select :abcd foo from dual where x = :wxyz /* and y = :blah / :foo */",
                     "select ? foo from dual where x = ? /* and y = :blah / :foo */",
                     new String[] {"abcd", "wxyz", "blah", "foo"}, new String[] {"abcd", "wxyz"});
        _testResults("select :abcd foo from dual where x = :wxyz /* and y = :blah / :foo */ and z = :bar",
                     "select ? foo from dual where x = ? /* and y = :blah / :foo */ and z = ?",
                     new String[] {"abcd", "wxyz", "blah", "foo", "bar"}, new String[] {"abcd", "wxyz", "bar"});
    }
    
    public void testLineCommentInteraction() {
        _testResults("select :abcd foo from dual where x = :wxyz -- and y = :blah \n" +
        	     " and z = :bar",
                     "select ? foo from dual where x = ? -- and y = :blah \n" +
                     " and z = ?",
                     new String[] {"abcd", "wxyz", "blah", "bar"},
                     new String[] {"abcd", "wxyz", "bar"});
    }
    
    public void testUnclosedComment() {
        // This should cause errors, but we shouldn't blow up
        _testResults("select abcd /* from dual where x = :xxx and y = :xxx",
                     "select abcd /* from dual where x = :xxx and y = :xxx",
                     new String[] {});
    }
    
    public void testQuotedIdentifierInteraction() {
        _testResults("select :abcd \"name:wxyz\" from dual",
                     "select ? \"name:wxyz\" from dual",
                     new String[] {"abcd"});
    }
    
    public void testUnclosedQuotedIdentifier() {
        _testResults("select :abcd \"name:wxyz from dual where x = :x",
                     "select ? \"name:wxyz from dual where x = :x",
                     new String[] {"abcd"});
    }
    
    public void testStatementEndsWithColon() {
        _testResults("select :abcd foo from dual where x = :",
                     "select ? foo from dual where x = ?",
                     new String[] {"abcd", ""});
    }
    
    public void testDoubleColonIdentifier() {
        // This is necessary for handling certain SQL Server statements
        _testResults("select ::abcd foo from dual",
                     "select ::abcd foo from dual",
                     new String[] {});
    }
    
    public void testOutParameters() {
        _testResults("begin :x := sqlerrm ( :n ); end;",
                     "begin ? := sqlerrm ( ? ); end;",
                     new String[] {"x", "n"});
    }
    
    public void testRealStatement() {
         _testResults(" select sl_data_pk,\n" +
             "\ttrntyp \"trntyp\",\n" +
             "\tinvnum \"invnum\",\n" +
             "\tsupnum \"supnum\",\n" +
             "\tclient_id \"client_id\",\n" +
             "\tinvtyp \"invtyp\",\n" +
             "\tsadnum \"sadnum\",\n" +
             "\twaybil \"waybil\",\n" +
             "\tinvdte \"invdte\",\n" +
             "\torgref \"orgref\",\n" +
             "\tsrc_host \"src_host\",\n" +
             "\tsrc_port \"src_port\",\n" +
             "\tship_id \"ship_id\",\n" +
             "\tdoc_num \"doc_num\",\n" +
             "\ttrack_num \"track_num\",\n" +
             " \t:i_c_sys_absent_ind_char \"vc_tms_ship_id\", sl_dtl_idx from ( select  data_dtl_buffer_dtl_seq, sl_data_pk, sl_data_fk,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,1,20)) as sl_dtl_idx,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,21,20)) as SEGNAM,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,41,1)) as TRNTYP,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,42,35)) as INVNUM,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,77,20)) as SUPNUM,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,97,32)) as CLIENT_ID,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,129,1)) as INVTYP,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,130,20)) as SADNUM,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,150,20)) as WAYBIL,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,170,8)) as INVDTE,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,178,10)) as ORGREF,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,188,100)) as SRC_HOST,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,288,10)) as SRC_PORT,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,298,30)) as SHIP_ID,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,328,20)) as DOC_NUM,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,348,20)) as TRACK_NUM,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,368,30)) as VC_TMS_SHIP_ID,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,398,40)) as VC_SF_HOST_EXT_ID from SESSION.sl_data_dtl_buffer  ) x where sl_data_fk = :sl_data_pk  order by data_dtl_buffer_dtl_seq",
             " select sl_data_pk,\n" +
             "\ttrntyp \"trntyp\",\n" +
             "\tinvnum \"invnum\",\n" +
             "\tsupnum \"supnum\",\n" +
             "\tclient_id \"client_id\",\n" +
             "\tinvtyp \"invtyp\",\n" +
             "\tsadnum \"sadnum\",\n" +
             "\twaybil \"waybil\",\n" +
             "\tinvdte \"invdte\",\n" +
             "\torgref \"orgref\",\n" +
             "\tsrc_host \"src_host\",\n" +
             "\tsrc_port \"src_port\",\n" +
             "\tship_id \"ship_id\",\n" +
             "\tdoc_num \"doc_num\",\n" +
             "\ttrack_num \"track_num\",\n" +
             " \t? \"vc_tms_ship_id\", sl_dtl_idx from ( select  data_dtl_buffer_dtl_seq, sl_data_pk, sl_data_fk,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,1,20)) as sl_dtl_idx,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,21,20)) as SEGNAM,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,41,1)) as TRNTYP,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,42,35)) as INVNUM,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,77,20)) as SUPNUM,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,97,32)) as CLIENT_ID,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,129,1)) as INVTYP,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,130,20)) as SADNUM,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,150,20)) as WAYBIL,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,170,8)) as INVDTE,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,178,10)) as ORGREF,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,188,100)) as SRC_HOST,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,288,10)) as SRC_PORT,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,298,30)) as SHIP_ID,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,328,20)) as DOC_NUM,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,348,20)) as TRACK_NUM,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,368,30)) as VC_TMS_SHIP_ID,\n" +
             "rtrim(ORA.SUBSTR(data_dtl_info,398,40)) as VC_SF_HOST_EXT_ID from SESSION.sl_data_dtl_buffer  ) x where sl_data_fk = ?  order by data_dtl_buffer_dtl_seq",
             new String[] {"i_c_sys_absent_ind_char", "sl_data_pk"});
    }

    public void testMissingBindVariable() {
        _testResults("select :abcd foo from dual",
                     "select :abcd foo from dual",
                     new String[] {}, new BindList());
    
        BindList args = new BindList();
        args.add("abcd", "value of abcd");
        args.add("xxxx", MocaType.INTEGER, null);
        _testResults("select :abcd foo, :wxyz bar, :xxxx zzz from dual",
                     "select ? foo, :wxyz bar, ? zzz from dual",
                     new String[] {"abcd", "xxxx"});
    }
    
    public void testExtraBindVariable() {
        _testResults("select :abcd foo from dual",
                     "select ? foo from dual",
                     new String[] {"abcd", "wxyz"}, new String[] {"abcd"});
    }
    
    public void testUnbindStatement() {
        BindList args = new BindList();
        args.add("aaa", MocaType.STRING, "TEST");
        args.add("bbb", MocaType.STRING, "'Testing'");
        args.add("ccc", MocaType.STRING, "'Test''ing'");
        args.add("ddd", MocaType.INTEGER, 403);
        args.add("eee", MocaType.DOUBLE, 3.1416);
        args.add("fff", MocaType.DATETIME, "20090512191110");
        
        _testUnbind("select :aaa foo from foo " +
                    "where f = :aaa and y in (:bbb, :ccc) or w < :ddd" +
                    " and w > :eee and z = to_date(:fff)",
                    "select 'TEST' foo from foo " +
                    "where f = 'TEST' and y in ('''Testing''', '''Test''''ing''') or w < 403" +
                    " and w > 3.1416 and z = to_date('20090512191110')",
                    args);
    }
    
    public void testUnbindStatementWithVariableAtEnd() {
        BindList args = new BindList();
        args.add("aaa", MocaType.STRING, "TEST");
        
        _testUnbind("select :aaa foo from foo where x = :aaa",
                    "select 'TEST' foo from foo where x = 'TEST'",
                    args);
    }
    
    public void testUnbindStatementWithReferenceVariable() {
        BindList args = new BindList();
        args.add("aaa", MocaType.STRING, "TEST");
        args.add("bbb", MocaType.STRING_REF, "");
        
        _testUnbind("select :aaa into :bbb from foo where x = :aaa",
                    "select 'TEST' into ? /* '' */ from foo where x = 'TEST'",
                    args);

        args = new BindList();
        args.add("a1", MocaType.INTEGER_REF, 3);
        args.add("a2", MocaType.INTEGER_REF, null);
        args.add("b1", MocaType.STRING_REF, "XXX");
        args.add("b2", MocaType.STRING_REF, null);
        args.add("c1", MocaType.DOUBLE_REF, 3.14);
        args.add("c2", MocaType.DOUBLE_REF, null);
        
        _testUnbind("select :a1, :b1, :c1 into :a2, :b2, :c2 from foo ",
                    "select ? /* 3 */, ? /* 'XXX' */, ? /* 3.14 */ into ? /* null */, ? /* null */, ? /* null */ from foo ",
                    args);
}
    
    //
    // Implementation
    //
    private void _testResults(String before, String after, String[] names, BindList args) {
        SQLBinder binder = new SQLBinder(before, args);
        assertEquals(after, binder.getBoundStatement());
        assertEquals(Arrays.asList(names), binder.getNames());
    }
    
    private void _testResults(String before, String after, String[] in, String[] out) {
        BindList args = new BindList();
        for(int i = 0; i < in.length; i++) {
            args.add(in[i], MocaType.STRING, "ARG: " + in[i]);
        }
        _testResults(before, after, out, args);
    }
    
    private void _testResults(String before, String after, String[] names) {
        _testResults(before, after, names, names);
    }

    private void _testUnbind(String before, String after, BindList args) {
        SQLBinder binder = new SQLBinder(before, args);
        assertEquals(after, binder.getUnboundStatement());
    }
    
    
}
