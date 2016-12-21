/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.sam.moca.server.db.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This is a {@link Driver} like implementation that basically delegates the
 * connection retrieval to {@link ConnectionManager} to get the connections.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaDriver implements Driver {
    
    static {
        try {
            DriverManager.registerDriver(new MocaDriver());
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static final String MOCA_DRIVER = "jdbc:moca:";

    // @see java.sql.Driver#connect(java.lang.String, java.util.Properties)
    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!url.startsWith(MOCA_DRIVER)) {
            // If the url isn't supported by us just return null
            return null;
        }
        return ConnectionManager.getConnection(
            url.substring(MOCA_DRIVER.length()), info);
    }

    // @see java.sql.Driver#acceptsURL(java.lang.String)
    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url != null && url.startsWith(MOCA_DRIVER);
    }

    // @see java.sql.Driver#getPropertyInfo(java.lang.String, java.util.Properties)
    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
            throws SQLException {
        throw new UnsupportedOperationException("getPropertyInfo is not implemented!");
    }

    // @see java.sql.Driver#getMajorVersion()
    @Override
    public int getMajorVersion() {
        return 1;
    }

    // @see java.sql.Driver#getMinorVersion()
    @Override
    public int getMinorVersion() {
        return 0;
    }

    // @see java.sql.Driver#jdbcCompliant()
    @Override
    public boolean jdbcCompliant() {
        return true;
    }
    
    // @see java.sql.Driver#getParentLogger()
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
}
