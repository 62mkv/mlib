/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.redprairie.moca.server.repository;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.server.exec.CommandSequence;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.parse.MocaParser;

/**
 * Trigger - This class is the in memory representation of the *.mtrg files.
 * 
 * 
 * <pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author cjolly
 * @version $Revision$
 */

public class MocaTrigger implements Serializable, Trigger, TriggerOverridable {

    /**
     * Default constructor
     */
    public MocaTrigger(String name, String command) {
        _name = name;
        _command = command;
        _description = null;
        _syntax = null;
        _documentation = null;
        _version = null;
        _disabled = false;
        _sequence = 0;
    }

    // @see com.redprairie.moca.server.repository.Trigger#getName()
    
    @Override
    public String getName() {
        return _name;
    }

    // @see com.redprairie.moca.server.repository.Trigger#getCommand()
    
    @Override
    public String getCommand() {
        return _command;
    }

    // @see com.redprairie.moca.server.repository.Trigger#getDescription()
    
    @Override
    public String getDescription() {
        return _description;
    }

    /**
     * Set the description for this Trigger
     * 
     * @param description
     */
    public void setDescription(String description) {
        this._description = description;
    }

    // @see com.redprairie.moca.server.repository.Trigger#getSyntax()
    
    @Override
    public String getSyntax() {
        return _syntax;
    }

    /**
     * Sets the Local syntax definition of the trigger
     * 
     * @param syntax
     */
    public void setSyntax(String syntax) {
        this._syntax = syntax;
    }

    // @see com.redprairie.moca.server.repository.Trigger#getDocumentation()
    
    @Override
    public String getDocumentation() {
        return _documentation;
    }

    /**
     * Sets the text of the Documentation.
     * 
     * @param documentation
     */
    public void setDocumentation(String documentation) {
        this._documentation = documentation;
    }

    // @see com.redprairie.moca.server.repository.Trigger#getFileName()
    
    @Override
    public String getFileName() {
        return _fileName;
    }
    
    // @see com.redprairie.moca.server.repository.Trigger#getFile()
    
    @Override
    public File getFile() {
        return _file;
    }

    /**
     * Set the file for this Trigger. This will also set the
     * <code>fileName</code> property.
     * 
     * @param documentation
     */
    public void setFile(File file) {
        _file = file;
        _fileName = file.getName();
    }

    // @see com.redprairie.moca.server.repository.Trigger#getVersion()
    
    @Override
    public String getVersion() {
        return _version;
    }

    /**
     * set the Version of this Trigger
     * 
     * @param version
     */
    public void setVersion(String version) {
        this._version = version;
    }

    /**
     * Checks the value of the disable property of this Trigger.
     * 
     * @return true or false
     */
    @Override
    public boolean isDisabled() {
        return _disabled;
    }

    /**
     * Set the disable property
     * 
     * @param disable
     */
    public void setDisabled(boolean disabled) {
        this._disabled = disabled;
    }

    // @see com.redprairie.moca.server.repository.Trigger#getFireSequence()
    
    @Override
    public int getFireSequence() {
        return _sequence;
    }

    /**
     * @see #getSortSeq()
     * @param sequence
     */
    public void setFireSequence(int sequence) {
        this._sequence = sequence;
    }
    
    public void addArgument(ArgumentInfo arg) {
        if (_args == null) {
            _args = new ArrayList<ArgumentInfo>();
        }
        _args.add(arg);
    }
    
    // @see com.redprairie.moca.server.repository.Trigger#getArguments()
    
    @Override
    public List<ArgumentInfo> getArguments() {
        return _args;
    }
    
    public void setSortSequence(int sortSequence) {
        _sortSequence = sortSequence;
    }
    
    // @see com.redprairie.moca.server.repository.Trigger#getSortSequence()
    
    @Override
    public int getSortSequence() {
        return _sortSequence;
    }
    
    // @see com.redprairie.moca.server.repository.Trigger#execute(com.redprairie.moca.server.exec.ServerContext)
    
    @Override
    public void execute(ServerContext ctx) throws MocaException {
        
        if (_disabled) {
            return;
        }
        
        CommandSequence compiled;
        synchronized(this) {
            if (_compiled == null) {
                _compiled = new MocaParser(_syntax).parse(); 
            }
            compiled = _compiled;
        }
        
        compiled.execute(ctx);
        
        // This is a trigger, so throw away the results.
        return;
    }
    
    // @see java.lang.Object#toString()
    
    @Override
    public String toString() {
        return "TRIGGER(" + _name + " on " + _command + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MocaTrigger)) {
            return false;
        }
        
        MocaTrigger other = (MocaTrigger) obj;
        return _name.equals(other._name) && _command.equals(other._command);
    }
    
    @Override
    public int hashCode() {
        return _name.hashCode() * 37 + _command.hashCode();
    }
    
    // @see com.redprairie.moca.server.repository.TriggerOverridable#overrideTrigger(com.redprairie.moca.server.repository.Trigger)
    @Override
    public void overrideTrigger(Trigger trigger) {
        _description = trigger.getDescription();
        _syntax = trigger.getSyntax();
        _documentation = trigger.getDocumentation();
        _version = trigger.getVersion();
        _sequence = trigger.getFireSequence();
        
        if (trigger.getSortSequence() > _sortSequence) {
            _sortSequence = trigger.getSortSequence();
            _disabled = trigger.isDisabled();
        }
    }

    //
    // Implementation
    //
    private final String _name;
    private final String _command; // command upon which this Trigger will fire
    private int _sortSequence;
    private String _description;
    private transient File _file;
    private String _fileName;
    private transient String _documentation;
    private String _syntax;
    private String _version;
    private boolean _disabled;
    private int  _sequence;
    private transient List<ArgumentInfo> _args;
    private transient CommandSequence _compiled;
    private static final long serialVersionUID = 1907590583555725950L;
}
