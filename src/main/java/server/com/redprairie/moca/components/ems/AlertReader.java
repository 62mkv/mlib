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

package com.redprairie.moca.components.ems;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;

import com.redprairie.moca.alerts.AlertFile;
import com.redprairie.moca.alerts.EMSException;
import com.redprairie.moca.alerts.util.AlertUtils;

/**
 * Reads the pertinent EMS alert files.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author Brandon Grady
 * @version $Revision$
 *
 */
public class AlertReader {

    /**
     * Transports messages to the EMS system.
     * 
     * @param moca the MOCA context. This argument cannot be null.
     * @param fileName Full path to the specific file (trg or xml) to 
     *        transport.
     * @throws EMSException 
     */
    public AlertReader (String fileName) throws EMSException {
        _alertFiles.add(new AlertFile(fileName));
    }

    /**
     * Transports all messages in the spooler directory to the EMS system.
     * @throws EMSException 
     */
    public AlertReader() throws EMSException {
        
        // Go to the SPOOL directory and list all the TRIGGER files.
        File dir = AlertUtils.getSpoolDir();
        
        File[] files = dir.listFiles(new SuffixFilter(AlertFile.TRG_EXT));
        
        for (File file : files) {
            _alertFiles.add(new AlertFile(file));
        }
    }
    
    /**
     * A filename filter that searches for files with a particular 
     * suffix.
     */
    public static class SuffixFilter implements FilenameFilter {
        public SuffixFilter(String suffix) {
            if (suffix .startsWith(".")) {
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
    } // TODO - copied this from CommandRepositoryReader - can we share it?
    
    /**
     * Returns an array of the files read.
     * @return
     */
    public AlertFile[] getAlertFiles() {
        return (AlertFile[]) 
            _alertFiles.toArray(new AlertFile[_alertFiles.size()]);
    }
    
    // ----------------------------------
    // Implementation
    // ----------------------------------
    private Set<AlertFile> _alertFiles = new HashSet<AlertFile>();
}
