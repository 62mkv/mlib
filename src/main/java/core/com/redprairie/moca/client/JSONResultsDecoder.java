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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.JsonToken;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.SimpleResults;

/**
 * A class to decode a sequence of characters in JSON format into a MocaResults 
 * object.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class JSONResultsDecoder {
    /**
     * Reads the MocaResults representation from the Reader assuming it is
     * in JSON format.  The reader should be closed by caller.
     * @param reader
     * @return
     * @throws JsonParseException
     * @throws IOException
     */
    public static MocaResults decode(Reader reader) 
            throws JsonParseException, IOException {
        JsonFactory f = new JsonFactory();
        // We don't want to close the reader afterwards.
        f.disable(Feature.AUTO_CLOSE_SOURCE);
        JsonParser jp = f.createJsonParser(reader);
        
        JsonToken token = jp.nextToken();
        
        // This means that there was no result set provided.
        if (token == null) {
            return null;
        }
        
        if (token != JsonToken.START_OBJECT) {
            throw new JsonParseException("Expected Start Object Token", 
                    jp.getCurrentLocation());
        }
        
        try {
            return decode(jp);
        }
        finally {
            jp.close();
        }
    }
    
    /**
     * This assumes the current token is on the start of the result set object.
     * @param jp
     * @return
     * @throws JsonParseException
     * @throws IOException
     */
    private static MocaResults decode(JsonParser jp) 
            throws JsonParseException, IOException {
        EditableResults res = new SimpleResults();
        
        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = jp.getCurrentName();
            // move to value, or START_OBJECT/START_ARRAY
            jp.nextToken();
            // contains an array of name/values
            if (JSONResultsEncoder.METADATA.equals(fieldname)) {
                // This should move us to the begin object position if we
                // have values, else it will be end array.
                while (jp.nextToken() != JsonToken.END_ARRAY) {
                    // Now we move to the name
                    jp.nextToken();
                    String nameField = jp.getText();
                    // move to type
                    jp.nextToken();
                    String type = jp.getText();
                    MocaType mocaType = MocaType.lookup(type.charAt(0));
                    // Move to the length
                    jp.nextToken();
                    int length = jp.getIntValue();
                    
                    res.addColumn(nameField, mocaType, length);
                    
                    JsonToken token = jp.nextToken();
                    if (token != JsonToken.END_ARRAY) {
                        throw new JsonParseException("Expected End Array Token", 
                                jp.getCurrentLocation());
                    }
                }
            }
            // contains an array of arrays
            else if (JSONResultsEncoder.VALUES.equals(fieldname)) {
                // Each time we go in here is a row.
                while (jp.nextToken() != JsonToken.END_ARRAY) {
                    res.addRow();
                    
                    int position = 0;
                    // This is each row contents.
                    while (jp.nextToken() != JsonToken.END_ARRAY) {
                        
                        if (jp.getCurrentToken() == JsonToken.VALUE_NULL) {
                            // We have to post decrement position to make sure
                            // we are on the right column count.
                            res.setNull(position++);
                            continue;
                        }
                        
                        MocaType type = res.getColumnType(position);
                        
                        switch (type) {
                        case STRING:
                        case STRING_REF:
                            String text = jp.getText();
                            res.setStringValue(position, text);
                            break;
                        case INTEGER:
                        case INTEGER_REF:
                            int intValue = jp.getIntValue();
                            res.setIntValue(position, intValue);
                            break;
                        case DOUBLE:
                        case DOUBLE_REF:
                            double doubleValue = jp.getDoubleValue();
                            res.setDoubleValue(position, doubleValue);
                            break;
                        case BOOLEAN:
                            boolean booleanValue = jp.getBooleanValue();
                            res.setBooleanValue(position, booleanValue);
                            break;
                        case BINARY:
                            byte[] binary = jp.getBinaryValue();
                            res.setBinaryValue(position, binary);
                            break;
                        case DATETIME:
                            String dateString = jp.getText();
                            Date date;
                            try{
                                SimpleDateFormat formatter =
                                        (SimpleDateFormat)JSONResultsEncoder._formatter.clone();
                                    date = formatter.parse(dateString);
                            }
                            catch(ParseException e){
                                throw new JsonParseException(
                                    "Incorrect date format",
                                    jp.getCurrentLocation(), e);
                            }
                            res.setDateValue(position, date);
                            break;
                        case RESULTS:
                            MocaResults nestedRes = JSONResultsDecoder.decode(jp);
                            res.setResultsValue(position, nestedRes);
                            break;
                        default:
                            // If we got here means we have an object that is
                            // serializable and we need to deserialize it.
                            byte[] bytes = jp.getBinaryValue();
                            
                            ByteArrayInputStream bais = new ByteArrayInputStream(
                                    bytes);
                            ObjectInputStream ois = new ObjectInputStream(bais);
                            Object obj;
                            try {
                                obj = ois.readObject();
                            }
                            catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                            
                            res.setValue(position, obj);
                            break;
                        }
                        position++;
                    }
                }
            }
            else {
                throw new IllegalStateException("Unrecognized field '"
                        + fieldname + "'!");
            }
        }

        return res;
    }
}
