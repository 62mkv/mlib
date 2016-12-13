/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.server.db.translate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.redprairie.moca.pool.PoolException;
import com.redprairie.moca.server.db.jdbc.ConnectionValidator;
import com.redprairie.moca.server.db.jdbc.DefaultDbValidator;
import com.redprairie.moca.server.db.jdbc.SQLPoolException;

/**
 * Validator to be used with SQL Server database
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class SQLServerValidator extends DefaultDbValidator {
    /**
     * @param connectionValidator The {@link ConnectionValidator} used to validate connections
     * @param isolationLevel
     */
    public SQLServerValidator(ConnectionValidator connectionValidator, int isolationLevel) {
        super(connectionValidator, isolationLevel);
    }
    
    // @see com.redprairie.moca.server.db.jdbc.DefaultDbValidator#initialize(java.sql.Connection)
    public void initialize(Connection conn) throws PoolException {
        super.initialize(conn);
   
        Statement s;
        
        try {
            // Make sure we can concatenate nulls with non-nulls and get a non-null.
            s = conn.createStatement();
            try {
                s.execute("SET CONCAT_NULL_YIELDS_NULL OFF");
            }
            finally {
                s.close();
            }
            
            // Make sure we're running in the read committed snapshot isolation level.
            s = conn.createStatement();
            try {
                ResultSet res = s.executeQuery("select is_read_committed_snapshot_on from sys.databases where name=db_name()");
                res.next();
                
                boolean isReadCommittedSnapshot = res.getBoolean(1);
                if (isReadCommittedSnapshot == false) {
                    throw new SQLException("The database isolation level must be READ_COMMITTED_SNAPSHOT");
                }
            }
            finally {
                s.close();
            }
        }
        catch (SQLException e) {
            throw new SQLPoolException(e);
        }
    }
}
