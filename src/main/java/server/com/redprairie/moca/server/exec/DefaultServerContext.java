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

package com.redprairie.moca.server.exec;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.transaction.xa.XAResource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.redprairie.moca.DatabaseTool;
import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaConstants;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaOperator;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaTrace;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.TransactionHook;
import com.redprairie.moca.client.MocaConnection;
import com.redprairie.moca.client.ServerSideConnection;
import com.redprairie.moca.exceptions.AuthenticationException;
import com.redprairie.moca.exceptions.AuthorizationException;
import com.redprairie.moca.exceptions.MocaDBException;
import com.redprairie.moca.exceptions.RemoteSessionClosedException;
import com.redprairie.moca.exceptions.SessionClosedException;
import com.redprairie.moca.exceptions.UnexpectedException;
import com.redprairie.moca.server.CommandInterceptor;
import com.redprairie.moca.server.SecurityLevel;
import com.redprairie.moca.server.TransactionManagerUtils;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.BindMode;
import com.redprairie.moca.server.db.DBAdapter;
import com.redprairie.moca.server.db.DBType;
import com.redprairie.moca.server.db.MocaTransaction;
import com.redprairie.moca.server.db.exceptions.MissingWhereClauseException;
import com.redprairie.moca.server.dispatch.MessageResolver;
import com.redprairie.moca.server.legacy.NativeAdapterFactory;
import com.redprairie.moca.server.legacy.NativeLibraryAdapter;
import com.redprairie.moca.server.log.TraceUtils;
import com.redprairie.moca.server.log.exceptions.LoggingException;
import com.redprairie.moca.server.log.render.SecureLogMessage;
import com.redprairie.moca.server.parse.MocaParseException;
import com.redprairie.moca.server.parse.MocaParser;
import com.redprairie.moca.server.profile.CommandPath;
import com.redprairie.moca.server.profile.CommandUsage;
import com.redprairie.moca.server.profile.CommandUsageStatistics;
import com.redprairie.moca.server.profile.NullCommandUsage;
import com.redprairie.moca.server.repository.ArgType;
import com.redprairie.moca.server.repository.ArgumentInfo;
import com.redprairie.moca.server.repository.CFunctionCommand;
import com.redprairie.moca.server.repository.Command;
import com.redprairie.moca.server.repository.CommandArgumentFilter;
import com.redprairie.moca.server.repository.CommandFilter;
import com.redprairie.moca.server.repository.CommandRepository;
import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.ComponentLibraryFilter;
import com.redprairie.moca.server.repository.JavaCommand;
import com.redprairie.moca.server.repository.LocalSyntaxCommand;
import com.redprairie.moca.server.repository.TransactionType;
import com.redprairie.moca.server.repository.Trigger;
import com.redprairie.moca.server.repository.TriggerFilter;
import com.redprairie.moca.server.session.MocaSessionUtils;
import com.redprairie.moca.server.session.SessionToken;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.util.ArgCheck;
import com.redprairie.util.SQLArgReplacer;

/**
 * Primary implementation of ServerContext.
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class DefaultServerContext implements ServerContext, JavaVariableContext {

    /**
     * This interface allows us to not be aware of the implementation
     */
    public interface DefaultServerContextAware {
        public DefaultServerContext getDefaultServerContext();
    }
    
    /**
     * An instance of <code>MocaContext</code> that is handed off to executing
     * components.
     */
    private class ComponentContext implements MocaContext, DefaultServerContextAware {
        
        public ComponentContext() {
            _dbTool = new MocaDatabaseTool(_transactionManager);
        }
        
        public DefaultServerContext getDefaultServerContext() {
            return DefaultServerContext.this;
        }
        // javadoc inherited from interface
        public MocaResults executeCommand(String command) throws MocaException {
            return _executeTranslate(command, null, false);
        }

        // javadoc inherited from interface
        public MocaResults executeInline(String command) throws MocaException {
            return _executeTranslate(command, null, true);
        }

        // @see com.redprairie.moca.MocaContext#executeCommand(java.lang.String,
        // com.redprairie.moca.db.BindList, boolean)
        public MocaResults executeCommand(String command, Map<String, ?> args)
                throws MocaException {
            return _executeTranslate(command, valueMap(args), false);
        }

        // @see com.redprairie.moca.MocaContext#executeCommand(java.lang.String,
        // com.redprairie.moca.db.BindList, boolean)
        public MocaResults executeInline(String command, Map<String, ?> args)
                throws MocaException {
            return _executeTranslate(command, valueMap(args), true);
        }
        
        // javadoc inherited from interface
        public Object getVariable(String name) {
            MocaValue value = _getVariable(name, null, true);

            // If it's null, just return now.
            if (value == null) return null;
            
            // Otherwise, just return the value
            return value.getValue();
        }

        // javadoc inherited from interface
        public MocaValue getStackVariable(String name) {
            return _getVariable(name, null, true);
        }
        
        // @see com.redprairie.moca.MocaContext#getStackVariable(java.lang.String, boolean)
        @Override
        public MocaValue getStackVariable(String name, boolean markAsUsed) {
            return _getVariable(name, null, markAsUsed);
        }

        public boolean isVariableAvailable(String name) {
            return _isVariableMapped(name);
        }

        // javadoc inherited from interface
        public String getSystemVariable(String name) {
            return _getSystemVariable(name);
        }        

        // javadoc inherited from interface
        public void putSystemVariable(String name, String value) {
            DefaultServerContext.this.putSystemVariable(name, value);
        }  
        
        // javadoc inherited from interface
        public void removeSystemVariable(String name) {
            DefaultServerContext.this.removeSystemVariable(name);
        }  
        
        // javadoc inherited from interface
        @Deprecated
        public void logError(String text) {
            _logger.error(text);
        }
        
        // javadoc inherited from interface
        @Deprecated
        public void logWarning(String text) {
            _logger.warn(text);
        }

        // javadoc inherited from interface
        @Deprecated
        public void logInfo(String text) {
            _logger.info(text);
        }

        // javadoc inherited from interface
        @Deprecated
        public void logDebug(String text) {
            _logger.debug(text);
        }

        // javadoc inherited from interface
        @Deprecated
        public void logUpdate(String text) {
            _logger.info(text);
        }

        /*
         * @see com.redprairie.moca.MocaContext#trace(java.lang.String)
         */
        @Override
        public void trace(String text) {
            _flowLogger.debug(text);
        }

        // javadoc inherited from interface
        // Note that all messages from here are now considered general messages
        public void trace(int level, String text) {
            switch (level) {
            case MocaTrace.MGR:
                _managerLogger.debug(text);
                break;
            case MocaTrace.PERF:
                _performanceLogger.debug(text);
                break;
            case MocaTrace.SERVER:
                _serverLogger.debug(text);
                break;
            case MocaTrace.SQL:
                _sqlLogger.debug(text);
                break;
            case MocaTrace.SRVARGS:
                _argumentLogger.debug(text);
                break;
            // We default to flow if it wasn't any of the other values
            default:
                _flowLogger.debug(text);
                break;
            }
        }

        public boolean traceEnabled(int level) {
            boolean enabled = false;
            try {
                enabled = TraceUtils.isSessionTraceLevelEnabled(level);
            }
            catch (LoggingException e) {
                e.printStackTrace();
            }
            
            // If the session trace isn't enabled then we also have to check
            // the global trace as well
            if (!enabled) {
                enabled = TraceUtils.isGlobalTraceLevelEnabled(level);
            }
            
            return enabled;
        }

        public void setTraceLevel(int level) {
            try {
                TraceUtils.setSessionTraceLevel(level);
            }
            catch (LoggingException e) {
                // We ignore the error and just let it go on
                e.printStackTrace();
            }
        }

        public void setTraceLevel(String level) {
            try {
                TraceUtils.setSessionTraceLevel(level);
            }
            catch (LoggingException e) {
                // We ignore the error and just let it go on
                e.printStackTrace();
            }
        }

        public void setTraceFile(String filename) {
            setTraceFile(filename, false);
        }

        public void setTraceFile(String filename, boolean append) {
            try {
                TraceUtils.enableSessionTracing(filename, append);
            }
            catch (LoggingException e) {
                // We ignore the error and just let it go on
                e.printStackTrace();
            }
        }

        public void commit() throws MocaException {
            DefaultServerContext.this.commit();
        }

        public void rollback() throws MocaException {
            DefaultServerContext.this.rollback();
        }

        public EditableResults newResults() {
            return new SimpleResults();
        }

        // @see
        // com.redprairie.moca.MocaContext#getRegistryValue(java.lang.String,
        // java.lang.String)
        public String getRegistryValue(String key) {
            return DefaultServerContext.this.getRegistryValue(key, true);
        }

        // @see
        // com.redprairie.moca.MocaContext#getRegistryValue(java.lang.String,
        // java.lang.String. java.lang.Boolean)
        public String getRegistryValue(String key, boolean expand) {
            return DefaultServerContext.this.getRegistryValue(key, expand);
        }
        
        // @see
        // com.redprairie.moca.MocaContext#getTransactionAttribute(java.lang.String)
        public Object getTransactionAttribute(String name) {
            return DefaultServerContext.this.getTransactionAttribute(name);
        }

        // @see
        // com.redprairie.moca.MocaContext#removeTransactionAttribute(java.lang.String)
        public void removeTransactionAttribute(String name) {
            DefaultServerContext.this.removeTransactionAttribute(name);
        }

        // @see
        // com.redprairie.moca.MocaContext#setTransactionAttribute(java.lang.String,
        // java.lang.Object)
        public void setTransactionAttribute(String name, Object value) {
            DefaultServerContext.this.setTransactionAttribute(name, value);
        }

        // javadoc inherited from interface
        public void addTransactionHook(TransactionHook hook) {
            _transactionManager.addTransactionHook(hook);
        }
        
        // @see com.redprairie.moca.MocaContext#setSessionAttribute(java.lang.String, java.lang.Object)
        @Override
        public void setSessionAttribute(String name, Object value) {
            _session.putAttribute(name, value);
        }
        
        // @see com.redprairie.moca.MocaContext#getSessionAttribute(java.lang.String)
        @Override
        public Object getSessionAttribute(String name) {
            return _session.getAttribute(name);
        }
        
        // @see com.redprairie.moca.MocaContext#removeSessionAttribute(java.lang.String)
        @Override
        public void removeSessionAttribute(String name) {
           _session.removeAttribute(name);
        }

        // @see com.redprairie.moca.MocaContext#setSessionAttribute(java.lang.String, java.lang.Object)
        @Override
        public void setRequestAttribute(String name, Object value) {
            _request.putAttribute(name, value);
        }
        
        // @see com.redprairie.moca.MocaContext#getSessionAttribute(java.lang.String)
        @Override
        public Object getRequestAttribute(String name) {
            return _request.getAttribute(name);
        }
        
        // @see com.redprairie.moca.MocaContext#removeSessionAttribute(java.lang.String)
        @Override
        public void removeRequestAttribute(String name) {
           _request.removeAttribute(name);
        }

        // @see com.redprairie.moca.MocaContext#getConnection()
        @Override
        public Connection getConnection() {
            // Check to make sure the status is okay before giving a connection.
            DefaultServerContext.this.checkStatus();
            return _transactionManager.getConnection();
        }
        
        @Override
        public String getDbType() {
            return String.valueOf(DefaultServerContext.this.getDbType());
        }

        @Override
        public MocaArgument[] getArgs() {
            return getArgs(false);
        }
        
        @Override
        public MocaArgument[] getArgs(boolean getUsed) {
            return getCommandArgs(getUsed, false);
        }

        @Override
        public MocaResults getLastResults(int level) {
            EditableResults retRes = null;
            
            if (level <= 0) {
                throw new IllegalArgumentException("The level must be 1 or " +
                                "greater");
            }
            
            try {
                _dataStackLock.lock();
                // We only want to process it if the data stack level contains
                // the desired level
                if (level <= _dataStackLevel) {
                    _DataStackElement stack = _dataStack[_dataStackLevel - level];
                    if (stack != null) {
                        retRes = newResults();
                        MocaUtils.copyColumns(retRes, stack.res);
                        
                        MocaUtils.copyCurrentRowByIndex(retRes, stack.iter);
                    }
                }
            }
            finally {
                _dataStackLock.unlock();
            }
            
            return retRes;
        }
        // @see com.redprairie.moca.MocaContext#executeCommand(java.lang.String, com.redprairie.moca.MocaArgument[])
        @Override
        public MocaResults executeCommand(String command, MocaArgument... args)
                throws MocaException {
            Map<String, MocaValue> map = new HashMap<String, MocaValue>();
            for (MocaArgument arg : args) {
                map.put(arg.getName(), arg.getDataValue());
            }
            return _executeTranslate(command, map, false);
        }
        
        // @see com.redprairie.moca.MocaContext#executeInline(java.lang.String, com.redprairie.moca.MocaArgument[])
        @Override
        public MocaResults executeInline(String command, MocaArgument... args)
                throws MocaException {
            Map<String, MocaValue> map = new HashMap<String, MocaValue>();
            for (MocaArgument arg : args) {
                map.put(arg.getName(), arg.getDataValue());
            }
            return _executeTranslate(command, map, true);
        }
        
        @Override
        public MocaResults executeSQL(String sql, MocaArgument... args) throws MocaException {
            return _transactionManager.executeSQL(sql, args);
        }

        @Override
        public MocaResults executeSQL(String sql, Map<String, ?> args) throws MocaException {
            return _transactionManager.executeSQL(sql, args);
        }
        
        // @see com.redprairie.moca.MocaContext#getDb()
        @Override
        public DatabaseTool getDb() {
            checkStatus();
            return _dbTool;
        }
        
        // @see com.redprairie.moca.MocaContext#enlistResource(javax.transaction.xa.XAResource)
        @Override
        public void enlistResource(XAResource resource) throws MocaException {
            checkStatus();
            MocaTransaction tx = _currentOrCreateTransaction();
            try {
                tx.addXAResource(resource);
            }
            catch (SQLException e) {
                throw new MocaDBException(e);
            }
        }
        
        private final DatabaseTool _dbTool;
    }
    
    private class MyTransactionManager extends MocaTransactionManager {
        /**
         * @param adapter
         * @param transactionList
         */
        public MyTransactionManager(DBAdapter adapter,
                Deque<MocaTransaction> transactionList) {
            super(DefaultServerContext.this, adapter, transactionList);
        }
        
        // @see com.redprairie.moca.server.exec.MocaDatabaseTool#getConnection()
        @Override
        public Connection getConnection() {
            checkStatus();
            return super.getConnection();
        }

        // @see com.redprairie.moca.server.exec.MocaDatabaseTool#commit()
        @Override
        public void commit() throws MocaException {
            checkStatus();
            boolean okToCommit = true;
            try {
                // Call native (C) precommit hooks.
                if (_nativeAdapter != null) {
                    _nativeAdapter.preCommit();
                }
                super.commit();
            }
            catch (MocaException e) {
                okToCommit = false;
                throw e;
            }
            finally {
                releaseNativeProcess(okToCommit);
            }
        }

        // @see com.redprairie.moca.server.exec.MocaDatabaseTool#rollback()
        @Override
        public void rollback() throws MocaException {
            checkStatus();
            try {
                super.rollback();
            }
            finally {
                releaseNativeProcess(false);
            }
        }

        // @see com.redprairie.moca.server.exec.MocaDatabaseTool#executeSQL(java.lang.String, java.util.Map)
        @Override
        public MocaResults executeSQL(String sql, Map<String, ?> args)
                throws MocaException {
            // Before executing make sure status is okay.
            checkStatus();
            _logger.debug("Executing SQL");

            ServerContextStatus previousStatus = _status;
            long beginTime = System.nanoTime();
            CommandPath currentPath = _commandPath.get();
            _commandPath.set(CommandPath.forSQL(currentPath));
            try {
                _status = ServerContextStatus.SQL_EXECUTION;
                
                _logArguments();
                
                this._currentPath = _commandPath.get();
                return super.executeSQL(sql, args);
            }
            finally {
                _logger.debug("SQL Exec complete");
                long endTime = System.nanoTime();
                _cmdPerf.logCommandExecution(_commandPath.get(), endTime - beginTime);
                _status = previousStatus;
                _commandPath.set(currentPath);
            }
        }

        // @see com.redprairie.moca.server.exec.MocaDatabaseTool#executeSQL(java.lang.String, com.redprairie.moca.MocaArgument[])
        @Override
        public MocaResults executeSQL(String sql, MocaArgument... args)
                throws MocaException {
            // Before executing make sure status is okay.
            checkStatus();
            _logger.debug("Executing SQL");

            ServerContextStatus previousStatus = _status;
            long beginTime = System.nanoTime();
            CommandPath currentPath = _commandPath.get();
            _commandPath.set(CommandPath.forSQL(currentPath));
            try {
                _status = ServerContextStatus.SQL_EXECUTION;
                
                _logArguments();
                
                this._currentPath = _commandPath.get();
                return super.executeSQL(sql, args);
            }
            finally {
                _logger.debug("SQL Exec complete");
                long endTime = System.nanoTime();
                _cmdPerf.logCommandExecution(_commandPath.get(), endTime - beginTime);
                _status = previousStatus;
                _commandPath.set(currentPath);
            }
        }

        // @see com.redprairie.moca.server.exec.MocaTransactionManager#commitDB()
        @Override
        public void commitDB() throws SQLException {
            checkStatus();
            super.commitDB();
        }

        // @see com.redprairie.moca.server.exec.MocaTransactionManager#getNextSequenceValue(java.lang.String)
        @Override
        public String getNextSequenceValue(String name) throws MocaException {
            checkStatus();
            return super.getNextSequenceValue(name);
        }

        // @see com.redprairie.moca.server.exec.MocaTransactionManager#rollbackDB()
        @Override
        public void rollbackDB() throws SQLException {
            checkStatus();
            super.rollbackDB();
        }

        // @see com.redprairie.moca.server.exec.MocaTransactionManager#rollbackDB(java.lang.String)
        @Override
        public void rollbackDB(String savepoint) throws SQLException {
            checkStatus();
            super.rollbackDB(savepoint);
        }

        // @see com.redprairie.moca.server.exec.MocaTransactionManager#setSavepoint(java.lang.String)
        @Override
        public void setSavepoint(String savepoint) throws SQLException {
            checkStatus();
            super.setSavepoint(savepoint);
        }
    }

    /**
     * 
     * 
     */
    public DefaultServerContext(ScriptAdapter scriptAdapter, DBAdapter dbAdapter,
                         SessionContext session,
                         SystemContext sys,
                         RequestContext request,
                         CommandRepository componentRespository,
                         NativeAdapterFactory nativePool,
                         CommandUsage cmdPerf,
                         Collection<String> blacklistedArgs,
                         MessageResolver messageResolver,
                         RemoteConnectionFactory connectionFactory) {
        _scriptAdapter = scriptAdapter;
        _dbAdapter = dbAdapter;
        _session = session;
        _sys = sys;
        _request = request;
        _componentRepository = componentRespository;
        _nativePool = nativePool;
        _cmdPerf = (cmdPerf == null) ? new NullCommandUsage() : cmdPerf;
        _blacklistedArgs = blacklistedArgs;
        _messageResolver = messageResolver;
        _transactionManager = new MyTransactionManager(_dbAdapter, _transactionStack);
        _connectionFactory = connectionFactory;
    }

    // @see com.redprairie.moca.server.exec.ServerContext#newResults()
    public EditableResults newResults() {
        return new SimpleResults();
    }
    
    public MocaContext getComponentContext() {
        // Before handing out make sure status is okay
        checkStatus();
        return new ComponentContext();
    }

    // @see com.redprairie.moca.server.exec.ServerContext#popStack()
    @Override
    public void popStack(boolean keepErrorState) {
        try {
            _dataStackLock.lock();
            boolean errorStateSet = _dataStack[_dataStackLevel].errorStatePushed;
            MocaException errorState = _dataStack[_dataStackLevel].pushedError;
            
            // First we null out the reference so the garbage collector can
            // collect the data stack if needed
            _dataStack[_dataStackLevel] = null;
            _dataStackLevel--;
            _stackLevel.decrementLevel();
            
            if (keepErrorState && _dataStackLevel >= 0 && errorStateSet) {
                _dataStack[_dataStackLevel].errorStatePushed = true;
                _dataStack[_dataStackLevel].pushedError = errorState;
            }
            
        }
        finally {
            _dataStackLock.unlock();
        }
    }

    // @see com.redprairie.moca.server.exec.ServerContext#pushStack()
    @Override
    public void pushStack() {
        try {
            _dataStackLock.lock();
            _dataStackLevel++;
            _stackLevel.incrementLevel();
            _dataStack[_dataStackLevel] = new _DataStackElement();
        }
        finally {
            _dataStackLock.unlock();
        }
    }
    

    // @see com.redprairie.moca.server.exec.ServerContext#setCommand(java.lang.String)
    @Override
    public void setCommand(ExecutableComponent command) {
        try {
            _dataStackLock.lock();
            _DataStackElement frame = _dataStack[_dataStackLevel];
            
            frame.command = command;
        }
        finally {
            _dataStackLock.unlock();
        }
    }

    // @see com.redprairie.moca.server.exec.ServerContext#setColumns(com.redprairie.moca.MocaResults)
    @Override
    public void setColumns(MocaResults res) {
        try {
            _dataStackLock.lock();
            _DataStackElement frame = _dataStack[_dataStackLevel];
            
            frame.res = res;
        }
        finally {
            _dataStackLock.unlock();
        }
    }

    // @see com.redprairie.moca.server.exec.ServerContext#setRow(com.redprairie.moca.RowIterator)
    public void setRow(RowIterator row) {
        try {
            _dataStackLock.lock();
            _DataStackElement frame = _dataStack[_dataStackLevel];
            
            frame.iter = row;
        }
        finally {
            _dataStackLock.unlock();
        }
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#clearArgs()
    public void clearArgs() {
        try {
            _dataStackLock.lock();
            _DataStackElement frame = _dataStack[_dataStackLevel];
            
            frame.args.clear();
            frame.argList.clear();
        }
        finally {
            _dataStackLock.unlock();
        }
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#addArg(java.lang.String, com.redprairie.moca.MocaOperator, com.redprairie.moca.server.MocaValue)
    public void addArg(String name, MocaOperator oper, MocaValue value) {
        try {
            _dataStackLock.lock();
            _DataStackElement frame = _dataStack[_dataStackLevel];
            String lowerName = name.toLowerCase();
            _StackArg arg = new _StackArg(name, oper, value);
            
            frame.args.put(lowerName, arg);
            frame.argList.add(arg);
        }
        finally {
            _dataStackLock.unlock();
        }
    }

    @Override
    public MocaException getLastError() {
        try {
            _dataStackLock.lock();
            for (int i = _dataStackLevel; i >= 0; i--) {
                _DataStackElement frame = _dataStack[i];
                if (frame.errorStateSet) {
                    return frame.error;
                }
            }
        }
        finally {
            _dataStackLock.unlock();
        }

        return null;
    }
        
    // @see com.redprairie.moca.server.exec.ServerContext#setError(com.redprairie.moca.MocaException)
    public void setError(MocaException e) {
        try {
            _dataStackLock.lock();
            _DataStackElement frame = _dataStack[_dataStackLevel];
            
            frame.error = e;
            frame.errorStateSet = true;
            frame.pushedError = e;
            frame.errorStatePushed = true;
        }
        finally {
            _dataStackLock.unlock();
        }
    }
    
    @Override
    public void fixErrorState() {
        try {
            _dataStackLock.lock();
            _DataStackElement frame = _dataStack[_dataStackLevel];
            
            if (!frame.errorStateSet) {
                frame.errorStateSet = true;
                frame.error = frame.pushedError;
            }
        }
        finally {
            _dataStackLock.unlock();
        }
    }
    
    public void clearError() {
        try {
            _dataStackLock.lock();
            if (_dataStackLevel >= 0) {
                _DataStackElement frame = _dataStack[_dataStackLevel];
                
                frame.error = null;
                frame.errorStateSet = false;
                frame.errorStatePushed = false;
            }
        }
        finally {
            _dataStackLock.unlock();
        }
    }
        
    // @see com.redprairie.moca.server.exec.ServerContext#getStackLevel()
    public StackLevel getStackLevel() {
        // This can be used in a separate thread, but the data stack lock must
        // be acquired before retrieval and released after you are done using it.
        return _stackLevel;
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#getVariable(java.lang.String, boolean)
    public MocaValue getVariable(String name, boolean markUsed) {
        return getVariable(name, null, markUsed);
    }

    // @see com.redprairie.moca.server.exec.ServerContext#getVariable(java.lang.String, boolean)
    public MocaValue getVariable(String name, String alias, boolean markUsed) {
        return _getVariable(name, alias, markUsed);
    }

    // @see com.redprairie.moca.server.exec.ServerContext#isVariableAvailable(java.lang.String)
    public boolean isVariableAvailable(String name) {
        return _isVariableMapped(name);
    }

    // javadoc inherited from interface
    // @see com.redprairie.moca.server.exec.ServerContext#getSystemVariable(java.lang.String)
    public String getSystemVariable(String name) {
        return _getSystemVariable(name);
    }
    
    // javadoc inherited from interface
    // @see com.redprairie.moca.server.exec.ServerContext#putSystemVariable(java.lang.String, java.lang.Sting)
    public void putSystemVariable(String name, String value) {
        String oldValue = _session.putVariable(name, value);
        
        _request.putVariable(name, value);
        
        // This will cover to make sure that if the old value was different
        // then the new one.
        // The first part of the if covers if oldvalue was null and value
        // was not
        // The second part of the if covers 2 parts: when oldvalue was not 
        // null and value was null (since equals is not true in this) and 
        // when they are both not null and are not equals
        if (_nativeAdapter != null && 
                ((value != null && oldValue == null) || 
                (oldValue != null && !oldValue.equals(value)))) {
            _nativeAdapter.setEnvironmentVariable(name, value);
        }
    }
    
    @Override
    public void putJavaSystemVariable(String name, String value) {
        _session.putVariable(name, value);
        _request.putVariable(name, value);
    }
    
    // javadoc inherited from interface
    // @see com.redprairie.moca.server.exec.ServerContext#removeSystemVariable(java.lang.String)
    public void removeSystemVariable(String name) {
        if (_session.isVariableMapped(name)) {
            _session.removeVariable(name);
            if (_nativeAdapter != null) {
                _nativeAdapter.setEnvironmentVariable(name, null);
            }
        }
        
        _request.removeVariable(name);
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#getVariableAsArgument(java.lang.String, boolean)
    public MocaArgument getVariableAsArgument(String name, boolean markUsed, boolean equalsOnly) {
        return _getVariableOrArg(name, null, markUsed, equalsOnly);
    }

    // @see com.redprairie.moca.server.exec.ServerContext#getVariableAsArgument(java.lang.String, boolean)
    public MocaArgument getVariableAsArgument(String name, String alias, boolean markUsed, boolean equalsOnly) {
        return _getVariableOrArg(name, alias, markUsed, equalsOnly);
    }

    // @see com.redprairie.moca.server.exec.ServerContext#getCommandArgs()
    public MocaArgument[] getCommandArgs(boolean getAll, boolean useLowLevel) {
        List<MocaArgument> argList = new ArrayList<MocaArgument>();
        if (!_commandStack.isEmpty()) {
            _CommandStackElement top = _commandStack.getFirst();
            if (top != null && top.args != null) {
                for (_StackArg arg : top.args) {
                    if (getAll || !arg.used) {
                        String argName = arg.name;
                        MocaArgument outArg = new MocaArgument(argName, arg.oper,
                            arg.value.getType(), arg.value.getValue());
                        argList.add(outArg);
                    }
                }
            }
        }
        
        if (useLowLevel) {
            try {
                _dataStackLock.lock();
                _DataStackElement frame = _dataStack[_dataStackLevel];
                for (_StackArg arg : frame.argList) {
                    if (getAll || !arg.used) {
                        String argName = arg.name;
                        MocaArgument outArg = new MocaArgument(argName, arg.oper,
                            arg.value.getType(), arg.value.getValue());
                        argList.add(outArg);
                    }
                }
            }
            finally {
                _dataStackLock.unlock();
            }
        }

        return argList.toArray(new MocaArgument[argList.size()]);
    }

    // @see com.redprairie.moca.server.exec.ServerContext#getRegistryValue(java.lang.String, java.lang.String, java.lang.Boolean)
    public String getRegistryValue(String key, boolean expand) {
        return _getRegistryValue(key, expand);
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#removeTransactionAttribute(java.lang.String)
    public void removeTransactionAttribute(String name) {
        _transactionManager.removeTransactionAttribute(name);
    }

    // @see com.redprairie.moca.server.exec.ServerContext#getTransactionAttribute(java.lang.String)
    public Object getTransactionAttribute(String name) {
        return _transactionManager.getTransactionAttribute(name);
    }

    // @see com.redprairie.moca.server.exec.ServerContext#setTransactionAttribute(java.lang.String, java.lang.Object)
    public void setTransactionAttribute(String name, Object value) {
        _transactionManager.setTransactionAttribute(name, value);
    }
    
    public CompiledScript compileScript(String script, String language) throws MocaException {
        _checkAuthorization(SecurityLevel.SCRIPT, "SCRIPT");

        _CommandStackElement currentCommand = _commandStack.isEmpty() ? null : _commandStack.getFirst();
        
        Command runningCommand = currentCommand != null ? currentCommand.command : null;
        
        ScriptAdapter adapter = _scriptAdapter;
        if (language != null) {
            adapter = _scriptAdapters.get(language);
            if (adapter == null) {
                    throw new MocaException(3929); // TODO real exception
            }
        }
        
        return adapter.compile(script, runningCommand);
        
    }

    // @see com.redprairie.moca.server.exec.ServerContext#executeScript(java.lang.String)
    public MocaResults executeScript(CompiledScript compiled) throws MocaException {
        _checkAuthorization(SecurityLevel.SCRIPT, "SCRIPT");

        CommandPath currentPath = _commandPath.get();
        _commandPath.set(CommandPath.forScript(currentPath));
        
        ServerContextStatus previousStatus = _status;
        try {
            _status = ServerContextStatus.SCRIPT_EXECUTION;
            
            _logArguments();
            
            return compiled.execute(new ComponentContext());
        }
        finally {
            _status = previousStatus;
            _commandPath.set(currentPath);
        }
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#executeScript(java.lang.String)
    public Object evaluateScript(CompiledScript compiled) throws MocaException {
        _checkAuthorization(SecurityLevel.SCRIPT, "SCRIPT");
        
        CommandPath currentPath = _commandPath.get();
        _commandPath.set(CommandPath.forScript(currentPath));

        ServerContextStatus previousStatus = _status;
        try {
            _status = ServerContextStatus.SCRIPT_EXECUTION;
            
            _logArguments();
            
            return compiled.evaluate(new ComponentContext());
        }
        finally {
            _status = previousStatus;
            _commandPath.set(currentPath);
        }
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#executeSQLWithVars(java.lang.String, java.lang.String)
    
    @Override
    public MocaResults executeSQLWithVars(String sql)
            throws MocaException {
        return executeSQLWithVars(sql, null);
    }

    
    // @see com.redprairie.moca.server.exec.ServerContext#executeSQL(java.lang.String)
    @Override
    public MocaResults executeSQLWithVars(String sql, String profileHint) throws MocaException {
        _checkAuthorization(SecurityLevel.SQL, "SQL");
        
        ServerContextStatus previousStatus = _status;
        long beginTime = System.nanoTime();
        CommandPath currentPath = _commandPath.get();
        if (profileHint == null) {
            _commandPath.set(CommandPath.forSQL(currentPath));
        }
        else {
            _commandPath.set(CommandPath.forSQL(currentPath, profileHint));
        }
        try {
            _status = ServerContextStatus.SQL_EXECUTION;
            
            _logArguments();
            
            SQLArgReplacer sqlScanner = new SQLArgReplacer(sql, this);
            String resultSQL = sqlScanner.getSQLString();
            BindList resultArgs = sqlScanner.getBindList();
            BindMode mode = resultArgs.isEmpty() ? BindMode.AUTO : BindMode.VAR;
            
            // We don't want to try if we don't even have enough characters
            // for the word update or delete.
            if (resultSQL.length() > 6) {
                String beginSql = resultSQL.substring(0, 6);
                // If it is an update or delete then we have to make sure we
                // have a where clause as well.
                if (beginSql.equalsIgnoreCase("update") || 
                        beginSql.equalsIgnoreCase("delete")) {
                    // We use the regex with the case not mattering
                    Matcher whereMatcher = _whereClauseMatcher.matcher(
                            resultSQL);
                    if (!whereMatcher.find()) {
                        throw new MissingWhereClauseException();
                    }
                }
            }
            
            EditableResults res = _dbAdapter.executeSQL(this, _currentOrCreateTransaction(), 
                    resultSQL, resultArgs, mode, false, _commandPath.get());
            if (res != null) {
                res.reset();
            }
            return res;
        }
        catch (SQLException e) {
            throw new MocaDBException(e);
        }
        finally {
            long endTime = System.nanoTime();
            _cmdPerf.logCommandExecution(_commandPath.get(), endTime - beginTime);
            _status = previousStatus;
            _commandPath.set(currentPath);
        }
    }
    
    @Override
    public ExecutableComponent lookupNamedCommand(String verbNounClause, boolean override) throws CommandNotFoundException {
        // First, try to execute known commands.
        _logger.debug(MocaUtils.concat("Looking up command: ", verbNounClause));
        final CommandInterceptor interceptor = _overriddenCommands.get(verbNounClause);
        if (interceptor != null) {
            _logger.debug(MocaUtils.concat("Found command interceptor [", 
                interceptor, "] for Command: ", verbNounClause));
            return new ExecutableComponent() {
                
                @Override
                public MocaResults execute(ServerContext ctx) throws MocaException {
                    return interceptor.intercept(ctx.getComponentContext());
                }
            };
        }
        
        Command current = _commandStack.isEmpty() ? null : _commandStack.getFirst().command;
        
        return _componentRepository.resolveCommand(verbNounClause, override, current);        
    }
    
    @Override
    public MocaResults executeDefinedCommand(Command commandDef, List<Trigger> triggers) throws MocaException {
        _logArguments();
        
        _logger.debug(MocaUtils.concat("Executing Command: ", commandDef));
        
        _checkAuthorization(commandDef.getSecurityLevel(), commandDef);
        
        // While executing, we need to keep track of the current command's
        // argument list. This is so a more complex command can capture the
        // arguments and use them for SQL variable replacement, e.g.
        // We don't need to lock here since this is just a read only operation
        _commandStack.addFirst(new _CommandStackElement(commandDef, 
                _dataStack[_dataStackLevel].argList));
        
        // We need to keep the current command path, for profiling purposes.
        CommandPath currentPath = _commandPath.get();
        _commandPath.set(CommandPath.forCommand(currentPath, commandDef));

        // From now on, we're authorized, due to the fact that we're running a command.
        _authStack++;

        MocaException excp = null;
        boolean needToRollback = true;
        long beginTime = System.nanoTime();

        try {
            if (commandDef.getTransactionType() == TransactionType.REQUIRES_NEW) {
                _logger.debug("Creating sub transaction");
                _pushTransaction();
                _nativeAdapterStack.addFirst(_nativeAdapter);
                _nativeAdapter = null;
            }
            
            MocaResults results = commandDef.execute(this);

            // Only look at triggers if executing a top-level command.
            if (triggers != null && triggers.size() != 0) {

                // Create a new stack frame in the current execution engine.
                pushStack();

                CommandPath beforeTriggerPath = _commandPath.get();

                try {
                    // Within this stack level, set the result columns.
                    setColumns(results);

                    _logger.debug(MocaUtils.concat("Firing triggers...    (", 
                            commandDef.getName(), ")"));

                    if (_logger.isDebugEnabled()) {
                        StringBuilder builder = new StringBuilder();
                        for (Trigger trigger : triggers) {
                            if (!trigger.isDisabled()) { 
                                if (builder.length() > 0) {
                                    builder.append("\n;\n");
                                }
                                else {
                                    // We move it to next line to make it a little easier to read
                                    builder.append("\n");
                                }
                                builder.append(trigger.getSyntax().trim());
                            }
                        }
                        
                        _logger.debug(builder);
                    }
                    
                    // If there are no rows, we should still execute
                    // triggers once.
                    if (results.getRowCount() == 0) {
                        for (Trigger trigger : triggers) {
                            if (!trigger.isDisabled()) {
                                _commandPath.set(CommandPath.forTrigger(beforeTriggerPath, trigger));
                                long triggerBegin = System.nanoTime();
                                try {
                                    trigger.execute(this);
                                }
                                finally {
                                    _cmdPerf.logCommandExecution(_commandPath.get(), System.nanoTime() - triggerBegin);
                                    _commandPath.set(beforeTriggerPath);
                                }
                            }
                        }
                    }
                    else {
                        // Execute once per row.
                        RowIterator rows = results.getRows();
                        while (rows.hasNext()) {

                            rows.next();
                            // To push the row's data onto the stack, tell
                            // the context about the current row's data.
                            setRow(rows);

                            for (Trigger trigger : triggers) {
                                if (!trigger.isDisabled()) {
                                    _commandPath.set(CommandPath.forTrigger(beforeTriggerPath, trigger));
                                    long triggerBegin = System.nanoTime();
                                    try {
                                        trigger.execute(this);
                                    }
                                    finally {
                                        _cmdPerf.logCommandExecution(_commandPath.get(), System.nanoTime() - triggerBegin);
                                        _commandPath.set(beforeTriggerPath);
                                    }
                                }
                            }
                        }
                    }
                    
                    _logger.debug(MocaUtils.concat(
                            "Done Firing triggers...    (", commandDef.getName(), ")"));
                }
                finally {
                    popStack(false);
                    _commandPath.set(beforeTriggerPath);
                }
                    
            }
            needToRollback = false;
            return results;
        }
        catch (MocaException e) {
            excp = e;
            throw e;
        }
        finally {
            try {
                if (commandDef.getTransactionType() == TransactionType.REQUIRES_NEW) {
                    if (needToRollback) {
                        _logger.debug("Rolling back sub transaction");
                    }
                    else {
                        _logger.debug("Committing sub transaction");
                    }
                    _popTransaction(needToRollback);
                    // This means that keepalive was set.  It can't be still
                    // executing as that makes no sense here since we 
                    // defined the scope of the native adapter to start
                    // at this stack level.
                    if (_nativeAdapter != null) {
                        _logger.debug("Native adapter used in nested " +
                                        "transaction with keepalive -- releasing");
                        _nativeAdapter.release();
                        _nativeAdapter = null;
                    }
                    _nativeAdapter = _nativeAdapterStack.removeFirst();
                }
            }
            catch (MocaException e) {
                _logger.error("Exception closing sub-transaction: " + e, e);
                throw e;
            }
            finally {
                _authStack--;
                _commandStack.removeFirst();

                if (_logger.isDebugEnabled()) {
                    _logger.debug(MocaUtils.concat("Executed Command: ", commandDef));
                    if (excp != null) {
                        _logger.debug(MocaUtils.concat("*** RAISING ERROR ", 
                                excp.getErrorCode()));
                    }
                    if (!_commandStack.isEmpty()) {
                        Command formerCommand = _commandStack.getFirst().command;
                        if (formerCommand != null) {
                            _logger.debug(MocaUtils.concat("Resuming execution of ", formerCommand));
                        }
                    }
                }

                long endTime = System.nanoTime();
                _cmdPerf.logCommandExecution(_commandPath.get(), endTime - beginTime);
                _commandPath.set(currentPath);
            }
        }

    }

    // @see com.redprairie.moca.server.exec.ServerContext#executeNamedCommand(java.lang.String, boolean)
    public MocaResults executeNamedCommand(String verbNounClause, boolean override) throws MocaException {
        ExecutableComponent exec = this.lookupNamedCommand(verbNounClause, override);
        return exec.execute(this);
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#executeRemote(java.lang.String, java.lang.String)
    @Override
    public MocaResults executeRemote(String remoteHost, String commandText) throws MocaException {

        _checkAuthorization(SecurityLevel.REMOTE, "REMOTE");

        CommandPath beforePath = _commandPath.get();
        CommandPath remotePath = CommandPath.forRemote(beforePath, remoteHost);
        _commandPath.set(remotePath);

        long remoteBegin = System.nanoTime();

        ServerContextStatus previousStatus = _status;
        try {
            _status = ServerContextStatus.REMOTE_EXECUTION;
            _logger.debug(MocaUtils.concat("Executing command on remote host (", 
                    remoteHost, "): ", commandText));
            MocaConnection conn = _getRemoteConnection(remoteHost, true, false);
            MocaArgument[] context;
            if (conn instanceof LocalConnection) {
                _logger.debug("Redirecting remote command to local server " +
                		"with different transaction...");
                context = null;
            }
            else {
                // Set up a stack context to be used for remote calls, but only
                // if it is real remote call.
                context = prepareNormalContext();
            }
            // We always need the command args for @* support
            MocaArgument[] commandArgs = getCommandArgs(false, false);
            try {
                return conn.executeCommandWithContext(commandText, context, commandArgs);
            }
            catch (MocaException e) {
                int errorCode = e.getErrorCode();
                if (errorCode == AuthenticationException.CODE) {
                    throw new RemoteAuthenticationException(e.getMessage());
                }
                else if (errorCode == SessionClosedException.CODE) {
                    throw new RemoteSessionClosedException();
                }
                else {
                    throw e;
                }
            }
        }
        finally {
            _logger.debug("Remote execution complete");
            _status = previousStatus;
            _cmdPerf.logCommandExecution(remotePath, System.nanoTime() - remoteBegin);
            _commandPath.set(beforePath);
        }
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#executeParallel(java.lang.String, java.lang.String, boolean)
    @Override
    public MocaResults executeParallel(String hosts, String commandText,
                                       boolean inTransaction)
            throws MocaException {
        _checkAuthorization(SecurityLevel.REMOTE, "PARALLEL");
        
        _logger.debug(MocaUtils.concat("Executing ", inTransaction ? "in" : "", 
                "parallel command on hosts (", hosts, 
                "): ", commandText));
        MocaArgument[] context = prepareNormalContext();
        MocaArgument[] commandArgs = getCommandArgs(false, false);
        
        String[] hostList = hosts.split(",");
        ExecutorService pool = Executors.newFixedThreadPool(hostList.length);
        List<RemoteCall> tasks = new ArrayList<RemoteCall>();
        
        for (String host : hostList) {
            MocaConnection conn = _getRemoteConnection(host, inTransaction, true);
            tasks.add(new RemoteCall(conn, commandText, context, commandArgs));
        }
        
        EditableResults out = newResults();
        out.addColumn("system", MocaType.STRING);
        out.addColumn("status", MocaType.INTEGER);
        out.addColumn("resultset", MocaType.RESULTS);
        
        CommandPath beforePath = _commandPath.get();
        CommandPath remotePath = CommandPath.forRemote(beforePath, hosts);
        _commandPath.set(remotePath);

        ServerContextStatus previousStatus = _status;
        try {
            _status = ServerContextStatus.REMOTE_EXECUTION;
            // Keep track of ther overall status of the execution.
            int overallStatus = 0;
            
            List<Future<MocaResults>> allResults = pool.invokeAll(tasks);
            int i = 0;
            for (Future<MocaResults> runResult : allResults) {
                out.addRow();
                out.setStringValue("system", hostList[i]);
                try {
                    MocaResults res = runResult.get();
                    out.setIntValue("status", 0);
                    out.setResultsValue("resultset", res);
                }
                catch (ExecutionException e) {
                    Throwable t = e.getCause();
                    if (t instanceof MocaException) {
                        // Keep track of the error state if we're checking for errors
                        int errorCode = ((MocaException) t).getErrorCode();
                        
                        
                        // We only allow the execution status to kill the current transaction if
                        // using inparallel.
                        if (inTransaction) {
                            overallStatus = errorCode;
                        }
                        
                        MocaResults res = ((MocaException) t).getResults();
                        if (res == null) {
                            res = new SimpleResults();
                        }
                        out.setIntValue("status", errorCode);
                        out.setResultsValue("resultset", res);
                    }
                    else {
                        // This is done as an info, since there is no easy way
                        // to propagate the error to the client
                        _logger.info("Unexpected Exception received while " +
                                "processing parallel execution for host: " + 
                                hostList[i], t);
                        out.setIntValue("status", UnexpectedException.CODE);
                        out.setResultsValue("resultset", new SimpleResults());
                    }
                }
                i++;
            }
            
            // If there was a failure, throw an exception.
            if (overallStatus != 0) {
                throw new ParallelExecutionException(overallStatus, out);
            }
        }
        catch (InterruptedException e) {
            throw new MocaInterruptedException(e);
        }
        finally {
            _logger.debug("Parallel execution complete");
            _status = previousStatus;
            _commandPath.set(beforePath);
            pool.shutdownNow();
        }
        
        return out;
    }
    
    @SuppressWarnings("unchecked")
    private MocaConnection _getRemoteConnection(String remoteHost, 
            boolean inTransaction, boolean parallel)
            throws MocaException {
        MocaTransaction tx = _currentOrCreateTransaction();
        long timeout = 0L;

        // Get list of current remote transactions.  
        Map<String, RemoteTransaction> remoteTransactions = (Map<String, RemoteTransaction>)tx.getAttribute(REMOTE_TX_ATTR);
        
        if (remoteTransactions == null) {
            remoteTransactions = new LinkedHashMap<String, RemoteTransaction>();
            tx.setAttribute(REMOTE_TX_ATTR, remoteTransactions);
        }
        
        // Break up the host into host#timeout
        String[] hostTimeout = remoteHost.split("#", 2);
        if (hostTimeout.length == 2) {
            remoteHost = hostTimeout[0];
            try {
                timeout = Long.parseLong(hostTimeout[1]) * 1000L;
            }
            catch (NumberFormatException e) {
                _logger.warn("Invalid timeout specified (" + hostTimeout[1] + ")");
                timeout = 0L;
            }
        }

        // If we don't have one for the named server, create a new one.
        RemoteTransaction remote = remoteTransactions.get(remoteHost);
        MocaConnection conn;
        if (remote == null) {
            Map<String, String> env = new LinkedHashMap<String, String>(_request.getAllVariables());
            
            // Push a session key into the request, corresponding to the ID of the user. 
            env.put(MocaSessionUtils.AUTH_ENV_KEY, 
                    MocaSessionUtils.newRemoteKey(_session.getUserId()));
            
            String remoteServerMapping = MocaRegistry.REGSEC_SERVER_MAPPING + 
                "." + remoteHost;
            // We have to check the registry to see if there are any mappings
            // that we have to transform the remote into something else
            String serverMapping = _sys.getConfigurationElement(
                    remoteServerMapping, false);
            
            // If the registry value wasn't present default back to remote host
            if (serverMapping == null) {
                serverMapping = remoteHost;
            }
            
            String url = _sys.getConfigurationElement(
                    MocaRegistry.REGKEY_SERVER_URL, false);
            
            // If the resultant mapping matches the url in the registry, then
            // we bypass the remote call and instead use a local connection to
            // avoid the performance hit of the connection protocol.
            // We don't support this for parallel.
            if (serverMapping.equals(url) && !parallel) {
                conn = new LocalConnection();
            }
            else {
                try {
                    conn = _connectionFactory.getConnection(serverMapping, env);
                }
                catch (MocaException e) {
                    throw new RemoteConnectionFailedException(serverMapping, e);
                }
            }
            
            conn.setAutoCommit(false);
            conn.setRemote(true);
            remoteTransactions.put(remoteHost, new RemoteTransaction(conn, inTransaction));
        }
        else {
            conn = remote.getConn();
        }
        
        conn.setTimeout(timeout);
        return conn;
    }

    private MocaArgument[] prepareNormalContext() {
        // Go through the data stack and grab a "normalized" context.  Basically, that means to make a list of current
        // stack values and push those into a single "row" of data.
        Map<String, MocaArgument> contextMap = new LinkedHashMap<String, MocaArgument>();
        Set<String> hiddenArguments = new HashSet<String>();
        
        // Loop through the data stack elements
        for (int i = _dataStackLevel; i >= 0; i--) {
            _DataStackElement stack = _dataStack[i];
            
            // If there are results, push those values into the map.
            if (stack.iter != null && stack.res != null) {
                for (int c = 0; c < stack.res.getColumnCount(); c++) {
                    String columnName = stack.res.getColumnName(c).toLowerCase();
                    if (!contextMap.containsKey(columnName) && 
                            !hiddenArguments.contains(columnName)) {
                        // If the result set is hidden then we want to
                        // make sure it gets hidden
                        Object value = stack.iter.getValue(c);
                        
                        if (value == MocaConstants.HIDDEN) {
                            hiddenArguments.add(columnName);
                        }
                        MocaType type = stack.res.getColumnType(c);
                        contextMap.put(columnName, new MocaArgument(columnName, type, stack.iter.getValue(c)));
                    }
                }
            }
            
            // If there are arguments at this level, push those values (and operators)
            // into the map.
            if (stack.args != null) {
                for (Map.Entry<String, _StackArg> entry : stack.args.entries()) {
                    String name = entry.getKey(); // already lower case
                    if (!contextMap.containsKey(name) && 
                            !hiddenArguments.contains(name.toLowerCase())) {
                        _StackArg arg = entry.getValue();
                        contextMap.put(name, new MocaArgument(arg.name, arg.oper, arg.value.getType(), arg.value.getValue()));
                    }
                }
            }
        }
        
        _logArguments();
        
        // Squeeze the context list into an array. 
        MocaArgument[] context = contextMap.values().toArray(new MocaArgument[contextMap.size()]);
        return context;
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#executeCommand(java.lang.String, java.util.Map, boolean)
    @Override
    public MocaResults executeCommand(String command, Map<String, ?> args, boolean keepContext)
            throws MocaException {
        return _executeTranslate(command, valueMap(args), keepContext);
    }
    
    private static Map<String, MocaValue> valueMap(Map<String, ?> input) {
        if (input == null) return null;
        
        Map<String, MocaValue> values = new LinkedHashMap<String, MocaValue>(input.size());
        for (Map.Entry<String, ?> entry : input.entrySet()) {
            Object value = entry.getValue();
            values.put(entry.getKey(), new MocaValue(MocaType.forValue(value), value));
        }
        return values;
    }
    
    @Override
    public MocaResults executeCommandWithRemoteContext(String command, Collection<MocaArgument> context,
                                                       Collection<MocaArgument> args) throws MocaException {

        // Keep track of incoming command args -- they're part of our remote
        // protocol
        List<_StackArg> incomingArgs = new ArrayList<_StackArg>();
        if (args != null) {
            for (MocaArgument arg : args) {
                String name = arg.getName();
                MocaValue value = new MocaValue(arg.getType(), arg.getValue());
                incomingArgs.add(new _StackArg(name, arg.getOper(), value));
            }
        }
        _CommandStackElement tmpStack = new _CommandStackElement(null, incomingArgs);
        _commandStack.addFirst(tmpStack);

        ServerContextStatus previousStatus = _status;
        try {
            _status = ServerContextStatus.IN_ENGINE;
            // Now, deal with the "normal" context. We treat the incoming
            // context as args, even though they're not,
            // and the data stack args differ greatly from the command args (set
            // up above).
            if (context != null) {
                Map<String, _StackArg> execArgs = new HashMap<String, _StackArg>();
                for (MocaArgument arg : context) {
                    String name = arg.getName();
                    String lowerName = name.toLowerCase();
                    MocaValue value = new MocaValue(arg.getType(), arg.getValue());
                    execArgs.put(lowerName, new _StackArg(name, arg.getOper(), value));
                }
                MocaResults res = _executeQuery(command, execArgs, false);
                return res;
            }
            else {
                MocaResults res = _executeQuery(command, null, false);
                return res;
            }
        }
        finally {
            _status = previousStatus;
            _commandStack.removeFirst();
        }
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#commit()
    @Override
    public void commit() throws MocaException {
        _transactionManager.commit();
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#rollback()
    @Override
    public void rollback() throws MocaException {
        _transactionManager.rollback();
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#getDbType()
    @Override
    public DBType getDbType() {
        return _dbAdapter.getDBType();
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#getNextSequenceValue(java.lang.String)
    @Override
    public String getNextSequenceValue(String name) throws MocaException {
        ServerContextStatus previousStatus = _status;
        try {
            _status = ServerContextStatus.SQL_EXECUTION;
            return _transactionManager.getNextSequenceValue(name);
        }
        finally {
            _status = previousStatus;
        }
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#getNativeLibraryAdapter()
    @Override
    synchronized
    public NativeLibraryAdapter getNativeLibraryAdapter() throws MocaException {
        if (_nativeAdapter == null) {
            _nativeAdapter = _nativePool.getNativeAdapter(this);
        }
        
        return _nativeAdapter;
    }
    
    @Override
    public void commitDB() throws SQLException {
        _transactionManager.commitDB();
    }

    @Override
    public MocaResults executeSQL(String sql, BindList args, boolean autoBind, boolean ignoreResults)
            throws SQLException, MocaException {
        // Before executing make sure status is okay.
        checkStatus();
        _logger.debug("Executing SQL");

        MocaTransaction tx = _currentOrCreateTransaction();
        
        ServerContextStatus previousStatus = _status;
        long beginTime = System.nanoTime();
        CommandPath currentPath = _commandPath.get();
        _commandPath.set(CommandPath.forSQL(currentPath));
        try {
            _status = ServerContextStatus.SQL_EXECUTION;
            
            _logArguments();
            
            BindMode bindMode = autoBind ? BindMode.AUTO : BindMode.NONE;
            
            EditableResults res = _dbAdapter.executeSQL(this, tx, sql, args, bindMode, 
                    ignoreResults, _commandPath.get());
            
            if (res != null) {
                res.reset();
            }
            return res;
        }
        finally {
            _logger.debug("SQL Exec complete");
            long endTime = System.nanoTime();
            _cmdPerf.logCommandExecution(_commandPath.get(), endTime - beginTime);
            _status = previousStatus;
            _commandPath.set(currentPath);
        }
    }

    @Override
    public void rollbackDB() throws SQLException {
        _transactionManager.rollbackDB();
    }
    
    @Override
    public void setSavepoint(String savepoint) throws SQLException {
        _transactionManager.setSavepoint(savepoint);
    }

    @Override
    public void rollbackDB(String savepoint) throws SQLException {
        _transactionManager.rollbackDB(savepoint);
    }
    
    @Override
    public void close() {
        if (!_closed) {
            _logger.debug("Server Context Closed");
    
            // Now we have to rollback each transaction one by one.
            while (_transactionStack.size() > 0) {
                try {
                    rollback();
                }
                catch (MocaException e) {
                    _logger.error("Error closing transaction (should already be closed)");
                }
                _transactionStack.remove();
            }
            
            // Release the native process when we're done with our
            // transaction.
            try {
                if (_nativeAdapter != null) {
                    _nativeAdapter.release();
                    _nativeAdapter = null;
                }
            }
            catch (MocaException e) {
                _logger.error("Error releasing native process");
            }
            
            // After we are finished closing make sure to mark it as so.
            _closed = true;
        }
    }
    
    /* (non-Javadoc)
     * @see com.redprairie.moca.server.exec.ServerContext#hasKeepalive()
     */
    @Override
    public boolean hasKeepalive() {
        return _nativeAdapter != null;
    }
    
    @Override
    public boolean hasTransaction() {
        if (_transactionStack.isEmpty()) {
            return false;
        }
        
        if (_transactionStack.size() == 1 && !_transactionStack.getFirst().isOpen()) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public void logDebug(Object msg) {
        _logger.debug(msg);
    }
    
    //
    // Implementation
    //
    
    // Execute a query request and translate the arguments into well-known value objects.
    private MocaResults _executeTranslate(String query, Map<String, MocaValue> args, 
            boolean keepContext) throws MocaException {
        ServerContextStatus previousStatus = _status;
        try {
            _status = ServerContextStatus.IN_ENGINE;
            MocaResults res;
            _logger.debug(MocaUtils.concat("Command initiated: [", query, "], Inline: ", keepContext));
            if (args != null) {
                Map<String, _StackArg> execArgs = new HashMap<String, _StackArg>();
                for (Entry<String, MocaValue> e : args.entrySet()) {
                    String name = e.getKey();
                    String lowerName = name.toLowerCase();
                    MocaValue value = e.getValue();
                    execArgs.put(lowerName, new _StackArg(name, MocaOperator.EQ, value));
                }
                res = _executeQuery(query, execArgs, keepContext);
            }
            else {
                res = _executeQuery(query, null, keepContext);
            }
            
            return res;
        }
        finally {
            _status = previousStatus;
        }
    }

    // Execute a client query
    private MocaResults _executeQuery(String query, Map<String, _StackArg> args, boolean keepContext)
            throws MocaException {
        // Make sure status is okay before executing the command.
        checkStatus();
        _logger.debug("Parsing command... ");

        CommandSequence compiled;
        try {
            MocaParser parser = new MocaParser(query);
            compiled = parser.parse();
            _logger.debug("Parsed command");
        }
        catch (MocaParseException e) {
            _logger.debug(MocaUtils.concat("Parse Error: ", e));
            throw e;
        }

        _previousStatement.push(query);

        _DataStackElement[] tempStack = null;
        int tempStackLevel = 0;
        
        if (!keepContext) {
            try {
                _dataStackLock.lock();
                // We copy the stack level
                tempStackLevel = _dataStackLevel;
                _dataStackLevel = -1;
                // We copy the actual stack up to the stack level
                tempStack = Arrays.copyOf(_dataStack, tempStackLevel + 1);
                // We then have to fill the datastack with nulls, so that our
                // context is cleared
                Arrays.fill(_dataStack, 0, tempStackLevel + 1, null);
            }
            finally {
                _dataStackLock.unlock();
            }
        }
        pushStack();
        setCommand(compiled);
        try {
            if (args != null) {
                try {
                    _dataStackLock.lock();
                    _DataStackElement frame = _dataStack[_dataStackLevel];
                    frame.args.putAll(Multimaps.forMap(args));
                    frame.argList.addAll(args.values());
                }
                finally {
                    _dataStackLock.unlock();
                }
            }
            _logger.debug("Executing...");
            return compiled.execute(this);
        }
        finally {
            popStack(false);
            _previousStatement.pop();
            if (!keepContext) {
                try {
                    _dataStackLock.lock();
                    // We copy the stack level back in
                    _dataStackLevel = tempStackLevel;
                    // We copy the actual stack up to the stack level back in
                    System.arraycopy(tempStack, 0, _dataStack, 0, _dataStackLevel + 1);
                }
                finally {
                    _dataStackLock.unlock();
                }
            }
        }
    }
    
    /**
     * This will log a server argument given the values.  Any value that is a
     * Java object will default to what toString would provide as if it wasn't
     * overwritten by the class
     * @param type The type, or the first part appended to the trace ie. Argument
     *             or Published
     * @param name The name of the value
     * @param value The value itself
     */
    private void _logServerArguments(String type, String name, MocaValue value) {
        
        switch (value.getType()) {
        case OBJECT:
            Object javaObj = value.getValue();
            if (javaObj == null) {
                _argumentLogger.debug(MocaUtils.concat(type, "  ", name, 
                        "=null  (", value.getType().toString(), ")"));
            }
            else if (javaObj == MocaConstants.HIDDEN) {
                _argumentLogger.debug(MocaUtils.concat(type, "  ", name, "=", 
                        javaObj));
            }
            else {
                _argumentLogger.debug(MocaUtils.concat(type, "  ", name, "=", 
                        javaObj.getClass(), "@", System.identityHashCode(javaObj), 
                        "  (", value.getType().toString(), ")"));
            }
            break;

        default:         
            if (_blacklistedArgs != null && _blacklistedArgs.contains(name)) {     
                _argumentLogger.debug(new SecureLogMessage(true,
                    type + "  " + name + "=%1$s (" + value.getType().toString() + ")", value.asString()));
            }
            else {
                _argumentLogger.debug(MocaUtils.concat(type, "  ", name, "=", 
                    value.asString(), "  (", value.getType().toString(), ")"));
            }
                
            break;
        }
    }
    
    /**
     * This will log all the current arguments or results on the stack to the
     * argument logger if it is enabled.
     */
    private void _logArguments() {
        // We only want to go through the arguments if the logger
        // has trace enabled
        if (_argumentLogger.isDebugEnabled()) {
            // We loop through the stack, and we include the 0th level since
            // a user can pass in what people refer to as bind variables, but
            // really are just lvl 0 stack variables.
            for (int i = _dataStackLevel; i >= 0; i--) {
                _DataStackElement traceFrame = _dataStack[i];
                List<_StackArg> traceArgs = traceFrame.argList;
                RowIterator traceIter = traceFrame.iter;
                MocaResults traceMetadata = traceFrame.res;
                
                // We print out the results first
                if (traceMetadata != null && traceIter != null) {
                    // Now loop through all the columns of the metadata
                    for (int j = 0; j < traceMetadata.getColumnCount(); ++j) {
                        MocaType traceType = traceMetadata.getColumnType(j);
                        _logServerArguments("Published", traceMetadata
                                .getColumnName(j), new MocaValue(
                                traceType, traceIter.getValue(j)));
                    }
                }

                // We print out the arguments last
                if (traceArgs != null) {
                    for (_StackArg arg : traceArgs) {
                        MocaValue value = arg.value;
                        _logServerArguments("Argument", arg.name, value);
                    }
                }
                
                _logger.debug("--------------------------------------");
                
            }
        }
    }
    
    private void releaseNativeProcess(boolean okToCommit) throws MocaException {
        
        // Release the native process when we're done with our
        // transaction.
        if (_nativeAdapter != null) {
            // Call native (C) postcommit hooks.
            _nativeAdapter.postTransaction(okToCommit);
            
            // release the adapter, unless something is executingon the C side.
            if (!_nativeAdapter.isKeepaliveSet() && !_nativeAdapter.currentlyActive()) {
                _nativeAdapter.release();
                _nativeAdapter = null;
            }
        }
    }
    
    private MocaValue _getVariable(String name, String alias, boolean markUsed) {
        // Shortcut if null is passed in
        if (name == null && alias == null) {
            return null;
        }
        
        String lowerName = (name == null || name.isEmpty()) ? null : name.toLowerCase();
        String lowerAlias = (alias == null || alias.isEmpty()) ? null : alias.toLowerCase();
        
        // We shouldn't have to synchronize this since we are just doing a read
        for (int i = _dataStackLevel; i >= 0; --i) {
            _DataStackElement stack = _dataStack[i];
            if (stack.iter != null && stack.res != null) {
                int column = -1;
                if (lowerName != null) {
                    column = stack.res.getColumnNumber(lowerName);
                }
                if (lowerAlias != null) {
                    int tmpCol = stack.res.getColumnNumber(lowerAlias);
                    // If the alias column exists in the current stack level,
                    // use it, but only if the actual column name is not found,
                    // or was found in this stack level but earlier in the result
                    // set.
                    if (tmpCol != -1 && (column == -1 || tmpCol < column)) {
                        column = tmpCol;
                    }
                }
                
                if (column != -1) {
                    if (stack.iter.getValue(column) == MocaConstants.HIDDEN) {
                        return null;
                    }
                    return new MocaValue(stack.res.getColumnType(column), 
                            stack.iter.getValue(column));
                }
            }
            
            // Check arguments.
            MocaArgument argValue = null;
            if (lowerName != null) {
                argValue = stack.getArg(lowerName, markUsed, true);
            }
            
            if (lowerAlias != null) {
                MocaArgument tmp = stack.getArg(lowerAlias, markUsed, true);
                if (argValue == null) {
                    argValue = tmp;
                }
                else {
                    if (tmp != null && stack.useAlias(lowerName, lowerAlias)) {
                        argValue = tmp;
                    }
                }
            }
            
            if (argValue != null) {
                return new MocaValue(argValue.getType(), argValue.getValue());
            }
        }
        
        return null;
    }
    
    private MocaArgument _getVariableOrArg(String name, String alias, boolean markUsed, boolean equalsOnly) {
        // Shortcut if null is passed in
        if (name == null && alias == null) {
            return null;
        }
        
        String lowerName = (name == null || name.isEmpty()) ? null : name.toLowerCase();
        String lowerAlias = (alias == null || alias.isEmpty()) ? null : alias.toLowerCase();
        
        // We shouldn't have to synchronize this since we are just doing a read
        for (int i = _dataStackLevel; i >= 0; --i) {
            _DataStackElement stack = _dataStack[i];
            if (stack.iter != null && stack.res != null) {
                int column = -1;
                if (lowerName != null) {
                    column = stack.res.getColumnNumber(lowerName);
                }
                if (lowerAlias != null) {
                    int tmpCol = stack.res.getColumnNumber(lowerAlias);
                    if (tmpCol != -1 && tmpCol < column) {
                        column = tmpCol;
                    }
                }
                
                if (column != -1) {
                    if (stack.iter.getValue(column) == MocaConstants.HIDDEN) {
                        return null;
                    }
                    Object valueData = stack.iter.getValue(column);
                    MocaType valueType = stack.res.getColumnType(column);
                    return new MocaArgument(name, MocaOperator.EQ, valueType, valueData);
                }
            }
            
            MocaArgument argValue = stack.getArg(lowerName, markUsed, equalsOnly);
            if (lowerAlias != null) {
                MocaArgument tmp = stack.getArg(lowerAlias, markUsed, true);
                if (argValue == null) {
                    argValue = tmp;
                }
                else {
                    if (tmp != null && stack.useAlias(lowerName, lowerAlias)) {
                        argValue = tmp;
                    }
                }
            }
            
            if (argValue == null && lowerAlias != null) {
                argValue = stack.getArg(lowerAlias, markUsed, true);
            }
            
            if (argValue != null) {
                return argValue;
            }
        }
        
        return null;
    }
    
    private boolean _isVariableMapped(String name) {
        String lowerName = name.toLowerCase();
        // We shouldn't have to synchronize this since we are just doing a read
        for (int i = _dataStackLevel; i >= 0; --i) {
            _DataStackElement stack = _dataStack[i];
            if (stack.res != null && stack.res.containsColumn(lowerName) && 
                    stack.iter != null) {
                if (stack.iter.getValue(lowerName) == MocaConstants.HIDDEN) {
                    return false;
                }
                return true;
            }

            if (stack.args.containsKey(lowerName)) {
                return true;
            }
        }
        
        return false;
    }

    private String _getSystemVariable(String name) {
        if (_request != null && _request.isVariableMapped(name)) {
            return _request.getVariable(name);
        }
        else if (_session != null && _session.isVariableMapped(name)) {
            return _session.getVariable(name);
        }
        else {
            return _sys.getVariable(name);
        }
    }
    
    private String _getRegistryValue(String key, boolean expand) {
        return _sys.getConfigurationElement(key, expand);
    }
    
    private static class _StackArg {
        private _StackArg(String name, MocaOperator oper, MocaValue value) {
            ArgCheck.notNull(name);
            ArgCheck.notNull(oper);
            ArgCheck.notNull(value);
            this.name = name;
            this.oper = oper;
            this.value = value;
            this.used = false;
        }
        private String name;
        private MocaOperator oper;
        private MocaValue value;
        private boolean used;
    }

    private static class _CommandStackElement {
        
        private _CommandStackElement(Command command, Collection<_StackArg> args) {
            this.command = command;
            this.args = args;
        }
        private Command command;
        private Collection<_StackArg> args;
    }
    
    private static class _DataStackElement {
        
        private _DataStackElement() {
            args = ArrayListMultimap.create();
            argList = new ArrayList<_StackArg>();
        }
        
        private MocaArgument getArg(String name, boolean markUsed, boolean equalsOnly) {
            Collection<_StackArg> argsForName = null;
            // Check if the key exists before getting it, this way we
            // can reduce some overhead of initializing the collection behind
            // the multimap
            if (args.containsKey(name)) {
                argsForName = args.get(name);
            }
            
            if (argsForName == null || argsForName.size() == 0) {
                return null;
            }

            _StackArg arg = null;
            for (_StackArg tmp : argsForName) {
                // Operator mismatch -- don't return other operators 
                if (!equalsOnly || tmp.oper == MocaOperator.EQ) {
                    arg = tmp;
                    if (markUsed) tmp.used = true;
                }
            }
            
            if (arg == null) {
                return null;
            }

            return new MocaArgument(arg.name, arg.oper, arg.value.getType(), arg.value.getValue());
        }
        
        private boolean useAlias(String name, String alias) {
            // This method is called to determine which argument to use, the
            // one with the primary name, or the one with the alias.  The
            // last one on the stack should take precedence.  The only time
            // this method should be called is when both name and alias are
            // not null, and are known to be on the current stack level as
            // arguments.
            boolean useAlias = false;
            // last one wins.
            for (_StackArg arg : argList) {
                if (arg.name.equalsIgnoreCase(name)) {
                    useAlias = false;
                }
                else if (arg.name.equalsIgnoreCase(alias)) {
                    useAlias = true;
                }
            }
            return useAlias;
        }
        
        private MocaException error;
        private boolean errorStateSet = false;
        private MocaException pushedError;
        private boolean errorStatePushed = false;
        private final Multimap<String, _StackArg> args;
        private final List<_StackArg> argList;
        
        // No one should ever call .next() on the MocaResults.  This is only
        // here for column metadata
        private MocaResults res;
        // No one should ever call .next() on the RowIterator, it is a single
        // row pointing into the result set
        private RowIterator iter;
        // This is the command that is being executed at this stack level.  Useful
        // for reporting and management purposes
        private ExecutableComponent command;
    }
    
    private MocaTransaction _currentOrCreateTransaction() throws MocaException {
        MocaTransaction current = _transactionStack.peekFirst();
        if (current == null) {
            // If there is no current tx and we have a global tx then we need
            // start a new moca tx to tie to it
            if (TransactionManagerUtils.getCurrentTransaction() != null) {
                current = _dbAdapter.newTransaction();
                _transactionStack.addFirst(current);
            }
            else {
                _pushTransaction();
            }
        }
        
        MocaTransaction tx = _transactionStack.getFirst();
        return tx;
    }
    
    private void _pushTransaction() throws MocaException {
        // We have to suspend the top tx
        MocaTransaction current = _transactionStack.peekFirst();
        
        if (current != null) {
            try {
                current.suspend();
            }
            catch (SQLException e) {
                throw new MocaDBException(e);
            }
        }
        MocaTransaction tx = _dbAdapter.newTransaction();
        _transactionStack.addFirst(tx);
    }
    
    private void _popTransaction(boolean rollback) throws MocaException {
        if (rollback) {
            rollback();
        }
        else {
            commit();
        }
        _transactionStack.removeFirst();
        // We have to resume the now top tx if there was one
        MocaTransaction current = _transactionStack.peekFirst();
        if (current != null) {
            try {
                current.resume();
            }
            catch (SQLException e) {
                throw new MocaDBException(e);
            }
        }
    }
    
    /**
     * This method will return the stack contents for the given MocaContext
     * 
     * The result set is created by going level by level of the stack first
     * putting the result values of the stack first in the result.  Then it
     * will append the arguments for that stack level.
     * 
     * If there is any collision between columns in results or arguments the
     * first occurance of that column will be display in the result set.  
     * Multiple columns with the same name will not exist in the result set
     * 
     * If the stack is not flattened it will create a new result set for each
     * level of the stack and insert it into the returned result set in the 
     * column <code>results</code>.  Each row of the returned result will also
     * state what command was executed at the level <code>stack_command</code> 
     * as well as the stack level itself <code>stack_level</code>
     * @param ctx The moca context to get the stack dump from
     * @param flatten Whether or not to flatten the data stack into a single
     *        row of results
     * @return The results containing the stack dump
     */
    public static MocaResults dumpStack(MocaContext ctx, boolean flatten) {
        
        EditableResults retRes = ctx.newResults();
        
        if (ctx instanceof DefaultServerContextAware) {
            DefaultServerContext me = 
                ((DefaultServerContextAware) ctx).getDefaultServerContext();
            
            if (flatten) {
                retRes.addRow();
            }
            else {
                retRes.addColumn("results", MocaType.RESULTS);
                retRes.addColumn("stack_command", MocaType.STRING);
                retRes.addColumn("stack_level", MocaType.INTEGER);
            }
            
            try {
                me._dataStackLock.lock();
                
                int stackLevel = me._dataStackLevel;
                int actualLevel = me.getStackLevel().getLevel();
                // Now loop through all the stack
                for (; actualLevel >= 0; --actualLevel, --stackLevel) {
                    _DataStackElement element; 
                    if (stackLevel >= 0) {
                        element = me._dataStack[stackLevel];
                    }
                    else {
                        element = null;
                    }
                    
                    EditableResults result;
                    // If we are flattening we want to keep using the same
                    // result
                    if (flatten) {
                        result = retRes;
                    }
                    // Else we use a new result set
                    else {
                        retRes.addRow();
                        if (element != null) {
                            retRes.setStringValue("stack_command", String.valueOf(element.command));
                            retRes.setIntValue("stack_level", actualLevel);
                        }
                        else {
                            retRes.setStringValue("stack_command", "");
                            retRes.setIntValue("stack_level", actualLevel);
                        }
                        
                        result = ctx.newResults();
                        result.addRow();
                        
                        retRes.setResultsValue("results", result);
                    }
                    
                    // Only add to the result set if we're on the known stack
                    if (element != null) {
                        RowIterator rowIter = element.iter;
                        MocaResults results = element.res;
                        
                        // If the last result doesn't have a row we just ignore it
                        if (rowIter != null && results != null) {
                            
                            // Now go through all the columns of the next result set
                            for (int j = 0; j < results.getColumnCount(); ++j) {
                                String columnName = results.getColumnName(j);
                                
                                // If our result set doesn't already contain this column
                                // that we write stuff into it setting the value
                                if (!result.containsColumn(columnName)) {
                                    Object value = rowIter.getValue(j);
                                    
                                    // If it is hidden we want to do the toString
                                    if (value == MocaConstants.HIDDEN) {
                                        result.addColumn(columnName, 
                                                MocaType.STRING);
                                        result.setValue(columnName, 
                                                value.toString());
                                    }
                                    else {
                                        result.addColumn(columnName, 
                                                results.getColumnType(j));
                                        result.setValue(columnName, value);
                                    }
                                }
                            }
                        }
                        
                        for (_StackArg arg : element.argList) {
                            String columnName = arg.name;
                            // If our result set doesn't already contain this column
                            // that we write stuff into it setting the value
                            if (!result.containsColumn(columnName)) {
                                result.addColumn(columnName, 
                                        arg.value.getType());
                                result.setValue(columnName, 
                                        arg.value.getValue());
                            }
                        }
                    }
                }
            }
            finally {
                me._dataStackLock.unlock();
            }
            
        }

        return retRes;
    }
    
    /**
     * This will list the given commands for the given MocaContext.  By default
     * it will return all commands that are found.  An optional CommandFilter
     * can be specified which will allow a user to pick a command by name and/or
     * sequence level.
     * @param ctx The moca context to check for commands
     * @return A MocaResults that contains all the information pertinent to
     *         the commands that passed the filter.
     *         <br> <b>Returned Columns:</b>
     *         <br> cmplvl - The component level name as a {@link java.lang.String}
     *         <br> cmplvlseq - The sort sequence of the component level as an 
     *         {@link int}
     *         <br> command - The actual name of the command as a 
     *         {@link java.lang.String}
     *         <br> cmptyp - The command type of the command as a 
     *         {@link java.lang.String}
     *         <br> type - The command type of the command abbreviated as a 
     *         {@link java.lang.String} 
     *         <br> syntax - The syntax of the command if it is local syntax as 
     *         a {@link java.lang.String}
     *         <br> class - The class name of the command if it is a java method
     *         as a {@link java.lang.String}
     *         <br> functn - The function of the command if it is a C function or
     *         the method if is a java method as a {@link java.lang.String}
     *         <br> insecure - Whether or not this command is insecure as a 
     *         {@link boolean}
     *         <br> trnstyp - The transaction type of the command as a 
     *         {@link java.lang.String} 
     */
    public static MocaResults listCommands(MocaContext ctx, CommandFilter filter) {
        EditableResults retRes = ctx.newResults();
        
        retRes.addColumn("cmplvl", MocaType.STRING);
        retRes.addColumn("cmplvlseq", MocaType.INTEGER);
        retRes.addColumn("command", MocaType.STRING);
        retRes.addColumn("cmdtyp", MocaType.STRING);
        retRes.addColumn("type", MocaType.STRING);
        retRes.addColumn("syntax", MocaType.STRING);
        retRes.addColumn("class", MocaType.STRING);
        retRes.addColumn("functn", MocaType.STRING);
        retRes.addColumn("security", MocaType.STRING);
        retRes.addColumn("trnstyp", MocaType.STRING);
        retRes.addColumn("filename", MocaType.STRING);
        retRes.addColumn("desc", MocaType.STRING);
        
        // If the ctx isn't our context object then don't allow it
        if (ctx instanceof DefaultServerContextAware) {
            DefaultServerContext me = 
                ((DefaultServerContextAware) ctx).getDefaultServerContext();
            
            // Now we actually get all the commands from the repository
            List<Command> commands = me._componentRepository.getAllCommands();
            
            // Loop through each of them and check if it passes the filter
            // and then set the information on the result set
            for (Command command : commands) {
                if (filter == null || filter.accept(command.getName(), 
                        command.getLevel().getName())) {
                    retRes.addRow();
                    
                    // These values are common to all commands
                    retRes.setStringValue("cmplvl", command.getLevel().getName());
                    retRes.setIntValue("cmplvlseq", command.getLevel().getSortseq());
                    retRes.setStringValue("command", command.getName());
                    retRes.setStringValue("security", command.getSecurityLevel().toString());
                    retRes.setStringValue("trnstyp", command.getTransactionType().toString());
                    retRes.setStringValue("filename", command.getFileName());
                    retRes.setStringValue("desc", command.getDescription());
                    
                    switch (command.getType()) {
                    case LOCAL_SYNTAX:
                        retRes.setStringValue("cmdtyp", "L");
                        retRes.setStringValue("type", "Local Syntax");
                        
                        // If it is local syntax then we need to add the syntax
                        if (command instanceof LocalSyntaxCommand) {
                            retRes.setStringValue("syntax", 
                                    ((LocalSyntaxCommand)command).getSyntax().trim());
                        }
                        break;
                    case C_FUNCTION:
                        retRes.setStringValue("cmdtyp", "C");
                        retRes.setStringValue("type", "C Function");
                        
                        // If it is a CFunctionCommand that we need to append
                        // the function to it
                        if (command instanceof CFunctionCommand) {
                            retRes.setStringValue("functn", 
                                    ((CFunctionCommand)command).getFunction());   
                        }
                        break;
                    case SIMPLE_C_FUNCTION:
                        retRes.setStringValue("cmdtyp", "C");
                        retRes.setStringValue("type", "Simple C Function");
                        
                        // If it is a CFunctionCommand that we need to append
                        // the function to it
                        if (command instanceof CFunctionCommand) {
                            retRes.setStringValue("functn", 
                                    ((CFunctionCommand)command).getFunction());   
                        }
                        break;
                    case COM_METHOD:
                        retRes.setStringValue("cmdtyp", "O");
                        retRes.setStringValue("type", "COM Method");
                        break;
                    case JAVA_METHOD:
                        retRes.setStringValue("cmdtyp", "J");
                        retRes.setStringValue("type", "Java Method");
                        
                        // If it is a JavaMethod that we need to append the
                        // class and function/method to it
                        if (command instanceof JavaCommand) {
                            retRes.setStringValue("class", 
                                    ((JavaCommand)command).getClassName());
                            retRes.setStringValue("functn", 
                                    ((JavaCommand)command).getMethod()); 
                        }
                        break;
                    default:
                        // This means it is unknown
                        retRes.setStringValue("cmdtyp", "U");
                        retRes.setStringValue("type", "Unknown");
                        break;
                    }
                }
            }
        }
        
        return retRes;
    }
    
    public static Collection<CommandUsageStatistics> listCommandUsage(MocaContext ctx) {
        // If the ctx isn't our context object then don't allow it
        if (ctx instanceof DefaultServerContextAware) {
            DefaultServerContext me = 
                ((DefaultServerContextAware) ctx).getDefaultServerContext();
            return me._cmdPerf.getStats();
        }
        else {
            return null;
        }
    }

    public static void clearCommandUsage(MocaContext ctx) {
        // If the ctx isn't our context object then don't allow it
        if (ctx instanceof DefaultServerContextAware) {
            DefaultServerContext me = 
                ((DefaultServerContextAware) ctx).getDefaultServerContext();
            me._cmdPerf.reset();
        }
    }

    /**
     * This will list the given command arguments for the given MocaContext.  By
     * default it will return all command arguments available.  An optional
     * CommandArgumentFilter can be specified which will allow a user to pick
     * a command argument by name, component level and/or argument name.
     * @param ctx The Moca Context
     * @param filter The optional filter that can narrow the command arguments
     *        returned
     * @return A MocaResults that contains all the information pertinent to
     *         the command arguments that passed the filter.
     *         <br> <b>Returned Columns:</b>
     *         <br> cmplvl - The component level name as a {@link java.lang.String}
     *         <br> command - The actual name of the command as a 
     *         {@link java.lang.String}
     *         <br> argname - The argument name as a {@link java.lang.String}
     *         <br> altnam - The alternate name for the argument as a 
     *         {@link java.lang.String} 
     *         <br> argtyp - The argument data type as a {@link java.lang.String}
     *         <br> fixval - The default value of the argument if a value was
     *         not provided as a {@link java.lang.String}
     *         <br> argidx - The index of the argument for the command as a 
     *         {@link int}
     *         <br> argreq - Whether or not this argument is required as a 
     *         {@link boolean} 
     */
    public static MocaResults listCommandArguments(MocaContext ctx, 
            CommandArgumentFilter filter) {
        EditableResults retRes = ctx.newResults();
        
        retRes.addColumn("cmplvl", MocaType.STRING);
        retRes.addColumn("command", MocaType.STRING);
        retRes.addColumn("argnam", MocaType.STRING);
        retRes.addColumn("altnam", MocaType.STRING);
        retRes.addColumn("argtyp", MocaType.STRING);
        retRes.addColumn("fixval", MocaType.STRING);
        retRes.addColumn("argidx", MocaType.INTEGER);
        retRes.addColumn("argreq", MocaType.BOOLEAN);
        
        // If the ctx isn't our context object then don't allow it
        if (ctx instanceof DefaultServerContextAware) {
            DefaultServerContext me = 
                ((DefaultServerContextAware) ctx).getDefaultServerContext();
            
            // Now we actually get all the commands from the repository
            List<Command> commands = me._componentRepository.getAllCommands();
            
            for (Command command : commands) {
                List<ArgumentInfo> arguments = command.getArguments();
                int index = 0;
                
                for (ArgumentInfo argument : arguments) {
                    if (filter == null || filter.accept(
                            command.getLevel().getName(), command.getName(), 
                            argument.getName())) {
                        retRes.addRow();
                        
                        retRes.setStringValue("cmplvl", command.getLevel().getName());
                        retRes.setStringValue("command", command.getName());
                        retRes.setStringValue("argnam", argument.getName());
                        retRes.setStringValue("altnam", argument.getAlias());
                        // If the data type is null then set it to UNKNOWN
                        if (argument.getDatatype() == null) {
                            retRes.setStringValue("argtyp", ArgType.UNKNOWN.toString());
                        }
                        else {
                            retRes.setStringValue("argtyp", argument.getDatatype().toString());
                        }
                        retRes.setStringValue("fixval", argument.getDefaultValue());
                        retRes.setIntValue("argidx", index);
                        retRes.setBooleanValue("argreq", argument.isRequired());
                    }
                    index++;
                }
            }
        }
        
        return retRes;
    }
    
    /**
     * This will list the given library versions for the given MocaContext.  By
     * default it will return all library versions available.  An optional
     * ComponentLibraryFilter can be specified which will allow a user to pick
     * a library by category.
     * @param ctx The Moca Context
     * @param filter The optional filter that can narrow the libraries returned
     * @return A MocaResults that contains all the information pertinent to
     *         the command arguments that passed the filter.
     *         <br> <b>Returned Columns:</b>
     *         <br> category - The component level name as a {@link java.lang.String}
     *         <br> library_name - The C library name associated with this 
     *         library as a {@link java.lang.String}
     *         <br> library_type - The type of library this is as a 
     *         {@link java.lang.String}  This can be Java, C, COM
     *         <br> package_name - The java package if provided for this library
     *         as a {@link java.lang.String} 
     *         <br> version - The compile date or version as a 
     *         {@link java.lang.String}
     *         <br> product - The product license this library falls under as a
     *         {@link java.lang.String}
     *         <br> licensed - Whether this library is licensed or not as a 
     *         {@link boolean}
     */
    public static MocaResults listLibraryVersions(MocaContext ctx, 
            ComponentLibraryFilter filter) {
        EditableResults retRes = ctx.newResults();
        
        retRes.addColumn("category", MocaType.STRING);
        retRes.addColumn("library_type", MocaType.STRING);
        retRes.addColumn("library_name", MocaType.STRING);
        retRes.addColumn("package_name", MocaType.STRING);
        retRes.addColumn("progid", MocaType.STRING);
        retRes.addColumn("sort_seq", MocaType.INTEGER);
        retRes.addColumn("version", MocaType.STRING);
        retRes.addColumn("product", MocaType.STRING);
        
        // If the ctx isn't our context object then don't allow it
        if (ctx instanceof DefaultServerContextAware) {
            DefaultServerContext me = ((DefaultServerContextAware) ctx)
                    .getDefaultServerContext();
            
            List<ComponentLevel> components = me._componentRepository.getLevels();
            
            for (ComponentLevel component : components) {
                
                if (filter != null) {
                    // If the filter is provided and the component didn't pass
                    // then go to the next one
                    if (!filter.accept(component)) {
                        continue;
                    }
                }
                
                retRes.addRow();
                
                retRes.setStringValue("category", component.getName());
                retRes.setStringValue("library_name", component.getLibrary());
                retRes.setStringValue("package_name", component.getPackage());
                retRes.setStringValue("progid", component.getProgid());
                retRes.setIntValue("sort_seq", component.getSortseq());
                retRes.setStringValue("version", component.getVersion());
                retRes.setStringValue("product", component.getProduct());
                
                String type = "????";
                
                if (component.getPackage() != null && 
                        !component.getPackage().trim().isEmpty()) {
                    type = "Java";
                }
                else if (component.getLibrary() != null && 
                        !component.getLibrary().trim().isEmpty()) {
                    type = "C";
                }
                else if (component.getProgid() != null && 
                        !component.getProgid().trim().isEmpty()) {
                    type = "COM";
                }
                
                retRes.setStringValue("library_type", type);
            }
        }
        
        return retRes;
    }
    
    /**
     * This will list the given levels for the given MocaContext.  By default
     * it will return all levels that are found.  An optional level name
     * can be specified which will allow a user to pick a level by name.
     * @param ctx The moca context to check for commands
     * @param levelName The level to return if specified
     * @return A MocaResults that contains all the information pertinent to
     *         the commands that passed the filter.
     *         <br> <b>Returned Columns:</b>
     *         <br> name - The component level name as a {@link java.lang.String}
     *         <br> desc - The component level description as a 
     *         {@link int}
     *         <br> srt_seq - The sort sequence of the component level as an 
     *         {@link int}
     *         <br> editable - Whether or not this component level is editable 
     *         as a {@link boolean}
     *         <br> cmd_dir - The directory which contains commands for this 
     *         component level as a {@link java.lang.String} 
     */
    public static MocaResults listLevels(MocaContext ctx, String levelName) {
        EditableResults retRes = ctx.newResults();
        
        retRes.addColumn("name", MocaType.STRING);
        retRes.addColumn("desc", MocaType.STRING);
        retRes.addColumn("srt_seq", MocaType.INTEGER);
        retRes.addColumn("editable", MocaType.BOOLEAN);
        retRes.addColumn("cmd_dir", MocaType.STRING);
        
        // If the ctx isn't our context object then don't allow it
        if (ctx instanceof DefaultServerContextAware) {
            DefaultServerContext me = 
                ((DefaultServerContextAware) ctx).getDefaultServerContext();
            
            // Now we actually get all the commands from the repository
            List<ComponentLevel> levels = me._componentRepository.getLevels();
            
            // Loop through each of them and check if it passes the filter
            // and then set the information on the result set
            for (ComponentLevel level : levels) {
                if (levelName == null || levelName.equals(level.getName())) {
                    retRes.addRow();
                    
                    retRes.setStringValue("name", level.getName());
                    retRes.setStringValue("desc", level.getDescription());
                    retRes.setIntValue("srt_seq", level.getSortseq());
                    retRes.setBooleanValue("editable", level.isEditable());
                    retRes.setStringValue("cmd_dir", level.getCmdDir());
                }
            }
        }
        
        return retRes;
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#getStatus()
    @Override
    public ServerContextStatus getCurrentStatus() {
        return _status;
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#setCurrentStatus(com.redprairie.moca.server.exec.ServerContextStatus)
    @Override
    public void setCurrentStatus(ServerContextStatus status) {
        _status = status;
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#getLastStatementInitiated()
    @Override
    public String getLastStatementInitiated() {
        return _previousStatement.peek();
    }
    
    /**
     * This will list the given triggers for the given MocaContext.  By default 
     * it will return all triggers available.  An optional TriggerFilter can be 
     * specified which will allow a user to pick a trigger by the name of the
     * command it is triggering off of
     * @param ctx The Moca Context
     * @param filter The optional filter that can narrow the triggers returned
     * @return A MocaResults that contains all the information pertinent to
     *         the triggers that passed the filter.
     *         <br> <b>Returned Columns:</b>
     *         <br> command - The actual name of the command the trigger will 
     *         fire off of as a {@link java.lang.String}
     *         <br> trgseq - The sequence the triggers will fire in as an 
     *         {@link int}
     *         <br> syntax - The syntax of the trigger as a 
     *         {@link java.lang.String}
     */
    public static MocaResults listTriggers(MocaContext ctx, TriggerFilter filter) {
        EditableResults retRes = ctx.newResults();
        
        retRes.addColumn("name", MocaType.STRING);
        retRes.addColumn("command", MocaType.STRING);
        retRes.addColumn("trgseq", MocaType.INTEGER);
        retRes.addColumn("syntax", MocaType.STRING);
        retRes.addColumn("enabled", MocaType.BOOLEAN);
        retRes.addColumn("filename", MocaType.STRING);
        
        // If the ctx isn't our context object then don't allow it
        if (ctx instanceof DefaultServerContextAware) {
            DefaultServerContext me = 
                ((DefaultServerContextAware) ctx).getDefaultServerContext();
            
            // Now we actually get all the triggers from the repository
            List<Trigger> triggers = me._componentRepository.getAllTriggers();
            
            for (Trigger trigger : triggers) {
                if (filter == null || filter.accept(trigger.getCommand())) {
                    retRes.addRow();
                    retRes.setStringValue("name", trigger.getName());
                    retRes.setStringValue("command", trigger.getCommand());
                    retRes.setIntValue("trgseq", trigger.getFireSequence());
                    retRes.setStringValue("syntax", trigger.getSyntax());
                    retRes.setBooleanValue("enabled", !trigger.isDisabled());
                    retRes.setStringValue("filename", trigger.getFileName());
                }
            }
        }
        
        return retRes;
    }
    
    /**
     * This will the component repository for the server
     * @param ctx The Moca Context
     * @return The component repository
     */
    public static CommandRepository getRepository(MocaContext ctx) {
        if (ctx instanceof DefaultServerContextAware) {
            DefaultServerContext me = 
                ((DefaultServerContextAware) ctx).getDefaultServerContext();
            
            return me._componentRepository;
        }
        
        return null;
    }
    
    private void _checkAuthorization(SecurityLevel required, Object command)
                throws MocaException{
        // Once we're executing a command that's defined in the system, we can
        // execute anything.
        if (_authStack > 0) {
            return;
        }
        
        // If no security is required, no need to check their session.
        if (required == SecurityLevel.OPEN) {
            return;
        }
        
        SecurityLevel authLevel = null;
        
        // If the session is authenticated, grab the security level from
        // that session token.
        SessionToken authToken = _session.getSessionToken();
        if (authToken != null) {
            authLevel = authToken.getSecurityLevel();
        }
        
        // Default to "open" security -- an unauthenticated session can
        // execute only unsecured commands.
        if (authLevel == null) authLevel = SecurityLevel.OPEN;
        
        // Three possibilities.  First, if security is OK, just return. If
        // there is no security token, the user needs to log in.  If there is
        // a security token, and it doesn't allow execution at the proper
        // level, throw an authorization exception.
        if (authLevel.compareTo(required) >= 0) {
            return;
        }
        else if (authLevel == SecurityLevel.OPEN) {
            throw new AuthenticationException(String.valueOf(command));
        }
        else {
            throw new AuthorizationException(String.valueOf(command));
        }
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#getSession()
    @Override
    public SessionContext getSession() {
        return _session;
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#getRequest()
    @Override
    public RequestContext getRequest() {
        return _request;
    }
    
    // @see java.lang.Object#finalize()
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
    
    private void checkStatus() {
        // First check if execution was interrupted, if so then throw our error
        if (Thread.interrupted()) {
            throw new MocaInterruptedException();
        }
        
        if (_closed) {
            throw new IllegalStateException("Context is closed.");
        }
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#getMessageResolver()
    @Override
    public MessageResolver getMessageResolver() {
        return _messageResolver;
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#getDatabaseTool()
    @Override
    public TransactionManager getTransactionManager() {
        return _transactionManager;
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#currentCommandPath()
    @Override
    public CommandPath currentCommandPath() {
        return _commandPath.get();
    }
    
    // @see com.redprairie.moca.server.exec.ServerContext#overrideCommand(java.lang.String, com.redprairie.moca.server.CommandInterceptor)
    @Override
    public void overrideCommand(String commandName,
        CommandInterceptor interceptor) {
        _overriddenCommands.put(commandName, interceptor);
    }

    // @see com.redprairie.moca.server.exec.ServerContext#clearOverriddenCommands()
    @Override
    public void clearOverriddenCommands() {
        _overriddenCommands.clear();
    }
    
    private class LocalConnection extends ServerSideConnection {

        // @see com.redprairie.moca.client.ServerSideConnection#executeCommandWithContext(java.lang.String, com.redprairie.moca.MocaArgument[], com.redprairie.moca.MocaArgument[])
        @Override
        public MocaResults executeCommandWithContext(String command,
                MocaArgument[] args, MocaArgument[] commandArgs)
                throws MocaException {
            MocaTransaction oldTransaction = _transactionStack.peekFirst();
            try {
                if (oldTransaction != null) {
                    oldTransaction.suspend();
                }
                if (_tx == null) {
                    _tx = _dbAdapter.newTransaction();
                }
                _tx.resume();
            }
            catch (SQLException e) {
                throw new MocaDBException(e);
            }
            _transactionStack.addFirst(_tx);
            Map<String, MocaValue> map = new HashMap<String, MocaValue>();
            if (commandArgs != null) {
                for (MocaArgument arg : commandArgs) {
                    map.put(arg.getName(), arg.getDataValue());
                }
            }
            try {
                // The Local Connection doesn't take any args directly.  To
                // maintain this this class must only be accessible through in
                // the DefaultServerContext.
                return DefaultServerContext.this._executeTranslate(command, 
                        map, true);
            }
            finally {
                // We remove the transaction directly, however our transaction
                // will be present in the remote 
                _transactionStack.remove(_tx);
                
                try {
                    _tx.suspend();
                    
                    if (oldTransaction != null) {
                        oldTransaction.resume();
                    }
                }
                catch (SQLException e) {
                    throw new MocaDBException(e);
                }
            }
        }
        
        private MocaTransaction _tx = null;
    }
    
    private static final String REMOTE_TX_ATTR = MocaTransactionManager.REMOTE_TX_ATTR;
    

    private static final Map<String, ScriptAdapter> _scriptAdapters = new HashMap<String, ScriptAdapter>();
    static {
//        _scriptAdapters.put("groovy", new GroovyScriptAdapter());
    }
    
    private static final Logger _logger = 
        LogManager.getLogger(DefaultServerContext.class);
    private static final Logger _serverLogger = 
        LogManager.getLogger("com.redprairie.moca.server");
    private static final Logger _argumentLogger =
        LogManager.getLogger("com.redprairie.moca.server.Argument");
    private static final Logger _sqlLogger = 
        LogManager.getLogger("com.redprairie.moca.server.db.Sql");
    private static final Logger _performanceLogger = 
        LogManager.getLogger("com.redprairie.moca.server.Performance");
    private static final Logger _managerLogger =
        LogManager.getLogger("com.redprairie.moca.server.Manager");
    private static final Logger _flowLogger = 
        LogManager.getLogger("com.redprairie.moca.server.Flow");
    
    private ServerContextStatus _status = ServerContextStatus.INACTIVE;
    private static final int _dataStackMaxSize = 500;

    private final RemoteConnectionFactory _connectionFactory;
    private final TransactionManager _transactionManager;
    private final ScriptAdapter _scriptAdapter;
    private final DBAdapter _dbAdapter;
    private final NativeAdapterFactory _nativePool;
    private final CommandRepository _componentRepository;
    private final SystemContext _sys;
    private final SessionContext _session;
    private final RequestContext _request;
    private final Collection<String> _blacklistedArgs;
    private final MessageResolver _messageResolver;
    
    /**
     * dataStackLevel holds the level of the stack as it is currently executing.
     * entireStackLevel holds the level of the stack, but is not reset for
     * srvInitiateCommand or MocaContext.executeCommand calls.
     * A -1 means that there is no command currently being executed, this state
     * will be shown as 0 in tracing.
     * A 0 means that we have received a command to parse and executed
     * Anything higher than 0 means that we are actively executing some kind of
     * command
     */
    private int _dataStackLevel = -1;
    
    /**
     * This is the locking mechanism for concurrent access to the data stack.
     * If any state of the data stack is to change you must lock on this object.
     * Also if any read operation requires ability to be called by another 
     * thread it must also lock on this.
     */
    private final Lock _dataStackLock = new ReentrantLock();

    /**
     * No one should directly iterate over this without using the 
     * {@link #_dataStackLevel} to tell what stack values are available for use
     */
    private final _DataStackElement[] _dataStack = 
        new _DataStackElement[_dataStackMaxSize];
    private final LinkedList<_CommandStackElement> _commandStack = 
        new LinkedList<_CommandStackElement>();
    private AtomicReference<CommandPath> _commandPath = 
        new AtomicReference<CommandPath>();
    private final LinkedList<MocaTransaction> _transactionStack = 
        new LinkedList<MocaTransaction>();
    private final LinkedList<NativeLibraryAdapter> _nativeAdapterStack = 
        new LinkedList<NativeLibraryAdapter>();
    private final LinkedList<String> _previousStatement = 
        new LinkedList<String>();
    private final MocaStackLevel _stackLevel = new MocaStackLevel();
    private final CommandUsage _cmdPerf;
    private int _authStack = 0;
    private NativeLibraryAdapter _nativeAdapter;
    private volatile boolean _closed = false;
    
    private final Map<String, CommandInterceptor> _overriddenCommands = 
        new HashMap<String, CommandInterceptor>();
    
    static final Pattern _whereClauseMatcher = Pattern.compile("where", 
            Pattern.CASE_INSENSITIVE);
}
