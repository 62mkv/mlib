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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.SimpleDateFormat;

import com.sam.moca.MocaInterruptedException;
import com.sam.moca.MocaResults;
import com.sam.moca.RowIterator;
import com.sam.util.Base64;

/**
 * A class to encode a MocaResults object into an XML document.  This encoder
 * writes its output to a <code>Writer</code> object, which may be streamed
 * directly to an IO channel.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class XMLResultsEncoder {
    
    /**
     * Writes the XML representation of a MOCA Result set to a 
     * <code>Appendable</code>.
     * @param res a result set to be encoded. 
     * @param out the <code>Appendable</code> object to write the XML output to.
     * @throws IOException if an error occurred writing the output.
     */
    public static void writeResults(MocaResults res, Appendable out) throws IOException {
        
        if (res  == null) return;
        
        try {
            out.append("<moca-results>");
            out.append("<metadata>");
            int columns = res.getColumnCount();
            for (int i = 0; i < columns; i++) {
                out.append("<column name=\"");
                writeEscapedString(res.getColumnName(i), out);
                out.append("\" type=\"");
                out.append(res.getColumnType(i).getTypeCode());
                out.append("\" length=\"");
                out.append(String.valueOf(res.getMaxLength(i)));
                out.append("\" nullable=\"");
                out.append(String.valueOf(res.isNullable(i)));
                out.append("\"/>");
            }
            out.append("</metadata>");
            
            out.append("<data>");
            for (RowIterator row = res.getRows(); row.next();) {
                out.append("<row>");
                for (int i = 0; i < columns; i++) {
                    // If we were interrupted then stop it
                    if (Thread.interrupted()) {
                        throw new MocaInterruptedException();
                    }
                    out.append("<field");
                    if (!row.isNull(i)) {
                        switch(res.getColumnType(i)) {
                        case BOOLEAN:
                            out.append(">");
                            out.append(row.getBoolean(i) ? "1" : "0");
                            break;
                        case STRING:
                            out.append(">");
                            writeEscapedString(row.getString(i), out);
                            break;
                        case INTEGER:
                            out.append(">");
                            out.append(String.valueOf(row.getInt(i)));
                            break;
                        case DOUBLE:
                            out.append(">");
                            out.append(String.valueOf(row.getDouble(i)));
                            break;
                        case RESULTS:
                            out.append(">");
                            writeResults(row.getResults(i), out);
                            break;
                        case DATETIME:
                            SimpleDateFormat dateParser = (SimpleDateFormat) _dateFormatter.clone();
                            out.append(">");
                            out.append(dateParser.format(row.getDateTime(i)));
                            break;
                        case BINARY:
                            out.append(">");
                            Base64.encode(new ByteArrayInputStream((byte[])row.getValue(i)), out);
                            break;
                        default:
                            out.append(">");
                            break;
                        }
                    }
                    else {
                        out.append(" null=\"true\">");
                    }
                    out.append("</field>");
                }
                out.append("</row>");
            }
            
            out.append("</data>");
            out.append("</moca-results>");
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
    }

    
    /**
     * Escapes the string using predefined XML entities.
     * @param in the string to be escaped.
     * @param out the Appendable to which the escaped output should be written.
     * @throws IOException if a write error occurs on the output device.
     */
    public static void writeEscapedString(String in, Appendable out) throws IOException {
        if (in == null)
            return;
        
        int length = in.length();
        for (int i = 0; i < length; i++) {
            char c = in.charAt(i);
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
            case '\'':
                out.append("&apos;");
                break;
            case '"':
                out.append("&quot;");
                break;
            case ' ':
            case '\t':
            case '\n':
            case '\r':
                out.append(c);
                break;
            default:
                if (Character.isISOControl(c)) {
                    // Any non-whitespace control characters can appear as themselves, as long
                    // as they are escaped as character entities.  The exception, in Unicode 1.0,
                    // is that C0 control characters (0x00 .. 0x1f) cannot appear in any form.
                    // In order to allow strings that contain C0 control characters, we must move
                    // the entire protocol to XML 1.1.
                    if (c >= 0x00 && c <= 0x1f) {
                        // Output the special "not a character" character
                        out.append('\ufffd');
                    }
                    else {
                        out.append("&#x");
                        out.append(Integer.toHexString(c));
                        out.append(";");
                    }
                }
                else {
                    out.append(c);
                }
            }
        }
    }

    // 
    // Implementation
    //
    protected static final SimpleDateFormat _dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
}
