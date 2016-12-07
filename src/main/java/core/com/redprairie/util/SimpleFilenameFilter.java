/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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
 * This is a simple filename filter that just matches the file name exactly
 * to a string ignoring case.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class SimpleFilenameFilter implements FilenameFilter {

    public SimpleFilenameFilter(String fileName) {
        _fileName = fileName;
    }
    
    // @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
    @Override
    public boolean accept(File dir, String name) {
        if (name.equalsIgnoreCase(_fileName)) {
            return true;
        }
        return false;
    }
    
    private final String _fileName;
}
