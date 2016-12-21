/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.sam.moca.server.repository;

import java.io.Serializable;

/**
 * This class encapsulates an argument for a Trigger or Command.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author cjolly
 * @version $Revision$
 */

public class ArgumentInfo implements Serializable {
    
    public ArgumentInfo(String name, String alias, ArgType type, String defaultValue, boolean required) {
        _name = name;
        _alias = alias;
        _type = type;
        _defaultValue = defaultValue;
        _required = required;
        
    }
    public String getComment() {
        return _comment;
    }

    public void setComment(String comment) {
        _comment = comment;
    }

    public String getName() {
        return _name;
    }

    public String getAlias() {
        return _alias;
    }

    public String getDefaultValue() {
        return _defaultValue;
    }

    public ArgType getDatatype() {
        return  _type;
    }

    public boolean isRequired() {
        return _required;
    }

    private final static long serialVersionUID = 20080311L;
    private final String _name;
    private final String _alias;
    private final String _defaultValue;
    private final ArgType _type;
    private final boolean _required;
    private String _comment;
}
