/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.server.exec;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.redprairie.moca.DatabaseTool;
import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;

/**
 * This class is to be used in tandem with a DefaultServerContext.  This does
 * not make sense not in the context of one.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaDatabaseTool implements DatabaseTool {

    /**
     * @param adapter
     */
    public MocaDatabaseTool(TransactionManager tranManager) {
        super();
        _transactionManager = tranManager;
    }

    // @see com.redprairie.moca.DatabaseTool#getConnection()
    @Override
    public Connection getConnection() {
        return _transactionManager.getConnection();
    }

    // @see com.redprairie.moca.DatabaseTool#getDbType()
    @Override
    public String getDbType() {
        return _transactionManager.getDbType();
    }

    // @see com.redprairie.moca.DatabaseTool#getNextSequenceValue(java.lang.String)
    @Override
    public String getNextSequenceValue(String name) throws MocaException {
        return _transactionManager.getNextSequenceValue(name);
    }
    
    /**
     * @param savepoint
     * @throws SQLException
     */
    @Override
    public void rollbackDB(String savepoint) throws SQLException {
        _transactionManager.rollbackDB(savepoint);
    }

    // @see com.redprairie.moca.DatabaseTool#setSavepoint(java.lang.String)
    @Override
    public void setSavepoint(String savepoint) throws SQLException {
        _transactionManager.setSavepoint(savepoint);
    }
    
    // @see com.redprairie.moca.DatabaseTool#executeSQL(java.lang.String, java.util.Map)
    @Override
    public MocaResults executeSQL(String sql, Map<String, ?> args)
            throws MocaException {
        return _transactionManager.executeSQL(sql, args);
    }

    // @see com.redprairie.moca.DatabaseTool#executeSQL(java.lang.String, com.redprairie.moca.MocaArgument[])
    @Override
    public MocaResults executeSQL(String sql, MocaArgument... args)
            throws MocaException {
        return _transactionManager.executeSQL(sql, args);
    }

    private final TransactionManager _transactionManager;
}
