/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import com.sam.moca.MocaException;
import com.sam.moca.applications.createctl.CreateControlFile;
import com.sam.moca.applications.msql.Msql;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.util.Options;
import com.sam.moca.util.OptionsException;

/**
 * This is the mainline method for the CreateControlFile Main line.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class CreateControlFileMain {

    /**
     * @param args
     * @throws SystemConfigurationException 
     * @throws OptionsException 
     */
    public static void main(String[] args) throws SystemConfigurationException {
        Options opts = null;
        try {
            opts = Options.parse("hd:luDa:U:P:w:", args);
        }
        catch (OptionsException e) {
            System.err.println("Invalid option: " + e.getMessage());
            showUsage();
            System.exit(1);
        }
        
        if (opts.isSet('h')) {
            showUsage();
            System.exit(0);
        }
        
        // If u is specified then we don't load, we unload
        boolean load = !opts.isSet('u');
        
        // If both u and l were specified, we can't do both.
        if (!load && opts.isSet('l')) {
            System.err.println("ERROR:Can not use -u and -l options at the same time!");
            showUsage();
            System.exit(1);
        }
        
        String[] tables = opts.getRemainingArgs();
        
        if (tables.length == 0) {
            System.err.println("ERROR:Missing list of tables!");
            showUsage();
            System.exit(1);
        }
        
        Msql msql = null;
        
        if (opts.isSet('a')) {
            System.err.println("WARNING:Control files will be created by server!");
            String url = opts.getArgument('a');
            String user = opts.getArgument('U');
            String pass = opts.getArgument('P');
            
            try {
                msql = new Msql(url, user, pass, false, true);
            }
            catch (MocaException e) {
                System.err.println("There was an issue connecting or logging into client");
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
        else {
            ServerUtils.setupDaemonContext(CreateControlFileMain.class.getName(), 
                    true, true);
            msql = new Msql(false, true);
        }
        
        msql.setCharset(Charset.forName("UTF-8"));
        
        CreateControlFile createControl = new CreateControlFile(msql);
        
        File outputDirectory = null;
        if (opts.isSet('d')) {
            outputDirectory = new File(opts.getArgument('d'));
        }
        
        
        boolean createDataFile = false;
        
        if (load) {
            if(opts.isSet('w')) {
               System.err.println("Warning:Ignoring -w option, only valid when unloading");
            }
            if(opts.isSet('D')) {
                System.err.println("Warning:Ignoring -D option, only valid when unloading");
            }
        }
        else {
            createDataFile = opts.isSet('D');
            
            if (opts.isSet('w')) {
                if (!createDataFile) {
                    System.err.println("Warning:Ignoring -w option, since did not use -D option!");
                }
            }
        }
        
        try {
            Map<String, MocaException> exceptions;
            if (load) {
                exceptions = createControl.loadControlFiles(outputDirectory, 
                        tables);
            }
            else {
                exceptions = createControl.unloadControlFiles(outputDirectory, 
                        opts.getArgument('w'), createDataFile, tables); 
            }
            
            for (Entry<String, MocaException> entry : exceptions.entrySet()) {
                System.err.print("Exception encountered for table: ");
                System.err.print(entry.getKey());
                System.err.print(" - ");
                System.err.println(entry.getValue().getMessage());
            }
            
            System.exit(exceptions.size());
        }
        catch (IOException e) {
            System.err.println("There was an issue with provided directory.");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
    
    private static void showUsage() {
        System.err.println("Create load control file:");
        System.err.println("Usage: create_ctl.pl [-l] [-h] [-d <dir>] [-a <url>] [-U <user>] [-P <password>] table...");
        System.err.println("Usage: create_ctl.pl [-l] [-h] [-d <dir>] table...");

        System.err.println("Create unload control file:");
        System.err.println("Usage: create_ctl.pl -u [-hD] [-d <dir>] [-a <url>] [-U <user>] [-P <password>] table...");
        System.err.println("Usage: create_ctl.pl -u [-hD] [-d <dir>] table...");
        System.err.println("--------------------------------------------------------------------");
        System.err.println("    -h                  - Print this message.");
        System.err.println("    -l                  - Load control file (default).");
        System.err.println("    -u                  - UnLoad control file.");
        System.err.println("    -d <dir>            - Output directory.");
        System.err.println("-------------------- Unload options --------------------------------");
        System.err.println("    -D                  - Create data sub directory & data file.");
        System.err.println("    -w <moca command>   - Uses the moca command (most likely a SQL)");
        System.err.println("                       to help define what primary key values are");
        System.err.println("                       placed in the data file.");
        System.err.println("                       (default) Assumes all rows are to be unloaded.");
        System.err.println("                       Only valid when -D is specified.");
        System.err.println("----- Remote options (executed on remote server) -------------------");
        System.err.println("    -a <url>            - URL to connect to.");
        System.err.println("    -U <user>           - Login user");
        System.err.println("    -P <password>       - Login password\n");
        System.err.println();
    }
}
 