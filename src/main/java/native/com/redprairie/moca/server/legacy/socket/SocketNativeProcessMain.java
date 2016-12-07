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

package com.redprairie.moca.server.legacy.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import com.redprairie.moca.server.legacy.InternalNativeProcess;
import com.redprairie.moca.server.legacy.MocaServerAdapter;
import com.redprairie.moca.server.legacy.RemoteNativeProcess;

/**
 * Main class for socket-based NativeProcess implementation.
 * 
 * This program expects to receive a port number on its command line.  That port number will be used to
 * communicate with the main server, which is assumed to be listening on that port.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class SocketNativeProcessMain {
    public static void main(String[] args) throws Exception {
        String port = args[0];
        String portCrash = args[1];
        String processID = args[2];
        
        InetAddress localhost = InetAddress.getByAddress(new byte[] {127, 0, 0, 1});
        
        SocketChannel channelCrash = SocketChannel.open(new InetSocketAddress(
            localhost, Integer.parseInt(portCrash)));
        Socket connCrash = channelCrash.socket();
        connCrash.setTcpNoDelay(true);
        
        // We write out the crash id so server can know it
        Writer writer = new OutputStreamWriter(connCrash.getOutputStream(), "UTF-8");
        writer.write(processID);
        writer.write('\n');
        writer.flush();
        
        final InputStream istream = connCrash.getInputStream();
        
        Thread thread = new Thread() {

            // @see java.lang.Thread#run()
            @Override
            public void run() {
                try {
                    // If anything is sent back then we just
                    // exit, or if there is an issue
                    istream.read();
                }
                catch (IOException e) {

                }
                System.out.println("Native process shutdown via crash socket.");
                System.exit(0);
            }

        };
        
        thread.setName("MocaServerSocketCrashWatcher");
        thread.setDaemon(true);
        thread.start();
        
        _libraryAdapter = new InternalNativeProcess(processID);
        try {
            SocketChannel channel = SocketChannel.open(new InetSocketAddress(
                localhost, Integer.parseInt(port)));
            Socket conn = channel.socket();
            conn.setTcpNoDelay(true);
            
            // Set up in and out streams.
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(conn.getOutputStream()));
            out.flush();
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(conn.getInputStream()));
            
            out.writeUTF(processID);
            out.flush();
            
            // Create a server adapter instance that corresponds to the main server's connection.  Although we allow
            // for a specific MocaServerAdapter instance to be used, we always substitute a proxy that is
            // attached to our socket.
            
            // Create a handler for the back end of the proxy.
            CallbackHandler<RemoteNativeProcess> handler  = CallbackHandler.newHandler(out, in, RemoteNativeProcess.class, MocaServerAdapter.class, _libraryAdapter);
            
            // Now wait for requests.  This should go forever, until the process shuts down.
            handler.dispatchLoop();

            System.out.println("Native Process shutdown");
            System.exit(0);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    /**
     * This is a static reference to prevent this object from ever being garbage
     * collected to ensure these processes are always available as long as the
     * JVM is running.  If not then this object could be garbage collected if
     * the JVM optimized scope and saw that this value was out of scope. 
     */
    private static RemoteNativeProcess _libraryAdapter;
}
