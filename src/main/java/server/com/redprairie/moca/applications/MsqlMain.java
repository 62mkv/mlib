/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.applications;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

import javax.swing.JFileChooser;

import org.apache.logging.log4j.LogManager;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.applications.msql.Msql;
import com.redprairie.moca.applications.msql.MsqlEventHandler;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.log.LoggingConfigurator;
import com.redprairie.moca.util.AppUtils;
import com.redprairie.moca.util.Options;
import com.redprairie.moca.util.OptionsException;

/**
 * This is the mainline for the MSQL executable.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class MsqlMain {
    static void showUsage() {
        System.out
                .println("Usage: msql [ -ehikmqsvS ] [ -a <url> ]\n"
                        + "                           [ -u <user> ] [ -w <password> ]\n"
                        + "                           [ @ <file name> ] [ -e <encoding> ]\n");

        System.out.println("\t-a <url>       url to connect to\n"
                + "\t-l             Prompt for login/password\n"
                + "\t-u <user>      Login user\n"
                + "\t-w <password>  Login password\n"
                + "\t-i             InstallSql mode\n"
                + "\t-S             Server (not client) mode\n"
                + "\t-c             Single Command to process (install mode)\n"
                + "\t-k <key>       Use the specified client key\n"
                + "\t-h             Show help\n"
                + "\t-q             Don't do command syntax, do SQL\n"
                + "\t-v             Show version information\n"
                + "\t@              Script file name to process\n"
                + "\t-e             Encoding used for script files and editor");
    }

    public static void main(String[] args) throws SystemConfigurationException {
        Options opts = null;
        try {
            opts = Options.parse("Sa:u:w:c:k:ilqvh?@:e:", args);
        }
        catch (OptionsException e) {
            System.err.println("Invalid option: " + e.getMessage());
            showUsage();
            System.exit(1);
        }

        // By default run with flags of autocommit and multi line
        boolean autoCommitEnabled = true;

        // By default we run in client mode
        RunningMode mode = RunningMode.CLIENT;

        int exceptionCount = 0;
        String url = null;
        boolean login = false;
        String mocaUser = null;
        String mocaPass = null;
        boolean installMode = false;
        String singleCommand = null;
        String clientKey = null;

        if (opts.isSet('h') || opts.isSet('?')) {
            showUsage();
            return;
        }

        if (opts.isSet('S')) {
            // If we are set to be client mode run in server,
            // This is to guarantee we don't change it from DBSERVER
            // Although the ordering of the if statements already guarantee this
            if (mode != RunningMode.DBSERVER) {
                mode = RunningMode.SERVER;
            }
        }

        if (opts.isSet('a')) {
            url = opts.getArgument('a');
        }

        if (opts.isSet('u')) {
            mocaUser = opts.getArgument('u');
            login = true;
        }

        if (opts.isSet('w')) {
            mocaPass = opts.getArgument('w');
            login = true;
        }
        
        if (opts.isSet('k')) {
            clientKey = opts.getArgument('k');
            login = true;
        }
        
        if (opts.isSet('c')) {
            singleCommand = opts.getArgument('c');
            installMode = true;
        }

        // Only enable the trace messages if not in install mode
        if (opts.isSet('i')) {
            installMode = true;
        }

        if (opts.isSet('l')) {
            login = true;
        }

        if (opts.isSet('q')) {
            mode = RunningMode.DBSERVER;
            // Disable autocommit if we are running with db server mode
            autoCommitEnabled = false;
        }

        if (opts.isSet('v')) {
            traceMessage(AppUtils.getVersionBanner("MSQL"), installMode);
            return;
        }
        
        Charset charset = null;
        if (opts.isSet('e')) {
            String charsetString = opts.getArgument('e');
            
            charset = Charset.forName(charsetString);
        }
        
        traceMessage(AppUtils.getStartBanner("MSQL"), installMode);

        String[] remainingArgs = opts.getRemainingArgs();
        File scriptFile = null;
        
        for (String arg : remainingArgs) {
            if (arg.charAt(0) == '@') {
                String file = arg.substring(1);
                // First make sure we have a file
                if (file.trim().length() == 0) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setCurrentDirectory(new File("."));
                    int returnVal = chooser.showOpenDialog(null);
                    
                    if(returnVal == JFileChooser.APPROVE_OPTION) {
                       file = chooser.getSelectedFile().getName();
                    }
                }
                
                // Try to check the script file to see if it exists
                scriptFile = new File(file);
                
                if (!scriptFile.exists() || !scriptFile.isFile()) {
                    // If we can't get that file use with a .msql on the end
                    scriptFile = new File(file + ".msql");
                }
            }
        }

        Msql msql = null;

        // If we are running in client mode we have to make sure
        // the port has been initialized
        if (mode == RunningMode.CLIENT) {
            // If the port wasn't set, set it to our default
            if (url == null) {
                System.err.println(
                        "If you are running in client mode, you must "
                                + "provide a url to connect to");
                System.exit(1);
            }

            traceMessage("\nConnecting to server: " + url + "\n\n", installMode);
            
            // If we are supposed to login make sure we do all that
            if (login) {
                // If we weren't given a user then prompt for it
                if (mocaUser == null) {
                    Console console = System.console();
        
                    mocaUser = console.readLine("%s", "Login: ");
                }
        
                // If we weren't given a password then prompt for it
                if (mocaPass == null) {
                    Console console = System.console();
        
                    mocaPass = new String(console.readPassword("%s", "Password: "));
                }
            }
            
            // We also want to enable our loggers, since we aren't initializing
            // moca to do this for us
            LoggingConfigurator.configure();
            
            try {
                msql = new Msql(url, mocaUser, mocaPass, clientKey, false, !installMode);
            }
            catch (MocaException e) {
                System.err.println("Error encountered while connecting to " +
                		"client :" + e.getMessage());
                System.exit(1);
            }
            // We always have auto commit for client connection
            msql.setAutoCommit(true);
        }
        // This means we are running in server mode
        else {
            try {
                ServerUtils.setupDaemonContext(MsqlMain.class.getName(), true, true);
            }
            catch (SystemConfigurationException e) {
                LogManager.getLogger(LogManager.ROOT_LOGGER_NAME).fatal("Error setting up moca context " + 
                        e.getMessage());
                System.exit(1);
            }
            
            msql = new Msql(mode == RunningMode.DBSERVER, !installMode);
            
            msql.setAutoCommit(autoCommitEnabled);
        }
        
        // If we are not install mode we have to do some extra tracing stuff
        if (!installMode) {
            String value;
            if (mode == RunningMode.DBSERVER) {
                value = "SQL> ";   
            }
            else {
                value = "MSQL> ";
            }

            String prefix = "";
            if (!autoCommitEnabled) {
                prefix = "(autocommit off) ";
            }
            
            //Writer writer = new PrintWriter(System.out);
            Writer writer = new OutputStreamWriter(System.out, Charset.defaultCharset());
            
            try {
                writer.write(prefix);
                writer.write(value);
                writer.flush();
            }
            catch(IOException e) {
                System.err.println("There was a problem writing to the " +
                        "output :" + e.getMessage());
                System.exit(1);
            }
            
            // Now we add an event handler so that for each line we will
            // make sure to get our MSQL prompt back
            msql.addEventHandler(new MsqlPrinterHandler(value, 
                    writer, prefix));
        }
        else {
            msql.addEventHandler(new MsqlPrinterHandler(null, 
                    null, null));
        }
        
        // If a single command is provided just execute that and get out of here
        // This varies from script execution in that script execution keeps the
        // msql process around afterwards
        if (singleCommand != null) {
            Reader reader = null;
            try {
                // Now we create a reader from the command with a slash on the
                // next line to make sure it gets executed
                reader = new StringReader(singleCommand + "\n/");
                
                List<MocaException> exceptions = msql.executeCommands(reader);
                
                if (exceptions.size() > 0) {
                    exceptionCount += exceptions.size();
                    System.err.println(exceptions.size() + " error(s) occurred " +
                                "executing command execution");
                }
                System.exit(exceptionCount);
            }
            catch (IOException e) {
                System.err.println("There was a problem reading from the " +
                                "command: " + singleCommand);
                exceptionCount += 1;
                System.err.println(1 + " error(s) occurred " +
                            "executing command: " + singleCommand);
                System.exit(exceptionCount);
            }
            finally {
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (IOException e) {
                        System.err.println("Error closing file reader: " + 
                                e.getMessage());
                    }
                }
            }
        }
        
        // If we have a script file execute that first
        if (scriptFile != null) {
            Reader reader = null;
            try {
                reader = new InputStreamReader(new FileInputStream(scriptFile), 
                    charset == null ? Charset.defaultCharset() : charset);
                
                List<MocaException> exceptions = msql.executeCommands(reader);
                
                if (exceptions.size() > 0) {
                    exceptionCount += exceptions.size();
                    System.err.println(exceptions.size() + " error(s) occurred " +
                    		"executing script file");
                }
            }
            catch (FileNotFoundException e) {
                System.err.println("Could not open script file: " + 
                        e.getMessage());
                exceptionCount += 1;
                System.err.println(1 + " error(s) occurred " +
                            "executing script file");
            }
            catch (IOException e) {
                System.err.println("There was a problem reading from the " +
                		"file: " + scriptFile.getAbsolutePath());
                exceptionCount += 1;
                System.err.println(1 + " error(s) occurred " +
                            "executing script file");
            }
            finally {
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (IOException e) {
                        System.err.println("Error closing file reader: " + 
                                e.getMessage());
                    }
                }
            }
        }
        
        try {
            msql.setCharset(charset);
            List<MocaException> exceptions = msql.executeCommands(
                    new InputStreamReader(System.in, Charset.defaultCharset()));
            
            exceptionCount += exceptions.size();
        }
        catch (IOException e) {
            System.err.println("Msql encountered a problem reading from input:" 
                    + e.getMessage());
            exceptionCount++;
        }
        
        // Now we exit with how many errors we encountered
        System.exit(exceptionCount);
    }
    
    private static class MsqlPrinterHandler implements MsqlEventHandler {

        public MsqlPrinterHandler(String prompt, Writer writer, String prefix) {
            super();
            _prompt = prompt;
            _writer = writer;
            _prefix = prefix;
        }
        
        @Override
        public void traceEvent(String message, TraceType type) {
            // If we weren't given a writer, send the WARN and LOW to standard
            // output to give basic tracing
            if (_writer == null) {
                if (type != TraceType.INTERACTIVE) {
                    System.out.print(message);
                    System.out.flush();
                }
            }
            else {
                if (type != TraceType.NONINTERACTIVE) {
                    try {
                        _writer.write(message);
                        _writer.flush();
                    }
                    catch (InterruptedIOException e) {
                        throw new MocaInterruptedException(e);
                    }
                    catch (IOException e) {
                        System.err.println("There was a problem writing to " +
                        		"output");
                    }   
                }
            }
        }

        @Override
        public void notifyLine(String line, int multiLineCount) {
            // If we have a writer then we can look into the events
            if (_writer != null) {
                try {
                    // If we are going to be on the first line or we are
                    // about to execute a command then don't push the text over
                    if (multiLineCount == 0) {
                        _writer.write(_prefix);
                        _writer.write(_prompt);
                        _writer.flush();
                    }
                    else {
                        _writer.write(_prefix);
                        _writer.write("*" + _prompt);
                        _writer.flush();
                    }
                }
                catch (InterruptedIOException e) {
                    throw new MocaInterruptedException(e);
                }
                catch (IOException e) {
                    System.err.println("There was a problem writing to output");
                }
            }
        }
        
        @Override
        public void updatePrompt(String prompt) {
            _prompt = prompt;
        }
        
        @Override
        public void notifyCommandExecution(String command, long duration) {
            if (_writer != null) {
                try {
                    _writer.write(String.format(
                            "Execution duration : %.3f sec%n%n", duration / 1.0e9));
                }
                catch (InterruptedIOException e) {
                    throw new MocaInterruptedException(e);
                }
                catch (IOException e) {
                    System.err.println("There was a problem writing to the " +
                    		"output :" + e.getMessage());
                }
            }
        }
        
        private String _prompt;
        private final Writer _writer;
        private final String _prefix;
    }
    
    /**
     * This will trace a message to the appropriate output.  This will determine
     * whether or not we should trace based on the install mode.  The message
     * should contain all line feeds as none are added.
     * @param message The message to trace.
     * @param installMode Whether or not install mode is on
     */
    private static void traceMessage(String message, boolean installMode) {
        if (!installMode) {
            System.out.print(message);
        }
    }

    private enum RunningMode {
        SERVER, DBSERVER, CLIENT
    }
}
