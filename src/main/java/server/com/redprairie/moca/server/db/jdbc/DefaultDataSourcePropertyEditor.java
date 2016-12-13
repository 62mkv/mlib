/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.redprairie.moca.server.db.jdbc;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.CommonDataSource;

import com.atomikos.beans.PropertyException;
import com.atomikos.beans.PropertyUtils;

/**
 * This is a default property editor that will set the properties in a fashion
 * of if the source is our custom {@link DriverDataSource} it will just
 * call setProperties or it will use reflection to actually call the setters
 * for the given property values.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class DefaultDataSourcePropertyEditor implements
        DataSourcePropertyEditor {

    // @see com.redprairie.moca.server.db.jdbc.ConnectionManager.DataSourcePropertyEditor#applyProperties(javax.sql.CommonDataSource, java.util.Properties)
    @Override
    public void applyProperties(CommonDataSource dataSource, Properties props) throws SQLException {
        if (props != null) {
            if (dataSource instanceof DriverDataSource) {
                ((DriverDataSource)dataSource).setProperties(props);
            }
            else {
                try {
                    // TODO: this doesn't support property setting where we can rollback the properties applied
                    PropertyUtils.setProperties(dataSource, props);
                }
                catch (PropertyException e) {
                    throw new SQLException("Property(s) not supported", e);
                }
            }
        }
    }

}
