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

package com.redprairie.moca.server.exec;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaOperator;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.server.CommandInterceptor;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.DBType;
import com.redprairie.moca.server.dispatch.MessageResolver;
import com.redprairie.moca.server.legacy.NativeLibraryAdapter;
import com.redprairie.moca.server.profile.CommandPath;
import com.redprairie.moca.server.repository.Command;
import com.redprairie.moca.server.repository.Trigger;

/**
 * Represents the primary execution context of a MOCA server action. This
 * encapsulates the core MOCA execution engine, or at least one execution
 * context within that engine. A ServerContext instance is the owner of a MOCA
 * transaction, holds the state of the MOCA execution stack, executes local
 * syntax streams, executes named commands on behalf of component definitions,
 * and executes triggers based on other execution requests.
 * 
 * <em>NOTE: While this interface is public, it is intended for the use of MOCA itself, and is not available as an
 * application-accessible interface.  It is possible to damage the stability of the MOCA execution engine if this
 * object is misused.    
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public interface ServerContext extends ArgumentSource {

    /**
     * Returns a reference to a component-friendly execution context. This
     * context, encapsulated by the type <code>MocaContext</code> is what gets
     * passed to all components when they execute.
     * 
     * @return a MocaContext corresponding to this ServerContext.
     */
    public MocaContext getComponentContext();
    
    /**
     * Returns a reference to a component-friendly database utility.
     * 
     * @return a DatabseTool corresponding to this ServerContext.
     */
    public TransactionManager getTransactionManager();

    /**
     * Creates a blank results of the "preferred" type for this execution
     * context.
     * 
     * @return a new, editable results object.
     */
    public EditableResults newResults();

    /**
     * Removes a stack frame from the MOCA runtime stack. The MOCA stack is
     * pushed down when a new command is executed that needs access to preceding
     * command results. In the prototypical command stream A|B|C, before the
     * component B is executed, a new stack frame is pushed onto the stack.
     * After the entire stream is executed, the stack is popped out to the
     * original level.
     * @param keepErrorState if <code>true</code>, the error code from the
     * current execution stack level is copied to the one above.
     * 
     * @see #pushStack()
     */
    public void popStack(boolean keepErrorState);

    /**
     * Pushes a stack frame onto the MOCA runtime stack. See the
     * <code>popStack</code> method for a complete explanation of the MOCA
     * stack.
     * 
     * @see #popStack(boolean)
     */
    public void pushStack();

    /**
     * Sets up the result columns for the current frame of the MOCA runtime
     * stack. The result columns are effectively published to downstream
     * (upstack?) commands. This method simply sets up metadata about the
     * current stack frame's results. In order for data to be made available,
     * the <code>setRow</code method must be called.
     * 
     * @param res a <code>MocaResults</code> object to be used to configure the
     *            metadata about the published results at the current stack
     *            level.
     */
    public void setColumns(MocaResults res);

    /**
     * Passes a row of data to be used as the current frame of the MOCA runtime
     * stack. This method uses the data in the current position of
     * <code>row</code>, but does not advance the iterator.
     * 
     * @param row a <code>RowIterator</code> representing a row of data (with
     *            identical metadata to the columns passed in to
     *            <code>setColumns</code>.
     * 
     * @see #setColumns(MocaResults)
     */
    public void setRow(RowIterator row);

    /**
     * Tells the context what command or command sequence we
     * are running at the current stack level.
     * @param command
     */
    void setCommand(ExecutableComponent command);

    /**
     * Clears the current list of arguments. Since a command can be executed
     * multple times within a single stack frame (iterating over a result set
     * for a pipe), each execution of a command must reset the argument list
     * before executing.
     * 
     * @see #addArg(String, MocaOperator, MocaValue)
     */
    public void clearArgs();

    /**
     * Sets up the current execution stack's notion if its "arguments".
     * Arguments differ from stack results in that they are variables passed in
     * to a command, and that they potentially have an explicit operator.
     * Command arguments are available to downstream commands implicitly, but
     * they are also available explicitly as a command argument list, or through
     * the special SQL substitution variable @*.
     * 
     * @param name the name of the argument
     * @param oper the operator this argument was specified with
     * @param value a value representing this argument.
     */
    public void addArg(String name, MocaOperator oper, MocaValue value);

    /**
     * Returns the most recent exception that has been thrown by the MOCA engine
     * or a MOCA component.
     * 
     * @return the last error seen by MOCA in the current execution context.
     */
    public MocaException getLastError();

    /**
     * Sets the current execution error code and message according to the given
     * MOCA Exception object.
     * 
     * @param e
     */
    public void setError(MocaException e);

    /**
     * Used to associate the current stack level's pushed error state with
     * its official execution state.  Before this method is called, the stack
     * level has a pseudo-error state that has been pushed up the stack, but
     * not an official error state.
     */
    void fixErrorState();

    /**
     * Returns the current execution stack depth.  This represents the entire
     * stack depth; srvInitiateCommand and MocaContext.executeCommand calls do
     * not affect this value.
     * 
     * This object is not thread safe for level retrieval or toString reading
     * unless this is done on the same thread that this ServerContext is
     * linked to.
     * 
     * @return the current execution stack depth. This information is only
     *         really useful for logging and testing purposes.
     */
    public StackLevel getStackLevel();

    /**
     * Gets a variable from the current execution stack, looking for either a
     * primary name or an alias.  This differs from calling the single-argument
     * version of this method in that the first instance of either name will be
     * returned.  Therefore, if <code>alias</code> is on the stack higher than
     * <code>name</code>, then the value of <code>alias</code> is returned.
     * 
     * @param name the variable to look up.  This argument can be
     *          null, if <code>alias</code> is not null.
     * @param alias an alternate variable name to look up. This argument can be
     *          null, if <code>name</code> is not null.
     * @param markUsed If <code>false</code>, the variable fetched is not marked
     *            used. Otherwise, it is marked used. Used variables will not be
     *            included in argument lists.
     * @return the value of the argument, or <code>null</code> if the variable
     *         is not on the stack.  Note that the caller has no way of knowing
     *         whether the value came from the primary name or the alias.
     */
    public MocaValue getVariable(String name, String alias, boolean markUsed);

    /**
     * Sets a system variable (AKA environment variable) in the current
     * running context. 
     * 
     * @param name the name of the system variable
     * @param value the value of the system variable
     */
    public void putSystemVariable(String name, String value);
    
    /**
     * Removes a system variable (AKA environment variable) from the current
     * running context.
     * 
     * @param name the name of the system variable
     */
    public void removeSystemVariable(String name);

    /**
     * Looks up a context variable, and treats it as an argument (i.e. returning
     * name and operator information), whether or not it exists as an argument
     * or a result value on the stack. Stack result values will be treated as if
     * they were passed with the equals operator (MocaOperator.EQ).
     * 
     * @param name the argument name to look for.
     * @param alias an alternate argument to look for.  If both the name and
     *              alias are passed, each stack frame will be searched for
     *              both the primary name and the alias.  If this parameter is
     *              passed as <code>null</code>, only <code>name</code> will be
     *              used for the search.
     * @param markUsed if <code>true</code>, the argument is marked as used on
     *            the stack
     * @param equalsOnly if true, we should limit the search to arguments passed
     *            with the equals operator (MocaOperator.EQ).  Otherwise, any
     *            argument in the list could be returned.
     * @return a <code>MocaArgument</code> instance corresponding to the named
     *         value. If no value of the given name exists on the stack, either
     *         as an argument or a result column, <code>null</code> is returned.
     */
    public MocaArgument getVariableAsArgument(String name, String alias, boolean markUsed, boolean equalsOnly);

    /**
     * Looks up a value in the MOCA registry.
     * 
     * @param key Registry key name to use.
     * @param name
     * @param expand If <code>true</code>, environment variables of the form
     *            $VAR or %VAR% are expanded to their current values.
     * @return the registry value associated with the key name, or
     *         <code>null</code> if no value exists.
     */
    public String getRegistryValue(String key, boolean expand);

    /**
     * Removes the named attribute from the transaction.
     * 
     * @param name the name of the transaction attribute to be removed.
     */
    public void removeTransactionAttribute(String name);

    /**
     * Checks the current running transaction to see if an attribute has been
     * added of the given name.
     * 
     * @param name the name of the transaction attribute
     * @return an attribute associated with the given attribute name, or
     *         <code>null</code> if no such attribute exists.
     */
    public Object getTransactionAttribute(String name);

    /**
     * Adds a named object to the current transaction. When the current running
     * transaction is committed or rolled back, the reference to the attribute
     * is removed as well.
     * 
     * @param name the name of the transaction attribute
     * @param value the value of the transaction attribute
     */
    public void setTransactionAttribute(String name, Object value);

    /**
     * Executes a Groovy script. Used by the command execution code to run a
     * script.
     * 
     * @param script the script source to compile
     * @param language the language the script is written in.  If this parameter
     * is null, the Groovy language will be used.
     * @return the compiled script.
     * @throws MocaException if an error occurred running the script, or if the
     *             script threw an exception.
     */
    public CompiledScript compileScript(String script, String language) throws MocaException;
    
    /**
     * Executes a Groovy script. Used by the command execution code to run a
     * script.
     * 
     * @param compiled the compiled script to run.
     * @return the results of the script execution
     * @throws MocaException if an error occurred running the script, or if the
     *             script threw an exception.
     */
    public MocaResults executeScript(CompiledScript compiled) throws MocaException;

    /**
     * Executes a Groovy script, returning its result value.
     * 
     * @param compiled the compiled script to run
     * @return the results of the script execution
     * @throws MocaException if an error occurred running the script, or if the
     *             script threw an exception.
     */
    public Object evaluateScript(CompiledScript compiled) throws MocaException;

    /**
     * Executes a SQL statement. Variable replacement is done before execution,
     * according to the traditional MOCA rules of SQL variable replacement. Note
     * that this method is designed to be called from MOCA command execution
     * code to execute local syntax SQL.
     * 
     * @param sql the SQL to execute.
     * @return the results of the SQL execution
     * @throws MocaException if an error occurred SQL execution.
     */
    public MocaResults executeSQLWithVars(String sql) throws MocaException;
    
    /**
     * Executes a SQL statement. Variable replacement is done before execution,
     * according to the traditional MOCA rules of SQL variable replacement. Note
     * that this method is designed to be called from MOCA command execution
     * code to execute local syntax SQL.
     * 
     * @param sql the SQL to execute.
     * @param profile hint.
     * @return the results of the SQL execution
     * @throws MocaException if an error occurred SQL execution.
     */
    public MocaResults executeSQLWithVars(String sql, String profileHint) throws MocaException;

    /**
     * Looks up a named command (verb/noun clause) and executes it. This method
     * is intended to be called from the MOCA command execution code to cause
     * execution of a named command. This is not a valid method to initiate
     * commands. Note that arguments must have been filled in (via the addArg
     * method) before calling this method to kick off the command.
     * 
     * @param verbNounClause the named command to execute. This is expected to
     *            be in "normal" form: lower case, with a single space character
     *            between words.
     * @param override if <code>true</code>, the lookup mechanism looks to see
     *            if the current context is already executing a command of the
     *            given name, and looks for an overridden command that comes
     *            next in the command chain for that name
     * @return the results of the component execution
     * @throws MocaException if the component threw an exception, or if the
     *             command could not be found.
     */
    public MocaResults executeNamedCommand(String verbNounClause, boolean override)
            throws MocaException;

    /**
     * Compiles and executes a command string. This is the main (public) entry
     * point for executing a command string.
     * 
     * @param command The command string to execute.
     * @param args A list of arguments to be passed in. If any argument is
     *            passed as <code>null</code>, it is assumed to be of type
     *            String. Otherwise, the value is inspected to determine the
     *            data type.
     * @param keepContext If <code>true</code>, the current context (stack) is
     *            used to execute the command and resolve any arguments or
     *            variable references. Otherwise, a new stack is instantiated
     *            and used for this execution.
     * @return the results of the command execution
     * @throws MocaException if the command execution encountered an error.
     */
    public MocaResults executeCommand(String command, Map<String, ?> args, boolean keepContext)
            throws MocaException;

    /**
     * Executes the given command with support for setting up the remote
     * context. This can also be used to set up incoming command arguments.
     * 
     * @param command the command string to execute
     * @param context the data context to use to set up the incoming command.
     * @param args the command arguments passed to the enclosing command.
     * @returna MocaResults object returned by the remote server.
     * @throws MocaException upon application or communication error.
     */
    public MocaResults executeCommandWithRemoteContext(String command,
                                                       Collection<MocaArgument> context,
                                                       Collection<MocaArgument> args)
            throws MocaException;

    /**
     * Initiate a command on the given remote server. Existing remote
     * connections will be reused as needed, and the remote call will be
     * executed within the current transaction.
     * 
     * @param remoteHost the remote server to access. Depending on the form of
     *            the host string, different communication methods will be
     *            utilized (HTTP or legacy socket).
     * @param commandText The text of the command to be executed.
     * @return a MocaResults object returned by the remote server.
     * @throws MocaException upon application or communication error.
     */
    public MocaResults executeRemote(String remoteHost, String commandText) throws MocaException;

    /**
     * Initiate a command on the given set of remote servers. All remote
     * executions occur in parallel, on background threads.
     * 
     * @param remoteHost the remote servers to access, separated by commas.
     *            Depending on the form of the host string, different
     *            communication methods will be utilized (HTTP or legacy
     *            socket).
     * @param commandText The text of the command to be executed.
     * @param inTransaction if true, remote failure will cause the transaction
     *            to roll back.
     * @return a MocaResults object returned by the remote server.
     * @throws MocaException upon application or communication error.
     */
    public MocaResults executeParallel(String remoteHost, String commandText,
                                       boolean inTransaction)
            throws MocaException;

    /**
     * Commits the current MOCA transaction. This will commit the current
     * database transaction as well as any outstanding transactions on remote
     * servers that are in flight. Any transaction hooks that are registered
     * with the current transaction are fired as well.
     * 
     * @throws MocaException if an error occurred committing the transaction, or
     *             if some other commit-time failure occurred (e.g. hook
     *             failure, remote failure, etc.)
     */
    public void commit() throws MocaException;

    /**
     * Rolls back the current MOCA transaction. This will roll back the current
     * database transaction as well as any outstanding transactions on remote
     * servers that are in flight. Any transaction hooks that are registered
     * with the current transaction are fired as well.
     * 
     * @throws MocaException if an error occurred while rolling back the
     *             transaction, or if some other rollback-time failure occurred
     *             (e.g. hook failure, remote failure, etc.)
     */
    public void rollback() throws MocaException;

    /**
     * Returns the database type of the main MOCA database connection.
     * 
     * @return the database type, as a <code>DBType</code> enumeration value.
     */
    public DBType getDbType();

    /**
     * Returns the next value for a given sequence. This is the primary
     * application mechanism for obtaining the next value for a given database
     * sequence.
     * 
     * @param name the sequence name
     * @return the next sequence value
     * @throws MocaException
     */
    public String getNextSequenceValue(String name) throws MocaException;

    /**
     * Obtains a reference to a native library adapter associated with this MOCA
     * execution context. The native adapter is used to execute C code in the
     * current MOCA context. This interface should only be used by the component
     * execution classes to execute native (C/COM) code.
     * 
     * @return a <code>NativeLibraryAdapter</code> that can be used to execute C
     *         code.
     */
    public NativeLibraryAdapter getNativeLibraryAdapter() throws MocaException;

    /**
     * Executes the given SQL. This method does not do variable substitution,
     * but it does perform SQL translation.
     * 
     * @param sql the SQL to be executed.
     * @param args a list of arguments to be used as statement parameters.
     * @param autoBind if <code>true</code> then autobind is performed in
     *            addition to normal translation.
     * @param ignoreResults if <code>true</code>, don't bother to retrieve
     *            results. This can be useful for statements that would
     *            otherwise return large amounts of data, but are only being run
     *            for their result status.
     * @return a MocaResults instance
     * @throws SQLException If a database exception occurs.
     * @throws MocaException If a translation or other MOCA execution exception
     *             occurs.
     */
    public MocaResults executeSQL(String sql, BindList args, boolean autoBind, boolean ignoreResults)
            throws SQLException, MocaException;

    /**
     * Commits just the database transaction. No hooks or remote transactions
     * are affected by this call.
     * 
     * @throws SQLException if a database exception occurs.
     */
    public void commitDB() throws SQLException;

    /**
     * Rolls back just the database transaction. No hooks or rmote transactions
     * are affected by this call.
     * 
     * @throws SQLException
     */
    public void rollbackDB() throws SQLException;

    /**
     * Sets up a savepoint. The savepoint is a database-only construct, so it is
     * local to the current transaction. Also, non-database resources are not
     * tracked or rolled back according to the savepoint.
     * 
     * @param savepoint The name of the savepoint to create.
     * @throws SQLException
     */
    public void setSavepoint(String savepoint) throws SQLException;

    /**
     * Rolls back the database to the named savepoint. The savepoint must have
     * been created using the {@link #setSavepoint(String)} method.
     * 
     * @param savepoint the name of the savepoint to roll back to.
     * @throws SQLException If the savepoint does not exist or there was some
     *             database error rolling back to the savepoint.
     */
    public void rollbackDB(String savepoint) throws SQLException;

    /**
     * Closes the context, releasing all in-flight resources. This is a useful
     * method for when transactions, native processes and other resources are
     * being used on behalf of the running context, and a low-level exception
     * bubbles up to the top level.
     */
    public void close();
    
    /**
     * This will set the current operational status of the Server Context.  This
     * call back is usually done only from within a Command.
     * @param status The status to set on the context
     */
    public void setCurrentStatus(ServerContextStatus status);

    /**
     * Retrieves the current operational status of the Server Context
     * @return The status at the time of invocation
     */
    public ServerContextStatus getCurrentStatus();

    /**
     * @return true if this context has native keepalive set.
     */
    public boolean hasKeepalive();

    /**
     * clears the error state of the current execution stack level.  This
     * has no effect on the error state of previous execution stack levels,
     * so the overall error state may still be set.
     */
    public void clearError();
    
    /**
     * This will return the last statement that was initiated for the context.
     * This occurs when someone calls things such as srvInitiateCommand, 
     * srvExecuteCommand, MocaContext.executeCommand, MocaContext.executeInline,
     * and execute server command.
     * @return The last initiated statement
     */
    public String getLastStatementInitiated();
    
    /**
     * This will return the session that is associated with this
     * context.
     * @return The session.
     */
    public SessionContext getSession();
    
    /**
     * This will return the message resolver associated with this context.
     * A message resolver can be used for error code translation.
     * @return The message resolver currently associated with the context.
     */
    public MessageResolver getMessageResolver();

    /**
     * This will return the current command path of the execution of this
     * context.  This is safe to be executed from another thread while this
     * is executing.
     * @return The currently executing command path
     */
    public CommandPath currentCommandPath();

    /**
     * @param commandDef
     * @param triggers
     * @return
     * @throws MocaException
     */
    MocaResults executeDefinedCommand(Command commandDef, List<Trigger> triggers)
            throws MocaException;

    /**
     * @param verbNounClause
     * @param override
     * @return
     * @throws CommandNotFoundException
     */
    ExecutableComponent lookupNamedCommand(String verbNounClause, boolean override)
            throws CommandNotFoundException;

    /**
     * @return true if this context has an active transaction.
     */
    boolean hasTransaction();

    /**
     * @param msg
     */
    public void logDebug(Object msg);

    /**
     * This will return the request context that is associated with this
     * context.
     * @return The request context object.
     */
    public RequestContext getRequest();
    
    public void overrideCommand(String commandName, CommandInterceptor interceptor);
    
    public void clearOverriddenCommands();
}