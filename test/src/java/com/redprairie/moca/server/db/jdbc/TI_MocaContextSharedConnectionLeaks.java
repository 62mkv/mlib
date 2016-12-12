/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2015
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

package com.redprairie.moca.server.db.jdbc;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.exceptions.UnexpectedException;
import com.redprairie.moca.util.AbstractMocaTestCase;
import com.redprairie.moca.util.MocaTestUtils;
import com.redprairie.moca.util.MocaUtils;

/**
 * These are integration tests to validate the behavior
 * of sharing a MocaContext between threads that we correctly detect
 * this as it's bad code essentially.
 * The MocaContext shouldn't be shared across threads as we'll run into
 * issues such as associating multiple JDBC connections with one transaction
 * or the reverse - sharing one connection with multiple transactions.
 * 
 * Copyright (c) 2015 JDA Software
 * All Rights Reserved
 */
public class TI_MocaContextSharedConnectionLeaks extends AbstractMocaTestCase {
    
    private static final String TEST_SQL = "select 1 from dual";
    
    /**
     * In this test the following occurs:
     * <ol>
     * <li> We spawn a child thread and pass it the parent threads MocaContext
     * <li> We haven't actually done anything in the parent thread yet so it hasn't established
     *      a connection/transaction yet. We execute a SQL statement using the child context (works fine)
     *      then a SQL statement using the parent context.
     * <li> When executing using the parent context an {@link IllegalStateException} should happen because
     *      there will be an attempt made to register a second connection (the parent MocaContexts) with
     *      the transaction that the child thread is using (since transactions are scoped to the current thread
     *      and because the parent thread never established a transaction). When this happens the JBoss
     *      Transaction Manager will complain it can't enlist a second resource of the same type and fail
     *      which we react to by throwing up the {@link IllegalStateException}.
     * <li> Back in the parent thread when we execute some SQL it will also fail with {@link IllegalStateException}
     *      because it sees two transactions (the child one from the previous step and the just created one in the parent
     *      and complains how we're sharing them).
     * </ol>
     * 
     * This is basically the same as {@link #testLeakingContextBetweenThreadsParentExecutesFirst()} except step 3
     * the error happens for a slightly different reason (associating two connections with a single transaction).
     * 
     * @throws MocaException
     * @throws InterruptedException
     * @throws BrokenBarrierException
     * @throws ExecutionException
     */
    @Test
    public void testLeakingContextBetweenThreadsChildExecutesFirst() throws MocaException,
            InterruptedException, BrokenBarrierException, ExecutionException {
        testSharingMocaContextWithChildThread(false);
    }
    
    /**
     * In this test the following occurs:
     * <ol>
     * <li> We make a SQL call in the parent thread's MocaContext, this makes it so that
     *      both a transaction and connection are established within the MocaContext in the parent thread.
     * <li> We spawn a child thread and pass it the parent threads MocaContext
     * <li> We execute another SQL statement inside of the child thread using the parent thread's MocaContext.
     *      When this happens the existing connection tries to register itself with the child thread's transaction
     *      but it should fail with {@link IllegalStateException} because the connection was already associated
     *      with the parent transaction therefore trying to link it to the child transaction should complain about
     *      it already being in use by another transaction.
     * <li> After validating the child thread behavior we go back to the parent thread where we execute SQL again
     *      and it should fail the same way because now it sees that the connection got associated with the child
     *      transaction (from the previous step) and complains with an {@link IllegalStateException} because we're
     *      trying to associate the parent again.
     * </ol>
     * 
     * This is basically the same as {@link #testLeakingContextBetweenThreadsChildExecutesFirst()} except step 3
     * the error happens for a slightly different reason (trying to associate a single connection with two different transactions).
     * 
     * @throws MocaException
     * @throws InterruptedException
     * @throws BrokenBarrierException
     * @throws ExecutionException
     */
    @Test
    public void testLeakingContextBetweenThreadsParentExecutesFirst() throws MocaException,
    InterruptedException, BrokenBarrierException, ExecutionException {
        testSharingMocaContextWithChildThread(true);
    }
    
    private void testSharingMocaContextWithChildThread(boolean executeSqlInParentThreadFirst) 
            throws InterruptedException, BrokenBarrierException, ExecutionException, MocaException {
        final MocaContext context = MocaUtils.currentContext();       
        
        // Get some connections setup in the DB pool
        MocaTestUtils.makeConcurrentDbRequests(5, 1);
        
        // This will change the behavior of test slightly, see both test cases, basically if this
        // executes first then the parent thread will already have a transaction/connection.
        if (executeSqlInParentThreadFirst) {
            context.executeSQL(TEST_SQL);    
        }
        
        // Some references we can use in the parent thread to validate child thread execution
        final AtomicReference<MocaException> childThreadException = new AtomicReference<MocaException>();
        final AtomicBoolean localContextExecutedCorrectly = new AtomicBoolean();
        
        // Spawn a child thread and share the parent thread's MocaContext with it (bad!).
        Thread thread = new Thread() {

            @Override
            public void run() {
                MocaContext localContext = MocaUtils.currentContext();
                try {
                    // Using the local context first should always work
                    localContext.executeSQL(TEST_SQL);
                    localContextExecutedCorrectly.set(true);
                    
                    // Failure occurs here for various reasons either:
                    // 1) Two connections (parent/child) for a single transaction (child transaction)
                    //    - see testLeakingContextBetweenThreadsChildExecutesFirst
                    // 2) A single connection trying to be associated with two transactions
                    //    - see testLeakingContextBetweenThreadsParentExecutesFirst
                    context.executeSQL(TEST_SQL);
                    localContext.commit();
                }
                catch (MocaException expectedFromSecondSql) {
                    try {
                        childThreadException.set(expectedFromSecondSql);
                        localContext.rollback();
                    }
                    catch (MocaException e1) {
                        // Ignore
                    }
                    
                }
            }
        };
        
        thread.start();
        thread.join();
        
        assertTrue("The local context executes first and should alwyays complete",
            localContextExecutedCorrectly.get());
        MocaException childException = childThreadException.get();
        // Should fail with an IllegalStateException in regards to transaction/connection
        // usage across threads wrapped inside of an UnexpectedException
        assertEquals(UnexpectedException.class, childException.getClass());
        assertEquals(IllegalStateException.class, childException.getCause().getClass());
        
        try {
            context.executeSQL(TEST_SQL);
            context.commit();
            fail("Should have failed with IllegalStateException");
        }
        catch (Exception expected) {
            context.rollback();
            // Should fail with an IllegalStateException in regards to transaction/connection
            // usage across threads wrapped inside of an UnexpectedException
            assertEquals(UnexpectedException.class, expected.getClass());
            assertEquals(IllegalStateException.class, expected.getCause().getClass());
        }
        
        // Make sure the state of the pool/server is fine and we can still execute more SQL
        MocaTestUtils.makeConcurrentDbRequests(10, 5);
    }

}