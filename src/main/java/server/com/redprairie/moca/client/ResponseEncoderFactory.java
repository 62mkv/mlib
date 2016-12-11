/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.log.Log;

/**
 * A factory that provides a response encoder to use when encoding and writing
 * a response back to a client.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author mlange
 * @version $Revision$
 */
public class ResponseEncoderFactory {
    public static ResponseEncoder getResponseEncoder(HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     String sessionId,
                                                     boolean allowCompression) {
        // The request header may include a header specifying the encoder to use. 
        //String responseFormat = request.getHeader("Response-Encoder");
        String responseFormat = request.getParameter("ResponseFormat");

        System.out.println("responseFormat:" + responseFormat);
        boolean useCompression;
        if (allowCompression) {
            // If the browser support gzip compression, we might want to support compression.
            String encodings = request.getHeader("Accept-Encoding");
            useCompression = (encodings != null && encodings.indexOf("gzip") != -1);
        }
        else {
            useCompression = false;
        }
            
        if (responseFormat != null) {
            // The flat response encoder is used for C-based clients. 
            if (responseFormat.equalsIgnoreCase("Flat")) {
                return new FlatResponseEncoder(response, sessionId);       
            }
            // The json response encoder. 
            else if (responseFormat.equalsIgnoreCase("Json")) {
                return new JSONResponseEncoder(response, sessionId, useCompression);       
            }
        }
        
        // The default response encoder is XML.
        return new XMLResponseEncoder(response, sessionId, useCompression);
    }
}
