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

import com.redprairie.moca.server.db.translate.TranslationException;
import com.redprairie.moca.server.db.translate.filter.TU_AbstractFilterTest;
import com.redprairie.moca.server.db.translate.filter.TranslationFilter;


/**
 * Unit tests for MSForUpdateFilter
 * 
 * <b><pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_MSForUpdateFilter extends TU_AbstractFilterTest {
    public void testNoForUpdate() {
        _testTranslation(
                "select * from foo.bar xyz123 where xyz123.x is null",
                "select * from foo.bar xyz123 where xyz123.x is null");
        _testTranslation(
                "select sysdate from dual where 1=1",
                "select sysdate from dual where 1=1");
    }
    
    public void testSimpleSelectForUpdateNoWait() {
        _testTranslation(
                "select * from foo where x = 1234 for update nowait",
                "select * from foo (UPDLOCK) where x = 1234");
    }
    
    public void testSimpleSelectForUpdate() {
        _testTranslation(
                "select * from foo where x = 1234 for update of x",
                "select * from foo (UPDLOCK) where x = 1234");
    }
    
    public void testForUpdateOfNowait() {
        _testTranslation(
                "select * from foo where x = 1234 for update of x NOWAIT",
                "select * from foo (UPDLOCK) where x = 1234");
    }

    public void testSimpleJoin() {
        _testTranslation(
                "select * from foo, bar where x = 1234 for update of foo.x",
                "select * from foo (UPDLOCK), bar where x = 1234");
        _testTranslation(
            "select * from foo, bar where x = 1234 for update of bar.x",
            "select * from foo, bar (UPDLOCK) where x = 1234");
    }

    public void testSimpleJoinWithAlias() {
        _testTranslation(
                "select * from foo f, bar b where x = 1234 for update of f.x",
                "select * from foo f (UPDLOCK), bar b where x = 1234");
        _testTranslation(
            "select * from foo f, bar b where x = 1234 for update of b.x",
            "select * from foo f, bar b (UPDLOCK) where x = 1234");
    }

    public void testSimpleJoinMultipleLocks() {
        _testTranslation(
                "select * from foo f, bar b where x = 1234 for update of f.x, b.y",
                "select * from foo f (UPDLOCK), bar b (UPDLOCK) where x = 1234");
        _testTranslation(
            "select * from foo, bar where x = 1234 for update of foo.x, bar.y",
            "select * from foo(UPDLOCK), bar(UPDLOCK) where x = 1234");
    }

    public void testAnsiOuterJoinSingleLock() {
        _testTranslation(
                "select * from foo f outer join bar b on x = 1234 for update of f.x",
                "select * from foo f (UPDLOCK) outer join bar b on x = 1234");
        _testTranslation(
            "select * from foo f outer join bar b on x = 1234 where y = 'test' for update of b.x",
            "select * from foo f outer join bar b (UPDLOCK) on x = 1234 where y = 'test'");
    }

    public void testOuterJoinWithSubquery() throws TranslationException {
        _testTranslation(
                "select invsub.* from invsub " +
                "left outer join (select lodnum foo, count(*) locqty from invlod group by lodnum) " +
                "on invsub.lodnum = foo " +
                "where invsub.lodnum = 'XXX' for update of invsub.lodnum",
                "select invsub.* from invsub (UPDLOCK) " +
                "left outer join (select lodnum foo, count(*) locqty from invlod group by lodnum) " +
                "on invsub.lodnum = foo " +
                "where invsub.lodnum = 'XXX'");
    }

    
    // @see com.redprairie.moca.db.translate.filter.TU_AbstractFilterTest#_getFilter()
    @Override
    protected TranslationFilter _getFilter() {
        return new MSForUpdateFilter();
    }
}
