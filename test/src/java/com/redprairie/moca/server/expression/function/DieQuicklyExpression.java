/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.server.expression.function;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.expression.Expression;

public class DieQuicklyExpression implements Expression {

    @SuppressWarnings("serial")
    public static class QuickDeathException extends MocaException {
        public QuickDeathException(String tag) {
            super(90210, "QUICK DEATH: " + tag);
        }
    }
    
    public DieQuicklyExpression(String tag) {
        _tag = tag;
    }

    @Override
    public MocaValue evaluate(ServerContext ctx) throws MocaException {
        throw new QuickDeathException(_tag);
    }
    
    private final String _tag;
}