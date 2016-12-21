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

package com.sam.moca.advice;

import java.sql.Connection;
import java.util.Map;

import javax.transaction.xa.XAResource;

import com.google.common.collect.ForwardingObject;
import com.sam.moca.DatabaseTool;
import com.sam.moca.EditableResults;
import com.sam.moca.MocaArgument;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaValue;
import com.sam.moca.TransactionHook;
import com.sam.moca.server.exec.DefaultServerContext;
import com.sam.moca.server.exec.DefaultServerContext.DefaultServerContextAware;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class ForwardingMocaContext extends ForwardingObject implements
        MocaContext, DefaultServerContextAware {
    
    public ForwardingMocaContext(MocaContext context) {
        _context = context;
    }

    // @see com.sam.moca.MocaContext#getConnection()
    @Override
    public Connection getConnection() {
        return delegate().getConnection();
    }

    // @see com.sam.moca.MocaContext#getDbType()
    @Override
    public String getDbType() {
        return delegate().getDbType();
    }

    // @see com.sam.moca.MocaContext#getVariable(java.lang.String)
    @SuppressWarnings("deprecation")
    @Override
    public Object getVariable(String name) {
        return delegate().getVariable(name);
    }

    // @see com.sam.moca.MocaContext#isVariableAvailable(java.lang.String)
    @SuppressWarnings("deprecation")
    @Override
    public boolean isVariableAvailable(String name) {
        return delegate().isVariableAvailable(name);
    }

    // @see com.sam.moca.MocaContext#getStackVariable(java.lang.String)
    @Override
    public MocaValue getStackVariable(String name) {
        return delegate().getStackVariable(name);
    }
    
    // @see com.sam.moca.MocaContext#getStackVariable(java.lang.String, boolean)
    @Override
    public MocaValue getStackVariable(String name, boolean markAsUsed) {
        return delegate().getStackVariable(name, markAsUsed);
    }

    // @see com.sam.moca.MocaContext#getSystemVariable(java.lang.String)
    @Override
    public String getSystemVariable(String name) {
        return delegate().getSystemVariable(name);
    }

    // @see com.sam.moca.MocaContext#putSystemVariable(java.lang.String, java.lang.String)
    @Override
    public void putSystemVariable(String name, String value) {
        delegate().putSystemVariable(name, value);
    }

    // @see com.sam.moca.MocaContext#removeSystemVariable(java.lang.String)
    @Override
    public void removeSystemVariable(String name) {
        delegate().removeSystemVariable(name);
    }

    // @see com.sam.moca.MocaContext#getRegistryValue(java.lang.String)
    @Override
    public String getRegistryValue(String key) {
        return delegate().getRegistryValue(key);
    }

    // @see com.sam.moca.MocaContext#getRegistryValue(java.lang.String, boolean)
    @Override
    public String getRegistryValue(String key, boolean expand) {
        return delegate().getRegistryValue(key, expand);
    }

    // @see com.sam.moca.MocaContext#executeCommand(java.lang.String)
    @Override
    public MocaResults executeCommand(String command) throws MocaException {
        return delegate().executeCommand(command);
    }

    // @see com.sam.moca.MocaContext#executeInline(java.lang.String)
    @Override
    public MocaResults executeInline(String command) throws MocaException {
        return delegate().executeInline(command);
    }

    // @see com.sam.moca.MocaContext#executeCommand(java.lang.String, java.util.Map)
    @Override
    public MocaResults executeCommand(String command, Map<String, ?> args)
            throws MocaException {
        return delegate().executeCommand(command, args);
    }

    // @see com.sam.moca.MocaContext#executeInline(java.lang.String, java.util.Map)
    @Override
    public MocaResults executeInline(String command, Map<String, ?> args)
            throws MocaException {
        return delegate().executeInline(command, args);
    }

    // @see com.sam.moca.MocaContext#executeCommand(java.lang.String, com.sam.moca.MocaArgument[])
    @Override
    public MocaResults executeCommand(String command, MocaArgument... args)
            throws MocaException {
        return delegate().executeCommand(command, args);
    }

    // @see com.sam.moca.MocaContext#executeInline(java.lang.String, com.sam.moca.MocaArgument[])
    @Override
    public MocaResults executeInline(String command, MocaArgument... args)
            throws MocaException {
        return delegate().executeInline(command, args);
    }

    // @see com.sam.moca.MocaContext#logError(java.lang.String)
    @Override
    @Deprecated
    public void logError(String text) {
        delegate().logError(text);
    }

    // @see com.sam.moca.MocaContext#logWarning(java.lang.String)
    @Override
    @Deprecated
    public void logWarning(String text) {
        delegate().logWarning(text);
    }

    // @see com.sam.moca.MocaContext#logInfo(java.lang.String)
    @Override
    @Deprecated
    public void logInfo(String text) {
        delegate().logInfo(text);
    }

    // @see com.sam.moca.MocaContext#logDebug(java.lang.String)
    @Override
    @Deprecated
    public void logDebug(String text) {
        delegate().logDebug(text);
    }

    // @see com.sam.moca.MocaContext#logUpdate(java.lang.String)
    @Override
    @Deprecated
    public void logUpdate(String text) {
        delegate().logUpdate(text);
    }

    // @see com.sam.moca.MocaContext#trace(int, java.lang.String)
    @Override
    public void trace(int level, String text) {
        delegate().trace(level, text);
    }

    // @see com.sam.moca.MocaContext#trace(java.lang.String)
    @Override
    public void trace(String text) {
        delegate().trace(text);
    }

    // @see com.sam.moca.MocaContext#traceEnabled(int)
    @Override
    public boolean traceEnabled(int level) {
        return delegate().traceEnabled(level);
    }

    // @see com.sam.moca.MocaContext#setTraceLevel(int)
    @Override
    public void setTraceLevel(int level) {
        delegate().setTraceLevel(level);
    }

    // @see com.sam.moca.MocaContext#setTraceLevel(java.lang.String)
    @Override
    public void setTraceLevel(String level) {
        delegate().setTraceLevel(level);
    }

    // @see com.sam.moca.MocaContext#setTraceFile(java.lang.String)
    @Override
    public void setTraceFile(String filename) {
        delegate().setTraceFile(filename);
    }

    // @see com.sam.moca.MocaContext#setTraceFile(java.lang.String, boolean)
    @Override
    public void setTraceFile(String filename, boolean append) {
        delegate().setTraceFile(filename, append);
    }

    // @see com.sam.moca.MocaContext#removeTransactionAttribute(java.lang.String)
    @Override
    public void removeTransactionAttribute(String name) {
        delegate().removeTransactionAttribute(name);
    }

    // @see com.sam.moca.MocaContext#getTransactionAttribute(java.lang.String)
    @Override
    public Object getTransactionAttribute(String name) {
        return delegate().getTransactionAttribute(name);
    }

    // @see com.sam.moca.MocaContext#setTransactionAttribute(java.lang.String, java.lang.Object)
    @Override
    public void setTransactionAttribute(String name, Object value) {
        delegate().setTransactionAttribute(name, value);
    }

    // @see com.sam.moca.MocaContext#removeSessionAttribute(java.lang.String)
    @Override
    public void removeSessionAttribute(String name) {
        delegate().removeSessionAttribute(name);
    }

    // @see com.sam.moca.MocaContext#getSessionAttribute(java.lang.String)
    @Override
    public Object getSessionAttribute(String name) {
        return delegate().getSessionAttribute(name);
    }

    // @see com.sam.moca.MocaContext#setSessionAttribute(java.lang.String, java.lang.Object)
    @Override
    public void setSessionAttribute(String name, Object value) {
        delegate().setSessionAttribute(name, value);
    }

    // @see com.sam.moca.MocaContext#removeRequestAttribute(java.lang.String)
    @Override
    public void removeRequestAttribute(String name) {
        delegate().removeRequestAttribute(name);
    }

    // @see com.sam.moca.MocaContext#getRequestAttribute(java.lang.String)
    @Override
    public Object getRequestAttribute(String name) {
        return delegate().getRequestAttribute(name);
    }

    // @see com.sam.moca.MocaContext#setRequestAttribute(java.lang.String, java.lang.Object)
    @Override
    public void setRequestAttribute(String name, Object value) {
        delegate().setRequestAttribute(name, value);
    }

    // @see com.sam.moca.MocaContext#addTransactionHook(com.sam.moca.TransactionHook)
    @Override
    public void addTransactionHook(TransactionHook hook) {
        delegate().addTransactionHook(hook);
    }

    // @see com.sam.moca.MocaContext#commit()
    @Override
    public void commit() throws MocaException {
        delegate().commit();
    }

    // @see com.sam.moca.MocaContext#rollback()
    @Override
    public void rollback() throws MocaException {
        delegate().rollback();
    }

    // @see com.sam.moca.MocaContext#newResults()
    @Override
    public EditableResults newResults() {
        return delegate().newResults();
    }

    // @see com.sam.moca.MocaContext#getArgs()
    @Override
    public MocaArgument[] getArgs() {
        return delegate().getArgs();
    }

    // @see com.sam.moca.MocaContext#getArgs(boolean)
    @Override
    public MocaArgument[] getArgs(boolean getUsed) {
        return delegate().getArgs(getUsed);
    }

    // @see com.sam.moca.MocaContext#getLastResults(int)
    @Override
    public MocaResults getLastResults(int level) {
        return delegate().getLastResults(level);
    }

    // @see com.sam.moca.MocaContext#executeSQL(java.lang.String, java.util.Map)
    @Override
    public MocaResults executeSQL(String sql, Map<String, ?> args)
            throws MocaException {
        return delegate().executeSQL(sql, args);
    }

    // @see com.sam.moca.MocaContext#executeSQL(java.lang.String, com.sam.moca.MocaArgument[])
    @Override
    public MocaResults executeSQL(String sql, MocaArgument... args)
            throws MocaException {
        return delegate().executeSQL(sql, args);
    }

    // @see com.sam.moca.MocaContext#getDb()
    @Override
    public DatabaseTool getDb() {
        return delegate().getDb();
    }
    

    // @see com.sam.moca.server.exec.DefaultServerContext.DefaultServerContextAware#getDefaultServerContext()
    @Override
    public DefaultServerContext getDefaultServerContext() {
        MocaContext delegate = delegate();
        if (delegate instanceof DefaultServerContextAware) {
            return ((DefaultServerContextAware)delegate).getDefaultServerContext();
        }
        return null;
    }
    
    // @see com.sam.moca.MocaContext#enlistResource(javax.transaction.xa.XAResource)
    @Override
    public void enlistResource(XAResource resource) throws MocaException {
        delegate().enlistResource(resource);
    }

    // @see com.google.common.collect.ForwardingObject#delegate()
    @Override
    protected MocaContext delegate() {
        return _context;
    }

    protected final MocaContext _context;
}
