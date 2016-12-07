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

package com.redprairie.moca.server.legacy;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.util.ResultUtils;

/**
 * Any error encountered while invoking a legacy (C) component.
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class CommandInvocationException extends MocaException {
    private static final long serialVersionUID = -8373066961384775545L;

    public CommandInvocationException(int code, String message, boolean messageResolved, MocaResults res, Args[] args) {
        super(code, message);
        if (res != null && res instanceof WrappedResults) {
            EditableResults copy = new SimpleResults();
            ResultUtils.copyResults(copy, res);
            res.close();
            setResults(copy);
        }
        else {
            setResults(res);
        }
        
        if (args != null) {
            for (Args arg : args) {
                if (arg.isLookup()) {
                    addLookupArg(arg.getName(), (String)arg.getValue());
                }
                else {
                    addArg(arg.getName(), arg.getValue());
                }
            }
        }
        _messageResolved = messageResolved;
    }
    
    public CommandInvocationException(int code, String message, boolean messageResolved, MocaResults res) {
        this(code, message, messageResolved, res, null);
    }
    
    @Override
    public String toString() {
        return super.toString() + " (ERR=" + getErrorCode() +")";
    }

    // @see com.redprairie.moca.MocaException#addArg(java.lang.String, java.lang.Object)
    @Override
    public void addArg(String name, Object value) {
        super.addArg(name, value);
    }

    // @see com.redprairie.moca.MocaException#addLookupArg(java.lang.String, java.lang.String)
    @Override
    public void addLookupArg(String name, String value) {
        super.addLookupArg(name, value);
    }
    
    // @see com.redprairie.moca.MocaException#isMessageResolved()
    
    @Override
    public boolean isMessageResolved() {
        return _messageResolved;
    }
    
    private final boolean _messageResolved;
}
