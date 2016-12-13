/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.coverage.repository;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.coverage.parse.CoverageMocaParser;
import com.redprairie.moca.server.exec.CommandSequence;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.LocalSyntaxCommand;

/**
 * Unfortunately this class must extend LocalSyntaxCommand since we do instanceof
 * in various places.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class LocalSyntaxCoverageCommand extends LocalSyntaxCommand {
    private static final long serialVersionUID = -1275122588630953011L;

    /**
     * @param name
     * @param level
     */
    public LocalSyntaxCoverageCommand(String name, ComponentLevel level, 
        MocaClassData classData, int lineOffset) {
        super(name, level);
        _data = classData;
        _lineOffset = lineOffset;
    }
    
    // @see com.redprairie.moca.server.repository.LocalSyntaxCommand#executeWithContext(com.redprairie.moca.server.exec.ServerContext)
    @Override
    protected MocaResults executeWithContext(ServerContext ctx)
            throws MocaException {
        CommandSequence compiled;
        synchronized(this) {
            if (_compiled == null) {
                _compiled = new CoverageMocaParser(_syntax, _data, false, 
                    _lineOffset).parse(); 
            }
            compiled = _compiled;
        }
        
        return compiled.execute(ctx);
    }
    
    private transient CommandSequence _compiled;
    private final MocaClassData _data;
    private final int _lineOffset;
}
