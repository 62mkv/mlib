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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Factory methods for various {@link ConnectionValidator}.
 * 
 * Copyright (c) 2014 RedPrairie Corporation
 * All Rights Reserved
 */
public class ConnectionValidators {
    
    private ConnectionValidators() {}
    
    /**
     * Generates a {@link ConnectionValidator} that validates
     * the connection by executing the given testQuery. The test
     * query should be performant.
     * @param testQuery The query to test the connection with
     * @return The validator
     */
    public static ConnectionValidator newQueryTestValidator(String testQuery) {
        return new QueryTestConnectionValidator(testQuery);
    }
    
    /**
     * Connection validator using a test query. The test query should be performant.
     * This is an alternative to JDBC 4 {@link Connection#isValid(int)) which isn't
     * supported by all JDBC drivers e.g. JTDS.
     */
    private static class QueryTestConnectionValidator implements ConnectionValidator {
        
        public QueryTestConnectionValidator(String testQuery) {
            _query = testQuery;
        }
        
        @Override
        public void validate(Connection conn) throws SQLException {
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery(_query)) {
            }
        }

        private final String _query;
    }
}
