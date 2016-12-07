/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.server.repository;

import java.util.regex.Pattern;

/**
 * This filter can be used to have a regular expression to match a given
 * component library to it's name.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class RegularExpressionComponentLibraryNameFilter implements
        ComponentLibraryFilter {
    
    /**
     * Creates a regular expression from the passed in string with the case 
     * insensitive option enabled.
     * @param expression
     */
    public RegularExpressionComponentLibraryNameFilter(String expression) {
        _pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
    }
    
    public RegularExpressionComponentLibraryNameFilter(Pattern pattern) {
        _pattern = pattern;
    }

    // @see com.redprairie.moca.server.repository.ComponentLibraryFilter#accept(com.redprairie.moca.server.repository.ComponentLevel)
    @Override
    public boolean accept(ComponentLevel level) {
        String name = level.getName();
        return _pattern.matcher(name).matches();
    }
    
    private final Pattern _pattern;
}
