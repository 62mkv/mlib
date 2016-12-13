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

package com.redprairie.moca;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.redprairie.moca.util.ResultUtils;
import com.redprairie.util.ArgCheck;

/**
 * Editable MOCA results that is a native Java, high-performance implementation
 * of an in-memory result set object.
 * 
 * This class is not internally thread safe.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class SimpleResults implements EditableResults, Serializable {
    
    public class SimpleIterator implements RowIterator {
        
        private SimpleIterator() {
            _rowIter = null;
            _currentRow = null;
        }
        
        public boolean next() {
            if (_rowIter == null) {
                _rowIter = _dataRows.iterator();
            }
            
            if (_rowIter.hasNext()) {
                _currentRow = _rowIter.next();
            }
            else {
                _currentRow = null;
            }
            
            return (_currentRow != null);
        }
        
        // @see com.redprairie.moca.RowIterator#hasNext()
        @Override
        public boolean hasNext() {
            if (_rowIter == null) {
                _rowIter = _dataRows.iterator();
            }
            return _rowIter.hasNext();
        }
        
        /**
         * Sets the row, this is zero based
         * @param rownum The zero based row number
         */
        public void setRow(int rownum) {
            _rowIter = _dataRows.listIterator(rownum);
            if (_rowIter.hasNext()) {
                _currentRow =  _rowIter.next();
            }
            else {
                _currentRow = null;
            }
        }

        public String getString(int index) {
            Object value = _getData(index);
            if (value == null) {
                return null;
            }
            if (value instanceof String) {
                return (String)value;
            }
            
            return String.valueOf(value);
        }

        public String getString(String name) {
            return getString(_getValidColumn(name));
        }

        public int getInt(int index) {
            Object value = _getData(index);
            
            if (value == null) {
                return 0;
            }
            else if (value instanceof Number) {
                return ((Number)value).intValue();
            }
            else {
                return Integer.parseInt(String.valueOf(value));
            }
        }

        public int getInt(String name) {
            return getInt(_getValidColumn(name));
        }

        public double getDouble(int index) {
            Object value = _getData(index);
            
            if (value == null) {
                return 0;
            }
            else if (value instanceof Number) {
                return ((Number)value).doubleValue();
            }
            else {
                return Double.parseDouble(String.valueOf(value));
            }
        }

        public double getDouble(String name) {
            return getDouble(_getValidColumn(name));
        }

        public boolean getBoolean(int index) {
            Object value = _getData(index);
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

        public boolean getBoolean(String name) {
            return getBoolean(_getValidColumn(name));
        }

        public Date getDateTime(int index) {
            Object value = _getData(index);
            if (value instanceof Date) {
                return new Date(((Date)value).getTime());
            }
            return null;
        }

        public Date getDateTime(String name) {
            return getDateTime(_getValidColumn(name));
        }
        
        public MocaResults getResults(int index) {
            Object value = _getData(index);
            if (value instanceof MocaResults) {
                return (MocaResults)value;
            }
            else {
                return null;
            }
        }
        
        public MocaResults getResults(String name) {
            return getResults(_getValidColumn(name));
        }

        public Object getValue(int index) {
            return _getData(index);
        }

        public Object getValue(String name) {
            return getValue(_getValidColumn(name));
        }

        // @see com.redprairie.moca.MocaResults#isNull(int)
        public boolean isNull(int index) {
            return (_getData(index) == null);
        }

        // @see com.redprairie.moca.MocaResults#isNull(java.lang.String)
        public boolean isNull(String name) {
            return isNull(_getValidColumn(name));
        }


        private Object _getData(int index) {
            if (_currentRow == null) {
                throw new IllegalStateException("No current row");
            }
            
            if (index < 0 || index >= _currentRow.length) {
                return null;
            }
            
            return _currentRow[index];
        }
        

        public void removeRow() {
            if (_rowIter == null) {
                throw new IllegalStateException("No current row");
            }
            
            _rowIter.remove();
            
            _currentRow = null;
        }

        private Iterator<Object[]> _rowIter;
        private Object[] _currentRow;
    }
    

    public void addColumn(String columnName, MocaType type) {
        addColumn(columnName, type, 0);
    }

    public void addColumn(String columnName, MocaType type, int length) {
        addColumn(columnName, type, length, true);
    }
    
    public void addColumn(String columnName, MocaType type, int length, boolean nullable) {
        // Set up new column
        _ColumnData newColumn = new _ColumnData();
        newColumn._name = columnName;
        newColumn._type = type;
        newColumn._nullable = nullable;
        newColumn._maxLen = length;
        
        // Add that column into our list
        int pos = _metadata.size();
        _metadata.add(newColumn);
        
        // Put the column name into our hashtable
        String lowerName = columnName.toLowerCase();
        if (_columns.get(lowerName) == null) {
            _columns.put(lowerName, Integer.valueOf(pos));
        }
        
        // Add a new column to each row
        for (ListIterator<Object[]> i = _dataRows.listIterator(); i.hasNext();) {
            Object[] row = i.next();
            Object[] newRow = new Object[row.length + 1];
            System.arraycopy(row, 0, newRow, 0, row.length);
            i.set(newRow);

            if (_editRow == row) {
                _editRow = newRow;
            }
        }
    }
    
    /**
     * Default constructor for SimpleResults.
     */
    public SimpleResults() {
        _iterator = new SimpleIterator(); 
    }

    public void addRow() {
        Object[] rowData = new Object[_metadata.size()];
        _dataRows.add(rowData);
        _editRow = rowData;
        _iterator = new SimpleIterator();
    }

    public void addRow(Map<String, Object> rowData) {
        // First, add the new row. 
        addRow();
        
        // Then, set the values for the new row.  All column additions will be done here.
        setValues(rowData);
    }
    
    @Override
    public void setValues(Map<String, Object> rowData) {
        for (Map.Entry<String, Object> i : rowData.entrySet()) {
            String name = i.getKey().toLowerCase();
            Object value = i.getValue();
            
            Integer column = _columns.get(name);
            if (column == null) {
                MocaType type = MocaType.forValue(value);
                addColumn(name, type);
            }
            
            setValue(name, value);
        }
    }

    public void removeRow() {
        _iterator.removeRow();
        _editRow = _iterator._currentRow;
    }
    
    public void setBinaryValue(int num, byte[] value) {
        _setData(num, value, MocaType.BINARY);
    }

    public void setBinaryValue(String name, byte[] value) {
        setBinaryValue(_getValidColumn(name), value);
    }

    public void setBooleanValue(int num, boolean value) {
        _setData(num, Boolean.valueOf(value), MocaType.BOOLEAN);
    }

    public void setBooleanValue(String name, boolean value) {
        setBooleanValue(_getValidColumn(name), value);
    }

    public void setDateValue(int num, Date value) {
        _setData(num, value, MocaType.DATETIME);
    }

    public void setDateValue(String name, Date value) {
        setDateValue(_getValidColumn(name), value);
    }

    public void setDoubleValue(int num, double value) {
        _setData(num, Double.valueOf(value), MocaType.DOUBLE);
    }

    public void setDoubleValue(String name, double value) {
        setDoubleValue(_getValidColumn(name), value);
    }

    public void setIntValue(int num, int value) {
        _setData(num, Integer.valueOf(value), MocaType.INTEGER);
    }

    public void setIntValue(String name, int value) {
        setIntValue(_getValidColumn(name), value);
    }

    public void setNull(int num) {
        _setData(num, null, null);
    }
    
    public void setNull(String name) {
        setNull(_getValidColumn(name));
    }

    public void setStringValue(int num, String value) {
        _setData(num, value, MocaType.STRING);
    }

    public void setStringValue(String name, String value) {
        setStringValue(_getValidColumn(name), value);
    }
    
    public void setResultsValue(int num, MocaResults value) {
        if (!(value instanceof SimpleResults || value instanceof BeanResults<?>)) {
            SimpleResults subRes = new SimpleResults();
            ResultUtils.copyResults(subRes, value);
            _setData(num, subRes, MocaType.RESULTS);
        }
        else {
            _setData(num, value, MocaType.RESULTS);
        }
    }
    
    public void setResultsValue(String name, MocaResults value) {
        setResultsValue(_getValidColumn(name), value);
    }

    public void setValue(int num, Object value) {
        if (value instanceof MocaResults) {
            setResultsValue(num, (MocaResults) value);
        }
        else {
            _setData(num, value, null);
        }
    }

    public void setValue(String name, Object value) {
        setValue(_getValidColumn(name), value);
    }

    public void reset() {
        _iterator = new SimpleIterator();
        _editRow = null;
    }

    public MocaType getColumnType(int index) {
        _ColumnData meta = _getMetadata(index);
        return meta._type;
    }

    public MocaType getColumnType(String name) {
        _ColumnData meta = _getMetadata(name);
        return meta._type;
    }
    
    public int getMaxLength(int index) {
        _ColumnData meta = _getMetadata(index);
        return meta._maxLen;
    }

    public int getMaxLength(String name) {
        _ColumnData meta = _getMetadata(name);
        return meta._maxLen;
    }

    public boolean isNullable(int index) {
        _ColumnData meta = _getMetadata(index);
        return meta._nullable;
    }

    public boolean isNullable(String name) {
        _ColumnData meta = _getMetadata(name);
        return meta._nullable;
    }

    public String getColumnName(int index) {
        _ColumnData meta = _getMetadata(index);
        return meta._name;
    }

    public int getColumnNumber(String name) {
        Integer index = (Integer) _columns.get(name.toLowerCase());
        if (index == null)
            return -1;
        else
            return index.intValue();
    }
    
    public boolean containsColumn(String name) {
        Integer index = (Integer) _columns.get(name.toLowerCase());
        return (index != null);
    }
    
    public int getColumnCount() {
        return _metadata.size();
    }
    
    public int getRowCount() {
        return _dataRows.size();
    }
    
    public void close() {
        // Do nothing...no native objects to release.
    }
    
    // @see com.redprairie.moca.MocaResults#getRows()
    @Override
    public RowIterator getRows() {
        return new SimpleIterator();
    }
    
    //
    // Iterator-based methods
    //
    
    // @see com.redprairie.moca.RowIterator#getBoolean(int)
    @Override
    public boolean getBoolean(int index) {
        return _iterator.getBoolean(index);
    }

    // @see com.redprairie.moca.RowIterator#getBoolean(java.lang.String)
    @Override
    public boolean getBoolean(String name) {
        return _iterator.getBoolean(name);
    }

    // @see com.redprairie.moca.RowIterator#getDateTime(int)
    @Override
    public Date getDateTime(int index) {
        return _iterator.getDateTime(index);
    }

    // @see com.redprairie.moca.RowIterator#getDateTime(java.lang.String)
    @Override
    public Date getDateTime(String name) {
        return _iterator.getDateTime(name);
    }

    // @see com.redprairie.moca.RowIterator#getDouble(int)
    @Override
    public double getDouble(int index) {
        return _iterator.getDouble(index);
    }

    // @see com.redprairie.moca.RowIterator#getDouble(java.lang.String)
    @Override
    public double getDouble(String name) {
        return _iterator.getDouble(name);
    }

    // @see com.redprairie.moca.RowIterator#getInt(int)
    @Override
    public int getInt(int index) {
        return _iterator.getInt(index);
    }

    // @see com.redprairie.moca.RowIterator#getInt(java.lang.String)
    @Override
    public int getInt(String name) {
        return _iterator.getInt(name);
    }

    // @see com.redprairie.moca.RowIterator#getResults(int)
    @Override
    public MocaResults getResults(int index) {
        return _iterator.getResults(index);
    }

    // @see com.redprairie.moca.RowIterator#getResults(java.lang.String)
    @Override
    public MocaResults getResults(String name) {
        return _iterator.getResults(name);
    }

    // @see com.redprairie.moca.RowIterator#getString(int)
    
    @Override
    public String getString(int index) {
        return _iterator.getString(index);
    }

    // @see com.redprairie.moca.RowIterator#getString(java.lang.String)
    @Override
    public String getString(String name) {
        return _iterator.getString(name);
    }

    // @see com.redprairie.moca.RowIterator#getValue(int)
    @Override
    public Object getValue(int index) {
        return _iterator.getValue(index);
    }

    // @see com.redprairie.moca.RowIterator#getValue(java.lang.String)
    @Override
    public Object getValue(String name) {
        return _iterator.getValue(name);
    }

    // @see com.redprairie.moca.RowIterator#isNull(int)
    @Override
    public boolean isNull(int index) {
        return _iterator.isNull(index);
    }

    // @see com.redprairie.moca.RowIterator#isNull(java.lang.String)
    @Override
    public boolean isNull(String name) {
        return _iterator.isNull(name);
    }

    // @see com.redprairie.moca.RowIterator#next()
    @Override
    public boolean next() {
        boolean nextResult = _iterator.next();
        _editRow = _iterator._currentRow;
        return nextResult;
    }
    
    // @see com.redprairie.moca.RowIterator#hasNext()
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
    public SimpleResults sort(String column) {
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
    public SimpleResults sort(String column, Comparator<?> comparator) {
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
    public SimpleResults sort(String[] columns) {
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
    public SimpleResults sort(String[] columns, Comparator<?>[] comparators) {
        ArgCheck.notNull(columns, "sort columns argument must not be null");
        ArgCheck.notNull(comparators, "comparators argument must not be null");
        ArgCheck.isTrue(comparators.length == columns.length,
                "columns and comparators must have equal lengths");
        String[] realColumns = new String[columns.length];
        Comparator<?>[] realComparators = new Comparator[columns.length];
        
        for (int i = 0; i < columns.length; i++) {
            String name = columns[i];
            if (name.startsWith("-")) {
                realColumns[i] = name.substring(1);
                realComparators[i] = Collections.reverseOrder(comparators[i]);
            }
            else {
                realColumns[i] = name;
                realComparators[i] = comparators[i];
            }
        }

        
        Collections.sort(_dataRows, new _ColumnComparator(_columns, 
                realColumns, realComparators));
        _iterator = new SimpleIterator();
        return this;
    }
    
    /**
     * Sets the current row for the row iterator associated with this result set.
     * @param rownum The zero based row number to move to
     */
    public void setRow(int rownum) {
        _iterator.setRow(rownum);
        _editRow = _iterator._currentRow;
    }
    
    /**
     * Converts the data type of a single column. This method preserves the
     * ordering of existing columns, and converts a column to another data type.
     * If a conversion to the target type is not possible, the column's value
     * becomes <code>null</code>.
     * 
     * @param name the name of the column to convert. If there are more than one
     *            column with the given name, the conversion applies to the
     *            first column with that name.
     * @param type The desired type for the column.
     */
    public void promoteColumn(String name, MocaType type) {
        promoteColumn(_getValidColumn(name), type);
    }
    
    /**
     * Converts the data type of a single column. This method preserves the
     * ordering of existing columns, and converts a column to another data type.
     * If a conversion to the target type is not possible, the column's value
     * becomes <code>null</code>.
     * 
     * @param num the index of the column to convert.
     * @param type The desired type for the column.
     */
    public void promoteColumn(int num, MocaType type) {
        _ColumnData colData = _getMetadata(num);
        MocaType oldType = colData._type;
        if (oldType == type) return;
        
        // Change the data for each row
        for (ListIterator<Object[]> i = _dataRows.listIterator(); i.hasNext();) {
            Object[] row = i.next();
            row[num] = new MocaValue(colData._type, row[num]).asType(type);
        }
        
        colData._type = type;
    }

    //
    // Implementation
    //
    private static final long serialVersionUID = -4417669509371329444L;

    private static class _ColumnData implements Serializable {
        private int _maxLen;
        private String _name;
        private boolean _nullable;
        private MocaType _type;
        
        private static final long serialVersionUID = -2497997713315497016L;
    }
    
    private static class _ColumnComparator implements Comparator<Object[]> {
        public _ColumnComparator(Map<String, Integer> columns, 
                String[] columnNames, Comparator<?>[] comparators) {
            _columns = new int[columnNames.length];
            _comparators = (Comparator[])comparators.clone();
            for (int i = 0; i < columnNames.length; i++) {
                _columns[i] = _getValidColumn(columns, columnNames[i]);
            }
        }
        
        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public int compare(Object[] rowA, Object[] rowB) {
            for (int i = 0; i < _columns.length; i++) {
                Object objA = rowA[_columns[i]];
                Object objB = rowB[_columns[i]];
                
                if (objA == null && objB == null) continue;
                if (objA == null && objB != null) return -1;
                if (objA != null && objB == null) return 1;

                int result = 0;
                if (_comparators[i] != null) {
                    result = _comparators[i].compare(objA, objB);
                }
                else if (objA instanceof Comparable && objB instanceof Comparable) {
                    result = (((Comparable)objA).compareTo(objB));
                }
                if (result != 0) return result;
            }
            return 0;
        }
        
        private final int[] _columns;

        @SuppressWarnings("rawtypes")
        private final Comparator[] _comparators;
    }
    
    private _ColumnData _getMetadata(String name) {
        Integer index = (Integer) _columns.get(name.toLowerCase());
        if (index == null)
            throw new ColumnNotFoundException(name);
        return (_ColumnData)_metadata.get(index.intValue());
    }
    
    private _ColumnData _getMetadata(int index) {
        if (index < 0 || index >= _metadata.size())
            throw new ColumnNotFoundException(index);
        return (_ColumnData)_metadata.get(index);
    }
    
    private void _setData(int index, Object value, MocaType valueType) {
        if (_editRow == null) {
            throw new IllegalStateException("No current row");
        }
        
        if (index < 0 || index >= _editRow.length) {
            throw new IllegalArgumentException("Invalid column index");
        }
        
        // Ensure that the passed-in value is compatible with the declared
        // column type.
        if (value != null) {
            if (valueType == null) {
                valueType = MocaType.lookupClass(value.getClass());
            }
            MocaType assignedType = ((_ColumnData)_metadata.get(index))._type;
            if (!valueType.equals(assignedType)) {
                throw new IllegalArgumentException("type mismatch: Expected " + assignedType + ", got " + valueType);
            }
        }
        
        _editRow[index] = value;
    }

    private int _getValidColumn(String name) {
        return _getValidColumn(_columns, name);
    }
    
    private static int _getValidColumn(Map<String, Integer> columns, String name) {
        Integer index = (Integer) columns.get(name.toLowerCase());
        if (index == null)
            throw new ColumnNotFoundException(name);
        else
            return index.intValue();
    }
    
    // Override readObject behavior, so that we always have an iterator.
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        _iterator = new SimpleIterator();
        _columns = new HashMap<String, Integer>();
        int pos = 0;
        for (_ColumnData col : _metadata) {
            if (_columns.get(col._name) == null) {
                _columns.put(col._name, pos);
            }
            pos++;
        }

    }
    
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{ rows:" + _dataRows.size() + " cols:{");
        int colNum = 0;
        for (_ColumnData col : _metadata) {
            if (colNum++ > 0) {
                buf.append(",");
            }
            buf.append("\"").append(col._name).append("\":").append(col._type);
        }
        buf.append("}}");
        return buf.toString();
    }

    
    private List<Object[]> _dataRows = new ArrayList<Object[]>();
    private List<_ColumnData> _metadata = new ArrayList<_ColumnData>();
    private transient SimpleIterator _iterator = null;
    private transient Object[] _editRow = null;
    private transient Map<String, Integer> _columns = new HashMap<String, Integer>();
}
