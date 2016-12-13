/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.client.jackson;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;

import com.redprairie.moca.MocaResults;
import com.redprairie.moca.RowIterator;

/**
 * This can be used to convert a MocaResults to JSON using a provided
 * Jackson JsonGenerator.  This will serialize the MocaResults instance to
 * an array that contains multiple objects, one for each row.  Each object will 
 * contain the column/value pair for each value in the row in the same order.  
 * This class can only 
 * be used to serialize the MocaResults to JSON.  Unfortunately there is no way 
 * to deserialize back to a MocaResults exactly due to not returning column 
 * metadata information so column type information is lost.  This is designed
 * to be read by some client that doesn't care this object was once a 
 * MocaResults object.
 * 
 * A codec should be used since it is possible that Date objects and other
 * various Java Objects could be contained in the result set.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class SimpleMocaResultsSerializer extends JsonSerializer<MocaResults> {

    // @see org.codehaus.jackson.map.JsonSerializer#serialize(java.lang.Object, org.codehaus.jackson.JsonGenerator, org.codehaus.jackson.map.SerializerProvider)
    @Override
    public void serialize(MocaResults value, JsonGenerator jgen,
        SerializerProvider provider) throws IOException,
            JsonProcessingException {
        jgen.writeStartArray();
        RowIterator iter = value.getRows();
        while (iter.next()) {
            jgen.writeStartObject();
            for (int j = 0; j < value.getColumnCount(); ++j) {
                String fieldName = value.getColumnName(j);
                Object obj = iter.getValue(j);
                JacksonTools.serliazeObjectField(fieldName, obj, jgen, provider);
            }
            jgen.writeEndObject();
        }
        jgen.writeEndArray();
    }
    
    // @see org.codehaus.jackson.map.JsonSerializer#serializeWithType(java.lang.Object, org.codehaus.jackson.JsonGenerator, org.codehaus.jackson.map.SerializerProvider, org.codehaus.jackson.map.TypeSerializer)
    @Override
    public void serializeWithType(MocaResults value, JsonGenerator jgen,
        SerializerProvider provider, TypeSerializer typeSer)
            throws IOException, JsonProcessingException {
        typeSer.writeTypePrefixForScalar(value, jgen);
        serialize(value, jgen, provider);
        typeSer.writeTypeSuffixForScalar(value, jgen);
    }
    
    // @see org.codehaus.jackson.map.JsonSerializer#handledType()
    @Override
    public Class<MocaResults> handledType() {
        return MocaResults.class;
    }
}
