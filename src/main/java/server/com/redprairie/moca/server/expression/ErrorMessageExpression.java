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

package com.redprairie.moca.server.expression;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.dispatch.ErrorMessageBuilder;
import com.redprairie.moca.server.exec.ServerContext;

public class ErrorMessageExpression implements Expression {
    public ErrorMessageExpression() {
    }
    
    public MocaValue evaluate(ServerContext ctx) throws MocaException {
        MocaException lastError = ctx.getLastError();
        if (lastError == null) {
            return new MocaValue(MocaType.STRING, null);
        }
        else {
            String message;
            
            if (lastError.isMessageResolved()) {
                message = lastError.getMessage();
            }
            else {
                message = new ErrorMessageBuilder(
                    lastError.getErrorCode(), lastError.getMessage(), 
                    lastError.getArgList(), ctx.getMessageResolver()).getMessage();
            }
            
            return new MocaValue(MocaType.STRING, message);
        }
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return "@!";
    }
}
