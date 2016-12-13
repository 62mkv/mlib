/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.mad.reporters;

import java.rmi.RemoteException;
import java.util.Formatter;

import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.legacy.NativeProcess;
import com.redprairie.moca.server.legacy.NativeProcessPool;

/**
 * This is a simple Class that returns a formatted report with information about
 * the Native Process Poll and also information regarding each individual Native
 * Process within the pool.
 * 
 * Copyright (c) 2012 Sam Corporation All Rights Reserved
 * 
 * @author eknapp
 */
public class NativeProcessReporter {

    public static String generateNativeProcessReport() {
        // Initialize the Report formatter.
        StringBuilder builder = new StringBuilder();
        Formatter formatter = new Formatter(builder);

        // Access the Native Process Pool.
        ServerContextFactory factory = ServerUtils
            .globalAttribute(ServerContextFactory.class);
        NativeProcessPool pool = factory.getNativePool();

        // Format the Native Process Summary Information.
        String format = "%1$-25s%2$-10s%n";
        formatter.format(format, "Native-Processes:", pool.getSize());
        formatter.format(format, "Peak-Processes:", pool.getPeakSize());
        formatter.format(format, "Maximum-Processes", pool.getMaximumSize());
        builder.append("\n");

        // Format the Individual Native Process Information.
        format = "%1$-25s%2$-70s%n";
        for (NativeProcess process : pool.getAllProcesses()) {
            try {
                formatter.format(format, "Process-ID:", process.getId());
                formatter.format(format, "Date-Created:", process.dateCreated());
                formatter.format(format, "Thread:", process.getAssociatedThread());
                formatter.format(format, "Last-Request:", process.lastCall());
                formatter.format(format, "Last-Request-Date:", process.lastCallDate());
                formatter.format(format, "Alive:", process.isAlive());
                formatter.format(format, "Keep-Alive:", process.isKeepaliveSet());
                formatter.format(format, "Requests:", pool.timesTaken(process));
                builder.append("\n");
            }
            catch (RemoteException re) {
                formatter.close();
                throw new RuntimeException(re);
            }
        }

        String report = builder.toString();
        formatter.close();
        return report;
    }

}
