/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

package com.redprairie.moca.server.db;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.redprairie.moca.MocaType;

/**
 * Class defining a list of variables to be passed to a SQL prepared statement.
 * Each variable has a name, a type and a value.  This class is primarily meant
 * to be used in a JNI-based interface layer.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author Derek Inksetter
 * @version $Revision$
 */
public class BindList implements Serializable {

    private static final long serialVersionUID = -3543723541222290091L;
    /**
     * Add a variable, passing an explicit MOCA type of the variable.
     * 
     * @param name the name of the variable.
     * @param type an instance of MocaType.
     * @param value the value of the variable.
     */
    public void add(String name, MocaType type, Object value) {
        _add(name, type, value);
    }
    
    /**
     * Add a variable, passing an explicit type code.  The code is
     * translated into a MocaType instance.
     * 
     * @param name the name of the variable.
     * @param typeCode  
     * @param value
     */
    public void add(String name, char typeCode, Object value) {
        _add(name, MocaType.lookup(typeCode), value);
    }
    
    /**
     * Add a variable, passing an explicit type code.  The code is
     * translated into a MocaType instance.
     * 
     * @param name the name of the variable.
     * @param typeCode  
     * @param value
     */
    public void add(String name, char typeCode, Object value, int size) {
        _add(name, MocaType.lookup(typeCode), value, size);
    }
    
    /**
     * Add an integer variable, passing an explicit type code.  The code is
     * translated into a MocaType instance.
     * 
     * @param name
     * @param typeCode
     * @param value
     */
    public void add(String name, char typeCode, int value) {
        _add(name, MocaType.lookup(typeCode), Integer.valueOf(value));
    }
    
    /**
     * Add a double variable, passing an explicit type code.  The code is
     * translated into a MocaType instance.
     * 
     * @param name
     * @param typeCode
     * @param value
     */
    public void add(String name, char typeCode, double value) {
        _add(name, MocaType.lookup(typeCode), Double.valueOf(value));
    }
    
    /**
     * Add a boolean variable, passing an explicit type code.  The code is
     * translated into a MocaType instance.
     * 
     * @param name
     * @param typeCode
     * @param value
     */
    public void add(String name, char typeCode, boolean value) {
        _add(name, MocaType.lookup(typeCode), Boolean.valueOf(value));
    }
    
    /**
     * Add an object value, inferring the type from the class of the
     * object passed.
     * 
     * @param name
     * @param value
     */
    public void add(String name, Object value) {
        _add(name, MocaType.lookupClass(value.getClass()), value);
    }
    
    /**
     * Returns the type of the bind variable identified by <code>name</code>.
     * @param name the name of the bind variable to look up.
     * @return an instance of <code>MocaType</code> corresponding to the type
     * of the variable represented by the given name, or <code>null</code> if
     * the named variable is not a part of this bind list.
     */
    public MocaType getType(String name) {
        _Data data = _variables.get(name.toLowerCase());
        if (data == null) return null;
        return data._type;
    }
    
    
    /**
     * Returns the defined size of the given bind variable.  The size
     * attribute only makes sense for String bind variables.
     * @param name the name of the bind variable to look up
     * @return the defined size of the named variable.  For non-string
     * bind variables, the size is undefined.
     */
    public int getSize(String name) {
        _Data data = _variables.get(name.toLowerCase());
        if (data == null) return 0;
        return data._size;
    }

    /**
     * Returns the value of the bind variable identified by <code>name</code>.
     * @param name the name of the bind variable to look up.
     * @return the value of the variable represented by the given name, or
     * <code>null</code> if the named variable is not a part of this bind list.
     */
    public Object getValue(String name) {
        _Data data = _variables.get(name.toLowerCase());
        if (data == null) return null;
        return data._value;
    }
    
    /**
     * Returns a boolean value to indicate whether this bind list contains any
     * reference variables.  I.e. any variables with of type
     * <code>MocaType.INTEGER_REF</code>, <code>MocaType.STRING_REF</code>, 
     * or <code>MocaType.DOUBLE_REF</code>.
     * @return <code>true</code> if this bind list contains any reference
     * variables.
     */
    public boolean hasReferences() {
        return _hasReferences;
    }
    
    /**
     * Returns true if this bind list contains the named variable
     * @param name the name to look up.
     * @return <code>true</code> if this bind list contains the named variable.
     */
    public boolean contains(String name) {
        return _variables.containsKey(name.toLowerCase());
    }
    
    public void setValue(String name, Object value) {
        _Data data = _variables.get(name.toLowerCase());
        if (data == null) {
            throw new IllegalArgumentException("missing bind varaible: " + name);
        }
        
        if (value != null &&
            !data._type.getValueClass().isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("value class mismatch: " +
                    "expecting " + data._type.getValueClass() + ", got " +
                    value.getClass());
        }
        data._value = value;
    }
    
    /**
     * Determine if the bind list is empty.
     * @return true if no bind variables are present in the bind list.
     */
    public boolean isEmpty() {
        return _variables.isEmpty();
    }
    
    /**
     * Returns all the variable names in this bind list.
     * @return
     */
    public Collection<String> getNames() {
        List<String> names = new ArrayList<String>(_variables.size());
        for (_Data data: _variables.values()) {
            names.add(data._exactName);
        }
        return names;
    }
    
    //
    // Implementation
    //
    
    private void _add(String name, MocaType type, Object value, int size) {
        _Data data = new _Data();
        data._type = type;
        data._value = value;
        data._size = size;
        data._exactName = name;
        
        if (type == MocaType.DATETIME) {
            if (value != null && value instanceof String) {
                if (value.equals("")) {
                    data._value = null;
                }
                else {
                    data._value = value;
                }
            }
            else if (value != null && value instanceof Timestamp) {
                data._value = value;
            }
            else if (value != null && value instanceof Date) {
                data._value = new Timestamp(((Date)value).getTime());
            }
        }

        if (size < 0) {
            if (value == null) {
                data._size = 0;
            }
            else if (type.equals(MocaType.STRING) ||
                     type.equals(MocaType.STRING_REF)) {
                data._size = ((String)value).length();
            }
            else if (type == MocaType.DATETIME){
                data._size = 14;
            }
        }
        
        _variables.put(name.toLowerCase(), data);

        if (type.equals(MocaType.INTEGER_REF) ||
            type.equals(MocaType.DOUBLE_REF) ||
            type.equals(MocaType.STRING_REF)) {
            _hasReferences = true;
        }
    }

    private void _add(String name, MocaType type, Object value) {
        _add(name, type, value, -1);
    }
    
    private static class _Data implements Serializable {
        private static final long serialVersionUID = 1L;
        String _exactName;
        MocaType _type;
        Object _value;
        int _size;
    }
    
    private final Map<String, _Data> _variables = new LinkedHashMap<String, _Data>();
    private boolean _hasReferences = false;
}
