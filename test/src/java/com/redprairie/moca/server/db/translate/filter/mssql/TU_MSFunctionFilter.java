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

package com.redprairie.moca.server.db.translate.filter.mssql;

import com.redprairie.moca.MocaType;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.TranslationException;
import com.redprairie.moca.server.db.translate.filter.TU_AbstractFilterTest;
import com.redprairie.moca.server.db.translate.filter.TranslationFilter;


/**
 * Unit tests for MSFunctionFilter
 * 
 * <b><pre>
 * Copyright (c) 20167 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_MSFunctionFilter extends TU_AbstractFilterTest {
    public void testNoFunction() {
        _testTranslation(
                "select * from foo.bar xyz123 where xyz123.x is null",
                "select * from foo.bar xyz123 where xyz123.x is null");
        _testTranslation(
                "select 'x' " +
                " from dual " +
                "where exists " +
                "(select 'x' from foo where ((a = 'value') or (b = 'value')) and c is not null)",
                "select 'x' " +
                " from dual " +
                "where exists " +
                "(select 'x' from foo where ((a = 'value') or (b = 'value')) and c is not null)");
    }
    
    public void testDecode() {
        _testTranslation(
                "select decode (x, 'a', 'b', 'c') from dual where 1=1",
                "select (case when x = 'a' then 'b' else 'c' end) from dual where 1=1");
    }
    
    public void testDecodeWithNullOption() {
        _testTranslation(
                "select decode (x, 'a', 'b', null, 'x', 'c') from dual where 1=1",
                "select (case when x = 'a' then 'b' when x is null then 'x' else 'c' end) from dual where 1=1");
    }
    
    public void testNestedDecode() {
        _testTranslation(
                "select decode(decode(x, 'a', 'b', 'c'), null, 'foo', 'bar') from dual where 1=1",
                "select (case when  (case when x = 'a' then 'b' else 'c' end) is null then 'foo' else 'bar' end) from dual where 1=1");
    }
    
    public void testDecodeWithComplexArg() {
        _testTranslation(
                "select decode(COALESCE(x, '----'), '----', cast(null as varchar(1000)), 'xxxx', 'x', x) first_of_month from dual",
                "select (case when COALESCE(x, '----') = '----' then  cast(null as varchar(1000)) when COALESCE(x, '----') = 'xxxx' then 'x' else x end) first_of_month from dual");
    }
    
    public void testTrunc() {
        _testTranslation(
                "insert into foo values(trunc(3.14), trunc(3.14, 1), round(3.75), round(3.75, 1))",
                "insert into foo values( round(3.14, 0, 1), round(3.14, 1, 1), round(3.75, 0), round(3.75, 1))");
    }
    
    public void testReplace() {
        _testTranslation(
                "select replace(decode(trunc(x), 1, 'one', 'other'), 'o', 'z') stuff from dual",
                "select replace(convert(varchar(8000), (case when  round(x, 0, 1) = 1 then  'one' else  'other' end)),convert(varchar(8000), 'o'),isnull(convert(varchar(8000), 'z'), N'')) stuff from dual");
        _testTranslation(
                "select replace('sl_async_evt_que','_hdr','') || '_seq' seq_name from dual",
                "select replace(convert(varchar(8000), 'sl_async_evt_que'),convert(varchar(8000), '_hdr'),isnull(convert(varchar(8000), ''), N'')) || '_seq' seq_name from dual");
    }
    
    public void testInstr() {
        _testTranslation(
                "select instr(c, '-') stuff from foo",
                "select CHARINDEX(CONVERT(varchar(8000), '-'), CONVERT(VARCHAR(8000), c)) stuff from foo");
    }
    
    public void testToCharNoFormat() {
        _testTranslation(
                "select to_char(x) xyz from dual",
                "select CONVERT(NVARCHAR, x) xyz from dual");
    }
    
    public void testToCharNumericFormat() {
        _testTranslation(
                "select to_char(c, '999') xyz from foo",
                "select STR(c, 4, 0) xyz from foo");
    }
    
    public void testToCharNumericFormatZeroPad() {
        _testTranslation(
                "select to_char(c, '0999') xyz from foo",
                "select CASE SIGN(c) WHEN -1 THEN N'-' ELSE N' ' END + RIGHT(N'000000000000000000000000000000'+LTRIM(STR(ABS(c), 4, 0)),4) xyz from foo");
    }
    
    public void testToCharNumericFormatWithDecimals() {
        _testTranslation(
                "select to_char(c, '999.99') xyz from foo",
                "select STR(c, 7, 2) xyz from foo");
    }
    
    public void testToCharNumericFormatWithDecimalsZeroPad() {
        _testTranslation(
                "select to_char(c, '0999.99') xyz from foo",
                "select CASE SIGN(c) WHEN -1 THEN N'-' ELSE N' ' END + RIGHT(N'000000000000000000000000000000'+LTRIM(STR(ABS(c), 7, 2)),7) xyz from foo");
    }
    
    public void testToCharNumericFormatOnlyDecimals() {
        try {
            String result = _performTranslation(
                "select to_char(c, '.999') xyz from foo", null);
            fail("Expected TranslationException, got: [" + result + "]");
        }
        catch (TranslationException e) {
            // Normal
        }
    }
    
    public void testToCharDateFormat() {
        _testTranslation(
                "select to_char(c, 'YYYYMMDDHH24MISS') xyz from foo",
                "select (CASE WHEN c is null THEN cast(null as nvarchar) ELSE " +
                "CONVERT(NVARCHAR, DATEPART(yy, c))+" +
                "RIGHT(N'00' + CONVERT(NVARCHAR, DATEPART(mm, c)), 2)+" +
                "RIGHT(N'00' + CONVERT(NVARCHAR, DATEPART(dd, c)), 2)+" +
                "RIGHT(N'00' + CONVERT(NVARCHAR, DATEPART(hh, c)), 2)+" +
                "RIGHT(N'00' + CONVERT(NVARCHAR, DATEPART(mi, c)), 2)+" +
                "RIGHT(N'00' + CONVERT(NVARCHAR, DATEPART(ss, c)), 2) " +
                "END) xyz from foo");
    }
    
    public void testToCharDateFormatWithPunctuation() {
        _testTranslation(
                "select to_char(c, 'YYYY/MM/DD HH24:MI:SS') xyz from foo",
                "select (CASE WHEN c is null THEN cast(null as nvarchar) ELSE " +
                "CONVERT(NVARCHAR, DATEPART(yy, c))+ N'/' +" +
                "RIGHT(N'00' + CONVERT(NVARCHAR, DATEPART(mm, c)), 2)+ N'/' +" +
                "RIGHT(N'00' + CONVERT(NVARCHAR, DATEPART(dd, c)), 2)+ N' ' +" +
                "RIGHT(N'00' + CONVERT(NVARCHAR, DATEPART(hh, c)), 2)+ N':' +" +
                "RIGHT(N'00' + CONVERT(NVARCHAR, DATEPART(mi, c)), 2)+ N':' +" +
                "RIGHT(N'00' + CONVERT(NVARCHAR, DATEPART(ss, c)), 2)" +
                "END) xyz from foo");
    }
    
    public void testAlternateDateFormat() {
        _testTranslation(
                "select to_char(c, 'MM/DD/YY') xyz from foo",
                "select (CASE WHEN c is null THEN cast(null as nvarchar) ELSE " +
                "RIGHT(N'00' + CONVERT(NVARCHAR, DATEPART(mm, c)), 2)+ N'/' +" +
                "RIGHT(N'00' + CONVERT(NVARCHAR, DATEPART(dd, c)), 2)+ N'/' +" +
                "RIGHT(CONVERT(NVARCHAR, DATEPART(yy, c)), 2)" +
                "END) xyz from foo");
    }
    
    public void testToCharOddDateFormats() {
        _testTranslation(
                "select to_char(d, 'YYQWW') year_qtr_week," +
                "       to_char(d, 'YYYYIW') year_week," +
                "       to_char(d, 'J') daynum from foo",
                "select (CASE WHEN d is null THEN cast(null as nvarchar) ELSE " +
                "        RIGHT(CONVERT(NVARCHAR, DATEPART(yy, d)), 2) + " +
                "        DATENAME(q, d)+" +
                "        RIGHT(N'00' + DATENAME(wk, d), 2) " +
                "       END) year_qtr_week, " +
                "       (CASE WHEN d is null THEN cast(null as nvarchar) ELSE " +
                "        CONVERT(NVARCHAR, DATEPART(yy, d))+" +
                "        RIGHT(N'00' + DATENAME(wk, d), 2)" +
                "       END) year_week, " +
                "       (CASE WHEN d is null THEN cast(null as nvarchar) ELSE " +
                "        CONVERT(NVARCHAR, CEILING(CONVERT(REAL, DATEDIFF(DAY, " +
                "        CONVERT(DATETIME, N'1753/01/01'), d))))" +
                "       END) daynum from foo");
        _testTranslation(
                "select to_char(d, 'DD-MONTH-YYYY') datename from foo",
                "select (CASE WHEN d is null THEN cast(null as nvarchar) ELSE" +
                "        RIGHT(N'00' + CONVERT(NVARCHAR, DATEPART(dd, d)), 2)+N'-'+" +
                "        UPPER(LEFT(CONVERT(NVARCHAR,DATENAME(mm, d))+SPACE(9), 9))+N'-'+" +
                "        CONVERT(NVARCHAR, DATEPART(yy, d)) END) datename from foo");
    }
    
    public void testToDateStringArg() {
        _testTranslation(
                "select to_date('20061123111520') dateval from dual",
                "select CONVERT(DATETIME, '2006-11-23 11:15:20', 20) dateval from dual");
    }
    
    public void testToDateDefaultFormat() {
        _testTranslation(
                "select to_date('20061123111520', 'YYYYMMDDHH24MISS') dateval from dual",
                "select CONVERT(DATETIME, '2006-11-23 11:15:20', 20) dateval from dual");
    }
    
    public void testToDateColumnArg() {
        try {
            String result = _performTranslation(
                    "select to_date(col) stuff from dual", null);
            fail("Expected TranslationException, got [" + result + "]");
        }
        catch (TranslationException e) {
            // Normal
        }
    }
    
    public void testToDateDefaultFormatBindVariable() {
        BindList bind = new BindList();
        bind.add("q01", "20061123111520");
        bind.add("q02", "YYYYMMDDHH24MISS");
        _testTranslation(
                "select to_date(:q01, :q02) dateval from dual",
                "select CONVERT(DATETIME, :q01__td, 20) dateval from dual",
                bind);
        Object tempBind = bind.getValue("q01__td");
        assertEquals("2006-11-23 11:15:20", tempBind);
    }
    
    public void testToDateDefaultFormatBadDateString() {
        BindList bind = new BindList();
        bind.add("q01", "2006112311Rxx20");
        bind.add("q02", "YYYYMMDDHH24MISS");
        try {
            String result = _performTranslation("select to_date(:q01, :q02) dateval from dual",
                bind);
            fail("Expected TranslationException, got [" + result + "]");
        }
        catch (TranslationException e) {
            // Normal
        }
    }
    
    public void testToDateDefaultFormatBindVariableNoTimePortion() {
        BindList bind = new BindList();
        bind.add("q01", "20061123");
        bind.add("q02", "YYYYMMDDHH24MISS");
        _testTranslation(
                "select to_date(:q01, :q02) dateval from dual",
                "select CONVERT(DATETIME, :q01__td, 20) dateval from dual",
                bind);
        Object tempBind = bind.getValue("q01__td");
        assertEquals("2006-11-23", tempBind);
    }
    
    public void testToDateNoFormatBindVariableNoTimePortion() {
        BindList bind = new BindList();
        bind.add("q01", "20061123");
        _testTranslation(
                "select to_date(:q01) dateval from dual",
                "select CONVERT(DATETIME, :q01__td, 20) dateval from dual",
                bind);
        Object tempBind = bind.getValue("q01__td");
        assertEquals("2006-11-23", tempBind);
    }
    
    public void testToDateNoFormatBindVariableNullValue() {
        BindList bind = new BindList();
        bind.add("q01", MocaType.STRING, null);
        _testTranslation(
                "select to_date(:q01) dateval from dual",
                "select CONVERT(DATETIME,:q01) dateval from dual",
                bind);
    }
    
    public void testToDateNoFormatNullValue() {
        _testTranslation(
                "select to_date(null) dateval from dual",
                "select CONVERT(DATETIME,null) dateval from dual");
    }
    
    public void testToDateNoFormatNoTimePortion() {
        _testTranslation(
                "select to_date('20061123') dateval from dual",
                "select CONVERT(DATETIME, '2006-11-23', 20) dateval from dual");
    }
    
    public void testToDateAlternateFormat() {
        _testTranslation(
                "select to_date('11/23/2006', 'MM/DD/YYYY') dateval from dual",
                "select CONVERT(DATETIME, '2006-11-23', 20) dateval from dual");
    }
    
    public void testToDateMonthFormat() {
        _testTranslation(
                "select to_date('23-NOV-2006', 'DD-MON-YYYY') dateval from dual",
                "select CONVERT(DATETIME, '23 NOV 2006', 13) dateval from dual");
    }
    
    public void testToDateMonthFormatWithTime() {
        _testTranslation(
                "select to_date('23-NOV-2006 15:13:02', 'DD-MON-YYYY HH24:MI:SS') dateval from dual",
                "select CONVERT(DATETIME, '23 NOV 2006 15:13:02', 13) dateval from dual");
    }
    
    public void testDoubledParentheses() {
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
    
    public void testDateDiffDays() {
        _testTranslation(
                "select moca_util.date_diff_days(ins_dt, sysdate) foo from sl_alg_def where rownum < 20",
                "select (datediff(dd, ins_dt, sysdate)+cast(datediff(ss,dateadd(dd,datediff(dd,ins_dt, sysdate),ins_dt), sysdate) as float)/86400.0) foo from sl_alg_def where rownum < 20");            
    }
    
    
    // @see com.redprairie.moca.db.translate.filter.TU_AbstractFilterTest#_getFilter()
    @Override
    protected TranslationFilter _getFilter() {
        return new MSFunctionFilter();
    }
}

