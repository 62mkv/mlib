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

package com.redprairie.moca.web;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.map.ser.std.ToStringSerializer;

import com.google.common.collect.Multimap;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.server.InstanceUrl;

/**
 * Console Results provides a standardized object representation that can
 * built-up and delivered to the client-side handlers expecting JSON encoded
 * object representation.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author Matt Horner
 * @version $Revision$
 */
public class WebResults<T> {
    public WebResults() {
        _data = new ArrayList<T>();
        _status = 0;
        _message = "";
        _totalRows = -1;
    }

    /**
     * @return Returns the data.
     */
    public List<T> getData() {
        return _data;
    }

    /**
     * @param data The data to set.
     */
    public void setData(List<T> data) {
        _data = data;
    }

    /**
     * @return Returns the status.
     */
    public int getStatus() {
        return _status;
    }

    /**
     * @param status The status to set.
     */
    public void setStatus(int status) {
        _status = status;
    }

    /**
     * @return Returns the message.
     */
    public String getMessage() {
        return _message;
    }

    /**
     * @param message The message to set.
     */
    public void setMessage(String message) {
        _message = message;
    }
    
    /**
     * Set total rows that are in all of the pages if this represents
     * a paged result set.
     * @param total
     */
    public void setTotalRows(int total) {
        _totalRows = total;
    }
    
    /**
     * Get total rows if the results are paged in the back.
     * @return
     */
    public int getTotalRows() {
        return _totalRows;
    }

    /**
     * Add an entity to the results set.
     * 
     * @param data The templated data object to add to the results.
     */
    public void add(T data) {
        _data.add(data);
    }

    /**
     * Handles packaging up an exception and delivering the bad news through the
     * results set to the client-side handlers.
     * 
     * @param exception The exception thrown to report on.
     */
    public void handleException(Exception exception) {
        _status = -1;
        if (exception instanceof MocaException) {
            _status = ((MocaException) exception).getErrorCode();
        }
        _message = exception.getMessage();
    }

    /**
     * Converts on this object into a JSON encoded object.
     * 
     * @return String the JSON encoded object representation.
     * @throws IOException 
     */
    public String toJsonString() throws IOException {
        StringWriter writer = new StringWriter();
        
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        
        // Need to add some customer serializers
        SimpleModule testModule = new SimpleModule(
            "WebResultsModule", new Version(1, 0, 0, null));
        ToStringSerializer toStringSerializer = ToStringSerializer.instance;
        testModule.addSerializer(RoleDefinition.class, toStringSerializer);
        testModule.addSerializer(InstanceUrl.class, toStringSerializer);
        testModule.addSerializer(StackTraceElement.class, toStringSerializer);
        mapper.registerModule(testModule);
        
        JsonFactory fac = new JsonFactory();
        fac.setCodec(mapper);
        
        JsonGenerator jgen = fac.createJsonGenerator(writer);
        
        try {
            jgen.writeStartObject();
            
            jgen.writeNumberField("status", _status);
            
            jgen.writeStringField("message", _message);
            
            if (_totalRows >= 0) { 
                jgen.writeNumberField("total", _totalRows);
            }
            
            jgen.writeFieldName("data");
            
            jgen.writeStartArray();
            
            for (T data : _data) {
                writeObject(data, jgen);
            }
            
            jgen.writeEndArray();
            
            jgen.writeEndObject();
            
            jgen.flush();
        }
        catch (JsonGenerationException e) {
            e.printStackTrace();
            _status = -1;
            _message = e.getMessage();

            return "{\"status\":" + _status + ",\"message\":\"" + _message
                    + "\",\"data\":{}";
        }
        
        return writer.toString();
    }
    
    private static void writeObject(Object object, JsonGenerator jgen) 
            throws JsonGenerationException, IOException {
        if (object instanceof Multimap<?, ?>) {
            Multimap<?, ?> multimap = (Multimap<?, ?>) object;

            for (Entry<?, ?> entry : multimap.asMap().entrySet()) {
                jgen.writeStartObject();
                jgen.writeObjectField("name", entry.getKey());
                jgen.writeObjectField("value", entry.getValue());
                jgen.writeEndObject();
            }
        }
        else if (object instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) object;

            for (Entry<?, ?> entry : map.entrySet()) {
                jgen.writeStartObject();
                jgen.writeObjectField("name", entry.getKey());
                jgen.writeObjectField("value", entry.getValue());
                jgen.writeEndObject();
            }
        }
        else if (object instanceof MocaResults) {
            MocaResults res = (MocaResults) object;
            RowIterator iter = res.getRows();
            while (iter.next()) {
                jgen.writeStartObject();
                for (int j = 0; j < res.getColumnCount(); ++j) {
                    jgen.writeObjectField(res.getColumnName(j), iter
                        .getValue(j));
                }
                jgen.writeEndObject();
            }
        }
        else {
            jgen.writeObject(object);
        }
    }

    private List<T> _data;
    private int _status;
    private String _message;
    private int _totalRows;
}