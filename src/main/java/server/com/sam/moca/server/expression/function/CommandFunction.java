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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.MocaValue;
import com.sam.moca.server.exec.ServerContext;

/**
 * Executes a command indicated by the name of the function. A single underscore
 * will remain unchanged in the command name, while a double-underscore will
 * turn into a space.  Therefore the function (var_publish__data) will turn into
 * the command "var_publish data", and the arguments to that command will come in
 * as moca_farg0, moca_farg1, ..., moca_fargn.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public class CommandFunction extends BaseFunction {
    public CommandFunction(String name) {
        _commandBase = name.replace("__", " ");
    }
    
    @Override
    protected MocaValue invoke(ServerContext ctx, List<MocaValue> args) throws MocaException {
        Map<String, Object> argMap = new LinkedHashMap<String, Object>();
        int i = 1;
        for (MocaValue argValue : args) {
            argMap.put("moca_farg" + i, argValue.getValue());
            i++;
        }
        
        MocaResults res = ctx.executeCommand(_commandBase, argMap, false);

        if (res != null && res.next()) {
            return new MocaValue(res.getColumnType(0), res.getValue(0));
        }
        else {
            return new MocaValue(MocaType.STRING, null);
        }
    }
    
    private final String _commandBase;
}
