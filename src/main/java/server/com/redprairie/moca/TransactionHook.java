/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.redprairie.moca;

/**
 * An interface representing a mechanism for an application to synchronize
 * its state with a MOCA transaction.  A class implementing this interface
 * should register itself with a <code>MocaContext</code> via the
 * {@link MocaContext#addTransactionHook} method.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public interface TransactionHook {
    /**
     * This method will get called before MOCA commits the current transaction.
     * If the current transaction is to be rolled back, this method is not
     * called.  If this method throws an exception, 
     * @param ctx a reference to the current MOCA context.
     * @throws MocaException
     */
    public void beforeCommit(MocaContext ctx) throws MocaException;
    
    /**
     * This method will get called after MOCA's transaction is completed (i.e.
     * either committed or rolled back).  Care should be taken not to perform
     * any database operations in this method that could cause locks to be
     * taken out, as they will be held until the end of the following
     * transaction.
     * 
     * @param ctx a reference to the current MOCA context.
     * @param committed <code>true</code> if the transaction has been committed,
     * <code>false</code> if it has been rolled back.
     * @throws MocaException
     */
    public void afterCompletion(MocaContext ctx, boolean committed)
            throws MocaException;
}
