/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.redprairie.moca.server.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.log.eventfactory.MocaLogEventFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * An integration test class that tests simultaneous threads logging against the current system.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TI_ThreadedLoggingTest {
    
    private static TraceState _traceState;
    
    @BeforeClass
    public static void clearTraceState() throws SystemConfigurationException {
        // delete possible left over log files from previous runs which would cause failures
        cleanUpPreviousTests(false);
        LoggingConfigurator.configure();
        _traceState = MocaLogEventFactory._localTraceState.get();
        MocaLogEventFactory._localTraceState.remove();
    }

    private static void cleanUpPreviousTests(boolean leaving) {
        for (int i = 0; i < 5; i++) {
            final File f = new File("moca-trace-" + i + ".log");
            if (leaving) f.deleteOnExit();
            if (f.exists() && !f.delete()) {
                System.err.println("Unable to delete existing trace file:" + f.getAbsolutePath());
            }
        }
    }
    
    @AfterClass
    public static void restoreTraceState() {
        if (_traceState != null) {
            MocaLogEventFactory._localTraceState.set(_traceState);
        }

        // Cleanup the trace files created by the test.
        System.out.println("Cleaning up moca trace files.");
        cleanUpPreviousTests(true);
    }

    private static class TestRun implements Runnable {
        TestRun(int i) {
            _testNumber = i;
        }
        
        // @see java.lang.Runnable#run()
        @Override
        public void run() {
            // Simulate the MDC behavior of MOCA when it dispatches requests.
            TraceState trace = new TraceState("test" + _testNumber);
            MocaLogEventFactory._localTraceState.set(trace);
            String filename = "moca-trace-" + _testNumber + ".log";
            
            try {
                if (_testNumber % 2 == 0) {
                    System.out.println("Turning on tracing: " + _testNumber);
                    trace.configureLogFileName(filename);
                    trace.setLevel("*");
                    trace.applyTraceStateToThread();
                }
                for (int i = 0; i < 100; i++) {
                   Thread.sleep(20);
                   _log.debug("message [" + i +"] from test [" + _testNumber +"]");
                }
            }
            catch (InterruptedException e) {
                _log.error("caught exception", e);
            }
            finally {
                trace.closeLogging();
                MocaLogEventFactory._localTraceState.remove();
            }
        }
        
        private final int _testNumber;
        static private Logger _log = LogManager.getLogger("com.redprairie.moca.log"); 
    }

    @Test
    public void testMultipleThreadsWithSameLogger() throws Exception {
        
        ExecutorService pool = Executors.newFixedThreadPool(3);
        Future<?>[] result = new Future<?>[5];
        for (int i = 0; i < 5; i++) {
            TestRun command = new TestRun(i);
            result[i] = pool.submit(command);
        }
        
        pool.shutdown();
        
        pool.awaitTermination(30, TimeUnit.SECONDS);
        
        for (int i = 0; i < 5; i++) {
            assertNull(result[i].get());
        }
        
        for (int i = 0; i < 5; i+=2) {
            validateFile(i);
        }
         
        assertFalse(new File("moca-trace-3.log").exists());
        assertFalse(new File("moca-trace-1.log").exists());
    }

    /**
     * @param i The file number
     * @throws IOException 
     */
    private void validateFile(int fileNumber) throws IOException {
        File file = new File("moca-trace-" + fileNumber + ".log");
        assertTrue("file: " + file + " should exist.", file.exists());
        
        BufferedReader input;
        try {
            input = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF-8"));
            
            try {
                String line = null; 
                int lineCount = 0;
                while (( line = input.readLine()) != null){
                    String expected = "message [" + lineCount +"] from test [" + fileNumber +"] []";
                    assertTrue("Expected: " + expected + " in file: " + file, line.endsWith(expected));
                    lineCount++;
                }
              }
              catch (IOException e) {
                  e.printStackTrace();
              }
              finally {
                input.close();
              }  
        }
        catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
    }
}
