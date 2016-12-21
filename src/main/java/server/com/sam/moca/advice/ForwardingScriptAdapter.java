/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.sam.moca.advice;

import com.google.common.collect.ForwardingObject;
import com.sam.moca.MocaException;
import com.sam.moca.server.exec.CompiledScript;
import com.sam.moca.server.exec.ScriptAdapter;
import com.sam.moca.server.repository.Command;

/**
 * TODO
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class ForwardingScriptAdapter extends ForwardingObject implements ScriptAdapter {
    
    public ForwardingScriptAdapter(ScriptAdapter adapter) {
        _adapter = adapter;
    }

    @Override
    public CompiledScript compile(String script, Command runningCommand) throws MocaException {
        return delegate().compile(script, runningCommand);
    }
    
    
    // @see com.google.common.collect.ForwardingObject#delegate()
    @Override
    protected ScriptAdapter delegate() {
        return _adapter;
    }

    @Override
    public boolean equals(Object object) {
        return object == this || delegate().equals(object);
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }
    
    private final ScriptAdapter _adapter;
}
