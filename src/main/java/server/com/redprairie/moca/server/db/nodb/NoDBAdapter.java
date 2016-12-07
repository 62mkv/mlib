/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.server.db.nodb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.server.TransactionManagerUtils;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.BindMode;
import com.redprairie.moca.server.db.DBAdapter;
import com.redprairie.moca.server.db.DBType;
import com.redprairie.moca.server.db.MocaTransaction;
import com.redprairie.moca.server.db.jdbc.ConnectionPoolStatistics;
import com.redprairie.moca.server.db.jdbc.ConnectionStatistics;
import com.redprairie.moca.server.exec.ArgumentSource;
import com.redprairie.moca.server.exec.UnimplementedOperationException;
import com.redprairie.moca.server.profile.CommandPath;

/**
 * This class implements the DBAdapter, but throws NotImplementedExceptions for
 * most operations except basic ones like close.  This implementation also
 * supports the use of a transaction manager so that transaction aware resources
 * can still be handled transactionally.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class NoDBAdapter implements DBAdapter {
    
    private static final ConnectionPoolStatistics NO_DB_POOL_STATS = new ConnectionPoolStatistics(0, 0, 0); 

    // @see com.redprairie.moca.server.db.DBAdapter#close()
    @Override
    public void close() throws SQLException {
        // Do nothing
    }

    // @see com.redprairie.moca.server.db.DBAdapter#executeSQL(com.redprairie.moca.server.exec.ArgumentSource, com.redprairie.moca.server.db.MocaTransaction, java.lang.String, com.redprairie.moca.server.db.BindList, com.redprairie.moca.server.db.BindMode, boolean, com.redprairie.moca.server.profile.CommandPath)
    @Override
    public EditableResults executeSQL(ArgumentSource argumentSource,
        MocaTransaction tx, String sqlStatement, BindList bindList,
        BindMode bindMode, boolean ignoreResults, CommandPath commandPath)
            throws SQLException, MocaException {
        throw new UnimplementedOperationException("No database was supplied");
    }

    // @see com.redprairie.moca.server.db.DBAdapter#getConnection(com.redprairie.moca.server.db.MocaTransaction)
    @Override
    public Connection getConnection(MocaTransaction tx, CommandPath commandPath) throws SQLException {
        throw new UnimplementedOperationException("No database was supplied");
    }

    // @see com.redprairie.moca.server.db.DBAdapter#getDBType()
    @Override
    public DBType getDBType() {
        return DBType.NONE;
    }

    // @see com.redprairie.moca.server.db.DBAdapter#getConnectionStatistics()
    public Map<Connection, ConnectionStatistics> getConnectionStatistics() {
        throw new UnimplementedOperationException("No database was supplied");
    }

    // @see com.redprairie.moca.server.db.DBAdapter#getConnectionPoolStatistics()
    public ConnectionPoolStatistics getConnectionPoolStatistics() {
        // The pool is non existent so return empty stats
        return NoDBAdapter.NO_DB_POOL_STATS;
    }
    
    // @see com.redprairie.moca.server.db.DBAdapter#getNextSequenceValue(com.redprairie.moca.server.db.MocaTransaction, java.lang.String)
    @Override
    public String getNextSequenceValue(MocaTransaction tx, String sequence, 
            CommandPath commandPath)
            throws SQLException {
        throw new UnimplementedOperationException("No database was supplied");
    }

    // @see com.redprairie.moca.server.db.DBAdapter#logPerformance()
    @Override
    public void logPerformance() {
        throw new UnimplementedOperationException("No database was supplied");
    }

    // @see com.redprairie.moca.server.db.DBAdapter#newTransaction()
    @Override
    public MocaTransaction newTransaction() {
        try {
            TransactionManager manager = TransactionManagerUtils.getManager();
            return new NoDBTransaction(manager);
        }
        catch (SystemException e) {
            // This shouldn't ever happen
            throw new RuntimeException(e);
        }
    }
    
    // @see com.redprairie.moca.server.db.DBAdapter#getDataSource()
    @Override
    public DataSource getDataSource() {
        throw new UnimplementedOperationException("No database was supplied");
    }
}
