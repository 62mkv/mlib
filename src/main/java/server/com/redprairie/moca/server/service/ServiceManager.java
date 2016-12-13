/*
 *  $URL: https://athena.redprairie.com/svn/prod/moca/branches/2011.2-dev/src/java/server/com/redprairie/moca/server/service/ServiceManager.java $
 *  $Author: mlange $
 *  $Date: 2011-11-08 09:25:31 -0600 (Tue, 08 Nov 2011) $
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

package com.redprairie.moca.server.service;

import java.io.File;
import java.util.Map;

import com.redprairie.moca.util.Options;
import com.redprairie.moca.util.OptionsException;

public class ServiceManager {
 
    /*
     * Request to start a service.
     * 
     * This will result in prunsrv making a JNI call to 
     * ServiceManagerWedge.startService(serviceName);
     */
    private static void start(String serviceName) {       
        System.out.println("Attempting to start the service...");   
        System.out.println();
         
        try {
            String command = "net start " + serviceName;
            ServiceTools.executeCommand(command);
        } 
        catch (ServiceManagerException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /*
     * Request to stop a service.
     *   
     * This will result in prunsrv making a JNI call to 
     * ServiceManagerWedge.stopService(serviceName);
     */
    private static void stop(String serviceName) {
        System.out.println("Attempting to stop the service...");
        
        try {
            String command = "net stop " + serviceName;
            ServiceTools.executeCommand(command);
        } 
        catch (ServiceManagerException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }  
    
    /*
     * Install a service.
     */  
    private static void install(String serviceName, String environmentName, boolean autostart, String username, String password, int stopTimeout) {       
        System.out.println("Installing service...");
        
        // Get the service we'll be working with from a services.xml file.
        Service service = null;
        try {
            service = ServiceReader.find(serviceName);
            System.out.println();
            System.out.println(service);
            System.out.println();
        }
        catch (ServiceReaderException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }   
        
        // Install the service.
        try {
            Map<String, String> environment = ServiceTools.getEnvironment(environmentName);  
            String lesdir = environment.get("LESDIR");
            String mocadir = environment.get("MOCADIR");
            String classpath = environment.get("CLASSPATH");
            
            StringBuilder command = new StringBuilder();
            command.append(mocadir + "\\bin\\prunsrv.exe //IS//" + service.getName());
            command.append(" --DisplayName=\"" + service.getDisplayName() + "\"");
            command.append(" --Description=\"" + service.getDescription() + "\"");
            command.append(" --Classpath=\"" + classpath + "\"");
            command.append(" --StartMode=jvm --StartClass=com.redprairie.moca.server.service.ServiceManagerWedge");
            command.append(" --StartMethod=startService ++StartParams=" + service.getName() );
            command.append(" --StopMode=jvm  --StopClass=com.redprairie.moca.server.service.ServiceManagerWedge");
            command.append(" --StopMethod=stopService ++StopParams=" + service.getName());
            command.append(" --LogPath=" + lesdir + File.separator + "log");
            command.append(" --LogPrefix=" + service.getName());
            command.append(" --StdOutput=auto --StdError=auto");         

            // We only add the startup mode if they specified auto start.
            if (autostart) {
                command.append(" --Startup auto");
            }  
            
            // We only add the username and password if they are provided.
            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                command.append(" --ServiceUser " + username + " --ServicePassword " + password);
            }  
            
            // We only add the stop timeout if they are provided.
            if (stopTimeout > 0) {
                command.append(" --StopTimeout= " + stopTimeout);
            }
            
            // We need to add each environment variable as a separate argument.
            for (Map.Entry<String, String> entry : environment.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue().replaceAll(";", "';'");
                command.append(" ++Environment=\"" + name + "=" + value + "\"");
            }
            
            // We need to add each depends on item as a separate argument.
            for (String dependsOn : service.getDependsOn()) {
                command.append(" ++DependsOn=\"" + dependsOn + "\"");
            }
            
            ServiceTools.executeCommand(command.toString());
        } 
        catch (ServiceManagerException e) {
            System.err.println("Could not install service.  Does the service already exist?");
            System.exit(e.getErrorCode());
        }
        
        System.out.println("Done");
    }
    
    /*
     * Uninstall a service.
     */  
    private static void uninstall(String serviceName, String environmentName) {
        System.out.println("Uninstalling service...");
    
        try {
            Map<String, String> environment = ServiceTools.getEnvironment(environmentName);  
            String mocadir = environment.get("MOCADIR");
            
            String command = mocadir + "\\bin\\prunsrv.exe //DS//" + serviceName;
            ServiceTools.executeCommand(command);
        } 
        catch (ServiceManagerException e) {
            System.exit(e.getErrorCode());
        }
        
        System.out.println("Done");
    }
    
    /*
     * Print usage for the service manager.  
     * 
     * The following are valid command lines:
     *     
     *     servicemgr [-a moca] [-e dev] [-s auto|manual] [-u foo -p bar] [-t 120] install
     *     servicemgr [-a moca] [-e dev]                                         uninstall
     *     servicemgr [-a moca] [-e dev]                                         start
     *     servicemgr [-a moca] [-e dev]                                         stop
     *     servicemgr           [-e dev]                                         dump
     *     
     * If "-a <application name>" is not provided "moca" is used.       
     * If "-e <environment name>" is not provided %MOCA_ENVNAME% is used.
     * If "-s <mode>" is not provided "manual" is used.
     */  
    private static void printUsage() {
        System.out.println("Usage: servicemgr [-a <application name>]\n"
                +          "                  [-e <environment name>]\n" 
                +          "                  [-s auto|manual]\n"
                +          "                  [-u <username> -p <password>]\n" 
                +          "                  [-t <timeout(seconds)>]\n"
                +          "                  <action>\n"
                + "\twhere \"action\" must be one of: \n"
                + "\t       install   - to install as a service\n"
                + "\t       uninstall - to uninstall a service\n"
                + "\t       start     - to start a service\n"
                + "\t       stop      - to stop a service\n"
                + "\t       dump      - to dump a batch file of all environment settings\n");
    }
    
    public static void main(String[] args) throws ServiceManagerException {  
        
        // Parse command line arguments.
        Options opts = null;
        try {
            opts = Options.parse("a:e:s:u:p:t:h?", args);
        }
        catch (OptionsException e) {
            System.err.println("Invalid option: " + e.getMessage());
            printUsage();
            System.exit(1);
        }
        
        // Get the required application name.
        String applicationName = null;
        if (opts.isSet('a')) {
            applicationName = opts.getArgument('a');
        }
        else {
            applicationName = "moca";
        }
 
        // Get the required environment name, which we'll take from the environment if necessary.
        String environmentName = null;
        if (opts.isSet('e')) {
            environmentName = opts.getArgument('e');
        }
        else {
            environmentName = System.getenv("MOCA_ENVNAME");
        }
        
        if (environmentName == null) {
            System.err.println("Missing environment name and MOCA_ENVNAME is not set.\n");
            printUsage();
            System.exit(1);    
        }
        
        // Build our service name from our application and environment names.
        String serviceName = ServiceTools.getServiceName(applicationName, environmentName); 
        
        // Get start mode.
        boolean autostart = false;
        if (opts.isSet('s')) {
            String startMode = opts.getArgument('s');
            if (startMode.equalsIgnoreCase("auto")) {
                autostart = true;
            } else if (startMode.equalsIgnoreCase("manual")) {
                autostart = false;
            }
            else {
                System.err.println("Start mode must be \"auto\" or \"manual\".\n");
                printUsage();
                System.exit(1);
            }
        }
        
        // Get the required action.
        String[] remainingArgs = opts.getRemainingArgs();       
        if (remainingArgs.length != 1) {
            printUsage();
            System.exit(1);            
        }
        
        String action = remainingArgs[0];       
        
        // Get the username and password if we're installing a service.
        String username = null;
        String password = null;
        int timeout = 0;
        if (action.equalsIgnoreCase("install")) {
            if (opts.isSet('u')) {
                username = opts.getArgument('u');
            }
            if (opts.isSet('p')) {
                password = opts.getArgument('p');
            }
            if (opts.isSet('t')) {
                timeout = Integer.valueOf(opts.getArgument('t'));
            }
        }
        
        // Deal with the given action.
        if (action.equalsIgnoreCase("install")) {
            install(serviceName, environmentName, autostart, username, password, timeout);
        }
        else if (action.equalsIgnoreCase("uninstall")) {
            uninstall(serviceName, environmentName);
        }
        else if (action.equalsIgnoreCase("start")) {
            start(serviceName);
        }
        else if (action.equalsIgnoreCase("stop")) {
            stop(serviceName);
        }
        else {
            printUsage();
            System.exit(1);
        }
    }
}
