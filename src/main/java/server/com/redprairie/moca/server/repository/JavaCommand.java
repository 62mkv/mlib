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

package com.redprairie.moca.server.repository;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.server.exec.ComponentAdapter;
import com.redprairie.moca.server.exec.ServerContext;

/**
 * JavaCommand - SubClass to the Abstract Command Classs. This overides the
 * default behavior to give functionality to methods that are specific to java
 * commands.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author cjolly
 * @version $Revision$
 */

public class JavaCommand extends Command {
    private static final long serialVersionUID = 323506413191432126L;
    /**
     * 
     */
    public JavaCommand(String name, ComponentLevel level) {
        super(name, level);
        _className = null;
        _method = null;
    }

    // @see com.redprairie.moca.server.repository.Command#getClassname()
    public String getClassName() {
        return _className;
    }

    // @see
    // com.redprairie.moca.server.repository.Command#setClassname(java.lang.String)
    public void setClassName(String className) {
        _className = className;
    }

    // @see com.redprairie.moca.server.repository.Command#getMethod()
    public String getMethod() {
        return _method;
    }

    // @see
    // com.redprairie.moca.server.repository.Command#setMethod(java.lang.String)
    public void setMethod(String method) {
        _method = method;
    }

    @Override
    public CommandType getType() {
        return CommandType.JAVA_METHOD;
    }

    // @see
    // com.redprairie.moca.server.repository.Command#execute(com.redprairie.moca.server.exec.ServerContext)
    @Override
    protected MocaResults executeWithContext(ServerContext ctx) throws MocaException {
        
        ComponentAdapter adapter;
        synchronized(this) {
            if (_adapter == null) {
                // Make corresponding arrays, as needed by the component adapter
                MocaType[] argTypes = getArgTypes();
                
                _adapter = ComponentAdapter.lookupMethod(getLevel().getPackage(), _className, _method, argTypes);
            }
            adapter = _adapter;
        }

        Object[] args = getArgs(ctx);
        
        return adapter.executeMethod(args, ctx.getComponentContext());
    }
    
    //
    // Implementation
    //
    private String _className;
    private String _method;
    private transient ComponentAdapter _adapter;
}
