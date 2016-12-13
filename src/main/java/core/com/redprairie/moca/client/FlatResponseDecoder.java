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

package com.redprairie.moca.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;

/**
 * A class that decodes a "flat" response back from the server.  The
 * flat response decoder is meant for use by clients that can't parse XML.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author mlange
 * @version $Revision$
 */
class FlatResponseDecoder implements ResponseDecoder {
    
    public FlatResponseDecoder(URLConnection conn, String encoding) throws IOException {
        _stream = conn.getInputStream();
        _sessionId = conn.getHeaderField("Session-Id");
        _status = Integer.parseInt(conn.getHeaderField("Command-Status"));
        _encoding = encoding;
    }
    
    // @see com.redprairie.moca.client.ResponseDecoder#decodeResponse()
    @Override
    public MocaResults decodeResponse() throws MocaException, ProtocolException {
        try {
            FlatResultsDecoder resDecoder = new FlatResultsDecoder(
                    _stream, _encoding);
            if (_status != 0) {
                throw resDecoder.decodeError(_status);
            }
            return resDecoder.decode();
        }
        catch (IOException e) {
            throw new ProtocolException("Error Interpreting Flat Response", e);
        }
    }

    // @see com.redprairie.moca.client.ResponseDecoder#getSessionId()
    @Override
    public String getSessionId() {
        return _sessionId;
    }
    
    private final String _encoding;
    private final int _status;
    private final String _sessionId;
    private final InputStream _stream;
}
