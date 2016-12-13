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

package com.redprairie.moca.applications;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.repository.CommandRepository;
import com.redprairie.moca.server.repository.docs.CommandDocumentationException;
import com.redprairie.moca.server.repository.file.Mbuild;
import com.redprairie.moca.util.MocaIOException;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.moca.util.Options;
import com.redprairie.moca.util.OptionsException;
import com.redprairie.util.SuffixFilenameFilter;

/**
 * This class is the implementation of the documentation generation program for
 * use with MOCA based products.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MgendocMain {
    public static void main(String[] args) {
        Options opts = null;
        try {
            opts = Options.parse("hd:i:t:D", args);
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
        
        ServerContext context = null;
        try {
            context = ServerUtils.setupDaemonContext(MgendocMain.class.getName(), 
                    true, true);
        }
        catch (SystemConfigurationException e) {
            System.err.println("Error setting up MOCA context.");
            e.printStackTrace(System.err);
            System.exit(1);
        }
        
        MocaContext moca = context.getComponentContext();
        File baseDirectory;
        if (opts.isSet('d')) {
            String unexpandedName = opts.getArgument('d').replace('\\', '/');
            String expandedDirectory = MocaUtils.expandEnvironmentVariables(
                    moca, unexpandedName);
            baseDirectory = new File(expandedDirectory);
        }
        else {
            baseDirectory = new File(MocaUtils.expandEnvironmentVariables(
                    moca, "$LESDIR/docs"));
        }
        
        // If the docs directory isn't there we have to make it
        if (!baseDirectory.exists()) {
            if (!baseDirectory.mkdir()) {
                System.err.print(
                    "There was a problem creating directory: ");
                System.err.println(baseDirectory);
                System.exit(1);
            }
        }
        else if (!baseDirectory.isDirectory()) {
            System.err.print(
                "Provided file is present but isn't a directory: ");
            System.err.println(baseDirectory);
            System.exit(1);
        }
        
        String type;
        if (opts.isSet('t')) {
            type = opts.getArgument('t');
        }
        else {
            type = "all";
        }
        
        String integratorTransType;
        if (opts.isSet('i')) {
            integratorTransType = opts.getArgument('i');
        }
        else {
            integratorTransType = "enabled";
        }
        
        List<String> subDirectoryList = new ArrayList<String>(Arrays.asList(
                "commands", "css", "database", "gif", "integration", "lib", 
                "script", "wm"));
        
        List<String> fileList = new ArrayList<String>(Arrays.asList(
                "$MOCADIR/docs/css/redprairie.css,css",
                "$MOCADIR/docs/gif/arrow.gif,gif",
                "$MOCADIR/docs/gif/redprairie.gif,gif",
                "$MOCADIR/docs/html/wm.html,wm/index.html",
                "$MOCADIR/docs/html/database.html,database/index.html",
                "$MOCADIR/docs/script/fixdesc.js,script"));
        
        boolean loadAll = type.contains("all"); 
        
        if (loadAll || type.contains("moca")) {
            subDirectoryList.add("moca");
            fileList.add("$MOCADIR/docs/MOCA-CommandGrammar.vsd,moca");
        }
        
        if (loadAll || type.contains("integrator") || type.contains("integration")) {
            subDirectoryList.add("integration");
        }
        
        if (loadAll || type.contains("wm")) {
            subDirectoryList.add("wm");
        }
        
        if (loadAll || type.contains("database")) {
            subDirectoryList.add("database");
        }
        
        System.out.println("\nGenerating documentation...\n");
        
        File mocaDocsFile = new File(MocaUtils.expandEnvironmentVariables(moca, 
                "$MOCADIR/docs"));
        
        if (opts.isSet('D') && !mocaDocsFile.equals(baseDirectory)) {
            File unableToDeleteFile = deleteDirectoryAndContents(baseDirectory, 
                    subDirectoryList);
            
            if (unableToDeleteFile != null) {
                System.err.print("There was a problem deleting the file: ");
                System.err.println(unableToDeleteFile.getAbsolutePath());
                System.exit(1);
            }
        }

        File unableToCreateFile = createDirectories(moca, baseDirectory, 
                subDirectoryList);
        
        if (unableToCreateFile != null) {
            System.err.print("There was a problem creating the file: ");
            System.err.println(unableToCreateFile.getAbsolutePath());
            System.exit(1);
        }
        
        File unableToCopyFile = copyFiles(moca, baseDirectory, fileList);
        
        if (unableToCopyFile != null) {
            System.err.print("There was a problem copying the file: ");
            System.err.println(unableToCopyFile.getAbsolutePath());
            System.exit(1);
        }
        
        unableToCopyFile = copyIntegratorGifs(moca, baseDirectory);
        
        if (unableToCopyFile != null) {
            System.err.print("There was a problem copying the file: ");
            System.err.println(unableToCopyFile.getAbsolutePath());
            System.exit(1);
        }
        
        if (loadAll || type.contains("commands")) {
            System.out.println("Creating command documentation...");
            SystemContext system = ServerUtils.globalContext();
            
            Mbuild mbuild = new Mbuild(system);
            
            CommandRepository repos = mbuild.getRepository(new Mbuild.MbuildLogger(false));
            
            try {
                mbuild.writeDocumentation(repos, baseDirectory);
            }
            catch (CommandDocumentationException e) {
                System.err.println("There was an error writing documentation.");
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
        
        if (loadAll || type.contains("database")) {
            System.out.println("Creating database schema documentation...");
            try {
                moca.executeCommand("generate db documentation");
            }
            catch (MocaException e) {
                System.err.println("There was an error generating db documentation.");
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
        
        // If SLDIR exists and either integration was supplied or load all was
        // then we load the integration documentation.
        if ((loadAll || type.contains("integration")) && 
                moca.getSystemVariable("SLDIR") != null) {
            System.out.println("Creating integration documentation...");
            
            String enaFlag;
            
            if (integratorTransType.contains("all")) {
                enaFlag = "F";
            }
            else {
                enaFlag = "T";
            }
            
            try {
                moca.executeCommand("sl_produce integration doc", 
                        new MocaArgument("ena_flg", enaFlag));
            }
            catch (MocaException e) {
                System.err.println("There was an error generating integration documentation.");
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }
    
    private static File deleteDirectoryAndContents(File baseDirectory,
            List<String> subDirectories) {
        System.out.println("Removing the existing documentation directory tree...");
        File[] files = baseDirectory.listFiles();
        for (File file : files) {
            // If the sub directory wasn't supplied then we delete everything
            // else we only delete if it is in the list
            if (subDirectories != null && 
                    !subDirectories.contains(file.getName())) {
                continue;
            }
            if (file.isDirectory()) {
                File notDeletedFile = deleteDirectoryAndContents(file, null);
                if (notDeletedFile != null) {
                    return notDeletedFile;
                }
            }
            else if (!file.delete()) {
                return file;
            }
        }
        // If the sub directory isn't provided means we are in a nested call
        // which means we have to delete the directory as well.
        if (subDirectories == null && !baseDirectory.delete()) {
            return baseDirectory;
        }
        
        return null;
    }
    
    private static File createDirectories(MocaContext moca, File directory, 
            List<String> subDirectories) {
        
        System.out.println("Creating the documentation directory tree...");
        
        String tempDir = MocaUtils.expandEnvironmentVariables(moca, "$LESDIR/temp");
        
        File tempDirFile = new File(tempDir);
        
        if (!tempDirFile.exists()) {
            if (!tempDirFile.mkdirs()) {
                return tempDirFile;
            }
        }
        
        for (String subDirectory : subDirectories) {
            File childDirFile = new File(directory, subDirectory);
            if (!childDirFile.exists()) {
                if (!childDirFile.mkdir()) {
                    return childDirFile;
                }
            }
        }
        
        return null;
    }
    
    private static File copyFiles(MocaContext moca, File baseDirectory, 
            List<String> fileListLocations) {
        
        System.out.println("Copying base files into the documentation directory tree...");
        
        for (String fileListLocation : fileListLocations) {
            String[] fileListArray = fileListLocation.split(",");
            
            File originalFile = new File(MocaUtils.expandEnvironmentVariables(
                    moca, fileListArray[0].replace('\\', '/')));
            String fileName = originalFile.getName();
            String target = fileListArray[1];
            
            File targetFile;
            // If the target has more then a directory then we use their name
            // This is because java doesn't really have the copy functionality
            // as a command prompt where it will throw the file in the directory
            // if it already exists or create the new name in the new directory
            if (target.contains("\\") || target.contains("/")) {
                targetFile = new File(baseDirectory, target);                
            }
            else {
                targetFile = new File(baseDirectory, target + '/' + fileName);
            }
            
            try {
                // We don't want to copy over an existing file.
                if (!targetFile.equals(originalFile)) {
                    String originalCanon = originalFile.getCanonicalPath();
                    String targetCanon = targetFile.getCanonicalPath();
                    MocaUtils.copyFile(originalCanon, targetCanon);
                }
            }
            catch (IOException e) {
                System.err.print("Exception occurred while copying file: ");
                System.err.print(originalFile);
                System.err.print(" to: ");
                System.err.println(targetFile);
                e.printStackTrace(System.err);
                
                return originalFile;
            }
            catch (MocaIOException e) {
                System.err.print("Exception occurred while copying file: ");
                System.err.print(originalFile);
                System.err.print(" to: ");
                System.err.println(targetFile);
                e.printStackTrace(System.err);
                
                return originalFile;
            }
        }
        
        return null;
    }
    
    private static File copyIntegratorGifs(MocaContext moca, 
            File baseDirectory) {
        
        String expandedSlGifDir = MocaUtils.expandEnvironmentVariables(moca, 
                "$SLDIR/docs/gif");
        
        File slGifDir = new File(expandedSlGifDir);
        
        if (slGifDir.exists() && slGifDir.isDirectory()) {
            File[] files = slGifDir.listFiles(new SuffixFilenameFilter("gif"));
            
            for (File file : files) {
                String targetFile = baseDirectory.getAbsolutePath() + "/gif/"
                        + file.getName();
                try {
                    MocaUtils.copyFile(file.getAbsolutePath(),
                            targetFile);
                }
                catch (MocaIOException e) {
                    System.err.print("Exception occurred while copying file: ");
                    System.err.print(file);
                    System.err.print(" to: ");
                    System.err.println(targetFile);
                    e.printStackTrace(System.err);
                    
                    return file;
                }
            }
        }
        
        return null;
    }
    
    private static void showUsage() {
        System.err.println("Usage: mgendoc.pl [-t <type>] [-d <directory>] [-i <trans>] [-D]");
        System.err.println("                                                                ");
        System.err.println("       Where <type> is the type of documentation to             ");
        System.err.println("                    generate and is one of: all                 ");
        System.err.println("                                   commands                     ");
        System.err.println("                                   database                     ");
        System.err.println("                                   integration                  ");
        System.err.println("                                   moca                         ");
        System.err.println("                                   wm                           ");
        System.err.println("                                                                ");
        System.err.println("       Where <trans> is the type of integration transactions to ");
        System.err.println("                     generate and is one of:                    ");
        System.err.println("                                   all                          ");
        System.err.println("                                   enabled                      ");
        System.err.println("                                                                ");
        System.err.println("       Where -D deletes any existing documentation              ");
        System.err.println("                                                                ");
    }
}
