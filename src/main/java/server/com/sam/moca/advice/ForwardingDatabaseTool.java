/*
 *  $URL$
 *  $Author$
 *  $Date$
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

package com.sam.moca.advice;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.google.common.collect.ForwardingObject;
import com.sam.moca.DatabaseTool;
import com.sam.moca.MocaArgument;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class ForwardingDatabaseTool extends ForwardingObject implements DatabaseTool {
    
    public ForwardingDatabaseTool(DatabaseTool databaseTool) {
        _databaseTool = databaseTool;
    }

    // @see com.sam.moca.DatabaseTool#getConnection()
    @Override
    public Connection getConnection() {
        return delegate().getConnection();
    }

    // @see com.sam.moca.DatabaseTool#rollbackDB(java.lang.String)
    @Override
    public void rollbackDB(String savepoint) throws SQLException {
        delegate().rollbackDB(savepoint);
    }

    // @see com.sam.moca.DatabaseTool#getDbType()
    @Override
    public String getDbType() {
        return delegate().getDbType();
    }

    // @see com.sam.moca.DatabaseTool#getNextSequenceValue(java.lang.String)
    @Override
    public String getNextSequenceValue(String name) throws MocaException {
        return delegate().getNextSequenceValue(name);
    }

    // @see com.sam.moca.DatabaseTool#setSavepoint(java.lang.String)
    @Override
    public void setSavepoint(String savepoint) throws SQLException {
        delegate().setSavepoint(savepoint);
    }

    // @see com.sam.moca.DatabaseTool#executeSQL(java.lang.String, java.util.Map)
    @Override
    public MocaResults executeSQL(String sql, Map<String, ?> args)
            throws MocaException {
        return delegate().executeSQL(sql, args);
    }

    // @see com.sam.moca.DatabaseTool#executeSQL(java.lang.String, com.sam.moca.MocaArgument[])
    @Override
    public MocaResults executeSQL(String sql, MocaArgument... args)
            throws MocaException {
        return delegate().executeSQL(sql, args);
    }
    
    // @see com.google.common.collect.ForwardingObject#delegate()
    @Override
    protected DatabaseTool delegate() {
        return _databaseTool;
    }

    private final DatabaseTool _databaseTool;
}
