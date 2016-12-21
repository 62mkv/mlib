/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.sam.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A filename filter that searches for files with a particular suffix.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class SuffixFilenameFilter implements FilenameFilter {
    public SuffixFilenameFilter(String suffix) {
        if (suffix.startsWith(".")) {
            _suffix = suffix;
        }
        else {
            _suffix = "." + suffix;
        }
    }

    // @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(_suffix);
    }
    
    private final String _suffix;
}
