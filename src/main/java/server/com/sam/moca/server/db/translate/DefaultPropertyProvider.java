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

package com.sam.moca.server.db.translate;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.CommonDataSource;

import com.atomikos.beans.PropertyException;
import com.atomikos.beans.PropertyUtils;
import com.sam.moca.server.db.jdbc.DataSourcePropertyEditor;
import com.sam.moca.server.db.jdbc.DefaultDataSourcePropertyEditor;
import com.sam.moca.server.db.jdbc.DriverDataSource;
import com.sam.moca.server.db.jdbc.PropertyProvider;

/**
 * Default property provider that provides safe method invocations.  This
 * class should be extended to add database specific property overrides.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class DefaultPropertyProvider implements PropertyProvider {

    
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
    public Properties modifyConnectionProperties(Properties prop, String name) {
        return prop;
    }
    
    /**
     * This is used to return a connection name given to the connections properties.
     * This is defined in the <code>PoolListener</code>, because each database has different 
     * limitations as to the allowed length of the connection name.
     * 
     * @param poolName
     * @param connectionId
     * @return a string used for a connection name
     */
    public String getConnectionName(String poolName, long connectionId) {
        return String.format("%s-%06x", poolName, connectionId);
    }
    
    /**
     * @param source
     * @param props
     * @throws SQLException
     */
    public void initializeDataSourceProperties(CommonDataSource source,
        Properties props) throws SQLException {
        if (source instanceof DriverDataSource) {
            ((DriverDataSource)source).initializeDefaultProperties(props);
        }
        else {
            try {
                PropertyUtils.setProperties(source, props);
            }
            catch (PropertyException e) {
                throw new SQLException(e);
            }
        }
    }
    
    /**
     * @return
     */
    public DataSourcePropertyEditor getPropertyEditor() {
        return new DefaultDataSourcePropertyEditor();
    }
}
