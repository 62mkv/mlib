/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.redprairie.moca.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.redprairie.util.ArgCheck;

/**
 * Utils class for non-specific test functionality
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author j1014071
 */
public class TestUtils {

    // Use for testing
    private static final TestUtils _instance = new TestUtils();
    
    /**
     * A timeout value for a particular test can be overridden by setting an environment variable
     * of the form: "{CLASSNAME}_{VARNAME}_TIMEOUT"
     * @param cls
     * @param name
     * @param defaultValue
     * @return
     */
    public static <T> int getTestTimeout(Class<T> cls, String name, int defaultValue){
        return _instance._getTestTimeout(cls, name, defaultValue);
    }

    /**
     * Get integer value from environment variable
     * @param varName
     * @param defaultValue
     * @return
     */
    public static int getEnvInteger(String varName, int defaultValue){
        return _instance._getEnvInteger(varName, defaultValue);
    }

    /**
     * Reads a file and dumps its lines to a log
     * @throws IOException
     */
    public static void dumpFileContentsToLog(File _logFile, Logger _logger) throws IOException {
        ArgCheck.notNull(_logFile, "_logFile cannot be null");
        ArgCheck.notNull(_logger, "_logger cannot be null");

        if(!_logFile.isFile()){
            _logger.error("Missing file: " + _logFile.getAbsolutePath());
        } else {
            List<String> lines = Files.readLines(_logFile, Charsets.UTF_8);
            for(String line: lines){
                _logger.info(line);
            }
        }
    }

    /**
     * Used for testing only
     */
    <T> int _getTestTimeout(Class<T> cls, String name, int defaultValue){
        String varName = (cls.getSimpleName() + "_" + name + "_TIMEOUT").toUpperCase(Locale.ROOT);
        int result = this._getEnvInteger(varName, defaultValue);
        return result;
    }
    
    /**
     * Used for testing only
     */
    int _getEnvInteger(String varName, int defaultValue){
        String value = this._getEnv(varName);
        if(value != null && !value.isEmpty()){
            int newValue = Integer.parseInt(value);
            if(newValue > 0){
                return newValue;
            }
        }        
        return defaultValue;
    }
    
    /**
     * Used for testing only
     */
    String _getEnv(String varName){
        return System.getenv(varName);
    }

}
