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

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Map;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.server.db.BindList;

/**
 * Adapter class between the legacy MOCA (C) code and the java server layer.
 * This interface essentially comprises the API for C code.
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public interface MocaServerAdapter extends MocaTraceAdapter {

    //
    // DBLIB API
    //
    
    /**
     * The equivalent of dbExecStr/dbExecBind functions.
     */
    public NativeReturnStruct executeSQL(String sqlStatement, BindList bindList, boolean autoBind,
                                  boolean ignoreResults) throws RemoteException;

    /**
     * Returns the database type as an integer. Values are defined as in existing C code.
     * 
     * @return Oracle = 1, MS SQL Server = 2, Other = 4.
     */
    public int getDBType() throws RemoteException;
    
    /**
     * Rolls back the current database transaction. This call has no effect on
     * other MOCA transactional features, such as transaction hooks, remote
     * commands, etc.
     * 
     * @throws SQLException
     */
    public void rollbackDB() throws SQLException, RemoteException;

    /**
     * Sets up a database savepoint with the given name.
     * 
     * @param savepoint the name of the savepoint to be created.
     * @throws SQLException
     */
    public void setSavepoint(String savepoint) throws SQLException, RemoteException;
    
    /**
     * Rolls back the database to the named savepoint.
     * @param savepoint
     * @throws SQLException
     */
    public void rollbackDBToSavepoint(String savepoint) throws SQLException, RemoteException;
    
    /**
     * Commits the current database transaction.  This call has no effect on
     * other MOCA transactional features, such as transaction hooks, remote
     * commands, etc.
     * 
     * @throws SQLException
     */
    public void commitDB() throws SQLException, RemoteException;
    
    //
    // OSLIB API
    //

    /**
     * Lookup a registry value in the system configuration. 
     * 
     * @param key the registry key to look up.
     * @param expand if true, environment variables found in the registry keys
     *            will be expanded according to Unix and Windows rules for
     *            environment variable expansion.
     * @return the variable value as a string, or <code>null</code> if the
     *         registry value is not present.
     */
    public String getRegistry(String key, boolean expand) throws RemoteException;
    
    /**
     * Looks up an environment variable in the system configuration or execution
     * context. Client requests can come in with overridden environment
     * variables, so the runtime-modified environment is queried, as well as the
     * overall system configuration and actual OS environment variables.
     * 
     * @param name the name of the environment variable to check. Note that on
     *            operating systems where environment variables are
     *            case-sensitive, the name is converted to upper case before
     *            lookup. That make cause surprising results.
     * @return the value of the environment variable.
     */
    public String getEnvironment(String name) throws RemoteException;

    /**
     * Sets an environment variable in the system configuration or execution
     * context.
     * 
     * @param name the name of the environment variable to set.
     * @param value the value of the environment variable to set.
     */
    public void putEnvironment(String name, String value) throws RemoteException;
    

    /**
     * Removes an environment variable from the system configuration or execution
     * context.
     * 
     * @param name the name of the environment variable to remove.
     */
    public void removeEnvironment(String name) throws RemoteException;
    
    //
    // SRVLIB API
    //
    
    /**
     * Execute a command in this MOCA server context. If <code>clearStack</code>
     * is passed as <code>true</code>, the existing variable stack is reset to
     * empty for this command execution. Otherwise, the stack remains as is.
     * 
     * @param command a command to execute in valid MOCA command syntax
     * @param args arguments to pass along to the execution engine when executing
     *            this command.  This parameter can be null, meaning no arguments.
     * @param clearStack indicates whether to start out with a clean stack or to
     *            use the current one.
     * @return a <code>NativeReturnStruct</code> object containing the results 
     *            of the command.
     */
    public NativeReturnStruct executeCommand(String command, 
                                      Map<String, Object> args,
                                      boolean clearStack) throws RemoteException;

    /**
     * Looks up a variable in the current runtime stack. The stack will be
     * queried for the primary name as well as the alias.
     * 
     * @param name the name of the variable to look up.
     * @param alias an alternate name to look up.
     * @param equalsOnly don't check all operator types -- only equals
     * @return an argument object corresponding to the named variable element,
     *         or <code>null</code> if the variable is not present on the stack.
     */
    public MocaArgument getStackElement(String name, String alias, boolean equalsOnly) throws RemoteException;

    /**
     * Returns the argument list for the currently executing command.
     * 
     * @param getAll if <code>true</code>, all arguments are returned.
     *            Otherwise, only unused (unreferenced) arguments are returned.
     * @return an array of {@link MocaArgument} elements corresponding to the
     *         currently executing command's argument list (where clause).
     */
    public MocaArgument[] getStackArgs(boolean getAll) throws RemoteException;

    /**
     * Commits the MOCA transaction.
     * 
     * @throws MocaException if an error occurred preventing the commit from
     *             occurring. If an exception is thrown from this method, the
     *             transaction has not been committed, although it's possible
     *             that a partial commit took place.
     */
    public void commitTx() throws MocaException, RemoteException;

    /**
     * Rolls back the MOCA transaction.
     * 
     * @throws MocaException if an error occurred preventing the rollback from
     * occurring.
     */
    public void rollbackTx() throws MocaException, RemoteException;
    
    /**
     * Returns the next value for a given sequence.
     * 
     * @param name the sequence name
     * @return the next sequence value
     * @throws MocaException if an error occurred obtaining the next sequence
     *             value, or there's no sequence in the database of the given
     *             name.
     */
    public String getNextSequenceValue(String name) throws MocaException, RemoteException;

    
    /**
     * This method is used to perform message translation of error message IDs,
     * as well as other message lookup capabilities.
     * @param lookupId The message identifier to be looked up
     * @return The translated message if there is one, or <code>null</code> if
     * no translated message exists.
     */
    public String translateMessage(String lookupId);

}