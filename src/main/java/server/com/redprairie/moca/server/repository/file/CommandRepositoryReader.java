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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.redprairie.moca.server.exec.CommandNotFoundException;
import com.redprairie.moca.server.parse.CommandReference;
import com.redprairie.moca.server.parse.MocaParseException;
import com.redprairie.moca.server.parse.MocaParser;
import com.redprairie.moca.server.parse.MocaSyntaxWarning;
import com.redprairie.moca.server.repository.Command;
import com.redprairie.moca.server.repository.CommandRepository;
import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.LocalSyntaxCommand;
import com.redprairie.moca.server.repository.MocaCommandRepository;
import com.redprairie.moca.server.repository.Trigger;

/**
 * Reads the MOCA command repository source directory structure and produces the resulting
 * in-memory command repository.
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class CommandRepositoryReader {
    
    /**
     *
     */
    public interface RepositoryReaderEvents {

        /**
         * @param length
         */
        void setDirectoryCount(int length);

        /**
         * @param string
         */
        void setCurrentDirectory(File directory);

        /**
         * @param string
         */
        void setCurrentLevel(File levelFile);

        /**
         * @param string
         */
        void setCurrentCommand(File commandFile);

        /**
         * @param string
         */
        void setCurrentTrigger(File triggerFile);

        /**
         * @param string
         * @param e
         */
        void reportError(String string, Throwable e);
        
        /**
         * @param msg
         */
        void reportWarning(String msg);
        
        /**
         * @param msg
         */
        void reportTriggerOverrideError(String msg);
        
        /**
         * @param length
         */
        void setLevelCount(int length);

        /**
         * @param length
         */
        void setCommandCount(int length);

        /**
         * @param length
         */
        void setTriggerCount(int length);

        /**
         * 
         */
        void finishedLoading();
    }
    
    /**
     * A filename filter that searches for files with a particular suffix.
     */
    public static class SuffixFilter implements FilenameFilter {
        public SuffixFilter(String suffix) {
            if (suffix .startsWith(".")) {
                _suffix = suffix;
            }
            else {
                _suffix = "." + suffix;
            }
        }

        // @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(_suffix);
        }
        
        private final String _suffix;
    }
    
    public CommandRepositoryReader(File[] directories, RepositoryReaderEvents events, RepositoryFileReaderFactory cmdReadFact) {
        this (directories, events, cmdReadFact, 0);
    }
    
    public CommandRepositoryReader(File[] directories, RepositoryReaderEvents events, RepositoryFileReaderFactory cmdReadFact,
                                   int warningDepth) {
        _directories = Arrays.copyOf(directories, directories.length);
        _events = events;        
        _reader = cmdReadFact.getCommandReader(_events);
        _levelReader = cmdReadFact.getLevelReader(_events);
        _triggerReader = cmdReadFact.getTriggerReader(_events);
        _warningDepth = warningDepth;
    }
    
    public CommandRepository readAll() {
        _events.setDirectoryCount(_directories.length);
        MocaCommandRepository repos = new MocaCommandRepository();
        for (File dir : _directories) {
            _directoriesProcessed++;
            _events.setCurrentDirectory(dir);
            
            if (!dir.exists()) {
                _events.reportWarning("Directory " + dir + " does not exist");
                continue;
            }
            
            // Grab a list of .mlvl files in the directory.
            File[] levelFiles = dir.listFiles(new SuffixFilter(".mlvl"));
            _events.setLevelCount(levelFiles.length);

            // Read each *.mlvl file
            for (File levelFile : levelFiles) {                
                
                ComponentLevel level;
                try {
                    _levelsProcessed++;
                    _events.setCurrentLevel(levelFile);
                    level = _levelReader.read(levelFile);
                    // Check to see if we've already read this level
                    ComponentLevel existing = repos.getLevelByName(level.getName());
                    if (existing != null) {
                        throw new RepositoryReadException(
                            "Level " + level.getName() + " already exists in repository");
                    }

                    repos.addLevel(level);
                }
                catch (RepositoryReadException e) {
                    _errorCount++;
                    _events.reportError(e.getMessage(), null);
                    continue;
                }
                
                // Figure out the subdirectory name by stripping off the .mlvl from the
                // level file name (we know it ends in .mlvl)
                String levelName = levelFile.getName();
                levelName = levelName.substring(0, levelName.length() - 5);
                File levelDir = new File(levelFile.getParentFile(), levelName);
                
                // If the level directory doesn't exist or isn't a directory,
                // then we need to notify the builder so they can resolve
                if (levelDir == null || !levelDir.exists() || 
                        !levelDir.isDirectory()) {
                    _errorCount++;
                    _events.reportError("Unable to find cmdsrc directory: "
                            + levelDir.getAbsolutePath(),
                            new FileNotFoundException());
                    continue;
                }
                
                // Save the level location to the level itself
                try {
                    level.setCmdDir(levelDir.getCanonicalPath());
                } catch (IOException e) {
                    level.setCmdDir(levelDir.getAbsolutePath());
                }

                // Look for matching files for commands and triggers
                File[] commandFiles = levelDir.listFiles(new SuffixFilter(".mcmd"));
                _events.setCommandCount(commandFiles.length);

                File[] triggerFiles = levelDir.listFiles(new SuffixFilter(".mtrg"));
                _events.setTriggerCount(triggerFiles.length);
                
                // Spin through command files in the subdirectory
                Set<String> commands = new LinkedHashSet<String>();
                for (File commandFile : commandFiles) {
                    
                    try {
                        _commandsProcessed++;
                        _events.setCurrentCommand(commandFile);
                        Command command = _reader.read(commandFile, level);
                        String commandName = command.getName();
                        boolean added = commands.add(commandName);
                        if (!added) {
                            _errorCount++;
                            _events.reportError("The command: \"" + commandName
                                    + "\" already exists in level: "
                                    + levelName, null);
                        }
                        
                        command.setFile(commandFile);
                        repos.addCommand(command);
                    }
                    catch (RepositoryReadException e) {
                        _errorCount++;
                        _events.reportError(e.getMessage(), null);
                    }
                }
                
                // Spin through trigger files in the subdirectory
                for (File triggerFile : triggerFiles) {
                    try {
                        _triggersProcessed++;
                        _events.setCurrentTrigger(triggerFile);
                        Trigger trigger = _triggerReader.read(triggerFile, level);
                        repos.addTrigger(trigger);
                    }
                    catch (RepositoryReadException e) {
                        _errorCount++;
                        _events.reportError(e.getMessage(), null);
                    }
                }
            }
        }
        
        List<RepositoryReadException> exceptions = repos.consolidateTriggers();
        
        for (RepositoryReadException excp : exceptions) {
            _errorCount++;
            _events.reportTriggerOverrideError(excp.getMessage());
        }
        
        // Loop through all the commands we know about in the repository
        List<Command> commands = repos.getAllCommands();
        _events.setCommandCount(commands.size());
        for (Command c : commands) {
            _events.setCurrentCommand(c.getFile());
            // We're specifically looking at local syntax commands.
            if (c instanceof LocalSyntaxCommand) {
                
                // Chec the syntax.  This is somewhat unnecessary, as the syntax
                // has already been checked.  We need this, though, to find
                // unresolved commands
                String syntax = ((LocalSyntaxCommand) c).getSyntax();
                MocaParser parser = new MocaParser(syntax, _warningDepth);
                try {
                    parser.parse();
                }
                catch (MocaParseException e) {
                    _errorCount++;
                    _events.reportError("Unexpected parse error: ", e);
                }
                
                // Now find, and attempt to resolve command references.
                for (CommandReference ref : parser.getCommandReferences()) {
                    try {
                        repos.resolveCommand(ref.getVerbNounClause(), ref.isOverride(), c);
                    }
                    catch (CommandNotFoundException e) {
                        _events.reportWarning("Referenced command not found: " + ref.getVerbNounClause());
                    }
                }
                for (MocaSyntaxWarning warn : parser.getWarnings()) {
                    _events.reportWarning(String.valueOf(warn));
                }
            }
        }
        
        List<Trigger> triggers = repos.getAllTriggers();
        _events.setTriggerCount(triggers.size());
        for (Trigger t : triggers) {
            _events.setCurrentTrigger(t.getFile());
            String syntax = t.getSyntax();
            if (syntax != null) {
                MocaParser parser = new MocaParser(syntax, _warningDepth);
                try {
                    parser.parse();
                }
                catch (MocaParseException e) {
                    _errorCount++;
                    _events.reportError("Unexpected parse error: ", e);
                }
                
                for (CommandReference ref : parser.getCommandReferences()) {
                    try {
                        repos.resolveCommand(ref.getVerbNounClause(), ref.isOverride(), null);
                    }
                    catch (CommandNotFoundException e) {
                        _events.reportWarning("Referenced command not found: " + ref.getVerbNounClause());
                    }
                }
                for (MocaSyntaxWarning warn : parser.getWarnings()) {
                    _events.reportWarning(String.valueOf(warn));
                }
            }
        }
        
        _events.finishedLoading();
        return repos;
    }
    
    public int getDirectoriesProcessed() {
        return _directoriesProcessed;
    }

    public int getLevelsProcessed() {
        return _levelsProcessed;
    }

    public int getCommandsProcessed() {
        return _commandsProcessed;
    }

    public int getTriggersProcessed() {
        return _triggersProcessed;
    }

    public int getErrorCount() {
        return _errorCount;
    }

    //
    // Implementation
    //
    private final File[] _directories;
    private final RepositoryReaderEvents _events;
    private int _directoriesProcessed;
    private int _levelsProcessed;
    private int _commandsProcessed;
    private int _triggersProcessed;
    private int _errorCount;
    
    private final CommandReader _reader;
    private final LevelReader _levelReader;
    private final TriggerReader _triggerReader;
    private final int _warningDepth;
}
