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

package com.sam.moca.applications.dbupgrade;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sam.moca.MocaException;
import com.sam.moca.MocaInterruptedException;
import com.sam.moca.applications.installsql.InstallSql;
import com.sam.moca.applications.mload.Mload;
import com.sam.moca.applications.msql.Msql;
import com.sam.moca.applications.msql.MsqlEventHandler;
import com.sam.moca.applications.preprocessor.MocaCPreProcessor;
import com.sam.moca.client.LoginFailedException;
import com.sam.moca.components.base.GenericException;
import com.sam.moca.util.MocaUtils;
import com.sam.moca.util.Options;
import com.sam.moca.util.OptionsException;

/**
 * This is the interface to calling into the dbupgrade.  This will call the
 * upgrade scripts in a directory as they should be.  This can be ran in 3
 * modes.  These are represented by the 3 available functions on this class.
 * 
 * <br>
 * {@link #processVersion(String)} : This will run all the directories after and
 * including the version specified.
 * <br>
 * {@link #processDirectory(String)} : This will run all the scripts in the
 * specified directory
 * <br>
 * {@link #processFileName(String)} : This will run the specified script file
 * <br>
 * 
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class Dbupgrade {
    
    public Dbupgrade(Appendable normalOutput, Appendable errorOutput, 
            boolean showWarnings) {
        super();
        _normalOutput = normalOutput;
        _errorOutput = errorOutput;
        _showWarnings = showWarnings;
    }

    /**
     * @param directoryName
     * @throws IOException
     * @throws FileNotFoundException
     */
    public List<MocaException> processDirectory(String directoryName) throws IOException, 
            FileNotFoundException {
        File fileDirectory = new File(directoryName);
        
        // If the directory specified doesn't exist or isn't a directory
        // then we error telling them that
        if (!fileDirectory.exists() || !fileDirectory.isDirectory()) {
            throw new FileNotFoundException(fileDirectory.getCanonicalPath());
        }
        
        return processDirectory(fileDirectory);
    }
    
    /**
     * Will process all the script files in this directory.  The directory should
     * be guaranteed to be a directory by the caller and is not checked here.
     * @param directory The directory to start processing
     * @throws IOException 
     */
    private List<MocaException> processDirectory(File directory) throws IOException {
        _normalOutput.append(_separatorStars);
        _normalOutput.append("\nProcessing scripts from "); 
        _normalOutput.append(directory.getCanonicalPath());
        _normalOutput.append('\n');
        _normalOutput.append(_separatorStars);
        _normalOutput.append("\n\n");
        
        File[] acceptedFiles = directory.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                // We only want to check if it is a file
                if (file.isFile()) {
                    String name = file.getName();
                    if (name.equals("dbupgrade.pl")) {
                        return false;
                    }
                    
                    // We split them on the dot
                    String[] periodSeparted = name.split("\\.");
                    
                    // If the file extension ends with pl, sql, msql and mload
                    if (Arrays.asList("pl", "sql", "msql", "mload").contains(
                            periodSeparted[periodSeparted.length - 1]
                                    .toLowerCase())) {
                        return true;
                    }
                }
                return false;
            }
            
        });
        
        // Now we sort the file so they come back in the correct order
        Arrays.sort(acceptedFiles, new Comparator<File>() {

            @Override
            public int compare(File o1, File o2) {
                String file1 = o1.getName();
                String file2 = o2.getName();
                
                // We want to sort the file lexicographically
                return file1.compareTo(file2);
            }
            
        });
        
        List<MocaException> exceptions = new ArrayList<MocaException>();
        for (File file : acceptedFiles) {
            exceptions.addAll(processFile(file));
        }
        
        _normalOutput.append(_separatorStars);
        _normalOutput.append("\nProcessed scripts from ");
        _normalOutput.append(directory.getCanonicalPath());
        _normalOutput.append("\n");
        _normalOutput.append(_separatorStars);
        _normalOutput.append("\n\n");
        
        return exceptions;
    }
    
    /**
     * This will actually process a file according to it's extension.  It will
     * pass the file to the appropriate app.
     * 
     * <br><br>
     * .pl -> perl executable
     * <br>
     * .sql -> installsql
     * <br>
     * .msql -> msql
     * <br>
     * .mload -> mload
     * <br>
     * @param file The file to process
     * @throws IOException This occurs if the stream could not be written to
     *         or if the file was not found
     */
    private List<MocaException> processFile(File file) throws IOException {
        _normalOutput.append(_separatorDashes);
        _normalOutput.append("\nProcessing ");
        _normalOutput.append(file.getName());
        _normalOutput.append('\n');
        
        String fileName = file.getName();
        
        String[] periodSeparted = fileName.split("\\.");
        List<MocaException> exceptions = new LinkedList<MocaException>();
        
        // If there were no periods then there is no extension
        if (periodSeparted.length == 1) {
            if (_showWarnings) {
                _errorOutput.append("\nWARNING: No file extension found - " +
                		"skipping file...\n");
            }
            
            return exceptions;
        }
        
        String extension = periodSeparted[periodSeparted.length - 1];
        
        // We set the current working directory of the parent of the file
        // so that execute os command can work correctly
        MocaUtils.currentContext().putSystemVariable("CWD", 
                file.getParentFile().getCanonicalPath());
        
        if (extension.equalsIgnoreCase("pl")) {
            ProcessBuilder pb = new ProcessBuilder("perl", 
                    fileName);
            
            pb.directory(file.getParentFile());
            
            // TODO we have the issue that if the perl script wants input; we can't give it any and it will freeze
            Process perl = pb.start();
            
            Thread outputThread;
            Thread errorThread;
            {
                BufferedReader processOutput = new BufferedReader(
                        new InputStreamReader(perl.getInputStream(), Charset.defaultCharset()));
                
                outputThread = new OutputReader(processOutput, _normalOutput);
                
                outputThread.start();
                
                BufferedReader processError = new BufferedReader(
                        new InputStreamReader(perl.getErrorStream(), Charset.defaultCharset()));
                
                errorThread = new OutputReader(processError, _errorOutput);
                
                errorThread.start();
            }
            
            try {
                perl.waitFor();
            }
            catch (InterruptedException e) {
                // We shouldn't be interrupted, but kill perl
                // just in case
                perl.destroy();
                
                throw new MocaInterruptedException(e);
            }
            finally {
                // We also want to interrupt our reader threads in case we were
                // killed before they were done
                outputThread.interrupt();
                errorThread.interrupt();
            }
            
            // Now add for each value returned into the exception container
            for (int i = 0; i < perl.exitValue(); ++i) {
                exceptions.add(new GenericException("Perl script "
                        + "encountered an error"));
            }
        }
        else if (extension.equalsIgnoreCase("sql")) {
            InstallSql installSql = new InstallSql(_normalOutput);
            
            try {
                exceptions = installSql.processFiles(Arrays.asList(
                        file.getAbsolutePath()), 
                        new MocaCPreProcessor(file.getParent()), false);
            }
            catch (MocaException e) {
                _errorOutput.append("There was a problem obtaining the " +
                		"database information :");
                _errorOutput.append(e.getMessage());
                _errorOutput.append('\n');
                exceptions.add(e);
            }
        }
        else if (extension.equalsIgnoreCase("msql")) {
            Msql msql = new Msql(false, false);
            
            msql.setCharset(Charset.forName("UTF-8"));
            
            msql.addEventHandler(new MsqlEventHandler() {

                @Override
                public void notifyCommandExecution(String command, long duration) {
                    
                }

                @Override
                public void notifyLine(String line, int multiLineCount) {
                    
                }

                @Override
                public void traceEvent(String message, TraceType type) {
                    if (type != TraceType.NONINTERACTIVE) {
                        try {
                            _normalOutput.append(message);
                        }
                        catch (IOException e) {
                            try {
                                _errorOutput.append("There was a problem writing " +
                                		"to normal output :");
                                _errorOutput.append(e.getMessage());
                                _errorOutput.append('\n');
                            }
                            catch (IOException e1) {
                                _logger.warn("Could not write to normal or " +
                                		"error output!", e1);
                            }
                        }
                    }
                }

                @Override
                public void updatePrompt(String prompt) {
                    
                }
                
            });
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF-8"));
            try {
                exceptions = msql.executeCommands(reader);
            }
            finally {
                try {
                    reader.close();
                }
                catch (IOException e) {
                    _logger.warn("Unexpected IO Exception", e);
                }
            }
        }
        else if (extension.equalsIgnoreCase("mload")) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF-8"));
            List<String> arguments = new ArrayList<String>();
            
            try {
                String line;
                
                // Now we read through the file to get all the arguments
                while ((line = reader.readLine()) != null) {
                    // Split it on equal and then throw it in the arguments
                    String[] equalsSplit = line.split("=");
                    
                    for (String equalSplit : equalsSplit) {
                        arguments.add(equalSplit);
                    }
                }
            }
            finally {
                try {
                    reader.close();
                }
                catch (IOException e) {
                    _logger.warn("Unexpecte IO Exception", e);
                }
            }
            
            Exception thrown = null;
            try {
                Mload mload = new Mload(Options.parse(Mload.COMMAND_LINE_OPTIONS, 
                        arguments.toArray(new String[arguments.size()])), 
                        file.getParent());
                
                mload.setStreams(_normalOutput, _errorOutput);

                int mloadErrorCount = mload.load();
                
                for (int i = 0; i < mloadErrorCount; ++i) {
                    exceptions.add(new GenericException("Mload script "
                            + "encountered an error"));
                }
            }
            catch (OptionsException e) {
                thrown = e;
            }
            catch (LoginFailedException e) {
                thrown = e;
            }
            catch (MocaException e) {
                thrown = e;
            }
            catch (ParseException e) {
                thrown = e;
            }
            catch (IOException e) {
                thrown = e;
            }
            
            if (thrown != null) {
                // We want to print this guy out to the error output.
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                thrown.printStackTrace(pw);
                _errorOutput.append(sw.toString());
                exceptions.add(new MocaException(2, "Mload script "
                        + "encountered an error", thrown));       
            }
        }
        else {
            if (_showWarnings) {
                _errorOutput.append("\nWARNING: Unknown file extension found " +
                		"- skipping file...\n");
            }
        }
        
        if (!exceptions.isEmpty()) {
            _normalOutput.append("\nERROR: ");
            _normalOutput.append(String.valueOf(exceptions.size()));
            _normalOutput.append(" error(s) occurred processing file\n");
            _normalOutput.append("       Please check for severity!\n");
        }
        
        _normalOutput.append(_separatorDashes);
        _normalOutput.append("\n\n");
        
        return exceptions;
    }
    
    private class OutputReader extends Thread {

        public OutputReader(BufferedReader reader, Appendable appendable) {
            super();
            _bufferedReader = reader;
            _appendable = appendable;
        }
        
        @Override
        public void run() {
            String line;
            
            try {
                while ((line = _bufferedReader.readLine()) != null) {
                    _appendable.append(line);
                    _appendable.append('\n');
                }
            }
            catch (IOException e) {
                try {
                    _errorOutput.append("There was a problem writing " +
                                    "to normal output :");
                    _errorOutput.append(e.getMessage());
                    _errorOutput.append('\n');
                }
                catch (IOException e1) {
                    _logger.warn("Could not write to normal or " +
                                    "error output!", e1);
                }
            }
        }
        
        private final BufferedReader _bufferedReader;
        private final Appendable _appendable;
    }
    
    /**
     * This will process the current directory trying to locate the named
     * file.  It will then process that file as the extension dictates.
     * @param fileName The name of the file to process
     * @throws IOException This will occur if there was any problem writing
     *         to the output stream
     * @throws FileNotFoundException this is thrown if the file doesn't exist
     */
    public List<MocaException> processFileName(String fileName) throws IOException, 
            FileNotFoundException {
        
        File file = new File(".", fileName);
        
        // If the file specified doesn't exist or isn't a file
        // then we error telling them that
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException(file.getCanonicalPath());
        }
        
        return processFile(file);
    }
    
    /**
     * This will process the current working directory with the given version.
     * It will first determine all the directories that are in the correct
     * format and find which are equal or would take place after the version
     * provided.
     * @param version The version to upgrade from
     * @throws IOException This will occur if there was any problem writing
     *         to the output stream
     */
    public List<MocaException> processVersion(String version) throws IOException {
        String[] versionNumbers = version.split("\\.");
        
        if (versionNumbers.length > 3) {
            version = versionNumbers[0] + "." + versionNumbers[1] + "."
                    + versionNumbers[2];
        }
        
        _normalOutput.append("Upgrading from version ");
        _normalOutput.append(version);
        _normalOutput.append("\n\n");
        
        File currentDirectory = new File(".");
        final long directoryNormalized = getLongValueForMatcher(version);
        
        File[] directories = currentDirectory.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                // We only want to process directories
                if (file.isDirectory()) {
                    // Some directories are "special" and we ignore them.
                    if (file.getName().equals("CVS") ||
                        file.getName().equals(".svn") ||
                        file.getName().equals("42to50") ||
                        file.getName().equals("common")) {          
                            return false;
                    }
                    
                    long currentDirectoryNormalized = getLongValueForMatcher(
                            file.getName());
                    
                    // Get ones with larger or equal numbers than our version
                    if (directoryNormalized <= currentDirectoryNormalized) {
                        return true;
                    }
                }
                
                return false;
            }
        });
        
        // Need to sort the directories to get the proper upgrade sequence
        List<File> sortedDirs = new ArrayList<File>(Arrays.asList(directories));
        
        // Now sort the list by versions
        Collections.sort(sortedDirs, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                // We need to worry about overflow here,
                // so just normalize to [-1, 0, 1]
                long diff = getLongValueForMatcher(o1.getName()) - 
                            getLongValueForMatcher(o2.getName());
                return (diff > 0 ? 1 : diff < 0 ? -1 : 0);
            }
        });
        
        _normalOutput.append("Upgrade sequence\n\n");

        for (File directoryName : sortedDirs) {
            _normalOutput.append("    ");
            _normalOutput.append(directoryName.getName());
            _normalOutput.append('\n');
        }
        
        _normalOutput.append('\n');
        
        List<MocaException> exceptions = new ArrayList<MocaException>();
        
        for (File directory : sortedDirs) {
            
            exceptions.addAll(processDirectory(directory));
        }
        
        return exceptions;
    }
    
    /**
     * This will normalize the name of a directory to an integer value to 
     * tell if this directory is to be ran after another
     * 
     * <pre>
     * The following algorithm is being used to normalize the version numbers:
     * 
     *      1. Get the tokens of all four parts of the version.  If a fourth 
     *         number is not provided, assume it is 0.
     * 
     *      2. Zero-pad every number to 4 digits.
     * 
     *      3. Create a fifth part that signifies if this version is an "alpha",
     *         "beta", "to", both or neither.
     * 
     *      4. Create a sixth part, which is the "alpha" or "beta" version 
     *         zero-padded to 4 digits.
     * </pre>
     * 
     * @param matcher The matcher that is correct for the directories
     * @return the integer value for this matcher
     */
    private long getLongValueForMatcher(String fileName) {
        long normalizeValue = 0;
        
        Matcher directoryMatcher = _directoryPattern.matcher(fileName);
        
        if (directoryMatcher.matches()) {
            String toDirectory = directoryMatcher.group(1);
            boolean isToDirectory = toDirectory == null ? false : 
                !toDirectory.isEmpty();
            
            // Put the major version first
            normalizeValue += Integer.parseInt(directoryMatcher.group(2));
            normalizeValue *= 1000;
            // Then comes the minor version
            normalizeValue += Integer.parseInt(directoryMatcher.group(3));
            normalizeValue *= 1000;
            // Then the service pack version
            normalizeValue += Integer.parseInt(directoryMatcher.group(4));
            normalizeValue *= 1000;
            
            String hotfixVersion = directoryMatcher.group(5);
            if (hotfixVersion != null && !hotfixVersion.trim().isEmpty()) {
                normalizeValue += Integer.parseInt(hotfixVersion);
            }
            normalizeValue *= 1000;
            
            // Now we apply the value for alpha beta and to version
            // to and alpha/beta then add nothing
            // alpha/beta then add 1000
            // to then add 2000
            // neither then add 3000
            String alphaBeta = directoryMatcher.group(6);
            if (alphaBeta != null && !alphaBeta.trim().isEmpty()) {
                if (!isToDirectory) {
                    normalizeValue += 1000;
                }
            }
            else {
                if (isToDirectory) {
                    normalizeValue += 2000;
                }
                else {
                    normalizeValue += 3000;
                }
            }
            normalizeValue *= 1000;
            
            String alphaBetaVersion = directoryMatcher.group(7);
            if (alphaBetaVersion != null && !alphaBetaVersion.trim().isEmpty()) {
                normalizeValue += Integer.parseInt(alphaBetaVersion);
            }
        }
        
        return normalizeValue;
    }

    /**
     * This matcher is to find a directory in the pattern that allows for 
     * (to_)NNNN.NNNN.NNNN(.NNNN)([ab]NNNN).
     * 
     * The directory must start with 4 numbers or to_.  It must then have 3-4
     * sets of numbers varying from 1-4 numbers.  It then can also have an alpha
     * or beta tag appended with it; with 4 numbers on it
     * 
     * It also allows for capturing groups so that we can decipher what the
     * directory is actually like
     * 
     * Example: to_2009.1.0.10a2
     * 
     * Group 1: contains to_ if that was provided
     * Group 2: shows the major version ie. 2009
     * Group 3: shows the minor version ie. 1
     * Group 4: shows the service pack version ie. 0
     * Group 5: contains the hotfix version ie. 10 if it was provided
     * Group 6: contains the letter if it was an alpha (a) or beta (b) tag ie. a
     * Group 7: contains the build for that alpha if present ie. 2
     */
    private final static Pattern _directoryPattern = 
        Pattern.compile("(to_)?(\\d{1,4})\\.(\\d{1,4})\\.(\\d{1,4})(?:\\.(\\d{1,4}))?(?:([ab])(\\d{1,4}))?");
    
    private final static String _separatorStars  = 
        "*******************************************************************************";
    
    private final static String _separatorDashes = 
        "-------------------------------------------------------------------------------";
    
    private final static Logger _logger = LogManager.getLogger(Dbupgrade.class);
    
    private final boolean _showWarnings;
    private final Appendable _normalOutput;
    private final Appendable _errorOutput;
}
