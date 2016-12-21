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

package com.sam.moca.server.expression.function;

import java.util.List;

import com.sam.moca.MocaException;
import com.sam.moca.MocaType;
import com.sam.moca.MocaValue;
import com.sam.moca.server.exec.ServerContext;

/**
 * Returns the largest of a list of values.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public class MaxFunction extends BaseFunction {

    @Override
    protected MocaValue invoke(ServerContext ctx, List<MocaValue> args) throws MocaException {
        if (args.size() == 0) {
            throw new FunctionArgumentException("expected arguments");
        }
        
        MocaValue maxValue = null;
        
        for (MocaValue a : args) {
            if (maxValue == null || maxValue.asDouble() < a.asDouble()) {
                maxValue = a;
            }
        }
        
        if (maxValue == null) {
            maxValue = new MocaValue(MocaType.INTEGER, null);
        }
        
        return maxValue;
    }
}
