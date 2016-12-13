/*
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

package com.redprairie.moca.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * General purpose JDBC utility methods.
 *
 * <b><pre>
 * Copyright (c) 2016 J. Lawrence Podmolik
 * Copyright (c) 20163 Redpoint Technologies LLC
 * All Rights Reserved.
 * </pre></b>
 *
 * @author  J. Lawrence Podmolik
 * @version $Revision$
 */

public final class JDBCUtils {

    /**
     * Closes the specified connection.  Any exceptions that occur
     * during this operation are suppressed.
     * 
     * @param conn the connection to close (if non-null).
     */
    public static void cleanup(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { }
        }
    }

    /**
     * Closes the specified statement.  Any exceptions that occur
     * during this operation are suppressed.
     * 
     * @param stmt the statement to close (if non-null).
     */
    public static void cleanup(Statement stmt) {
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException e) { }
        }
    }

    /**
     * Closed the specified connection and statement.  Any exceptions
     * that occur during this operation are suppressed.
     * 
     * @param conn the connection to close (if non-null).
     * @param stmt the statement to close (if non-null).
     */
    public static void cleanup(Connection conn, Statement stmt) {
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException e) { }
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { }
        }
    }
    
    //------------------------------
    // implementation:
    //------------------------------
    
    // prevent instantiation:
    private JDBCUtils() { }
}
