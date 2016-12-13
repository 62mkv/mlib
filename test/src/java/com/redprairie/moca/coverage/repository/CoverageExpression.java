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

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.coverage.BranchCoverage;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.expression.Expression;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class CoverageExpression implements Expression, Serializable {
    private static final long serialVersionUID = -5434431729308481380L;
    /**
     * 
     */
    public CoverageExpression(Expression expression, BranchCoverage branch) {
        _expression = expression;
        _branch = branch;
    }

    // @see com.redprairie.moca.server.expression.Expression#evaluate(com.redprairie.moca.server.exec.ServerContext)
    @Override
    public MocaValue evaluate(ServerContext ctx) throws MocaException {
        MocaValue value = _expression.evaluate(ctx);
        boolean wasTrue = value.asBoolean();
        _branch.branchHit(wasTrue);
        return value;
    }
    
    @Override
    public String toString(){
        return _expression.toString();
    }

    private final Expression _expression;
    private final BranchCoverage _branch;
}
