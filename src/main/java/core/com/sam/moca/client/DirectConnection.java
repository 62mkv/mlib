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

package com.sam.moca.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sam.moca.MocaArgument;
import com.sam.moca.MocaException;
import com.sam.moca.MocaInterruptedException;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.NotFoundException;
import com.sam.moca.SimpleResults;
import com.sam.moca.client.crypt.BlowfishEncryptionStrategy;
import com.sam.moca.client.crypt.EncryptionStrategy;
import com.sam.moca.client.crypt.NullEncryptionStrategy;
import com.sam.moca.client.crypt.RPBFEncryptionStrategy;
import com.sam.moca.exceptions.ReadResponseException;

/**
 * Java implementation of a MOCA client connection.  All socket I/O is done via
 * Java APIs, and no native libraries rre required.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class DirectConnection implements MocaConnection {
    
    /**
     * Open a connetion to the MOCA service running on the given host and
     * port.  The optional Environment structure represents a set of name/value
     * pairs that represent client-side context.
     *  
     * @param host the host to connect to.
     * @param port the port to connect to.
     * @param env a mapping of names to values that represents the MOCA
     * environment.  The passed variables are available to any called
     * server-side components.  If this argument is passed as null, no
     * environment entries will be passed.
     * @param charset the character set to use for client->server communications.
     * If this parameter is <code>null</code>, use the JVM default character set. 
     * @throws MocaException if an error occurs connecting to the remote
     * host.
     */
    public DirectConnection(String host, int port, Map<String, String> env, String charset)
           throws MocaException {
        if (host == null) {
            throw new IllegalArgumentException("Host cannot be null");
        }
        
        _env = new LinkedHashMap<String, String>();
        if (env != null) {
            _env.putAll(env);
        }
        _envString = ConnectionUtils.buildEnvironmentString(_env);
        _host = host;
        _port = port;

        _connect();
        
        String encryptionType = null;
        _encryption = new NullEncryptionStrategy();
        String serverKey = "";
        try {
            MocaResults res = this.executeCommand("get encryption information");
            if (res.next()) {
                encryptionType = res.getString("name");
                
                // If no explicit character set was requested, use the server's
                // character set.
                if (charset == null && res.containsColumn("charset")) {
                    charset = res.getString("charset");
                }
                
                if (res.containsColumn("server_key")) {
                    serverKey = res.getString("server_key");
                }
                
                // If no explicit character set was requested, use the server's
                // character set.
                if (serverKey == null) {
                    serverKey = "";
                }
            }
        }
        catch (MocaException ignore) {}
        
        _encryption = _getEncryptionStrategy(encryptionType);
        _charset = charset;
        _serverKey = serverKey;
    }

   /**
    * Open a connetion to the MOCA service running on the given host and
    * port.  No client-side context (environment) will be passed along.
    *  
    * @param host the host to connect to.
    * @param port the port to connect to.
    * @throws MocaException if an error occurs connecting to the remote
    * host.
    */
    public DirectConnection(String host, int port)
            throws MocaException {
        this(host, port, null);
    }
    /**
     * Open a connetion to the MOCA service running on the given host and
     * port.  The optional Environment structure represents a set of name/value
     * pairs that represent client-side context.
     *  
     * @param host the host to connect to.
     * @param port the port to connect to.
     * @param env a mapping of names to values that represents the MOCA
     * environment.  The passed variables are available to any called
     * server-side components.  If this argument is passed as null, no
     * environment entries will be passed. 
     * @throws MocaException if an error occurs connecting to the remote
     * host.
     */
    public DirectConnection(String host, int port, Map<String, String> env)
           throws MocaException {
        this(host, port, env, null);
    }

    // @see com.sam.moca.client.MocaConnection#close()
    public void close() {
        _dispose();
    }

    // @see com.sam.moca.client.MocaConnection#executeCommand(java.lang.String)
    public MocaResults executeCommand(String command) throws MocaException {
        return _execute(command, null, null);
    }
    
    // @see com.sam.moca.client.MocaConnection#executeCommandWithArgs(java.lang.String, com.sam.moca.MocaArgument[])
    @Override
    public MocaResults executeCommandWithArgs(String command, MocaArgument... args) throws MocaException {
        return _execute(command, args, null);
    }

    // @see com.sam.moca.client.MocaConnection#executeCommandWithContext(java.lang.String, com.sam.moca.MocaArgument[], com.sam.moca.MocaArgument[])
    @Override
    public MocaResults executeCommandWithContext(String command, MocaArgument[] args, MocaArgument[] commandArgs)
            throws MocaException {
        return _execute(command, args, commandArgs);
    }

    // @see com.sam.moca.client.MocaConnection#setAutoCommit(boolean)
    public void setAutoCommit(boolean autoCommit) {
        if (!autoCommit) {
            _flags |= FLAG_NOCOMMIT;
        }
        else {
            _flags &= ~FLAG_NOCOMMIT;
        }
    }

    // @see com.sam.moca.client.MocaConnection#isAutoCommit()
    public boolean isAutoCommit() {
        return ((_flags & FLAG_NOCOMMIT) == 0);
    }
    
    // @see com.sam.moca.client.MocaConnection#setAutoCommit(boolean)
    public void setRemote(boolean remote) {
        if (remote) {
            _flags |= FLAG_REMOTE;
        }
        else {
            _flags &= ~FLAG_REMOTE;
        }
    }

    // @see com.sam.moca.client.MocaConnection#isAutoCommit()
    public boolean isRemote() {
        return ((_flags & FLAG_REMOTE) == 0);
    }
    
    // @see com.sam.moca.client.MocaConnection#setEnvironment(java.util.Map)
    
    public void setEnvironment(Map<String, String> env) {
        _env.clear();
        if (env != null) {
            _env.putAll(env);
        }
        _envString = ConnectionUtils.buildEnvironmentString(_env);
    }
    
    // @see com.sam.moca.client.MocaConnection#getEnvironment()
    public Map<String, String> getEnvironment() {
        //Copy the environment so we can't directly make changes
        return new LinkedHashMap<String, String>(_env);
    }
    
    @Override
    public void setTimeout(long ms) {
        if (ms < 0) {
            throw new IllegalArgumentException("timeout must be >= 0");
        }
        
        _timeout = ms;
    }
    
    @Override
    public String toString() {
        return _host + ":" + _port;
    }
    
    @Override
    public String getServerKey() {
        return _serverKey;
    }
    
    // @see java.lang.Object#finalize()
    
    protected void finalize() throws Throwable {
        super.finalize();
        _dispose();
    }
    
    //
    // Implementation
    //
    private static final int FLAG_NOCOMMIT      = 0x01;
    @SuppressWarnings("unused")
    private static final int FLAG_ASCII_COMM    = 0x02;
    @SuppressWarnings("unused")
    private static final int FLAG_LICENSE_CHECK = 0x04;
    @SuppressWarnings("unused")
    private static final int FLAG_KEEPALIVE     = 0x08;
    private static final int FLAG_REMOTE        = 0x10;
    
    private EncryptionStrategy _getEncryptionStrategy(String name) {
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
    
    private int _intLength(int value) {
        int length;
        int temp;

        // Add one right away if the number is a negative.
        length = (value <= 0) ? 1 : 0;

        // Add up the digits in the given number.
        for (temp = value; temp != 0; temp /= 10, length++)
            ;

        return length;
    }
    
    private void _sendCommand(String command, MocaArgument[] context, MocaArgument[] args) throws IOException, MocaException {
        _checkForServerClose();
        String encryptionMethod = _encryption.getName();
        String encryptionInfo = "";
        String service = "";
        String schema = "";
        String traceEncoding = "~0~~0~~-1";
        
        StringBuilder environmentEncoding = new StringBuilder(_envString);
        environmentEncoding.append(":__CONTEXT__=");
        NormalizedContextEncoder ctxEncoder = new NormalizedContextEncoder(_charset);
        ctxEncoder.encodeContext(context, environmentEncoding);
        environmentEncoding.append('|');
        ctxEncoder.encodeContext(args, environmentEncoding);
        
        byte[] commandBytes = _getBytes(command);
        
        int flags = ((commandBytes.length % 18) << 8) | (_flags & 0xff);

        String versionSeg = "V104^";
        String encryptionSeg = encryptionMethod + "^" + encryptionInfo + "^";
        String fiddlyBitsSeg = service + "^" + schema + "^" + traceEncoding + "^" + environmentEncoding + "^" + flags + "^";
        
        byte[] versionSegBytes = _getBytes(versionSeg);
        byte[] encryptionSegBytes = _getBytes(encryptionSeg);
        byte[] fiddlyBitsSegBytes = _getBytes(fiddlyBitsSeg);
        
        byte[] restBytes = new byte[fiddlyBitsSegBytes.length + commandBytes.length];
        System.arraycopy(fiddlyBitsSegBytes, 0, restBytes, 0, fiddlyBitsSegBytes.length);
        System.arraycopy(commandBytes, 0, restBytes, fiddlyBitsSegBytes.length, commandBytes.length);
        restBytes = _encryption.encrypt(restBytes);
        
        int headerLength = encryptionSegBytes.length;
        int restLength = restBytes.length;
        int totalLength = _intLength(headerLength) + 1 + headerLength + _intLength(restLength) + restLength;
        
        _out.write(versionSegBytes);
        _out.write(_getBytes(String.valueOf(totalLength)));
        _out.write('^');
        _out.write(_getBytes(String.valueOf(headerLength)));
        _out.write('^');
        _out.write(encryptionSegBytes);
        _out.write(_getBytes(String.valueOf(restLength)));
        _out.write('^');
        _out.write(restBytes);
        _out.flush();
    }
    
    private String _nextField() throws IOException {
        ByteArrayOutputStream str = new ByteArrayOutputStream();
        try {
            while (true) {
                int c = _in.read();
                if (c == -1 || c == '^') {
                    break;
                }
                str.write(c);
            }
    
            String field;
            if (_charset == null) {
                field = str.toString( Charset.defaultCharset().name());
            }
            else {
                field = str.toString(_charset);
            }
            return field;
        }
        finally {
            str.close();
        }
    }
    
    private byte[] _readBytes(int length) throws IOException {
        byte[] tmp = new byte[length];
        int total = 0;
        while (total < length) {
            int nread = _in.read(tmp, total, length - total);
            if (nread == -1) {
                break;
            }
            total += nread;
        }

        return tmp;
    }

    private String _readBytesAsString(int length) throws IOException {
        byte[] data = _readBytes(length);
        
        if (_charset == null) {
            return new String(data, Charset.defaultCharset().name());
        }
        else {
            return new String(data, _charset);
        }
    }
    
    private byte[] _getBytes(String s) {
        if (_charset == null) {
            return s.getBytes(Charset.defaultCharset());
        }
        else {
            try {
                return s.getBytes(_charset);
            }
            catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException("Character Set Error:" + e);
            }
        }
    }
    
    private void _skipBytes(int length) throws IOException {
        _readBytes(length);
    }
    
    private SimpleResults _readResponse() throws IOException, MocaException {
        if (_timeout > 0) {
            _sc.configureBlocking(false);
            Selector selector = Selector.open();
            try {
                _sc.register(selector, SelectionKey.OP_READ);
                int keys = selector.select(_timeout);
                if (keys == 0 && _timeout != 0) {
                    throw new ConnectionTimeoutException("no response from server");
                }
            }
            finally {
                selector.close();
                _sc.configureBlocking(true);
            }
        }
        
        // This would contain the version info, but we just ignore it.
        _nextField();
        int headerLength = Integer.parseInt(_nextField());
        InputStream tmp = _in;
        _in = _encryption.getInputWrapper(_in, headerLength, EncryptionStrategy.DECRYPT_MODE);
        // This would return the command count, but we just ignore it.
        Integer.parseInt(_nextField());
        String statusText = _nextField();
        int statusCode = Integer.parseInt(statusText);
        int messageLength = Integer.parseInt(_nextField());
        String message = _readBytesAsString(messageLength);
        _skipBytes(1);
        int rowCount = Integer.parseInt(_nextField());
        int columnCount = Integer.parseInt(_nextField());
        String dataTypes = _nextField();
        String columnMetadata = _nextField();
        
        SimpleResults results = new SimpleResults();
        
        String[] metadataFields = columnMetadata.split("~", -1);
        
        for (int c = 0, m = 1; c < columnCount; c++) {
            char typeCode = dataTypes.charAt(c);
            MocaType type = MocaType.lookup(Character.toUpperCase(typeCode));
            boolean isNullable = Character.isLowerCase(typeCode);
            String name = metadataFields[m++];
            int definedMaxLength = Integer.parseInt(metadataFields[m++]);
            // This is the actual max length position, no longer used
            Integer.parseInt(metadataFields[m++]);
            // This is the short description position, no longer used
            m++;
            // this is the long description position, no longer used
            m++;
            
            results.addColumn(name, type, definedMaxLength, isNullable);
        }

        _in = tmp;
        
        for (int r = 0; r < rowCount; r++) {
            int rowLength = Integer.parseInt(_nextField());
            tmp = _in;
            _in = _encryption.getInputWrapper(_in, rowLength, EncryptionStrategy.DECRYPT_MODE);
            results.addRow();
            for (int c = 0; c < columnCount; c++) {
                char typeCode = (char)_in.read();
                MocaType type = MocaType.lookup(typeCode);
                int dataLength = Integer.parseInt(_nextField());
                
                if (dataLength == 0) {
                    results.setNull(c);
                }
                
                else {
                    if (type.equals(MocaType.BINARY)) {
                        // The first 8 bytes are encoding the length of the data.
                        // We've already got that, so there's really no need to
                        // interpret it.
                        _skipBytes(8);
                        byte[] data = _readBytes(dataLength - 8);
                        results.setBinaryValue(c, data);
                    }
                    else if (type.equals(MocaType.RESULTS)) {
                        byte[] data = _readBytes(dataLength);
                        InputStream dataStream = new ByteArrayInputStream(data);
                        MocaResults sub = new FlatResultsDecoder(dataStream, _charset).decode();
                        results.setResultsValue(c, sub);
                    }
                    else {
                        String data = _readBytesAsString(dataLength);
                        if (type.equals(MocaType.STRING) || type.equals(MocaType.STRING_REF)) {
                            results.setStringValue(c, data);
                        }
                        else if (type.equals(MocaType.INTEGER) || type.equals(MocaType.INTEGER_REF)) {
                            try {
                                results.setIntValue(c, Integer.parseInt(data));
                            }
                            catch (NumberFormatException e) {
                                throw new ProtocolException("error parsing integer: " + data, e);
                            }
                        }
                        else if (type.equals(MocaType.DOUBLE) || type.equals(MocaType.DOUBLE_REF)) {
                            try {
                                results.setDoubleValue(c, Double.parseDouble(data));
                            }
                            catch (NumberFormatException e) {
                                throw new ProtocolException("error parsing double: " + data, e);
                            }
                        }
                        else if (type.equals(MocaType.BOOLEAN)) {
                            results.setBooleanValue(c, !data.equals("0"));
                        }
                        else if (type.equals(MocaType.DATETIME)) {
                            try {
                                DateFormat fmt = (DateFormat) _dateFormatter.clone();
                                results.setDateValue(c, fmt.parse(data));
                            }
                            catch (ParseException e) {
                                throw new ProtocolException("error parsing date: " + data, e);
                            }
                        }
                        else {
                            // TODO deal with unknown data types
                        }
                    }
                }
            }        
            _in = tmp;
        }
        
        if (statusCode != 0) {
            if (statusCode == NotFoundException.DB_CODE || statusCode == NotFoundException.SERVER_CODE) {
                throw new NotFoundException(statusCode, results);
            }
            else {
                throw new ServerExecutionException(statusCode, message, results);
            }
        }
        
        return results;  
    }
    
    private SimpleResults _execute(String command, MocaArgument[] context, MocaArgument[] args) throws MocaException {
        try {
            try {
                _sendCommand(command, context, args);
            }
            catch (InterruptedIOException e) {
                throw new MocaInterruptedException(e);
            }
            catch (ClosedByInterruptException e) {
                throw new MocaInterruptedException(e);
            }
            catch (IOException e) {
                _reconnect();
                try {
                    _sendCommand(command, context, args);
                }
                catch (InterruptedIOException e2) {
                    throw new MocaInterruptedException(e2);
                }
                catch (ClosedByInterruptException e2) {
                    throw new MocaInterruptedException(e);
                }
                catch (IOException e2) {
                    throw new SendRequestException(e);
                }
            }
    
            try {
                return _readResponse();
            }
            catch (InterruptedIOException e) {
                throw new MocaInterruptedException(e);
            }
            catch (ClosedByInterruptException e) {
                throw new MocaInterruptedException(e);
            }
            catch (IOException e) {
                throw new ReadResponseException(e);
            }
            catch (NumberFormatException e) {
                throw new ReadResponseException(e);
            }
            catch (RuntimeException e) {
                throw new ReadResponseException(e);
            }
        }
        catch (MocaInterruptedException e) {
            // If we were interrupted make sure to close out the sockets.
            _dispose();
            throw e;
        }
    }
    
    private void _dispose() {
        try {
            if (_s != null) {
                _s.close();
            }
            if (_sc != null) {
                _sc.close();
            }
        }
        catch (IOException e) {
            // ignore
        }
    }
    
    private void _connect() throws MocaException {
        try {
            InetSocketAddress addr = new InetSocketAddress(_host, _port);
            if (addr.isUnresolved()) {
                throw new ConnectionFailedException("Unable to resolve address: " + _host);
            }
            _sc = SocketChannel.open(addr);
            _s = _sc.socket();
            _s.setTcpNoDelay(true);
            _out = new BufferedOutputStream(_s.getOutputStream());
            _in = new BufferedInputStream(_s.getInputStream());
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            throw new ConnectionFailedException("unable to connect", e);
        }
    }
    
    private void _reconnect() throws MocaException {
        try {
            _out.close();
            _in.close();
            _s.close();
            _sc.close();
        }
        catch (IOException e) {
            throw new ConnectionFailedException("unable to reestablish socket connection", e);
        }
        _connect();
    }
    
    private void _checkForServerClose() throws IOException {
        _sc.configureBlocking(false);
        try {
            int avail = _sc.read(ByteBuffer.allocate(1));
            if (avail != 0) throw new IOException("Unexpected data");
        }
        finally {
            _sc.configureBlocking(true);
        }
    }
    
    private int _flags = 0;
    private EncryptionStrategy _encryption;
    private String _host;
    private int _port;
    private final Map<String, String> _env;
    private String _envString;
    private Socket _s;
    private SocketChannel _sc;
    private OutputStream _out;
    private InputStream _in;
    private long _timeout = 0L;
    private SimpleDateFormat _dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
    private final String _charset;
    private final String _serverKey;
}
