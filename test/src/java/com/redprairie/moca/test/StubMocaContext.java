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

package com.redprairie.moca.test;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.TransactionHook;

/**
 * A testing class that stubs out the MOCA Context Only implements the
 * isVariableAvailable and getVariable methods. The Map object passed in
 * contains MOCA variables. Subclasses of this class can implement needed
 * methods to make this into a true mock object.
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public class StubMocaContext {

    public StubMocaContext() {
        this(new HashMap<String, Object>());
    }
    /**
     * 
     */
    public StubMocaContext(Map<String, Object> hashValues) {
        _hashValues = hashValues;
    }

    public void addTransactionHook(TransactionHook hook) {
    }

    public void commit() throws MocaException {
    }

    public MocaResults executeCommand(String command) throws MocaException {
        return null;
    }

    public MocaResults executeCommand(String command, Map<String, Object> args)
            throws MocaException {
        return null;
    }

    public MocaResults executeInline(String command) throws MocaException {

        if (command.startsWith("[SELECT nvl(U_VERSION, 0)")) {
            EditableResults results = new SimpleResults();
            results.addColumn("u_version", MocaType.INTEGER);
            results.addRow();
            results.setIntValue(0, 2);

            return results;
        }

        return null;
    }

    public MocaResults executeInline(String command, Map<String, Object> args)
            throws MocaException {
        return null;
    }

    public Connection getConnection() {
        return null;
    }
    
    public String getDbType() {
        return null;
    }

    public String getRegistryValue(String key) {
        return null;
    }

    public String getRegistryValue(String key, boolean expand) {
        return null;
    }

    public String getSystemVariable(String name) {
        return null;
    }
    
    public void putSystemVariable(String name, String value) {
        return;
    }
    
    public void removeSystemVariable(String name) {
        return;
    }
    
    public Object getTransactionAttribute(String name) {
        return null;
    }

    public Object getVariable(String name) {
        if (isVariableAvailable(name)) return _hashValues.get(name);

        return null;
    }

    public boolean isVariableAvailable(String name) {
        return _hashValues.containsKey(name);
    }
    
    public MocaValue getStackVariable(String name) {
        if (isVariableAvailable(name)) {
            Object value = _hashValues.get(name);
            return new MocaValue(MocaType.forValue(value), value);
        }
        else {
            return null;
        }
    }

    public void logDebug(String text) {
    }

    public void logError(String text) {
    }

    public void logInfo(String text) {
    }

    public void logUpdate(String text) {
    }

    public void logWarning(String text) {
    }

    public EditableResults newResults() {
        return new SimpleResults();
    }

    public void removeTransactionAttribute(String name) {
    }

    public void rollback() throws MocaException {
    }

    public void setTraceFile(String filename) {
    }

    public void setTraceFile(String filename, boolean append) {
    }

    public void setTraceLevel(int level) {
    }

    public void setTraceLevel(String level) {
    }

    public void setTransactionAttribute(String name, Object value) {
    }
    
    public Object getSessionAttribute(String name) {
        return null;
    }

    public void removeSessionAttribute(String name) {
    }

    public void setSessionAttribute(String name, Object value) {
    }
    
    public void trace(int level, String text) {
    }

    public void trace(String text) {
    }

    public boolean traceEnabled(int level) {
        return false;
    }
    
    public MocaArgument[] getArgs() {
        return new MocaArgument[0];
    }
    
    public MocaArgument[] getArgs(boolean getUsed) {
        return new MocaArgument[0];
    }
    
    public MocaResults getLastResults(int level) {
        // TODO Auto-generated method stub
        return null;
    }
    
    //
    // Implementation
    //
    private Map<String, Object> _hashValues;

    // @see com.redprairie.moca.MocaContext#executeCommand(java.lang.String, com.redprairie.moca.MocaArgument[])
    
    public MocaResults executeCommand(String command, MocaArgument... args)
            throws MocaException {
        // TODO Auto-generated method stub
        return null;
    }
    // @see com.redprairie.moca.MocaContext#executeInline(java.lang.String, com.redprairie.moca.MocaArgument[])
    
    public MocaResults executeInline(String command, MocaArgument... args)
            throws MocaException {
        // TODO Auto-generated method stub
        return null;
    }
}