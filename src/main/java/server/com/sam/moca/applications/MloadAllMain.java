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

package com.sam.moca.applications;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.aop.framework.ProxyFactory;

import com.sam.moca.MocaException;
import com.sam.moca.applications.mload.Mload;
import com.sam.moca.client.LoginFailedException;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.util.AppUtils;
import com.sam.moca.util.InterruptedIOExceptionAdvice;
import com.sam.moca.util.Options;
import com.sam.moca.util.OptionsException;
import com.sam.moca.util.SeparateTargetFactory;
import com.sam.util.WildcardFilenameFilter;

/**
 * This is the mainline for the mload_all executable.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author gvaneyck
 * @version $Revision$
 */
public class MloadAllMain {

    public static final String COMMAND_LINE_OPTIONS = "hsUa:C:L:M:";
    private static final Pattern _matchSpaces = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
    private static PrintStream out = null, err = null;

    public static void printUsage() {
        System.out
            .print("Usage: mload_all [-hsU]\n"
                    + "                 [-a <address>] [-C ctl_ext] [-L dir] [-M dir]\n"
                    + "                 [dir[=mload_options] [dir...]]\n"
                    + "\t-h            Print this message\n"
                    + "\t-s            Silent mode\n"
                    + "\t-U            Upgrade mode\n"
                    + "\t                 Set by the installer during upgrades\n"
                    + "\t                 to given precedence to updates vs. inserts\n"
                    + "\t-a <address>  IP address to connect to  (for client mode)\n"
                    + "\t-C <ext>      Control file's extension (default .ctl)\n"
                    + "\t-L <dir>      Log file directory\n"
                    + "\t-M <dir>      Create and use master control file in this directory. (debug mode)\n");
    }

    /**
     * This is the mainline for the mload_all application
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        Options opts = null;
        try {
            opts = Options.parse(COMMAND_LINE_OPTIONS, args);
        }
        catch (OptionsException e) {
            System.err.println("Invalid option: " + e.getMessage());
            printUsage();
            System.exit(1);
        }

        if (opts.isSet('h')) {
            printUsage();
            System.exit(1);
        }

        System.out.print(AppUtils.getStartBanner("MLOAD All"));

        try {
            ServerUtils.setupDaemonContext("MloadAllMain", true, true);
        }
        catch (SystemConfigurationException e1) {
            e1.printStackTrace();
            System.exit(1);
        }

        DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyMMdd_HHmmss");
        String startTime = dateFormat.print(new Date().getTime());

        String ctlFileExt = (opts.isSet('C') ? opts.getArgument('C') : "ctl");
        String address = (opts.isSet('a') ? "-a " + opts.getArgument('a') : "");
        String silent = (opts.isSet('s') ? " -s" : "");
        String upgrade = (opts.isSet('U') ? " -U" : "");

        Writer masterCtl = null;
        File masterFile = null;
        int errorCount = 0;
        
        try {
            if (opts.isSet('M')) {
                // In lieu of a getPID method from Java... (format is PID@Host)
                String pid = ManagementFactory.getRuntimeMXBean().getName();
    
                // Set up a master control file that will be run instead
                String controlName = "mload_all_" + pid.substring(0, pid.indexOf('@')) + ".mld";
                masterFile = new File(opts.getArgument('M') + File.separator + controlName);
    
                try {
                    masterCtl = new OutputStreamWriter(
                        new FileOutputStream(masterFile), "UTF-8");
                    masterCtl.write("#MLOAD\n");
                }
                catch (IOException e) {
                    System.err.println("Can not create control file -- "
                            + masterFile.getName());
                    System.exit(1);
                }
            }
    
            // Standard output streams are the default
            out = System.out;
            err = System.err;
            if (opts.isSet('L')) {
                // Set up the LOG and ERROR filenames
                String dir = opts.getArgument('L');
                File logDir = new File(dir);
                if (!logDir.isDirectory()) {
                    System.err.println("Could not find log directory: " + dir);
                    System.exit(1);
                }
    
                String baseFile = "mload_data_" + startTime;
                File logFile = new File(logDir, baseFile + ".log");
                File errorFile = new File(logDir, baseFile + "_errors.log");
    
                // Redirect out and err to the wanted files
                try {
                    out = new PrintStream(new FileOutputStream(logFile), true, "UTF-8");
                }
                catch (FileNotFoundException e) {
                    System.err.println("Can't open '" + logFile.getName()
                            + "' for logging!");
                    System.exit(1);
                }
                catch (UnsupportedEncodingException e) {
                    System.err.println("Encoding exception:  " + e.getMessage());
                    System.exit(1);
                }
    
                try {
                    err = new PrintStream(new FileOutputStream(errorFile), true, "UTF-8");
                }
                catch (FileNotFoundException e) {
                    System.err.println("Can't open '" + errorFile.getName()
                            + "' for errors!");
                    System.exit(1);
                }
                catch (UnsupportedEncodingException e) {
                    System.err.println("Encoding exception:  " + e.getMessage());
                    System.exit(1);
                }
            }
    
            // Either the user specified directories via command line options
            // or we'll process the current directory
            String[] remainArgs = opts.getRemainingArgs();
            if (remainArgs.length == 0)
                remainArgs = new String[] { ".= " };
    
            StringBuilder cmdStr;
            for (String arg : remainArgs) {
                // dir=opt pairs
                String[] vals = arg.split("=");
    
                // Iterate through all the control files in the specified directory
                File ctlDir = new File(vals[0]);
                if (!ctlDir.isDirectory()) {
                    System.err.println("Could not change to directory: " + vals[0]);
                    System.exit(1);
                }
    
                // Get the list of control files.
                File[] ctlFiles = ctlDir.listFiles(new WildcardFilenameFilter("*." + ctlFileExt));
  
                // Sort the list of control files.
                Arrays.sort(ctlFiles, new Comparator<File>() {
                    public int compare(File f1, File f2)
                    {
                        return f1.getName().compareTo(f2.getName());
                    } 
                });

                for (File ctlFile : ctlFiles) {
                    // Check for a similarly named data directory
                    String filename = ctlFile.getName();
                    File subdir = new File(ctlFile.getParent(), 
                            filename.substring(0, filename.indexOf('.')));
                    if (!subdir.isDirectory()) {
                        System.err.println("Warning: Missing data directory "
                                + ctlDir.getName() + "/" + subdir.getName() + "!");
                        continue;
                    }
    
                    // Build the command string and execute it or write it
                    cmdStr = new StringBuilder();
                    cmdStr.append(address);
                    cmdStr.append(" -H -c ");
                    cmdStr.append(String.format("\"%s\"", ctlFile.getPath()));
                    cmdStr.append(" -D ");
                    cmdStr.append(String.format("\"%s\"", subdir.getPath()));
                    cmdStr.append(silent);
                    cmdStr.append(upgrade);
                    cmdStr.append(" ");
                    cmdStr.append(vals.length > 1 ? vals[1] : "");
    
                    if (opts.isSet('M')) {
                        try {
                            masterCtl.write(cmdStr.toString() + "\n");
                        }
                        catch (IOException e) {
                            System.err.println("Could not write to master control file!");
                            System.exit(1);
                        }
                    }
                    else {
                        try {
                            errorCount += runMload(parse(cmdStr.toString()));
                        }
                        catch (OptionsException e) {
                            System.err.println("Invalid options for Mload: "
                                    + cmdStr.toString());
                            errorCount++;
                        }
                    }
                }
            }
        }
        finally {
            if (masterCtl != null) {
                try {
                    masterCtl.close();
                }
                catch (IOException e) {
                    System.err.println("Could not write to master control file!");
                    System.exit(1);
                }
            }
        }

        // If we're building a master control file, execute it
        if (opts.isSet('M')) {

            StringBuilder cmdStr = new StringBuilder();
            cmdStr.append("-m -c ");
            cmdStr.append(String.format("\"%s\"", masterFile.getPath()));

            try {
                errorCount += runMload(parse(cmdStr.toString()));
            }
            catch (OptionsException e) {
                System.err.println("Invalid options for Mload: "
                        + cmdStr.toString());
            }

            System.out.println("NOTE: Please remove temporary file "
                    + masterFile.getPath());
        }

        String endTime = dateFormat.print(new Date().getTime());
        System.out.println("Load Started at " + startTime);
        System.out.println("Load Ended   at " + endTime);
        
        // Close the file logging output streams if necessary
        out.close();
        err.close();

        System.exit(errorCount);
    }

    /**
     * This runs mload with the given options
     * @param cmdStr The command line options for mload
     * @throws OptionsException if there was an invalid option specified
     */
    private static int runMload(String[] cmdStr) throws OptionsException {
        Options opts;
        opts = Options.parse(Mload.COMMAND_LINE_OPTIONS, cmdStr);

        Mload mload = new Mload(opts);
        
        ProxyFactory factory = new ProxyFactory(new Class[]{Appendable.class});
        
        factory.addAdvice(new InterruptedIOExceptionAdvice());
        factory.setAopProxyFactory(new SeparateTargetFactory());

        // We proxy out the appendables since we don't want to worry about
        // checking every single IOException.
        // Proxy out the normal output
        factory.setTarget(System.out);
        
        Appendable normalOutput = (Appendable)factory.getProxy();

        // Then Proxy out the error output
        factory.setTarget(System.err);
        
        Appendable errorOutput = (Appendable)factory.getProxy();

        mload.setStreams(normalOutput, errorOutput);
        
        try {
            return mload.load();
        }
        catch (FileNotFoundException e) {
            printError(e, cmdStr);
            return 1;
        }
        catch (LoginFailedException e) {
            printError(e, cmdStr);
            return 1;
        }
        catch (IOException e) {
            printError(e, cmdStr);
            return 1;
        }
        catch (OptionsException e) {
            printError(e, cmdStr);
            return 1;
        }
        catch (MocaException e) {
            printError(e, cmdStr);
            return 1;
        }
        catch (ParseException e) {
            printError(e, cmdStr);
            return 1;
        }
    }
    
    private static String[] parse(String commandline){
        List<String> matchList = new ArrayList<String>();
        Matcher regexMatcher = _matchSpaces.matcher(commandline);
        while (regexMatcher.find()) {
            matchList.add(regexMatcher.group().replace("\"", ""));
        }
        return matchList.toArray(new String[matchList.size()]);
    }
    
    private static void printError(Exception e, String[] args) {
        System.err.print("Error: ");
        System.err.print(e.getMessage());
        System.err.println();
        System.err.println();
    }
}
