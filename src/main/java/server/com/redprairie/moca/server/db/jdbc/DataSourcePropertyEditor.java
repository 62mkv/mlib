package com.redprairie.moca.server.db.jdbc;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.CommonDataSource;

/**
 * This is a callback that can be used to tell how a specific data source
 * should be configured given a set of properties.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public interface DataSourcePropertyEditor {
    public void applyProperties(CommonDataSource dataSource, Properties props) throws SQLException;
}