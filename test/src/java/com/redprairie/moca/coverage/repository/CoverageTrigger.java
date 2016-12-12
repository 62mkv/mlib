/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.coverage.repository;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.coverage.parse.CoverageMocaParser;
import com.redprairie.moca.server.exec.CommandSequence;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.repository.ArgumentInfo;
import com.redprairie.moca.server.repository.Trigger;
import com.redprairie.moca.server.repository.TriggerOverridable;

/**
 * This is a standard trigger with code coverage classes included
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class CoverageTrigger implements Trigger, TriggerOverridable, Serializable {
    private static final long serialVersionUID = 2366730406267725261L;

    /**
     * @param name
     * @param command
     */
    public CoverageTrigger(Trigger trigger, MocaClassData classData, int offset) {
        _trigger = trigger;
        _data = classData;
        _lineOffset = offset;
    }

    // @see com.redprairie.moca.server.repository.Trigger#getName()
    @Override
    public String getName() {
        return _trigger.getName();
    }
    
    // @see com.redprairie.moca.server.repository.Trigger#getCommand()
    @Override
    public String getCommand() {
        return _trigger.getCommand();
    }
    
    // @see com.redprairie.moca.server.repository.Trigger#getDescription()
    @Override
    public String getDescription() {
        if (_overriddenTrigger != null) {
            return _overriddenTrigger.getDescription();
        }
        return _trigger.getDescription();
    }
    
    // @see com.redprairie.moca.server.repository.Trigger#getSyntax()
    @Override
    public String getSyntax() {
        if (_overriddenTrigger != null) {
            return _overriddenTrigger.getSyntax();
        }
        return _trigger.getSyntax();
    }
    
    // @see com.redprairie.moca.server.repository.Trigger#getDocumentation()
    @Override
    public String getDocumentation() {
        if (_overriddenTrigger != null) {
            return _overriddenTrigger.getDocumentation();
        }
        return _trigger.getDocumentation();
    }
    
    // @see com.redprairie.moca.server.repository.Trigger#getFileName()
    @Override
    public String getFileName() {
        return _trigger.getFileName();
    }
    
    // @see com.redprairie.moca.server.repository.Trigger#getFile()
    @Override
    public File getFile() {
        return _trigger.getFile();
    }
    
    // @see com.redprairie.moca.server.repository.Trigger#getVersion()
    @Override
    public String getVersion() {
        if (_overriddenTrigger != null) {
            return _overriddenTrigger.getVersion();
        }
        return _trigger.getVersion();
    }
    
    // @see com.redprairie.moca.server.repository.Trigger#getFireSequence()
    @Override
    public int getFireSequence() {
        if (_overriddenTrigger != null) {
            return _overriddenTrigger.getFireSequence();
        }
        return _trigger.getFireSequence();
    }
    
    // @see com.redprairie.moca.server.repository.Trigger#getArguments()
    @Override
    public List<ArgumentInfo> getArguments() {
        return _trigger.getArguments();
    }
    
    // @see com.redprairie.moca.server.repository.Trigger#getSortSequence()
    @Override
    public int getSortSequence() {
        int overriddenSortSequence = Integer.MIN_VALUE;
        if (_overriddenTrigger != null) {
            overriddenSortSequence = _overriddenTrigger.getSortSequence();
        }
        
        int targetSortSequence = _trigger.getSortSequence();
        
        return overriddenSortSequence < targetSortSequence ? targetSortSequence : overriddenSortSequence;
    }
    
    // @see com.redprairie.moca.server.repository.Trigger#execute(com.redprairie.moca.server.exec.ServerContext)
    @Override
    public void execute(ServerContext ctx) throws MocaException {
        if (isDisabled()) {
            return;
        }
        
        CommandSequence compiled;
        synchronized(this) {
            if (_compiled == null) {
                _compiled = new CoverageMocaParser(getSyntax(), _data, 
                    false, _lineOffset).parse(); 
            }
            compiled = _compiled;
        }
        
        compiled.execute(ctx);
        
        // This is a trigger, so throw away the results.
        return;
    }
    
    // @see com.redprairie.moca.server.repository.Trigger#isDisabled()
    public boolean isDisabled() {
        int overriddenSortSequence = Integer.MIN_VALUE;
        if (_overriddenTrigger != null) {
            overriddenSortSequence = _overriddenTrigger.getSortSequence();
        }
        
        int targetSortSequence = _trigger.getSortSequence();
        
        return overriddenSortSequence < targetSortSequence ? _trigger.isDisabled() : _overriddenTrigger.isDisabled();
    }
    
    // @see com.redprairie.moca.server.repository.TriggerOverridable#overrideTrigger(com.redprairie.moca.server.repository.Trigger)
    @Override
    public void overrideTrigger(Trigger trigger) {
        _overriddenTrigger = trigger;
    }
    
    // @see java.lang.Object#equals(java.lang.Object)
    @Override
    public boolean equals(Object obj) {
        return _trigger.equals(obj);
    }
    
    // @see java.lang.Object#hashCode()
    @Override
    public int hashCode() {
        return _trigger.hashCode();
    }

    private transient CommandSequence _compiled;
    private Trigger _overriddenTrigger;
    private final MocaClassData _data;
    private final Trigger _trigger;
    private final int _lineOffset;
}
