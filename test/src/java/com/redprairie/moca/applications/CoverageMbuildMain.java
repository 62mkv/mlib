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

package com.redprairie.moca.applications;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;

import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.coverage.repository.CoberturaCommandRepository;
import com.redprairie.moca.coverage.repository.MocaProjectData;
import com.redprairie.moca.coverage.repository.XmlCoverageRepositoryFileReader;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.log.LoggingConfigurator;
import com.redprairie.moca.server.repository.CommandRepository;
import com.redprairie.moca.server.repository.ComponentLibraryFilter;
import com.redprairie.moca.server.repository.RegularExpressionComponentLibraryNameFilter;
import com.redprairie.moca.server.repository.file.Mbuild;
import com.redprairie.moca.server.repository.file.Mbuild.MbuildLogger;
import com.redprairie.moca.util.AppUtils;
import com.redprairie.moca.util.Options;
import com.redprairie.moca.util.OptionsException;

/**
 * This is the mainline for mbuild coverage.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class CoverageMbuildMain {

    /**
     * @param args
     * @throws SystemConfigurationException 
     */
    public static void main(String[] args) throws SystemConfigurationException {
        Options opts;
        try {
            opts = Options.parse("vD:hi:e:", args);
        }
        catch (OptionsException e1) {
            System.out.println("Usage: mbuildcoverage [-v] [-D depth] [-h] [-i <regex>] [-e <regex>]");
            return;
        }
        
        if (opts.isSet('h')) {
            System.out.println("Usage: mbuild [-v] [-D depth] [-h]");
            System.out.println("\t-v      Verbose mode.  Provides increased logging.");
            System.out.println("\t-Ddepth Enable pipe depth warning at depth.");
            System.out.println("\t-iregex Regular expression of which component levels to include.");
            System.out.println("\t-eregex Regular expression of which component levels to exclude.");
            System.out.println("\t-h      Help. Provides command-line usage information.");
            return;
        }
        
        boolean verbose = false;
        if (opts.isSet('v')) {
            verbose = true;
        }
        
        int warningDepth = 0;
        if (opts.isSet('D')) {
            warningDepth = Integer.parseInt(opts.getArgument('D'));
        }
        
        String includeRegex = null;
        if (opts.isSet('i')) {
            includeRegex = opts.getArgument('i');
        }
        
        String excludeRegex = null;
        if (opts.isSet('e')) {
            excludeRegex = opts.getArgument('e');
        }
        
        System.out.print(AppUtils.getStartBanner("Mbuild Coverage"));
        
        LoggingConfigurator.configure();
        long startTime = System.currentTimeMillis();
        SystemContext sys = ServerUtils.globalContext();
        String memoryFile = sys.getConfigurationElement(MocaRegistry.REGKEY_SERVER_MEMORY_FILE);
        
        MbuildLogger logger = new MbuildLogger(verbose);
        
        ComponentLibraryFilter includeFilter = null;
        if (includeRegex != null) {
            includeFilter = new RegularExpressionComponentLibraryNameFilter(includeRegex);
        }
        
        ComponentLibraryFilter excludeFilter = null;
        if (excludeRegex != null) {
            excludeFilter = new RegularExpressionComponentLibraryNameFilter(excludeRegex);
        }
        
        Mbuild mbuild = new Mbuild(sys);
        MocaProjectData data = new MocaProjectData();
        CommandRepository repository = mbuild.getRepository(logger, 
            new XmlCoverageRepositoryFileReader(data, includeFilter, 
                excludeFilter), warningDepth);
        
        // Now we persist the real CommandRepository with the project data
        CoberturaCommandRepository cobRepos = new CoberturaCommandRepository(repository, data);
        
        // Now write the command configuration to disk
        if (verbose) {
            System.out.println("Writing memory file: " + memoryFile);
        }

        // Actually write the configuration out via object serialization
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(memoryFile)));
            out.writeObject(cobRepos);
            out.flush();
        }
        catch (IOException e) {
            System.err.println(new Date() + " Error writing file: " + memoryFile + ": " + e);
            if (verbose) {
                e.printStackTrace();
            }
        }
        finally {
            if (out != null) try {
                out.close();
            }
            catch (IOException e) {
                System.err.println(new Date() + " Error closing file: " + memoryFile + ": " + e);
                if (verbose) {
                    e.printStackTrace();
                }
            }
        }
        
        // OK, wrap it up
        System.out.printf( "       Directories: %4d%n", logger.getDirCount());
        System.out.printf( "  Component Levels: %4d%n", logger.getLevelCount());
        System.out.printf( "          Commands: %4d%n", logger.getCommandCount());
        System.out.printf( "          Triggers: %4d%n", logger.getTriggerCount());
        System.out.printf( "   Errors Reported: %4d%n", logger.getErrorCount());
        
        long endTime = System.currentTimeMillis();
        System.out.println("Total Elapsed Time: " + (endTime - startTime) + "ms");
    }
}
