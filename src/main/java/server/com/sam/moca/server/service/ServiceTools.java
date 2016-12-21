/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.sam.moca.server.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.sam.moca.MocaRegistry;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.server.registry.RegistryReader;
import com.sam.util.CommandLineParser;

public class ServiceTools {
    
    /*
     * Get a map of environment name/value pairs for the given environment name.
     */  
    static Map<String, String> getEnvironment(String environmentName) throws ServiceManagerException {
        
        if (_environment == null)
            buildEnvironment(environmentName); 
        
        return _environment;
    }
    
    /*
     * Execute the given command in the current environment, read its stdout & stderr
     * and write it back to our own stdout.
     */
    static void executeCommand(String command) throws ServiceManagerException {
        executeCommand(command, null);
    }
    
    static void executeCommand(String command, Map<String, String> environment, PrintStream stream) throws ServiceManagerException { 
        List<String> argv = CommandLineParser.split(command);
        executeCommand(argv, environment, stream);
    }
    
    /*
     * Execute the given command with the given environment, read its stdout & stderr
     * and write it back to the given PrintStream if a stream is provided.
     */  
    static void executeCommand(List<String> command, Map<String, String> environment, PrintStream stream) throws ServiceManagerException {      
        
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);      
            
            if (environment != null) {
                Map<String, String> env = pb.environment();
                env.putAll(environment); 
            }
            
            Process p = pb.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.defaultCharset()));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (stream != null)
                        stream.println(line);
                }
            }
            catch (IOException e) {
                throw new ServiceManagerException("Could not gobble stdout from command", e);
            } 
            finally {
                try { reader.close(); } catch (IOException e) { e.printStackTrace(); }
            }
            
            int exitValue = p.waitFor();
            if (exitValue != 0)
                throw new ServiceManagerException("Command exited with status " + exitValue);      
        }
        catch (IOException e) {
            throw new ServiceManagerException("Command could not be found", e);
        }
        catch (InterruptedException e) {
            throw new ServiceManagerException("Command was interrupted", e);
        }       
    }
    
    /*
     * Execute the given command with the given environment, read its stdout & stderr
     * and write it back to our own stdout.
     */  
    static void executeCommand(String command, Map<String, String> environment) throws ServiceManagerException {
        executeCommand(command, environment, System.out);
    }

    static String getServiceName(String applicationName, String environmentName) {
        return applicationName + "." + environmentName;  
    }
    
    static String getApplicationName(String serviceName) {
        String[] tokens = serviceName.split("\\.", 2);
        if (tokens.length != 2)
            return null;
        
        return tokens[0];
    }

    static String getEnvironmentName(String serviceName) {
        String[] tokens = serviceName.split("\\.", 2);
        if (tokens.length != 2)
            return null;
        
        return tokens[1];
    }
    
    private static synchronized void buildEnvironment(String environmentName) throws ServiceManagerException {
        String registryPathname = null;
        try {
            RptabReader reader = new RptabReader(environmentName);          
            registryPathname = reader.getRegistryPathname();
            if (registryPathname == null)
                throw new ServiceManagerException("The environment '" + environmentName + "' does not exist");
        }
        catch (SystemConfigurationException e) {
            throw new ServiceManagerException("Could not read the rptab file", e);
        }
        catch (IllegalArgumentException e) {
            throw new ServiceManagerException("Invalid environment name argument", e);
        }
        
        try {
            Reader reader = new InputStreamReader(new FileInputStream(registryPathname), "UTF-8");
            RegistryReader registryReader = new RegistryReader(reader);
            _environment = registryReader.getConfigurationSection(MocaRegistry.REGSEC_ENVIRONMENT, true);
        }
        catch (FileNotFoundException e) {
            throw new ServiceManagerException("Registry file not found: " + registryPathname, e);
        }
        catch (SystemConfigurationException e) {
            throw new ServiceManagerException("A problem occurred parsing the registry file: " + registryPathname, e);
        }
        catch (UnsupportedEncodingException e) {
            throw new ServiceManagerException("A problem occurred reading the registry file: " + registryPathname, e);
        }   
        
        // We always need to include the environment name and registry pathname as well.
        _environment.put("MOCA_ENVNAME", environmentName);
        _environment.put("MOCA_REGISTRY", registryPathname);   
    }
    
    private static Map<String, String> _environment = null;
}