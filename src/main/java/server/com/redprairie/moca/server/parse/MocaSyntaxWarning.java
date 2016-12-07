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
 * TODO Class Description
 * 
 * Copyright (c) 2011 RedPrairie Corporation All Rights Reserved
 * 
 * @author derek
 */
public class MocaSyntaxWarning extends SyntaxPoint {
    public MocaSyntaxWarning(String type, String message, int line, int pos) {
        super(line, pos);
        _type = type;
        _message = message;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return _type;
    }

    /**
     * @return Returns the message.
     */
    public String getMessage() {
        return _message;
    }
        
    @Override
    public String toString() {
        return "line " + getLine() + "." + getPos() + ": " + _type + " - " + _message;
    }

    private final String _type;
    private final String _message;
}
