/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2014
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

package com.redprairie.moca.cluster.manager.simulator;

import java.io.IOException;
import java.io.InputStream;

import javax.management.MalformedObjectNameException;

import com.redprairie.moca.server.MocaServerMain;
import com.redprairie.moca.server.MocaServiceFunctions;

/**
 * 
 * Wrapper for {@link MocaServerMain} that will
 * detect when the parent process has closed and
 * subsequent will exit itself as well. This is to
 * fix an issue where the shutdown hook in the parent
 * process may not run to kill off this child process e.g.
 * if the parent process is killed with "kill -9" the Java
 * shutdown hook will not run.
 * 
 * Copyright (c) 2014 RedPrairie Corporation
 * All Rights Reserved
 */
public class TestMocaServerMain {
    
    public static void main(String args[]) throws Exception {
        startCrashListener();
        MocaServerMain.main(args);
    }
    
    // Starts up a thread that blocks on reading the input stream
    // of the processes standard in so that if the parent process ever
    // goes down this would close and subsequently we would exit this process
    private static void startCrashListener() {
        final InputStream istream = System.in;
        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    // If anything is sent back then we just
                    // exit
                    istream.read();
                }
                catch (IOException ignore) {}
                
                // Parent process closed so lets close this child process.
                // First try a standard System.exit(0), however, this might
                // not work if the MocaSecurityManager is setup so then
                // we shutdown via MocaServiceFunctions
                try {
                    System.exit(0);
                }
                catch (SecurityException ex) {
                    try {
                        MocaServiceFunctions.main(new String[] { "stop" });
                    }
                    catch (MalformedObjectNameException e) {
                        e.printStackTrace();
                    }
                    catch (IOException e) {
                    	e.printStackTrace();
                    }
                }
                
            }

        };
        
        thread.setName("ParentProcessCrashWatcher");
        thread.setDaemon(true);
        thread.start();
    }

}
