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

package com.sam.moca.server.expression;

import com.sam.moca.MocaException;
import com.sam.moca.MocaType;
import com.sam.moca.MocaValue;
import com.sam.moca.server.exec.CompiledScript;
import com.sam.moca.server.exec.ServerContext;

public class ScriptExpression implements Expression {
    public ScriptExpression(String script) {
        _script = script;
    }
    
    public MocaValue evaluate(ServerContext ctx) throws MocaException {
        CompiledScript compiled;
        synchronized(this) {
            if (_compiled == null) {
                _compiled = ctx.compileScript(_script, null);
            }
            compiled = _compiled;
        }
        
        Object scriptResult = ctx.evaluateScript(compiled);
        return new MocaValue(MocaType.forValue(scriptResult), scriptResult);
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("[[");
        out.append(_script);
        out.append("]]");
        
        return out.toString();
    }

    private final String _script;
    private transient CompiledScript _compiled;
}
