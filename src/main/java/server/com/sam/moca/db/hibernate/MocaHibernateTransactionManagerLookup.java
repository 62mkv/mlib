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

package com.sam.moca.db.hibernate;

import java.util.Properties;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.hibernate.HibernateException;
import org.hibernate.transaction.TransactionManagerLookup;

import com.sam.moca.server.TransactionManagerUtils;

/**
 * Transaction Manager that is used with hibernate to provide it our 
 * configured transaction manager.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaHibernateTransactionManagerLookup implements
        TransactionManagerLookup {

    // @see org.hibernate.transaction.TransactionManagerLookup#getTransactionIdentifier(javax.transaction.Transaction)
    @Override
    public Object getTransactionIdentifier(Transaction transaction) {
        return transaction;
    }

    // @see org.hibernate.transaction.TransactionManagerLookup#getTransactionManager(java.util.Properties)
    @Override
    public TransactionManager getTransactionManager(Properties props)
            throws HibernateException {
        return TransactionManagerUtils.getManager();
    }

    // @see org.hibernate.transaction.TransactionManagerLookup#getUserTransactionName()
    @Override
    public String getUserTransactionName() {
        return null;
    }

}
