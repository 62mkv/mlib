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

package com.redprairie.moca.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.redprairie.moca.MocaColumn;

/**
 * A class to describe a class in terms of the properties it posesses.  Any
 * class can be inspected as a bean.  All properties of the class are made
 * available, with the exception of the <code>class</code> property that
 * comes from <code>java.lang.Object</code>.
 * 
 * Properties of an object are defined by the existence of methods that conform
 * to the JavaBeans naming conventions. I.e. the method
 * <code>String getPropertyName()</code> defines a property,
 * <code>propertyName</code> of type String.  If a corresponding <code>set</code>
 * method is present, the property is considered to be writable.
 * 
 * @see com.redprairie.moca.util.BeanProperty
 *
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author Derek Inksetter
 * @version $Revision$
 */
public class BeanInspector {

    /**
     * Static factory method that returns a <code>BeanInspector</code> instance
     * for an array's component type.
     * @param array the array to be inspected.  Note that the declared type
     * of the array is inspected, not the elements of the array.
     * @return an instance of <code>BeanInpector</code> with the declared type
     * of <code>array</code> as the target class. 
     */
    public static BeanInspector inspect(Object[] array) {
        return BeanInspector.inspect(array.getClass().getComponentType());
    }
    
    /**
     * Static factory method that returns a <code>BeanInspector</code> instance
     * for an object. 
     * @param o an object to be inspected.  This argument may not be
     * <code>null</code>.
     * @return an instance of <code>BeanInspector</code> with <code>o</code>'s
     * class as the target class.
     */
    public static BeanInspector inspect(Object o) {
        return BeanInspector.inspect(o.getClass());
    }
    
    /**
     * Static factory method that returns an instance of BeanInspector for a
     * particular class.
     * @param cls a class to be inspected.  This argument may not be
     * <code>null</code>.
     * @return an instance of <code>BeanInspector</code> with <code>cls</code>
     * as the target class.
     */
    synchronized
    public static BeanInspector inspect(Class<?> cls) {
        BeanInspector inspector = _classCache.get(cls);
        if (inspector == null) {
            inspector = new BeanInspector(cls);
            _classCache.put(cls, inspector);
        }
        return inspector; 
    }
    
    /**
     * Returns property information for a given property.  If the name does
     * not correspond to a property of the class, <code>null</code> is 
     * returned.
     * @param name the name of a property.
     * @return a <code>BeanProperty</code> object correponding to the named
     * property, or <code>null</code> if the property is not present in the
     * target class.
     */
    public BeanProperty getProperty(String name) {
        return _properties.get(name);
    }
    
    /**
     * Returns property information for a given property.  If the name does
     * not correspond to a declared column name of the class, <code>null</code> is 
     * returned.
     * @param name the column name of a property.
     * @return a <code>BeanProperty</code> object correponding to the named
     * property, or <code>null</code> if the property is not present in the
     * target class.
     */
    public BeanProperty getPropertyByColumnName(String name) {
        return _columnProperties.get(name);
    }
    
    /**
     * Returns all properties for the class. 
     * @return an array of <code>BeanProperty</code> objects corresponding
     * to the properties of the target class.
     */
    public BeanProperty[] getProperties() {
        Collection<BeanProperty> temp = _properties.values();
        return temp.toArray(new BeanProperty[temp.size()]);
    }
    
    //
    // Implementation
    //
    private BeanInspector(Class<?> cls) {
        _properties = new HashMap<String, BeanProperty>();
        _columnProperties = new HashMap<String, BeanProperty>();

        Method[] methods = cls.getMethods();
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName();
            
            if (methods[i].getDeclaringClass().equals(Object.class))
                continue;
            if (Modifier.isStatic(methods[i].getModifiers()))
                continue;
            
            String columnName = null;
            int columnOrder = 9999;
            MocaColumn columnAnnotation = methods[i].getAnnotation(MocaColumn.class);
            if (columnAnnotation != null) {
                columnName = columnAnnotation.name();
                if (columnName != null && columnName.length() == 0) {
                    columnName = null;
                }
                columnOrder = columnAnnotation.order();
            }
            
            if (methods[i].getParameterTypes().length == 0) {
                if (methodName.startsWith("get") && !Character.isLowerCase(methodName.charAt(3))) {
                    String propName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                    BeanProperty property = _getOrCreateProperty(propName);
                    property.setGetMethod(methods[i]);
                    if (columnName != null) property.setColumnName(columnName);
                    if (columnOrder != 9999) property.setOrder(columnOrder);
                }
                else if (methodName.startsWith("is") && methods[i].getReturnType().equals(Boolean.TYPE)) {
                    String propName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
                    BeanProperty property = _getOrCreateProperty(propName);
                    property.setGetMethod(methods[i]);
                    if (columnName != null) property.setColumnName(columnName);
                    if (columnOrder != 9999) property.setOrder(columnOrder);
                }
            }
            else if (methods[i].getParameterTypes().length == 1 &&
                     methodName.startsWith("set") && !Character.isLowerCase(methodName.charAt(3))) {
                String propName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                BeanProperty property = _getOrCreateProperty(propName);
                property.setSetMethod(methods[i]);
                if (columnName != null) property.setColumnName(columnName);
                if (columnOrder != 9999) property.setOrder(columnOrder);
            }
        }
        
        for (BeanProperty property : _properties.values()) {
            _columnProperties.put(property.getColumnName(), property);
        }
    }
    
    private BeanProperty _getOrCreateProperty(String propName) {
        BeanProperty property = _properties.get(propName);
        if (property == null) {
            property = new BeanProperty(propName);
            _properties.put(property.getName(), property);
        }
        return property;
    }
    
    private final static HashMap<Class<?>, BeanInspector> _classCache = new HashMap<Class<?>, BeanInspector>();
    private final Map<String, BeanProperty> _properties;
    private final Map<String, BeanProperty> _columnProperties;
}
