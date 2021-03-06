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

package com.sam.moca.client;

import java.util.Map;

import com.sam.moca.MocaException;
import com.sam.moca.MocaRegistry;
import com.sam.moca.server.ServerUtils;

/**
 * Unit tests for DirectConnection.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_DirectConnection extends TU_AbstractConnection {
    public static final int DEFAULT_PORT = Integer.parseInt(ServerUtils
        .globalContext().getConfigurationElement(
            MocaRegistry.REGKEY_SERVER_CLASSIC_PORT, "4550"));
    
    public void testInvalidConnection() throws MocaException {
        try {
            _getConnection("nosuchhost", null);
            fail("Expected connection failure");
        }
        catch (ConnectionFailedException e) {
            // Normal
        }
    }

    protected MocaConnection _getConnection(String host, Map<String, String> env) throws MocaException {
        return new DirectConnection(host, DEFAULT_PORT, env);
    }
    
}
