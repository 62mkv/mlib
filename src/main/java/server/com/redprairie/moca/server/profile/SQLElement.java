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

package com.redprairie.moca.server.profile;


/**
 * TODO Class Description
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class SQLElement implements CommandPathElement {
    
    private static final long serialVersionUID = -8360716734235728384L;

    public static final SQLElement INSTANCE = new SQLElement();
    
    private final String _label;
    
    private SQLElement() {
        // pivate constructor to prevent instantiation
        _label = "SQL";
    }
    
    SQLElement(String label) {
        // Package protected constructor to prevent instantiation
        _label = "SQL - " + label;
    }
    
    @Override
    public String getGroup() {
        return "";
    }
    
    @Override
    public String getName() {
        return _label;
    }

    @Override
    public String getType() {
        return "SQL";
    }
    
    @Override
    public String toString() {
        return _label;
    }

    // @see java.lang.Object#hashCode()
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_label == null) ? 0 : _label.hashCode());
        return result;
    }

    // @see java.lang.Object#equals(java.lang.Object)
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SQLElement other = (SQLElement) obj;
        if (_label == null) {
            if (other._label != null) return false;
        }
        else if (!_label.equals(other._label)) return false;
        return true;
    }

}
