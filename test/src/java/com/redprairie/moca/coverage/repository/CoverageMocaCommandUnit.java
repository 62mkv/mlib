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

import java.io.Serializable;
import java.util.List;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.coverage.LineCoverage;
import com.redprairie.moca.server.exec.CommandArg;
import com.redprairie.moca.server.exec.CommandUnit;
import com.redprairie.moca.server.exec.ServerContext;

/**
 * This is a wrapper for a CommandUnit so that it captures coverage when this
 * command unit is actually executed.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class CoverageMocaCommandUnit implements CommandUnit, Serializable {
    private static final long serialVersionUID = 5121852919263357749L;
    
    public CoverageMocaCommandUnit(CommandUnit command, LineCoverage coverage) {
        _command = command;
        _lineHit = coverage;
    }

    // @see com.redprairie.moca.server.exec.ExecutableComponent#execute(com.redprairie.moca.server.exec.ServerContext)
    @Override
    public MocaResults execute(ServerContext ctx) throws MocaException {
        _lineHit.lineHit();
        return _command.execute(ctx);
    }

    // @see com.redprairie.moca.server.exec.CommandUnit#getArgList()
    @Override
    public List<CommandArg> getArgList() {
        return _command.getArgList();
    }

    // @see com.redprairie.moca.server.exec.CommandUnit#getLanguage()
    @Override
    public String getLanguage() {
        return _command.getLanguage();
    }

    // @see com.redprairie.moca.server.exec.CommandUnit#getVerbNounClause()
    @Override
    public String getVerbNounClause() {
        return _command.getVerbNounClause();
    }

    // @see com.redprairie.moca.server.exec.CommandUnit#isOverride()
    @Override
    public boolean isOverride() {
        return _command.isOverride();
    }

    // @see com.redprairie.moca.server.exec.CommandUnit#getScript()
    @Override
    public String getScript() {
        return _command.getScript();
    }

    // @see com.redprairie.moca.server.exec.CommandUnit#getSql()
    @Override
    public String getSql() {
        return _command.getSql();
    }

    private final CommandUnit _command;
    private final LineCoverage _lineHit;
}
