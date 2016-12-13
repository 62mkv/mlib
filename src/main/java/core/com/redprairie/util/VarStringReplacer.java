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

package com.redprairie.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.redprairie.util.StringReplacer.ReplacementStrategy;


/**
 * Allows for replacement of variable-oriented strings.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
public class VarStringReplacer {
    
    public VarStringReplacer(ReplacementStrategy strategy) {
        _strategy = strategy;
    }
    
    /**
     * Perform the variable substitution on the given string.
     * @param in the string on which to perform substitution.
     * @return the input string with delimited identifiers substituted with
     *         their values, according to the passed-in ReplacementStrategy.
     */
    public String translate(String in) {
        // Don't bother if a null string is passed in.
        if (in == null)
            return null;

        StringBuilder out = new StringBuilder();
        int next = 0;
        
        Matcher m = PREFIX_PATTERN.matcher(in);
        while (m.find(next)) {
            // Find the next prefix in our string
            int suffixPos = -1;
            int prefixLen = -1;
            int suffixLen = -1;
            
            int prefixPos = m.start();

            out.append(in.substring(next, prefixPos));
            
            next = prefixPos;
            
            if (in.charAt(next) == '%') {
                suffixPos = in.indexOf('%', next + 1);
                prefixLen = 1;
                suffixLen = 1;
            }
            else if (in.startsWith("${", next)) {
                suffixPos = in.indexOf('}', next + 2);
                prefixLen = 2;
                suffixLen = 1;
            }
            else if (in.charAt(next) == '$') {
                suffixPos = next + 1;
                while (suffixPos < in.length()) {
                    char c = in.charAt(suffixPos);
                    if (!Character.isLetterOrDigit(c) && c != '_') break;
                    suffixPos++;
                }
                
                prefixLen = 1;
                suffixLen = 0;
            }

            // If nothing found, quit
            if (prefixPos < 0) {
                break;
            }
            
            // Append the string before the delimiter
            out.append(in.substring(next,  prefixPos));
            
            if (suffixPos < 0) {
                break;
            }
            
            // Extract the key and do the lookup
            String key = in.substring(prefixPos + prefixLen, suffixPos);
            String value = _strategy.lookup(key);
            
            // If null is returned, leave the parameter intact
            if (value == null) {
                out.append(in.substring(prefixPos, suffixPos + suffixLen));
            }
            else {
                out.append(value);
            }
            
            // Continue with the rest of the string (after the suffix)
            next = suffixPos + suffixLen;
        }

        out.append(in.substring(next));
        return out.toString();
    }

    private final ReplacementStrategy _strategy;
    private static final Pattern PREFIX_PATTERN = Pattern.compile("[%$]");
}
