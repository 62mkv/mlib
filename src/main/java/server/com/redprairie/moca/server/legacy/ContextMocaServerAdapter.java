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

package com.redprairie.moca.server.legacy;

import java.sql.SQLException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaException.Args;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.exceptions.MocaDBException;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.DBType;
import com.redprairie.moca.server.exec.JavaVariableContext;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.log.TraceUtils;
import com.redprairie.moca.server.profile.CommandPath;
import com.redprairie.moca.util.MocaUtils;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public class ContextMocaServerAdapter implements MocaContextServerAdapter {
    
    public ContextMocaServerAdapter(ServerContext ctx) {
        _ctx = ctx;
    }

    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#commitDB()

    @Override
    public void commitDB() throws SQLException {
        _ctx.commitDB();
    }

    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#commitTx()

    @Override
    public void commitTx() throws MocaException {
        _ctx.commit();

    }

    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#executeCommand(java.lang.String, boolean)
    @Override
    public NativeReturnStruct executeCommand(String command, 
            Map<String, Object> args, boolean clearStack) {
        MocaResults res;
        try {
            ServerContext context = ServerUtils.getCurrentContext();
            if (context == null) {
                // We have to set the current context in case if this was call
                // say from another thread spawned by COM when running in the
                // same process.
                ServerUtils.setCurrentContext(_ctx);
            }
            
            if (clearStack) {
                res = _ctx.executeCommand(command, args, false);
            }
            else {
                res = _ctx.executeCommand(command, args, true);
            }
        }
        catch (NotFoundException e) {
            // Not really an error -- don't mess up the logs
            _logger.debug("Throwing NotFoundException");
            return new NativeReturnStruct(e);
        }
        catch (MocaException e) {
            // This mimics what the Dispatcher does on an exception
            _logger.debug(MocaUtils
                    .concat("Exception raised from command: ", e), e);
            return new NativeReturnStruct(e);
        }
        return new NativeReturnStruct(res);
    }

    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#executeSQL(java.lang.String, com.redprairie.moca.server.db.BindList, boolean, boolean)
    @Override
    public NativeReturnStruct executeSQL(String sqlStatement, BindList bindList, 
            boolean autoBind, boolean ignoreResults) {
        _logger.debug(MocaUtils.concat("Executing SQL from native code: ", 
                sqlStatement));
        MocaResults res;
        try {
            res = _ctx.executeSQL(sqlStatement, bindList, autoBind, ignoreResults);
            return new NativeReturnStruct(res, bindList);
        }
        catch (MocaException e) {
            return new NativeReturnStruct(e);
        }
        catch (SQLException e) {
            // We have to convert the SQL error codes to be negative or to be
            // Unknown if it was an error code zero.
            int status = e.getErrorCode();
            if (status == 0) {
                status = MocaDBException.UNKNOWN_ERROR_CODE;
            }
            else if (status > 0) {
                status = -status;
            }
            
            return new NativeReturnStruct(status, new SimpleResults(), 
                    e.getMessage(), false, new Args[0], null);
        }
    }

    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#getDBType()

    @Override
    public int getDBType() {
        DBType type = _ctx.getDbType();
        switch (type) {
            case ORACLE: return 1;
            case MSSQL: return 2;
            default: return 0;
        }
    }

    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#getEnvironment(java.lang.String)

    @Override
    public String getEnvironment(String name) {
        return _ctx.getSystemVariable(name);
    }

    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#putEnvironment(java.lang.String, java.lang.String)

    @Override
    public void putEnvironment(String name, String value) {
        if (_ctx instanceof JavaVariableContext) {
            ((JavaVariableContext) _ctx).putJavaSystemVariable(name, value);
        }
        else {
            _ctx.putSystemVariable(name, value);
        }
    }
    
    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#removeEnvironment(java.lang.String)

    @Override
    public void removeEnvironment(String name) {
        _ctx.removeSystemVariable(name);
    }
    
    
    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#getRegistry(java.lang.String, java.lang.String)

    @Override
    public String getRegistry(String key, boolean expand) {
        return _ctx.getRegistryValue(key, expand);
    }

    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#getStackArgs()
    @Override
    public MocaArgument[] getStackArgs(boolean getAll) {
        return _ctx.getCommandArgs(getAll, false);
    }

    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#getStackElement(java.lang.String, java.lang.String)

    @Override
    public MocaArgument getStackElement(String name, String alias, boolean equalsOnly) {
        MocaArgument element = _ctx.getVariableAsArgument(name, false, false);
        if (element == null && alias != null) {
            element = _ctx.getVariableAsArgument(alias, false, false);
        }
        return element;
    }

    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#log(int, java.lang.String)

    @Override
    public void log(int level, String message) {
        switch (level) {
        case 1:
            _logger.debug(message);
            break;
        case 2:
            _logger.info(message);
            break;
        case 3:
            _logger.warn(message);
            break;
        case 4:
            _logger.error(message);
            break;
        }
    }

    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#rolbackTx()

    @Override
    public void rollbackTx() throws MocaException {
        _ctx.rollback();
    }

    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#rollbackDB()

    @Override
    public void rollbackDB() throws SQLException {
        _ctx.rollbackDB();
    }

    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#rollbackDB(java.lang.String)

    @Override
    public void rollbackDBToSavepoint(String savepoint) throws SQLException {
        _ctx.rollbackDB(savepoint);
    }
    
    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#setSaveoint(java.lang.String)
    @Override
    public void setSavepoint(String savepoint) throws SQLException {
        _ctx.setSavepoint(savepoint);
        
    }
    
    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#getNextSequenceValue(java.lang.String)
    
    @Override
    public String getNextSequenceValue(String name) throws MocaException {
        return _ctx.getNextSequenceValue(name);
    }

    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#trace(int, java.lang.String)

    @Override
    public void trace(int level, String message) {
        MocaContext mocaContext = _ctx.getComponentContext();
        mocaContext.trace(level, message);
    }

    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#setTraceFileName(java.lang.String, boolean)
    @Override
    public void setTraceFileName(String fileName, boolean append) {
        _ctx.getComponentContext().setTraceFile(fileName, append);
    }
    
    // @see com.redprairie.moca.server.legacy.MocaTraceAdapter#getTraceLevel()
    @Override
    public int getTraceLevel() {
        int level = TraceUtils.getSessionTraceLevel();
        if (level <= 0) {
            level = TraceUtils.getGlobalTraceLevel();
        }
        return level;
    }

    // @see com.redprairie.moca.server.legacy.MocaTraceAdapter#setTraceLevel(int)
    @Override
    public void setTraceLevel(int level) {
        _ctx.getComponentContext().setTraceLevel(level);
    }
    
    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#translateErrorMessage(int)
    @Override
    public String translateMessage(String lookupId) {
        String message = _ctx.getMessageResolver().getMessage(lookupId);
        return message;
    }
    
    // @see com.redprairie.moca.server.legacy.MocaServerAdapter#getCurrentCommandPath()
    @Override
    public CommandPath getCurrentCommandPath() {
        return _ctx.currentCommandPath();
    }
    
    //
    // Implementation
    //
    private final ServerContext _ctx;
    private static transient Logger _logger = LogManager.getLogger(
            ContextMocaServerAdapter.class);
}
