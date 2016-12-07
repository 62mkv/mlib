/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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


/**
 * Allows for replacement of delimited items in a string to produce
 * a parameterized string.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
public class StringReplacer {
    
    /**
     * Interface to allow different mechanisms to replace delimited
     * strings.  The <code>lookup</code> method will be called whenever
     * a delimited string (the key) is found by the StringReplacer.
     */
    public static interface ReplacementStrategy {
        public String lookup(String key);
    }
    
    public StringReplacer(char delim, ReplacementStrategy strategy) {
        this(String.valueOf(delim), String.valueOf(delim), strategy);
    }
    
    public StringReplacer(String prefix, String suffix, ReplacementStrategy strategy) {
        _prefix = prefix;
        _suffix = suffix;
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
        do {
            // Find the next prefix in our string 
            int prefixPos = in.indexOf(_prefix, next);
            if (prefixPos < 0) {
                out.append(in.substring(next));
                break;
            }
            
            // Append the string before the delimiter
            out.append(in.substring(next,  prefixPos));
            
            // Find the end of this reference
            int suffixPos = in.indexOf(_suffix, prefixPos + _prefix.length());
            
            if (suffixPos < 0) {
                out.append(in.substring(prefixPos));
                break;
            }
            
            // Extract the key and do the lookup
            String key = in.substring(prefixPos + _prefix.length(), suffixPos);
            String value = _strategy.lookup(key);
            
            // If null is returned, leave the parameter intact
            if (value == null) {
                out.append(in.substring(prefixPos, suffixPos + _suffix.length()));
            }
            else {
                out.append(value);
            }
            
            // Continue with the rest of the string (after the suffix)
            next = suffixPos + _suffix.length();
        } while (next < in.length());

        return out.toString();
    }
    
    //
    // Implementation
    //
    
    private final String _prefix;
    private final String _suffix;
    private final ReplacementStrategy _strategy;
}
