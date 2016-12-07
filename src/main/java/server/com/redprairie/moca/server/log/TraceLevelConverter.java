/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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

package com.redprairie.moca.server.log;

import com.redprairie.moca.MocaTrace;
/**
 * A utility class that converts the
 * existing MOCA trace levels to corresponding
 * package filters. Note that filters *exclude* messages
 * so we need to be checking for the switches that are
 * not set instead of the ones that are set.
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
public class TraceLevelConverter {
    public static int getTraceLevelsFromString(String levels) {
        int total = 0;
        
        if (levels != null) {
            for (char c : levels.toCharArray()) {
                // If we find star anywhere just return max immediately.
                if (c == '*') {
                    return MocaTrace.getAllLevels();
                }
                int charLevel = getTraceLevelFromCharacter(c);
                
                // If it is greater than 0 then add it to our value, but we must
                // use the or inclusive operator in case if the same character
                // is passed twice.
                if (charLevel > 0) {
                    total |= charLevel;
                }
            }
        }
        
        return total;
    }
    
    /**
     * This method will convert a given character to the equivalent MocaTrace 
     * object.  A -1 will be returned if the level is not supported.
     * @param level The level to convert to a MocaTrace
     * @return The equivalent MocaTrace level
     */
    public static int getTraceLevelFromCharacter(char level) {
        int trace;
        switch (level) {
        case 'M':
            trace = MocaTrace.MGR;
            break;
        case 'W':
            trace = MocaTrace.FLOW;
            break;
        case 'S':
            trace = MocaTrace.SQL;
            break;
        case 'X':
            trace = MocaTrace.SERVER;
            break;
        case 'A':
            trace = MocaTrace.SRVARGS;
            break;
        case 'R':
            trace = MocaTrace.PERF;
            break;
        default:
            trace = -1;
            break;
        }
        return trace;
    }
}
