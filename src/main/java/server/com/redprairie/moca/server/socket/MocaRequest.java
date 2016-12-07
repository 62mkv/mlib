/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.server.socket;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.client.ConnectionUtils;
import com.redprairie.moca.client.NormalizedContextDecoder;
import com.redprairie.moca.client.ProtocolException;
import com.redprairie.moca.client.crypt.BlowfishEncryptionStrategy;
import com.redprairie.moca.client.crypt.EncryptionStrategy;
import com.redprairie.moca.client.crypt.NullEncryptionStrategy;
import com.redprairie.moca.client.crypt.RPBFEncryptionStrategy;

/**
 * Encapsulates a MOCA request. The request consists of the command, any
 * arguments, as well as environment and encryption encoding that got sent along
 * with the request from the socket client.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class MocaRequest {
    

    public MocaRequest(int version, String encryptionType, byte[] data, Charset encoding, String host, int port)
            throws ProtocolException {
        _encryptionType = encryptionType;
        _encoding = encoding;
        _host = host;
        _port = port;
        parse(version, encryptionType, data);
    }
    
    /**
     * @return Returns the command.
     */
    public String getCommand() {
        return _command;
    }
    
    /**
     * @return Returns the env.
     */
    public Map<String, String> getEnv() {
        return _env;
    }
    
    public List<MocaArgument> getArgs() {
        return _args;
    }
    
    public List<MocaArgument> getContext() {
        return _context;
    }
    
    /**
     * @return Returns the flags.
     */
    public boolean isAutoCommit() {
        return (_flags & FLAG_NOCOMMIT) == 0;
    }
    
    public boolean isRemote() {
        return (_flags & FLAG_REMOTE) != 0;
    }
    
    public String getEncryptionType() {
        return _encryptionType;
    }
    
    public String getHost() {
        return _host;
    }
    
    public int getPort() {
        return _port;
    }
    
    //
    // Implementation
    //
    private void parse(int version, String encryptionMethod, byte[] data) throws ProtocolException {
        
        if (version == 4) {
            EncryptionStrategy crypt = getEncryptionStrategy(encryptionMethod);
            data = crypt.decrypt(data);
        }
            
        ByteBuffer in = ByteBuffer.wrap(data);
        readStringField(in, _encoding); // Service Name: not used
        
        if (version == 4) {
            readStringField(in, _encoding); // Schema Name: not used
        }
        
        readStringField(in, _encoding); // Trace Info: not used
        String encodedEnvironment = readStringField(in, _encoding);
        _env = ConnectionUtils.parseEnvironmentString(encodedEnvironment);
        String contextEnv = _env.remove("__CONTEXT__");
        if (contextEnv != null) {
            NormalizedContextDecoder decoder = new NormalizedContextDecoder(
                contextEnv, _encoding);
            decoder.decode();
            _args = decoder.getArgs();
            _context = decoder.getContext();
        }
        
        _flags = readIntField(in);
        _command = new String(data, in.position() + in.arrayOffset(), in.remaining(), _encoding);
    }
    
    public static EncryptionStrategy getEncryptionStrategy(String name) {
        if (name != null && name.equalsIgnoreCase("rpbf")) {
            return new RPBFEncryptionStrategy();
        }
        else if (name != null && name.equals("blowfish")) {
            return new BlowfishEncryptionStrategy();
        }
        else {
            return new NullEncryptionStrategy();
        }
    }
    
    private int readIntField(ByteBuffer buffer) {
        return Integer.parseInt(readStringField(buffer, _encoding));
    }
    
    private String readStringField(ByteBuffer buffer, Charset encoding) {
        if (buffer.hasArray()) {
            byte[] rawData = buffer.array();
            int offset = buffer.arrayOffset();
            int position = buffer.position();
            int limit = buffer.limit();
            for (int count = offset + position; count < limit; count++) {
                if (rawData[count] == '^') {
                    buffer.position(count - offset + 1);
                    return new String(rawData, offset + position, count - (offset + position), encoding);
                }
            }
        }
        else {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            while (buffer.hasRemaining()) {
                byte b = buffer.get();
                if (b == '^') {
                    return new String(out.toByteArray(), encoding);
                }
                else {
                    out.write(b);
                }
            }
        }
        
        throw new RuntimeException("No Field Delimiter");
    }
    
    private static final int FLAG_NOCOMMIT      = 0x01;
    private static final int FLAG_REMOTE        = 0x10;

    private final Charset _encoding;
    private String _command;
    private String _encryptionType;
    private Map<String, String> _env;
    private List<MocaArgument> _args;
    private List<MocaArgument> _context;
    private int _flags;
    private String _host;
    private int _port;
}