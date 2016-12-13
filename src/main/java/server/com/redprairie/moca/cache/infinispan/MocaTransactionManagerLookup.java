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

package com.redprairie.moca.cache.infinispan;

import javax.transaction.TransactionManager;

import org.infinispan.transaction.lookup.TransactionManagerLookup;

import com.redprairie.moca.server.TransactionManagerUtils;

/**
 * This class is a simple lookup to be injected into infinispan so it can
 * have a reference to our transaction manager
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaTransactionManagerLookup implements TransactionManagerLookup {

    // @see org.infinispan.transaction.lookup.TransactionManagerLookup#getTransactionManager()
    @Override
    public TransactionManager getTransactionManager() throws Exception {
        return TransactionManagerUtils.getManager();
    }

}
