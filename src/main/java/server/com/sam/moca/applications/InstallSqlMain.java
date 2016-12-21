/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
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

package com.sam.moca.applications;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.springframework.aop.framework.ProxyFactory;

import com.sam.moca.MocaException;
import com.sam.moca.MocaRegistry;
import com.sam.moca.applications.installsql.InstallSql;
import com.sam.moca.applications.preprocessor.MocaCPreProcessor;
import com.sam.moca.applications.preprocessor.MocaPreProcessor;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.server.exec.SystemContext;
import com.sam.moca.util.AppUtils;
import com.sam.moca.util.InterruptedIOExceptionAdvice;
import com.sam.moca.util.Options;
import com.sam.moca.util.OptionsException;
import com.sam.moca.util.SeparateTargetFactory;

/**
 * This is the mainline for the InstallSql executable.  This is used to parse
 * sql, tbl, idx files to execute database or moca commands
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class InstallSqlMain {
    
    private static void showUsage() {
        System.err.println("Usage: installsql [-d] [file list]");
    }

    /**
     * This is the mainline for the installsql application
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        long begin = System.nanoTime();
        
        //Try to get the global context which reads the settings
        SystemContext context = ServerUtils.globalContext();
        
        // Override the database username/password if a DBA username/password are set in the registry.
        String dbaUsername = context.getConfigurationElement(MocaRegistry.REGKEY_DB_DBA_USERNAME);
        String dbaPassword = context.getConfigurationElement(MocaRegistry.REGKEY_DB_DBA_PASSWORD);
        
        if (dbaUsername != null && dbaPassword != null) {
            context.overrideConfigurationElement(MocaRegistry.REGKEY_DB_USERNAME, dbaUsername); 
            context.overrideConfigurationElement(MocaRegistry.REGKEY_DB_PASSWORD, dbaPassword); 
        }
        
        Options opts = null;
        try {
            opts = Options.parse("d", args);
        }
        catch (OptionsException e) {
            System.err.println("Invalid option: " + e.getMessage());
            showUsage();
            System.exit(1);
        }
        
        // Run in debug if the -d was provided
        boolean debug = opts.isSet('d');
        
        String[] filesToProcess = opts.getRemainingArgs();
        
        try {
            ServerUtils.setupDaemonContext(InstallSqlMain.class.getName(), true, true);
        }
        catch (SystemConfigurationException e) {
            System.err.println("Error setting up moca context " + 
                    e.getMessage());
            System.exit(1);
        }
        
        System.out.print(AppUtils.getStartBanner("InstallSql"));
        
        MocaPreProcessor preprocessor = new MocaCPreProcessor(".");
        
        ProxyFactory factory = new ProxyFactory(new Class[]{Appendable.class});
        
        factory.addAdvice(new InterruptedIOExceptionAdvice());
        factory.setAopProxyFactory(new SeparateTargetFactory());

        // We proxy out the standard out since we don't want to worry about
        // checking every single IOException.
        // Proxy out the normal output
        factory.setTarget(System.out);
        
        Appendable normalOutput = (Appendable)factory.getProxy();

        InstallSql installSQL = new InstallSql(normalOutput);
        
        List<String> actualFilesToProcess = new LinkedList<String>();
        
        // Here we want to exclude directories.
        for (String fileToProcess : filesToProcess) {
            // All files must be relative to current directory or absolute paths
            File file = new File(fileToProcess);
            
            if (!file.isDirectory()) {
                actualFilesToProcess.add(fileToProcess);
            }
            else {
                LogManager.getLogger(InstallSqlMain.class).debug("Ignoring " +
                		"directory [" + file + "]");
            }
        }
        
        try {
            List<MocaException> exceptions = installSQL.processFiles(
                    actualFilesToProcess, preprocessor, debug);

            // Now we exit giving the # of exceptions we had
            System.exit(exceptions != null ? exceptions.size() : 0);
        }
        catch (IOException e) {
            // This technically could be a problem with writing to the appender
            // but System.out shouldn't fail, so we assume it is the preprocessor
            System.err.println("There was an error while running " +
            		"preprocessor " + e.getMessage());
            System.exit(1);
        }
        catch (MocaException e) {
            System.err.println("Could not obtain database type " + 
                    e.getMessage());
            System.exit(1);
        }
        finally {
            LogManager.getLogger(LogManager.ROOT_LOGGER_NAME).debug(
                    "Processing took " + ((System.nanoTime() - begin) / 1.0e9)
                            + " seconds");
        }
    }
}
