/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

package com.redprairie.moca.client;

import java.util.Map;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.SystemContext;

/**
 * Unit tests for HttpConnection using the XML encoding.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_HTTPConnectionWithXML extends TU_HttpConnection {
    
    // @see com.redprairie.moca.client.TU_AbstractConnection#_getConnection(java.util.Map)
    @Override
    protected MocaConnection _getConnection(String host, Map<String, String> env) throws MocaException {
        SystemContext ctx = ServerUtils.globalContext();
        String url = ctx.getConfigurationElement(MocaRegistry.REGKEY_SERVER_URL);
        return new HttpConnection(url, env, "XML");
    }
}
