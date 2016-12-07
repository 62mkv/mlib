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

package com.redprairie.moca.advice;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import com.google.common.collect.ForwardingObject;
import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.BindMode;
import com.redprairie.moca.server.db.DBAdapter;
import com.redprairie.moca.server.db.DBType;
import com.redprairie.moca.server.db.MocaTransaction;
import com.redprairie.moca.server.db.jdbc.ConnectionPoolStatistics;
import com.redprairie.moca.server.db.jdbc.ConnectionStatistics;
import com.redprairie.moca.server.exec.ArgumentSource;
import com.redprairie.moca.server.profile.CommandPath;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class ForwardingDBAdapter extends ForwardingObject implements DBAdapter {
    
    public ForwardingDBAdapter(DBAdapter adapter) {
        _adapter = adapter;
    }

    // @see com.redprairie.moca.server.db.DBAdapter#close()
    @Override
    public void close() throws SQLException {
        delegate().close();
    }

    // @see com.redprairie.moca.server.db.DBAdapter#executeSQL(com.redprairie.moca.server.db.MocaTransaction, java.lang.String, com.redprairie.moca.server.db.BindList, com.redprairie.moca.server.db.BindMode, boolean, com.redprairie.moca.server.profile.CommandPath)
    @Override
    public EditableResults executeSQL(ArgumentSource argumentSource, 
        MocaTransaction tx, String sqlStatement, BindList bindList, 
        BindMode mode, boolean ignoreResults, CommandPath commandPath) 
                throws SQLException, MocaException {
        return delegate().executeSQL(argumentSource, tx, sqlStatement, bindList,
            mode, ignoreResults, commandPath);
    }

    // @see com.redprairie.moca.server.db.DBAdapter#getConnection(com.redprairie.moca.server.db.MocaTransaction, com.redprairie.moca.server.profile.CommandPath)
    @Override
    public Connection getConnection(MocaTransaction tx, CommandPath commandPath)
            throws SQLException {
        return delegate().getConnection(tx, commandPath);
    }

    // @see com.redprairie.moca.server.db.DBAdapter#getDBType()
    @Override
    public DBType getDBType() {
        return delegate().getDBType();
    }

    // @see com.redprairie.moca.server.db.DBAdapter#getNextSequenceValue(com.redprairie.moca.server.db.MocaTransaction, java.lang.String, com.redprairie.moca.server.profile.CommandPath)
    @Override
    public String getNextSequenceValue(MocaTransaction tx, String sequence,
        CommandPath commandPath) throws SQLException {
        return delegate().getNextSequenceValue(tx, sequence, commandPath);
    }

    // @see com.redprairie.moca.server.db.DBAdapter#getConnectionStatistics()
    @Override
    public Map<Connection, ConnectionStatistics> getConnectionStatistics() {
        return delegate().getConnectionStatistics();
    }

    // @see com.redprairie.moca.server.db.DBAdapter#getConnectionPoolStatistics()
    @Override
    public ConnectionPoolStatistics getConnectionPoolStatistics() {
        return delegate().getConnectionPoolStatistics();
    }

    // @see com.redprairie.moca.server.db.DBAdapter#newTransaction()
    @Override
    public MocaTransaction newTransaction() {
        return delegate().newTransaction();
    }

    // @see com.redprairie.moca.server.db.DBAdapter#logPerformance()
    @Override
    public void logPerformance() {
        delegate().logPerformance();
    }

    // @see com.redprairie.moca.server.db.DBAdapter#getDataSource()
    @Override
    public DataSource getDataSource() {
        return delegate().getDataSource();
    }

    // @see com.google.common.collect.ForwardingObject#delegate()
    @Override
    protected DBAdapter delegate() {
        return _adapter;
    }
    
    private final DBAdapter _adapter;
}
