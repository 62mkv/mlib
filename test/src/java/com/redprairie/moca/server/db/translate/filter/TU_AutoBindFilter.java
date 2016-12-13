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

package com.redprairie.moca.server.db.translate.filter;

import com.redprairie.moca.server.db.BindList;



/**
 * Unit tests for SQLTokenizer
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_AutoBindFilter extends TU_AbstractFilterTest {
    public void testLastValue() {
        _testTranslation(
                "select * from foo.bar xyz123 where xyz123.x = 'test'",
                "select * from foo.bar xyz123 where xyz123.x = :q0");
    }
    
    public void testCommentedOutValue() {
        _testTranslation(
                "select z, x from foo where foo.x = 100 /*and bar.z = 'TEST'*/",
                "select z, x from foo where foo.x = :i0 /*and bar.z = 'TEST'*/");
    }
    
    public void testNullValue() {
        BindList args = new BindList();
        _testTranslation(
            "select 'x', '', to_date('', 'YYYYMMDDHH24MISS') from dual",
            "select :q0, :q1, to_date(:q2, :q3) from dual",
            args);
        assertEquals("x", args.getValue("q0"));
        assertNull(args.getValue("q1"));
        assertNull(args.getValue("q2"));
        assertEquals("YYYYMMDDHH24MISS", args.getValue("q3"));
    }
    
    public void testInhibitHint() {
        BindList args = new BindList();
        _testTranslation(
            "select 'x', '', /*#NOBIND*/to_date('', 'YYYYMMDDHH24MISS') /*#BIND*/ from dual where x=2",
            "select :q0, :q1, /*#NOBIND*/to_date('', 'YYYYMMDDHH24MISS') /*#BIND*/ from dual where x=:i2",
            args);
        assertEquals("x", args.getValue("q0"));
        assertNull(args.getValue("q1"));
        assertEquals(2, args.getValue("i2"));
    }
    
    public void testLargeIntegerBind() {
        BindList args = new BindList();
        _testTranslation(
            "select 'x', '', 9999999999 z from dual where x=2",
            "select :q0, :q1, 9999999999 z from dual where x=:i2",
            args);
        assertEquals("x", args.getValue("q0"));
        assertNull(args.getValue("q1"));
        assertEquals(2, args.getValue("i2"));
    }
    
    public void testVeryLargeIntegerBind() {
        BindList args = new BindList();
        _testTranslation(
            "select 'x', '', 999999999999999999999999 z from dual where x=2",
            "select :q0, :q1, 999999999999999999999999 z from dual where x=:i2",
            args);
        assertEquals("x", args.getValue("q0"));
        assertNull(args.getValue("q1"));
        assertEquals(2, args.getValue("i2"));
    }
    
    public void testNoBindAfterOrderBy() {
        BindList args = new BindList();
        _testTranslation(
            "select a, b, c from foo union select a, b, c from bar order by 2, 1",
            "select a, b, c from foo union select a, b, c from bar order by 2, 1",
            args);
        assertTrue(args.isEmpty());
    }
    
    /**
     * Order by in subquery isn't ANSI SQL but it's supported by Oracle and can be used in
     * SQL Server if used in conjunction with TOP. Therefore, this tests that we don't bind
     * anything in the order by but do bind after it.
     */
    public void testBindAfterOrderByInSubQuery() {
    	_testTranslation(
                "select * from (select * from task_definition where auto_start = 1 order by 2, 1) tmp where rownum < 5",
                "select * from (select * from task_definition where auto_start = :i0 order by 2, 1) tmp where rownum < :i1");
    }
    
    public void testNoBindOnIntegerClause() {
        BindList args = new BindList();
        _testTranslation(
            "select * from foo where bar=100 and 1=1 and 1=1",
            "select * from foo where bar=:i0 and 1=1 and 1=1",
            args);
        assertEquals(100, args.getValue("i0"));
        args = new BindList();
        _testTranslation(
            "select * from foo where bar=100 and 1=1 and 1=0",
            "select * from foo where bar=:i0 and 1=1 and 1=0",
            args);
        assertEquals(100, args.getValue("i0"));
    }
    
    // @see com.redprairie.moca.db.translate.filter.TU_AbstractFilterTest#_getFilter()
    @Override
    protected TranslationFilter _getFilter() {
        return new AutoBindFilter();
    }
}

