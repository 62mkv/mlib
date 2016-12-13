/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.util.DaemonThreadFactory;
import com.redprairie.moca.util.NonMocaDaemonThreadFactory;

/**
 * The core MOCA socket server layer.  This class will initiate a server on
 * a given port and hand off the socket to a processing class upon activity.
 * 
 * @author dinksett
 */
public class GenericProtocolServer {
    
    /**
     * Creates an instance of a socket server on the given port, and using a
     * thread pool of size <code>poolSize</code> threads.
     * 
     * @param port the port to use to listen on.
     * @param poolSize the size of the thread pool to use to handle incoming requests.
     */
    public GenericProtocolServer(int port, int poolSize) {
        // TODO: need to make this non moca daemon thread factory and pass out new request contextes
        this(port, Executors.newFixedThreadPool(poolSize, new DaemonThreadFactory("sock-" + port)));
    }
    
    /**
     * Creates an instance of a socket server on the given port, and using a thread pool
     * passed in to handle incoming requests.
     * 
     * @param port the port to use to listen on.
     * @param pool the thread pool to use to handle incoming requests.
     */
    GenericProtocolServer(int port, ExecutorService pool) {
        _port = port;
        _execPool = pool;
    }
    
    /**
     * Start listening for requests
     * @param factory
     */
    public void start(SocketProcessorFactory factory) throws SocketProcessorException {
        Selector selector = null;
        SelectionKey serverKey;
        ServerSocketChannel listener = null;
        boolean wasInterrupted = false;
        Set<SocketChannel> registeredChannels = Collections.synchronizedSet(
            new HashSet<SocketChannel>());

        try {
            // First, configure a listener socket channel.  All requests will come through on that channel.
            listener = ServerSocketChannel.open();
            ServerSocket socket = listener.socket();
            socket.bind(new InetSocketAddress(_port));
            // Re-assign the port number after binding incase 0 (random free port) was specified
            _port = socket.getLocalPort();
            listener.configureBlocking(false);

            // We need a Selector and SelectionKey to enable NIO non-blocking reads.
            selector = Selector.open();
            serverKey = listener.register(selector, SelectionKey.OP_ACCEPT);
            
            try {
                // This queue is set up to allow execution threads to signal that they're done with the socket. 
                Queue<SocketChannel> returnedClients = new ConcurrentLinkedQueue<SocketChannel>();

                
                // Execution occurs on blocking sockets.  We're only looking for activity on the incoming socket, then we
                // remove it from the list of sockets we're selecting on.
                for (;;) {
                    
                    // If a socket has been returned to us, put it back in the select list.
                    while (!returnedClients.isEmpty()) {
                        SocketChannel client = returnedClients.poll();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    }

                    // Select a socket that's ready.
                    selector.select();
                    
                    // If interrupt occurs during select the thread still will
                    // be interrupted.
                    if(Thread.interrupted()){
                        _logger.info("ServerSocket was interrupted while" +
                        		" listening for clients.");
                        wasInterrupted = true;
                        break;
                    }
                    
                    
                    Set<SelectionKey> keys = selector.selectedKeys();

                    Iterator<SelectionKey> iterator = keys.iterator();
                    // Looking at each key, take the appropriate action
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        // We handle all so remove them all
                        iterator.remove();
                        
                        if (key == serverKey) {
                            // New incoming connection
                            SocketChannel client = listener.accept();
                            if (client != null) {
                                client.configureBlocking(false);
                                client.register(selector, SelectionKey.OP_READ);
                            }
                        }
                        else {
                            // Activity on an already open connection.  First, we cancel the select operation
                            key.cancel();
                            
                            SocketChannel client = (SocketChannel) key.channel();
                            
                            // Then, reconfigure the socket channel to blocking mode.  This is because we have
                            // code that expects to operate on the socket's data in a procedural fashion.
                            client.configureBlocking(true);
                            
                            SocketProcessor processor = factory.getSocketProcessor();
                            
                            registeredChannels.add(client);
                            
                            _execPool.submit(new ActivityHandler(client, 
                                processor, selector, returnedClients, 
                                registeredChannels));
                        }
                    }
                }
            }
            catch (IOException e) {
                _logger.error("IO Error from socket processor factory", e);

                if (checkForInterruptedException(e)) {
                    wasInterrupted = true;
                }
            }
            catch (SocketProcessorException e) {
                _logger.error("Error from socket processor factory", e);
            }
        }
        catch (IOException e) {
            if (checkForInterruptedException(e)) {
                wasInterrupted = true;
            }
            throw new SocketProcessorException("Unable to listen on port "
                    + _port + ": " + e, e);
        }
        finally {
            if (selector != null) {
                try {
                    selector.close();
                }
                catch (IOException e) {
                    _logger.debug("Problem closing server selector", e);
                }
            }
            
            if (listener != null) {
                try {
                    listener.close();
                }
                catch (IOException e) {
                    _logger.debug("Problem closing server socket channel", e);
                }
            }
            
            // This has to have daemon threads in case if the threads got in
            // a state that they wouldn't respond
            ExecutorService service = Executors.newCachedThreadPool(
                new NonMocaDaemonThreadFactory("ProtocolServer SocketCloser"));
            try {
                boolean interrupted = Thread.interrupted();
                synchronized (registeredChannels) {
                    Iterator<SocketChannel> iterator = registeredChannels.iterator();
                    while (iterator.hasNext()) {
                        final SocketChannel channel = iterator.next();
                        // We remove the values from the set via the iterator
                        // since the set is shared in another thread and
                        // we don't want the other thread if it is stuck to
                        // keep a reference to all of the sockets
                        iterator.remove();
                        // If we were interrupted then just always forcibly terminate
                        // to not wait for shutdown
                        if (!interrupted) {
                            Future<Void> future = service.submit(new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    // Try to shut these down nicely
                                    channel.socket().shutdownOutput();
                                    channel.socket().shutdownInput();
                                    return null;
                                }
                            });
                            
                            try {
                                future.get(2, TimeUnit.SECONDS);
                            }
                            catch (InterruptedException e) {
                                interrupted = true;
                            }
                            catch (ExecutionException e) {
                                _logger.debug("There was a problem closing down socket nicely, forcibly closing");
                            }
                            catch (TimeoutException e) {
                                _logger.debug("Took longer than 2 seconds to close socket nicely, forcibly closing");
                            }
                        }
                        
                        try {
                            channel.close();
                        }
                        catch (IOException e) {
                            _logger.warn("There was a problem closing down client socket channel", e);
                        }
                    }
                }
                
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
            finally {
                service.shutdownNow();
            }
            
            //We know that we were interrupted.
            //Shutdown the executor service and interrupt ourself.
            if(wasInterrupted){
                Thread.currentThread().interrupt();
            }
            
            _execPool.shutdownNow();
        }
        
    }
    
    /**
     * Gets the port the server is set to run on. If a random free port is specified by
     * using the port 0, this method will return 0 until {@link #start(SocketProcessorFactory)}
     * is called where the port is then bound to an actual port number.
     * @return The port
     */
    public int getPort() {
        return _port;
    }
    
    private boolean checkForInterruptedException(IOException e) {
        Throwable e1 = e;
        while (e1 != null) {
            // is this the InterruptedIOException?
            if (e1 instanceof InterruptedException
                    || e1 instanceof InterruptedIOException
                    || e1 instanceof ClosedByInterruptException) {
                // Yes, so we set to interrupt.
                return true;
            }
            // Keep going deeper to find the possible
            // InterruptIOException.
            e1 = e1.getCause();
        }

        return false;
    }
    
    /*
     * Handles the activity of an incoming socket channel.
     */
    private static class ActivityHandler implements Runnable {
        public ActivityHandler(SocketChannel client, SocketProcessor processor,
                                Selector selector, Queue<SocketChannel> returnQueue,
                                Set<SocketChannel> registeredSockets) {
            _client = client;
            _selector = selector;
            _returnQueue = returnQueue;
            _processor = processor;
            _registeredSockets = registeredSockets;
        }

        @Override
        public void run() {
            try {
                // Detect close of input
                _logger.debug("Socket activity");
                PushbackInputStream in = new PushbackInputStream(_client.socket().getInputStream());
                
                int available = in.read();
                if (available < 0) {
                    _logger.debug("Detected socket close");
                    _client.close();
                }
                else {
                    in.unread(available);
                    Socket sock = _client.socket();
                    OutputStream out = sock.getOutputStream();
                    SocketEndpoint publicSocket = new SocketEndpoint(sock, in, out);
                    _logger.debug("Detected socket request activity.  Calling socket processor");
                    _processor.process(publicSocket);
                    _logger.debug("Socket processing complete.");
                }
                
                if (_client.isOpen()) {
                    _logger.debug("Returning socket back to the pool.");
                    _returnQueue.add(_client);
                    _selector.wakeup();
                }
            }
            catch (IOException e) {
                _logger.error("Error reading socket", e);
                silentlyClose(_client);
            }
            catch (SocketProcessorException e) {
                _logger.error("Error processing socket request", e);
                silentlyClose(_client);
            }
            // Safeguard from a bad SocketProcessor, close the socket for unexpected exceptions
            catch (Throwable t) {
                _logger.error("Unexpected error occurred while processing socket request", t);
                silentlyClose(_client);
                throw t;
            }
            finally {
                // If the channel is not open then the socket was closed,
                // so we don't have to retain a reference
                if (!_client.isOpen()) {
                    _registeredSockets.remove(_client);
                }
            }
        }
        
        private void silentlyClose(SocketChannel sc) {
            try {
                sc.close();
            }
            catch (IOException ignore) {
            }
        }
        
        private final SocketChannel _client;
        private final SocketProcessor _processor;
        private final Selector _selector;
        private final Queue<SocketChannel> _returnQueue;
        private final Set<SocketChannel> _registeredSockets;
    }
    
    private int _port;
    private final ExecutorService _execPool;
    private static final Logger _logger = LogManager.getLogger(GenericProtocolServer.class);
}
