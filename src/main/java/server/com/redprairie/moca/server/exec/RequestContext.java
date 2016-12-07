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

package com.redprairie.moca.server.exec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Attributes of a single request.
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class RequestContext {
    
    public RequestContext() {
        this(null);
    }
    
    public RequestContext(Map<String, String> env) {
        Map<String, String> envMap;
        if (env == null) {
            envMap = new HashMap<String, String>();
        }
        else {
            envMap = new HashMap<String, String>(env);
        }
        _env = Collections.synchronizedMap(envMap);
        _attrs = Collections.synchronizedMap(new HashMap<String, Object>());
    }

    public String getVariable(String name) {
        return _env.get(name.toUpperCase());
    }
    
    public boolean isVariableMapped(String name) {
        return _env.containsKey(name.toUpperCase());
    }
    
    /**
     * Returns an unmodifiable view of the request environment variables.  If
     * the map is iterated upon, then it must be externally synchronized
     * @return
     */
    public Map<String, String> getAllVariables() {
        return Collections.unmodifiableMap(_env);
    }
    
    public String putVariable(String name, String value) {
        return _env.put(name.toUpperCase(), value);
    }
    
    public String removeVariable(String name) {
        return _env.remove(name.toUpperCase());
    }

    public Object getAttribute(String name) {
        return _attrs.get(name.toUpperCase());
    }
    
    public void putAttribute(String name, Object value) {
        _attrs.put(name.toUpperCase(), value);
    }
    
    public Object removeAttribute(String name) {
        return _attrs.remove(name.toUpperCase());
    }
    
    private final Map<String, String> _env;
    private final Map<String, Object> _attrs;
}
