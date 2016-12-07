/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2006
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
package com.redprairie.webservices.config;

/**
 * popultae and hold the name and type of input arguments of operation
 * 
 * <b>
 * 
 * <pre>
 *  Copyright (c) 2005 RedPrairie Corporation
 *  All rights reserved.
 * </pre>
 * 
 * </b>
 * 
 * @author Mohanesha.C
 * @version $Revision$
 */
public class OperationArgument {

    /**
     * @return Returns the _name.
     */
    public String getName() {
        return _name;
    }

    /**
     * @param name The _name to set.
     */
    public void setName(String name) {
        this._name = name;
    }

    /**
     * @return Returns the _type.
     */
    public String getType() {
        return _type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this._type = type;
    }

    /**
     * @return Returns the _nullable.
     */    
    public boolean isNullable() {
        return _nullable;
    }

    /**
     * @param nullable The nullable to set.
     */    
    public void setNullable(boolean nullable) {
        this._nullable = nullable;
    }
    
    /**
     * @return the column name
     */
    public String getColumn() {
        if (_column == null) return _name;
        else return _column;
    }

    /**
     * Sets the column name to the given value.
     * @param column the column name
     */
    public void setColumn(String column) {
        _column = column;
    }
    
    // -----------------------------
    // implementation:
    // -----------------------------

    private String _name = null;
    private String _type = null;
    private String _column = null;
    private boolean _nullable = false;
}
