/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.applications;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.springframework.aop.framework.ProxyFactory;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.applications.dbupgrade.Dbupgrade;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.util.AppUtils;
import com.redprairie.moca.util.InterruptedIOExceptionAdvice;
import com.redprairie.moca.util.MocaIOException;
import com.redprairie.moca.util.Options;
import com.redprairie.moca.util.OptionsException;
import com.redprairie.moca.util.SeparateTargetFactory;

/**
 * This is the mainline for the dbupgrade executable.  This executable is used
 * to process upgrade directories
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class DbupgradeMain {
    
    private static void showUsage() {
        System.err.println(
                "Usage: dbupgrade [-h] [-w] \n" +
                "\t               There must be only 1 of the following provided\n" +
                "\t               [-d <directory>] [-f <filename>] [-v <version>]\n" +
                "\t-w             Show warning messages\n" +
                "\t-d <directory> Process <directory> only\n" +
                "\t-f <filename>  Process <filename> only\n" +
                "\t-v <version>   Start upgrading at <version>\n" +
                "\t-h             Show this message\n");
    }

    /**
     * The mainline for dbupgrade.
     * @param args The arguments passed in the command line
     */
    public static void main(String[] args) {
        
        System.setProperty("com.redprairie.moca.config","D:\\MFC\\mlib\\src\\resource\\82.registry");
        System.setProperty("LESDIR","D:\\MFC\\mlib");
        args = new String[2];
        args[0] = "-w";
        args[1] ="-fsrc\\resource\\sample_table.sql";
        long begin = System.nanoTime();
        
        //Try to get the global context which reads the settings
        SystemContext context = ServerUtils.globalContext();
        
        //Disable the query limit since we're doing an upgrade.
        context.overrideConfigurationElement(MocaRegistry.REGKEY_SERVER_QUERY_LIMIT, "0");
        
        // Override the database username/password if a DBA username/password are set in the registry.
        String dbaUsername = context.getConfigurationElement(MocaRegistry.REGKEY_DB_DBA_USERNAME);
        String dbaPassword = context.getConfigurationElement(MocaRegistry.REGKEY_DB_DBA_PASSWORD);
        
        if (dbaUsername != null && dbaPassword != null) {
            context.overrideConfigurationElement(MocaRegistry.REGKEY_DB_USERNAME, dbaUsername); 
            context.overrideConfigurationElement(MocaRegistry.REGKEY_DB_PASSWORD, dbaPassword); 
        }
        
        Options opts = null;
        try {
            opts = Options.parse("hwd:f:v:", args);
        }
        catch (OptionsException e) {
            System.err.println("Invalid option: " + e.getMessage());
            showUsage();
            System.exit(1);
        }
        
        String directory = null;
        String fileName = null;
        String version = null;
        
        int argumentsProvided = 0;
        
        if (opts.isSet('d')) {
            directory = opts.getArgument('d');
            argumentsProvided++;
        }
        
        if (opts.isSet('f')) {
            fileName = opts.getArgument('f');
            argumentsProvided++;
        }
        
        if (opts.isSet('v')) {
            version = opts.getArgument('v');
            argumentsProvided++;
        }
        
        // If the help was asked for or we didn't get a file directory
        if (opts.isSet('h') || argumentsProvided != 1) {
            showUsage();
            System.exit(0);
        }
        
        try {
            ServerUtils.setupDaemonContext(DbupgradeMain.class.getName(), true, true);
        }
        catch (SystemConfigurationException e) {
            System.err.println("Error setting up moca context " + 
                    e.getMessage());
            System.exit(1);
        }
        
        boolean showWarnings = opts.isSet('w');
        
        
        System.out.print(AppUtils.getStartBanner("Database Upgrade Tool"));
        
        ProxyFactory factory = new ProxyFactory(new Class[]{Appendable.class});
        
        factory.addAdvice(new InterruptedIOExceptionAdvice());
        factory.setAopProxyFactory(new SeparateTargetFactory());

        // We proxy out the appendables since we don't want to worry about
        // checking every single IOException.
        // Proxy out the normal output
        factory.setTarget(System.out);
        
        Appendable normalOutput = (Appendable)factory.getProxy();

        // Then Proxy out the error output
        factory.setTarget(System.err);
        
        Appendable errorOutput = (Appendable)factory.getProxy();
        
        Dbupgrade dbupgrade = new Dbupgrade(normalOutput, errorOutput, 
                showWarnings);
        
        List<MocaException> exceptions = new ArrayList<MocaException>(0);
        
        try {
            if (version != null) {
                exceptions = dbupgrade.processVersion(version);
            }
            else if (directory != null) {
                exceptions = dbupgrade.processDirectory(directory);
            }
            // This means we only provided the file name
            else {
                exceptions = dbupgrade.processFileName(fileName);
            }
        }
        catch (FileNotFoundException e) {
            // We couldn't find the file for some reason
            System.err.println("The specified file cannot be found : " + 
                    e.getMessage());
            exceptions.add(new MocaIOException("File not found", e));
        }
        catch (IOException e) {
            // There was a problem writing to the output
            System.err.println("There was a problem encountered while writing " +
            		"to a stream script : " + e.getMessage());
            exceptions.add(new MocaIOException("IO Exception", e));
        }
        finally {
            LogManager.getLogger(LogManager.ROOT_LOGGER_NAME).debug(
                    "Processing took " + ((System.nanoTime() - begin) / 1.0e9)
                            + " seconds");
        }
        
        System.exit(exceptions.size());
    }
    
}
