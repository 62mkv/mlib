/*
 *  $URL$
 *  $Author$
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

import com.redprairie.moca.server.db.translate.filter.TU_AbstractFilterTest;
import com.redprairie.moca.server.db.translate.filter.TranslationFilter;


/**
 * Unit tests for NStringLiteralFilter
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author mlange
 */
public class TU_NStringLiteralFilter extends TU_AbstractFilterTest {
    
    public void testSimpleNStringLiteral() { 
        _testTranslationWithWhitespace(
            "select * from foo where bar like 'myvalue'",
            "select * from foo where bar like N'myvalue'"); 
    }
    
    public void testScrunchedNStringLiteral() {
        _testTranslationWithWhitespace(
            "select * from foo where bar like'myvalue'",
            "select * from foo where bar like N'myvalue'");
    }
    
    public void testMultilineNStringLiteral() {
        _testTranslationWithWhitespace(
            "select * from foo where bar like\n'myvalue'",
            "select * from foo where bar like\nN'myvalue'");
    }
    
    // @see com.redprairie.moca.db.translate.filter.TU_AbstractFilterTest#_getFilter()
    @Override
    protected TranslationFilter _getFilter() {
        return new NStringLiteralFilter();
    }
}


