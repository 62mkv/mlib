/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

package com.redprairie.moca.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.exceptions.ReadResponseException;
import com.redprairie.moca.util.DaemonThreadFactory;

/**
 * Java implementation of a MOCA client connection.  All socket I/O is done via
 * Java APIs, and no native libraries rre required.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class HttpConnection implements MocaConnection {
    
    /**
     * Open a connection to the MOCA service running on the given host and
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
    public HttpConnection(String url, Map<String, String> env, 
            String responseType) throws MocaException {
        if (url == null) {
            throw new IllegalArgumentException("URL cannot be null");
        }
                
        _env = new LinkedHashMap<String, String>();
        if (env != null) {
            _env.putAll(env);
        }
        _envString = XMLRequestEncoder.buildXMLEnvironmentString(_env);
        
        _baseURL = url;
        try {
            _baseURI = new URI(url);
        }
        catch (URISyntaxException e) {
            throw new ConnectionFailedException("Could not connect to server: " + e, e);
        }
        _url = url;
        _responseProtocol = responseType;
        _cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

        _pool = Executors.newCachedThreadPool(new DaemonThreadFactory("HttpConnection"));

        // Hit the server to see if it's up.  This will also populate our server key
        String serverKey = "";
        try {
            MocaResults res = executeCommandWithContext("get encryption information", null, null);
            if (res.next()) {
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
        catch (MocaException e) {
            _pool.shutdownNow();
            throw e;
        }
        _serverKey = serverKey;
    }

   /**
    * Open a connection to the MOCA service running on the given host and
    * port.  No client-side context (environment) will be passed along.
    *  
    * @param url the base URL to use to communicate with MOCA.
    * @throws MocaException if an error occurs connecting to the remote
    * host.
    */
    public HttpConnection(String url)
            throws MocaException {
        this(url, null);
    }
    
    public HttpConnection(String url, Map<String, String> env) 
            throws MocaException {
        this(url, env, "Json");
    }

    // @see com.redprairie.moca.client.MocaConnection#close()
    public void close() {
        _dispose();
    }
    
    @Override
    public String getServerKey() {
        return _serverKey;
    }
    

    
    /**
     * This method is the same as a close but also propagates the MocaException
     * if there was one.
     * @throws MocaException
     */
    void disconnect() throws MocaException {
        if (_sessionID != null) {
            _sendCommand(null, null, null);
        }
        _pool.shutdownNow();
    }

    // @see com.redprairie.moca.client.MocaConnection#executeCommand(java.lang.String)
    public MocaResults executeCommand(String command) throws MocaException {
        return executeCommandWithContext(command, null, null);
    }
    
    // @see com.redprairie.moca.client.MocaConnection#executeCommandWithArgs(java.lang.String, com.redprairie.moca.MocaArgument[])
    @Override
    public MocaResults executeCommandWithArgs(String command, 
            MocaArgument... args) throws MocaException {
        return executeCommandWithContext(command, args, null);
    }
    
    // @see com.redprairie.moca.client.MocaConnection#executeCommandWithContext(java.lang.String, com.redprairie.moca.MocaArgument[], com.redprairie.moca.MocaArgument[])
    @Override
    public MocaResults executeCommandWithContext(String command, 
            MocaArgument[] args, MocaArgument[] commandArgs) 
            throws MocaException {

        if (command == null) {
            throw new IllegalArgumentException("command cannot be null");
        }
                
        return _execute(command, args, commandArgs);
    }
    
 
    
    // @see com.redprairie.moca.client.MocaConnection#setAutoCommit(boolean)
    public void setAutoCommit(boolean autoCommit) {
        _autoCommit = autoCommit;
    }

    // @see com.redprairie.moca.client.MocaConnection#isAutoCommit()
    public boolean isAutoCommit() {
        return _autoCommit;
    }
    
    @Override
    public void setRemote(boolean remote) {
        _remote = remote;
    }
    
    @Override
    public boolean isRemote() {
        return _remote;
    }
    
    // @see com.redprairie.moca.client.MocaConnection#getEnvironment()
    @Override
    synchronized
    public Map<String, String> getEnvironment() {
        //Copy the environment so we can't directly make changes
        return new LinkedHashMap<String, String>(_env);
    }
    
    // @see com.redprairie.moca.client.MocaConnection#setEnvironment(java.util.Map)
    synchronized
    public void setEnvironment(Map<String, String> env) {
        _env.clear();
        if (env != null) {
            _env.putAll(env);
        }
        _envString = XMLRequestEncoder.buildXMLEnvironmentString(_env);
    }
    
    // @see com.redprairie.moca.client.MocaConnection#setTimeout(long)
    
    @Override
    public void setTimeout(long ms) {
        _timeout = ms;
    }
    
    @Override
    synchronized
    public String toString() {
        return _url;
    }
    
   // @see java.lang.Object#finalize()
    
    protected void finalize() throws Throwable {
        super.finalize();
        _dispose();
    }
    
    //
    // Implementation
    //
    
    private class GetResultsCallable implements Callable<MocaResults> {

        public GetResultsCallable(HttpURLConnection conn, String command,
                MocaArgument[] context, MocaArgument[] args) {
            this.command = command;
            this.conn = conn;
            this.args = args;
            this.context = context;
        }

        @Override
        public MocaResults call() throws Exception {
            Writer requestWriter = null;
            try {
                OutputStream formStream = conn.getOutputStream();
                requestWriter = new OutputStreamWriter(formStream, "UTF-8");
                String envString;
                // We want to make sure updates to environment are seen.
                synchronized(HttpConnection.this) {
                    envString = _envString;
                }
                XMLRequestEncoder.encodeRequest(command, _sessionID,
                        envString, _autoCommit, _remote, context, args, requestWriter);

                requestWriter.flush();

                int httpStatus = conn.getResponseCode();

                if (httpStatus != 200) {
                    throw new ProtocolException("Unexpected HTTP status: "
                            + httpStatus);
                }

                _cookies.put(_baseURI, conn.getHeaderFields());
                
                InputStream resultStream = conn.getInputStream();
                ResponseDecoder decoder = 
                    ResponseDecoderFactory.getResponseDecoder(conn);

                try {
                    MocaResults results = decoder.decodeResponse();
                    return results;
                }
                finally {
                    synchronized (HttpConnection.this) {
                        // Save Session ID, no matter what the execution status
                        // of the command.
                        String sessionID = decoder.getSessionId();
                        if (sessionID == null || sessionID.isEmpty()) {
                            _sessionID = null;
                            _url = _baseURL;
                        } 
                        else if (!sessionID.equals(_sessionID)) {
                            _sessionID = sessionID;
                            _url = _baseURL + "?msession=" + sessionID;
                        }
                    }

                    // Close the result stream.
                    try {
                        resultStream.close();
                    }
                    catch (IOException ignore) {
                        // Ignore
                    }
                }
            }
            catch (InterruptedIOException e) {
                throw new MocaInterruptedException(e);
            }
            catch (IOException e) {
                throw new ReadResponseException(e);
            }
            finally {
                // Close the input stream.
                try {
                    if (requestWriter != null) requestWriter.close();
                }
                catch (IOException ignore) {
                    // Ignore
                }
            }
        }

        private final String command;
        private final HttpURLConnection conn;
        private final MocaArgument[] context;
        private final MocaArgument[] args;
    }
    
    private MocaResults _sendCommand(String command, MocaArgument[] context, 
            MocaArgument[] args) throws MocaException {

        HttpURLConnection conn;
        
        try {
            String url;
            // We want to make sure updates to url are seen.
            synchronized (this) {
                url = _url;
            }
            URL requestURL = new URL(url);
            
            conn = (HttpURLConnection) requestURL.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.addRequestProperty("content-type", "application/moca-xml");
            conn.addRequestProperty("Response-Encoder", _responseProtocol);
            conn.addRequestProperty("Accept-Encoding", "gzip");
            
            Map<String, List<String>> cookiesToSet = _cookies.get(_baseURI, conn.getRequestProperties());
            if (cookiesToSet != null) {
                for(Map.Entry<String,List<String>> cookie : cookiesToSet.entrySet()) {
                    for (String value : cookie.getValue()) {
                        conn.addRequestProperty(cookie.getKey(), value);
                    }
                }
            }
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            throw new ConnectionFailedException("Could not connect to server: " + e, e);
        }            
        
        // Spawn a thread to get results from the server.  If, while waiting for that request to complete,
        Future<MocaResults> future = null;
        try {
            future = _pool.submit(new GetResultsCallable(conn, command, context, args));
            if (_timeout > 0L) {
                return future.get(_timeout, TimeUnit.MILLISECONDS);
            }
            else {
                return future.get();
            }
        }
        catch (InterruptedException e) {
            // We want to cancel the thread request
            if (future != null) {
                future.cancel(true);
            }
            
            // Also disconnect the http connection.
            conn.disconnect();
            
            throw new MocaInterruptedException(e);
        }
        catch (TimeoutException e) {
            // We want to cancel the thread request
            if (future != null) {
                future.cancel(true);
            }
            
            // Also disconnect the http connection.
            conn.disconnect();
            
            throw new ConnectionTimeoutException("No response from server", e);
        }
        catch (ExecutionException e) {
            Throwable original = e.getCause();
            if (original instanceof MocaException) {
                throw (MocaException) original;
            }
            else {
                throw new ReadResponseException(e.getCause());
            }
        }
    }

    private MocaResults _execute(String command, MocaArgument[] args, MocaArgument[] commandArgs) throws MocaException {
        try {
            return _sendCommand(command, args, commandArgs);
        }
        catch (MocaInterruptedException e) {
            // We want our interrupt exception to propagate up all the way
            throw e;
        }
        catch (RuntimeException e) {
            throw new ReadResponseException(e);
        }
    }
    
    private void _dispose() {
        try {
            disconnect();
        }
        catch (MocaException e) {
            // Ignore
        }
    }
 
    private final String _responseProtocol;
    private boolean _autoCommit = true;
    private boolean _remote = false;
    private String _baseURL;
    private URI _baseURI;
    private String _url;
    private String _sessionID;
    private CookieManager _cookies;
    private long _timeout = 0L;
    private final Map<String, String> _env;
    private String _envString;
    private final String _serverKey;
    private ExecutorService _pool;
}
