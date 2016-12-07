/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

import oracle.jdbc.pool.OracleDataSource;

/**
 * This is an oracle specific property setter for a data source.  In this case
 * oracle has a special setConnectionProperties method available on all of their
 * CommonDataSource classes to set the properties directly.  So we use that and
 * if it is not we fall back to the {@link DefaultDataSourcePropertyEditor} 
 * behavior.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class OracleDataSourcePropertyEditor extends DefaultDataSourcePropertyEditor {

    // @see com.redprairie.moca.server.db.jdbc.DataSourcePropertyEditor#applyProperties(javax.sql.CommonDataSource, java.util.Properties)
    @Override
    public void applyProperties(CommonDataSource dataSource, Properties props)
            throws SQLException {
        if (dataSource instanceof OracleDataSource) {
            ((OracleDataSource)dataSource).setConnectionProperties(props);
        }
        else {
            super.applyProperties(dataSource, props);
        }
    }

}
