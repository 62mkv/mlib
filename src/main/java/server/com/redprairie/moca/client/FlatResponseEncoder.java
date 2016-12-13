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
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.redprairie.moca.MocaResults;

/**
 * A class that encodes and writes a "flat" response back to a requester.  The
 * flat response encoder is meant for use by clients that can't parse XML.  These
 * clients can instead use <code>sqlDecodeResults( )</code> to populate a result 
 * set from the encoded results.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author mlange
 * @version $Revision$
 */
class FlatResponseEncoder implements ResponseEncoder {
    
    public FlatResponseEncoder(HttpServletResponse response, String sessionId) {
        _response = response;
        _sessionId = sessionId;
    }
    
    public void writeResponse(MocaResults res, String message, int status) throws IOException {
        
        // Set our content type and response headers.
        _response.setContentType("application/moca; charset=UTF-8");
        _response.addHeader("Cache-Control", "no-cache");
        _response.addHeader("Command-Status", String.valueOf(status));
        _response.addHeader("Session-Id", _sessionId);

        // Write the encoded results.
        final ServletOutputStream stream = _response.getOutputStream();
        // We wrap the outputstream since we don't want flush to do anything
        // to the underlying ServletOutputStream as it is not needed and causes
        // issues with https.
        FlatResultsEncoder.writeResults(res, message, new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                stream.write(b);
            }
            
        }, "UTF-8");
    }
    
    private final HttpServletResponse _response;
    private final String _sessionId;
}
