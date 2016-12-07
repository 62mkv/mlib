/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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
package com.redprairie.moca.alerts;

import java.io.IOException;
import java.io.StringWriter;

import com.redprairie.moca.client.XMLResultsEncoder;

/**
 * @author grady
 * @version $Revision$
 */
public class EventGroup {

    /**
     * @return XML representation of the qualifier
     * @throws IOException 
     */
    public String asXML() throws IOException {
        StringWriter out = new StringWriter();

        out.append("<" + AlertXML.GROUP + " " + AlertXML.GROUP_ATTR_NAME + "=\"");
        XMLResultsEncoder.writeEscapedString(_name, out);
        out.append("\" />\n");
        
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
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (other == null) {
            return false;
        }
        
        if (!(other instanceof EventGroup)) {
            return false;
        }
        
        final EventGroup eg = (EventGroup) other;
        
        return _name.equals(eg._name);
    }
    
    @Override
    public int hashCode() {
        return _name.hashCode();
    }
    
    @Override
    public String toString() {
        return "EventGroup(moca): " + _name;
    }
    
    // Implementation
    private String _name = "";
}