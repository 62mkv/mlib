/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.sam.moca.server.expression.function;

import java.util.Map;

/**
 * A utility to map one format "language" to another.  This is useful for
 * implementing a converter to use the format language from one formatting
 * tool (e.g. Oracle's TO_CHAR/TO_DATE formats) to be used with another,
 * similar tool (e.g. Java's SimpleDateFormat).
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */

class FormatMapper {
    
    /**
     * Creates an instance of this class using the given Map to implement
     * the mappings.  Mappings can map a string to the same thing in the
     * target language.  Since the mapper is only applied once to a format
     * string, such a mapping is not considered recursive, and will work as
     * expected.  Longer keys are searched for first, so a mapping can exist
     * between YYYY and YY and the longer one will be used if it is present.
     * 
     * To indicate an unsupported language element, map a key to a null value.
     * The null will cause the entire translation step to be aborted.
     * 
     * @param mappings a Map of Strings to Strings that represent the 
     */
    public FormatMapper(Map<String, String> mappings) {
        _mappings = mappings;
        int longest = 0;
        for (String key : mappings.keySet()) {
            if (key.length() > longest) longest = key.length();
        }
        _longestKey = longest;
    }
    
    public String apply(String format) {
        int formatLength = format.length();
        
        StringBuilder output = new StringBuilder();
        
        // Go character by character, looking for keys.
        outer:
        for (int i = 0; i < formatLength; i++) {
            // Check for longer formats first
            for (int l = _longestKey; l >= 1; l--) {
                if (i + l <= formatLength) {
                    String word = format.substring(i, i + l);
                    if (_mappings.containsKey(word)) {
                        String mapping = _mappings.get(word);
                        if (mapping == null) {
                            return null;
                        }
                        else {
                            output.append(mapping);
                            i+= (l - 1);
                            continue outer;
                        }
                    }
                }
            }
            
            // If no mapping, push the character as is.
            output.append(format.charAt(i));
        }
        
        return output.toString();
    }
    
    private Map<String, String> _mappings;
    private int _longestKey;
}
