/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2006
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.SimpleResults;

/**
 * A class to decode a stream of bytes into a MocaResults object.  The
 * encoding of the byte stream must be the same encoding as produced by
 * the MOCA C function sqlEncodeResults.
 * 
 * <b><pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class FlatResultsDecoder {
    
    public FlatResultsDecoder(InputStream in, String charset) {
        _in = in;
        _charset = charset;
    }
    
    synchronized
    public MocaException decodeError(int errorCode) throws IOException, ProtocolException {
        _decode(errorCode);
        return _exception;
    }

    synchronized
    public MocaResults decode() throws IOException, ProtocolException {
        _decode(0);
        return _results;
    }
    
    synchronized
    private void _decode(int errorCode) throws IOException, ProtocolException {
        EditableResults res = null;
        
        int rowCount = 0;
        int columnCount = 0;
        String dataTypes = null;
        String columnInfo = null;
        String errorMessage = null;
        
        try {
            do {
                // Read the stanza prefix [xxxxx=]some-value
                byte[] prefix = new byte[5];
                int nbytes = _in.read(prefix);

                if (nbytes == -1) {
                    return;
                }
                
                // We have to keep reading until we have 5 bytes or end.
                while (nbytes != 5) {
                    int nmore = _in.read(prefix, nbytes, 5 - nbytes);
                    if (nmore == -1) {
                        break;
                    }
                    
                    nbytes += nmore;
                }
                
                if (nbytes != 5) {
                    throw new ProtocolException("error reading stream: unrecognized stanza: " + Arrays.toString(prefix));
                }
                int eq = _in.read();
                if (eq != '=') {
                    throw new ProtocolException("error reading stream: unrecognized character: " + eq);
                }
                
                String key = new String(prefix,  Charset.defaultCharset().name());
                if (key.equals("RDATA")) {
                    break;
                }
                
                // Read to the delimiter character
                String tmp = _nextField();
                
                if (key.equals("NROWS")) {
                    try {
                        rowCount = Integer.parseInt(tmp);
                    }
                    catch (NumberFormatException e) {
                        throw new ProtocolException("error reading stream: illegal number of rows: " + tmp, e);
                    }
                }
                else if (key.equals("NCOLS")) {
                    try {
                        columnCount = Integer.parseInt(tmp);
                    }
                    catch (NumberFormatException e) {
                        throw new ProtocolException("error reading stream: illegal number of columns: " + tmp, e);
                    }
                }
                else if (key.equals("DTYPE")) {
                    dataTypes = tmp;
                }
                else if (key.equals("CINFO")) {
                    columnInfo = tmp;
                }
                else if (key.equals("EMESG")) {
                    errorMessage = tmp;
                }
            } while (true);
            
            res = new SimpleResults();
            
            // Set up the column header information
            String[] metadataFields = columnInfo.split("~");
            
            for (int c = 0, m = 0; c < columnCount; c++) {
                char typeCode = dataTypes.charAt(c);
                MocaType type = MocaType.lookup(Character.toUpperCase(typeCode));
                // This is the nullable column, no longer used
                // If it was lower case then it was nullable
                Character.isLowerCase(typeCode);
                String name = metadataFields[m++];
                int definedMaxLength = Integer.parseInt(metadataFields[m++]);
                // This is athe actual max length position, no longer used
                Integer.parseInt(metadataFields[m++]);
                
                res.addColumn(name, type, definedMaxLength);
            }
            
            // Next read the row data
            for (int r = 0; r < rowCount; r++) {
                res.addRow();
                for (int c = 0; c < columnCount; c++) {
                    char typeCode = (char)_in.read();
                    MocaType type = MocaType.lookup(typeCode);
                    int dataLength = Integer.parseInt(_nextField());
                    
                    if (dataLength == 0) {
                        res.setNull(c);
                    }
                    else {
                        if (type.equals(MocaType.BINARY)) {
                            // The first 8 bytes are encoding the length of the data.
                            // We've already got that, so there's really no need to
                            // interpret it.
                            int skipped = 8;
                            while (skipped > 0) {
                                long skipCount = _in.skip(skipped);
                                if (skipCount == 0) {
                                    if (_in.read() == -1) {
                                        throw new ProtocolException(
                                                "Unexpected end of stream " +
                                                "found, expected to find " +
                                                "encoding as 8 bytes ");
                                    }
                                    else {
                                        skipCount = 1;
                                    }
                                }
                                skipped -= skipCount; 
                            }
                            byte[] data = _readBytes(dataLength - 8);
                            res.setBinaryValue(c, data);
                        }
                        else if (type.equals(MocaType.RESULTS)) {
                            byte[] data = _readBytes(dataLength);
                            InputStream dataStream = new ByteArrayInputStream(data);
                            MocaResults sub = new FlatResultsDecoder(dataStream, _charset).decode();
                            res.setResultsValue(c, sub);
                        }
                        else {
                            String data = _readBytesAsString(dataLength);
                            if (type.equals(MocaType.STRING) || type.equals(MocaType.STRING_REF)) {
                                res.setStringValue(c, data);
                            }
                            else if (type.equals(MocaType.INTEGER) || type.equals(MocaType.INTEGER_REF)) {
                                try {
                                    res.setIntValue(c, Integer.parseInt(data));
                                }
                                catch (NumberFormatException e) {
                                    throw new ProtocolException("error parsing integer: " + data, e);
                                }
                            }
                            else if (type.equals(MocaType.DOUBLE) || type.equals(MocaType.DOUBLE_REF)) {
                                try {
                                    res.setDoubleValue(c, Double.parseDouble(data));
                                }
                                catch (NumberFormatException e) {
                                    throw new ProtocolException("error parsing double: " + data, e);
                                }
                            }
                            else if (type.equals(MocaType.BOOLEAN)) {
                                res.setBooleanValue(c, !data.equals("0"));
                            }
                            else if (type.equals(MocaType.DATETIME)) {
                                try {
                                    DateFormat fmt = (DateFormat) FlatResultsEncoder._dateFormatter.clone();
                                    res.setDateValue(c, fmt.parse(data));
                                }
                                catch (ParseException e) {
                                    throw new ProtocolException("error parsing date: " + data, e);
                                }
                            }
                            else {
                                // TODO deal with unknown data types
                            }
                        }
                    }
                }
            }
        }
        finally {
            if (errorCode != 0) {
                if (errorCode == NotFoundException.DB_CODE || errorCode == NotFoundException.SERVER_CODE) {
                    _exception = new NotFoundException(errorCode, res);
                }
                else {
                    _exception = new ServerExecutionException(errorCode, errorMessage, res);
                }
            }
            else {
                _results = res;
            }
        }
    }
    
    // 
    // Implementation
    //
    private String _nextField(boolean decode) throws IOException {
        ByteArrayOutputStream str = new ByteArrayOutputStream();
        while (true) {
            int c = _in.read();
            if (c == -1 || c == FlatResultsEncoder.DELIMITER) {
                break;
            }
            if (decode && c == FlatResultsEncoder.ESCAPE) {
                c = _in.read();
            }
            str.write(c);
        }

        String field;
        if (_charset == null) {
            field = str.toString( Charset.defaultCharset().name());
        }
        else {
            field = str.toString(_charset);
        }
        return field;
    }
    
    private String _nextField() throws IOException {
        return _nextField(false);
    }

    private byte[] _readBytes(int length) throws IOException {
        byte[] tmp = new byte[length];
        int total = 0;
        do {
            int nread = _in.read(tmp, total, length - total);
            if (nread == -1) {
                break;
            }
            total += nread;
        } while (total < length);

        return tmp;
    }
    
    private String _readBytesAsString(int length) throws IOException {
        byte[] data = _readBytes(length);
        if (_charset == null) {
            return new String(data, "UTF-8");
        }
        else {
            return new String(data, _charset);
        }
    }
    
    private InputStream _in;
    private String _charset;
    private MocaResults _results;
    private MocaException _exception;
}
