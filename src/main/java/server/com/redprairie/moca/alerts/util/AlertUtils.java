/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005-2009
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

package com.redprairie.moca.alerts.util;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.util.MocaUtils;

/**
 * Set of utility functions used in the EMS sections of MOCA.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author Brandon Grady
 * @version $Revision$
 */
public class AlertUtils {
    
    public static final String PROCESSED_DIR_DEFAULT = "$LESDIR/files/emsout/prc";
    public static final String BAD_DIR_DEFAULT = "$LESDIR/files/emsout/bad";
    public static final String SPOOL_DIR_DEFAULT = "$LESDIR/files/emsout";
    public static final String LOCALE_DEFAULT = "US_ENGLISH";

    /**
     * Gets the EMS spool directory from the registry, or it returns the 
     * default which is $LESDIR/files/emsout
     * @return
     */
    public static File getSpoolDir() {
        return SpoolDirHolder._spoolDirectory;
    }
    
    /**
     * This class is just used for lazy initialization of the spool directory
     * object.  This will be initialized the first time the class is called and
     * is thread safe in doing so.
     */
    private static class SpoolDirHolder {
        static final File _spoolDirectory = getEMSDir(
                MocaRegistry.REGKEY_EMS_SPOOL_DIR, 
                SPOOL_DIR_DEFAULT);
    }
    
    /**
     * Gets the EMS processed directory from the registry, or it returns the 
     * default which is $LESDIR/files/emsout/prc
     * @return
     */
    public static File getProcessedDir() {
        return ProcessDirHolder._processedDirectory;
    }
    
    /**
     * This class is just used for lazy initialization of the process directory
     * object.  This will be initialized the first time the class is called and
     * is thread safe in doing so.
     */
    private static class ProcessDirHolder {
        static final File _processedDirectory = getEMSDir(
                MocaRegistry.REGKEY_EMS_PROCESSED_DIR, 
                PROCESSED_DIR_DEFAULT);
    }
    
    /**
     * Gets the EMS bad directory from the registry, or it returns the 
     * default which is $LESDIR/files/emsout/bad
     * @return
     */
    public static File getBadDir() {
        return BadDirHolder._badDirectory;
    }
    
    /**
     * This class is just used for lazy initialization of the bad directory
     * object.  This will be initialized the first time the class is called and
     * is thread safe in doing so.
     */
    private static class BadDirHolder {
        static final File _badDirectory = getEMSDir(
                MocaRegistry.REGKEY_EMS_BAD_DIR, 
                BAD_DIR_DEFAULT);
    }
    
    /**
     * Gets the EMS URL from the registry.
     */
    public static String getUrl() {
        if (_url.equals("")) {
            synchronized (AlertUtils.class) {
                final MocaContext moca = MocaUtils.currentContext();
                _url = moca.getRegistryValue(MocaRegistry.REGKEY_EMS_URL);
            }
        }
        return _url;
    }
    
    /**
     * @return
     */
    public static String getDefaultLocale() {
        if (_locale.equals("")) {
            synchronized (AlertUtils.class) {
                final MocaContext moca = MocaUtils.currentContext();
                _locale = moca.getSystemVariable("LOCALE_ID");
                if (_locale == null || _locale.equals("")) {
                    _locale = LOCALE_DEFAULT;
                }
            }
        }
        
        return _locale;
    }

    // ----------------------------
    // Implementation
    // ----------------------------
    
    /**
     * Returns the directory or the pointer to the default path. 
     */
    private static File getEMSDir(String registryKey, String defaultPath) {
        final MocaContext moca = MocaUtils.currentContext();

        String path = moca.getRegistryValue(registryKey);

        // If the registry entry is not set, use the default
        if (path == null) {
            _logger.debug(registryKey + " not found - using default.");
            path = MocaUtils.expandEnvironmentVariables(moca, defaultPath);
            return new File(path);
        }
        
        // Check if the directory exists, fall back on default if necessary
        File file = new File(path);
        if (!file.exists()) {
            _logger.debug(registryKey + " not found - using default.");
            path = MocaUtils.expandEnvironmentVariables(moca, defaultPath);
            return new File(path);
        }
        else {
            return file;
        }
    }
    
    // Implementation
    private static final Logger _logger = LogManager.getLogger(AlertUtils.class);
    private static String _url = "";
    private static String _locale = "";
}
