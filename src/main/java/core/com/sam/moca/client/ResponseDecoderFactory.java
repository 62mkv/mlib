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
import java.io.InputStream;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

/**
 * A factory that provides a response decoder to use when decoding and reading
 * a response back from the server.
 * 
 * <b><pre>
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class ResponseDecoderFactory {
    /**
     * This will return the appropriate response decoder given the connection
     * settings.
     * @param conn
     * @return The response encoder to use.
     * @throws IOException If a problem occurs with connection.
     */
    public static ResponseDecoder getResponseDecoder(URLConnection conn) 
            throws IOException {
        String contentType = conn.getContentType();
        
        int charsetLocation = contentType.indexOf("charset");
        String charset;
        if (charsetLocation != -1) {
            // we get the encoding one character after for the equals and
            // charset characters
            charset = contentType.substring(charsetLocation + 8);
        }
        else {
            charset = "UTF-8";
        }
        
        if (contentType.startsWith("application/json")) {
            return new JSONResponseDecoder(conn, charset);
        }
        else if (contentType.startsWith("application/moca")) {
            return new FlatResponseDecoder(conn, charset);
        }
        
        // The default response encoder is XML.
        String contentEncoding = conn.getContentEncoding();
        boolean useGzip = (contentEncoding != null && contentEncoding.equals("gzip"));
        InputStream in = conn.getInputStream();
        if (useGzip) {
            in = new GZIPInputStream(in);
        }
        return new XMLResultsDecoder(in);
    }
}
