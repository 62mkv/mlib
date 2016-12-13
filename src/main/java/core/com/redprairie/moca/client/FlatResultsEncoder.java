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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.RowIterator;

/**
 * A class to encode a MocaResults object into a stream of bytes.  The
 * encoding of the byte stream will be the same encoding as produced by
 * the MOCA C function sqlEncodeResults.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class FlatResultsEncoder {
    
    public static final char DELIMITER = '^';
    public static final char ESCAPE = '%';
    public static final char COLUMN_DELIMITER = '~';
    
    public static void writeResults(MocaResults res, String message, 
            OutputStream out, String encoding) {
        OutputStreamWriter hdr;
        try {
            if (encoding == null) {
                hdr = new OutputStreamWriter(out,  Charset.defaultCharset().name());
            }
            else {
                hdr = new OutputStreamWriter(out, encoding);
            }
            
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Invalid Encoding [" + encoding + "] " + e); 
        }
        try {
            if (message != null) {
                hdr.append("EMESG=");
                _escapeString(hdr, message);
                hdr.append(DELIMITER);
            }
            
            if (res != null) {
                
                int rowCount = res.getRowCount();
                int columnCount = res.getColumnCount();
    
                hdr.append("NROWS=");
                hdr.append(String.valueOf(rowCount));
                hdr.append(DELIMITER);
                hdr.append("NCOLS=");
                hdr.append(String.valueOf(columnCount));
                hdr.append(DELIMITER);
                hdr.append("DTYPE=");
                for (int i = 0; i < columnCount; i++) {
                    MocaType columnType = res.getColumnType(i);
                    char typeChar = columnType.getTypeCode();
                    if (res.isNullable(i)) {
                        typeChar = Character.toLowerCase(typeChar);
                    }
                    hdr.append(typeChar);
                }
                hdr.append(DELIMITER);
                hdr.append("CINFO=");
                for (int i = 0; i < columnCount; i++) {
                    hdr.append(res.getColumnName(i));
                    hdr.append(COLUMN_DELIMITER);
                    hdr.append(String.valueOf(res.getMaxLength(i)));
                    hdr.append(COLUMN_DELIMITER);
                    hdr.append(String.valueOf(res.getMaxLength(i)));
                    hdr.append(COLUMN_DELIMITER);
                }
                hdr.append(DELIMITER);
                hdr.append("RDATA=");
                
                for (RowIterator row = res.getRows(); row.next();) {
                    for (int i = 0; i < columnCount; i++) {
                        MocaType columnType = res.getColumnType(i);
                        char typeChar = columnType.getTypeCode();
                        hdr.append(typeChar);
                        
                        if (row.isNull(i)) {
                            hdr.append("0");
                            hdr.append(DELIMITER);
                        }
                        else {
                            if (columnType.equals(MocaType.RESULTS)) {
                                byte[] value = encodeResults(row.getResults(i), null, encoding);
                                hdr.append(String.valueOf(value.length));
                                hdr.append(DELIMITER);
                                hdr.flush();
                                out.write(value);
                            }
                            else if (columnType.equals(MocaType.BINARY)) {
                                byte[] value = (byte[])row.getValue(i);
                                hdr.append(String.valueOf(value.length + 8));
                                hdr.append(DELIMITER);
                                hdr.append(String.format("%08x", value.length));
                                hdr.flush();
                                out.write(value);
                            }
                            else if (columnType.equals(MocaType.BOOLEAN)) {
                                hdr.append("1");
                                hdr.append(DELIMITER);
                                hdr.append(row.getBoolean(i) ? '1' : '0');
                            }
                            else {
                                Object value;
                                if (columnType.equals(MocaType.DATETIME)) {
                                    SimpleDateFormat formatter = (SimpleDateFormat) _dateFormatter.clone();
                                    value = formatter.format(row.getDateTime(i));
                                }
                                else {
                                    value = row.getValue(i);
                                }
                                
                                String valueAsString = String.valueOf(value);
                                
                                hdr.append(String.valueOf(valueAsString.length()));
                                hdr.append(DELIMITER);
                                hdr.append(valueAsString);
                            }
                        }
                    }
                }
            }
            hdr.flush();
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Unexpected memory problem: " + e, e);
        }
    }
    
    public static byte[] encodeResults(MocaResults res, String message, String encoding) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter hdr;
        try {
            if (encoding == null) {
                hdr = new OutputStreamWriter(out, Charset.defaultCharset().name());
            }
            else {
                hdr = new OutputStreamWriter(out, encoding);
            }
            
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Invalid Encoding: " + e); 
        }
        
        try {
            if (message != null) {
                hdr.append("EMESG=");
                _escapeString(hdr, message);
                hdr.append(DELIMITER);
            }
            
            if (res != null) {
                
                int rowCount = res.getRowCount();
                int columnCount = res.getColumnCount();
    
                hdr.append("NROWS=");
                hdr.append(String.valueOf(rowCount));
                hdr.append(DELIMITER);
                hdr.append("NCOLS=");
                hdr.append(String.valueOf(columnCount));
                hdr.append(DELIMITER);
                hdr.append("DTYPE=");
                for (int i = 0; i < columnCount; i++) {
                    MocaType columnType = res.getColumnType(i);
                    char typeChar = columnType.getTypeCode();
                    if (res.isNullable(i)) {
                        typeChar = Character.toLowerCase(typeChar);
                    }
                    hdr.append(typeChar);
                }
                hdr.append(DELIMITER);
                hdr.append("CINFO=");
                for (int i = 0; i < columnCount; i++) {
                    hdr.append(res.getColumnName(i));
                    hdr.append(COLUMN_DELIMITER);
                    hdr.append(String.valueOf(res.getMaxLength(i)));
                    hdr.append(COLUMN_DELIMITER);
                    hdr.append(String.valueOf(res.getMaxLength(i)));
                    hdr.append(COLUMN_DELIMITER);
                }
                hdr.append(DELIMITER);
                hdr.append("RDATA=");
                
                while (res.next()) {
                    for (int i = 0; i < columnCount; i++) {
                        MocaType columnType = res.getColumnType(i);
                        char typeChar = columnType.getTypeCode();
                        hdr.append(typeChar);
                        
                        if (res.isNull(i)) {
                            hdr.append("0");
                            hdr.append(DELIMITER);
                        }
                        else {
                            if (columnType.equals(MocaType.RESULTS)) {
                                byte[] value = encodeResults(res.getResults(i), null, encoding);
                                hdr.append(String.valueOf(value.length));
                                hdr.append(DELIMITER);
                                hdr.flush();
                                out.write(value);
                            }
                            else if (columnType.equals(MocaType.BINARY)) {
                                byte[] value = (byte[])res.getValue(i);
                                hdr.append(String.valueOf(value.length + 8));
                                hdr.append(DELIMITER);
                                hdr.append(String.format("%08x", value.length));
                                hdr.flush();
                                out.write(value);
                            }
                            else if (columnType.equals(MocaType.BOOLEAN)) {
                                hdr.append("1");
                                hdr.append(DELIMITER);
                                hdr.append(res.getBoolean(i) ? '1' : '0');
                            }
                            else {
                                Object value;
                                if (columnType.equals(MocaType.DATETIME)) {
                                    SimpleDateFormat formatter = (SimpleDateFormat) _dateFormatter.clone();
                                    value = formatter.format(res.getDateTime(i));
                                }
                                else {
                                    value = res.getValue(i);
                                }
                                String valueAsString = String.valueOf(value);
                                hdr.append(String.valueOf(valueAsString.getBytes(hdr.getEncoding()).length));
                                hdr.append(DELIMITER);
                                hdr.append(valueAsString);
                            }
                        }
                    }
                }
            }
            hdr.flush();
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Unexpected memory problem: " + e, e);
        }
        return out.toByteArray();
    }
    
    // 
    // Implementation
    //
    private static void _escapeString(Appendable out, String in) throws IOException {
        int length = in.length();
        for (int i = 0; i < length; i++) {
            char c = in.charAt(i);
            if (c == DELIMITER || c == ESCAPE) {
                out.append(ESCAPE);
            }
            out.append(c);
        }
    }

    static final SimpleDateFormat _dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
}
