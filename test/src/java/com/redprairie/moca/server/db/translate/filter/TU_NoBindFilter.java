/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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
 * TODO Class Description
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_NoBindFilter extends TU_AbstractFilterTest {
    
    public void testBindVariableReplaced() {
        BindList args = new BindList();
        args.add("foo", "test");
        _testTranslation(
                "select * from foo.bar xyz123 where xyz123.x = /*#nobind*/:foo/*#bind*/",
                "select * from foo.bar xyz123 where xyz123.x = /*#nobind*/N'test'/*#bind*/", args);
    }
    
    public void testCommentedOutValue() {
        _testTranslation(
                "select z, x from foo where foo.x = 100 /*and bar.z = 'TEST'*/",
                "select z, x from foo where foo.x = 100 /*and bar.z = 'TEST'*/");
    }
    
    public void testCommentedOutValueWithNoBind() {
        BindList args = new BindList();
        args.add("foo", 10);
        args.add("z", "TEST");
        _testTranslation(
                "select z, x from foo where foo.x = /*#nobind*/:foo /*and bar.z = :z*/",
                "select z, x from foo where foo.x = /*#nobind*/10 /*and bar.z = :z*/", args);
    }
    
    // @see com.redprairie.moca.server.db.translate.filter.TU_AbstractFilterTest#_getFilter()
    @Override
    protected TranslationFilter _getFilter() {
        return new NoBindFilter(true);
    }

}
