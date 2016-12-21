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

import static org.junit.Assert.*;

import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.server.ServerUtils;

/**
 * 
 * Base MOCA test case that handles bootstrapping the
 * daemon context for tests to execute commands.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 */
abstract class AbstractBaseMocaTestCase {
    
    /** 
     * Prepare the test context.  This method is called before every test
     * case is executed.  It ensures that the protected field <code>_moca</code>
     * is initialized correctly and that a transaction is started.  This method
     * is defined in {@link junit.framework.TestCase}.
     */
    public void setUp() throws Exception {
        ServerUtils.setupDaemonContext(getClass().getName(), true, true);
        _moca = MocaUtils.currentContext();
        mocaSetUp();
    }
    
    /** 
     * Clean up the test context.  This method is called after every test case
     * is executed.  It ensures that the transaction started in the
     * <code>startUp</code> method is rolled back, avoiding any unnecessary
     * changes to the database.
     */
    public void tearDown() throws Exception {
        try {
            mocaTearDown();
        }
        finally {
            _moca.rollback();
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
