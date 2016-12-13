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
public class TU_CommentFilter extends TU_AbstractFilterTest {
    public void testLeadingComment() {
        _testTranslation(
                "/*XYZ*/select * from foo.bar xyz123 where xyz123.x = 'test'",
                "select * from foo.bar xyz123 where xyz123.x = 'test'");
    }
    
    public void testCommentedOutCode() {
        _testTranslation(
                "select z, x from foo where foo.x = 100 /*and bar.z = foo.z*/",
                "select z, x from foo where foo.x = 100 ");
    }
    
    // @see com.redprairie.moca.db.translate.filter.TU_AbstractFilterTest#_getFilter()
    @Override
    protected TranslationFilter _getFilter() {
        return new CommentFilter();
    }
}

