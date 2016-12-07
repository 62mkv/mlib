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

package com.redprairie.moca.server.db.jdbc;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.CommonDataSource;

/**
 * Class that provides property based configuration methods.  The provider is
 * used to generate the properties for a given datbase connection request.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public interface PropertyProvider {
    /**
     * Called just before establishing a connection.  Allows the listener to modify
     * the connection properties prior to the connection being established.
     * @param prop the original properties object as configured.  This should be
     * treated as read-only, and should not be modified by this call.
     * @param name the name given to the connection by the pool.  This method can be
     * used (by some JDBC drivers) to pass the connection name to the database server
     * and be stored in the list of active sessions.
     * @return another properties object.  It is acceptable to return the same
     * object as originally passed.
     */
    public Properties modifyConnectionProperties(Properties prop, String name);
    
    
    /**
     * This is used to return a connection name given to the connections properties.
     * This is defined in the <code>PoolListener</code>, because each database has different 
     * limitations as to the allowed length of the connection name.
     * 
     * @param poolName
     * @param connectionId
     * @return a string used for a connection name
     */
    public String getConnectionName(String poolName, long connectionId);
    
    /**
     * @param source
     * @param props
     * @throws SQLException
     */
    public void initializeDataSourceProperties(CommonDataSource source, 
        Properties props) throws SQLException;
    
    /**
     * Returns the property editor for this pool listener.
     * @return
     */
    public DataSourcePropertyEditor getPropertyEditor();
}
