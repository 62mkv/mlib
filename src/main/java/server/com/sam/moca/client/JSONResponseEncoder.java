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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

import com.sam.moca.MocaResults;
import com.sam.moca.util.ResponseUtils;

/**
 * A class that encodes and writes a JSON response back to a requester.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author mlange
 * @version $Revision$
 */
class JSONResponseEncoder implements ResponseEncoder {
    
    public JSONResponseEncoder(HttpServletResponse response, String sessionId, boolean doCompression) {
        _response = response;
        _sessionId = sessionId;
        _doCompression = doCompression;
    }
    
    public void writeResponse(MocaResults res, String message, int status) throws IOException {
        
        
        // Set our content type and response headers.
        _response.setContentType("application/json; charset=UTF-8");
        _response.addHeader("Cache-Control", "no-cache");
        _response.addHeader("Command-Status", String.valueOf(status));
        _response.addHeader("Session-Id", _sessionId);
        if (message != null) {
            _response.addHeader("Message", ResponseUtils.encodeHeader(message));
        }
 
        if (res != null) {
            OutputStream out;
            if (_doCompression) {
                // Go with GZIP
                _response.setHeader("Content-Encoding", "gzip");
                out = new GZIPOutputStream(_response.getOutputStream());
            }
            else {
                // No compression
                out = _response.getOutputStream();
            }
     
            final Writer writer = new OutputStreamWriter(out, "UTF-8");
            try {
                JSONResultsEncoder.writeResults(res, new Writer() {
    
                    @Override
                    public void close() throws IOException {
                        writer.close();
                    }
    
                    @Override
                    public void flush() throws IOException {
                        // We don't want to flush as this is a big performance
                        // issue for https and we don't have to flush the underlying
                        // print writer stream over http.  We just need to make sure
                        // the characters are indeed written.
                    }
    
                    @Override
                    public void write(char[] cbuf, int off, int len)
                            throws IOException {
                        writer.write(cbuf, off, len);
                    }
                });
            }
            finally {
                writer.close();
            }
        }
    }

    private final HttpServletResponse _response;
    private final String _sessionId;
    private final boolean _doCompression;
}
