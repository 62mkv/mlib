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

package com.redprairie.moca.client;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonGenerator.Feature;

import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.RowIterator;

/**
 * A class to encode a MocaResults object into a stream of characters in JSON
 * format.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class JSONResultsEncoder {
    
    public static final String METADATA = "metadata";
    public static final String VALUES = "values";
    
    /**
     * Writes the XML representation of a MOCA Result set to a 
     * <code>Writer</code>.  The writer needs to be closed by calling code.
     * @param res a result set to be encoded. 
     * @param out the <code>Writer</code> object to write the JSON output to.
     * @throws IOException if an error occurred writing the output.
     */
    public static void writeResults(MocaResults res, Writer out) throws IOException {
        JsonFactory f = new JsonFactory();
        // We don't want to close the writer afterwards.
        f.disable(Feature.AUTO_CLOSE_TARGET);

        final JsonGenerator generator = f.createJsonGenerator(out);
        
        writeResults(res, generator);
        
        generator.close();
    }
    
    /**
     * This will write a moca results object to the generator at it's current
     * position.
     * @param res
     * @param generator
     * @throws IOException
     */
    private static void writeResults(MocaResults res, JsonGenerator generator) throws IOException {
        if (res == null) {
            return;
        }
        
        int columnCount = res.getColumnCount();
        MocaType[] types = new MocaType[columnCount];
        
        generator.writeStartObject();
        generator.writeArrayFieldStart(METADATA);
        
        for (int i = 0; i < columnCount; i++) {
            generator.writeStartArray();
            types[i] = res.getColumnType(i);
            generator.writeString(res.getColumnName(i));
            generator.writeString(Character.toString(types[i].getTypeCode()));
            generator.writeNumber(res.getMaxLength(i));
            generator.writeEndArray();
        }
        
        generator.writeEndArray();
        
        generator.writeArrayFieldStart(VALUES);
        
        for (RowIterator row = res.getRows(); row.next();) {
            generator.writeStartArray();
            for (int i = 0; i < columnCount; i++) {
                if (row.isNull(i)) {
                    generator.writeNull();
                    continue;
                }
                switch(types[i]) {
                case STRING:
                case STRING_REF:
                    generator.writeString(row.getString(i));
                    break;
                case BINARY:
                    generator.writeBinary((byte[])row.getValue(i));
                    break;
                case BOOLEAN:
                    generator.writeBoolean(row.getBoolean(i));
                    break;
                case RESULTS:
                    JSONResultsEncoder.writeResults(row.getResults(i), generator);
                    break;
                case DOUBLE:
                case DOUBLE_REF:
                    generator.writeNumber(row.getDouble(i));
                    break;
                case INTEGER:
                case INTEGER_REF:
                    generator.writeNumber(row.getInt(i));
                    break;
                case DATETIME:
                    Date date = row.getDateTime(i);
                    SimpleDateFormat formatter =
                            (SimpleDateFormat)_formatter.clone();
                        String dateString = formatter.format(date);
                    generator.writeString(dateString);
                    break;
                default:
                    Object value = row.getValue(i);
                    if (value instanceof Serializable || 
                            value instanceof Externalizable) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(baos);
                        oos.writeObject(value);
                        
                        // This kinda sucks since we have 2 copies of the same
                        // byte array.  Need to find way to reduce.
                        byte[] bytes = baos.toByteArray();
                        generator.writeBinary(bytes);
                    }
                    else {
                        generator.writeNull();
                    }
                    break;
                }
            }
            generator.writeEndArray();
        }
        
        generator.writeEndArray();
        
        generator.writeEndObject();
    }

    static final SimpleDateFormat _formatter = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss");
}
