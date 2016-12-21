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

package com.sam.moca.util;

import junit.framework.TestCase;

import com.sam.moca.MocaContext;

/**
 * This class is a simple wrapper class around TestCase used in JUNIT testing.
 * This class implements 2 functions that are required to use the TestCase which
 * are the SetUp and tearDown for creation and removal of the connection.  There
 * is also another method, testErrorCode which is just a method that will test
 * a specific command to see if it will return the specified error code.  If
 * needed to change or add something to setUp or tearDown, just override the
 * mocaSetUp or mocaTearDown methods.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author William A. Burns
 * @author Derek G. Inksetter
 * @version $Revision$
 */

public abstract class AbstractMocaTestCase extends TestCase {
    
    /** 
     * Prepare the test context.  This method is called before every test
     * case is executed.  It ensures that the protected field <code>_moca</code>
     * is initialized correctly and that a transaction is started.  This method
     * is defined in {@link junit.framework.TestCase}.
     */
    public final void setUp() throws Exception {
        _baseTest.setUp();
    }
    
    /** 
     * Clean up the test context.  This method is called after every test case
     * is executed.  It ensures that the transaction started in the
     * <code>startUp</code> method is rolled back, avoiding any unnecessary
     * changes to the database.
     */
    public final void tearDown() throws Exception {
        _baseTest.tearDown();
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
    protected void testCommandForError(String command, int error) {
        _baseTest.testCommandForError(command, error);
    }
    
    /**
     * The current MOCA context for this test class.  This will hold a current
     * working context whenever a test case is run.
     */
    protected MocaContext _moca;
    
    // Use a composition of the base test class since we must extend
    // TestCase still to work with JUnit 3. This just redirects to our
    // protected methods which subclasses can implement
    private final AbstractBaseMocaTestCase _baseTest = new AbstractBaseMocaTestCase() {
         
        @Override
        protected void mocaSetUp() throws Exception {
            AbstractMocaTestCase.this._moca = this._moca;
            AbstractMocaTestCase.this.mocaSetUp();
        }
         
        @Override
        protected void mocaTearDown() throws Exception {
            AbstractMocaTestCase.this.mocaTearDown();
        }
    };
}
