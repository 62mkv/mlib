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

package com.redprairie.moca.applications;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

import org.springframework.aop.framework.ProxyFactory;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.applications.mload.Mload;
import com.redprairie.moca.client.LoginFailedException;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.util.AppUtils;
import com.redprairie.moca.util.InterruptedIOExceptionAdvice;
import com.redprairie.moca.util.Options;
import com.redprairie.moca.util.OptionsException;
import com.redprairie.moca.util.SeparateTargetFactory;

/**
 * This is the mainline for the mload executable.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author gvaneyck
 * @version $Revision$
 */
public class MloadMain {
    
    public static final String COMMAND_LINE_OPTIONS = "h?c:ghmsHTUD:d:e:l:f:v:o:t:Ca:u:w:";
    
    public static void shortUsage()
    {
        System.out.print(
                "Usage: mload -c <control file> [ -ghmsHTU ]\n" +
                "             [ -D <data dir> ] [ -d <data file> ]\n" +
                "             [ -e <number> ] [ -l <lines> ]\n" +
                "             [ -f <delim> ] [ -v <var name> ]\n" +
                "             [ -o <trace file> ] [ -t <trace levels> ]\n" +
                "             [ -C [ -a <host> ] -u <user> -w <password> ]\n");
    }

    public static void longUsage()
    {
        shortUsage();
        System.out.print(
                "\t-c <file name>     Control file containing components\n" +
                "\t-g                 Debug messages (print commands)\n" +
                "\t-h                 Show help\n" +
                "\t-m                 Control file is a master control file\n" +
                "\t-s                 Silent mode - do not show errors\n" +
                "\t-H                 Header line defines column names\n" +
                "\t-T                 Generate Oracle trace file\n" +
                "\t-U                 Database upgrade in progress\n" +
                "\t                      Set by the installer during upgrades\n" +
                "\t                      to give precedence to updates vs. inserts\n" +
                "\t-D <data dir>      Data directory (Default is the current directory)\n" +
                "\t-d <data files>    Data file(s) (Default is '*.csv')\n" +
                "\t-e <number>        Max. number of errors to show\n" +
                "\t-l <number>        Lines to process before commit\n" +
                "\t                      >0 for number of lines before commit\n" +
                "\t                      =0 to commit after each data file\n" +
                "\t-f <delim>         Field delimiter (Default is comma)\n" +
                "\t-v <var name>      Variable name (Default is '@VAR')\n" +
                "\t-o <trace file>    Trace file pathname\n" +
                "\t-t <trace levels>  Trace level switches\n" +
                "\t-C                 Client mode\n" +
                "\t-a <host>          Host URL (if in client mode)\n" +
                "\t                      Default is URL in registry or\n" +
                "\t                      http://localhost:4500/moca\n" +
                "\t-u <user>          Login user (if in client mode)\n" +
                "\t-w <password>      Login password (if in client mode)\n" +
                "\n" +
                "Trace Level Switches\n" +
                "   W - Application Flow Messages\n" +
                "   M - Manager Messages\n" +
                "   R - Performance Statistics\n" +
                "   A - Server Arguments\n" +
                "   X - Server Messages\n" +
                "   S - SQL Calls\n");
    }
    
    /**
     * This is the mainline for the mload application
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        Options opts = null;
        try {
            opts = Options.parse(COMMAND_LINE_OPTIONS, args);
        }
        catch (OptionsException e) {
            System.err.println("Invalid option: " + e.getMessage());
            shortUsage();
            System.exit(1);
        }
        
        if (opts.isSet('h') || opts.isSet('?')) {
            longUsage();
            return;
        }
        
        if (!opts.isSet('m') && !opts.isSet('c')) {
            shortUsage();
            System.exit(1);
        }
        
        System.out.print(AppUtils.getStartBanner("MLOAD"));
        
        try {
            ServerUtils.setupDaemonContext("MloadMain", true, true);
        }
        catch (SystemConfigurationException e1) {
            e1.printStackTrace();
            System.exit(1);
        }

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
        
        int errorCount = 0;
        try {
            errorCount = mload.load();
        }
        catch (FileNotFoundException e) {
            printError(e, args);
            System.exit(1);
        }
        catch (LoginFailedException e) {
            printError(e, args);
            System.exit(1);
        }
        catch (IOException e) {
            printError(e, args);
            System.exit(1);
        }
        catch (OptionsException e) {
            printError(e, args);
            System.exit(1);
        }
        catch (MocaException e) {
            printError(e, args);
            System.exit(1);
        }
        catch (ParseException e) {
            printError(e, args);
            System.exit(1);
        }
        
        System.exit(errorCount);
    }
    
    private static void printError(Exception e, String[] args) {
        System.err.print("Error: ");
        System.err.print(e.getMessage());
        System.err.println();
        System.err.println();
    }
}
