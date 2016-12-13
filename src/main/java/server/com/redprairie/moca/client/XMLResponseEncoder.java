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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

import com.redprairie.moca.MocaResults;

/**
 * A class that encodes and writes an XML-formatted response back to a requester.  The
 * XML response encoder is used by clients that can parse XML.  Clients that can't
 * parse XML should use the flat response encoder instead.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author mlange
 * @version $Revision$
 */
class XMLResponseEncoder implements ResponseEncoder {
    
    public XMLResponseEncoder(HttpServletResponse response, String sessionId, boolean doCompression) {
        _response = response;
        _sessionId = sessionId;
        _doCompression = doCompression;
    }
    
    public void writeResponse(MocaResults res, String message, int status) throws IOException {
        
        _response.setContentType("application/xml; charset=UTF-8");
        _response.addHeader("Cache-Control", "no-cache");

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
 
        Writer writer = new OutputStreamWriter(out, "UTF-8");
        try {
            
            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    
            // Currently, there is a bug with the Sun implementation of SAX that causes OutOfMemory errors
            // when a DOCTYPE tag is present.  See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6536111
            // When that is fixed, the following line can be put back in.
            //
            // out.append("<!DOCTYPE moca-response SYSTEM \"moca-response.dtd\">\n");
            writer.append("<moca-response>");
            if (_sessionId != null) {
                writer.append("<session-id>").append(_sessionId).append("</session-id>");
            }
            
            writer.append("<status>").append(String.valueOf(status)).append("</status>");
            
            // TODO - Use a different encoder if "format=xml, format=legacy, etc..."
            
            if (message != null) {
                writer.append("<message>");
                XMLResultsEncoder.writeEscapedString(message, writer);
                writer.append("</message>");
            }
            
            if (res != null) {    
                XMLResultsEncoder.writeResults(res, writer);
            }
            
            writer.append("</moca-response>");
        }
        finally {
            writer.close();
        }
    }
    
    private final HttpServletResponse _response;
    private final String _sessionId;
    private final boolean _doCompression;
}
