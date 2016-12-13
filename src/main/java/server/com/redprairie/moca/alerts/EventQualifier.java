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
package com.redprairie.moca.alerts;

import java.io.IOException;
import java.io.StringWriter;

import com.redprairie.moca.client.XMLResultsEncoder;

/**
 * @author grady
 * @version $Revision$
 */
public class EventQualifier {
    // Constants
    public static final String FLDTYP_BOOLEAN = "BOOLEAN";
    public static final String FLDTYP_DATETIME = "DATETIME";
    public static final String FLDTYP_DATETIME_CONVERTED = "DATETIME_CONV";
    public static final String FLDTYP_FLOAT = "FLOAT";
    public static final String FLDTYP_INTEGER = "INTEGER";
    public static final String FLDTYP_STRING = "STRING";
    public static final String FLDTYP_UOM_PREFIX = "UOM_";
    
    public static final String PASSED_BOOLEAN = "B";
    public static final String PASSED_DATETIME = "D";
    public static final String PASSED_DATETIME_CONVERTED = "DTC";
    public static final String PASSED_FLOAT = "F";
    public static final String PASSED_INTEGER = "I";

    /**
     * Translates the passed type to the EMS type.
     * @param type
     * @return EMS type
     */
    public static String translateType(String type) {
        String emsType;
        
        if (type.equals(PASSED_BOOLEAN)) {
            emsType = FLDTYP_BOOLEAN;
        }
        else if (type.equals(PASSED_DATETIME)) {
            emsType = FLDTYP_DATETIME;
        }
        else if (type.equals(PASSED_DATETIME_CONVERTED)) {
            emsType = FLDTYP_DATETIME_CONVERTED;
        }
        else if (type.equals(PASSED_FLOAT)) {
            emsType = FLDTYP_FLOAT;
        }
        else if (type.equals(PASSED_INTEGER)) {
            emsType = FLDTYP_INTEGER;
        }
        else if (type.startsWith(FLDTYP_UOM_PREFIX)) {
            emsType = type;
        }
        else {
            emsType = FLDTYP_STRING;
        }
        
        return emsType;
    }

    /**
     * @return XML representation of the qualifier
     * @throws IOException 
     */
    public String asXML() throws IOException {
        StringWriter out = new StringWriter();

        out.append("<" + AlertXML.QUALIFIER + " " + AlertXML.QUAL_ATTR_NAME + "=\"");
        XMLResultsEncoder.writeEscapedString(_name, out);
        out.append("\" type=\"");
        XMLResultsEncoder.writeEscapedString(_type, out);
        out.append("\">");
        XMLResultsEncoder.writeEscapedString(_value, out);
        out.append("</" + AlertXML.QUALIFIER + ">\n");
        
        return out.toString();
    }

    /**
     * @return the name
     */
    public String getName() {
        return _name;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     * @return the value
     */
    public String getValue() {
        return _value;
    }
    
    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        _value = value;
    }
    
    public String getType() {
        return _type;
    }
    
    public void setType(String type) {
        _type = type;
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (other == null) {
            return false;
        }
        
        if (!(other instanceof EventQualifier)) {
            return false;
        }
        
        final EventQualifier eq = (EventQualifier) other;
        
        return _name.equals(eq._name) && _type.equals(eq._type) 
               && _value.equals(eq._value);
    }
    
    @Override
    public int hashCode() {
        return _name.hashCode() + _type.hashCode() + _value.hashCode();
    }
    
    @Override
    public String toString() {
        return "EventQualifier(moca): " + _name + " - Type: " + _type
            + " - Value: " + _value;
    }

    // Implementation
    private String _name = "";
    private String _type = FLDTYP_STRING; // Defaults to STRING
    private String _value = "";
}