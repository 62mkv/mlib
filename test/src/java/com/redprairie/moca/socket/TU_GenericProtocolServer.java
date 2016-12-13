/*
 *  $URL$
 *  $Author$
 *  $Date$
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

package com.redprairie.moca.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.client.ConnectionFailedException;

import static org.junit.Assert.*;

/**
 * Tests the Generic Protocol Server to re-ensure that the interrupted flag is
 * set after an interrupt.
 * 
 * Copyright (c) 2011 Sam Corporation All Rights Reserved
 * 
 * @author klehrke
 */
public class TU_GenericProtocolServer {
    /***
     * Test that the GenericProtocolServer is interrupted properly and that the
     * executor service shuts down.
     * 
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void testSelectInterrupt() throws InterruptedException, TimeoutException {
        Exchanger<Boolean> exchanger = new Exchanger<Boolean>();
        int port = 0;
        ExecutorService es = Mockito.mock(ExecutorService.class);
        RunnableTask rt = new RunnableTask(es, exchanger, port);
        Thread testThread = new Thread(rt);
        testThread.start();
        testThread.join(2000);
        testThread.interrupt();
        Boolean interrupted = exchanger.exchange(null, 100, TimeUnit.MILLISECONDS);
        assertTrue(interrupted);
        Mockito.verify(es, Mockito.timeout(1000)).shutdownNow();

    }

    /***
     * Test that the GenericProtocolServer is interrupted properly and that the
     * executor service shuts down.
     * 
     * @throws InterruptedException
     * @throws TimeoutException 
     */
    @Test
    public void testImmediateInterrupt() throws InterruptedException, TimeoutException {
        Exchanger<Boolean> exchanger = new Exchanger<Boolean>();
        int port = 0;
        ExecutorService es = Mockito.mock(ExecutorService.class);
        RunnableTask rt = new RunnableTask(es, exchanger, port);
        Thread testThread = new Thread(rt);
        testThread.start();
        testThread.interrupt();
        Boolean interrupted = exchanger.exchange(null, 100, TimeUnit.MILLISECONDS);
        assertTrue(interrupted);
        Mockito.verify(es, Mockito.timeout(1000)).shutdownNow();
    }
    
    /**
     * This test is to make sure that if 5 separate threads all connect to
     * the socket that they will all work and get EOF signals appropriately
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     * @throws SocketProcessorException
     * @throws IOException 
     */
    @Test
    public void testLotsOfConcurrentConnectionsAndWrites() throws 
            InterruptedException, TimeoutException, ExecutionException, 
            SocketProcessorException, IOException {
        Exchanger<Boolean> exchanger = new Exchanger<Boolean>();
        
        // we open a server socket channel to get an emphemeral port
        ServerSocket sock = new ServerSocket();
        sock.setReuseAddress(true);
        sock.bind(null);
        // Save off the port and close it so we can use it.
        int port = sock.getLocalPort();
        sock.close();
        
        ExecutorService es = Mockito.mock(ExecutorService.class);
        CommandContextSocketProcessor processor = Mockito.mock(
            CommandContextSocketProcessor.class);
        RunnableTask rt = new RunnableTask(es, exchanger, port, processor);
        Thread testThread = new Thread(rt);
        testThread.start();
        
        ExecutorService service = Executors.newCachedThreadPool();
        Callable<Integer> socket = new SocketRunnable(port);
        int size = 5;
        Collection<Future<Integer>> futures = new ArrayList<Future<Integer>>(size);
        for (int i = 0; i < size; ++i) {
            futures.add(service.submit(socket));
        }
        
        // We wait until all 5 are handed off
        Mockito.verify(processor, Mockito.timeout(1000).times(size)).getSocketProcessor();
        testThread.interrupt();
        // We now wait for all of them to get the stop signals appropriately.
        for (Future<Integer> future : futures) {
            assertEquals(Integer.valueOf(-1), future.get(1, TimeUnit.SECONDS));
        }
        Boolean interrupted = exchanger.exchange(null, 100, TimeUnit.MILLISECONDS);
        assertTrue(interrupted);
        Mockito.verify(es, Mockito.timeout(1000)).shutdownNow();
    }
    
    /**
     * Tests that we're able to start a server on a random port and get
     * back the bounded port after socket binding occurs
     * @throws InterruptedException 
     */
    @Test
    public void testServerOnRandomPort() throws InterruptedException {
        final GenericProtocolServer server = new GenericProtocolServer(0, 1);
        // delay the startup/binding of the socket by 100ms
        // to test waiting for it to be bound.
        final int startDelay = 100; 
        Thread t = new Thread() {
            public void run() {
                try {
                    Thread.sleep(startDelay);
                    server.start(Mockito.mock(CommandContextSocketProcessor.class));
                }
                catch (SocketProcessorException e) {
                    e.printStackTrace();
                    fail("Exception should not have occurred during test");
                }
                catch (InterruptedException expected) {
                    // We're interrupting this thread in the test
                }
            }
        };
        t.start();
        // Wait until the socket has been bound
        while (server.getPort() == 0) {
            // We'll poll then for roughly 10 iterations given our startDelay
            Thread.sleep(startDelay / 10);
        }
        assertTrue(server.getPort() > 0);
        t.interrupt();
    }

    private static class SocketRunnable implements Callable<Integer> {
        
        public SocketRunnable(int port) {
            _port = port;
        }
        
        private final int _port;

        // @see java.util.concurrent.Callable#call()
        @Override
        public Integer call() throws Exception {
            InetSocketAddress addr = new InetSocketAddress("localhost", _port);
            if (addr.isUnresolved()) {
                throw new ConnectionFailedException("Unable to resolve address");
            }
            SocketChannel _sc = SocketChannel.open(addr);
            Socket _s = _sc.socket();
            
            OutputStream _out = new BufferedOutputStream(_s.getOutputStream());
            InputStream _in = new BufferedInputStream(_s.getInputStream());
            
            // Just write to force a notification
            _out.write(new byte[]{0x21, 0x54});
            _out.flush();
            
            // We should always receive end of file
            return _in.read(new byte[2]);
        }
        
    }

    /***
     * 
     * Helper Runnable class to run the GenericProtocol Server from.
     * 
     * 
     * Copyright (c) 2011 Sam Corporation All Rights Reserved
     * 
     * @author klehrke
     */
    static class RunnableTask implements Runnable {
        final ExecutorService es;
        final int port;
        final Exchanger<Boolean> ex;
        final CommandContextSocketProcessor processor;

        public RunnableTask(ExecutorService es, Exchanger<Boolean> ex, int port) {
            this(es, ex, port, Mockito.mock(CommandContextSocketProcessor.class));
        }
        
        public RunnableTask(ExecutorService es, Exchanger<Boolean> ex, int port, 
            CommandContextSocketProcessor processor) {
            this.es = es;
            this.port = port;
            this.ex = ex;
            this.processor = processor;
        }

        public void run() {
            GenericProtocolServer gps = new GenericProtocolServer(port, es);
            try {
                gps.start(processor);
            }
            catch (SocketProcessorException e) {
                e.printStackTrace();
            }

            try {
                ex.exchange(Thread.interrupted(), 100, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
    }
}
