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

package com.redprairie.moca.server.repository;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.RequiredArgumentException;
import com.redprairie.moca.server.SecurityLevel;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.ExecutableComponent;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.ServerContextStatus;
import com.redprairie.moca.server.repository.docs.CommandDocumentation;
import com.redprairie.util.ArgCheck;

/**
 * Command - The in-memory representation of a command. currently the only
 * reason this class is abstract is because it should not be instantiated. <b>
 * 
 * <pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author cjolly
 * @version $Revision$
 */

public abstract class Command implements Serializable, ExecutableComponent {

    public Command(String name, ComponentLevel level) {
        ArgCheck.notNull(name);
        ArgCheck.notNull(level);
        
        _name = name;
        _level = level;
        _description = null;
        _documentation = null;
        _version = null;
        _fileName = null; 
        _security = SecurityLevel.OPEN;
        _readOnly = false;
        _args = new ArrayList<ArgumentInfo>();
        _transactionType = TransactionType.REQUIRED;
    }
    
    // @see com.redprairie.moca.server.exec.ExecutableComponent#execute(com.redprairie.moca.server.exec.ServerContext)
    @Override
    public final MocaResults execute(ServerContext ctx) throws MocaException {
        ServerContextStatus previousStatus = ctx.getCurrentStatus();
        try {
            ctx.setCurrentStatus(ServerContextStatus.getStatusForCommandType(
                    getType()));
            return executeWithContext(ctx);
        }
        finally {
            ctx.setCurrentStatus(previousStatus);            
        }
    }

    /**
     * This method is to be overridden by subclasses and is to be used as the
     * execution medium.
     * @param ctx The server context to execute on
     * @return The Moca Results from the execution
     * @throws MocaException If there was an exception while executing
     */
    protected abstract MocaResults executeWithContext(ServerContext ctx) throws MocaException;

    /**
     * returns a List of arguments associated with this object.
     * 
     * @return
     */
    public List<ArgumentInfo> getArguments() {
        return _args;
    }

    /**
     * Return the ArgumentInfo associated with he argument name
     * 
     * @param tname
     * @return
     */
    public ArgumentInfo getArgument(String tname) {
        ArgumentInfo arg = null;
        for (int i = 0; i < _args.size(); i++) {
            if (_args.get(i).getName().equals(tname)) {
                arg = _args.get(i);
                break;
            }
            if (_args.get(i).getAlias().equals(tname)) {
                arg = _args.get(i);
                break;
            }
        }
        return arg;
    }

    /**
     * Returns ArgumentInfo that was replaced or null
     * 
     * @param argument
     * @return returns an ArgumentInfo that was replace or if no replacement
     *         took place then null would be returned.
     */
    public void addArgument(ArgumentInfo argument) {
        _args.add(argument);
    }

    // @see com.redprairie.moca.server.repository.ExecutableComponent#getName()
    public String getName() {
        return _name;
    }

    /**
     * Common to all Command Objects. Sets the Name of the command
     * 
     * @param name
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Common to all Command Objects. Return the description
     * 
     * @return The commands description
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Common to all Command Objects. set the commands Description
     * 
     * @param description
     */
    public void setDescription(String description) {
        _description = description;
    }

    /**
     * Get the commands Level object
     * 
     * @return level object or null if not assigned.
     */
    public ComponentLevel getLevel() {
        return _level;
    }

    /**
     * Set the commands Level
     * 
     * @param level
     */
    public void setLevel(ComponentLevel level) {
        _level = level;
    }

    /**
     * Get the command type, alternatively one could use instanceof to determine
     * the specific command class associated with the Command object
     * 
     * @return CommandType enum reflecting the type of Command Object.
     */
    public abstract CommandType getType();

    /**
     * Common to all Command Objects. This function retrieves the documentation
     * for the Command. The storage for the Documentation is transient so the
     * documenation may or maynot exist.
     * 
     * @return String
     */
    public CommandDocumentation getDocumentation() {
        return _documentation;
    }

    /**
     * Common to all Command Objects. Set the documentation for the current
     * command.
     * 
     * @param documentation
     */
    public void setDocumentation(CommandDocumentation documentation) {
        _documentation = documentation;
    }

    /**
     * Common to all Command Objects. Get the version
     * 
     * @return
     */
    public String getVersion() {
        return _version;
    }

    /**
     * Common to all Command Objects. Set the Version
     * 
     * @param version
     */
    public void setVersion(String version) {
        _version = version;
    }

    /**
     * Common to all Command Objects. This function retrieves the filename
     * of where the command is stored
     * 
     * @return String
     */
    public String getFileName() {
        return _fileName;
    }

    /**
     * Returns the file where this command was loaded from. This will include
     * the full pathname of the command file.  Note that this method may return
     * <code>null</code> if this object has been serialized.
     * 
     * @return
     */
    public File getFile() {
        return _file;
    }

    /**
     * Common to all Command Objects. Set the filename for the current
     * command.
     * 
     * @param documentation
     */
    public void setFile(File file) {
        _file = file;
        _fileName = file.getName();
    }

    /**
     * Common to all Command Objects. Returns boolean to indicate the value of
     * the insecure option
     * 
     * @return boolean
     */
    public SecurityLevel getSecurityLevel() {
        return _security;
    }

    /**
     * Common to all Command Objects. Sets the insecure boolean property
     * 
     * @param insecure
     */
    public void setSecurityLevel(SecurityLevel security) {
        _security = security;
    }

    /**
     * Common to all Command Objects. Returns a boolean, True if the Command is
     * marked as readOnly or false if is not readOnly.
     * 
     * @return
     */
    public boolean isReadOnly() {
        return _readOnly;
    }

    /**
     * Common to all Command Objects. Sets the readOnly property
     * 
     * @param readOnly
     */
    public void setReadOnly(boolean readOnly) {
        _readOnly = readOnly;
    }
    
    /**
     * Returns the transaction type of this command
     * @return
     */
    public TransactionType getTransactionType() {
        return _transactionType;
    }
    
    public void setTransactionType(TransactionType type) {
        _transactionType = type;
    }
    
    synchronized
    protected MocaType[] getArgTypes() {
        if (_argTypes == null) {
            // Make corresponding arrays, as needed by the component adapter
            _argTypes = new MocaType[_args.size()];
            
            // Loop through the defined arguments, filling in our types array
            int i = 0;
            for (ArgumentInfo arg: _args) {
                _argTypes[i] = arg.getDatatype().getMocaType();
                i++;
            }
        }
        return _argTypes;
    }
    
    protected Object[] getArgs(ServerContext ctx) throws MocaException {
        
        // This command has a list of defined arguments.
        MocaType[] argTypes = getArgTypes();
        Object[] args = new Object[_args.size()];
        
        // Loop through the defined arguments, filling in our argument list
        int i = 0;
        for (ArgumentInfo arg: _args) {
            MocaValue value = ctx.getVariable(arg.getName(), arg.getAlias(), true);

            if ((value == null || value.getValue() == null) && arg.getDefaultValue() != null) {
                value = new MocaValue(MocaType.STRING, arg.getDefaultValue());
            }
            
            if ((value == null || value.getValue() == null) && arg.isRequired()) {
                throw new RequiredArgumentException(getName(), arg.getName());
            }
            
            args[i] = ServerUtils.copyArg(value, argTypes[i]);
            i++;
        }
        
        return args;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Command)) {
            return false;
        }
        
        Command other = (Command) obj;
        
        if (!_level.equals(other._level)) {
            return false;
        }

        if (!_name.equals(other._name)) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        return _level.hashCode() * 37 + _name.hashCode();
    }
    
    // @see java.lang.Object#toString()
    
    @Override
    public String toString() {
        return _level.getName() + "/" + _name;
    }

    //
    // Implementation
    //
    private static final long serialVersionUID = 20110821L;
    private String _name;
    private String _description;
    private transient File _file;
    private String _fileName;
    private ComponentLevel _level;
    private transient CommandDocumentation _documentation;
    private String _version;
    private SecurityLevel _security;
    private boolean _readOnly;
    private TransactionType _transactionType;
    private List<ArgumentInfo> _args;
    private transient MocaType[] _argTypes;
}
