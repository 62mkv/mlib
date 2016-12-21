/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.sam.moca.server.socket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;

import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.RowIterator;
import com.sam.moca.client.FlatResultsEncoder;
import com.sam.moca.client.crypt.EncryptionStrategy;
import com.sam.moca.util.MocaUtils;

/**
 * Encodes a MOCA response to the output channel.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */

public class MocaResponseEncoder extends SimpleChannelDownstreamHandler {
    /**
     * 
     */
    public MocaResponseEncoder(Charset encoding) {
        super();
        _encoding = encoding;
    }
    
    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        Object message = e.getMessage();
        if (!(message instanceof MocaResponse)) {
            ctx.sendDownstream(e);
            return;
        }
        
        MocaResponse result = (MocaResponse) message;
        MocaResults res = result.getResults();
        EncryptionStrategy crypt = MocaRequest.getEncryptionStrategy(result.getEncryptionType());

        // Encode the header
        { // Reduce local variable scope
            // cmdCount ^ errorCode ^ messageLen ^ message ^ nRows ^ ncols ^ dypes ^ colInfo
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            OutputStreamWriter out = new OutputStreamWriter(buf, _encoding);
    
            // Command Count -- not used
            out.append("-1").append('^');
    
            // Error code
            int errorCode = result.getStatus();
            out.append(String.valueOf(errorCode)).append('^');
            
            // Error message
            String errorMessage = result.getMessage();
            if (errorMessage != null) {
                byte[] messageBytes = errorMessage.getBytes(_encoding);
                out.append(String.valueOf(messageBytes.length)).append('^');
                out.append(errorMessage).append('^');
            }
            else {
                out.append("0^^");
            }
            
            // Results metadata
            if (res == null) {
                out.append("0^0^^~~~~~~^");
            }
            else {
                // Row count
                out.append(String.valueOf(res.getRowCount())).append('^');
                
                // Column count
                int columns = res.getColumnCount();
                out.append(String.valueOf(columns)).append('^');
                
                // Column types
                for (int i = 0; i < columns; i++) {
                    out.append(res.getColumnType(i).getTypeCode());
                }
                out.append('^');
                
                // Column names.  The defined vs. actual length distinction is
                // ignored, as are the short and long column description.
                for (int i = 0; i < columns; i++) {
                    out.append('~').append(res.getColumnName(i));
                    out.append('~').append(String.valueOf(res.getMaxLength(i)));
                    out.append('~').append(String.valueOf(res.getMaxLength(i)));
                    out.append('~').append(res.getColumnName(i));
                    out.append('~').append(res.getColumnName(i));
                }
                out.append("~^");
            }
            
            out.flush();
            
            byte[] headerBytes = buf.toByteArray();
            buf = null;
            
            headerBytes = crypt.encrypt(headerBytes);
            ChannelBuffer header = ChannelBuffers.dynamicBuffer(headerBytes.length + 15);
            
            String prefix = "V104^" + headerBytes.length + "^";
            header.writeBytes(prefix.getBytes(_encoding));
            header.writeBytes(headerBytes);
            e.getChannel().write(header);

        }
        
        // Now do the data
        if (res != null) {
            RowIterator row = res.getRows();
            int columns = res.getColumnCount();

            while (row.next()) {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();

                for (int i = 0; i < columns; i++) {
                    MocaType type = res.getColumnType(i);
                    // We know that type characters are byte-equivalent in UTF-8
                    buf.write((byte)type.getTypeCode());
                    if (row.isNull(i)) {
                        buf.write('0');
                        buf.write('^');
                    }
                    else {
                        switch (type) {
                        case STRING:
                        case DOUBLE:
                        case INTEGER:
                            writeStringToStream(buf, row.getString(i));
                            break;
                        case DATETIME:
                            writeStringToStream(buf, MocaUtils.formatDate(row.getDateTime(i)));
                            break;
                        case BOOLEAN:
                            buf.write('1');
                            buf.write('^');
                            buf.write(row.getBoolean(i) ? '1' : '0');
                            break;
                        case RESULTS:
                            byte[] encoded = FlatResultsEncoder.encodeResults(row.getResults(i), null, "UTF-8");
                            writeDataToStream(buf, encoded);
                            break;
                        case BINARY:
                            byte[] data = (byte[])row.getValue(i);
                            buf.write(String.format("%d^%08x", data.length + 8, data.length).getBytes(_encoding));
                            buf.write(data);
                            break;
                        default:
                            buf.write('0');
                            buf.write('^');
                            break;
                        }
                    }
                }
                
                ChannelBuffer rowOutput = ChannelBuffers.dynamicBuffer();
                byte[] rowBytes = buf.toByteArray();
                buf = null;
                rowBytes = crypt.encrypt(rowBytes);
                String rowPrefix = "" + rowBytes.length + "^";
                rowOutput.writeBytes(rowPrefix.getBytes(_encoding));
                rowOutput.writeBytes(rowBytes);

                e.getChannel().write(rowOutput);
            }
        }
    }
    
    private void writeStringToStream(OutputStream rowOutput, String data) throws IOException {
        writeDataToStream(rowOutput, data.getBytes(_encoding));
    }
    
    private void writeDataToStream(OutputStream rowOutput, byte[] data) throws IOException {
        rowOutput.write(String.valueOf(data.length).getBytes(_encoding));
        rowOutput.write((byte)'^');
        rowOutput.write(data);
    }
    
    private final Charset _encoding;
}