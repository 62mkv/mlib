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

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ForwardingObject;
import com.sam.moca.EditableResults;
import com.sam.moca.MocaArgument;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaOperator;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaValue;
import com.sam.moca.RowIterator;
import com.sam.moca.server.CommandInterceptor;
import com.sam.moca.server.db.BindList;
import com.sam.moca.server.db.DBType;
import com.sam.moca.server.dispatch.MessageResolver;
import com.sam.moca.server.exec.CommandNotFoundException;
import com.sam.moca.server.exec.CompiledScript;
import com.sam.moca.server.exec.ExecutableComponent;
import com.sam.moca.server.exec.RequestContext;
import com.sam.moca.server.exec.ServerContext;
import com.sam.moca.server.exec.ServerContextStatus;
import com.sam.moca.server.exec.SessionContext;
import com.sam.moca.server.exec.StackLevel;
import com.sam.moca.server.exec.TransactionManager;
import com.sam.moca.server.legacy.NativeLibraryAdapter;
import com.sam.moca.server.profile.CommandPath;
import com.sam.moca.server.repository.Command;
import com.sam.moca.server.repository.Trigger;

/**
 * This is a simple forwarding object that encapsulates a server context
 * allowing for easier extensibility
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class ForwardingServerContext extends ForwardingObject implements
        ServerContext {
    
    public ForwardingServerContext(ServerContext context) {
        _context = context;
    }

    // @see com.sam.moca.server.exec.ArgumentSource#getVariable(java.lang.String, boolean)
    @Override
    public MocaValue getVariable(String name, boolean markUsed) {
        return delegate().getVariable(name, markUsed);
    }

    // @see com.sam.moca.server.exec.ArgumentSource#getSystemVariable(java.lang.String)
    @Override
    public String getSystemVariable(String name) {
        return delegate().getSystemVariable(name);
    }

    // @see com.sam.moca.server.exec.ArgumentSource#getVariableAsArgument(java.lang.String, boolean, boolean)
    @Override
    public MocaArgument getVariableAsArgument(String name, boolean markUsed,
        boolean equalsOnly) {
        return delegate().getVariableAsArgument(name, markUsed, equalsOnly);
    }

    // @see com.sam.moca.server.exec.ArgumentSource#isVariableAvailable(java.lang.String)
    @Override
    public boolean isVariableAvailable(String name) {
        return delegate().isVariableAvailable(name);
    }

    // @see com.sam.moca.server.exec.ArgumentSource#getCommandArgs(boolean, boolean)
    @Override
    public MocaArgument[] getCommandArgs(boolean getAll, boolean useLowLevel) {
        return delegate().getCommandArgs(getAll, useLowLevel);
    }

    // @see com.sam.moca.server.exec.ServerContext#getComponentContext()
    @Override
    public MocaContext getComponentContext() {
        return delegate().getComponentContext();
    }

    // @see com.sam.moca.server.exec.ServerContext#getTransactionManager()
    @Override
    public TransactionManager getTransactionManager() {
        return delegate().getTransactionManager();
    }

    // @see com.sam.moca.server.exec.ServerContext#newResults()
    @Override
    public EditableResults newResults() {
        return delegate().newResults();
    }

    // @see com.sam.moca.server.exec.ServerContext#popStack(boolean)
    @Override
    public void popStack(boolean keepErrorState) {
        delegate().popStack(keepErrorState);
    }

    // @see com.sam.moca.server.exec.ServerContext#pushStack()
    @Override
    public void pushStack() {
        delegate().pushStack();
    }

    // @see com.sam.moca.server.exec.ServerContext#setColumns(com.sam.moca.MocaResults)
    @Override
    public void setColumns(MocaResults res) {
        delegate().setColumns(res);
    }

    // @see com.sam.moca.server.exec.ServerContext#setRow(com.sam.moca.RowIterator)
    @Override
    public void setRow(RowIterator row) {
        delegate().setRow(row);
    }

    // @see com.sam.moca.server.exec.ServerContext#setCommand(com.sam.moca.server.exec.ExecutableComponent)
    @Override
    public void setCommand(ExecutableComponent command) {
        delegate().setCommand(command);
    }

    // @see com.sam.moca.server.exec.ServerContext#clearArgs()
    @Override
    public void clearArgs() {
        delegate().clearArgs();
    }

    // @see com.sam.moca.server.exec.ServerContext#addArg(java.lang.String, com.sam.moca.MocaOperator, com.sam.moca.MocaValue)
    @Override
    public void addArg(String name, MocaOperator oper, MocaValue value) {
        delegate().addArg(name, oper, value);
    }

    // @see com.sam.moca.server.exec.ServerContext#getLastError()
    @Override
    public MocaException getLastError() {
        return delegate().getLastError();
    }

    // @see com.sam.moca.server.exec.ServerContext#setError(com.sam.moca.MocaException)
    @Override
    public void setError(MocaException e) {
        delegate().setError(e);
    }

    // @see com.sam.moca.server.exec.ServerContext#fixErrorState()
    @Override
    public void fixErrorState() {
        delegate().fixErrorState();
    }

    // @see com.sam.moca.server.exec.ServerContext#getStackLevel()
    @Override
    public StackLevel getStackLevel() {
        return delegate().getStackLevel();
    }

    // @see com.sam.moca.server.exec.ServerContext#getVariable(java.lang.String, java.lang.String, boolean)
    @Override
    public MocaValue getVariable(String name, String alias, boolean markUsed) {
        return delegate().getVariable(name, alias, markUsed);
    }

    // @see com.sam.moca.server.exec.ServerContext#putSystemVariable(java.lang.String, java.lang.String)
    @Override
    public void putSystemVariable(String name, String value) {
        delegate().putSystemVariable(name, value);
    }

    // @see com.sam.moca.server.exec.ServerContext#removeSystemVariable(java.lang.String)
    @Override
    public void removeSystemVariable(String name) {
        delegate().removeSystemVariable(name);
    }

    // @see com.sam.moca.server.exec.ServerContext#getVariableAsArgument(java.lang.String, java.lang.String, boolean, boolean)
    @Override
    public MocaArgument getVariableAsArgument(String name, String alias,
        boolean markUsed, boolean equalsOnly) {
        return delegate().getVariableAsArgument(name, alias, markUsed, equalsOnly);
    }

    // @see com.sam.moca.server.exec.ServerContext#getRegistryValue(java.lang.String, boolean)
    @Override
    public String getRegistryValue(String key, boolean expand) {
        return delegate().getRegistryValue(key, expand);
    }

    // @see com.sam.moca.server.exec.ServerContext#removeTransactionAttribute(java.lang.String)
    @Override
    public void removeTransactionAttribute(String name) {
        delegate().removeTransactionAttribute(name);
    }

    // @see com.sam.moca.server.exec.ServerContext#getTransactionAttribute(java.lang.String)
    @Override
    public Object getTransactionAttribute(String name) {
        return delegate().getTransactionAttribute(name);
    }

    // @see com.sam.moca.server.exec.ServerContext#setTransactionAttribute(java.lang.String, java.lang.Object)
    @Override
    public void setTransactionAttribute(String name, Object value) {
        delegate().setTransactionAttribute(name, value);
    }

    // @see com.sam.moca.server.exec.ServerContext#compileScript(java.lang.String, java.lang.String)
    @Override
    public CompiledScript compileScript(String script, String language) throws MocaException {
        return delegate().compileScript(script, language);
    }

    // @see com.sam.moca.server.exec.ServerContext#executeScript(java.lang.String, java.lang.String)
    @Override
    public MocaResults executeScript(CompiledScript script)
            throws MocaException {
        return delegate().executeScript(script);
    }

    // @see com.sam.moca.server.exec.ServerContext#evaluateScript(java.lang.String, java.lang.String)
    @Override
    public Object evaluateScript(CompiledScript script)
            throws MocaException {
        return delegate().evaluateScript(script);
    }

    // @see com.sam.moca.server.exec.ServerContext#executeSQLWithVars(java.lang.String)
    @Override
    public MocaResults executeSQLWithVars(String sql) throws MocaException {
        return delegate().executeSQLWithVars(sql);
    }
    
    // @see com.sam.moca.server.exec.ServerContext#executeSQLWithVars(java.lang.String, java.lang.String)
    @Override
    public MocaResults executeSQLWithVars(String sql, String profileHint)
            throws MocaException {
        return delegate().executeSQLWithVars(sql, profileHint);
    }

    // @see com.sam.moca.server.exec.ServerContext#executeNamedCommand(java.lang.String, boolean)
    @Override
    public MocaResults executeNamedCommand(String verbNounClause,
        boolean override) throws MocaException {
        return delegate().executeNamedCommand(verbNounClause, override);
    }

    // @see com.sam.moca.server.exec.ServerContext#executeCommand(java.lang.String, java.util.Map, boolean)
    @Override
    public MocaResults executeCommand(String command, Map<String, ?> args,
        boolean keepContext) throws MocaException {
        return delegate().executeCommand(command, args, keepContext);
    }

    // @see com.sam.moca.server.exec.ServerContext#executeCommandWithRemoteContext(java.lang.String, java.util.Collection, java.util.Collection)
    @Override
    public MocaResults executeCommandWithRemoteContext(String command,
        Collection<MocaArgument> context, Collection<MocaArgument> args)
            throws MocaException {
        return delegate().executeCommandWithRemoteContext(command, context, args);
    }

    // @see com.sam.moca.server.exec.ServerContext#executeRemote(java.lang.String, java.lang.String)
    @Override
    public MocaResults executeRemote(String remoteHost, String commandText)
            throws MocaException {
        return delegate().executeRemote(remoteHost, commandText);
    }

    // @see com.sam.moca.server.exec.ServerContext#executeParallel(java.lang.String, java.lang.String, boolean)
    @Override
    public MocaResults executeParallel(String remoteHost, String commandText,
        boolean inTransaction) throws MocaException {
        return delegate().executeParallel(remoteHost, commandText, inTransaction);
    }

    // @see com.sam.moca.server.exec.ServerContext#commit()
    @Override
    public void commit() throws MocaException {
        delegate().commit();
    }

    // @see com.sam.moca.server.exec.ServerContext#rollback()
    @Override
    public void rollback() throws MocaException {
        delegate().rollback();
    }

    // @see com.sam.moca.server.exec.ServerContext#getDbType()
    @Override
    public DBType getDbType() {
        return delegate().getDbType();
    }

    // @see com.sam.moca.server.exec.ServerContext#getNextSequenceValue(java.lang.String)
    @Override
    public String getNextSequenceValue(String name) throws MocaException {
        return delegate().getNextSequenceValue(name);
    }

    // @see com.sam.moca.server.exec.ServerContext#getNativeLibraryAdapter()
    @Override
    public NativeLibraryAdapter getNativeLibraryAdapter() throws MocaException {
        return delegate().getNativeLibraryAdapter();
    }

    // @see com.sam.moca.server.exec.ServerContext#executeSQL(java.lang.String, com.sam.moca.server.db.BindList, boolean, boolean)
    @Override
    public MocaResults executeSQL(String sql, BindList args, boolean autoBind,
        boolean ignoreResults) throws SQLException, MocaException {
        return delegate().executeSQL(sql, args, autoBind, ignoreResults);
    }

    // @see com.sam.moca.server.exec.ServerContext#commitDB()
    @Override
    public void commitDB() throws SQLException {
        delegate().commitDB();
    }

    // @see com.sam.moca.server.exec.ServerContext#rollbackDB()
    @Override
    public void rollbackDB() throws SQLException {
        delegate().rollbackDB();
    }

    // @see com.sam.moca.server.exec.ServerContext#setSavepoint(java.lang.String)
    @Override
    public void setSavepoint(String savepoint) throws SQLException {
        delegate().setSavepoint(savepoint);
    }

    // @see com.sam.moca.server.exec.ServerContext#rollbackDB(java.lang.String)
    @Override
    public void rollbackDB(String savepoint) throws SQLException {
        delegate().rollbackDB(savepoint);
    }

    // @see com.sam.moca.server.exec.ServerContext#close()
    @Override
    public void close() {
        delegate().close();
    }

    // @see com.sam.moca.server.exec.ServerContext#setCurrentStatus(com.sam.moca.server.exec.ServerContextStatus)
    @Override
    public void setCurrentStatus(ServerContextStatus status) {
        delegate().setCurrentStatus(status);
    }

    // @see com.sam.moca.server.exec.ServerContext#getCurrentStatus()
    @Override
    public ServerContextStatus getCurrentStatus() {
        return delegate().getCurrentStatus();
    }

    // @see com.sam.moca.server.exec.ServerContext#hasKeepalive()
    @Override
    public boolean hasKeepalive() {
        return delegate().hasKeepalive();
    }

    // @see com.sam.moca.server.exec.ServerContext#clearError()
    @Override
    public void clearError() {
        delegate().clearError();
    }

    // @see com.sam.moca.server.exec.ServerContext#getLastStatementInitiated()
    @Override
    public String getLastStatementInitiated() {
        return delegate().getLastStatementInitiated();
    }

    // @see com.sam.moca.server.exec.ServerContext#getSession()
    @Override
    public SessionContext getSession() {
        return delegate().getSession();
    }

    // @see com.sam.moca.server.exec.ServerContext#getMessageResolver()
    @Override
    public MessageResolver getMessageResolver() {
        return delegate().getMessageResolver();
    }

    // @see com.sam.moca.server.exec.ServerContext#currentCommandPath()
    @Override
    public CommandPath currentCommandPath() {
        return delegate().currentCommandPath();
    }

    // @see com.sam.moca.server.exec.ServerContext#executeDefinedCommand(com.sam.moca.server.repository.Command, java.util.List)
    @Override
    public MocaResults executeDefinedCommand(Command commandDef,
        List<Trigger> triggers) throws MocaException {
        return delegate().executeDefinedCommand(commandDef, triggers);
    }

    // @see com.sam.moca.server.exec.ServerContext#lookupNamedCommand(java.lang.String, boolean)
    @Override
    public ExecutableComponent lookupNamedCommand(String verbNounClause,
        boolean override) throws CommandNotFoundException {
        return delegate().lookupNamedCommand(verbNounClause, override);
    }

    // @see com.sam.moca.server.exec.ServerContext#hasTransaction()
    @Override
    public boolean hasTransaction() {
        return delegate().hasTransaction();
    }

    // @see com.sam.moca.server.exec.ServerContext#logDebug(java.lang.Object)
    @Override
    public void logDebug(Object msg) {
        delegate().logDebug(msg);
    }

    // @see com.sam.moca.server.exec.ServerContext#getRequest()
    @Override
    public RequestContext getRequest() {
        return delegate().getRequest();
    }
    
    // @see com.sam.moca.server.exec.ServerContext#overrideCommand(java.lang.String, com.sam.moca.server.CommandInterceptor)
    @Override
    public void overrideCommand(String commandName,
        CommandInterceptor interceptor) {
        delegate().overrideCommand(commandName, interceptor);
    }

    // @see com.sam.moca.server.exec.ServerContext#clearOverriddenCommands()
    @Override
    public void clearOverriddenCommands() {
        delegate().clearOverriddenCommands();
    }

    // @see com.google.common.collect.ForwardingObject#delegate()
    @Override
    public ServerContext delegate() {
        return _context;
    }

    private final ServerContext _context;
}
