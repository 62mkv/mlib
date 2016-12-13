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
import java.util.Date;
import java.util.Map;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaOperator;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.util.DateUtils;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class XMLRequestEncoder {
    
    /**
     * @param command
     * @param context
     * @param args
     * @param out
     * @throws IOException
     */
    public static void encodeRequest(String command, String sessionId, String envString, 
                              boolean autoCommit, boolean remote, 
                              MocaArgument[] context, MocaArgument[] args,
                              Appendable out) throws IOException {
        out.append("<moca-request autocommit=\"");
        out.append(String.valueOf(autoCommit));
        out.append("\"");
        
        if (remote) {
            out.append(" remote=\"true\"");
        }
        
        out.append(">");
        if (sessionId != null) {
            out.append("<session id=\"");
            out.append(sessionId);
            out.append("\"/>");
        }
        if (envString != null) {
            out.append("<environment>");
            
            out.append(envString);
            out.append("</environment>");
        }
        
        if (context != null) {
            out.append("<context>");
            writeArgs(context, out);
            out.append("</context>");
        }
        
        if (args != null) {
            out.append("<args>");
            writeArgs(args, out);
            out.append("</args>");
        }
        
        if (command != null) {
            out.append("<query>");
            escapeXML(command, out);
            out.append("</query>");
        }
        
        out.append("</moca-request>");
    }
    
    public static String buildXMLEnvironmentString(Map<String, String> env) {
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, String> var : env.entrySet()) {
            String name = var.getKey();
            String value = var.getValue();
            
            try {
                buf.append("<var name=\"");
                escapeXML(name.toUpperCase(), buf);
                buf.append("\" value=\"");
                escapeXML(value, buf);
                buf.append("\"/>");
            }
            catch (IOException e) {
                // Could be thrown by escapeXML, but won't because we're writing
                // to a StringBuilder.
            }
        }
        return buf.toString();
    }
    
    private static void writeArgs(MocaArgument[] args, final Appendable out) throws IOException {
        
        for (MocaArgument arg : args) {
            String name = arg.getName();
            Object value = arg.getValue();
            MocaType type = arg.getType();
            MocaOperator oper = arg.getOper();

            out.append("<field name=\"");
            escapeXML(name, out);
            out.append("\" type=\"" + type.toString() + "\"");
            out.append(" oper=\"" + oper.toString() + "\"");
            
            if (value == null) {
                out.append(" null=\"true\"/>");
            }
            else {
                switch(type) {
                case BOOLEAN:
                case STRING:
                case INTEGER:
                case DOUBLE:
                    out.append(">");
                    escapeXML(String.valueOf(value), out);
                    break;
                case DATETIME:
                    out.append(">");
                    out.append(DateUtils.formatDate((Date)value));
                    break;
                case RESULTS:
                    out.append(">");
                    // We have to escape all the text so the tags don't get interpreted
                    // again.
                    Appendable app = new Appendable() {
                        
                        @Override
                        public Appendable append(CharSequence csq, int start, int end)
                                throws IOException {
                            escapeXML(csq.subSequence(start, end), out);
                            return this;
                        }
                        
                        @Override
                        public Appendable append(char c) throws IOException {
                            escapeXML(Character.toString(c), out);
                            return this;
                        }
                        
                        @Override
                        public Appendable append(CharSequence csq) throws IOException {
                            escapeXML(csq, out);
                            return this;
                        }
                    };
                    XMLResultsEncoder.writeResults((MocaResults)value, app);
                    break;
                case BINARY:
                default:
                    out.append(">");
                    break;
                }
                out.append("</field>");
            }
        }

    }

    private static void escapeXML(CharSequence s, Appendable out) throws IOException {
        if (s != null) {
            int length = s.length();
            for (int i = 0; i < length; i++) {
                char c = s.charAt(i);
                switch (c) {
                case '&':
                    out.append("&amp;");
                    break;
                case '<':
                    out.append("&lt;");
                    break;
                case '>':
                    out.append("&gt;");
                    break;
                case '"':
                    out.append("&quot;");
                    break;
                case '\'':
                    out.append("&apos;");
                    break;
                default:
                    out.append(c);
                    break;
                }
            }
        }
    }
}
