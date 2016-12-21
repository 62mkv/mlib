/*
 *  $URL$
 *  $Author$
 *  $Date$
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

package com.sam.moca.client.jackson;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class JacksonTools {
    /**
     * This is here as a stop gap for a bug in jackson.  Unfortunately
     * the serialization config is not passed along for request configurations
     * when.
     * @param value
     * @param jgen
     * @param provider
     * @throws JsonProcessingException
     * @throws IOException
     */
    public static void serliazeObject(Object value, JsonGenerator jgen,
        SerializerProvider provider) throws JsonProcessingException, IOException {
        if (value != null) {
            JsonSerializer<Object> ser = provider.findTypedValueSerializer(
                value.getClass(), true, null);
            ser.serialize(value, jgen, provider);
        }
        else {
            jgen.writeNull();
        }
    }
    
    /**
     * This is here as a stop gap for a bug in jackson.  Unfortunately
     * the serialization config is not passed along for request configurations
     * when.
     * @param fieldName
     * @param value
     * @param jgen
     * @param provider
     * @throws JsonProcessingException
     * @throws IOException
     */
    public static void serliazeObjectField(String fieldName, Object value, JsonGenerator jgen,
        SerializerProvider provider) throws JsonProcessingException, IOException {
        if (value != null) {
            jgen.writeFieldName(fieldName);
            JsonSerializer<Object> ser = provider.findTypedValueSerializer(
                value.getClass(), true, null);
            ser.serialize(value, jgen, provider);
        }
        else {
            jgen.writeNullField(fieldName);
        }
    }
}
