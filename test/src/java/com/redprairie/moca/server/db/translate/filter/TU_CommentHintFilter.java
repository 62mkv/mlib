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

package com.redprairie.moca.server.db.translate.filter;

import com.redprairie.moca.server.db.translate.TranslationOptions;


/**
 * Unit tests for SQLTokenizer
 * 
 * <b><pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_CommentHintFilter extends TU_AbstractFilterTest {
    public void testSimpleHint() {
        TranslationOptions options = new TranslationOptions();
        _testTranslationWithOptions(
            "/*#nolimit*/select * from foo",
            "/*#nolimit*/select * from foo",
            options);
        assertTrue(options.hasHint("nolimit"));
    }
    
    public void testSimpleHintWithPreceedingWhiteSpace() {
        TranslationOptions options = new TranslationOptions();
        _testTranslationWithOptions(
            " /*#nolimit*/select * from foo",
            " /*#nolimit*/select * from foo",
            options);
        assertTrue(options.hasHint("nolimit"));
    }
    
    public void testHintWithValue() {
        TranslationOptions options = new TranslationOptions();
        _testTranslationWithOptions(
            "/*#rowlimit=10000*/select * from foo",
            "/*#rowlimit=10000*/select * from foo",
            options);
        assertTrue(options.hasHint("rowlimit"));
        assertEquals("10000", options.getHintValue("rowlimit"));
    }
    
    public void testHintWithWhiteSpace() {
        TranslationOptions options = new TranslationOptions();
        _testTranslationWithOptions(
            "/*# rowlimit = 10000 */select * from foo",
            "/*# rowlimit = 10000 */select * from foo",
            options);
        assertTrue(options.hasHint("rowlimit"));
        assertEquals("10000", options.getHintValue("rowlimit"));
    }
    
    // @see com.redprairie.moca.db.translate.filter.TU_AbstractFilterTest#_getFilter()
    @Override
    protected TranslationFilter _getFilter() {
        return new CommentHintFilter();
    }
}
