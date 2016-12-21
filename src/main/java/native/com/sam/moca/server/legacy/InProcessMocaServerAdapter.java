/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.sam.moca.server.legacy;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Map;

import com.sam.moca.EditableResults;
import com.sam.moca.MocaArgument;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.SimpleResults;
import com.sam.moca.server.db.BindList;
import com.sam.moca.util.ResultUtils;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public class InProcessMocaServerAdapter implements MocaServerAdapter {
    /**
     * 
     */
    public InProcessMocaServerAdapter(MocaServerAdapter delegate) {
        _delegate = delegate;
    }


    /**
     * @throws SQLException
     * @throws RemoteException
     * @see com.sam.moca.server.legacy.MocaServerAdapter#commitDB()
     */
    public void commitDB() throws SQLException, RemoteException {
        _delegate.commitDB();
    }


    /**
     * @throws MocaException
     * @throws RemoteException
     * @see com.sam.moca.server.legacy.MocaServerAdapter#commitTx()
     */
    public void commitTx() throws MocaException, RemoteException {
        try {
            _delegate.commitTx();
        }
        catch (MocaException e) {
            throw castException(e);
        }
    }


    /**
     * @param command
     * @param clearStack
     * @return
     * @throws RemoteException
     * @see com.sam.moca.server.legacy.MocaServerAdapter#executeCommand(java.lang.String, Map, boolean)
     */
    public NativeReturnStruct executeCommand(String command, 
            Map<String, Object> args, boolean clearStack) throws RemoteException {
        if (args != null) {
            for (Map.Entry<String, Object> arg : args.entrySet()) {
                // Make a results copy of any results values we see.  We can't deal with WrappedResults.
                if (arg.getValue() instanceof WrappedResults) {
                    EditableResults res = new SimpleResults();
                    ResultUtils.copyResults(res, (MocaResults)arg.getValue());
                    arg.setValue(res);
                }
            }
        }
        
        NativeReturnStruct retStruct = _delegate.executeCommand(command, 
                args, clearStack);
        
        // We have to make sure to convert all the result sets to 
        // WrappedResults
        return new NativeReturnStruct(retStruct.getErrorCode(), 
                castResults(retStruct.getResults(), true), retStruct.getMessage(), retStruct.isMessageResolved(),
                retStruct.getArgs(), retStruct.getBindList());
    }

    
    
    /**
     * @param sqlStatement
     * @param bindList
     * @param autoBind
     * @param ignoreResults
     * @return
     * @throws RemoteException
     * @see com.sam.moca.server.legacy.MocaServerAdapter#executeSQL(java.lang.String, com.sam.moca.server.db.BindList, boolean, boolean)
     */
    public NativeReturnStruct executeSQL(String sqlStatement, BindList bindList, 
            boolean autoBind, boolean ignoreResults) throws RemoteException {
        NativeReturnStruct retStruct = _delegate.executeSQL(sqlStatement, 
                bindList, autoBind, ignoreResults);
        return new NativeReturnStruct(retStruct.getErrorCode(), 
                castResults(retStruct.getResults(), true), retStruct.getMessage(), retStruct.isMessageResolved(),
                retStruct.getArgs(), retStruct.getBindList());
    }

    /**
     * @return
     * @throws RemoteException
     * @see com.sam.moca.server.legacy.MocaServerAdapter#getDBType()
     */
    public int getDBType() throws RemoteException {
        return _delegate.getDBType();
    }


    /**
     * @param name the name of the environment variable
     * @return the value of the environment variable
     * @throws RemoteException
     * @see com.sam.moca.server.legacy.MocaServerAdapter#getEnvironment(java.lang.String)
     */
    public String getEnvironment(String name) throws RemoteException {
        return _delegate.getEnvironment(name);
    }


    /**
     * @param name the name of the environment variable
     * @param value the value of the environment variable
     * @throws RemoteException
     * @see com.sam.moca.server.legacy.MocaServerAdapter#putEnvironment(java.lang.String, java.lang.String)
     */
    public void putEnvironment(String name, String value) throws RemoteException {
        _delegate.putEnvironment(name, value);
    }
    
    
    /**
     * @param name the name of the environment variable
     * @throws RemoteException
     * @see com.sam.moca.server.legacy.MocaServerAdapter#removeEnvironment(java.lang.String)
     */
    public void removeEnvironment(String name) throws RemoteException {
        _delegate.removeEnvironment(name);
    }
    
    
    /**
     * @param name
     * @return
     * @throws MocaException
     * @throws RemoteException
     * @see com.sam.moca.server.legacy.MocaServerAdapter#getNextSequenceValue(java.lang.String)
     */
    public String getNextSequenceValue(String name) throws MocaException, RemoteException {
        try {
            return _delegate.getNextSequenceValue(name);
        }
        catch (MocaException e) {
            throw castException(e);
        }
    }


    /**
     * @param section
     * @param key
     * @param expand
     * @return
     * @throws RemoteException
     * @see com.sam.moca.server.legacy.MocaServerAdapter#getRegistry(java.lang.String, java.lang.String, boolean)
     */
    public String getRegistry(String key, boolean expand) throws RemoteException {
        return _delegate.getRegistry(key, expand);
    }


    /**
     * @param getAll
     * @return
     * @throws RemoteException
     * @see com.sam.moca.server.legacy.MocaServerAdapter#getStackArgs(boolean)
     */
    public MocaArgument[] getStackArgs(boolean getAll) throws RemoteException {
        return _delegate.getStackArgs(getAll);
    }


    /**
     * @param name
     * @param alias
     * @return
     * @throws RemoteException
     * @see com.sam.moca.server.legacy.MocaServerAdapter#getStackElement(java.lang.String, java.lang.String, boolean)
     */
    public MocaArgument getStackElement(String name, String alias, boolean equalsOnly) throws RemoteException {
        return _delegate.getStackElement(name, alias, equalsOnly);
    }


    /**
     * @param level
     * @param message
     * @throws RemoteException
     * @see com.sam.moca.server.legacy.MocaServerAdapter#log(int, java.lang.String)
     */
    public void log(int level, String message) throws RemoteException {
        _delegate.log(level, message);
    }


    /**
     * @throws SQLException
     * @throws RemoteException
     * @see com.sam.moca.server.legacy.MocaServerAdapter#rollbackDB()
     */
    public void rollbackDB() throws SQLException, RemoteException {
        _delegate.rollbackDB();
    }


    /**
     * @param savepoint
     * @throws SQLException
     * @throws RemoteException
     * @see com.sam.moca.server.legacy.MocaServerAdapter#rollbackDBToSavepoint(java.lang.String)
     */
    public void rollbackDBToSavepoint(String savepoint) throws SQLException, RemoteException {
        _delegate.rollbackDBToSavepoint(savepoint);
    }


    /**
     * @throws MocaException
     * @throws RemoteException
     * @see com.sam.moca.server.legacy.MocaServerAdapter#rollbackTx()
     */
    public void rollbackTx() throws MocaException, RemoteException {
        try {
            _delegate.rollbackTx();
        }
        catch (MocaException e) {
            throw castException(e);
        }
    }


    /**
     * @param savepoint
     * @throws SQLException
     * @throws RemoteException
     * @see com.sam.moca.server.legacy.MocaServerAdapter#setSavepoint(java.lang.String)
     */
    public void setSavepoint(String savepoint) throws SQLException, RemoteException {
        _delegate.setSavepoint(savepoint);
    }


    /**
     * @param level
     * @param message
     * @throws RemoteException
     * @see com.sam.moca.server.legacy.MocaServerAdapter#trace(int, java.lang.String)
     */
    public void trace(int level, String message) throws RemoteException {
        _delegate.trace(level, message);
    }
    
    // @see com.sam.moca.server.legacy.MocaServerAdapter#setTraceFileName(java.lang.String, boolean)
    @Override
    public void setTraceFileName(String fileName, boolean append)
            throws RemoteException {
        _delegate.setTraceFileName(fileName, append);
    }
    
    // @see com.sam.moca.server.legacy.MocaTraceAdapter#getTraceLevel()
    @Override
    public int getTraceLevel() throws RemoteException {
        return _delegate.getTraceLevel();
    }

    // @see com.sam.moca.server.legacy.MocaTraceAdapter#setTraceLevel(int)
    @Override
    public void setTraceLevel(int level) throws RemoteException {
        _delegate.setTraceLevel(level);
    }
    
    // @see com.sam.moca.server.legacy.MocaServerAdapter#translateErrorMessage(int)
    @Override
    public String translateMessage(String lookupId) {
        return _delegate.translateMessage(lookupId);
    }
    
    private WrappedResults castResults(MocaResults res, boolean allocateNulls) {
        if (res == null || res instanceof WrappedResults) {
            return (WrappedResults) res;
        }
        else {
            WrappedResults out = new WrappedResults(res, allocateNulls);
            res.close();
            return out;
        }
    }
    
    private MocaException castException(MocaException e) throws MocaException {
        MocaResults res = e.getResults();
        if (res != null && !(res instanceof WrappedResults)) {
            e.setResults(castResults(res, true));
        }
        return e;
    }

    private final MocaServerAdapter _delegate;
    
    static {
        NativeReturnStruct.setResultsClass(WrappedResults.class);
    }
}
