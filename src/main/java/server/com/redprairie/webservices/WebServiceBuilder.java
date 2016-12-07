/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2006
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

package com.redprairie.webservices;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.redprairie.moca.util.Options;
import com.redprairie.moca.util.OptionsException;
import com.redprairie.webservices.config.ServiceConfigException;
import com.redprairie.webservices.config.ServiceConfiguration;
import com.redprairie.webservices.config.XFireServiceGenerator;
import com.redprairie.webservices.config.XMLServiceConfigurationReader;

/**
 * Constants used from XMLServiceConfigReader and XFireServiceGenerator
 * 
 * <b>
 * 
 * <pre>
 *    Copyright (c) 2005 RedPrairie Corporation
 *    All rights reserved.
 * </pre>
 * 
 * </b>
 * 
 * @author Mohanesha.C
 * @version $Revision$
 */
public class WebServiceBuilder {

    public static void main(String args[]) {
        String inputPath = null;
        String sourceArg = null;
        String configArg = null;
        
        // Parse Command-line options
        try {
            Options opt = Options.parse("p:s:c:", args);
            if (opt.isSet('p')) {
                inputPath = opt.getArgument('p');
            }
            
            if (opt.isSet('s')) {
                sourceArg = opt.getArgument('s');
            }

            if (opt.isSet('c')) {
                configArg = opt.getArgument('c');
            }
        }
        catch (OptionsException e) {
            System.err.println("Invalid Option");
            System.err.println("usage: WebServiceBuilder [-p service-path] [-c config-directory] [-s source-directory]");
            System.exit(1);
        }

        //
        // No input path: unable to process services
        //
        if (inputPath == null) {
            System.err.println("Warning: No Service Path Configured");
            System.exit(0);
        }
        

        File sourceDir;
        File configDir;
        
        // If they passed in a source target directory, use it.  Otherwise,
        // use LESDIR/temp/gensrc
        if (sourceArg == null) {
            String lesdir = System.getenv("LESDIR");
            if (lesdir == null) {
                System.err.println("Error: Can't find LESDIR");
                System.exit(1);
            }
            
            File tempDir = new File(new File(lesdir), "temp");
            sourceDir = new File(tempDir, "gensrc");
        }
        else {
            sourceDir = new File(sourceArg);
        }
        
        // If they passed in a configuration target directory, use it.
        // Otherwise, use LESDIR/temp/xfire
        if (configArg == null) {
            String lesdir = System.getenv("LESDIR");
            if (lesdir == null) {
                System.err.println("Error: Can't find LESDIR");
                System.exit(1);
            }

            File tempDir = new File(new File(lesdir), "temp");
            configDir = new File(tempDir, "xfire");
        }
        else {
            configDir = new File(configArg);
        }
        
        
        // Split the input path using platform-specific path separators.
        String[] dirNames = inputPath.split(File.pathSeparator);
        
        List<ServiceConfiguration> configList =
                new ArrayList<ServiceConfiguration>();
        
        for (String dirName : dirNames) {
            File dir = new File(dirName);
            if (!dir.exists()) {
                System.err.println("Warning: directory " + dirName +
                        " does not exist");
                continue;
            }
            else if (!dir.isDirectory()) {
                System.err.println("Warning: " + dirName + " is not a directory");
                continue;
            }
            File[] cfgFiles = dir.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".svc.xml");
                }
            });
            
            for (File configFile : cfgFiles) {
                try {
                    XMLServiceConfigurationReader reader =
                            new XMLServiceConfigurationReader(configFile);
                    ServiceConfiguration config = reader.process();
                    configList.add(config);
                }
                catch (FileNotFoundException e) {
                    System.err.println("Unexpected error: " + e);
                    e.printStackTrace();
                    System.err.println("Continuing...");
                }
                catch (ServiceConfigException e) {
                    System.err.println("File " + configFile +
                            ": error:" + e + "(" + e.getCause() + ")");
                    System.exit(1);
                }
            }
        }

        //
        // Generate the source and xfire configuration
        //
        XFireServiceGenerator generator =
                new XFireServiceGenerator(sourceDir, configDir);
        try {
            generator.generate(
                    configList.toArray(new ServiceConfiguration[configList.size()]));
        }
        catch (IOException e) {
            System.err.println("Error writing service implementation: " + e);
            e.printStackTrace();
            System.exit(1);
        }
   }
}
