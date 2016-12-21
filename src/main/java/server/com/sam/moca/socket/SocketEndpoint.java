/*
 *  $URL$
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

package com.sam.moca.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * A socket communication endpoint.  An instance of this class is sent to SocketProcessor instances when 
 * handling an incoming socket request.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public final class SocketEndpoint {
    /**
     * Returns a textual representation of this socket.
     */
    public String getRemoteInfo() {
        return _socket.getInetAddress().getHostAddress();
    }
    
    /**
     * Returns the local port this socket is connected to.  In a typical server socket situation, this
     * is the listen port.
     * @return
     */
    public int getLocalPort() {
        return _socket.getLocalPort();
    }
    
    /**
     * Returns the remote port this socket is connected to.  In a typical server socket situation, this
     * is the client's port.
     * @return
     */
    public int getRemotePort() {
        return _socket.getPort();
    }

    /**
     * An <code>InputStream</code> for the incoming socket request.  The stream returned from this method
     * must not be closed by the caller.  To close the socket, use the <code>close</code> method.
     * 
     * @return an open <code>InputStream</code> for the socket.
     */
    public InputStream getInputStream() {
        return _in;
    }
    
    /**
     * An <code>OutputStream</code> for the socket request.  The stream returned from this method
     * must not be closed by the caller.  To close the socket, use the <code>close</code> method.
     * 
     * @return an open <code>OutputStream</code> for the socket.
     */
    public OutputStream getOutputStream() {
        return _out;
    }
    
    /**
     * Closes the socket.  After calling this method, any additional I/O on the input or output streams
     * will throw an <code>IOException</code>.
     */
    public void close() {
        try {
            _in.close();
            _out.close();
            _socket.close();
        }
        catch (IOException e) {
            // Ignore close errors
        }
    }
    
    @Override
    public String toString() {
        return getRemoteInfo();
    }

    //
    // Implementation
    //
    SocketEndpoint(Socket socket, InputStream in, OutputStream out) {
        _socket = socket;
        _in = in;
        _out = out;
    }
    
    private Socket _socket;
    private InputStream _in;
    private OutputStream _out;
}
