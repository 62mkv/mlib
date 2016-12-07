/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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

import java.util.List;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.exec.ServerContext;

/**
 * Returns a new string that is a substring of the string argument.
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public class SubstrFunction extends BaseFunction {

    @Override
    protected MocaValue invoke(ServerContext ctx, List<MocaValue> args) throws MocaException {
        if (args.size() < 2 || args.size() > 3) {
            throw new FunctionArgumentException("expected 2 or 3 arguments");
        }
        
        String format = args.get(0).asString();
        int start = args.get(1).asInt();
        
        
        if (format == null) {
            return new MocaValue(MocaType.STRING, null);
        }
            
        // Convert to 0-based index with the following rules:
        // - 0 (1-based) is the same as 1 (1-based)
        // - Negative numbers count from the end of the string
        // - Numbers that are too large (pos or neg) are just set to end
        if (Math.abs(start) > format.length()) {
            start = format.length();
        }
        else if (start > 0) {
            start--;
        }
        else if (start < 0) {
            start = format.length() + start;
        }
        
        if (args.size() > 2) {
            int length = args.get(2).asInt();
            
            if (length <= 0) {
                return new MocaValue(MocaType.STRING, "");
            }
            
            if ((length + start) > format.length()) {
                length = format.length() - start;
            }
            
            return new MocaValue(MocaType.STRING, format.substring(start, length + start));
        }
        else {
            return new MocaValue(MocaType.STRING, format.substring(start));
        }
    }
}
