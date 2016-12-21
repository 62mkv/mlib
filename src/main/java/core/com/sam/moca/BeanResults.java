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

package com.sam.moca;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sam.moca.util.ResultUtils;
import com.sam.util.ArgCheck;

/**
 * An implementation of <code>MocaResults</code> that is backed by an object
 * that has JavaBeans-style properties.  All the named properties of the 
 * object (or defined class) are used to produce the results.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 *
 * @author Derek Inksetter
 * @version $Revision$
 */
public class BeanResults<T> implements ModifiableResults {
    
    public class BeanIterator implements RowIterator {
        
        private BeanIterator() {
            _index = _beans.iterator();
        }
        
        // @see com.sam.moca.MocaResults#next()
        public boolean next() {
            boolean hasNext = _index.hasNext();
            
            if (hasNext) {
                _current = _index.next();
            }
            else {
                _current = null;
            }
            
            return (hasNext);
        }
        
        @Override
        public boolean hasNext() {
            return _index.hasNext();
        }

        // @see com.sam.moca.MocaResults.getValue(int)
        public Object getValue(int index) {
            if (getColumnType(index).equals(MocaType.RESULTS)) {
                return getResults(index);
            }
            else {
                return _getValueByIndex(index);
            }
        }
        
        // @see com.sam.moca.MocaResults.getValue(java.lang.String)
        public Object getValue(String name) {
            if (getColumnType(name).equals(MocaType.RESULTS)) {
                return getResults(name);
            }
            else {
                return _getValueByName(name);
            }
        }
        
        // @see com.sam.moca.MocaResults#getString(int)
        public String getString(int index) {
            Object value = _getValueByIndex(index); 
            
            if (value == null)
                return null;
            return String.valueOf(value);
        }

        // @see com.sam.moca.MocaResults#getString(java.lang.String)
        public String getString(String name) {
            Object value = _getValueByName(name);
            
            if (value == null)
                return null;
            return String.valueOf(value);
        }

        // @see com.sam.moca.MocaResults#getInt(int)
        public int getInt(int index) {
            Object value = _getValueByIndex(index); 
            if (value == null) {
                return 0;
            }
            return ((Number)value).intValue();
        }

        // @see com.sam.moca.MocaResults#getInt(java.lang.String)
        public int getInt(String name) {
            Object value = _getValueByName(name); 
            if (value == null) {
                return 0;
            }
            return ((Number)value).intValue();
        }

        // @see com.sam.moca.MocaResults#getDouble(int)
        public double getDouble(int index) {
            Object value = _getValueByIndex(index); 
            if (value == null) {
                return 0.0;
            }
            return ((Number)value).doubleValue();
        }

        // @see com.sam.moca.MocaResults#getDouble(java.lang.String)
        public double getDouble(String name) {
            Object value = _getValueByName(name); 
            if (value == null) {
                return 0.0;
            }
            return ((Number)value).doubleValue();
        }

        // @see com.sam.moca.MocaResults#getBoolean(int)
        public boolean getBoolean(int index) {
            Object value = _getValueByIndex(index);
            return _coerceToBoolean(value);
        }

        // @see com.sam.moca.MocaResults#getBoolean(java.lang.String)
        public boolean getBoolean(String name) {
            Object value = _getValueByName(name); 
            return _coerceToBoolean(value);
        }

        // @see com.sam.moca.MocaResults#getDateTime(int)
        public Date getDateTime(int index) {
            Object value = _getValueByIndex(index); 
            if (value == null) {
                return null;
            }
            return new Date(((Date)value).getTime());
        }

        // @see com.sam.moca.MocaResults#getDateTime(java.lang.String)
        public Date getDateTime(String index) {
            Object value = _getValueByName(index); 
            if (value == null) {
                return null;
            }
            return new Date(((Date)value).getTime());
        }
        
        // @see com.sam.moca.MocaResults#getResults(int)
        public MocaResults getResults(int index) {
            Object value = _getValueByIndex(index);

            Class<?> valueClass = value.getClass();
            if (valueClass.isArray()) {
                return new BeanResults<Object>((Object[])value);
            }
            else if (value instanceof Collection<?>) {
                return new BeanResults<Object>((Collection<?>)value);
            }
            else {
                return new BeanResults<Object>(value);
            }
        }
        
        // @see com.sam.moca.MocaResults#getResults(java.lang.String)
        public MocaResults getResults(String name) {
            Object value = _getValueByName(name); 

            Class<?> valueClass = value.getClass();
            if (valueClass.isArray()) {
                return new BeanResults<Object>((Object[])value);
            }
            else if (value instanceof Collection<?>) {
                return new BeanResults<Object>((Collection<?>)value);
            }
            else {
                return new BeanResults<Object>(value);
            }
        }
        
        // @see com.sam.moca.MocaResults#isNull(int)
        public boolean isNull(int index) {
            Object value = _getValueByIndex(index); 
            return (value == null);
        }
        
        // @see com.sam.moca.MocaResults#isNull(java.lang.String)
        public boolean isNull(String name) {
            Object value = _getValueByName(name); 
            return (value == null);
        }

        //
        // Implementation
        //
        private Object _getValueByIndex(int index) {
            if (_current == null) {
                throw new IllegalStateException("No current row");
            }
            
            PropertyDescriptor property = _properties[index];

            try {
                Method getter = property.getReadMethod();
                if (getter == null) {
                    throw new IllegalArgumentException("can't get property: no get method");
                }

                Object value = getter.invoke(_current);
                return value;
            }
            catch (IllegalAccessException e) {
                throw new IllegalArgumentException("can't get property: no access", e);
            }
            catch (InvocationTargetException e) {
                throw new IllegalArgumentException(
                    "can't get property: exception thrown: " + e.getCause(), e.getCause());
            } 
        }
        
        private Object _getValueByName(String name) {
            if (_current == null) {
                throw new IllegalStateException("No current row");
            }
            PropertyDescriptor property = _propertyMap.get(name.toLowerCase());
            
            if (property == null)
                throw new ColumnNotFoundException(name);

            try {
                Method getter = property.getReadMethod();
                if (getter == null) {
                    throw new IllegalArgumentException(
                        "can't get property " + name + ": no get method");
                }

                Object value = getter.invoke(_current);
                return value;
            }
            catch (IllegalAccessException e) {
                throw new IllegalArgumentException("can't get property " + name + ": no access", e);
            }
            catch (InvocationTargetException e) {
                throw new IllegalArgumentException(
                    "can't get property " + name + ": exception thrown: " + e.getCause(), e.getCause());
            } 
        }

        private void _removeRow() {
            _index.remove();
            _current = null;
        }

        private Iterator<T> _index;
        private Object _current;
    }
    
    /**
     * Create an instance of this class for a specific object.  The concrete
     * class of the object will be used to determine the published columns
     * of the results.  The result object will contain exactly one row.
     * 
     * @param bean the object to be inspected for its properties.  This argument
     * cannot be <code>null</code>.
     */
    public BeanResults(T bean) {
        this(bean.getClass());
        _beans.add(bean);
        _iterator = new BeanIterator();
    }
    
    /**
     * Create an instance of this class for an array of objects.  The declared
     * type of the array will be used to determine the published columns
     * of the results, regardless of the objects contained in the array.  The
     * array elements will be used to produce the rows.
     * 
     * @param beans an array containing objects to be used to back this result set.
     */
    public BeanResults(T[] beans) {
        this(beans.getClass().getComponentType());
        _beans = new ArrayList<T>(Arrays.asList(beans));
        _iterator = new BeanIterator();
    }

    /**
     * Create an instance of this class for a collection of objects.  The first
     * element of the collection will be used to determine the published coumns.
     * 
     * @param beans an array containing objects to be
     */
    public BeanResults(Collection<T> beans) {
        Iterator<T> i = beans.iterator();
        
        if (!i.hasNext()) {
            throw new IllegalArgumentException("unknown type--no values in collection");
        }
        
        _cls = i.next().getClass();
        
        BeanInfo info;
        try {
             info = Introspector.getBeanInfo(_cls, Object.class);
             _filterUnknown(info.getPropertyDescriptors());
        }
        catch (IntrospectionException e) {
            throw new IllegalArgumentException("Unable to inspect bean", e);
        }

        _beans = new ArrayList<T>(beans);
        _iterator = new BeanIterator();
    }

    /**
     * Create an instance of this class for a specific object.  The concrete
     * class of the object will be used to determine the published columns
     * of the results.  The result object will contain exactly one row.
     * 
     * @param bean the object to be inspected for its properties.  This argument
     * cannot be <code>null</code>.
     */
    public BeanResults(Class<?> cls) {
        _cls = cls;
        
        BeanInfo info;
        try {
             info = Introspector.getBeanInfo(_cls, Object.class);
             _filterUnknown(info.getPropertyDescriptors());
        }
        catch (IntrospectionException e) {
            throw new IllegalArgumentException("Unable to inspect bean", e);
        }
        
        _beans = new ArrayList<T>();
        _iterator = new BeanIterator();
    }
    
    public void addRows(Collection<T> data) {
        _beans.addAll(data);
        _editBean = null;
        _iterator = new BeanIterator();
    }
    
    // @see com.sam.moca.MocaResults#reset()
    public void reset() {
        _iterator = new BeanIterator();
    }
    
    
    public RowIterator getRows() {
        return new BeanIterator();
    }

    // @see com.sam.moca.MocaResults#getColumnType(int)
    public MocaType getColumnType(int index) {
        PropertyDescriptor property = _properties[index];
        Class<?> cls = property.getPropertyType();
        MocaType columnType = MocaType.lookupClass(cls);
        if (columnType.equals(MocaType.UNKNOWN)) {
            return MocaType.RESULTS;
        }
        else {
            return columnType;
        }
    }
    
    // @see com.sam.moca.MocaResults#getColumnType(java.lang.String)
    public MocaType getColumnType(String name) {
        PropertyDescriptor property = _propertyMap.get(name.toLowerCase());
        if (property == null) return null;
        Class<?> cls = property.getPropertyType();
        MocaType columnType = MocaType.lookupClass(cls);
        if (columnType.equals(MocaType.UNKNOWN)) {
            return MocaType.RESULTS;
        }
        else {
            return columnType;
        }
    }
    
    // @see com.sam.moca.MocaResults#getMaxLength(int)
    public int getMaxLength(int index) {
        return 0;
    }
    
    // @see com.sam.moca.MocaResults#getMaxLength(java.lang.String)
    public int getMaxLength(String name) {
        return 0;
    }

    // @see com.sam.moca.MocaResults#isNullable(int)
    public boolean isNullable(int index) {
        PropertyDescriptor property = _properties[index];
        Class<?> cls = property.getPropertyType();
        
        // We only consider primitive types to be not nullable
        if (cls.isPrimitive())
            return false;
        else
            return true;
    }

    // @see com.sam.moca.MocaResults#isNullable(java.lang.String)
    public boolean isNullable(String name) {
        PropertyDescriptor property = _propertyMap.get(name.toLowerCase());
        if (property == null)
            return false;
        
        Class<?> cls = property.getPropertyType();
        
        // We only consider primitive types to be not nullable
        if (cls.isPrimitive())
            return false;
        else
            return true;
    }

    // @see com.sam.moca.MocaResults#getColumnName(int)
    public String getColumnName(int index) {
        return _getColumnName(_properties[index]);
    }

    // @see com.sam.moca.MocaResults#getColumnNumber(java.lang.String)
    public int getColumnNumber(String name) {
        PropertyDescriptor desc = _propertyMap.get(name.toLowerCase());
        for (int i = 0; i < _properties.length; i++) {
            if (_properties[i] == desc) {
                return i;
            }
        }
        return -1;
    }
    
    public boolean containsColumn(String name) {
        return _propertyMap.containsKey(name.toLowerCase());
    }

    // @see com.sam.moca.MocaResults#getColumnCount()
    public int getColumnCount() {
        return _properties.length;
    }
    
    public int getRowCount() {
        return _beans.size();
    }
    
    // @see com.sam.moca.MocaResults#close()
    public void close() {
        // Do nothing...no native objects to release.
    }

    // @see com.sam.moca.EditableResults#addRow()
    @SuppressWarnings("unchecked")
    public void addRow() {
        T bean;
        try {
            bean = (T)_cls.newInstance();
        }
        catch (InstantiationException e) {
            throw new IllegalArgumentException("unable to add a row to this bean", e);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("unable to add a row to this bean", e);
        }
        
        _beans.add(bean);
        _editBean = bean;
        _iterator = new BeanIterator();
    }
    
    public void removeRow() {
        if (_iterator == null) {
            throw new IllegalStateException("No current row");
        }

        _iterator._removeRow();
        _editBean = null;
        _iterator._current = null;
    }

    // @see com.sam.moca.EditableResults#setBinaryValue(int, byte[])
    public void setBinaryValue(int num, byte[] value) {
        _setValueByIndex(num, value);
    }

    // @see com.sam.moca.EditableResults#setBinaryValue(java.lang.String, byte[])
    public void setBinaryValue(String name, byte[] value) {
        _setValueByName(name, value);
    }

    // @see com.sam.moca.EditableResults#setBooleanValue(int, boolean)
    public void setBooleanValue(int num, boolean value) {
        _setValueByIndex(num, Boolean.valueOf(value));
    }

    // @see com.sam.moca.EditableResults#setBooleanValue(java.lang.String, boolean)
    public void setBooleanValue(String name, boolean value) {
        _setValueByName(name, Boolean.valueOf(value));
    }

    // @see com.sam.moca.EditableResults#setDateValue(int, java.util.Date)
    public void setDateValue(int num, Date value) {
        _setValueByIndex(num, value);
    }

    // @see com.sam.moca.EditableResults#setDateValue(java.lang.String, java.util.Date)
    public void setDateValue(String name, Date value) {
        _setValueByName(name, value);
    }

    // @see com.sam.moca.EditableResults#setDoubleValue(int, double)
    public void setDoubleValue(int num, double value) {
        _setValueByIndex(num, Double.valueOf(value));
    }

    // @see com.sam.moca.EditableResults#setDoubleValue(java.lang.String, double)
    public void setDoubleValue(String name, double value) {
        _setValueByName(name, Double.valueOf(value));
    }

    // @see com.sam.moca.EditableResults#setIntValue(int, int)
    public void setIntValue(int num, int value) {
        _setValueByIndex(num, Integer.valueOf(value));
    }

    // @see com.sam.moca.EditableResults#setIntValue(java.lang.String, int)
    public void setIntValue(String name, int value) {
        _setValueByName(name, Integer.valueOf(value));
    }

    // @see com.sam.moca.EditableResults#setNull(int)
    public void setNull(int num) {
        _setValueByIndex(num, null);
    }

    // @see com.sam.moca.EditableResults#setNull(java.lang.String)
    public void setNull(String name) {
        _setValueByName(name, null);
    }

    // @see com.sam.moca.EditableResults#setStringValue(int, java.lang.String)
    public void setStringValue(int num, String value) {
        _setValueByIndex(num, value);
    }

    // @see com.sam.moca.EditableResults#setStringValue(java.lang.String, java.lang.String)
    public void setStringValue(String name, String value) {
        _setValueByName(name, value);
    }
    
    // @see com.sam.moca.ModifiableResults#setResultsValue(int, com.sam.moca.MocaResults)
    public void setResultsValue(int num, MocaResults value) {
        Object[] data;
        if (value instanceof BeanResults<?>) {
            BeanResults<?> sub = (BeanResults<?>) value;
            data = sub.getData();
        }
        else {
            Class<?> cls = _properties[num].getPropertyType();
            BeanResults<?> sub = new BeanResults<Object>(cls);
            ResultUtils.copyRows(sub, value);
            data = sub.getData();
        }
        _setValueByIndex(num, data);
    }
    
    // @see com.sam.moca.ModifiableResults#setResultsValue(java.lang.String, com.sam.moca.MocaResults)
    public void setResultsValue(String name, MocaResults value) {
        Object[] data;
        if (value instanceof BeanResults<?>) {
            BeanResults<?> sub = (BeanResults<?>) value;
            data = sub.getData();
        }
        else {
            Class<?> cls = _propertyMap.get(name.toLowerCase()).getPropertyType();
            BeanResults<?> sub = new BeanResults<Object>(cls);
            ResultUtils.copyRows(sub, value);
            data = sub.getData();
        }
        _setValueByName(name, data);
    }

    // @see com.sam.moca.EditableResults#setValue(int, java.lang.Object)
    public void setValue(int num, Object value) {
        if (value instanceof MocaResults) {
            setResultsValue(num, (MocaResults)value);
        }
        else {
            _setValueByIndex(num, value);
        }
    }

    // @see com.sam.moca.EditableResults#setValue(java.lang.String, java.lang.Object)
    public void setValue(String name, Object value) {
        if (value instanceof MocaResults) {
            setResultsValue(name, (MocaResults)value);
        }
        else {
            _setValueByName(name, value);
        }
    }
    
    /**
     * Get the Class object representing the type of the bean being
     * wrapped by this results object. 
     * @return the data class of this results object. 
     */
    public Class<?> getDataClass() {
        return _cls;
    }
    
    /**
     * Get the resulting array from this 
     * @return
     */
    @SuppressWarnings("unchecked")
    public T[] getData() {
        
        return _beans.toArray((T[])Array.newInstance(_cls, _beans.size()));
    }
    
    synchronized
    public String toString() {
        StringBuilder buf = new StringBuilder();
        int count = getColumnCount();
        buf.append("{ rows:" + count + " cols:{");
        
        for (int col = 0; col < count; col++) {
            if (col > 0) {
                buf.append(",");
            }
            buf.append("\"").append(getColumnName(col)).append("\":").append(getColumnType(col));
        }
        buf.append("}}");
        return buf.toString();
    }
    
    //
    // Iterator-based methods
    //
    
    // @see com.sam.moca.RowIterator#getBoolean(int)
    @Override
    public boolean getBoolean(int index) {
        return _iterator.getBoolean(index);
    }

    // @see com.sam.moca.RowIterator#getBoolean(java.lang.String)
    @Override
    public boolean getBoolean(String name) {
        return _iterator.getBoolean(name);
    }

    // @see com.sam.moca.RowIterator#getDateTime(int)
    @Override
    public Date getDateTime(int index) {
        return _iterator.getDateTime(index);
    }

    // @see com.sam.moca.RowIterator#getDateTime(java.lang.String)
    @Override
    public Date getDateTime(String name) {
        return _iterator.getDateTime(name);
    }

    // @see com.sam.moca.RowIterator#getDouble(int)
    @Override
    public double getDouble(int index) {
        return _iterator.getDouble(index);
    }

    // @see com.sam.moca.RowIterator#getDouble(java.lang.String)
    @Override
    public double getDouble(String name) {
        return _iterator.getDouble(name);
    }

    // @see com.sam.moca.RowIterator#getInt(int)
    @Override
    public int getInt(int index) {
        return _iterator.getInt(index);
    }

    // @see com.sam.moca.RowIterator#getInt(java.lang.String)
    @Override
    public int getInt(String name) {
        return _iterator.getInt(name);
    }

    // @see com.sam.moca.RowIterator#getResults(int)
    @Override
    public MocaResults getResults(int index) {
        return _iterator.getResults(index);
    }

    // @see com.sam.moca.RowIterator#getResults(java.lang.String)
    @Override
    public MocaResults getResults(String name) {
        return _iterator.getResults(name);
    }

    // @see com.sam.moca.RowIterator#getString(int)
    
    @Override
    public String getString(int index) {
        return _iterator.getString(index);
    }

    // @see com.sam.moca.RowIterator#getString(java.lang.String)
    @Override
    public String getString(String name) {
        return _iterator.getString(name);
    }

    // @see com.sam.moca.RowIterator#getValue(int)
    @Override
    public Object getValue(int index) {
        return _iterator.getValue(index);
    }

    // @see com.sam.moca.RowIterator#getValue(java.lang.String)
    @Override
    public Object getValue(String name) {
        return _iterator.getValue(name);
    }

    // @see com.sam.moca.RowIterator#isNull(int)
    @Override
    public boolean isNull(int index) {
        return _iterator.isNull(index);
    }

    // @see com.sam.moca.RowIterator#isNull(java.lang.String)
    @Override
    public boolean isNull(String name) {
        return _iterator.isNull(name);
    }

    // @see com.sam.moca.RowIterator#next()
    @Override
    public boolean next() {
        boolean nextResult = _iterator.next();
        _editBean = _iterator._current;
        return nextResult;

    }
    
    // @see com.sam.moca.RowIterator#hasNext()
    @Override
    public boolean hasNext() {
        return _iterator.hasNext();
    }
    
    
    /**
     * Sort the rows of this result set by the given column.  The sort
     * algorithm used (modified merge sort) is stable, meaning that rows with
     * equal values for the given column will be not be reordered as a
     * result of the sort.
     * 
     * @param column the name of the column to sort by.  If the given column
     * name does not identify a unique column, the first matching column will
     * be used for the comparison.
     */
    public BeanResults<T> sort(String column) {
        return sort(new String[] {column});
    }
    
    /**
     * Sort the rows of this result set by the given column.  The sort
     * algorithm used (modified merge sort) is stable, meaning that rows with
     * equal values for the given column will be not be reordered as a
     * result of the sort.
     * 
     * @param column the name of the column to sort by.  If the given column
     * name does not identify a unique column, the first matching column will
     * be used for the comparison.
     * @param comparator a comparator that customizes the way the named column's
     * value in a given row will be compared to other rows' values.  If this
     * parameter is <code>null</code>, the natural ordering of the objects will
     * be used.
     */
    public BeanResults<T> sort(String column, Comparator<?> comparator) {
        return sort(new String[] {column}, new Comparator[] {comparator});
    }
    
    /**
     * Sort the rows of this result set by the given columns.  The sort
     * algorithm used (modified merge sort) is stable, meaning that rows with
     * equal values for the given columns will be not be reordered as a
     * result of the sort.
     * 
     * @param columns the names of the columns to sort by.  If any of the
     * given column names does not identify a unique column, the first matching
     * column will be used for that part of the comparison. 
     */
    public BeanResults<T> sort(String[] columns) {
        return sort(columns, new Comparator[columns.length]);
    }
    
    /**
     * Sort the rows of this result set by the given columns.  The sort
     * algorithm used (modified merge sort) is stable, meaning that rows with
     * equal values for the given columns will be not be reordered as a
     * result of the sort.
     * 
     * @param columns the names of the columns to sort by.  If any of the
     * given column names does not identify a unique column, the first matching
     * column will be used for that part of the comparison. 
     * @param comparators a collection of comparators that customizes the way
     * the named columns' value in a given row will be compared to other rows'
     * values.  The comparators array must not be null, and must have the same
     * number of elements as the <code>columns</code> array.
     */
    public BeanResults<T> sort(String[] columns, Comparator<?>[] comparators) {
        ArgCheck.notNull(columns, "sort columns argument must not be null");
        ArgCheck.notNull(comparators, "comparators argument must not be null");
        ArgCheck.isTrue(comparators.length == columns.length,
                "columns and comparators must have equal lengths");
        
        List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        List<Comparator<?>> realComparators = new ArrayList<Comparator<?>>();
        
        for (int i = 0; i < columns.length; i++) {
            String name = columns[i];
            if (name.startsWith("-")) {
                name = name.substring(1);
                realComparators.add(Collections.reverseOrder(comparators[i]));
            }
            else {
                realComparators.add(comparators[i]);
            }
            PropertyDescriptor property = _propertyMap.get(name.toLowerCase());
            if (property == null)
                throw new ColumnNotFoundException(name);
            descriptors.add(property);
        }
        
        Collections.sort(_beans, new _PropertyComparator<T>(descriptors, realComparators));
        _iterator = new BeanIterator();

        return this;
    }
    
    //
    // Implementation
    //
    
    /**
     * A class that compares properties of two objects for the purposes of
     * sorting the objects.
     */
    private class _PropertyComparator<S> implements Comparator<S> {
        
        public _PropertyComparator(List<PropertyDescriptor> properties, List<Comparator<?>> comparators) {
            _properties = properties;
            _comparators = comparators;
        }
        
        // javadoc inherited from superclass/interface
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public int compare(S o1, S o2) {
            Iterator<Comparator<?>> i = _comparators.iterator();
            for (PropertyDescriptor desc : _properties) {
                Object field1 = null;
                Object field2 = null;
                try {
                    Method getMethod = desc.getReadMethod();
                    field1 = getMethod.invoke(o1);
                    field2 = getMethod.invoke(o2);
                }
                catch (IllegalAccessException e) {
                    // An exception reading a bean property means we treat the
                    // property as null
                }
                catch (InvocationTargetException e) {
                    // An exception reading a bean property means we treat the
                    // property as null
                }
                
                int result = 0;
                Comparator comparator = i.next();
                if (comparator != null) {
                    result = comparator.compare(field1, field2);
                }
                else if (field1 instanceof Comparable) {
                    result = ((Comparable)field1).compareTo(field2);
                }
                if (result != 0) return result;
            }
            return 0;
        }
        
        
        private final List<PropertyDescriptor> _properties;
        private final List<Comparator<?>> _comparators;
    }
    
    private void _filterUnknown(PropertyDescriptor[] in) {
        List<PropertyDescriptor> out = new ArrayList<PropertyDescriptor>();
        _propertyMap = new LinkedHashMap<String, PropertyDescriptor>();
        for (int i = 0; i < in.length; i++) {
            Class<?> type = in[i].getPropertyType();
            if (MocaType.lookupClass(type).equals(MocaType.UNKNOWN)) {
                if (type.isArray() && MocaType.lookupClass(type.getComponentType()).equals(MocaType.UNKNOWN)) {
                    continue;
                }
            }
            
            Method getter = in[i].getReadMethod();
            if (getter == null)
                continue;
            
            getter.setAccessible(true);
            
            out.add(in[i]);

            String columnName = _getColumnName(in[i]);
            _propertyMap.put(columnName.toLowerCase(), in[i]);
        }

        Collections.sort(out, new Comparator<PropertyDescriptor>(){
            public int compare(PropertyDescriptor o1, PropertyDescriptor o2) {
                return _getColumnOrder(o1) - _getColumnOrder(o2);
            }
        });
        
        _properties = out.toArray(new PropertyDescriptor[out.size()]);
    }
    
    private void _setValueByIndex(int index, Object value) {
        if (_editBean == null) {
            throw new IllegalStateException("no current row");
        }
        
        PropertyDescriptor property = _properties[index];
        try {
            Method setter = property.getWriteMethod();
            if (setter == null) {
                throw new IllegalArgumentException("can't set property: no set method");
            }

            setter.invoke(_editBean, value);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("can't set property: no access", e);
        }
        catch (InvocationTargetException e) {
            throw new IllegalArgumentException(
                "can't set property: exception thrown: " + e.getCause(), e.getCause());
        } 

    }
    
    private void _setValueByName(String name, Object value) {
        if (_editBean == null) {
            throw new IllegalStateException("no current row");
        }
        
        PropertyDescriptor property = _propertyMap.get(name.toLowerCase());
        
        if (property == null)
            throw new ColumnNotFoundException(name);

        try {
            Method setter = property.getWriteMethod();
            if (setter == null) {
                throw new IllegalArgumentException("can't set property " + name + ": no set method");
            }

            setter.invoke(_editBean, value);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("can't set property " + name + ": no access", e);
        }
        catch (InvocationTargetException e) {
            throw new IllegalArgumentException(
                "can't set property " + name + ": exception thrown: " + e.getCause(), e.getCause());
        } 
    }
    
    private boolean _coerceToBoolean(Object value) {
        if (value == null) {
            return false;
        }
        else if (value instanceof Boolean) {
            return ((Boolean)value).booleanValue();
        }
        else if (value instanceof Number) {
            return (((Number)value).intValue() != 0);
        }
        else if (value instanceof String) {
            return (Integer.parseInt((String)value) != 0);
        }
        else {
            return false;
        }
    }
    
    private String _getColumnName(PropertyDescriptor desc) {
        Method readMethod = desc.getReadMethod();
        String columnName = null;
        
        MocaColumn columnAnnotation = readMethod.getAnnotation(MocaColumn.class);
        if (columnAnnotation != null) {
            columnName = columnAnnotation.name();
        }

        if (columnName == null || columnName.length() == 0) {
            columnName = desc.getName();
        }
        
        return columnName;
    }

    private int _getColumnOrder(PropertyDescriptor desc) {
        Method readMethod = desc.getReadMethod();

        MocaColumn columnAnnotation = readMethod.getAnnotation(MocaColumn.class);
        if (columnAnnotation != null) {
            return columnAnnotation.order();
        }
        else {
            return 9999999;
        }
    }

    private List<T> _beans;
    private Object _editBean = null;
    private final Class<?> _cls;
    private PropertyDescriptor[] _properties;
    private Map<String, PropertyDescriptor> _propertyMap;
    private BeanIterator _iterator;
}
