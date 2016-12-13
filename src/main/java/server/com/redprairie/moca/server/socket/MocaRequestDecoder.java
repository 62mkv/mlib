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

package com.redprairie.moca.server.socket;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.socket.SocketChannel;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;
import org.jboss.netty.handler.timeout.ReadTimeoutException;

/**
 * A decoder for the MOCA incoming request.  
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class MocaRequestDecoder extends ReplayingDecoder<MocaProtocolState> {

    public MocaRequestDecoder(Charset encoding) {
        super(MocaProtocolState.INITIAL);
        _encoding = encoding;
    }
    
    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer, MocaProtocolState state)
            throws Exception {
        switch (state) {
        case INITIAL:
            byte b = buffer.readByte();
            // Version  header.
            if (b == 'V') {
                byte[] vdata = new byte[4];
                buffer.readBytes(vdata);
                if (vdata[0] != '1' || vdata[1] != '0' || vdata[3] != '^') {
                    throw new Exception("Invalid Version String: ");
                }
                version = vdata[2] - '0';
            }
            else {
                buffer.discardReadBytes();
                version = 0;
            }
            checkpoint(MocaProtocolState.READ_VERSION);
        case READ_VERSION:
            totalSize = readIntField(buffer);
            checkpoint(MocaProtocolState.READ_SIZE);
        case READ_SIZE:
            // Here we diverge -- read the header portion for v104.  Otherwise, read the whole thing.
            if (version == 4) {
                readIntField(buffer); // Header size -- not used.
                encryptionMethod = readStringField(buffer);
                readStringField(buffer); // Encryption data -- not used.
                int streamSize = readIntField(buffer);
                data = new byte[streamSize];
            }
            else {
                data = new byte[totalSize];
            }
            
            buffer.readBytes(data);
            checkpoint(MocaProtocolState.INITIAL);
        }

        String host = "unknown";
        int port = 0;
        if (channel instanceof SocketChannel) {
            InetSocketAddress remote = ((SocketChannel)channel).getRemoteAddress();
            host = remote.getAddress().getHostAddress();
            port = remote.getPort();
        }
    
        return new MocaRequest(version, encryptionMethod, data, _encoding, host, port);
    }
    
    private int readIntField(ChannelBuffer buffer) {
        return Integer.parseInt(readStringField(buffer));
    }
    
    private String readStringField(ChannelBuffer buffer) {
        return new String(readNextField(buffer), _encoding);
    }
    
    private byte[] readNextField(ChannelBuffer buffer) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int count = 0; buffer.readable() && count < 1000; count++) {
            byte b = buffer.readByte();
            if (b == '^') {
                return out.toByteArray();
            }
            else {
                out.write(b);
            }
        }
        
        throw new RuntimeException("No Field Delimiter");
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        if (e.getCause() instanceof ReadTimeoutException) {
            log.debug("Socket Read Timeout");
        }
        else {
            log.debug("Socket exception: " + e);
        }
        
        e.getChannel().close();
    }

    private final Charset _encoding;
    
    private int version;
    private int totalSize;
    private String encryptionMethod = null;
    private byte[] data;
    private final static Logger log = LogManager.getLogger(MocaRequestDecoder.class);
}