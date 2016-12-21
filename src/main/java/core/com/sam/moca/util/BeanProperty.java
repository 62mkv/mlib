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
package com.sam.moca.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Represents a single property on a particular class. Information about the
 * name and type of the property is kept. This class also has a method to
 * access a property value on an instance of the bean class. 
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author Derek Inksetter
 * @version $Revision$
 */
public class BeanProperty implements Comparable<BeanProperty> {

    /**
     * Returns the name of the property.
     * @return the name of this property.
     */
    public String getName() {
        return _name;
    }
    
    /**
     * Returns the declared type of this property.  If an accessor is declared
     * to return a superclass or interface, that <em>declared</em> type is what
     * is returned from this method.  If an accessor returns a primitive type,
     * the corresponding TYPE class (e.g. Integer.TYPE) is returned from this
     * method.
     * 
     * @return the declared type of this property.
     */
    public Class<?> getType() {
        if (_getMethod != null) {
            return _getMethod.getReturnType();
        }
        else if (_setMethod != null) {
            return _setMethod.getParameterTypes()[0];
        }
        else {
            return null;
        }
    }
    
    /**
     * Returns the value of this property on a particular instance of the 
     * bean class.
     * 
     * @param bean an instance of the class on which this property exists.
     * @return the value of this property on the given bean.
     * @throws BeanPropertyException if the accessor throws an exception.
     * @throws IllegalArgumentException if <code>bean</code> is not an instance
     * of the bean class.
     */
    public Object getValue(Object bean) {
        if (_getMethod == null) {
            throw new IllegalArgumentException("no accessor for property " + _name);
        }
        
        try {
            return _getMethod.invoke(bean, _NOARGS);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("no access to property " + _name);
        }
        catch (InvocationTargetException e) {
            throw new BeanPropertyException("unexpected accessor exception on " + _name, e);
        }
    }
    
    /**
     * Changes the value of this property on a particular instance of the 
     * bean class.
     * 
     * @param bean an instance of the class on which this property exists.
     * @throws BeanPropertyException if the accessor throws an exception.
     * @throws IllegalArgumentException if <code>bean</code> is not an instance
     * of the bean class.
     */
    public void setValue(Object bean, Object value) {
        if (_setMethod == null) {
            throw new IllegalArgumentException("no mutator for property " + _name);
        }
        try {
            _setMethod.invoke(bean, new Object[] {value});
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("no access to property " + _name);
        }
        catch (InvocationTargetException e) {
            throw new BeanPropertyException("unexpected mutator exception on " + _name, e);
        }
    }
    
    /**
     * Indicates whether this property is readable.  That is, if
     * a public getter method exists for the property associated with this
     * instance.
     * @return <code>true</code> if this property is readable.
     */
    public boolean isReadable() {
        return (_getMethod != null && Modifier.isPublic(_getMethod.getModifiers()));
    }
    
    /**
     * Indicates whether this property is writable.  That is, if
     * a public setter method exists for the property associated with this
     * instance.
     * @return <code>true</code> if this property is writable.
     */
    public boolean isWritable() {
        return (_setMethod != null && Modifier.isPublic(_setMethod.getModifiers()));
    }
    
    /**
     * @return Returns the columnName.
     */
    public String getColumnName() {
        return _columnName;
    }

    /**
     * @param columnName The columnName to set.
     */
    public void setColumnName(String columnName) {
        _columnName = columnName;
    }

    /**
     * @param order The order to set.
     */
    public void setOrder(int order) {
        _order = order;
    }

    // @see java.lang.Comparable#compareTo(java.lang.Object)
    public int compareTo(BeanProperty b) {
        int result = (_order - b._order);
        if (result == 0) {
            result = _name.compareTo(b._name);
        }
        return result;
    }
    
    // @see java.lang.Object#equals(java.lang.Object)
    public boolean equals(Object o) {
        if (!(o instanceof BeanProperty)) return false;
        return ((BeanProperty)o)._name.equals(_name);
    }
    
    // @see java.lang.Object#hashCode()
    public int hashCode() {
        return _name.hashCode();
    }

    //
    // Package Interface
    //
    BeanProperty(String name) {
        _name = name;
        _columnName = name;
    }
    
    void setSetMethod(Method setMethod) {
        _setMethod = setMethod;
    }
    
    void setGetMethod(Method getMethod) {
        _getMethod = getMethod;
    }
    
    //
    // Implementation
    //
    private static final Object[] _NOARGS = new Object[0];
    private final String _name;
    private int    _order = 999999;
    private String _columnName;
    private Method _getMethod;
    private Method _setMethod;
}
