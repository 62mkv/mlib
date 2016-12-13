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

package com.redprairie.moca.server.expression.function;

import java.util.List;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.exec.ServerContext;

/**
 * Trims the argument string.  All trailing whitespace is removed.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public class TrimFunction extends BaseFunction {

    @Override
    protected MocaValue invoke(ServerContext ctx, List<MocaValue> args) throws MocaException {
        if (args.size() != 1) {
            throw new FunctionArgumentException("expected single argument");
        }

        MocaValue argValue = args.get(0);
        String strValue = argValue.asString();

        if (strValue == null) {
            return new MocaValue(MocaType.STRING, null);
        }
        else {
            return new MocaValue(MocaType.STRING, rtrim(strValue));
        }
    }
    
    private static String rtrim(String in) {
        for (int i = in.length(); i > 0; i--) {
            if (in.charAt(i - 1) > ' ') {
                return in.substring(0, i);
            }
        }
        return "";
    }
}
