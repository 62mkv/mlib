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

package com.redprairie.moca.coverage.repository;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.List;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.server.exec.CommandNotFoundException;
import com.redprairie.moca.server.exec.ExecutableComponent;
import com.redprairie.moca.server.repository.Command;
import com.redprairie.moca.server.repository.CommandRepository;
import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.Trigger;

/**
 * This is just a command repository that forwards all calls to the wrapped
 * command repository and also holds onto a Cobertura ProjectData instance
 * so they can be serialized together.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class CoberturaCommandRepository implements CoverageCommandRepository, Serializable {
    private static final long serialVersionUID = 1275380885087074260L;
    /**
     * 
     */
    public CoberturaCommandRepository(CommandRepository repository, ProjectData data) {
        _repository = repository;
        _data = data;
    }
    
    // @see com.redprairie.moca.server.repository.CommandRepository#getLevelByName(java.lang.String)
    @Override
    public ComponentLevel getLevelByName(String levelname) {
        return _repository.getLevelByName(levelname);
    }

    // @see com.redprairie.moca.server.repository.CommandRepository#getCommandByName(java.lang.String)
    @Override
    public List<Command> getCommandByName(String name) {
        return _repository.getCommandByName(name);
    }

    // @see com.redprairie.moca.server.repository.CommandRepository#getLevels()
    @Override
    public List<ComponentLevel> getLevels() {
        return _repository.getLevels();
    }

    // @see com.redprairie.moca.server.repository.CommandRepository#getTriggerByCommandName(java.lang.String)
    @Override
    public List<Trigger> getTriggerByCommandName(String cmdName) {
        return _repository.getTriggerByCommandName(cmdName);
    }

    // @see com.redprairie.moca.server.repository.CommandRepository#getLevelCount()
    @Override
    public int getLevelCount() {
        return _repository.getLevelCount();
    }

    // @see com.redprairie.moca.server.repository.CommandRepository#getTriggerCount()
    @Override
    public int getTriggerCount() {
        return _repository.getTriggerCount();
    }

    // @see com.redprairie.moca.server.repository.CommandRepository#getCommandCount()
    @Override
    public int getCommandCount() {
        return _repository.getCommandCount();
    }

    // @see com.redprairie.moca.server.repository.CommandRepository#getAllCommands()
    @Override
    public List<Command> getAllCommands() {
        return _repository.getAllCommands();
    }

    // @see com.redprairie.moca.server.repository.CommandRepository#getAllTriggers()
    @Override
    public List<Trigger> getAllTriggers() {
        return _repository.getAllTriggers();
    }

    // @see com.redprairie.moca.server.repository.CommandRepository#resolveCommand(java.lang.String, boolean, com.redprairie.moca.server.repository.Command)
    @Override
    public ExecutableComponent resolveCommand(String verbNounClause,
        boolean override, Command current) throws CommandNotFoundException {
        return _repository.resolveCommand(verbNounClause, override, current);
    }

    // @see com.redprairie.moca.coverage.repository.CoverageCommandRepository#getProjectData()
    @Override
    public ProjectData getProjectData() {
        return _data;
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        final File file = getCoberturaFile();
        _logger.info("Using datafile " + file + " for redprairie cobertura!");
        synchronized (CoberturaCommandRepository.class) {
            if (file.exists() && file.isFile()) {
                merge(file);
            }
        }
        Runtime.getRuntime().addShutdownHook(new CoberturaShutdownHook(_data, file));
    }
    
    private void merge(File file){
        ProjectData projData = CoverageDataFileHandler.loadCoverageData(file);
        if (projData != null) {
            _logger.info("Merging existing moca cobertura file");
            _data.merge(projData);
        }
    }
    
    private File getCoberturaFile(){
        String dataFile = System.getProperty("redprairie.cobertura.datafile");

        final File file;
        if (dataFile == null) {
            dataFile = "redprairie-cobertura.ser";
            // Try to put it in the $LESDIR/data by default
            String lesDir = System.getProperty("LESDIR");

            if (lesDir != null && lesDir.length() > 0) {
                file = new File(new File(lesDir, "data"), dataFile);
            }
            else {
                file = new File(dataFile);
            }
        }
        else {
            file = new File(dataFile);
        }
        
        return file;
    }
    
    // @see java.lang.Object#finalize()

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        final File file = getCoberturaFile();
        synchronized (CoberturaCommandRepository.class) {
            if (file.exists() && file.isFile()) {
                merge(file);
            }
            CoverageDataFileHandler.saveCoverageData(_data, file);
        }
    }


    public static class CoberturaShutdownHook extends Thread{
        private final WeakReference<ProjectData> _data;
        private final File _file;
        
        public CoberturaShutdownHook(ProjectData data, File file){
            _data = new WeakReference<ProjectData>(data);
            _file = file;
        }
        
        @Override
        // @see java.lang.Thread#run()
        public void run() {
            // Synchronize in case if multiple guys are trying to push
            synchronized (CoberturaCommandRepository.class) {
                ProjectData data = _data.get();
                if (data != null) {
                    if (_file.exists() && _file.isFile()) {
                        ProjectData projData = CoverageDataFileHandler
                            .loadCoverageData(_file);
                        if (projData != null) {
                            _logger
                                .info("Merging now existant moca cobertura file");
                            data.merge(projData);
                        }
                    }
                    CoverageDataFileHandler.saveCoverageData(data, _file);
                }
            }
        }
    }
    
    private final CommandRepository _repository;
    private final ProjectData _data;
    private static final Logger _logger = LogManager.getLogger(CoberturaCommandRepository.class);
}
