/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2007
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

package com.redprairie.moca.server.expression;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.exec.ServerContext;

public class LiteralExpression implements Expression {
    public LiteralExpression(Object value) {
        this(MocaType.lookupClass(value.getClass()), value);
    }
    
    public LiteralExpression(MocaType type, Object value) {
        _type = type;
        _value = value;
    }
    
    public MocaValue evaluate(ServerContext ctx) throws MocaException {
        return new MocaValue(_type, _value);
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return "(TYPE: " + _type +", VALUE: " + _value + ")";
    }

    
    private final MocaType _type;
    private final Object _value;
}
