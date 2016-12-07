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

package com.redprairie.moca.applications.mload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.client.ConnectionUtils;
import com.redprairie.moca.client.LoginFailedException;
import com.redprairie.moca.client.MocaConnection;
import com.redprairie.moca.client.ServerSideConnection;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.moca.util.Options;
import com.redprairie.moca.util.OptionsException;
import com.redprairie.util.WildcardFilenameFilter;

/**
 * Loads data from csv files based on commands defined by control files
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author gvaneyck
 * @version $Revision$
 */
public class Mload {

    // Used for parsing a row of data from a csv or otherwise delimited file
    enum FieldParsingState { BEGIN_FIELD, IN_QUOTES, END_QUOTES, IN_FIELD, END_FIELD }

    public static final String COMMAND_LINE_OPTIONS = "a:p:u:w:P:c:D:d:v:f:go:t:Tse:HUl:m";

    private Appendable outStream = System.out;
    private Appendable errStream = System.err;

    private String lastServer = null;
    
    private MocaConnection connection = null;
    private File datawd = new File("."); // Tracking the current data directory
    /**
     * This is needed since the current working directory of the jvm could be
     * possibly not what the control file and data directories are based off
     * of.  This happens if mload is invoked programmatically through java.
     * 
     * cwd - this will be the data directory if there is one.  If not it will
     *       stay as the current directory of the JVM.
     * fallbackcwd - this will be the programmatically assigned directory.  If 
     *               not it will fallback to the program cwd.
     * 
     * 
     * This is needed in case if you have a programmatically executed mload that
     * has a different cwd of the program and a data directory is specified.
     * That is because you have to have your data directory, your 'virtual'
     * working directory where ctl file lives and your actual program working
     * directory that cannot be changed.
     */
    private File virtualwd = new File(".");
    private String ctlFile = null; // Control file containing components

    private String ctlFileLines = null; // String containing expanded control file
    private boolean hasHeader = false; // Header line defines column names
    private String dataDir = "."; // Data directory (Default is the current directory)
    private String dataFile = "*.csv"; // Data file(s) (Default is '*.csv')
    private int maxErrors = 0; // Maximum number of errors to show
    private int linesPerCommit = 100; // Lines to process before commit
    private boolean isMaster = false; // Control file is a master control file
    private char fieldDelim = ','; // Field delimiter (Default is comma)
    private String varBaseName = "@VAR"; // Variable name (Default is '@VAR')
    private String traceFile = null; // Trace file pathname
    private String traceLevels = null; // Trace level switches
    private boolean debugMsg = false; // Debug messages (print commands)
    private boolean genOracleTrace = false; // Generate Oracle trace file
    private boolean silent = false; // Silent mode - do not show errors
    private boolean upgrading = false; // Database upgrade in progress
    private boolean clientMode = false; // Client mode
    private String hostURL = null; // Host URL (if in client mode)
    private String user = null; // Login user (if in client mode)
    private String pass = null; // Login password (if in client mode)
    // TODO: undocumented and unused -M <memory file>???
    
    private Options mainOpts = null;
    
    /**
     * The basic constructor for mload.  Make sure parseOptions is called
     * before calling load
     */
    public Mload() {
        super();
    }

    /**
     * This constructor sets up mload to be reading for an immediate call
     * to load
     * @param opts The command line options for mload
     */
    public Mload(Options opts) {
        this();
        parseOptions(opts);
    }
    
    /**
     * This constructor sets up mload to be reading for an immediate call
     * to load
     * @param opts The command line options for mload
     * @param workingDirectory The working directory to execute in
     */
    public Mload(Options opts, String workingDirectory) {
        this(opts);
        String expandedName = MocaUtils.expandEnvironmentVariables(
                MocaUtils.currentContext(), workingDirectory);
        virtualwd = new File(expandedName);
    }
    
    /**
     * This allows the output and error streams to be redirected
     * @param outputStream The stream to print standard output to
     * @param errorStream The stream to print error output to
     */
    public void setStreams(Appendable outputStream, Appendable errorStream) {
        outStream = outputStream;
        errStream = errorStream;
    }
    
    /**
     * Displays a standard formatted error with the information provided
     * @param reason The main error cause that occurred
     * @param record The record number this occurred on
     * @param rollbacks The number of records rolled back
     * @param line The first line of the record
     * @param curLine The current line of the record
     * @param file The name of the file that is being parsed
     * @throws IOException 
     */
    private void displayError(String reason, int record, int rollbacks,
            int line, int curLine, String file) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append(reason);
        buffer.append(" record ");
        buffer.append(record);
        buffer.append(", line number");
        if (line != curLine) {
            buffer.append("s: ");
            buffer.append(line);
            buffer.append('-');
            buffer.append(curLine);
        }
        else {
            buffer.append(": ");
            buffer.append(line);
        }
        buffer.append(" in the data file [");
        buffer.append(file);
        buffer.append("]\n     Rolled back record number");

        if (rollbacks > 0) {
            buffer.append("s ");
            buffer.append(record - rollbacks + 1);
            buffer.append(" -");
        }
        buffer.append(' ');
        buffer.append(record);

        outStream.append(buffer);
        outStream.append('\n');
    }

    /**
     * Builds the command string that would be run for this instance of mload
     * Used to print messages for mload_all and master control files
     * @return A string representing the command for this mload
     */
    public String getCommandString() {
        StringBuilder buffer = new StringBuilder("mload");
        if (isMaster) buffer.append(" -m");
        if (ctlFile != null) buffer.append(" -c " + ctlFile);
        if (!dataDir.equals(".")) buffer.append(" -D " + dataDir);
        if (!dataFile.equals("*.csv")) buffer.append(" -d " + dataFile);
        if (!varBaseName.equals("@VAR")) buffer.append(" -v " + varBaseName);
        if (fieldDelim != ',') buffer.append(" -f " + fieldDelim);
        if (hasHeader) buffer.append(" -H");
        if (debugMsg) buffer.append(" -g");
        if (traceFile != null) buffer.append(" -o " + traceFile);
        if (traceLevels != null) buffer.append(" -t " + traceLevels);
        if (genOracleTrace) buffer.append(" -T");
        if (silent) buffer.append(" -s");
        if (clientMode) {
            buffer.append(" -C");
            if (hostURL != null) buffer.append(" -a " + hostURL);
            if (user != null) buffer.append(" -u " + user);
            if (pass != null) buffer.append(" -w " + pass);
        }
        if (maxErrors != 0) buffer.append(" -e " + maxErrors);
        if (linesPerCommit != 100) buffer.append(" -l " + linesPerCommit);
        if (upgrading) buffer.append(" -U");
        
        return buffer.toString();
    }

    /**
     * Loads all relevant data given the options passed to parseOptions
     * @return The number of errors found while loading
     * @throws FileNotFoundException if a file or directory could not be found
     * @throws IOException if a file could not be read
     * @throws OptionsException if the child options in a master control file
     *                          could not be parsed
     * @throws MocaException if a command could not be executed
     * @throws LoginFailedException if the specified user could not be logged
     *                              in (during client mode)
     * @throws ParseException if there was a problem parsing a file
     */
    public int load() throws FileNotFoundException, IOException,
            OptionsException, MocaException, LoginFailedException,
            ParseException {
        //TODO: fail if parseOptions wasn't called?
        
        // Connect to the MOCA server if we're running as a client.
        if (clientMode) {
            // If client changes close existing connection
            if (connection != null && !hostURL.equals(lastServer)) {
                connection.close();
                connection = null;
            }
            
            if (connection == null) {
                connection = ConnectionUtils.createConnection(hostURL, null);
                ConnectionUtils.login(connection, user, pass, CLIENT_KEY);

                connection.setAutoCommit(false);
                lastServer = hostURL;
            }
        }
        else if (connection == null) {
            connection = new ServerSideConnection(false);
            connection.setAutoCommit(false);
        }

        // Moca trace facility
        if (traceFile != null) {
            connection.executeCommand("set trace where activate = 1 and filename = '" + traceFile + "'");
        }
        if (traceLevels != null) {
            connection.executeCommand("[[ moca.setTraceLevel(\"" + traceLevels + "\")]]");
        }

        // Start Oracle Trace
        if (genOracleTrace) {
            String cmd = "[alter session set sql_trace=true]";
            connection.executeCommand(cmd);
        }

        // Set the current working directory, default is '.'
        datawd = openFile(dataDir);
        if (!datawd.isDirectory()) {
            throw new FileNotFoundException("Could not set current data directory as " + datawd.getCanonicalPath());
        }

        // Process master control files
        if (isMaster) {
            BufferedReader masterCtlReader;
            InputStream tempStream;
            String message;
            if (ctlFile != null) {
                tempStream = new FileInputStream(ctlFile);
                message = "Reading from " + ctlFile + "...\n";
            }
            else {
                tempStream = System.in;
                message = "Reading from stdin...\n";
                ctlFile = "STDIN"; // For printing errors later
            }

            masterCtlReader = new BufferedReader(new InputStreamReader(tempStream, "UTF-8"));

            if (!silent) {
                outStream.append(message);
                outStream.append('\n');
            }

            String line;
            
            // Validate master control file MUST start with #MLOAD
            // Future maybe have version information
            if ((line = masterCtlReader.readLine()) == null || !line.startsWith("#MLOAD")) {
                throw new ParseException("Master control file must start with #MLOAD", 0);
            }

            Mload childCtlFile;
            Options childOpts;

            try {
                int totalErrors = 0;
                while ((line = masterCtlReader.readLine()) != null) {
                    String[] options = parseLine(line);
    
                    if (options != null) {
                        childCtlFile = new Mload();
                        
                        // The options the master started with are defaults for
                        // the child mload, with the exception of -m and -c
                        if (mainOpts != null)
                            childCtlFile.parseOptions(mainOpts, false);
                        childOpts = Options.parse(COMMAND_LINE_OPTIONS, options);
                        childCtlFile.parseOptions(childOpts);

                        outStream.append("Command: ");
                        outStream.append(childCtlFile.getCommandString());
                        outStream.append('\n');

                        totalErrors += childCtlFile.load();
                    }
                }
                
                return totalErrors;
            }
            finally {
                masterCtlReader.close();
            }
        }
        else {
            return processCtl();
        }
    }

    /**
     * Opens a file using the current working directory as a parent or as an
     * absolute path
     * @param fileName The name of file to open
     * @return The opened file
     * @throws FileNotFoundException if the file couldn't be located relatively
     *                               or absolutely
     */
    private File openFile(String fileName) throws FileNotFoundException {
        String expandedName = MocaUtils.expandEnvironmentVariables(MocaUtils.currentContext(), fileName);
        
        // Try relative path first
        File file = new File(datawd, expandedName);
        if (!file.exists()) {
            // Try the fallback directory
            file = new File(virtualwd, expandedName);
            
            if (!file.exists()) {
                // Try absolute path next, we don't want it to check the
                // cwd of java since this could be wrong spot so we also confirm
                // that the file is absolute
                file = new File(expandedName);
                if (!file.exists() || !file.isAbsolute()) {
                    throw new FileNotFoundException("Could not locate file " + expandedName);
                }                
            }
        }
        
        return file;
    }

    /**
     * Parses a line of a master control file
     * @param line The line to parse
     * @return The arguments to pass onto a child mload, if any
     */
    public String[] parseLine(String line) throws MocaException, IOException {
        String cmdOpts = "";
    
        line = line.trim();
        if (line.length() == 0) {
            return null;
        }
        
        // Process non comment lines
        if (!line.startsWith("#")) {
           // Check for master control commands  (line starts with %)
           if (line.startsWith("%")) {
               if (line.startsWith("CHDIR", 1)) {
                   String dir = MocaUtils.expandEnvironmentVariables(MocaUtils.currentContext(), line.substring(6).trim());
                   datawd = new File(datawd, dir);
                   if (!datawd.isDirectory()) {
                       throw new FileNotFoundException("Could not change to directory " + datawd.getCanonicalPath());
                   }
                   if (!silent) {
                       outStream.append("Change directory: ");
                       outStream.append(dir);
                       outStream.append('\n');
                   }
               }
               else if (line.startsWith("PUTVAR", 1)) {
                   String[] envPair = line.substring(7).split("=");
                   if (envPair.length == 2) {
                       Map<String, String> env = connection.getEnvironment();
                       envPair[1] = MocaUtils.expandEnvironmentVariables(MocaUtils.currentContext(), envPair[1]);
                       env.put(envPair[0], envPair[1]);
                       
                       if (!silent) {
                           outStream.append("Put variable: ");
                           outStream.append(envPair[0]);
                           outStream.append(" = ");
                           outStream.append(envPair[1]);
                           outStream.append('\n');
                       }
                   }
               }
               else if (line.startsWith("EXEC", 1)) {
                   String cmd = line.substring(5);
                   if (!silent) {
                       outStream.append("Command: ");
                       outStream.append(cmd);
                       outStream.append('\n');
                   }
                   connection.executeCommand(cmd);
               }
           }
           else {
               // Assume command line options for mload
               cmdOpts = line;
               cmdOpts = cmdOpts.replaceAll("[ \t\r\n]+", " ");

               // Do not pass along the -m option
               cmdOpts = cmdOpts.replaceAll("-m", "");
            }
        }
        
        if (cmdOpts.isEmpty())
            return null;
        else
            return cmdOpts.split(" ");
    }

    /**
     * Sets the parameters of mload based on command line options read in
     * @param opts The command line parameters for mload
     */
    public void parseOptions(Options opts) {
        mainOpts = opts;
        
        // 'm' option should only be set by initial call
        if (opts.isSet('m')) isMaster = true;
        if (opts.isSet('c')) ctlFile = opts.getArgument('c');
        if (opts.isSet('D')) dataDir = opts.getArgument('D');
        if (opts.isSet('d')) dataFile = opts.getArgument('d');
        if (opts.isSet('v')) varBaseName = opts.getArgument('v');
        if (opts.isSet('f')) fieldDelim = opts.getArgument('f').charAt(0);
        if (opts.isSet('H')) hasHeader = true;
        if (opts.isSet('g')) debugMsg = true;
        if (opts.isSet('o')) traceFile = opts.getArgument('o');
        if (opts.isSet('t')) traceLevels = opts.getArgument('t');
        if (opts.isSet('T')) genOracleTrace = true;
        if (opts.isSet('s')) silent = true;
        if (opts.isSet('C')) clientMode = true;
        if (opts.isSet('a')) hostURL = opts.getArgument('a');
        if (opts.isSet('u')) user = opts.getArgument('u');
        if (opts.isSet('w')) pass = opts.getArgument('w');
        if (opts.isSet('e')) maxErrors = Integer.parseInt(opts.getArgument('e'));
        if (opts.isSet('l')) linesPerCommit = Integer.parseInt(opts.getArgument('l'));
        if (opts.isSet('U')) upgrading = true;
        
        // Set the hostURL to a default if needed
        if (clientMode && hostURL == null) {
            hostURL = MocaUtils.currentContext().getRegistryValue(MocaRegistry.REGKEY_SERVER_URL);
            if (hostURL == null)
                hostURL = "http://localhost:4500/moca";
        }

        // Setup the data directory
        if (File.separatorChar == '\\')
            dataDir = dataDir.replace('/', '\\');
        else if (File.separatorChar == '/')
            dataDir = dataDir.replace('\\', '/');
    }

    public void parseOptions(Options opts, boolean inMaster) {
        parseOptions(opts);
        
        if (!inMaster) {
            ctlFile = null;
            isMaster = false;
        }
    }

    /**
     * Processes a control file
     * @return The number of errors that occurred
     * @throws FileNotFoundException if the control file could not be located
     * @throws IOException if the control file or a data file could not be read
     * @throws MocaException if an exception occurred when executing a command
     * @throws ParseException if the control file or a data file could not be parsed
     */
    public int processCtl() throws FileNotFoundException, IOException, MocaException, ParseException {
        // Format is for turning duration into a formatted string
        DateTimeFormatter dateFormat = DateTimeFormat
            .forPattern("HH:mm:ss.SSS").withZone(
                DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT")));
        
        BufferedReader inputFile = null;
        StringBuilder buffer;
        String line;
        int totalErrors = 0;

        // Read the control file
        ctlFileLines = readControlFile(ctlFile);

        File[] files = datawd.listFiles(new WildcardFilenameFilter(dataFile));

        if (files == null || files.length == 0) {
            
            // We have to check the fallback directory as well, since this
            // could have been invoked programmatically.
            files = virtualwd.listFiles(new WildcardFilenameFilter(dataFile));
            
            if (files == null || files.length == 0) {
                // Lastly we try the file as an absolute value.
                String expandedName = MocaUtils.expandEnvironmentVariables(
                        MocaUtils.currentContext(), dataFile);
                File file = new File(expandedName);
                if (!file.exists() || !file.isAbsolute()) {
                    errStream.append("Data file ");
                    errStream.append(dataFile);
                    errStream.append(" could not be found for control file ");
                    errStream.append(ctlFile);
                    errStream.append("!\n");
                    
//                    throw new FileNotFoundException("Could not locate file " + expandedName);
                    return 0;
                }
                files = new File[]{file};
            }
        }

        List<File> sortedFiles = new ArrayList<File>(Arrays.asList(files));
        
        // Now sort the list by Names
        Collections.sort(sortedFiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        
        String ctlFileLinesRestore = ctlFileLines;

        for (File dataFile : sortedFiles) {
            ctlFileLines = ctlFileLinesRestore;
            try {
                inputFile = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), "UTF-8"));
    
                String fileName;
                
                if (dataFile.isAbsolute()) {
                    fileName = dataFile.getAbsolutePath();
                }
                else {
                    fileName = dataFile.getParentFile().getName() + 
                        File.separator +
                        dataFile.getName();
                }
                
                buffer = new StringBuilder();
                buffer.append("Processing ");
                buffer.append(fileName);
                buffer.append("... ");
                if (linesPerCommit > 0) {
                    buffer.append("(commit window ");
                    buffer.append(linesPerCommit);
                    buffer.append(" lines)");
                }
                buffer.append('\n');
                outStream.append(buffer);
                outStream.append('\n');
    
                int columnCount = 0;
                int lineNum = 0;
                int curLineNum = 0;
                if (hasHeader) {
                    line = inputFile.readLine();
                    if (line == null || line.isEmpty()) {
                        throw new ParseException("Unable to read header line", 0);
                    }
                    
                    curLineNum++;
    
                    String[] fields = separateFields(line, fieldDelim);
                    columnCount = fields.length;
                    
                    buffer = new StringBuilder();
                    
                    boolean inAt = false;
                    int startPos = 0;
    
                    // This replaces headers with the generic @VAR#@
                    for (int pos = 0; pos < ctlFileLines.length(); pos++) {
                        char c = ctlFileLines.charAt(pos);
                        if (inAt) {
                            if (c == '@') {
                                String varName = ctlFileLines.substring(startPos, pos + 1);
                                boolean found = false;
                                
                                // Handle updating
                                if (varName.equalsIgnoreCase("@mode_updating@")) {
                                    varName = (upgrading ? "Y" : "N");
                                    found = true;
                                }
                                // Handle column names
                                else {
                                    for (int i = 0; i < fields.length; i++) {
//                                        String fieldName = "@" + fields[i] + "@";
                                        String fieldName = "@" + fields[i].trim() + "@";
                                        if (varName.equalsIgnoreCase(fieldName)) {
                                            varName = varBaseName + (i + 1) + "@";
                                            found = true;
                                            break;
                                        }
                                    }
                                }
                                
                                // Handle not found encapsulated by single quotes
                                if (!found &&
                                    startPos > 0 && ctlFileLines.charAt(startPos - 1) == '\'' &&
                                    pos < ctlFileLines.length() && ctlFileLines.charAt(pos + 1) == '\'') {
                                    varName = "";
                                }
                                
                                buffer.append(varName);
                                inAt = false;
                            }
                            else if (!Character.isLetterOrDigit(c) && c != '_') {
                                inAt = false;
                                buffer.append(ctlFileLines.substring(startPos, pos + 1));
                            }
                        }
                        else {
                            if (c == '@') {
                                startPos = pos;
                                inAt = true;
                            }
                            else {
                                buffer.append(c);
                            }
                        }
                    }

                    ctlFileLines = buffer.toString();
                }
    
                int recordNum = 0;
                int blankCount = 0;
                int rollbackNum = 0;
                int errCount = 0;
                int pendingCommitNum = 0;
                long startTime = System.currentTimeMillis();
    
                while ((line = inputFile.readLine()) != null) {
                    curLineNum++;
                    lineNum = curLineNum;
                    
                    // Skip empty lines
                    if (line.isEmpty())
                        continue;
                    
                    recordNum++;
    
                    // Skip empty records
                    if (line.matches("\\" + fieldDelim + "*")) {
                        if (!silent) {
                            buffer = new StringBuilder();
                            buffer.append("Ignoring record ");
                            buffer.append(recordNum);
                            buffer.append(", line number: ");
                            buffer.append(lineNum);
                            buffer.append(" in the data file [");
                            buffer.append(fileName);
                            buffer.append("] since all columns are blank");
                            outStream.append(buffer);
                            outStream.append('\n');
                        }
                        blankCount++;
                        continue;
                    }
                    
                    // Append extra lines from unmatched double-quotes if needed
                    StringBuilder lineBuffer = new StringBuilder(line);
                    
                    int numQuotes = 0;
                    for (char c : line.toCharArray()) {
                        if (c == '"')
                            numQuotes++;
                    }
                    
                    while (numQuotes % 2 != 0) {
                        line = inputFile.readLine();
                        if (line == null)
                            break;
                        
                        for (char c : line.toCharArray()) {
                            if (c == '"')
                                numQuotes++;
                        }
                        
                        lineBuffer.append('\n');
                        lineBuffer.append(line);
                        curLineNum++;
                    }
                    
                    line = lineBuffer.toString();
    
                    pendingCommitNum++;
                    String[] fields;
                    try {
                        fields = separateFields(line, fieldDelim);
                    }
                    catch (ParseException e) {
                        // Rethrow after adding more information
                        throw new ParseException(e.getMessage() + " on line "
                                + lineNum + " while parsing "
                                + dataFile.getName(), e.getErrorOffset());
                    }
    
                    // Check for an invalid amount of columns
                    if (columnCount != 0 && fields.length != columnCount) {
                        int rollbackCount = pendingCommitNum - 1;
                        errCount++;
                        rollbackNum += rollbackCount;
                        pendingCommitNum = 0;
    
                        connection.executeCommand("rollback");
                        displayError("Error: wrong number of columns when parsing",
                            recordNum, rollbackCount, lineNum, curLineNum, fileName);
                    }
                    else {
                        try {
                            subDataAndExec(fields, errCount);
                        }
                        catch (MocaException e) {
                            int rollbackCount = pendingCommitNum - 1;
                            errCount++;
                            rollbackNum += rollbackCount;
                            pendingCommitNum = 0;
    
                            connection.executeCommand("rollback");
    
                            if (!silent && (maxErrors == 0 || errCount < maxErrors)) {
                                displayError("Error " + e.getErrorCode() + " when executing on",
                                    recordNum, rollbackCount, lineNum, curLineNum, fileName);
                            }
                        }
    
                        if (recordNum % 100000 == 0) {
                            Date elapsed = new Date(System.currentTimeMillis() - startTime);
                            outStream.append(String.valueOf(recordNum));
                            outStream.append(" record(s) processed; elapsed: ");
                            outStream.append(dateFormat.print(elapsed.getTime()));
                            outStream.append('\n');
                        }
                    }
    
                    /*
                     * Commit pending, if linesPerCommit is set greater than
                     * zero and the number of pending commits is greater than
                     * or equal to it. Note that it is possible to miss this
                     * code if the number of lines in a file are less than
                     * linesPerCommit or on a fatal error so we call this later
                     * outside the loop as well
                     */
                    if (linesPerCommit > 0 && pendingCommitNum >= linesPerCommit) {
                        pendingCommitNum = 0;
                        connection.executeCommand("commit");
                    }
                } // while !eof
    
                if (pendingCommitNum > 0) {
                    connection.executeCommand("commit");
                }
    
                //Build up the result
                buffer = new StringBuilder();
    
                if (errCount > 0) {
                    buffer.append("     ");
                    buffer.append(recordNum);
                    buffer.append(" record(s) processed\n");
                }
                buffer.append("     ");
                buffer.append(recordNum - errCount - rollbackNum - blankCount);
                buffer.append(" record(s) successfully loaded\n");
                if (rollbackNum > 0) {
                    buffer.append("     ");
                    buffer.append(rollbackNum);
                    buffer.append(" record(s) successfully loaded, but rolled back\n");
                }
                if (errCount > 0) {
                    buffer.append("     ");
                    buffer.append(errCount);
                    buffer.append(" record(s) errored\n");
                }
                if (blankCount > 0) {
                    buffer.append("     ");
                    buffer.append(blankCount);
                    buffer.append(" blank record(s) ignored\n");
                }
                Date elapsed = new Date(System.currentTimeMillis() - startTime);
                buffer.append("       Elapsed Time: ");
                buffer.append(dateFormat.print(elapsed.getTime()));
                buffer.append('\n');
                outStream.append(buffer);
                outStream.append('\n');
    
                // Keep a counter of the total number of errored records.
                totalErrors += errCount;
            }
            finally {
                if (inputFile != null)
                    inputFile.close();
            }
        }

        return totalErrors;
    }

    /**
     * The base call for reading in a control file.  This also sets up the base
     * include recursion detection.
     * @param controlFile The name of the control file
     * @return A string containing the lines of the control file with includes
     *         having been recursed
     * @throws FileNotFoundException if the control file can't be found
     * @throws IOException if something prevents the control file from being read
     * @throws ParseException if the control file can't be parsed
     */
    private String readControlFile(String controlFile)
            throws FileNotFoundException, IOException, ParseException {
        try {
            return readControlFile(controlFile, new HashSet<String>());
        }
        catch (ParseException e) {
            throw new ParseException(e.getMessage() + " while processing " + controlFile, 0);
        }
    }

    private String readControlFile(String controlFile, HashSet<String> visited)
        throws FileNotFoundException, IOException, ParseException {
        
        // Prevent #include recursion
        if (visited.contains(controlFile))
            throw new ParseException("", 0); //Information gets added in later
        visited.add(controlFile);

        File file = openFile(controlFile);
        BufferedReader ctlReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line;
        StringBuilder buffer = new StringBuilder();

        try {
            int lineNum = 0;
            while ((line = ctlReader.readLine()) != null) {
                lineNum++;
                if (line.startsWith("#include ")) {
                    String includeFile = line.substring(9).trim();
                    try {
                        String includeLines = readControlFile(includeFile, visited);
                        buffer.append(includeLines);
                        buffer.append('\n');                        
                    }
                    catch (ParseException e) {
                        throw new ParseException("Detected recursion from #include of "
                                    + includeFile + " on line " + lineNum
                                    + " of " + controlFile, 0);
                    }
                }
                else if (!line.isEmpty() && !line.startsWith("#")
                        && !line.startsWith("!")) {
                    buffer.append(line);
                    buffer.append('\n');
                }
            }
        }
        finally {
            ctlReader.close();
        }
        
        visited.remove(controlFile);

        return buffer.toString().trim();
    }
    
    /**
     * Parses a delimiter-separated row of data into a string array
     * @param row The data row
     * @param delim The delimiter separating records
     * @return The fields of data as a string array
     * @throws ParseException if there was a problem parsing the row
     * @throws IOException 
     */
    private String[] separateFields(String row, char delim) throws ParseException, IOException {
        int startPos = 0;
        FieldParsingState state = FieldParsingState.BEGIN_FIELD;
        ArrayList<String> ret = new ArrayList<String>();

        for (int pos = 0; pos < row.length(); pos++) {
            char c = row.charAt(pos);

            switch (state) {
            case BEGIN_FIELD:
                if (c == '"') {
                    state = FieldParsingState.IN_QUOTES;
                    startPos = pos + 1;
                }
                else if (c == delim) {
                    ret.add("");
                }
                else if (!Character.isWhitespace(c)) {
                    state = FieldParsingState.IN_FIELD;
                    startPos = pos;
                }
                break;

            case IN_QUOTES:
                if (c == '"') {
                    state = FieldParsingState.END_QUOTES;
                }
                break;

            case END_QUOTES:
                if (c == '"') {
                    state = FieldParsingState.IN_QUOTES;
                }
                else if (c == delim) {
                    state = FieldParsingState.BEGIN_FIELD;
                    ret.add(row.substring(startPos, pos - 1).replace("\"\"", "\""));
                }
                else if (Character.isWhitespace(c)) {
                    state = FieldParsingState.END_FIELD;
                    ret.add(row.substring(startPos, pos - 1).replace("\"\"", "\""));
                }
                else {
                    throw new ParseException("Unexpected character at position " + (pos + 1), pos + 1);
                }
                break;

            case IN_FIELD:
                if (c == delim) {
                    state = FieldParsingState.BEGIN_FIELD;
                    ret.add(row.substring(startPos, pos).trim().replace("\"\"", "\""));
                }
                break;

            case END_FIELD:
                // Need to wait for a delimiter
                if (c == delim) {
                    state = FieldParsingState.BEGIN_FIELD;
                }
                else if (!Character.isWhitespace(c)) {
                    throw new ParseException("Unexpected character at position " + (pos + 1), pos + 1);
                }
            }
        }

        // End case handling
        switch (state) {
        case BEGIN_FIELD:
            ret.add("");
            break;

        case IN_QUOTES:
            //Note: old mload allowed this... (triggered here by end of file)
            throw new ParseException("Unmatched double quote at position " + (startPos - 1), startPos - 1);

        case END_QUOTES:
            ret.add(row.substring(startPos, row.length() - 1).trim().replace("\"\"", "\""));
            break;

        case IN_FIELD:
            ret.add(row.substring(startPos).replace("\"\"", "\"").trim().replace("\"\"", "\""));
            break;
        }

        // Replace ' with ''
        String[] retArray = ret.toArray(new String[ret.size()]);
        for (int pos = 0; pos < retArray.length; pos++) {
            retArray[pos] = retArray[pos].replace("'", "''");
        }
        
        if (debugMsg) {
            for (int i = 0; i < retArray.length; i++) {
                String value = retArray[i];
                String key = varBaseName + (i + 1);
                outStream.append(key);
                outStream.append(" = ");
                outStream.append(value);
                outStream.append('\n');
            }
        }

        return retArray;
    }
 
    /**
     * Substitutes a row of data into the ctlFileLines and the executes the
     * resulting command
     * @param fields The row of data to be substituted into the command
     * @param errCount The number of errors encountered thus far.  Used to
     *                 determine whether or not to print an error message
     * @throws MocaException if the command being executed has an exception
     * @throws IOException 
     */
    private void subDataAndExec(String[] fields, int errCount) throws MocaException, IOException {
        Pattern vars = Pattern.compile(varBaseName + "(\\d*)@");
        Matcher varMatcher = vars.matcher(ctlFileLines);
        StringBuilder buffer = new StringBuilder();
        int lastPos = 0;

        // Replace all @VAR#@ matches with the field
        while (varMatcher.find()) {
            buffer.append(ctlFileLines.substring(lastPos, varMatcher.start()));

            int column = Integer.parseInt(varMatcher.group(1)) - 1;
            if (column < fields.length) {
                buffer.append(fields[column]);
                lastPos = varMatcher.end();
            }
            else {
                lastPos = varMatcher.start();
            }
        }
        // Add on remaining characters
        buffer.append(ctlFileLines.substring(lastPos));
        
        String cmdStr = buffer.toString();
        if (debugMsg) {
            outStream.append(cmdStr);
            outStream.append("\n\n");
        }
        
        try {
            connection.executeCommand(cmdStr);
        }
        catch (MocaException e) {
            if (!silent && (maxErrors == 0 || errCount < maxErrors)) {
                buffer = new StringBuilder();
                buffer.append("ERROR: ");
                buffer.append(e.getErrorCode());
                buffer.append("\n ");
                buffer.append(cmdStr);
                buffer.append('\n');
                outStream.append(buffer);
                outStream.append('\n');
            }
            throw e;
        }
    }
    
    private final static String CLIENT_KEY = "mload#isfmwfbwptbbcksxiulhnrxoktvbpg";
}
