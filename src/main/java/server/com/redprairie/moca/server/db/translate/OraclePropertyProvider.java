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

package com.redprairie.moca.server.db.translate;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.CommonDataSource;

import oracle.jdbc.pool.OracleDataSource;

import com.redprairie.moca.server.db.jdbc.DataSourcePropertyEditor;
import com.redprairie.moca.server.db.jdbc.OracleDataSourcePropertyEditor;

/**
 * Oracle based property provider implementation.  This implementation makes
 * sure to set the program name in the v$session for the connection.  This name
 * is also limited to 48 chars as that is the oracle limit.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class OraclePropertyProvider extends DefaultPropertyProvider {
    @Override
    public Properties modifyConnectionProperties(Properties baseProperties, String name) {
        if (name != null) {
            Properties copy = new Properties();
            copy.putAll(baseProperties);
            copy.setProperty("v$session.program", name);
            return copy;
        }
        else {
            return baseProperties;
        }
    }
    
    @Override
    public String getConnectionName(String poolName, long connectionId){
        // Oracle only allows for 48 chars for the connection name.
        // 41 chars for the pool name, 1 dash, and 6 chars for the connectionId
        return String.format("%.41s-%06x", poolName, connectionId);
    }
    
    // @see com.redprairie.moca.server.db.nodb.PoolListener#initializeDataSourceProperties(javax.sql.CommonDataSource, java.util.Properties)
    @Override
    public void initializeDataSourceProperties(CommonDataSource source,
        Properties props) throws SQLException {
        if (source instanceof OracleDataSource) {
            ((OracleDataSource)source).setConnectionProperties(props);
        }
        else {
            super.initializeDataSourceProperties(source, props);
        }
    }
    
    // @see com.redprairie.moca.server.db.jdbc.DefaultPoolListener#getPropertyEditor()
    @Override
    public DataSourcePropertyEditor getPropertyEditor() {
        return new OracleDataSourcePropertyEditor();
    }
}
