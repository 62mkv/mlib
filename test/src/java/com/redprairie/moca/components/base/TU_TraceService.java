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

package com.redprairie.moca.components.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import junit.framework.Assert;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaTrace;
import com.redprairie.moca.RequiredArgumentException;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.util.AbstractMocaTestCase;
import com.redprairie.moca.util.MocaUtils;

/**
 * This class tests various tracing functions.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_TraceService extends AbstractMocaTestCase {

    /**
     * This tests to make sure the lesdirectory variable replacement works
     * correctly
     */
    public void testGettingATraceFileFromLESDIR() {
        
        String lesDirVariable = System.getenv("LESDIR");
        String testString = "This is just a test\n" +
                            "Please delete this file if opened" +
                            "There are no problems with this!";
        
        // If the les dir variable is not provided don't run test
        if (lesDirVariable == null || lesDirVariable.trim().length() == 0) {
            return;
        }
        
        File lesLogFile = new File(lesDirVariable + File.separator + "log");
        
        assertTrue("LESDIR/log doesn't exist", 
                lesLogFile.exists());
        
        assertTrue("LESDIR/log points to something that isn't a directory", 
                lesLogFile.isDirectory());
        
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", null, lesLogFile);
            
            // Register the temporary file for deletion upon exit
            tempFile.deleteOnExit();
        }
        catch (IOException e) {
            e.printStackTrace();
            fail("There was an error creating temporary file :" + e);
        }
        
        BufferedWriter out = null;
        
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tempFile), "UTF-8"));
            
            out.write(testString);
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception writing to temporary file: " + e);
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ignore) {
                    ignore.printStackTrace();
                }
            }
        }
        
        MocaResults results = null;
        try {
            results = _moca.executeCommand(
                    "get trace file " +
                    "  where pathname = '" + tempFile.getName() + "'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception Encountered :" + e);
        }
        
        RowIterator rowIter = results.getRows();
        
        String[] testStringSplit = testString.split("\n");
        int rowCount = 0;
        
        for (String testStringLine : testStringSplit) {
            assertTrue("There should be a row for every string", rowIter.next());
            assertEquals("The strings don't match what it should", 
                    testStringLine, rowIter.getString("text"));
            assertEquals("The line number doesn't match up", rowCount++, 
                    rowIter.getInt("line"));
        }
    }
    
    /**
     * This test makes sure that setting a trace file works correctly and that
     * disabling afterwards is okay as well.
     * @throws MocaException
     */
    public void testSettingTraceFileAndDisabling() throws MocaException {
        String fileName = "unittest-testfile.log";
        _moca.executeCommand(
                "set trace" +
                "  where filename = '" + fileName + "'" +
                "    and directory = '$LESDIR/log'" +
                "    and activate  = 1");
        
        String expandedLES = MocaUtils.expandEnvironmentVariables(_moca, 
                "$LESDIR/log");
        File traceFile = new File(expandedLES, fileName);
        
        assertTrue("The trace file wasn't created correctly", 
                traceFile.exists());
        
        _moca.executeCommand(
                "set trace" +
                "  where activate = 0");

        assertTrue("Trace file could not be deleted", traceFile.delete());
    }
    
    /**
     * This test makes sure that setting a tracing via context works
     * 
     * @throws MocaException
     */
    public void testSettingTraceViaContextAndDisabling() throws MocaException {
        String fileName = "context-trace.log";
        String expandedLES = MocaUtils.expandEnvironmentVariables(_moca,
            "$LESDIR/log");
        File traceFile = new File(expandedLES, fileName);
        _moca.setTraceLevel("*");
        _moca.setTraceFile(traceFile.getPath(), true);

        // required to actually trace
        _moca.executeCommand("publish data where x = 0");

        assertTrue("The trace file wasn't created correctly",
            traceFile.exists());

        _moca.executeCommand("set trace" + "  where activate = 0");

        assertTrue("Trace file could not be deleted", traceFile.delete());
    }
    
    public void testSettingTraceFileWithNoTraceFile() throws MocaException {
        try { 
            _moca.executeCommand("set trace where activate = 1");
            fail("We should have thrown a RequiredArgumentException");
        }
        catch (RequiredArgumentException e) {
            // We should have gone here.
        }
    }
    
    public void testWritingTraceMessage() throws MocaException {
        _moca.executeCommand(
                "write trace message " +
        	"  where message = 'TEST'" +
        	"    and level = 'X'");
    }
    
    public void testWritingLogMessage() throws MocaException {
        _moca.executeCommand(
                "write log message " +
                "  where message = 'TEST'" +
                "    and level = 'W'");
    }
    
    /**
     * This test is to make sure when setting trace levels that retrieving them
     * works correctly
     * @throws MocaException If a problem occurs while retrieving the trace
     *                       levels
     */
    public void testGetTraceLevels() throws MocaException {
        int traceLevel = 37;
        _moca.setTraceLevel(traceLevel);
        
        MocaResults results = _moca.executeCommand("get current trace levels");
        
        RowIterator rowIter = results.getRows();
        
        assertTrue("There should be 1 row", rowIter.next());
        
        //assertEquals(traceLevel, rowIter.getInt("levels"));
        assertEquals(MocaTrace.getAllLevels(), rowIter.getInt("levels"));
        assertFalse("There should be only 1 row", rowIter.next());
    }
    
    Logger _logger = LogManager.getLogger(TU_TraceService.class);
}
