/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
 *  Sam Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by Sam Corporation.
 *
 *  Sam Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by Sam Corporation.
 *
 *  $Copyright-End$
 */

package com.redprairie.moca.server.db.translate;

import com.redprairie.moca.MocaType;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.BindMode;

/**
 * Various Unit Tests for the MS SQL Server translator
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_SQLServerDialect extends TU_AbstractDialect {
    
    // @see com.redprairie.moca.server.db.translate.TU_AbstractDialect#dialectSetUp()
    @Override
    protected void dialectSetUp() throws Exception {
        super.dialectSetUp();
        
        _putComments = true;
    }
    
    // @see com.redprairie.moca.server.db.translate.TU_AbstractDialect#getDialect()
    @Override
    protected BaseDialect getDialect() {
        return new SQLServerDialect();
    }

    public void testTrivialTranslations() throws TranslationException {
        _runTest("", "");
        _runTest("\t\n\r", "\t\n\r");
    }
    
    public void testNoconvTranslations() throws TranslationException {
        // We don't want comments for our no conversion test
        _putComments = false;
        
        _runTest("/*NOCONV*/", "");
        _runTest("    /*NOCONV*/ select 'x' from dual", " select :q0 from dual");
        _runTest("/*NOCONV*/select n, decode(c, null, :value, c || :value) blah from foo where rownum < 10 for update",
                 "select n, decode(c, null, :value, c || :value) blah from foo where rownum < :i0 for update");
    }
    
    public void testSelectFromDual() throws TranslationException {
        _runTest("select 'x' from dual", "select :q0 from dual");
        BindList args = new BindList();
        args.add("value", MocaType.STRING, "blah");
        _runTest("select * from dual where value = :value",
                 "select * from dual where value = :value",
                 args);
    }

    public void testParamEqualsParam() throws TranslationException {
        BindList args = new BindList();
        args.add("i01", MocaType.INTEGER, 45);
        args.add("i02", MocaType.INTEGER, null);
        _runTest("select * from dual where :i01 = :i02",
                 "select * from dual where :i01 = :i02",
                 args);
    }

    public void testNowait() throws TranslationException {
        _runTest("select * from foo for update NOWAIT",
                 "select * from foo (UPDLOCK)");
    }

    public void testConcatenation() throws TranslationException {
        _runTest("select C || X from foo",
                 "select C + X from foo");
    }

    public void testDecodeRownum() throws TranslationException {
        BindList args = new BindList();
        args.add("value", MocaType.STRING, "blah");
        _runTest("select n, decode(c, null, :value, c || :value) blah from foo where rownum < 10 for update",
                 "select TOP 9 n, (case when c is null then  :value else  c + :value end) blah from foo (UPDLOCK) where 1=1",
                 args);
    }
    
    public void testSubstrWithTwoArgs() throws TranslationException {
        _runTest("select substr ('TESTGtest',5) x from dual",
                 "select SUBSTRING(CONVERT(NVARCHAR(4000),:q0), :i1, LEN(CONVERT(NVARCHAR(4000),:q0))) x from dual");
    }
    
    public void testToDateWithBind() throws TranslationException {
        BindList args = new BindList();
        args.add("q001", MocaType.STRING, "A");
        args.add("q002", MocaType.STRING, "B");
        args.add("q003", MocaType.STRING, "C");
        args.add("q004", MocaType.STRING, "D");
        args.add("q005", MocaType.STRING, "E");
        args.add("q006", MocaType.STRING, "20070411020122");
        args.add("q007", MocaType.STRING, "F");
        args.add("l001", MocaType.INTEGER, 100);
        args.add("l002", MocaType.INTEGER, 1);
        _runTest("insert into stop(stop_id, car_move_id, stop_seq, adr_id, stop_nam, stop_cmpl_flg, moddte, mod_usr_id, tms_stop_seq) values(:q001, :q002,  -:l001, :q003, :q004, :q005, to_date(:q006), :q007, :l002)",
                 "insert into stop(stop_id, car_move_id, stop_seq, adr_id, stop_nam, stop_cmpl_flg, moddte, mod_usr_id, tms_stop_seq) values(:q001, :q002,  -:l001, :q003, :q004, :q005, CONVERT(DATETIME, :q006__td, 20), :q007, :l002)",
                 args);
        assertEquals("2007-04-11 02:01:22", args.getValue("q006__td"));
    }
    
    public void testToDateWithBindAndTwoArgs() throws TranslationException {
        BindList args = new BindList();
        args.add("q001", MocaType.STRING, "A");
        args.add("q002", MocaType.STRING, "B");
        args.add("q003", MocaType.STRING, "C");
        args.add("q004", MocaType.STRING, "D");
        args.add("q005", MocaType.STRING, "E");
        args.add("q006", MocaType.STRING, "20070411020122");
        args.add("q007", MocaType.STRING, "YYYYMMDDHH24MISS");
        args.add("q008", MocaType.STRING, "F");
        args.add("l001", MocaType.INTEGER, 100);
        args.add("l002", MocaType.INTEGER, 1);
        _runTest("insert into stop(stop_id, car_move_id, stop_seq, adr_id, stop_nam, stop_cmpl_flg, moddte, mod_usr_id, tms_stop_seq) values(:q001, :q002,  -:l001, :q003, :q004, :q005, to_date(:q006, :q007), :q008, :l002)",
                 "insert into stop(stop_id, car_move_id, stop_seq, adr_id, stop_nam, stop_cmpl_flg, moddte, mod_usr_id, tms_stop_seq) values(:q001, :q002,  -:l001, :q003, :q004, :q005, CONVERT(DATETIME, :q006__td, 20), :q008, :l002)",
                 args);
        assertEquals("2007-04-11 02:01:22", args.getValue("q006__td"));
    }
    
    public void testToDateWithoutBind() throws TranslationException {
        TranslationOptions opt = new TranslationOptions(BindMode.NONE);
        BindList args = new BindList();
        _runTest("insert into stop(stop_id, car_move_id, stop_seq, adr_id, stop_nam, stop_cmpl_flg, moddte, mod_usr_id, tms_stop_seq) values('A', 'B',  -100, 'C', 'D', 'E', to_date('20070411020122'), 'F', 1)",
                 "insert into stop(stop_id, car_move_id, stop_seq, adr_id, stop_nam, stop_cmpl_flg, moddte, mod_usr_id, tms_stop_seq) values(N'A', N'B',  -100, N'C', N'D', N'E', CONVERT(DATETIME, N'2007-04-11 02:01:22', 20), N'F', 1)",
                 args, opt);
    }
    
    public void testLPAD() throws TranslationException {
        _runTest("select LPAD(:q00, length(:q01), :q02) test from dual",
                 "select (LEFT(REPLICATE(CONVERT(NVARCHAR(4000), :q02 ), len(:q01) )," +
                 "CASE SIGN( len(:q01) - LEN(CONVERT(NVARCHAR(4000), :q00)))" +
                 "WHEN 1 THEN LEN(:q01)-LEN(CONVERT(NVARCHAR(4000), :q00))" +
                 "ELSE 0 END) +" +
                 "LEFT(CONVERT(NVARCHAR(4000), :q00), LEN(:q01))) test from dual");
    }
    
    public void testOuterJoinWithForUpdate() throws TranslationException {
        _runTest("select invsub.* from invsub " +
                 "left outer join invlod " +
                 "on invlod.lodnum = invsub.lodnum " +
                 "where invlod.lodnum = 'XXX' for update",
                 "select invsub.* from invsub " +
                 "left outer join invlod (UPDLOCK) " +
                 "on invlod.lodnum = invsub.lodnum " +
                 "where invlod.lodnum = :q0");
    }
    
    public void testOuterJoinWithForUpdateOf() throws TranslationException {
        _runTest("select invsub.* from invsub " +
                 "left outer join invlod " +
                 "on invlod.lodnum = invsub.lodnum " +
                 "where invlod.lodnum = 'XXX' for update of invsub.phyflg",
                 "select invsub.* from invsub (UPDLOCK) " +
                 "left outer join invlod " +
                 "on invlod.lodnum = invsub.lodnum " +
                 "where invlod.lodnum = :q0");
    }
    
    public void testToDateWithDateTimeBind() throws TranslationException {
        BindList args = new BindList();
        args.add("q001", MocaType.STRING, "A");
        args.add("q002", MocaType.STRING, "B");
        args.add("q003", MocaType.STRING, "C");
        args.add("q004", MocaType.STRING, "D");
        args.add("q005", MocaType.STRING, "E");
        args.add("q006", MocaType.STRING, "20070411020122");
        args.add("q007", MocaType.STRING, "F");
        args.add("l001", MocaType.INTEGER, 100);
        args.add("l002", MocaType.INTEGER, 1);
        _runTest("insert into stop(stop_id, car_move_id, stop_seq, adr_id, stop_nam, stop_cmpl_flg, moddte, mod_usr_id, tms_stop_seq) values(:q001, :q002,  -:l001, :q003, :q004, :q005, to_date(:q006), :q007, :l002)",
                 "insert into stop(stop_id, car_move_id, stop_seq, adr_id, stop_nam, stop_cmpl_flg, moddte, mod_usr_id, tms_stop_seq) values(:q001, :q002,  -:l001, :q003, :q004, :q005, CONVERT(DATETIME, :q006__td, 20), :q007, :l002)",
                 args);
        assertEquals("2007-04-11 02:01:22", args.getValue("q006__td"));
    }

    public void testOuterJoinWithSubquery() throws TranslationException {
        _runTest("select invsub.* from invsub " +
                 "left outer join (select lodnum foo, count(*) locqty from invlod group by lodnum) " +
                 "on invsub.lodnum = foo " +
                 "where invsub.lodnum = 'XXX' for update of invsub.lodnum",
                 "select invsub.* from invsub (UPDLOCK) " +
                 "left outer join (select lodnum foo, count(*) locqty from invlod group by lodnum) " +
                 "on invsub.lodnum = foo " +
                 "where invsub.lodnum = :q0");
    }
    
    public void testNestedAnsiJoin() throws TranslationException {
        _runTest(
            "select carhdr.carcod, " +
            "carhdr.carnam, " +
            "contact.cont_id, " +
            "contact.cont_name, " +
            "contact.phnnum, " +
            "contact.faxnum, " +
            "contact.email_adr, " +
            "contact.geoloc_nam, " +
            "contact.cont_typ, " +
            "contact.comm_typ " +
            "from contact " +
            "INNER JOIN (adr_contact " +
            "INNER JOIN carhdr " +
            "on carhdr.adr_id = adr_contact.adr_id " +
            "and carhdr.carcod = 'FOREWAY') " +
            "on adr_contact.cont_id = contact.cont_id " +
            "and contact.cont_typ = 'TENDER' " +
            "and (contact.geoloc_nam is null or " +
            "contact.geoloc_nam " +
            "in " +
            "(select g.geoloc_nam " +
            "from adrmst a, " +
            "tm_geoloc g " +
            "LEFT OUTER JOIN tm_geoloc_pc_rng p on g.geoloc_nam = p.geoloc_nam " +
            "where a.ctry_name = nvl(g.ctry_name, (select ctry_name " +
            "from locale_mst " +
            "where locale_id = nvl(NULL,'US_ENGLISH'))) " +
            "and (g.adr_id is null " +
            "or g.adr_id = a.adr_id) " +
            "and (g.adrpsz is null " +
            "or g.adrpsz = a.adrpsz) " +
            "and (g.adrstc is null " +
            "or g.adrstc = a.adrstc) " +
            "and (g.adrcty is null " +
            "or (g.adrcty = a.adrcty " +
            "and g.adrstc = a.adrstc)) " +
            "and ((p.beg_adrpsz is null " +
            "and p.end_adrpsz is null) " +
            "or (a.adrpsz BETWEEN p.beg_adrpsz AND p.end_adrpsz)) " +
            "and a.adr_id = (select s.adr_id " +
            "from car_move cm, " +
            "stop s " +
            "where cm.car_move_id = 'CMV0003448' " +
            "and s.car_move_id = cm.car_move_id " +
            "and s.tms_stop_seq = '1' " +
            "and exists (select 'x' " +
            "from tm_geoloc gl " +
            "where gl.adr_id = s.adr_id)))) ",
            
            "select carhdr.carcod, " +
            "carhdr.carnam, " +
            "contact.cont_id, " +
            "contact.cont_name, " +
            "contact.phnnum, " +
            "contact.faxnum, " +
            "contact.email_adr, " +
            "contact.geoloc_nam, " +
            "contact.cont_typ, " +
            "contact.comm_typ " +
            "from contact " +
            "INNER JOIN (adr_contact " +
            "INNER JOIN carhdr " +
            "on carhdr.adr_id = adr_contact.adr_id " +
            "and carhdr.carcod = :q0) " +
            "on adr_contact.cont_id = contact.cont_id " +
            "and contact.cont_typ = :q1 " +
            "and (contact.geoloc_nam is null or " +
            "contact.geoloc_nam " +
            "in " +
            "(select g.geoloc_nam " +
            "from adrmst a, " +
            "tm_geoloc g " +
            "LEFT OUTER JOIN tm_geoloc_pc_rng p on g.geoloc_nam = p.geoloc_nam " +
            "where a.ctry_name = coalesce(g.ctry_name, (select ctry_name " +
            "from locale_mst " +
            "where locale_id = coalesce(NULL,:q2))) " +
            "and (g.adr_id is null " +
            "or g.adr_id = a.adr_id) " +
            "and (g.adrpsz is null " +
            "or g.adrpsz = a.adrpsz) " +
            "and (g.adrstc is null " +
            "or g.adrstc = a.adrstc) " +
            "and (g.adrcty is null " +
            "or (g.adrcty = a.adrcty " +
            "and g.adrstc = a.adrstc)) " +
            "and ((p.beg_adrpsz is null " +
            "and p.end_adrpsz is null) " +
            "or (a.adrpsz BETWEEN p.beg_adrpsz AND p.end_adrpsz)) " +
            "and a.adr_id = (select s.adr_id " +
            "from car_move cm, " +
            "stop s " +
            "where cm.car_move_id = :q3 " +
            "and s.car_move_id = cm.car_move_id " +
            "and s.tms_stop_seq = :q4 " +
            "and exists (select :q5 " +
            "from tm_geoloc gl " +
            "where gl.adr_id = s.adr_id)))) ");
    }
    
    public void testCommentAfterFrom() throws TranslationException {
        _runTest("select 'x' from /* blah */ dual",
                 "select :q0 from dual");
    }
    
    public void testCommentWithNoWhitespace() throws TranslationException {
        _runTest("select x/* blah */from dual",
                 "select x from dual");
    }
    
    public void testAutoBind() throws TranslationException {
        BindList args = new BindList();
        _runTest("select 'x' x from dual",
                 "select :q0 x from dual",
                 args);
        assertTrue(args.contains("q0"));
        assertEquals("x", args.getValue("q0"));
    }

    public void testAutoBindNoBindHint() throws TranslationException {
        BindList args = new BindList();
        _runTest("select 'x' x from dual where /*#nobind*/ a = 'hello' /*#bind*/ and b = 'goodbye'",
                 "select :q0 x from dual where a = N'hello' and b = :q1",
                 args);
        assertTrue(args.contains("q0"));
        assertTrue(args.contains("q1"));
        assertEquals("x", args.getValue("q0"));
        assertEquals("goodbye", args.getValue("q1"));
    }
    
    public void testUnicodeCastNoUnicodeHint() throws TranslationException {
        _runTestNoBind("select 'x' x from dual where /*#nounicode*/ a = 'hello' /*#unicode*/ and b = 'goodbye'",
                 "select N'x' x from dual where a = 'hello' and b = N'goodbye'");
    }
    
    public void testDistinctSubselectParentheses() throws TranslationException {
        _runTest("select distinct * from (" + "  select x blah, y blah2, z as blah3"
               + "    from foo" + "   where xxx < 999"
               + "     and yyy = 'yyy'"
               + "     and (rownum < 999999999)) subsel"
               + " where rownum < 2",
                 "select distinct TOP 1 * from ("
               + "  select TOP 999999998 x blah, y blah2, z as blah3"
               + "    from foo" + "   where xxx < :i0"
               + "     and yyy = :q1" + "     and ( 1=1)) subsel"
               + " where 1=1");
     }

    public void testDecodeWithAllBlanks() throws TranslationException {
        _runTestNoBind("select '' as foo, " +
        		"decode('', '', '', '') as bar " +
        		"from dual",
                       "select cast(null as nvarchar) as foo, " +
                        "(case when cast(null as nvarchar) is null then cast(null as nvarchar) else cast(null as nvarchar) end) as bar " +
                        "from dual");
     }

    public void testDecodeWithAllNull() throws TranslationException {
        _runTestNoBind("select null as foo, " +
                        "decode(null, null, null, null) as bar " +
                        "from dual",
                       "select null as foo, " +
                        "(case when null is null then null else cast(null as nvarchar) end) as bar " +
                        "from dual");
     }

    public void testToDateWithBlanks() throws TranslationException {
        _runTestNoBind("select to_date('') as foo " +
                        "from dual",
                       "select convert(DATETIME, cast(null as nvarchar)) as foo " +
                        "from dual");
     }

    public void testReplaceWithNullArgument() throws TranslationException {
        _runTest("select replace('sl_async_evt_que','_hdr','') || '_seq' seq_name from dual",
                 "select replace(convert(nvarchar(4000), :q0), convert(nvarchar(4000), :q1), isnull(convert(nvarchar(4000), :q2), N'')) + :q3 seq_name from dual");
        _runTestNoBind("select replace('sl_async_evt_que','_hdr','') || '_seq' seq_name from dual",
                 "select replace(convert(nvarchar(4000), N'sl_async_evt_que'), convert(nvarchar(4000), N'_hdr'), isnull(convert(nvarchar(4000), cast(null as nvarchar)), N'')) + N'_seq' seq_name from dual");
     }

    
    
}
