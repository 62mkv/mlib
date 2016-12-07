/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.server.db;

import java.sql.Connection;

/**
 * Simple immutable object to store a connection with a given name
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class NamedConnection {
    /**
     * @param conn
     * @param name
     */
    public NamedConnection(Connection conn, String name) {
        _conn = conn;
        _name = name;
    }
    
    /**
     * @return Returns the connection.
     */
    public Connection getConnection() {
        return _conn;
    }
    /**
     * @return Returns the name.
     */
    public String getName() {
        return _name;
    }
    
    private final Connection _conn;
    private final String _name;
}
