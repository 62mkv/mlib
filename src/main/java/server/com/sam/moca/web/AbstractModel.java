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

package com.sam.moca.web;

import java.util.HashMap;
import java.util.Map;

/**
 * Base model class to provide core functionality to extended model classes.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author Matt Horner
 * @version $Revision$
 */

public abstract class AbstractModel {
    /**
     * Register a given model with the appropriate method name.
     * 
     * @param modelName The model name as referenced by invoking requests.
     * @param methodName The method name to handle the request for the model
     *            name.
     */
    protected void _registerHandler(String modelName, String methodName) {
        _handlers.put(modelName, methodName);
    }

    /**
     * Produce the list of handlers provided by this class. This will be used in
     * direct
     * 
     * @return
     */
    public Map<String, String> publishHandlers() {
        return _handlers;
    }

    private Map<String, String> _handlers = new HashMap<String, String>();
}
