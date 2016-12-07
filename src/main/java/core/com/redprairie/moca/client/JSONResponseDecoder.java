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

package com.redprairie.moca.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import org.codehaus.jackson.JsonParseException;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.util.ResponseUtils;

/**
 * A class that decodes a JSON response back from the server.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
class JSONResponseDecoder implements ResponseDecoder {
    public JSONResponseDecoder(URLConnection conn, String encoding) 
            throws JsonParseException, IOException {
        _sessionId = conn.getHeaderField("Session-Id");
        _status = Integer.parseInt(conn.getHeaderField("Command-Status"));
        _conn = conn;
        _encoding = encoding;
        String contentEncoding = conn.getContentEncoding();
        _useGzip = (contentEncoding != null && contentEncoding.equals("gzip"));
    }
    
    // @see com.redprairie.moca.client.ResponseDecoder#decodeResults()
    synchronized
    public MocaResults decodeResponse() throws MocaException, ProtocolException {
        if (!initialized) {
            try {
                Reader reader;
                InputStream in = _conn.getInputStream();
                if (_useGzip) {
                    in = new GZIPInputStream(in);
                }
                
                if (_encoding == null) {
                    reader = new InputStreamReader(in, "UTF-8");
                }
                else {
                    reader = new InputStreamReader(in, _encoding);
                }
                _res = JSONResultsDecoder.decode(reader);
            }
            catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException("Invalid Encoding: " + e); 
            }
            catch (IOException e) {
                throw new ProtocolException("Error Interpreting Json Response", e);
            }
            initialized = true;
        }
        
        if (_status != 0) {
            String message = ResponseUtils.decodeHeader(_conn.getHeaderField("Message"));
            if (_status == NotFoundException.DB_CODE || 
                    _status == NotFoundException.SERVER_CODE) {
                throw new NotFoundException(_status, _res);
            }
            else {
                throw new ServerExecutionException(_status, message, _res);
            }
        }
        
        return _res;
    }
    
    // @see com.redprairie.moca.client.ResponseDecoder#getSessionId()
    
    public String getSessionId() {
        return _sessionId;
    }

    private MocaResults _res = null;
    private boolean initialized = false;
    private final URLConnection _conn;
    private final int _status;
    private final String _sessionId;
    private final String _encoding;
    private final boolean _useGzip;
}
