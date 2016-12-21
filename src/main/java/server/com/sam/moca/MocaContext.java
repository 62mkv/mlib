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

package com.sam.moca;

import java.sql.Connection;
import java.util.Map;

import javax.transaction.xa.XAResource;



/**
 * Class used to access the current running MOCA execution context.  This
 * class should not be instantiated directly, but should be acquired by a
 * component when it is invoked by the MOCA engine.
 * 
 * The MOCA execution context is used to initiate commands, access data
 * elements from the stack, and access system (environment) variables.
 * 
 * <code><pre>
 *     MocaContext ctx = MocaUtils.currentContext();
 *     String optional = (String) ctx.getVariable("optional");
 *     MocaResults res;
 *     try {
 *         res = ctx.initiateCommand("some command");
 *         while (res.next()) {
 *             String columnValue = res.getStringValue("column");
 *         }
 *     }
 *     finally {
 *         if (res != null) res.close();
 *     }
 * </pre></code>
 * 
 * @see com.sam.moca.util.MocaUtils#currentContext
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
public interface MocaContext
{
    /**
     * Acquire the JDBC connection associated with the current MOCA context.
     * @return an instance of <code>java.sql.Connection</code>.
     * 
     * This method is a convenience method for calling 
     * {@link DatabaseTool#getConnection()} so that the 
     * caller doesn't need to retrieve another instance to invoke the method.
     * 
     * @see DatabaseTool#getConnection()
     */
    public Connection getConnection();
    
    /**
     * Returns the database type as a string.
     * 
     * This method is a convenience method for calling 
     * {@link DatabaseTool#getDbType()} so that the 
     * caller doesn't need to retrieve another instance to invoke the method.
     * 
     * @see DatabaseTool#getDbType()
     */
    public String getDbType();
    
    /**
     * Gets a context variable from the current MOCA context.
     * @param name the name of the variable to retreive.
     * @return an object representing the value of the named variable in
     * the current MOCA context.
     * @deprecated Please use the {@link MocaContext#getStackVariable(String)}
     *             method instead.  Type conversion can be done on the 
     *             resultant object.  Also this way null can be differentiated
     *             between not being present and actually being null.
     */
    @Deprecated
    public Object getVariable(String name);
    
    /**
     * Returns a boolean value indicating if the named variable is present in the current
     * MOCA context.  If the variable is avaiable, but is null, this method will return true. 
     * @param name the name of the variable to check.
     * @return <code>true</code> if the variable <code>name</code> is available in the context.
     * @deprecated Please use the {@link MocaContext#getStackVariable(String)}
     *             method instead.  If it is not null it is equivalent to being
     *             available.
     */
    @Deprecated
    public boolean isVariableAvailable(String name);
    
    
    /**
     * Returns a {@link MocaValue} instance for the given stack variable. If the variable is not
     * available on the stack, <code>null</code> is returned.
     * @param name
     * @return
     */
    public MocaValue getStackVariable(String name);
    
    /**
     * Returns a {@link MocaValue} instance for the given stack variable. If the variable is not
     * available on the stack, <code>null</code> is returned.
     * @param name
     * @param markAsUsed
     * @return
     */
    public MocaValue getStackVariable(String name, boolean markAsUsed);
    
    /**
     * Gets a system variable from the current MOCA context.  System variables
     * are either environment variables, or variables set by the client
     * environment context.
     * @param name the name of the variable to retreive.
     * @return a String representing the value of the named variable in
     * the current MOCA system environment.
     */
    public String getSystemVariable(String name);

    /**
     * Puts a system variable into the current MOCA context.  System variables
     * are either environment variables, or variables set by the client
     * environment context.
     * @param name the name of the variable to put.
     * @param value the value of the variable to put.
     */
    public void putSystemVariable(String name, String value);
    
    /**
     * Removes a system variable from the current MOCA context.  System variables
     * are either environment variables, or variables set by the client
     * environment context.
     * @param name the name of the variable to remove.
     */
    public void removeSystemVariable(String name);
    
    /**
     * Look up configuration (registry) entries.  The MOCA registry is used to 
     * hold server configuration information.
     * @param key the registry key to look up.
     * @return the value stored in the registry associated with the given key.
     */
    public String getRegistryValue(String key);
    
    /**
     * Look up configuration (registry) entries. The MOCA registry is used to
     * hold server configuration information.
     *
     * @param key the registry key to look up.
     * @param expland <code>true</code> if the any environment variable
     *                references within the registry value should be expanded.
     * @return the value stored in the registry associated with the given key.
     */
    public String getRegistryValue(String key, boolean expand);
    
    /**
     * This allows for the manual enlistment of an XAResource from a component.
     * This resource will take part of the global transaction and will be done
     * atomically with the database transaction and any other transactions.
     * <p>
     * This can be usefull especially for tools that cannot have a transaction
     * manager plugged into them such as JMS providers.  This way you can
     * enlist the JMS based XAResource to your transaction so the message
     * is only properly consumed or sent upon commit of the transaction or 
     * undone on a rollback.
     * @param resource The resource to enlist
     * @throws MocaException This is thrown if there is a problem enlisting
     *         the transaction
     */
    public void enlistResource(XAResource resource) throws MocaException;
    
    /**
     * Execute a command in this MOCA context.  The existing variable stack
     * is reset to empty for this command execution.
     * 
     * @param command a command to execute in valid MOCA command syntax
     * @return a <code>MocaResults</code> object containing the results of
     * the command.
     * @throws MocaException if the command threw an exception, or if the
     * MOCA system was unable to process the command.
     */
    public MocaResults executeCommand(String command) throws MocaException;
    
    /**
     * Execute a command in this MOCA context.  The existing variable stack
     * is retained for this command execution.
     * 
     * @param command a command to execute in valid MOCA command syntax
     * @return a <code>MocaResults</code> object containing the results of
     * the command.
     * @throws MocaException if the command threw an exception, or if the
     * MOCA system was unable to process the command.
     */
    public MocaResults executeInline(String command) throws MocaException;

    /**
     * Execute a command with a passed-in starting context.  This is useful
     * for giving MOCA a starting set of arguments to use to create an initial
     * context. The existing variable stack is reset to empty for this command
     * execution, and is replaced with the passed-in arguments.
     * 
     * @param command a command to execute
     * @param args a list of arguments to be passed along to the executing command.
     * If this parameter is <code>null</code>, no initial argument list is passed.
     * @return a <code>MocaResults</code> object containing the results of
     * the command.
     * @throws MocaException if the command threw an exception, or if the
     * MOCA system was unable to process the command.
     */
    public MocaResults executeCommand(String command, Map<String, ?> args)
            throws MocaException;
    
    /**
     * Execute a command with a passed-in starting context.  This is useful
     * for giving MOCA a starting set of arguments to use to create an initial
     * context. The existing variable stack is retained for this command execution,
     * and is augmented by the passed-in arguments.
     * 
     * @param command a command to execute
     * @param args a list of arguments to be passed along to the executing command.
     * If this parameter is <code>null</code>, no initial argument list is passed.
     * @return a <code>MocaResults</code> object containing the results of
     * the command.
     * @throws MocaException if the command threw an exception, or if the
     * MOCA system was unable to process the command.
     */
    public MocaResults executeInline(String command, Map<String, ?> args)
            throws MocaException;
    
    /**
     * Execute a command with a passed-in starting context.  This is useful
     * for giving MOCA a starting set of arguments to use to create an initial
     * context. The existing variable stack is reset to empty for this command
     * execution, and is replaced with the passed-in arguments.
     * 
     * @param command a command to execute
     * @param args a list of arguments to be passed along to the executing command.
     * If this parameter is <code>null</code>, no initial argument list is passed.
     * @return a <code>MocaResults</code> object containing the results of
     * the command.
     * @throws MocaException if the command threw an exception, or if the
     * MOCA system was unable to process the command.
     */
    public MocaResults executeCommand(String command, MocaArgument... args)
            throws MocaException;
    
    /**
     * Execute a command with a passed-in starting context.  This is useful
     * for giving MOCA a starting set of arguments to use to create an initial
     * context. The existing variable stack is retained for this command execution,
     * and is augmented by the passed-in arguments.
     * 
     * @param command a command to execute
     * @param args a list of arguments to be passed along to the executing command.
     * If this parameter is <code>null</code>, no initial argument list is passed.
     * @return a <code>MocaResults</code> object containing the results of
     * the command.
     * @throws MocaException if the command threw an exception, or if the
     * MOCA system was unable to process the command.
     */
    public MocaResults executeInline(String command, MocaArgument... args)
            throws MocaException;
    
    /**
     * Log an error to the MOCA logger.
     * @param text the text of the error to send to the logger.
     */
    @Deprecated
    public void logError(String text);
    
    /**
     * Log a warning to the MOCA logger.
     * @param text the text of the warning to send to the logger.
     */
    @Deprecated
    public void logWarning(String text);
    
    /**
     * Log a message to the MOCA logger.
     * @param text the text of the message to send to the logger.
     */
    @Deprecated
    public void logInfo(String text);
    
    /**
     * Log a debug message to the MOCA logger.  The use of this method
     * is discouraged in favor of using the MOCA trace facility.
     * @param text the text of the message to send to the logger.
     */
    @Deprecated
    public void logDebug(String text);
    
    /**
     * TODO What is this used for?
     * @param text the text of the message to send to the logger.
     */
    @Deprecated
    public void logUpdate(String text);
    
    /**
     * Write trace information to the trace facility.  Most applications
     * will trace at the {@link MocaTrace#FLOW} level.
     * @param level one of the constants defined in the {@link MocaTrace} class.
     * @param text the text of the message to be traced.
     * @see MocaTrace
     */
    public void trace(int level, String text);
    
    /**
     * Write trace information to the trace facility, using the {@link MocaTrace#FLOW}
     * trace level.
     * @param text the text of the message to be traced.
     * @see MocaTrace
     */
    public void trace(String text);
    
    /**
     * Indicates if tracing is enabled at a particular level.  This method can
     * be used to avoid producing excessive trace messages, for cases where they
     * are expensive to produce.
     * 
     * @param level one of the constants defined in the {@link MocaTrace} class.
     * @return <code>true</code> if tracing is turned on for <code>level</code>,
     * <code>false</code> otherwise.
     * @see MocaTrace
     */
    public boolean traceEnabled(int level);
    
    /**
     * Sets the current trace level to the level specified by
     * <code>level</code>.  <code>level</code> can be a bitwise OR of different
     * levels defined in the MocaTrace enumeration.  Further trace messages of
     * the level(s) specified will be output to the trace file/device.
     *  
     * @param level the trace level(s) to enable.  Zero disables all tracing.
     */
    public void setTraceLevel(int level);
    
    /**
     * Sets the current trace level to the level specified by the string
     * <code>level</code>.  <code>level</code> should be a string of characters
     * that are recognized by the MOCA trace command line option processing.
     * To enable all known trace levels, use the value "*".
     * 
     * With the new tracing changes, any non null, non empty string
     * will result in a <code>*</code> trace level.
     *  
     * @param level the trace level(s) to enable.  Null or the empty string
     * disables all tracing.  "*" enables all tracing. 
     */
    public void setTraceLevel(String level);
    
    /**
     * Opens a trace file for tracing.  The file will be opened and truncated
     * after calling this method.  if the filename specifies a file that cannot
     * be opened, due to permission or directory problems, this method will
     * silently fail, and trace output will go the the standard output device
     * of the current process.
     * 
     * @param filename the name of the file to be used for tracing.  
     */
    public void setTraceFile(String filename);
    
    /**
     * Opens a trace file for tracing.  If the <append> argument is
     * <code>true</code>, the file will be appended to.  Otherwise, it will be
     * opened and truncated.  if the filename specifies a file that cannot
     * be opened, due to permission or directory problems, this method will
     * silently fail, and trace output will go the the standard output device
     * of the current process.
     * 
     * @param filename the name of the file to be used for tracing.
     */
    public void setTraceFile(String filename, boolean append);

    /**
     * Removes the named attribute from the current transaction.  When the
     * current transaction is completed (either via commit or rollback), all
     * objects associated with the current transaction are removed from the
     * context.  This method allows for their removal before the end of the
     * transaction.
     * 
     * @param name the name of the attribute to remove.  This argument
     * cannot be null.
     */
    public void removeTransactionAttribute(String name);

    /**
     * Gets the named attribute from the current transaction. After the
     * current transaction is completed (either via commit or rollback), all
     * objects associated with the current transaction are removed from the
     * context.  
     * 
     * @param name the name of the transaction attribute to get. This argument
     * cannot be null.
     * @return the value of the transaction attribute with the given name,
     * or <code>null</code> if there is no attribute with that name. 
     */
    public Object getTransactionAttribute(String name);

    /**
     * Sets a named attribute in the current transaction. After the
     * current transaction is completed (either via commit or rollback), all
     * objects associated with the current transaction are removed from the
     * context.
     * 
     * @param name the name of the transaction attribute to set. This argument
     * cannot be null.
     * @param value the value of the transaction attribute to associate
     * with the given name. 
     */
    public void setTransactionAttribute(String name, Object value);


    /**
     * Removes the named attribute from the current session. Session values are
     * retained across MOCA requests.
     * 
     * @param name the name of the attribute to remove. This argument cannot be
     *            null.
     */
    public void removeSessionAttribute(String name);

    /**
     * Gets the named attribute from the current session. Session attributes are
     * retained across MOCA requests.
     * 
     * @param name the name of the session attribute to get. This argument
     *            cannot be null.
     * @return the value of the session attribute with the given name, or
     *         <code>null</code> if there is no attribute with that name.
     */
    public Object getSessionAttribute(String name);

    /**
     * Sets a named attribute in the current session. After the current session
     * is closed (either via an active close or a timeout event), all objects
     * associated with the current session are eligible for garbage collection.
     * 
     * @param name the name of the session attribute to set. This argument
     *            cannot be null.
     * @param value the value of the session attribute to associate with the
     *            given name.
     */
    public void setSessionAttribute(String name, Object value);

    /**
     * Removes the named attribute from the current request context. Request values are
     * retained for the duration of a MOCA request unless removed.
     * 
     * @param name the name of the attribute to remove. This argument cannot be
     *            null.
     */
    public void removeRequestAttribute(String name);

    /**
     * Gets the named attribute from the current request context. Request attributes are
     * retained for the duration of a MOCA request.
     * 
     * @param name the name of the request attribute to get. This argument
     *            cannot be null.
     * @return the value of the request attribute with the given name, or
     *         <code>null</code> if there is no attribute with that name.
     */
    public Object getRequestAttribute(String name);

    /**
     * Sets a named attribute in the current request context. After the current
     * request is finished processing, all objects associated with the request
     * context are eligible for garbage collection.
     * 
     * @param name the name of the request attribute to set. This argument
     *            cannot be null.
     * @param value the value of the request attribute to associate with the
     *            given name.
     */
    public void setRequestAttribute(String name, Object value);

    /**
     * Adds an object to the current transaction that will be called during
     * the transaction lifecycle.
     * @param hook a <code>TransactionHook</code> object that will be invoked
     * at the appropriate time during the container transaction.  This argument
     * cannot be null.
     */
    public void addTransactionHook(TransactionHook hook);
    
    /**
     * Explicitly commits the current transaction.  This method should only be
     * used in situations where the calling program knows it's in control of
     * the MOCA context.  In particular, its use from within components is
     * strongly discouraged.
     * @throws MocaException if an error occurred during commit.
     */
    public void commit() throws MocaException;
    
    /**
     * Explicitly rolls back the current transaction.  This method should only
     * be used in situations where the calling program knows it's in control of
     * the MOCA context.  In particular, its use from within components is
     * strongly discouraged.
     * @throws MocaException if an error occurred during rollback.
     */
    public void rollback() throws MocaException;
    
    /**
     * Create an empty, editable results object.  The MOCA context will choose the most
     * appropriate <code>EditableResults</code> concrete class.
     * @return an instance of EditableResults, best suited to be returned from a
     * component call.
     */
    public EditableResults newResults();

    /**
     * Gather all arguments to the current command. If commands have been
     * accessed already, either as method parameters or explicit variable
     * references, they are not returned by this method. If this is called
     * outside of a command execution context, returns an empty array.
     * 
     * @return
     */
    public MocaArgument[] getArgs();

    /**
     * Gather all arguments to the current command. If this is called outside of
     * a command execution context, returns an empty array. The getUsed argument
     * controls whether all arguments are returned, or only unused ones. An
     * argument is considered "used" if it has been accessed in any way at
     * execution time. That means that the argument has been assigned to a
     * function/method argument or accessed via local syntax or groovy variable
     * references.
     *
     * @param getUsed if <code>true</code>, get all arguments, including those whose
     * values have already been used for some other purpose.
     * @return an array of arguments representing the current command's arguments.
     */
    MocaArgument[] getArgs(boolean getUsed);

    /**
     * Retrieves the Results for the given level.  Level 1 is the last command
     * execution in the sequence and so forth.  If the level is not available
     * null will be returned
     * @param level The level to go back (must be 1 or greater)
     * @return The result set at that level of the execution, null if level
     *         is not available
     */
    public MocaResults getLastResults(int level);

    /**
     * Executes the given SQL and returns the results to this caller.  This is a simple
     * mechanism for executing straightforward queries.  This mechanism is not effective
     * for all situations in which SQL must be executed, and presents some efficiency
     * concerns, but can handle most simple queries without difficulty.
     * 
     * Note that all results are read into memory, so very large queries should use another
     * SQL access mechanism, such as JDBC.
     * 
     * Argument types are determined by looking up the actual class of each named argument.
     * <code>null</code> values are assumed to be STRINGs.
     * 
     * This method is a convenience method for calling 
     * {@link DatabaseTool#executeSQL(String, Map)} so that the 
     * caller doesn't need to retrieve another instance to invoke the method.
     * 
     * @param sql The SQL query to run.
     * @param args A Map instance, containing named parameters to be passed to the query.
     * @return a MocaResults object, containing all rows returned from the query.
     * @throws MocaException if a SQL or MOCA error occurs executing the query.
     * @see DatabaseTool#executeSQL(String, Map)
     */
    MocaResults executeSQL(String sql, Map<String, ?> args) throws MocaException;

    /**
     * Executes the given SQL and returns the results to this caller.  This is a simple
     * mechanism for executing straightforward queries.  This mechanism is not effective
     * for all situations in which SQL must be executed, and presents some efficiency
     * concerns, but can handle most simple queries without difficulty.
     * 
     * Note that all results are read into memory, so very large queries should use another
     * SQL access mechanism, such as JDBC.
     * 
     * This method is a convenience method for calling 
     * {@link DatabaseTool#executeSQL(String, MocaArgument...)} so that the 
     * caller doesn't need to retrieve another instance to invoke the method.
     * 
     * @param sql The SQL query to run.
     * @param args A Map instance, containing named parameters to be passed to the query.
     * @return a MocaResults object, containing all rows returned from the query.
     * @throws MocaException if a SQL or MOCA error occurs executing the query.
     * @see DatabaseTool#executeSQL(String, MocaArgument...)
     */
    MocaResults executeSQL(String sql, MocaArgument... args) throws MocaException;
    
    /**
     * Retrieves the instance of DatabaseTool that is intrinsically tied to this
     * instance of MocaContext.  This object cannot be executed on concurrently
     * by itself or in conjunction with the MocaContext.  Normally this
     * object is executed upon in the same thread that it was asked for from.
     * @return The database tool instance tied to this moca context.
     */
    DatabaseTool getDb();
}
