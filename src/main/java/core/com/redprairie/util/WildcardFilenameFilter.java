/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A filename filter that translates glob formats into the necessary regular
 * expression
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author gvaneyck
 * @version $Revision$
 */
public class WildcardFilenameFilter implements FilenameFilter {

    public WildcardFilenameFilter(String expression) {
        super();
        // Replace the ? with a dot, a dot with backslash dot and a star
        // with dot star to be compliant with regular expression for
        // our wildcards
        _regularExpression = expression.replace(".", "\\.").replace('?', '.').replace("*", ".*");
    }

    @Override
    public boolean accept(File dir, String name) {
        if (name.matches(_regularExpression)) {
            return true;
        }
        else {
            return false;
        }
        
    }
    
    // We hold a regular expression in the background
    private final String _regularExpression;
}
