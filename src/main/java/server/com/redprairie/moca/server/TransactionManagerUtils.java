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

package com.redprairie.moca.server;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * This class is used to retrieve the transaction manager that is currently
 * associated with the MOCA process.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TransactionManagerUtils {
    /**
     * Returns the current transaction tied to this thread or null if
     * no transaction is currently in use
     * @return
     * @throws SQLException
     */
    public static Transaction getCurrentTransaction() {
        TransactionManager manager = managerRef.get();
        if (manager == null) {
            throw new IllegalStateException("No transaction manager was configured");
        }
        
        try {
            int state = manager.getStatus();

            if (state != Status.STATUS_ACTIVE && 
                    state != Status.STATUS_MARKED_ROLLBACK) {
                return null;
            }
            
            return manager.getTransaction();
        }
        catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static TransactionManager getManager() {
        TransactionManager manager = managerRef.get();
        
        if (manager == null) {
            throw new IllegalStateException("No transaction manager was configured");
        }
        
        return manager;
    }
    
    static void registerTransactionManager(TransactionManager manager) {
        managerRef.set(manager);
    }
    
    private static AtomicReference<TransactionManager> managerRef = 
        new AtomicReference<TransactionManager>();
}
