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

package com.redprairie.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Various tools for parsing command lines.
 * 
 * Copyright (c) 2009 RedPrairie Corporation All Rights Reserved
 * 
 * @author dinksett
 */
public class CommandLineParser {
    /**
     * Splits a command line into its component pieces. This is useful for
     * taking a single string and turning it into a collection of strings to be
     * passed to ProcessBuilder or some other method of starting up a process.
     * Single and double quotes can be used to group arguments together as a
     * single argument that contains spaces. The quotes will not be in the
     * eventual argument list.
     * 
     * @param line the line to be split apart
     * @return a <code>List</code> of strings containing the arguments from the
     *         command line, split up on whitespace.
     * @throws IllegalArgumentException This is thrown if there is unmatched
     *         quotes provided for the command line.
     */
    public static List<String> split(CharSequence line) 
            throws IllegalArgumentException {
        List<String> args = new ArrayList<String>();
        StringBuilder currentArg = null;
        int length = line.length();
        char inQuote = 0;
        
        for (int i = 0; i < length; i++) {
            char c = line.charAt(i);
            if (inQuote == 0 && Character.isWhitespace(c)) {
                inQuote = 0;
                if (currentArg != null) {
                    args.add(currentArg.toString());
                    currentArg = null;
                }
            }
            else if (inQuote == 0 && (c == '\'' || c == '"')) {
                inQuote = c;
                if (currentArg == null) {
                    currentArg = new StringBuilder();
                }
            }
            else if (c == inQuote) {
                inQuote = 0;
            }
            else {
                if (currentArg == null) {
                    currentArg = new StringBuilder();
                }
                currentArg.append(c);
            }
        }
        
        if (inQuote != 0) {
            throw new IllegalArgumentException(
                    "Unmatched quotes found in command line [" + line + "]");
        }
        
        if (currentArg != null) {
            args.add(currentArg.toString());
            currentArg = null;
        }
        
        return args;
    }
}
