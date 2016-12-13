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

package com.redprairie.moca.server.service;

import java.util.Map;

public class ServiceManagerWedge {

    /*
     * Start a service.
     * 
     * This is called by prunsrv via JNI when a service start request is made.
     * 
     * Everything written to stdout and stderr here is redirected by prunsrv to 
     * its stdout and stderr log files.
     * 
     * While screwy, it appears that we don't want to throw any exceptions from 
     * here.  Throwing an exception from here cause prunsrv to hang. The best we 
     * can do is write a message to stderr for prunsrv to redirect to its stderr 
     * log file and return.
     */
    static void startService(String args[]) {
        /* 
         * We annoyingly have to do these because common daemon writes a message
         * to our logfiles and doesn't throw a linefeed on the end of it. 
         */
        System.out.println();
        System.err.println();
        
        System.out.println("Attempting to start the service...");

        // We expect a single argument that represents the service name.
        if (args.length != 1) {
            System.err.println("Expected 1 argument - got " + args.length);
            return;
        }

        // Get the service and environment names.
        String serviceName = args[0];
        String environmentName = ServiceTools.getEnvironmentName(serviceName);
        
        Map<String, String> environment;
        try {
            environment = ServiceTools.getEnvironment(environmentName);
        }
        catch (ServiceManagerException e) {
            e.printStackTrace();
            return;
        }  
        
        // Get the service we'll be working with from a services.xml file and start it.
        Service service = null;
        try {
            service = ServiceReader.find(serviceName);
            System.out.println();
            System.out.println(service);
            System.out.println();
            service.start(environment);
        }
        catch (ServiceReaderException e) {
            e.printStackTrace();
            return;
        }
        catch (ServiceManagerException e) {
            e.printStackTrace();
            return;
        }   
     
        System.out.println("Service exited");
    }
         
    /*
     * Stop a service.
     * 
     * This is called by prunsrv via JNI when a service start request is made.
     *      
     * Everything written to stdout and stderr here is redirected by prunsrv to 
     * its stdout and stderr log files.
     * 
     * While screwy, it appears that we don't want to throw any exceptions from 
     * here.  Throwing an exception from here cause prunsrv to hang. The best we 
     * can do is write a message to stderr for prunsrv to redirect to its stderr 
     * log file and return.
     */
    static void stopService(String args[]) {
        /* 
         * We annoyingly have to do these because common daemon writes a message
         * to our logfiles and doesn't throw a linefeed on the end of it. 
         */
        System.out.println();
        System.err.println();
        
        System.out.println("Attempting to stop the service...");

        // We expect a single argument that represents the service name.
        if (args.length != 1) {
            System.err.println("Expected 1 argument - got " + args.length);
            return;
        }

        // Get the service and environment names.
        String serviceName = args[0];
        String environmentName = ServiceTools.getEnvironmentName(serviceName);  
        
        Map<String, String> environment;
        try {
            environment = ServiceTools.getEnvironment(environmentName);
        }
        catch (ServiceManagerException e) {
            e.printStackTrace();
            return;
        }  
        
        // Get the service we'll be working with from a services.xml file and start it.      
        Service service = null;
        try {
            service = ServiceReader.find(serviceName);
            System.out.println();
            System.out.println(service);
            System.out.println();
            service.stop(environment);
        }
        catch (ServiceReaderException e) {
            e.printStackTrace();
            return;
        }
        catch (ServiceManagerException e) {
            e.printStackTrace();
            return;
        }   
    }
}