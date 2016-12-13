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

package com.redprairie.moca.util;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.ServerUtils.CurrentValues;
import com.redprairie.moca.server.TestServerUtils;
import com.redprairie.moca.server.TransactionManagerUtils;
import com.redprairie.moca.server.exec.SystemContext;

/**
 * This class is a simple wrapper class around TestCase used in JUNIT testing.
 * This class implements 2 functions that are required to use the TestCase which
 * are the SetUp and tearDown for creation and removal of the connection.  There
 * is also another method, testErrorCode which is just a method that will test
 * a specific command to see if it will return the specified error code.  If
 * needed to change or add something to setUp or tearDown, just override the
 * mocaSetUp or mocaTearDown methods.
 * 
 * This class differs from AbstractMocaTestCase in that it allows for multi
 * concurrent C code executions to occur.  This TestCase relies that rmi 
 * registry is running.  Also this TestCase can be a bit slower in that it sets
 * up the context on each test function.  When we move this to JUnit 4 it will
 * become faster in that it will only occur this cost per test file instead 
 * instead of per test.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public abstract class AbstractMocaMultiThreadTestCase extends TestCase {
    private CurrentValues _previousValues = null;
    private SystemContext _previousSystem = null;
    
    /** 
     * Prepare the test context.  This method is called before every test
     * case is executed.  It ensures that the protected field <code>_moca</code>
     * is initialized correctly and that a transaction is started.  This method
     * is defined in {@link junit.framework.TestCase}.
     */
    public final void setUp() throws Exception {
        TransactionManager manager = null;
        try {
            manager = TransactionManagerUtils.getManager();
        }
        catch (IllegalStateException e) {
            // Ignore if no transaction manager is setup yet
        }
        if (manager != null) {
            Transaction tx = manager.getTransaction();
            int status = tx == null ? Status.STATUS_NO_TRANSACTION : tx.getStatus();
            if (status != Status.STATUS_COMMITTED && 
                    status != Status.STATUS_ROLLEDBACK &&
                    status != Status.STATUS_NO_TRANSACTION) {
                throw new IllegalStateException("Previous test(s) left a transaction open!");
            }
        }
        
        try {
            _previousValues = TestServerUtils.takeCurrentValues();
            _previousSystem = TestServerUtils.getGlobalContext();
        }
        catch (IllegalStateException ignore) {
            // We don't care if it wasn't initialized yet.  We only do this
            // so we can restore the context afterwards
            // This only happens if it is the first test to initialize the 
            // context
        }
        TestServerUtils.overrideGlobalContext(null);
        // First we null out the current context
        ServerUtils.setCurrentContext(null);
        ServerUtils.setupDaemonContext(this.getClass().getName(), 
                false, true);
        _moca = MocaUtils.currentContext();
        mocaSetUp();
    }
    
    /** 
     * Clean up the test context.  This method is called after every test case
     * is executed.  It ensures that the transaction started in the
     * <code>startUp</code> method is rolled back, avoiding any unnecessary
     * changes to the database.
     */
    public final void tearDown() throws Exception {
        try {
            mocaTearDown();
        }
        finally {
            CurrentValues temporaryValues = TestServerUtils.takeCurrentValues();
            temporaryValues.getServerContext().close();
            temporaryValues.getServerContextFactory().close();
            temporaryValues.getTraceState().closeLogging();

            if (_previousValues != null) {
                // Now restore the context back to what it was
                TestServerUtils.restoreValues(_previousValues);
                _previousValues = null;
            }
            
            SystemContext ctx = TestServerUtils.getGlobalContext();
            if (ctx != null) {
                ctx.clearAttributes();
            }
            TestServerUtils.overrideGlobalContext(_previousSystem);
            _previousSystem = null;
            _moca = null;
        }
    }
    
    /**
     * Called during the JUnit setUp phase (i.e. before every test case), after
     * the MOCA context is set up.
     */
    protected void mocaSetUp() throws Exception {
        // default implementation does nothing
    }

    /**
     * Called during the JUnit tearDown phase (i.e. after every test case),
     * before the database transaction is rolled back.
     */
    protected void mocaTearDown() throws Exception {
        // default implementation does nothing
    }
    
    /**
     * This method is to test a specific command if it will throw the desired
     * error code to test JUNITs.  It just simply executes the command in the
     * current MOCA context and if a MocaException is tossed it will catch it
     * and see if the value is equal to the error number provided.  If it is
     * different then the test will fail, the same if the command executes
     * successfully.
     * 
     * @param command - (Required) name of command to error.
     * @param error - (Required) int with value of error code to be returned.
     */
    protected void testCommandForError(String command, int error)
    {
        MocaResults res = null;
        try {
            res = _moca.executeCommand(command);
            fail("Command succeeded -- expected error code " + error);
        }
        catch (MocaException e) {
            assertEquals("Expected error code", error, e.getErrorCode());
        }
        finally {
            if (res != null) {
                res.close();
            }
        }
    }
    
    /**
     * The current MOCA context for this test class.  This will hold a current
     * working context whenever a test case is run.
     */
    protected MocaContext _moca;
}
