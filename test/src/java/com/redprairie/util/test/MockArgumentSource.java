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

package com.redprairie.util.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaOperator;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.exec.ArgumentSource;

/**
 * Mock object to be used when testing code that needs to check the ServerContext for variables.  This mock stub
 * only implements the basic variable, args, and system variable methods.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MockArgumentSource implements ArgumentSource {
    
    //
    // Stub Methods
    //
    public String getSystemVariable(String name) {
        return _vars.get(name.toLowerCase());
    }
    
    public MocaArgument getVariableAsArgument(String name, boolean markUsed, boolean equalsOnly) {
        _Arg arg = _argMap.get(name);
        
        if (arg == null) return null;
        
        if (markUsed) {
            arg.used = true;
        }
        
        if (equalsOnly && arg.arg.getOper() != MocaOperator.EQ) {
            return null;
        }
        
        return arg.arg;
    }

    public MocaArgument[] getCommandArgs(boolean markUsed, boolean useLowLevel) {
        List<MocaArgument> out = new ArrayList<MocaArgument>();
        for (_Arg arg : _args) {
            if (!arg.used) {
                out.add(arg.arg);
            }
        }
        return out.toArray(new MocaArgument[out.size()]);
    }

    public MocaValue getVariable(String name, boolean markUsed) {
        _Arg arg = _argMap.get(name);
        
        if (arg == null) {
            return null;
        }
        
        if (arg.arg.getOper() != MocaOperator.EQ) {
            return null;
        }
        else {
            if (markUsed) {
                arg.used = true;
            }
            
            return new MocaValue(arg.arg.getType(), arg.arg.getValue());
        }
    }
    
    public boolean isVariableAvailable(String name) {
        return _argMap.containsKey(name);
    }
    
    //
    // Mock Setup Methods
    //
    
    public void addArg(String name, MocaOperator oper, Object value, boolean addToList) {
        MocaArgument arg;
        if (value == null) {
            arg = new MocaArgument(name, oper, MocaType.STRING, null);
        }
        else {
            arg = new MocaArgument(name, oper, MocaType.lookupClass(value.getClass()), value);
        }
        
        _Arg tmp = new _Arg(arg);
        _argMap.put(name.toLowerCase(), tmp);
        if (addToList) _args.add(tmp);
    }
    
    public void addSystemVar(String name, String value) {
        _vars.put(name.toLowerCase(), value);
    }
    
    private static class _Arg {
        private _Arg(MocaArgument arg) {
            this.arg = arg;
            this.used = false;
        }
        private MocaArgument arg;
        private boolean used;
    }
    
    private final Map<String, String> _vars = new HashMap<String, String>();
    private final Map<String, _Arg> _argMap = new HashMap<String, _Arg>();
    private final List<_Arg> _args = new ArrayList<_Arg>();
}
