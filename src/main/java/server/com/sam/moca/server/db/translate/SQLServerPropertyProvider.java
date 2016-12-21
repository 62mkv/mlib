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

import java.util.Properties;

/**
 * SQL Server Property provider.  This implementation adds support for 
 * application name being set on the connection and limiting it's name
 * to 128 characters since that is the SQL Server limit.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class SQLServerPropertyProvider extends DefaultPropertyProvider {
    @Override
    public Properties modifyConnectionProperties(Properties baseProperties, String name) {
        if (name != null) {
            Properties copy = new Properties();
            copy.putAll(baseProperties);
            // MSQL property
            copy.setProperty("applicationName", name);
            // jTDS property
            copy.setProperty("appName", name);
            return copy;
        }
        else {
            return baseProperties;
        }
    }
    
    @Override
    public String getConnectionName(String poolName, long connectionId){
        // SQLServer allows 128 character applicationName.
        return String.format("%.121s-%06x", poolName, connectionId);
    }
}
