/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.util;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public class ResponseUtils {
    
    /**
     * Allows our special decoding of HTTP headers to allow the coexistence
     * of Unicode text, along with ISO-8859-1 encoded data. We do this by
     * encoding all non-latin characters as hex character values.
     * @param in the header value to be sent to the client.
     * @return the encoded form of the header value.
     */
    public static String encodeHeader(String in) {
        
        if (in == null)
            return null;

        StringBuilder out = new StringBuilder();
        
        int length = in.length();
        for (int i = 0; i < length; i++) {
            char c = in.charAt(i);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                out.append(' ');
            }
            else if (Character.isISOControl(c) || c > 255) {
                out.append("~");
                out.append(Integer.toHexString(c));
                out.append(";");
            }
            else if (c == '~') {
                out.append("~~");
            }
            else {
                out.append(c);
            }
        }
        
        return out.toString();
    }

    /**
     * Allows our special decoding of HTTP headers to allow the coexistence
     * of Unicode text, along with ISO-8859-1 encoded data. We do this by
     * encoding all non-latin characters as hex character values.
     * @param in the header as received from the server.
     * @return the decoded string form of the header.
     */
    public static String decodeHeader(String in) {
        if (in == null)
            return null;
    
        StringBuilder out = new StringBuilder();
        
        int length = in.length();
        for (int i = 0; i < length; i++) {
            char c = in.charAt(i);
            if (c == '~') {
                i++;
                if (in.charAt(i) == '~') {
                    out.append('~');
                }
                else {
                    int start = i;
                    for (;i < in.length(); i++) {
                        c = in.charAt(i);
                        if (c == ';') {
                            out.append((char) Integer.parseInt(in.substring(start, i), 16));
                            break;
                        }
                        else if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
                            ;
                        }
                        else {
                            out.append(in.substring(start, i));
                            break;
                        }
                    }
                }
            }
            else {
                out.append(c);
            }
        }
        
        return out.toString();
    }
}
