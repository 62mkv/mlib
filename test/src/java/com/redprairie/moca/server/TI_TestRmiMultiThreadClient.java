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

package com.redprairie.moca.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.Charset;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.moca.util.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * This class is to test to make sure that when a user creates a multi threaded
 * server task that exports to RMI that those RMI threads can correctly access
 * the moca context.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TI_TestRmiMultiThreadClient {
    
    private static final int AWAIT_TIMEOUT = TestUtils.getTestTimeout(TI_TestRmiMultiThreadClient.class, "AWAIT", 300);;
    private final static String BINDING_NAME = TI_TestRmiMultiThreadClient.class.getName() + new UID();
    private final static String READY_SIGNAL = "READY_SIGNAL";
    private final static String EXCEPTION_SIGNAL = "EXCEPTION_SIGNAL";
    
    private static Logger _logger = LogManager.getLogger();
    private Process _process = null;
    private Thread _outputThread = null; 
    // The CopyOnWriteArrayList is guaranteed not to throw ConcurrentModificationException
    final List<String> _stdout = new CopyOnWriteArrayList<String>();

    /**
     * @param args
     * @throws Throwable 
     * @throws RemoteException 
     * @throws AlreadyBoundException 
     * @throws SystemConfigurationException 
     */
    public static void main(String[] args) throws Throwable {
        try{
            RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
            System.out.println("Setting up daemon context.  Uptime: " + mxBean.getUptime());
            ServerUtils.setupDaemonContext("Test", false, true);
            _logger.info("Done setting up daemon context.  Uptime: " + mxBean.getUptime());

            TestRmi test = new TestRmi();
            String rmiPort = MocaUtils.currentContext().getRegistryValue(MocaRegistry.REGKEY_SERVER_RMI_PORT);
            int _rmiPort = rmiPort != null ? Integer.parseInt(rmiPort) : 
                Integer.parseInt(MocaRegistry.REGKEY_SERVER_RMI_PORT_DEFAULT);

            _logger.info("Exporting RMI object.  Uptime: " + mxBean.getUptime());
            Remote stub = UnicastRemoteObject.exportObject(test, 0);
            Registry registry = LocateRegistry.getRegistry(_rmiPort);
            // We bind it as a simple name
            _logger.info("Rebinding RMI registry.  Uptime: " + mxBean.getUptime());
            registry.rebind(args[0], stub);
            _logger.info("Done rebinding RMI registry.  Uptime: " + mxBean.getUptime());

            System.out.println(READY_SIGNAL);
        } catch (Throwable e){
            e.printStackTrace();
            System.out.println(EXCEPTION_SIGNAL);
        }
    } 

    public static interface TestRmiInterface extends Remote {
        public void doRemoteCall() throws RemoteException;
    }
    
    
    private static class TestRmi implements TestRmiInterface {

        // @see com.redprairie.moca.server.TI_TestRmiCalls.TestRmiInterface#doRemoteCall()
        @Override
        public void doRemoteCall() throws RemoteException {
            MocaContext moca = MocaUtils.currentContext();
            
            moca.logInfo("Worked");
        }
        
    }
    
    @BeforeClass
    public static void setupClass() throws SystemConfigurationException {
        ServerUtils.setupDaemonContext(TI_TestRmiMultiThreadClient.class.getName(), 
                false);
    }

    @After
    public void mocaTearDown() throws Exception {

        // Print the output from the child process to help troubleshooting timeout failures
        _logger.info("=========== Output From Child Process ==========");
        for(String line: _stdout){
            _logger.info(line);
        }
        _logger.info("================================================");

        // If we started up a process in our test, we have to make sure to
        // shut it down properly
        if (_process != null) {
            _process.destroy();
            _process.waitFor();
        }

        // Cleanup the thread as well
        if( _outputThread != null){
            if(_outputThread.isAlive()){
                _outputThread.interrupt();
                _outputThread.join();
            }
        }
    }

    /**
     * This test makes sure that when someone exports an Object into RMI that if
     * you properly configure the MOCA server before doing so that RMI threads
     * will have access to the server.
     * @throws NumberFormatException
     * @throws NotBoundException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testGettingObjectFromRmi() throws NumberFormatException, 
            NotBoundException, IOException, InterruptedException {
        
        // We want to start up ourselves so that we can connect via RMI
        String[] vmCommandLine = MocaUtils.newVMCommandLine();
        
        List<String> commandLine = new ArrayList<String>();
        
        commandLine.addAll(Arrays.asList(vmCommandLine));
        commandLine.add(this.getClass().getCanonicalName());
        commandLine.add(BINDING_NAME);
        
        ProcessBuilder pb = new ProcessBuilder(commandLine);
        // We redirect error stream so that if an error is printed out
        // we will see that message from the spawned process
        pb.redirectErrorStream(true);
        // We want to remove the trace level, so that we are sure we get the
        // ready signal only
        pb.environment().put("MOCA_TRACE_LEVEL", "*");
        
        _process = pb.start();

        final BufferedReader processOutput = new BufferedReader(
                new InputStreamReader(_process.getInputStream(), Charset.defaultCharset()));

        final CountDownLatch readyLatch = new CountDownLatch(1);
        final AtomicBoolean isException = new AtomicBoolean(false);
        
        _outputThread = new Thread() {
            @Override
            public void run() {
                try {
                    // We only want the first line output
                    String line;
                    while ((line = processOutput.readLine()) != null) {
                        _stdout.add(line);
                        if(line.contains(READY_SIGNAL)){
                            readyLatch.countDown();
                        } else if(line.contains(EXCEPTION_SIGNAL)){
                            isException.set(true);
                            readyLatch.countDown();
                        }
                    }
                }
                catch (IOException e) {
                    _stdout.add(e.toString());
                    readyLatch.countDown();
                }
            }
        };

        // We give the process 300 seconds to start up
        _outputThread.start();
        readyLatch.await(AWAIT_TIMEOUT, TimeUnit.SECONDS);
        assertEquals("Timeout waiting for the ready signal " + READY_SIGNAL, 0, readyLatch.getCount());
        assertFalse("Exception caught during execution", isException.get());
        
        String rmiPort = MocaUtils.currentContext().getRegistryValue(
                MocaRegistry.REGKEY_SERVER_RMI_PORT);
        int _rmiPort = rmiPort != null ? Integer.parseInt(rmiPort) : 
            Integer.parseInt(MocaRegistry.REGKEY_SERVER_RMI_PORT_DEFAULT);
        
        Registry registry = LocateRegistry.getRegistry("localhost", _rmiPort);
        TestRmiInterface stub = (TestRmiInterface)registry.lookup(BINDING_NAME);
        
        stub.doRemoteCall();
    }
}
