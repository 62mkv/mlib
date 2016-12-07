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
 * Describes a command reference in a parsing session. The parser keeps track of
 * all command references, which can be used to report warnings in case of
 * invalid command references.
 * 
 * Copyright (c) 2011 RedPrairie Corporation All Rights Reserved
 * 
 * @author derek
 */
public class CommandReference extends SyntaxPoint {
    public CommandReference(String verbNounClause, boolean override, int line, int pos) {
        super(line, pos);
        _verbNounClause = verbNounClause;
        _override = override;
    }

    /**
     * Returns the verb/noun clause associated with this reference.
     * @return
     */
    public String getVerbNounClause() {
        return _verbNounClause;
    }
    
    /**
     * Returns true if this reference is a command override (e.g. &caret;command)
     * @return
     */
    public boolean isOverride() {
        return _override;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CommandReference)) {
            return false;
        }
        CommandReference other = (CommandReference)obj;
        
        return other._verbNounClause.equals(_verbNounClause) && (other._override == _override);
    }
    
    @Override
    public int hashCode() {
        return _verbNounClause.hashCode() * 37 + (_override ? 1 : 0);
    }
    
    private final String _verbNounClause;
    private final boolean _override;
}
