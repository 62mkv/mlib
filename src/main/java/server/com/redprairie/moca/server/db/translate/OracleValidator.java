/*
 *  $URL$
 *  $Author$
 *  $Date$
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

package com.redprairie.moca.server.db.translate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import oracle.jdbc.driver.OracleConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.pool.PoolException;
import com.redprairie.moca.server.db.jdbc.ConnectionValidator;
import com.redprairie.moca.server.db.jdbc.DefaultDbValidator;
import com.redprairie.moca.server.db.jdbc.SQLPoolException;

/**
 * Validator to be used with Oracle database
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class OracleValidator extends DefaultDbValidator {
    /**
     */
    public OracleValidator(ConnectionValidator connectionValidator) {
        super(connectionValidator);
        
        String cacheVar = System.getenv("DB_CACHE_SIZE");
        if (cacheVar != null) {
            _cacheSize = Integer.parseInt(cacheVar);
        }
    }
    
    // @see com.redprairie.moca.server.db.jdbc.DefaultDbValidator#initialize(java.sql.Connection)
    @Override
    public void initialize(Connection conn) throws PoolException {
        super.initialize(conn);
        
        try {
            // There's some things we do only if we're using the Oracle JDBC driver.
            if (conn instanceof OracleConnection) {
                OracleConnection oConn = (OracleConnection) conn;
                
                // Enable implicit caching.
                if (_cacheSize == 0) {
                    oConn.setImplicitCachingEnabled(false);
                }
                else {
                    oConn.setImplicitCachingEnabled(true);
                    oConn.setStatementCacheSize(_cacheSize);
                }
                
                // Set the session time zone.
                Statement s = conn.createStatement();
                ResultSet rs = null;
                
                try {
                    rs = s.executeQuery("select dbtimezone from dual");
                    rs.next( );
                    String tz = rs.getString(1);
                    oConn.setSessionTimeZone(tz);
                }
                finally {
                    if (rs != null) {
                        try {
                            rs.close();
                        }
                        catch (SQLException e) {
                            _logger.debug("Exception (ignored) closing result set: "
                                    + e);
                        }
                    }
                    if (s != null) {
                        try {
                            s.close();
                        }
                        catch (SQLException e) {
                            _logger.debug("Exception (ignored) closing result set: "
                                    + e);
                        }
                    }
                }
            }
            
            // Set the data format.
            Statement s = conn.createStatement();
            try {
                s.execute("alter session set nls_date_format = 'YYYYMMDDHH24MISS'");
            }
            finally {
                s.close();
            }
        }
        catch (SQLException e) {
            throw new SQLPoolException(e);
        }
    }
    
    private final Logger _logger = LogManager.getLogger(OracleValidator.class);
    private int _cacheSize = 64;
}
