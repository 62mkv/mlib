/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.applications.wsdeploy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.util.IOUtils;
import com.redprairie.moca.util.MocaUtils;

/**
 * This class is designed to do the heavy work of wsdeploy.
 * 
 * Copyright (c) 2013 Sam Corporation All Rights Reserved
 * 
 * @author j1014843
 */
public class WebServiceBuilder {

    private static final String WAR_EXTENSION = ".war";
    private static final String WEBDEPLOY_DIR = "webdeploy";
    private static final String WS_DIR = "ws";
    private static final String TEMP_DIR = "temp";

    private final boolean _isWindows;
    private final String _prodDirs;
    private final SystemContext _ctx;
    private PrintWriter _writer;

    public WebServiceBuilder(String prodDirs) {
        _prodDirs = prodDirs;
        _ctx = ServerUtils.globalContext();
        // determine operating system for platform-specific behavior
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            _isWindows = false;
        }
        else {
            _isWindows = true;
        }

    }

    /**
     * For each prod-dir, locate ALL war files for this prod-dir. For each war
     * in a prod-dir (EXCLUDING %LESDIR%) : if directory structure matches
     * war-name then call Ant tasks to compile and deploy web-services code.
     * 
     * @param args
     */
    public int build() {
        int returnCode = 0;
        try {

            // get environment variables for prod-dirs and $LESDIR
            String lesDir = MocaUtils.expandEnvironmentVariables(_ctx,
                "$LESDIR");

            // set log file naming format
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
            String suffix = df.format(new java.util.Date());
            File logFileName = getFile(lesDir, "log", "wsdeploy-" + suffix
                    + ".log");

            // create and open log file, exit upon failure
            try {
                _writer = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(logFileName, false), Charset.defaultCharset()));
                _writer.println("***** WebServiceBuilder Log File *****");
                _writer.println();
            }
            catch (IOException e) {
                System.out.println("UNABLE TO CREATE LOG FILE!  ABORTING!" + e);
                e.printStackTrace();
                return 1;
            }

            returnCode += buildHelper(lesDir);
            log("");
            log("WebService " + "code successfully compiled and deployed to "
                    + getFile(lesDir, WEBDEPLOY_DIR).getAbsolutePath());
        }
        catch (Exception e) {
            if (_writer != null) {
                log(e);
            }
        }
        finally {
            if (_writer != null) {
                _writer.close();
            }
        }

        return returnCode;
    }

    private int buildHelper(String lesDir) throws SystemConfigurationException,
            FileNotFoundException {
        String warName = "";

        // OVERRIDE: If args[] isn't empty, use it as "prod-dirs"
        StringBuilder prodDirs = new StringBuilder();
        if (_prodDirs != null && !_prodDirs.isEmpty()) {
            log("Using parameter args[] as prod-dirs.");

            // First argument is prod-dirs
            for (String prodDir : _prodDirs.split(File.pathSeparator)) {
                prodDirs.append(File.pathSeparator).append(prodDir);
            }
        }
        else {
            // PROGRAMMATIC discovery of "prod-dirs" using system context
            prodDirs.append(_ctx
                .getConfigurationElement(MocaRegistry.REGKEY_SERVER_PROD_DIRS));
        }

        if (prodDirs.length() == 0) {
            log("Failed to load prod-dirs.  Exiting.");
            return 1;
        }

        // create the %LESDIR/webdeploy folder, AND the %LESDIR/temp folder
        for (File f : new File[] { getFile(lesDir, WEBDEPLOY_DIR),
                getFile(lesDir, TEMP_DIR) }) {
            if(!f.mkdir()) {
                log("Could not create directory: " + f.getAbsolutePath());
            }
        }

        // pre-pend %LESDIR% to the custom source folder
        String customSrcFolder = getFile(lesDir, WS_DIR).getAbsolutePath();
        // Process the prod-dirs in reverse so that products higher in the
        // product stack
        // get processed after the lower layers. Without this, overriding a
        // full service
        // using the same war filename causes the higher product's war to be
        // replaced
        // with the lower product's war, such that nothing is overriden.
        List<String> prodDirList = Arrays.asList(prodDirs.toString()
            .split(File.pathSeparator));
        Collections.reverse(prodDirList);
        // for each entry in %PROD-DIR%
        for (String prodDir : prodDirList) {
            // do not use war files from $LESDIR\webdeploy

            if (prodDir.equalsIgnoreCase(lesDir)) {
                continue;
            }

            List<File> files = getWarFilesForDirectory(prodDir);

            // for each war file in this prod-dir
            for (File f : files) {
                // extract war name, try to find matching directory
                warName = f.getName().replace(WAR_EXTENSION, "");
                log("--------------------------------------");
                log("Processing WAR: " + warName);

                try {
                    String customizationPath = getFile(customSrcFolder, warName).getAbsolutePath();
                    log("Checking for customizations at " + customizationPath);

                    File directory = getFile(customizationPath);

                    if (directory.isDirectory()) {
                        // Run Ant task to copy war files,
                        // compile matching custom code, and re-package war
                        log("Customizations found");

                        String command = "cd " + lesDir
                                + " && ant ws-build -Dtarget=" + warName
                                + " -Dwar-dir="
                                + f.getPath().replace(f.getName(), "");
                        log("Executing Ant " + "task: " + command);
                        log("");
                        execAntTask(command);
                    }
                    else {
                        log("Customizations NOT found");
                        // LES does not have a WS directory for this war
                        // so there are no customizations. Simply deploy
                        // the war file as is.

                        File copyDestinationPath = getFile(lesDir,
                            WEBDEPLOY_DIR, f.getName());
                        log("Deploying file " + f.getAbsolutePath() + " to "
                                + copyDestinationPath.getAbsolutePath());
                        copyFile(f, copyDestinationPath);
                    }
                }
                catch (IOException | InterruptedException e) {
                    log("EXCEPTION processing WAR: " + warName);
                    log(e);
                }
                finally {
                    log("Done processing WAR file");
                }
            } // end for each war in this prod-dir
        } // end for each prod-dir
        return 0;
    }

    /**
     * 
     * Execute an Ant task via command-line
     * 
     * @param cmd
     * @throws IOException
     * @throws InterruptedException
     */
    private void execAntTask(String cmd) throws IOException,
            InterruptedException {
        String line;

        Process p = null;
        // create platform-specific executor process
        if (_isWindows) {
            p = Runtime.getRuntime().exec("cmd /c " + cmd);
        }
        else {
            p = Runtime.getRuntime().exec(
                new String[] { System.getenv("SHELL"), "-c", cmd });
        }

        try (BufferedReader bri = new BufferedReader(new InputStreamReader(
            p.getInputStream(), Charset.defaultCharset()));
                BufferedReader bre = new BufferedReader(new InputStreamReader(
                    p.getErrorStream(), Charset.defaultCharset()))) {

            while ((line = bri.readLine()) != null) {
                log(line);
            }
            while ((line = bre.readLine()) != null) {
                log(line);
            }
            p.waitFor();
        }
    }

    /**
     * 
     * Get all *.war files for a directory. Do not include sub-directories.
     * 
     * @param dir
     * @return
     * @throws FileNotFoundException
     */
    private List<File> getWarFilesForDirectory(String dir)
            throws FileNotFoundException {
        List<File> result = new ArrayList<File>();
        File directory = getFile(dir, WEBDEPLOY_DIR);

        if (!directory.exists()) {
            log("Directory " + directory.getAbsolutePath() + " does not exist.");
            return result;
        }

        // get a list of all files containing ".war"
        File[] fileArray = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.contains(WAR_EXTENSION)) {
                    return true;
                }
                return false;
            }
        });

        if (fileArray == null) {
            log("Could not find wars in directory: " + directory);
            return result;
        }

        List<File> files = Arrays.asList(fileArray);
        // First add the files in this directory.
        for (File file : files) {
            result.add(file);
        }
        return result;
    }

    //Given a array of strings, generate a path. This will
    //create a path in a OS agnostic way.
    private File getFile(String... args) {
        if (args == null || args.length == 0) {
            return null;
        }

        File file = null;
        if (args.length > 1) {
            file = new File(args[0]);
            for (int i = 1; i < args.length; i++) {
                file = new File(file, args[i]);
            }
        }
        else {
            file = new File(args[0]);
        }

        return file;
    }

    /**
     * Helper method for creating logging output messages.
     * 
     * @param message
     */
    private void log(Object message) {
        if (message != null) {
            // set log file naming format
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSSS");
            String date = df.format(new java.util.Date());
            System.out.println(date + " - " + message);
            if (_writer != null) {
                _writer.println(date + " - " + message);
            }
        }
    }

    /**
     * Copies the source file to the destination file.
     * 
     * @param source
     * @param destination
     * @throws IOException
     */
    private void copyFile(File source, File destination) throws IOException {

        if (!destination.exists()) {
            if(destination.createNewFile()) {
                log("Could not create file: " + destination);
            }
        }

        FileInputStream sourceStream = null;
        FileOutputStream destinationStream = null;
        try {
            log("Copying " + source + " to " + destination.getAbsolutePath());
            sourceStream = new FileInputStream(source);
            destinationStream = new FileOutputStream(destination);
            IOUtils.copy(sourceStream, destinationStream);
        }
        finally {
            if (sourceStream != null) {
                sourceStream.close();
            }
            if (destinationStream != null) {
                destinationStream.close();
            }
        }
    }
}
