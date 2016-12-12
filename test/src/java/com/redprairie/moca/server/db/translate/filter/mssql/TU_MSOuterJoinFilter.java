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

import com.redprairie.moca.server.db.translate.filter.TU_AbstractFilterTest;
import com.redprairie.moca.server.db.translate.filter.TranslationFilter;


/**
 * Unit tests for MSOuterJoinFilter
 * 
 * <b><pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_MSOuterJoinFilter extends TU_AbstractFilterTest {
    public void testNoOuterJoin() {
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
    
    public void testSimpleOuterJoin() {
        _testTranslation(
            "select x from foo,bar where foo.a(+) = bar.a",
            "select x from foo,bar where foo.a =* bar.a");
        _testTranslation(
            "select x from foo,bar where foo.a = bar.a(+)",
            "select x from foo,bar where foo.a *= bar.a");
    }
    
    public void testMultipleEqualsOperators() {
        _testTranslation(
            "select x from foo,bar where foo.a(+) = bar.a and bar.x = 'abc'",
            "select x from foo,bar where foo.a =* bar.a and bar.x = 'abc'");
        _testTranslation(
            "select x from foo,bar where foo.a = bar.a(+) and bar.x = 'abc'",
            "select x from foo,bar where foo.a *= bar.a and bar.x = 'abc'");
    }
    
    public void testOuterJoinInSubquery() {
        _testTranslation(
            "select * from (select foo.a, bar.b from foo,bar where foo.a (+) = bar.a) x where x.b = 'abc'",
            "select * from (select foo.a, bar.b from foo,bar where foo.a =* bar.a) x where x.b = 'abc'");
        _testTranslation(
            "select * from (select foo.a, bar.b from foo,bar where foo.a = bar.a(+)) x where x.b = 'abc'",
            "select * from (select foo.a, bar.b from foo,bar where foo.a *= bar.a) x where x.b = 'abc'");
    }
    
    public void testComplexExpression() {
        _testTranslation(
            "select x from foo,bar where nvl(foo.a(+), 'hello') = bar.a",
            "select x from foo,bar where nvl(foo.a, 'hello') =* bar.a");
        _testTranslation(
            "select x from foo,bar where foo.a = nvl(bar.a(+), 'blah')",
            "select x from foo,bar where foo.a *= nvl(bar.a, 'blah')");
    }
    
    // @see com.redprairie.moca.db.translate.filter.TU_AbstractFilterTest#_getFilter()
    @Override
    protected TranslationFilter _getFilter() {
        return new MSOuterJoinFilter();
    }
}
