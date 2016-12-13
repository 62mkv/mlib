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

package com.redprairie.moca.applications.msql;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.applications.msql.MsqlEventHandler.TraceType;
import com.redprairie.moca.client.ConnectionUtils;
import com.redprairie.moca.client.LoginFailedException;
import com.redprairie.moca.client.MocaConnection;
import com.redprairie.moca.client.ServerSideConnection;
import com.redprairie.moca.exceptions.AuthenticationException;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.parse.MocaParseException;
import com.redprairie.moca.util.MocaIOException;

/**
 * This class handles msql required operations.  This is gernic enough to
 * allow a server side connection or client connection that is decided upon
 * creation of the Msql object.  You can then send a single command to the
 * Msql connection or multiple commands using a stream.  This stream should
 * have a forward slash <code>/<code> on a new line as the first word to tell
 * when a command execution should be done as the characters before it up to the
 * last forward slash will be executed as a single execution.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class Msql {
    
    /**
     * This constructor will create a Server Side Connection while using MSQL
     * @param autoCommitEnabled Whether or not auto commit is on by default
     */
    public Msql(boolean databaseModeEnabled, boolean interactive) {
        this(new ServerSideConnection(false), databaseModeEnabled, interactive);
    }


    /**
     * This constructor allows MSQL to connect to a client and authenticate
     * the user as required.  The user and password do not have to be provided,
     * but if both are it will try to login using that specified user and pass
     * combination
     * @param url The url to connect to
     * @param user The moca user name to use
     * @param pass The moca password to use for the user
     * @param clientKey TODO
     * @param autoCommitEnabled Whether or not auto commit is on by default
     * @param interactive Whether or not the msql should bring up dialogs
     * @throws MocaException If any problem occurs while connecting to the client
     *         or if there was an issue while authenticating the user
     */
    public Msql(String url, String user, String pass, 
            boolean databaseModeEnabled, boolean interactive) 
            throws MocaException {
        this(url, user, pass, null, databaseModeEnabled, interactive);
    }


    /**
     * This constructor allows MSQL to connect to a client and authenticate
     * the user as required.  The user and password do not have to be provided,
     * but if both are it will try to login using that specified user and pass
     * combination
     * @param url The url to connect to
     * @param user The moca user name to use
     * @param pass The moca password to use for the user
     * @param clientKey TODO
     * @param interactive Whether or not the msql should bring up dialogs
     * @param autoCommitEnabled Whether or not auto commit is on by default
     * @throws MocaException If any problem occurs while connecting to the client
     *         or if there was an issue while authenticating the user
     */
    public Msql(String url, String user, String pass, 
            String clientKey, boolean databaseModeEnabled, boolean interactive) 
            throws MocaException {
        super();
        _connection = ConnectionUtils.createConnection(url, null);
        _clientKey = clientKey;
        login(user, pass, clientKey);
        addApplicationId(_connection);
        _databaseModeEnabled = databaseModeEnabled;
        _interactive = interactive;
    }
    
    private void addApplicationId(MocaConnection conn){
        Map<String,String> env = conn.getEnvironment();
        if (env != null) {
            env.put(APPLICATION_KEY, "MSQL");
            conn.setEnvironment(env);
        }
    }
    
    public void login (String user, String password, String clientKey) throws LoginFailedException{
        if (user != null && password != null) {
            if (clientKey == null) clientKey = Msql.CLIENT_KEY;
            
            ConnectionUtils.login(_connection, user, password, clientKey);
        }
    }
    
    /**
     * @param connection
     * @param databaseModeEnabled
     * @param interactive
     */
    Msql(MocaConnection connection, boolean databaseModeEnabled, boolean interactive) {
        _connection = connection;
        addApplicationId(_connection);
        _clientKey = null;
        _databaseModeEnabled = databaseModeEnabled;
        _interactive = interactive;
    }
    
    /**
     * Set the autoCommit flag.  Normally, each command execution comprises a
     * single transaction.  If the autocommit flag is set to false, multiple
     * commands will be executed within the same transactional context. 
     * Warning: disabling autoCommit can affect the health of the server
     * framework, and should only be used for very short periods of time.
     * @param autoCommit the value of the autoCommit flag to be used for
     * subsequent commands.
     */
    public void setAutoCommit(boolean autoCommit) {
        _connection.setAutoCommit(autoCommit);
    }
    
    /**
     * Returns the value of the autoCommit flag for this connection.
     * @return the value of the autoCommit flag.
     */
    public boolean isAutoCommit() {
        return _connection.isAutoCommit();
    }
    
    /**
     * This will return the charset that will be used for scripts and editor 
     * files.  If null is returned then the default system charset would be 
     * used.
     * @return the overridden charset or null for default
     */
    public Charset getCharset() {
        return _encoding;
    }
    
    /**
     * Allows the setting of the charset for scripts and editor files.
     * @param charset the charset to set the script as or null to make it use
     *        the default charset
     */
    public void setCharset(Charset charset) {
        _encoding = charset;
    }
    
    /**
     * Execute a command, returning results from that command in a MocaResults
     * object.
     * @param command the text of the MOCA command sequence to execute.
     * Execution will occur on the target system that this connection is
     * associated with. 
     * @return a MocaResults object reprsenting the results of the command
     * execution.
     * @throws MocaException if an error occurs, either in communication with
     * the server, or upon execution of the command.
     */
    public MocaResults executeCommand(String command) throws MocaException {
        return _connection.executeCommand(command);
    }
    
    /**
     * This will execute the given set of commands from the Reader as they
     * come in.  This should only be called by itself or the other overloaded
     * command {@link #executeCommands(Reader)}
     * @param reader The reader to read the stream from
     * @param builder The current string builder
     * @return The list of exceptions encountered while running through this 
     *         stream
     * @throws IOException 
     */
    private List<MocaException> executeCommands(Reader reader, 
            StringBuilder builder) throws IOException {
        List<MocaException> exceptions = new ArrayList<MocaException>();
        
        BufferedReader bufferedReader = null;
        
        try {
            bufferedReader = new BufferedReader(reader);
            
            String line;
            int lineCount = 0;
            int multiLineCount = 0;
            StringBuilder accumulatedCommand;
            
            if (builder != null) {
                accumulatedCommand = builder;
            }
            else {
                accumulatedCommand = new StringBuilder();
            }
            
            while ((line = bufferedReader.readLine()) != null) {
                lineCount++;
                
                String commandToExecute = null;
                boolean temporaryCommandExecution = false;
                // If the first non whitespace character is a # then ignore
                // this is a comment
                if (line.trim().startsWith("#")) {
                    
                }
                else if (multiLineCount == 0) {
                    Matcher exitMatcher = _exitPattern.matcher(line);
                    Matcher msetMatcher = _msetPattern.matcher(line);
                    Matcher editMatcher = _editPattern.matcher(line);
                    Matcher listHistoryMatcher = _listHistoryPattern.matcher(
                            line);
                    Matcher historyMatcher = _historyPattern.matcher(line);
                    Matcher executeMatcher = _executePattern.matcher(line);
                    // If we encounter key words on the first line for quitting
                    // then we should do that
                    if (exitMatcher.matches()) {
                        break;
                    }
                    else if (line.trim().startsWith("DESC ")) {
                        commandToExecute = 
                            "describe table " + 
                            "  where table_name = '" + line.substring(4) + "'";
                        temporaryCommandExecution = true;
                    }
                    else if (msetMatcher.matches()) {
                        String command = msetMatcher.group(1);
                        String value = msetMatcher.group(2);
                        boolean invalidMsetCommand = false;
                        
                        if (command.equalsIgnoreCase("AUTOCOMMIT")) {
                            if (value.equalsIgnoreCase("ON")) {
                                _connection.setAutoCommit(true);
                            }
                            else if (value.equalsIgnoreCase("OFF")) {
                                _connection.setAutoCommit(false);
                            }
                            else {
                                invalidMsetCommand = true;
                            }
                        }
                        else if (command.equalsIgnoreCase("PROMPT")) {
                            Matcher promptQuoteMatcher = Pattern.compile(
                                    "\\s*\"(.*?)\"\\s*").matcher(value);
                            
                            String newPrompt;
                            if (promptQuoteMatcher.matches()) {
                                newPrompt = promptQuoteMatcher.group(1);
                            }
                            else {
                                newPrompt = value.trim();
                            }
                            
                            for (MsqlEventHandler eventHandler : _eventHandlers) {
                                eventHandler.updatePrompt(newPrompt);
                            }
                        }
                        else if (command.equalsIgnoreCase("ENVIRONMENT")) {
                            // If the environment is empty then null out
                            // the environment settings
                            if (value.trim().isEmpty()) {
                                _connection.setEnvironment(null);
                            }
                            else {
                                Map<String, String> environmentMapping = 
                                    new HashMap<String, String>();
                                // The common delimiter for environment is
                                // a colon, so we split on that to get each
                                // environment name value pair
                                String[] valuePairSplits = value.split(":");
                                
                                for (String valuePair : valuePairSplits) {
                                    // The name value is separated by an equals
                                    String[] envEqualsSplit = valuePair.split("=", 1);
                                    
                                    // If there was an equals in there then
                                    // send the name value pair into the
                                    // environment
                                    if (envEqualsSplit.length == 2) {
                                        environmentMapping.put(envEqualsSplit[0], 
                                                envEqualsSplit[1]);
                                    }
                                }
                                
                                _connection.setEnvironment(environmentMapping);
                            }
                        }
                        else if (command.equalsIgnoreCase("SPOOL")) {
                            if (value.equalsIgnoreCase("OFF")) {
                                Iterator<MsqlEventHandler> iter = 
                                    _eventHandlers.iterator();
                                
                                synchronized(_eventHandlers) {
                                    while (iter.hasNext()) {
                                        if (iter.next() instanceof SpoolHandler) {
                                            iter.remove();
                                        }
                                    }
                                }
                            }
                            else {
                                File spoolFile = new File(value.trim());
                                
                                try {
                                    // If the file exists and it is a file then
                                    // create the spool handler
                                    if (!spoolFile.exists()) {
                                        if (!spoolFile.createNewFile()) {
                                            traceMessage("File already exists :" + 
                                                    spoolFile.getAbsolutePath(),
                                                    TraceType.ALWAYS);
                                        }
                                    }
                                
                                    addEventHandler(new SpoolHandler(
                                        new OutputStreamWriter(
                                            new FileOutputStream(spoolFile, 
                                                false), "UTF-8")));
                                }
                                catch (IOException e) {
                                    _logger.debug("Exception encountered " +
                                    		"setting up spooler file: ", e);
                                    traceMessage("There was a problem setting" +
                                            " up the spooling file :"
                                                + e.getMessage(), 
                                                TraceType.ALWAYS);
                                }
                            }
                        }
                        else if (command.equalsIgnoreCase("COMMAND")) {
                            if (value.trim().equalsIgnoreCase("ON")) {
                                _databaseModeEnabled = false;
                                _commitCommand = true;
                            }
                            else if (value.trim().equalsIgnoreCase("OFF")) {
                                _databaseModeEnabled = true;
                                _commitCommand = false;
                            }
                            else {
                                invalidMsetCommand = true;
                            }
                        }
                        else if (command.equalsIgnoreCase("ENCODING")) {
                            if (value.equalsIgnoreCase("default")) {
                                _encoding = null;
                            }
                            else {
                                Exception excp = null;
                                try {
                                    Charset charset = Charset.forName(value);
                                    _encoding = charset;
                                }
                                catch (IllegalCharsetNameException e) {
                                    excp = e;
                                }
                                catch (UnsupportedCharsetException e) {
                                    excp = e;
                                }
                                catch (IllegalArgumentException e) {
                                    excp = e;
                                }
                                
                                if (excp != null) {
                                    traceMessage("Encoding provided [" + value + 
                                        "] is not valid\n", TraceType.ALWAYS);
                                    traceMessage("Cause is " + excp + 
                                        "\n", TraceType.ALWAYS);
                                }
                            }
                        }
                        else {
                            invalidMsetCommand = true;
                        }
                        
                        if (invalidMsetCommand) {
                            // First we add our parse exception
                            MocaException excp = new MocaParseException(
                                    lineCount, 0,
                                    "Invalid MSET command received [" + line
                                            + "]");
                            exceptions.add(excp);
                            
                            // Then output some messages warning the caller
                            traceMessage("Invalid option [" + value + "], valid commands are: \n", TraceType.ALWAYS);
                            traceMessage("     mset autocommit [on | off]\n", TraceType.ALWAYS);
                            traceMessage("     mset command [on | off] \n", TraceType.ALWAYS);
                            traceMessage("     mset environment <string> | off\n", TraceType.ALWAYS);
                            traceMessage("     mset prompt <prompt>\n", TraceType.ALWAYS);
                            traceMessage("     mset spool [ file | off ]\n", TraceType.ALWAYS);
                            traceMessage("     mset encoding <encoding> | default\n", TraceType.ALWAYS);
                        }
                    }
                    else if (line.trim().startsWith("@")) {
                        String fileName = line.trim().isEmpty() ? "" : line
                                .trim().substring(1);
                                
                        File scriptFile = new File(fileName);
                        
                        try {
                            // First see if the file as the name itself exists
                            if (!scriptFile.exists() || !scriptFile.isFile()) {
                                // If it didn't then try with .msql appended to it
                                scriptFile = new File(fileName + ".msql");
                                if (!scriptFile.exists()
                                        || !scriptFile.isFile()) {
                                    throw new MocaIOException("File [" + 
                                            fileName + "] cannot be opened");
                                }
                            }
                            
                            try {
                                Reader fileReader = new InputStreamReader(
                                    new FileInputStream(scriptFile), 
                                    _encoding == null ? Charset.defaultCharset() : 
                                        _encoding);
                                
                                _notifyLines = false;
                                // Now actually call the script
                                List<MocaException> scriptExceptions = 
                                    executeCommands(fileReader, 
                                            accumulatedCommand);
                                exceptions.addAll(scriptExceptions);
                            }
                            catch (FileNotFoundException e) {
                                throw new MocaIOException("File [" + 
                                            fileName + "] cannot be found", e);
                            }
                            finally {
                                _notifyLines = true;
                            }
                        }
                        catch (MocaIOException e) {
                            exceptions.add(e);
                        }
                    }
                    else if (editMatcher.matches()) {
                        String value = editMatcher.group(1);
                        int historyLine = 1;
                        
                        if (value != null && value.length() > 0) {
                            try {
                                historyLine = Integer.parseInt(value);
                            }
                            // If we are given an invalid number just ignore,
                            // and keep it as 1
                            catch (NumberFormatException ignore) {
                                historyLine = 1;
                            }
                        }

                        File editFile = new File("mocaedt.buf");
                        Charset editorCharset = _encoding == null ? 
                                Charset.defaultCharset() : _encoding;
                        
                        // First we try to create the file and write to it
                        // the last history command
                        Writer writer = null;
                        try {
                            // If the file exists and it is a file then
                            // create the spool handler
                            if (!editFile.exists()) {
                                if (!editFile.createNewFile()) {
                                    traceMessage("File already exists :" + 
                                            editFile.getAbsolutePath(),
                                            TraceType.ALWAYS);
                                }
                            }

                            writer = new OutputStreamWriter(
                                new FileOutputStream(editFile, false), 
                                editorCharset);
                            
                            // We write the last thing to the history
                            if (_history.size() > historyLine - 1) {
                                String commandHistory = _history.get(historyLine -1);
                                
                                writer.write(commandHistory);
                            }
                        }
                        catch (IOException e) {
                            _logger.debug("Exception encountered creating " +
                            		"editor file: ", e);
                            traceMessage("There was a problem creating the " +
                            		"editor file :" + e.getMessage(), 
                            		TraceType.ALWAYS);
                        }
                        finally {
                            if (writer != null) {
                                try {
                                    writer.close();
                                }
                                catch (IOException e) {
                                    _logger.debug("Exception encountered while " +
                                    		"closing editor file: ", e);
                                    traceMessage("There was a problem closing" +
                                    		" the writer of the editor file :"
                                                    + e.getMessage(), 
                                                    TraceType.ALWAYS);
                                }
                            }
                        }
                        
                        String editorVariable = 
                            ServerUtils.globalContext().getVariable("EDITOR");
                        
                        // If the editor isn't provided then default
                        if (editorVariable == null || 
                                editorVariable.trim().isEmpty()) {
                            System.out.println(System.getProperty("os.name"));
                            // If the os name has windows in it use notepad
                            // by default
                            if (System.getProperty("os.name").toLowerCase()
                                    .contains("window")) {
                                editorVariable = "notepad";
                            }
                            // Else we default to vi if not windows
                            else {
                                editorVariable = "vi";
                            }
                        }
                        
                        ProcessBuilder pb = new ProcessBuilder(editorVariable, 
                                editFile.getAbsolutePath());
                        
                        Process editor = pb.start();
                        // Normally we would read the output, but we shouldn't
                        // have to with the editor
                        
                        try {
                            editor.waitFor();
                        }
                        catch (InterruptedException e) {
                            // We shouldn't be interrupted, but kill the editor
                            // just in case
                            editor.destroy();
                            
                            throw new MocaInterruptedException(e);
                        }
                        
                        // Now we read in the file to see if they changed or
                        // added a command to it
                        BufferedReader fileReader = null;
                        try {
                            fileReader = new BufferedReader(
                                new InputStreamReader(new FileInputStream(
                                    editFile), editorCharset));
                            
                            StringBuilder editorCommand = new StringBuilder();
                            
                            String editorLine;
                            
                            while ((editorLine = fileReader.readLine()) != null) {
                                if (editorCommand.length() > 0) {
                                    editorCommand.append('\n');
                                }
                                editorCommand.append(editorLine);
                            }
                            
                            _history.add(0, editorCommand.toString());
                            
                            traceMessage(1 + " :* " + _history.get(0) 
                                    + "\n", TraceType.ALWAYS);
                        }
                        finally {
                            if (fileReader != null) {
                                try {
                                    fileReader.close();
                                }
                                catch (IOException e) {
                                    _logger.debug("Exception encountered " +
                                    		"closing the editor file: ", e);
                                    traceMessage("There was a problem closing " +
                                    		"the editor file :" 
                                            + e.getMessage(), TraceType.ALWAYS);
                                }
                            }
                        }

                        // Lastly we delete the file to not leave remnants
                        if (!editFile.delete()) {
                            traceMessage("There was a problem deleting the " + 
                                    "editor file :" + editFile.getAbsolutePath(), 
                                    TraceType.ALWAYS);
                        }
                    }
                    else if (listHistoryMatcher.matches()) {
                        String value = listHistoryMatcher.group(1);
                        
                        int historyLine = 1;
                        
                        if (value != null && value.length() > 0) {
                            try {
                                historyLine = Integer.parseInt(value);
                            }
                            // If we are given an invalid number just ignore,
                            // and keep it as 1
                            catch (NumberFormatException ignore) {
                                historyLine = 1;
                            }
                        }
                        
                        if (_history.size() > historyLine - 1) {
                            String commandHistory = _history.get(historyLine -1);
                            
                            traceMessage(historyLine + " :* " + commandHistory 
                                    + "\n", TraceType.ALWAYS);
                        }
                    }
                    else if (historyMatcher.matches()) {
                        String value = historyMatcher.group(1);
                        
                        int historyLine = 1;
                        
                        if (value != null && value.length() > 0) {
                            try {
                                historyLine = Integer.parseInt(value);
                            }
                            // If we are given an invalid number just ignore,
                            // and keep it as 1
                            catch (NumberFormatException ignore) {
                                historyLine = 1;
                            }
                        }
                        
                        // Now go through all the history afterwards
                        for (int i = historyLine; _history.size() > i - 1; i++) {
                            String commandHistory = _history.get(i -1);
                            
                            traceMessage(i + " :* " + commandHistory 
                                    + "\n", TraceType.ALWAYS);
                        }
                    }
                    else if (executeMatcher.matches()) {
                        String value = executeMatcher.group(1);
                        
                        int historyLine = 1;
                        
                        if (value != null && value.length() > 0) {
                            try {
                                historyLine = Integer.parseInt(value);
                            }
                            // If we are given an invalid number just ignore,
                            // and keep it as 1
                            catch (NumberFormatException ignore) {
                                historyLine = 1;
                            }
                        }
                        
                        if (_history.size() > historyLine - 1) {
                            commandToExecute = _history.get(historyLine -1);
                        }
                    }
                    else {
                        accumulatedCommand.append(line);
                        multiLineCount++;
                    }
                }
                else {
                    if (line.trim().length() == 0) {
                        _history.add(0, accumulatedCommand.toString());
                        accumulatedCommand = new StringBuilder();
                        multiLineCount = 0;
                    }
                    else if (line.trim().charAt(0) == '/' && line.trim().length() == 1) {
                        commandToExecute = accumulatedCommand.toString();
                        // Clear out the command as well
                        accumulatedCommand.delete(0, accumulatedCommand.length());
                        _history.add(0, commandToExecute);
                    }
                    else {
                        accumulatedCommand.append('\n');
                        accumulatedCommand.append(line);
                        multiLineCount++;
                    }
                }
                
                // If the command to execute is provided then call it
                if (commandToExecute != null && commandToExecute.length() > 0) {
                    
                    traceMessage("\nExecuting... ", TraceType.INTERACTIVE);
                    traceMessage("\n" + commandToExecute + "\n\n", 
                            TraceType.NONINTERACTIVE);
                    
//                    long beginCommandTime = System.nanoTime();
//                    long endCommandTime;
                    
                    try {
                        MocaResults results;
                        
                        // If the last command was commit and the current command
                        // is commit then we don't want to execute it, if we
                        // are in install mode.
                        if (_interactive == true || 
                                !"COMMIT".equalsIgnoreCase(_lastCommand) || 
                                !"COMMIT".equalsIgnoreCase(commandToExecute)) {
                            _lastCommand = commandToExecute;
                        
                            // If we are running in database mode put the brackets
                            // around the command to execute, unless in temporary
                            // command execution mode
                            if (_databaseModeEnabled && !temporaryCommandExecution) {
                                results = executeCommand("[" + commandToExecute + "]");
                            }
                            // Else we just execute the command as is
                            else {
                                results = executeCommand(commandToExecute);
                                
                                // If it is mset command mode and we don't have auto
                                // commit then we want to commit after the command
                                // execution
                                if (_commitCommand && !_connection.isAutoCommit()) {
                                    _connection.executeCommand("commit");
                                    _lastCommand = "COMMIT";
                                }
                            }
                        }
                        else {
                            results = null;
                        }
                        
//                        endCommandTime = System.nanoTime();
                        
                        traceMessage("Success!\n\n", TraceType.ALWAYS);
                        
                        if (results != null && results.getRowCount() > 0) {
                            traceResults(results);
                            
                            traceMessage("\n(" + results.getRowCount() + 
                                    " Row(s) Affected)\n\n", TraceType.INTERACTIVE);
                        }
                    }
                    catch (MocaException e) {
//                        endCommandTime = System.nanoTime();
                        
                        _logger.debug("Exception encountered : ", e);
                        
                        traceMessage("Error!\n\n", TraceType.INTERACTIVE);
                        
                        if (e instanceof NotFoundException && _interactive) {
                            traceMessage("Command affected no rows\n\n", 
                                    TraceType.INTERACTIVE);
                        }
                        // Timeout of the session key.
                        else if (e.getErrorCode() == AuthenticationException.CODE && _interactive) {
                            traceMessage(String.format("%s%n", e.getMessage()), TraceType.INTERACTIVE);
                            Console console = System.console();
                            String mocaUser = console.readLine("%s", "Login: ");
                            String mocaPass = new String(console.readPassword("%s", "Password: "));
                            try {
                                login(mocaUser, mocaPass, _clientKey);
                            }
                            catch (LoginFailedException e1) {
                                _logger.debug("Exception encountered : ", e1);
                                traceMessage(String.format("%s%n", e.getMessage()), TraceType.INTERACTIVE);
                            }
                        }
                        else {
                            exceptions.add(e);
                            traceMessage("ERROR: " + e.getErrorCode(), 
                                    TraceType.ALWAYS);
                            
                            // TODO this is broken for error replacement
                            if (e.getMessage() != null) {
                                traceMessage(" - " + e.getMessage() + "\n", 
                                        TraceType.ALWAYS);
                            }
                            
                            traceMessage("\n", TraceType.ALWAYS);
                            
                            MocaResults results = e.getResults();
                            
                            if (results != null && results.getRowCount() > 0) {
                                traceResults(results);
                            }
                        }
                    }
                    
                    // TODO add something to allow this to be enabled, maybe another mset value
//                    for (MsqlEventHandler eventHandler : _eventHandlers) {
//                        eventHandler.notifyCommandExecution(commandToExecute, 
//                                endCommandTime - beginCommandTime);
//                    }
                        
                    accumulatedCommand = new StringBuilder();
                    multiLineCount = 0;
                }
                
                if (_notifyLines) {
                    for (MsqlEventHandler eventHandler : _eventHandlers) {
                        eventHandler.notifyLine(line, multiLineCount);
                    }
                }
            }
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                    _logger.debug(
                            "Exception encountered while closing reader: ", e);
                    traceMessage("Unable to close reader :" + e.getMessage(), 
                            TraceType.ALWAYS);
                }
            }
        }
        
        return exceptions;
    }
    
    /**
     * This will take a given stream and parse the bytes incoming to create
     * a command sequence.  It will behave just as if you called executeCommand
     * except that it won't execute a command until it finds a forward slash
     * in the stream "/" as the first non whitespace character of that line.
     * If auto commit has been disabled then a commit will be executed after
     * the Reader has been completely processed.
     * @param reader The reader to read from.  This is automatically closed
     *        after processing all data in it.
     * @return This will return a list containing all the moca exceptions
     *         that occurred while executing
     * @throws IOException This exception is thrown if there are any issues
     *         reading from the stream
     */
    public List<MocaException> executeCommands(Reader reader) 
            throws IOException {
        try {
            return executeCommands(reader, null);
        }
        finally {
            // If auto commit was disabled, then we need to do a commit
            // after we are done with the reader.
            if (!isAutoCommit()) {
                _logger.debug("Attempting commit since auto commit is disabled");
                try {
                    _connection.executeCommand("commit");
                }
                catch (MocaException e) {
                    _logger.debug("Could not commit : ", e);
                    traceMessage("ERROR: Could not commit :" + e.getMessage(), 
                            TraceType.ALWAYS);
                }
            }
        }
    }
    
    private static final Pattern _msetPattern = Pattern.compile(
            "\\s*MSET\\s*(\\S+)\\s*(.+)", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern _exitPattern = Pattern.compile(
            "\\s*(?:EXIT)|(?:QUIT)|(?:Q)\\s*", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern _editPattern = Pattern.compile(
            "\\s*(?:EDIT)|(?:ED)\\s*(\\S*)\\s*", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern _listHistoryPattern = Pattern.compile(
            "\\s*L\\s*(\\S*)\\s*", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern _historyPattern = Pattern.compile(
            "\\s*(?:HISTORY)|(?:HIST)|(?:H)\\s*(\\S*)\\s*", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern _executePattern = Pattern.compile(
            "\\s*/\\s*(\\d*)\\s*");
    
    private class SpoolHandler implements MsqlEventHandler {

        public SpoolHandler(Writer writer) {
            super();
            _writer = writer;
        }

        @Override
        public void notifyLine(String line, int multiLineCount) {
        }

        @Override
        public void traceEvent(String message, TraceType type) {
            try {
                if (_interactive) {
                    _writer.write(message);
                    _writer.flush();
                }
                else {
                    if (type != TraceType.INTERACTIVE) {
                        _writer.write(message);
                        _writer.flush();
                    }
                }
            }
            catch (IOException e) {
                _logger.debug("Exception encountered writing to spooler : ", e);
                traceMessage("There was a problem writing to the spooler :" 
                        + e.getMessage(), TraceType.ALWAYS);
                // We should remove the spooler handler, since we encountered
                // a problem writing to it
                _eventHandlers.remove(this);       
            }
        }

        @Override
        public void updatePrompt(String prompt) {
            
        }
        
        @Override
        public void notifyCommandExecution(String command, long duration) {
            try {
                _writer.write(String.format(
                        "Execution duration : %.3f sec%n%n", duration / 1.0e9));
            }
            catch (IOException e) {
                _logger.debug("Exception encountered : ", e);
                traceMessage("There was a problem writing to the spooler :" 
                        + e.getMessage(), TraceType.ALWAYS);
                // We should remove the spooler handler, since we encountered
                // a problem writing to it
                _eventHandlers.remove(this);
            }
        }
        
        private final Writer _writer;
    }
    
    /**
     * This will send the trace messages to the event handlers notifying on the
     * specified tracing type
     * @param message the message that we are tracing
     * @param type the trace type of this message
     */
    private void traceMessage(String message, TraceType type) {
        for (MsqlEventHandler eventHandler : _eventHandlers) {
            eventHandler.traceEvent(message, type);
        }
    }
    
    /**
     * This will trace a result set using {@link TraceType#ALWAYS} level to trace
     * @param results The moca results to use
     */
    protected void traceResults(MocaResults results) {
        
        StringBuilder columnBuilder = new StringBuilder();
        StringBuilder dividerBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();
        
        int columnCount = results.getColumnCount();
        
        List<String> columnNames = new ArrayList<String>(
                columnCount);
        LinkedList<String> values = new LinkedList<String>();
        Map<Integer, Integer> columnSizeMapping = 
            new LinkedHashMap<Integer, Integer>(columnCount);
        
        // First we setup the names and get the initial length
        // before checking values
        for (int i = 0; i < columnCount; ++i) {
            String columnName = results.getColumnName(i);
            
            columnNames.add(columnName);
            
            // We set the max length to be at least 3
            int maxLength = results.getMaxLength(i) > 2 ? results.getMaxLength(i) : 3;
            
            columnSizeMapping.put(i, columnName.length() > maxLength ? 
                    columnName.length() : maxLength);
        }
        
        RowIterator rowIter = results.getRows(); 
        // Now we actually go through the results and get the
        // string we need to print up
        while (rowIter.next()) {
            
            for (int i = 0; i < columnCount; ++i) {
                int valueLength = columnSizeMapping.get(i);
                Object value = rowIter.getValue(i);
                
                if (value != null) {
                    values.add(value.toString());
                    
                    // If our value is longer than our current
                    // lenght then update it
                    if (value.toString().length() > valueLength) {
                        columnSizeMapping.put(i, value.toString()
                                .length());
                    }
                }
                // If it was null set it to blank
                else {
                    values.add(null);
                }
            }
        }
        
        // Now we create the header and divider information
        for (Entry<Integer, Integer> entry : columnSizeMapping.entrySet()) {
         
            int columnWidth = (entry.getValue() > _maxColumnWidth ? 
                    _maxColumnWidth : entry.getValue());
            // Append the column names here
            {
                // Make sure we have 2 spaces between each column
                if (columnBuilder.length() > 0) {
                    columnBuilder.append("  ");
                }
                String columnName = columnNames.get(entry.getKey());
                columnBuilder.append(String.format("%-" + 
                        columnWidth + "s", columnName));
            }
            
            // Append the divider here
            {
                // Make sure we have 2 spaces between each column
                if (dividerBuilder.length() > 0) {
                    dividerBuilder.append("  ");                    
                }
                for (int j = 0; j < columnWidth; ++j) {
                    dividerBuilder.append('-');
                }
            }
        }
        
        // We want to put a new line after each the columns and the dividers
        columnBuilder.append('\n');
        dividerBuilder.append('\n');
        
        int count = 0;
        while (values.size() > 0) {
            
            // Make sure we have 2 spaces between each column, but don't do
            // it if we just ended a line
            if (valueBuilder.length() > 0 && count % columnCount != 0) {
                valueBuilder.append("  ");
            }
            
            int columnWidth = columnSizeMapping.get(count % columnCount);
            
            if (columnWidth > _maxColumnWidth) {
                columnWidth = _maxColumnWidth;
            }
            
            valueBuilder.append(String.format("%-" + columnWidth
                     + "s", values.pop()));
            count++;
            
            // If we are divisible by the column count it means we should move
            // our string to the next line, since we finished the row
            if (count % columnCount == 0) {
                valueBuilder.append('\n');
            }
        }
        
        traceMessage(columnBuilder.toString(), TraceType.ALWAYS);
        traceMessage(dividerBuilder.toString(), TraceType.ALWAYS);
        traceMessage(valueBuilder.toString(), TraceType.ALWAYS);
    }
    
    /**
     * This allows you to add an additional event handler to be notified of
     * msql events as they occur
     * @param handler The handler to be added
     */
    public void addEventHandler(MsqlEventHandler handler) {
        if (handler != null) {
            _eventHandlers.add(handler);
        }
    }
    
    private static final Logger _logger = LogManager.getLogger(Msql.class);
    
    protected final int _maxColumnWidth = 210;
    protected final boolean _interactive;
    protected final String _clientKey;
    protected List<String> _history = new ArrayList<String>();
    protected List<MsqlEventHandler> _eventHandlers = Collections
            .synchronizedList(new ArrayList<MsqlEventHandler>());
    protected boolean _databaseModeEnabled;
    protected String _lastCommand;
    protected boolean _notifyLines = true;
    protected boolean _commitCommand = false;
    protected Charset _encoding = null;
    protected final MocaConnection _connection;
    
    private final static String APPLICATION_KEY = "MOCA_APPL_ID";
    private final static String CLIENT_KEY = "msql";
}
