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

package com.redprairie.moca.server.profile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaInterruptedException;

/**
 * A version of command usage/profiling that handles persistent file storage,
 * in the form of a CSV (comma-separated values) file.  This file has the
 * advantage that it can be directly imported into spreadsheet programs for
 * more extensive analysis.
 * 
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class PersistedCommandUsage implements CommandUsage {
    
    /**
     * 
     */
    public PersistedCommandUsage(File usageFile) {
        _usageFile = usageFile;
        if (usageFile == null) {
            _logger.warn("No persistent profiling -- in memory only");
        }
        else if (usageFile.exists()) {
            try {
                readFile(usageFile);
            }
            catch (IOException e) {
                _logger.warn("Unable to read persistent profile: " + e);
            }
        }
        
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(this));
    }
    
    private static class ShutdownHook extends Thread {
        public ShutdownHook(PersistedCommandUsage usage) {
            _usage = new WeakReference<PersistedCommandUsage>(usage);
        }
        @Override
        public void run() {
            try {
                PersistedCommandUsage usage = _usage.get();
                if (usage != null) {
                    usage.writeFile(usage._usageFile);
                }
            }
            catch (IOException e) {
                _logger.debug("Unable to write persistent profile: " + e);
            }
        }
        
        private final WeakReference<PersistedCommandUsage> _usage;
    }
    
    @Override
    public void logCommandExecution(CommandPath path, long nanos) {
        
        CommandUsageStatistics stats = _pathLog.get(path);

        if (stats == null) {
            String strPath = path.toString();
            stats = _persistedLog.get(strPath);
        
            if (stats == null) {
                CommandPathElement cmd = path.getTop();
                stats = new CommandUsageStatistics(cmd, path);
                
                _persistedLog.put(strPath, stats);
            }
            _pathLog.put(path, stats);
        }

        stats.log(nanos);
    }
    
    @Override
    public Collection<CommandUsageStatistics> getStats() {
        List<CommandUsageStatistics> result = new ArrayList<CommandUsageStatistics>();
        
        for (CommandUsageStatistics stats : _persistedLog.values()) {
            CommandUsageStatistics copy = (CommandUsageStatistics) stats.clone();
            result.add(copy);
        }
        
        // Now that we have a copy of the values, it's safe to do some
        // calculations on them.
        ProfileUtils.calculateChildTimes(result);
        
        return result;
    }
    
    @Override
    public void reset() {
        _pathLog.clear();
        _persistedLog.clear();
    }
    
    private void readFile(File file) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        try {
            String line = in.readLine();
            if (line == null) {
                return;
            }
            
            String[] names = line.split(",");
            if (names.length < 8) {
                _logger.warn("Bad data format, ignoring persistent profile");
                return;
            }
            
            while ((line = in.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 8) {
                    _logger.warn("Bad data format, persistent profile may be invalid");
                    continue;
                }
                String levelName = values[0];
                String commandName = values[1];
                String commandType = values[2];
                String commandPath = values[3];
                try {
                    int count = Integer.parseInt(values[4]);
                    int total = Integer.parseInt(values[5]);
                    int min = Integer.parseInt(values[6]);
                    int max = Integer.parseInt(values[7]);
                
                    CommandUsageStatistics tmp = new CommandUsageStatistics(
                        levelName, commandName, commandType, commandPath,
                        count, total, min, max);
                    _persistedLog.put(tmp.getPath(), tmp);
                }
                catch (NumberFormatException e) {
                    _logger.warn("Bad data format, persistent profile may be invalid");
                }
            }
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        finally {
            in.close();
        }
    }
    
    private void writeFile(File file) throws IOException {
        Collection<CommandUsageStatistics> values = _persistedLog.values();
        
        // This method gets called when everything else is already done,
        // so it's safe to alter the statistics to include child times.
        ProfileUtils.calculateChildTimes(values);

        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        
        try {
            ProfileUtils.writeUsage(values, out);
        }
        finally {
            out.close();
        }
    }

    private final ConcurrentMap<CommandPath, CommandUsageStatistics> _pathLog = new ConcurrentHashMap<CommandPath, CommandUsageStatistics>();
    private final ConcurrentMap<String, CommandUsageStatistics> _persistedLog = new ConcurrentHashMap<String, CommandUsageStatistics>();
    private final File _usageFile;
    private final static Logger _logger = LogManager.getLogger(PersistedCommandUsage.class);
}
