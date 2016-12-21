/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20167
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

package com.sam.moca.util;

public class LikeMatcher {
    public LikeMatcher(String pattern) {
        _p = pattern.toCharArray();
    }

    public boolean match(String s) {
        return (_testMatch(s, 0, 0)); 
    }

    //
    // Implementation
    //
    private boolean _testMatch(String s, int sPos, int pPos) {
        // Go through the entire pattern string;
        while (pPos < _p.length) {
            // If we hit a wildcard, do some special processing
            final char p = _p[pPos];
            switch(p) {
            case '%':
                // For each "substring" created by "stripping off" each 
                // matching character from the string to be matched, and
                // recursively matching it with the rest of the pattern
                // string.
                while (sPos < s.length ()) {
                    if (_testMatch(s, sPos, pPos + 1)) return true;
                    sPos++;
                }

                break;
                
            case '_':
                if (sPos == s.length()) return false;
                sPos++;
                break;
                
            default:
                // If there's pattern left, but there's no string left,
                // it's not a match.  Also, if the characters don't match 
                // each other, it's not a match (duh!)
                if (sPos == s.length() || s.charAt(sPos) != p) {
                    return false;
                }

                // Move along... 
                sPos++;
                break;
            }
            // OK, we've matched as much as we can, continue on with
            // the next pattern character.
            pPos++;
        } 

        // We've made it to the end of the pattern.  Is there string left?
        if (sPos < s.length())
            return false;
        else
            return true;
    }
    
    private final char[] _p; 
}