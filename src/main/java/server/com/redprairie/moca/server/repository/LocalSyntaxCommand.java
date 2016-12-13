/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.redprairie.moca.server.repository;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.server.exec.CommandSequence;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.parse.MocaParser;

/**
 * Local Syntax Command - Subclass of Command
 * A subclass to overload Command class methods
 * to exhibit localsyntax behavior
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author cjolly
 * @version $Revision$
 */
public class LocalSyntaxCommand extends Command {
  
    /**
     *  Constructor
     */
    public LocalSyntaxCommand(String name, ComponentLevel level) {
        super(name, level);
    }
    
    // @see com.redprairie.moca.server.repository.Command#getSyntax()
    public String getSyntax() {
        return _syntax;
    }

    // @see com.redprairie.moca.server.repository.Command#setSyntax(java.lang.String)
    public void setSyntax(String syntax) {
         _syntax=syntax;
    }
    
    // @see com.redprairie.moca.server.repository.Command#getType()
    
    @Override
    public CommandType getType() {
        return CommandType.LOCAL_SYNTAX;
    }
    
    // @see
    // com.redprairie.moca.server.repository.Command#execute(com.redprairie.moca.MocaContext)
    @Override
    protected MocaResults executeWithContext(ServerContext ctx) throws MocaException {
        CommandSequence compiled;
        synchronized(this) {
            if (_compiled == null) {
                _compiled = new MocaParser(_syntax).parse(); 
            }
            compiled = _compiled;
        }
        
        return compiled.execute(ctx);
    }
    
    //
    // Implementation
    //
    protected String _syntax;
    protected transient CommandSequence _compiled;
    private static final long serialVersionUID = 4070528719462300157L;
}
