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

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import com.redprairie.moca.server.db.AbstractMocaTransaction;
import com.redprairie.moca.server.exec.UnimplementedOperationException;

/**
 * This class represents when the system is configured to run with no database.
 * Most functions will return unimplemented operation exception.  However
 * transactional features like commit and rollback will work as it will call
 * to the current transaction manager if any transactional aware resources
 * have registered themselves.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class NoDBTransaction extends AbstractMocaTransaction {

    /**
     * @throws SystemException 
     */
    protected NoDBTransaction(TransactionManager transaction) throws SystemException {
        super(transaction);
    }

    // @see com.redprairie.moca.server.db.MocaTransaction#getConnection()
    @Override
    public Connection getConnection() throws SQLException {
        throw new UnimplementedOperationException("No database was supplied");
    }

    // @see com.redprairie.moca.server.db.MocaTransaction#rollbackToSavepoint(java.lang.String)
    @Override
    public void rollbackToSavepoint(String name) throws SQLException {
        throw new UnimplementedOperationException("No database was supplied");
    }

    // @see com.redprairie.moca.server.db.MocaTransaction#setSavepoint(java.lang.String)
    @Override
    public void setSavepoint(String name) throws SQLException {
        throw new UnimplementedOperationException("No database was supplied");
    }
}
