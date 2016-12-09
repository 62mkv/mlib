/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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

package com.redprairie.moca.server.repository.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.repository.CommandRepository;
import com.redprairie.moca.server.repository.docs.CommandDocumentationException;
import com.redprairie.moca.server.repository.docs.DocWriter;
import com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents;
import com.redprairie.moca.server.repository.file.xml.XMLRepositoryFileReaderFactory;
import com.redprairie.moca.util.AppUtils;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.moca.util.Options;
import com.redprairie.moca.util.OptionsException;

/**
 * Class containing the <code>main</code> method for mbuild, the command repository builder
 * application.  This is the primary command-line MOCA repository interface.  The purpose
 * of the application is to walk through a list of directories and read all the appropriate
 * files in them.  Command-line options include:
 * 
 * -v    for verbose mode.  This causes the mbuild process to perform extensive logging.
 * -h    display help information
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class Mbuild {
    public static void main(String[] args) {
        // Parse Command-line arguments
        System.setProperty("com.redprairie.moca.config","D:\\MFC\\mlib\\src\\resource\\82.registry");
        System.setProperty("LESDIR","D:\\MFC\\mlib");
        Options opts;
        try {
            opts = Options.parse("lvdp:D:h", args);
        }
        catch (OptionsException e1) {
            System.out.println("Usage: mbuild [-l] [-v] [-h] [-d] [-D depth] [-p doc-dir]");
            return;
        }
        
        if (opts.isSet('h')) {
            System.out.println("Usage: mbuild [-l] [-v] [-h]");
            System.out.println("\t-l      List current command information.");
            System.out.println("\t-d      Produce Command Documentation.");
            System.out.println("\t-pdir   Documentation Output Directory.");
            System.out.println("\t-Ddepth Enable pipe depth warning at depth.");
            System.out.println("\t-v      Verbose mode.  Provides increased logging.");
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
        
        System.out.print(AppUtils.getStartBanner("Mbuild"));
        
        int errorCount = 0;
        
        long startTime = System.currentTimeMillis();
        SystemContext sys = ServerUtils.globalContext();
        String memoryFile = sys.getConfigurationElement(MocaRegistry.REGKEY_SERVER_MEMORY_FILE);
        
        CommandRepository repository = null;
        if (opts.isSet('l')) {
            ObjectInputStream in = null;
            try {
                in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(memoryFile)));
                repository = (CommandRepository) in.readObject();
                System.out.printf( "  Component Levels: %4d%n", repository.getLevelCount());
                System.out.printf( "          Commands: %4d%n", repository.getCommandCount());
                System.out.printf( "          Triggers: %4d%n", repository.getTriggerCount());
            }
            catch (IOException e) {
                errorCount++;
                System.err.println(new Date() + " Error reading file: " + memoryFile + ": " + e);
                if (verbose) {
                    e.printStackTrace();
                }
            }
            catch (ClassNotFoundException e) {
                errorCount++;
                System.err.println(new Date() + " Error reading file: " + memoryFile + ": " + e);
                if (verbose) {
                    e.printStackTrace();
                }
            }
            finally {
                if (in != null) try {
                    in.close();
                }
                catch (IOException e) {
                    errorCount++;
                    System.err.println(new Date() + " Error closing file: " + 
                            memoryFile + ": " + e);
                    if (verbose) {
                        e.printStackTrace();
                    }
                }
            }

        }
        else {
            String prodDirs = sys.getConfigurationElement(MocaRegistry.REGKEY_SERVER_PROD_DIRS);
            if (prodDirs == null) {
                errorCount++;
                System.err.println("ERROR: No product directories are defined in the registry");
                System.exit(errorCount);
            }

            String[] pathNames = prodDirs.split(File.pathSeparator);
            
            List<File> dirs = new ArrayList<File>();
            for (int i = 0; i < pathNames.length; i++) {
                File dir = new File(pathNames[i] + CMDSRC);
                if (dir.exists()) {
                    dirs.add(dir);
                }
            }

            MbuildLogger logger = new MbuildLogger(verbose);

            CommandRepositoryReader reader = new CommandRepositoryReader(
                dirs.toArray(new File[dirs.size()]), logger,
                new XMLRepositoryFileReaderFactory(), warningDepth);
            repository = reader.readAll();
            
            // Finished reading the command into memory.
            if (opts.isSet('d')) {
                // Docs mode
                if (verbose) {
                    System.out.println("Writing documentation");
                }
                
                String outDir = opts.getArgument('p');
                if (outDir == null) {
                    outDir = MocaUtils.expandEnvironmentVariables(sys, "$LESDIR/docs"); 
                }
                
                try {
                    DocWriter docs = new DocWriter(new File(MocaUtils.expandEnvironmentVariables(sys, "$MOCADIR/docs/xsl")), 
                                                   new File(outDir),
                                                   repository);
                    docs.createIndexPage();
                    docs.createComponentLevelPages();
                    docs.createCommandPages();
                    docs.createTriggerPages();
                }
                catch (CommandDocumentationException e) {
                    errorCount++;
                    System.err.println(new Date() + " Error generating command " +
                            "documentation file: " + e);
                    if (verbose) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                // Now write the command configuration to disk
                if (verbose) {
                    System.out.println("Writing memory file: " + memoryFile);
                }
        
                // Actually write the configuration out via object serialization
                ObjectOutputStream out = null;
                try {
                    out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(memoryFile)));
                    out.writeObject(repository);
                    out.flush();
                }
                catch (IOException e) {
                    errorCount++;
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
                        errorCount++;
                        System.err.println(new Date() + " Error closing file: " + memoryFile + ": " + e);
                        if (verbose) {
                            e.printStackTrace();
                        }
                    }
                }
                
                // OK, wrap it up
                System.out.printf( "       Directories: %4d%n", reader.getDirectoriesProcessed());
                System.out.printf( "  Component Levels: %4d%n", reader.getLevelsProcessed());
                System.out.printf( "          Commands: %4d%n", reader.getCommandsProcessed());
                System.out.printf( "          Triggers: %4d%n", reader.getTriggersProcessed());
                System.out.printf( "   Errors Reported: %4d%n", reader.getErrorCount());
                
                errorCount += reader.getErrorCount();
            }
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("Total Elapsed Time: " + (endTime - startTime) + "ms");
        
        System.exit(errorCount);
    }
    
    public static class MbuildLogger implements RepositoryReaderEvents {
        
        /**
         * 
         */
        public MbuildLogger(boolean verbose) {
            _verbose = verbose;
        }

        @Override
        public void finishedLoading() {
            if (_verbose) {
                System.out.println(new Date() + " Finished reading repository");
            }
        }

        @Override
        public void reportError(String message, Throwable e) {
            _errorCount++;
            System.err.println("ERROR (" + _currentFile + "): "+ message +
                (e == null ? "" :(": " + e)));
            
            if (_verbose && e != null) {
                e.printStackTrace();
            }
        }
        
        @Override
        public void reportWarning(String message) {
            System.err.println("WARNING (" + _currentFile + "): " + message);
        }
        
        // @see com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents#reportTriggerOverrideError(java.lang.String)
        @Override
        public void reportTriggerOverrideError(String msg) {
            System.err.println("ERROR (Trigger Override): " + msg);
        }

        @Override
        public void setDirectoryCount(int count) {
            _dirCount = count;
            _currentDir = 0;
            if (_verbose) {
                System.out.println(new Date() + " Reading " + _dirCount + " directories");
            }
        }

        @Override
        public void setCurrentDirectory(File dir) {
            _currentDir++;
            if (_verbose) {
                System.out.println(new Date() + " Reading directory: " + dir);
            }
            _currentFile = dir;
        }

        @Override
        public void setLevelCount(int count) {
            _levelCount = count;
            _currentLevel = 0;
            if (_verbose) {
                System.out.println(new Date() + " Reading " + count + " levels");
            }
        }

        @Override
        public void setCurrentLevel(File levelFile) {
            _currentLevel++;
            if (_verbose) {
                System.out.println(new Date() + " Reading level [" + _currentLevel + " of " + _levelCount +"]: " + levelFile.getName());
            }
            _currentFile = levelFile;
        }

        @Override
        public void setCommandCount(int count) {
            _commandCount = count;
            _currentCommand = 0;
            if (_verbose) {
                System.out.println(new Date() + " Reading " + count + " command files");
            }
        }

        @Override
        public void setCurrentCommand(File commandFile) {
            _currentCommand++;
            if (_verbose) {
                System.out.println(new Date() + " Reading command [" + _currentCommand + " of " + _commandCount +"]: " + commandFile.getName());
            }
            _currentFile = commandFile;
        }

        @Override
        public void setTriggerCount(int count) {
            _triggerCount = count;
            _currentTrigger = 0;
            if (_verbose) {
                System.out.println(new Date() + " Reading " + count + " triggers");
            }
        }
        
        @Override
        public void setCurrentTrigger(File triggerFile) {
            _currentTrigger++;
            if (_verbose) {
                System.out.println(new Date() + " Reading trigger [" + _currentTrigger+ " of " + _triggerCount +"]: " + triggerFile.getName());
            }
            _currentFile = triggerFile;
        }
        
        /**
         * @return Returns the commandCount.
         */
        public int getCommandCount() {
            return _commandCount;
        }
        
        /**
         * @return Returns the dirCount.
         */
        public int getDirCount() {
            return _dirCount;
        }
        
        /**
         * @return Returns the levelCount.
         */
        public int getLevelCount() {
            return _levelCount;
        }
        
        /**
         * @return Returns the triggerCount.
         */
        public int getTriggerCount() {
            return _triggerCount;
        }
        
        /**
         * @return Returns the errorCount.
         */
        public int getErrorCount() {
            return _errorCount;
        }

        private final boolean _verbose;
        private int _dirCount;
        @SuppressWarnings("unused")
        private int _currentDir;
        private int _levelCount;
        private int _currentLevel;
        private int _commandCount;
        private int _currentCommand;
        private int _triggerCount;
        private int _currentTrigger;
        private File _currentFile;
        
        private int _errorCount;
    }
    
    /**
     * @param system
     * @throws NullPointerException This is thrown if the SystemContext is null
     * @throws IllegalArgumentException This is thrown if the SystemContext
     *         doesn't have any prod dirs
     */
    public Mbuild(SystemContext system) throws NullPointerException, 
            IllegalArgumentException {
        _sys = system;
        
        String prodDirs = _sys.getConfigurationElement(MocaRegistry.REGKEY_SERVER_PROD_DIRS);
        if (prodDirs == null) {
            throw new IllegalArgumentException("System context contained no" +
            		" prod dirs");
        }
        
        String[] pathNames = prodDirs.split(File.pathSeparator);
        
        dataDirectories = new File[pathNames.length];
        for (int i = 0; i < pathNames.length; i++) {
            dataDirectories[i] = new File(pathNames[i] + CMDSRC);
        }
    }
    
    public CommandRepository getRepository(RepositoryReaderEvents repositoryReaderEvents) {
        return getRepository(repositoryReaderEvents, 
            new XMLRepositoryFileReaderFactory(), 0);
    }
    
    public CommandRepository getRepository(
        RepositoryReaderEvents repositoryReaderEvents, 
        RepositoryFileReaderFactory factory,
        int warningDepth) {
        CommandRepositoryReader reader = new CommandRepositoryReader(
                dataDirectories, repositoryReaderEvents, factory, warningDepth);
        return reader.readAll();
    }
    
    public void writeDocumentation(CommandRepository repository, 
            File outputDirectory) throws CommandDocumentationException {
        File outDir;
        if (outputDirectory == null) {
            outDir = new File(MocaUtils.expandEnvironmentVariables(_sys, 
                    "$LESDIR/docs"));
        }
        else {
            outDir = outputDirectory;
        }
        
        DocWriter docs = new DocWriter(new File(
                MocaUtils.expandEnvironmentVariables(_sys, "$MOCADIR/docs/xsl")), 
                outDir, repository);
        docs.createIndexPage();
        docs.createComponentLevelPages();
        docs.createCommandPages();
        docs.createTriggerPages();
    }

    private final File[] dataDirectories;
    private final SystemContext _sys;
    
    private final static String CMDSRC = File.separator + "src"
            + File.separator + "cmdsrc";
}
