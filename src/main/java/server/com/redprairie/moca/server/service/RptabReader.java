/*
 *  $URL$
 *  $Author$
 *  $Date$
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

package com.redprairie.moca.server.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.util.StringReplacer;
import com.redprairie.util.VarStringReplacer;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author mlange
 */
public class RptabReader {
    public RptabReader(String environmentName) throws SystemConfigurationException {
        this(environmentName, null);
    }

    public RptabReader(String environmentName, String rptabPathname) throws SystemConfigurationException {
        if (environmentName == null || environmentName.isEmpty()) {
            throw new IllegalArgumentException("The environment name cannot be null or empty");
        }
        
        if (rptabPathname == null || rptabPathname.isEmpty()) {
            if (System.getProperty("os.name").startsWith("Windows"))
                rptabPathname = expandEnvironmentVariables("%AllUsersProfile%\\Application Data\\RedPrairie\\Server\\rptab");
            else
                rptabPathname = "/etc/rptab";
        }

        initialize(environmentName, rptabPathname);
    }
    
    /**
     * @return Returns the environment name.
     */
    public String getEnvironmentName() {
        return _environmentName;
    }
    
    /**
     * @return Returns the value of the LESDIR environment variable.
     */
    public String getLesdir() {
        return _lesdir;
    }
    
    /**
     * @return Returns the pathname of the registry file.
     */ 
    public String getRegistryPathname() {
        return _registryPathname;
    } 
    
    /*
     * Get the LESDIR and registry file pathname using the rptab file.
     */
    private void initialize(String environmentName, String rptabPathname) throws SystemConfigurationException {
              
        Reader reader = null;
        try {    
            reader = new InputStreamReader(new FileInputStream(rptabPathname), "UTF-8");
            parseRptabInfo(reader, environmentName);
        }
        catch (FileNotFoundException e) {
            throw new SystemConfigurationException("An rptab file could not be found", e);
        }
        catch (UnsupportedEncodingException e) {
            throw new SystemConfigurationException("Problem with file encoding", e);
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ignore) { }
            }
        }       
    }
    
    private void parseRptabInfo(Reader reader, String environmentName) throws SystemConfigurationException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(reader);
            String buffer;
            while ((buffer = in.readLine()) != null) {

                // Trim the string just to make sure we don't include any trailing whitespace.
                String line = buffer.trim();   
                
                // Skip over any comments.
                if (line.startsWith("#")) {
                    continue;
                }
                
                // Did we find the given environment name?
                String[] tokens = line.split(File.pathSeparator);
                if (tokens.length >= 1 && tokens[0].equalsIgnoreCase(environmentName)) {
                    _environmentName = environmentName;
                    setLesdir(tokens);
                    setRegistryPathname(tokens);
                    break;
                }      
            }
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            throw new SystemConfigurationException("Error reading rptab file", e);
        }
        finally {
            if (in != null) {
                try { in.close(); } catch (IOException ignore) {}
            }
        }    
    }   
    
    private void setLesdir(String[] tokens) throws SystemConfigurationException {      
        if (tokens.length >= 2)
            _lesdir = tokens[1];
        
        File file = new File(_lesdir);
        if (!file.exists())
            throw new SystemConfigurationException("LESDIR does not exist: " + _lesdir);
    }
    
    private void setRegistryPathname(String[] tokens) throws SystemConfigurationException {
        
        // Registry file pathname provided in the rptab file.
        if (tokens.length >= 3 && !tokens[2].isEmpty()) {
            _registryPathname = tokens[2];
            return;
        }
        
        // The data directory is always the same so let's build it only once.
        String dataDir = _lesdir + File.separator + "data" + File.separator;
        
        // Check for %LESDIR%\data\registry.%USERNAME%
        String pathname;
        if (System.getProperty("os.name").startsWith("Windows"))
            pathname = expandEnvironmentVariables(dataDir + "registry.%USERNAME%");
        else
            pathname = expandEnvironmentVariables(dataDir + "registry.%LOGNAME%");
        if (new File(pathname).exists()) {
            _registryPathname = pathname;
            return;
        }
        
        // Check for %LESDIR%\data\registry.%MOCA_ENVNAME%
        pathname = expandEnvironmentVariables(dataDir + "registry.%MOCA_ENVNAME%");
        if (new File(pathname).exists()) {
            _registryPathname = pathname;
            return;
        }
        
        // Check for %LESDIR%\data\registry
        pathname = expandEnvironmentVariables(dataDir + "registry");
        if (new File(pathname).exists()) {
            _registryPathname = pathname;
            return;
        }

        throw new SystemConfigurationException("A registry file could not be found");
    }     
    
    private String expandEnvironmentVariables(String path) {                                              
        return new VarStringReplacer(new StringReplacer.ReplacementStrategy() {
            public String lookup(String name) {
                // LESDIR may not be in our environment yet, so...
                if (name.equalsIgnoreCase("LESDIR")) {
                    return _lesdir;
                }
                else if (name.equalsIgnoreCase("MOCA_ENVNAME")) {
                    return _environmentName;
                }
                else {
                    return System.getenv(name);
                }
            }
        }).translate(path);
    }
    
    private String _environmentName = null;
    private String _lesdir = null;
    private String _registryPathname = null;
}