/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.server.parse;

/**
 * Tracks a position in local syntax.  This class can be extended to provide
 * detailed reporting capability regarding local syntax events.  This is
 * especially useful when talking about source code warnings.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public abstract class SyntaxPoint {
    public SyntaxPoint(int line, int pos) {
        _line = line;
        _pos = pos;
    }

    /**
     * Returns the line number of the parsed string that referred to this command.
     * @return
     */
    public int getLine() {
        return _line;
    }
    
    /**
     * Returns the character position within the line that referred to this command.
     * @return
     */
    public int getPos() {
        return _pos;
    }
    
    private final int _line;
    private final int _pos;

}
