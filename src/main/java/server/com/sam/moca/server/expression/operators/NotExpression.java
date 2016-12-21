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

package com.sam.moca.server.expression.operators;

import com.sam.moca.MocaException;
import com.sam.moca.MocaType;
import com.sam.moca.MocaValue;
import com.sam.moca.server.exec.ServerContext;
import com.sam.moca.server.expression.Expression;

public class NotExpression implements Expression {
    public NotExpression(Expression target) {
        _target = target;
    }
    
    public MocaValue evaluate(ServerContext ctx) throws MocaException {
        MocaValue targetValue = _target.evaluate(ctx);
        
        boolean result = !targetValue.asBoolean();

        return new MocaValue(MocaType.BOOLEAN, Boolean.valueOf(result));
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return "!" + _target;
    }

    
    private final Expression _target;
}

