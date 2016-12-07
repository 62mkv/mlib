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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.registry.RegistryReader;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author mlange
 */
public class JavaStartStopInfo extends StartStopInfo {
    
    JavaStartStopInfo(String className, String[] args) {
        super();
        _className = className;
        _args = args;
    }
    
    void exec(String applicationName, Map<String, String> environment) throws ServiceManagerException {    
        
        System.out.println("Starting application: " + applicationName);
        
        // Get a registry reader.
        String registryPathname = environment.get("MOCA_REGISTRY");    
        RegistryReader registryReader;
        try {
            registryReader = new RegistryReader(new File(registryPathname));
        }
        catch (SystemConfigurationException e) {
            e.printStackTrace();
            return;
        }
        
        /*
         * The Java executable we will use is determined using the following precedence:
         * 
         * 1. java.vm registry key value
         * 2. "java"
         */
        String java = registryReader.getConfigurationElement(MocaRegistry.REGKEY_JAVA_VM, true);
        if (java == null || java.isEmpty())
            java = "java";
        
        /*
         * The Java arguments we will use is determined using the following precedence:
         * 
         * 1. java.vmargs.<application> registry key value
         * 2. java.vmargs
         * 3. Don't use any
         */      
        String vmargsApplicationKey = null;
        
        // java.vmargs.<application>
        vmargsApplicationKey = MocaRegistry.REGKEY_JAVA_VMARGS + "." + applicationName;
        String vmargs = registryReader.getConfigurationElement(vmargsApplicationKey, true); 
        
        // java.vmargs
        if (vmargs == null || vmargs.isEmpty()) {
            vmargs = registryReader.getConfigurationElement(MocaRegistry.REGKEY_JAVA_VMARGS, true);
            if (vmargs == null || vmargs.isEmpty())
                vmargs = "";
        }
        
        // We need to add -Xrs to all Windows versions < 6.0.             
        try {
            String osName = System.getProperty("os.name").toLowerCase(); 
            Double osVersion = Double.parseDouble(System.getProperty("os.version"));   
            
            if (osName.contains("windows") && osVersion < 6.0)
                vmargs += " -Xrs";
        }
        catch (NumberFormatException ignore) {
            // Ignore exceptions raised parsing the OS version.
            ;
        }
        
        // Build the actual command.
        List<String> command = new ArrayList<String>();
        command.add(java);
        
        if (vmargs != null && !vmargs.isEmpty()) {
            // we need to add each vm arg separately
            Collections.addAll(command, vmargs.split("\\s+"));
        }
        
        command.add(_className);
        
        for (String arg : _args) {
            command.add(arg);
        }
              
        // Start the actual service.
        try {
            System.out.println("Executing command: " + command);
            System.out.println(); 
            ServiceTools.executeCommand(command, environment, _debug ? System.out : null);
        }
        catch (ServiceManagerException e) {
            e.printStackTrace();
            return;
        }
    }
    
    public String toString() {
        
        StringBuilder str  = new StringBuilder();
        
        str.append(_className + ".main");    
        str.append("(");  
        
        for (int i = 0; i < _args.length; i++) {
            str.append(_args[i]);
            if (i != (_args.length-1)) {
                str.append(", ");
            }
        } 
        
        str.append(")");
        
        return str.toString();
    }
    
    private final String _className;
    private final String[] _args;
}
