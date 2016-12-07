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
public class Java32StartStopInfo extends StartStopInfo {
    
    Java32StartStopInfo(String className, String[] args) {
        super();
        _className = className;
        _args = args;
    }
    
    void exec(String applicationName, Map<String, String> environment) {    
              
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
         * 1. java.vm32 registry key value
         * 2. java.vm registry key value
         * 3. "java"
         */
        String java = registryReader.getConfigurationElement(MocaRegistry.REGKEY_JAVA_VM32, true);
        if (java == null || java.isEmpty())
            java = registryReader.getConfigurationElement(MocaRegistry.REGKEY_JAVA_VM, true);
        if (java == null || java.isEmpty())
            java = "java";
        
        /*
         * The Java arguments we will use is determined using the following precedence:
         * 
         * 1. java.vmargs32.<application> registry key value
         * 2. java.vmargs.<application> registry key value
         * 3. java.vmargs32 registry key value
         * 4. java.vmargs
         * 5. Don't use any
         */      
        String vmargsApplicationKey = null;
        
        // java.vmargs32.<application>
        vmargsApplicationKey = MocaRegistry.REGKEY_JAVA_VMARGS32 + "." + applicationName;
        String vmargs = registryReader.getConfigurationElement(vmargsApplicationKey, true); 
        
        // java.vmargs.<application>
        if (vmargs == null || vmargs.isEmpty()) {
            vmargsApplicationKey = MocaRegistry.REGKEY_JAVA_VMARGS + "." + applicationName;
            vmargs = registryReader.getConfigurationElement(vmargsApplicationKey, true);
        }
        
        // java.vmargs32
        if (vmargs == null || vmargs.isEmpty()) {
            vmargs = registryReader.getConfigurationElement(MocaRegistry.REGKEY_JAVA_VMARGS32, true);
        }
        
        // java.vmargs
        if (vmargs == null || vmargs.isEmpty()) {
            vmargs = registryReader.getConfigurationElement(MocaRegistry.REGKEY_JAVA_VMARGS, true);
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
        StringBuilder command = new StringBuilder();
        command.append(java);
        command.append(" ");
        
        if (vmargs != null && !vmargs.isEmpty()) {
            command.append(vmargs);
            command.append(" ");   
        }
        
        command.append(_className);
        
        for (String arg : _args) {
            command.append(" ");
            command.append(arg);
        }
        
        // Start the actual service.
        try {
            System.out.println("Executing command: " + command);
            System.out.println(); 
            ServiceTools.executeCommand(command.toString(), environment, _debug ? System.out : null);
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
