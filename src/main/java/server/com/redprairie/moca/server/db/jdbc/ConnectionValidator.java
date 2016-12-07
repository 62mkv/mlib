/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2014
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

package com.redprairie.moca.server.db.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * An interface used for validating that a {@link Connection}
 * is still usable (valid). ConnectionValidator implementations
 * should be threadsafe.
 * 
 * Copyright (c) 2014 RedPrairie Corporation
 * All Rights Reserved
 */
public interface ConnectionValidator {
    
    /**
     * Validates the connection is still usable. If it is
     * not a {@link SQLException} should be thrown.
     * @param conn The connection
     * @throws SQLException Thrown if the connection is no longer valid
     */
    void validate(Connection conn) throws SQLException;

}
