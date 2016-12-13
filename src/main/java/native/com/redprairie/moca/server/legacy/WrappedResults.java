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

package com.redprairie.moca.server.legacy;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.redprairie.moca.ColumnNotFoundException;
import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.util.ResultUtils;


/**
 * A class that wraps a C-accessible MOCA result set in a Java-accessable
 * class.  This class is used as an interface into C-MOCA result sets.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
public final class WrappedResults implements EditableResults
{
    /**
     * Constructor to be used by native code to provide a Java wrapper
     * around an existing C-MOCA result set pointer.
     * @param inRes an integer representation of a MOCA result set pointer.
     */
    public WrappedResults(int inRes, boolean needToDispose)
    {
        _internalRes = inRes;
        _needFirstRow = true;
        _needToDispose = needToDispose;
        _encoding = _getEncoding();
    }
    
    /**
     * Default constructor, to create a new results object with no rows or
     * columns.
     */
    public WrappedResults()
    {
        _internalRes = _newResults();
        _needFirstRow = true;
        _needToDispose = true;
        _encoding = _getEncoding();
        _allocateNulls = true;
    }
    
    /**
     * Constructs an instance of this class that enforces that null string 
     * columns are allocated as an empty string in addition to setting the
     * having the null indicator set. This is to support legacy behavior,
     * and some counter-intuitive behavior will result, such as
     * results.isNull(column) returning <code>true</code>, while
     * results.getValue(column) will NOT return <code>null</code>.
     * @param allocateNulls if <code>true</code>, null values from SQL queries
     * are handled in a way compatible with other DB access methods in MOCA.
     * Space for null values is allocated behind the scenes in the backing C
     * structures. This mode of operation is purely for backward compatibility
     * with existing C code and should not be used from Java applications.
     */
    public WrappedResults(boolean allocateNulls) {
        this();
        _allocateNulls = allocateNulls;
    }
    
    /**
     * Constructs an instance of this class from the data in another
     * <code>MocaResults</code> instance.
     * @param source the instance to use to populate this results object.  Each
     * row of source is accessed and the value of each column is copied into
     * the new result object.
     * @param allocateNulls if <code>true</code>, null values from SQL queries
     * are handled in a way compatible with other DB access methods in MOCA.
     * Space for null values is allocated behind the scenes in the backing C
     * structures. This mode of operation is purely for backward compatibility
     * with existing C code and should not be used from Java applications.
     */
    public WrappedResults(MocaResults source, boolean allocateNulls) {
        this(allocateNulls);
        ResultUtils.copyResults(this, source);
    }
    
    // javadoc inherited from interface
    public void reset() {
        _internalRow = 0;
        _needFirstRow = true;
    }

    // javadoc inherited from interface
    public boolean next() {
        int row; 
        if (_needFirstRow) {
            row = _firstRow(_internalRes);
            _needFirstRow = false;
        }
        else if (_nextRow != 0) {
            row = _nextRow;
        }
        else if (_internalRow != 0) {
            row = _nextRow(_internalRes, _internalRow);
        }
        else {
            row = 0;
        }
        
        // Keep the row and edit row in sync
        _internalRow = row;
        _editRow = row;
        _nextRow = 0;
        
        return (_internalRow != 0);
    }
    
    @Override
    public boolean hasNext() {
        if (_nextRow != 0) {
            return true;
        }
        else {
            return _hasNextRow(_internalRes, _internalRow, _needFirstRow);
        }
    }

    // javadoc inherited from interface
    public Object getValue(int index) {
        // Figure out the type of object we expect
        MocaType type = getColumnType(index);
        switch (type) {
        case INTEGER:
        case INTEGER_REF:
            if (_isNull(_internalRes, _internalRow, index))
                return null;
            else 
                return Integer.valueOf(_getIntValue(_internalRes, _internalRow, index));
            
        case DOUBLE:
        case DOUBLE_REF:
            if (_isNull(_internalRes, _internalRow, index))
                return null;
            else
                return Double.valueOf(_getDoubleValue(_internalRes, _internalRow, index));
            
        case BOOLEAN:
            if (_isNull(_internalRes, _internalRow, index))
                return null;
            else
                return Boolean.valueOf(_getBooleanValue(_internalRes, _internalRow, index));
        case STRING:
        case STRING_REF:
            return _getStringValue(_internalRes, _internalRow, index);
        case DATETIME:
            return _getDateValue(index);
        case BINARY:
            return _getBinaryValue(_internalRes, _internalRow, index);
        case OBJECT:
            return _getObjectValue(_internalRes, _internalRow, index);
        case RESULTS:
            return _getResultsValue(_internalRes, _internalRow, index);
        case GENERIC:
            return new GenericPointer(_getPointerValue(_internalRes, _internalRow, index));
        default:
            return null;
        }
    }

    // javadoc inherited from interface
    public Object getValue(String name) {
        return getValue(_getValidColumn(name));
    }

    // javadoc inherited from interface
    public String getString(int index) {
        Object value = getValue(index);
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String)value;
        }
        else {
            return value.toString();
        }
    }

    // javadoc inherited from interface
    public String getString(String name) {
        return getString(_getValidColumn(name));
    }

    // javadoc inherited from interface
    public int getInt(int index) {
        return _getIntValue(_internalRes, _internalRow, index);
    }

    // javadoc inherited from interface
    public int getInt(String name) {
        return getInt(_getValidColumn(name));
    }

    // javadoc inherited from interface
    public double getDouble(int index) {
        return _getDoubleValue(_internalRes, _internalRow, index);
    }

    // javadoc inherited from interface
    public double getDouble(String name) {
        return getDouble(_getValidColumn(name));
    }

    // javadoc inherited from interface
    public boolean getBoolean(int index) {
        return _getBooleanValue(_internalRes, _internalRow, index);
    }

    // javadoc inherited from interface
    public boolean getBoolean(String name) {
        return getBoolean(_getValidColumn(name));
    }

    // javadoc inherited from interface
    public Date getDateTime(int index) {
        return _getDateValue(index);
    }

    // javadoc inherited from interface
    public Date getDateTime(String name) {
        return getDateTime(_getValidColumn(name));
    }

    // javadoc inherited from interface
    public MocaResults getResults(int index) {
        return _getResultsValue(_internalRes, _internalRow, index);
    }

    // javadoc inherited from interface
    public MocaResults getResults(String name) {
        return getResults(_getValidColumn(name));
    }

    // javadoc inherited from interface
    public MocaType getColumnType(int index) {
        return MocaType.lookup(_getDataTypeCode(_internalRes, index));
    }

    // javadoc inherited from interface
    public MocaType getColumnType(String name) {
        return getColumnType(_getValidColumn(name));
    }

    public int getMaxLength(int index) {
        return _getDefinedMaxLength(_internalRes, index);
    }
    
    public int getMaxLength(String name) {
        return getMaxLength(_getValidColumn(name));
    }
    
    // javadoc inherited from interface
    public boolean isNullable(int index) {
        return _isNullable(_internalRes, index);
    }

    // javadoc inherited from interface
    public boolean isNullable(String name) {
        return isNullable(_getValidColumn(name));
    }

    // javadoc inherited from interface
    public String getColumnName(int index) {
        return _getColumnName(_internalRes, index);
    }

    // javadoc inherited from interface
    public int getColumnNumber(String name) {
        return _getColumnNum(_internalRes, name);
    }
    
    // javadoc inherited from interface
    public boolean containsColumn(String name) {
        int col = _getColumnNum(_internalRes, name);
        return (col >= 0); 
    }

    // javadoc inherited from interface
    public int getColumnCount() {
        return _getColumnCount(_internalRes);
    }
    
    public int getRowCount() {
        return _getRowCount(_internalRes);
    }
    
    public boolean isNull(int index) {
        return _isNull(_internalRes, _internalRow, index);
    }
    
    public boolean isNull(String name) {
        return _isNull(_internalRes, _internalRow, _getValidColumn(name));
    }
    
    /**
     * Add a new row to the current result set.
     */
    public void addRow() {
        _internalRow = 0;
        _needFirstRow = true;
        _editRow = _addRow(_internalRes);
    }
    
    
    public void removeRow() {
        if (_internalRow == 0) {
            throw new IllegalStateException("no current row");
        }

        int nextRow = _removeRow(_internalRes, _internalRow);
        
        _internalRow = 0;
        _editRow = 0;
        _nextRow = nextRow;
    }
    
    /**
     * Add a column to the current result set.
     * 
     * @param columnName the name of the column to add
     * @param type the <code>MocaType</code> of the column.
     */
    public void addColumn(String columnName, MocaType type) {
        _addColumn(_internalRes, columnName, type.getTypeCode(), 0, true);
    }
    
    /**
     * Add a column to the current result set.
     * @param columnName the name of the column to add
     * @param type the <code>MocaType</code> of the column.
     * @param length the defined maximum length of the column.  This argument
     * is ignored for some column types.  A length of zero implies an
     * unlimited length for strings.
     */
    public void addColumn(String columnName, MocaType type, int length) {
        _addColumn(_internalRes, columnName, type.getTypeCode(), length, true);
    }

    /**
     * Add a column to the current result set.
     * @param columnName the name of the column to add
     * @param type the <code>MocaType</code> of the column.
     * @param length the defined maximum length of the column.  This argument
     * is ignored for some column types.  A length of zero implies an
     * unlimited length for strings.
     */
    public void addColumn(String columnName, MocaType type, int length, boolean nullable) {
        _addColumn(_internalRes, columnName, type.getTypeCode(), length, nullable);
    }

    /**
     * Sets a string column value by name.
     * 
     * @param name the name of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setStringValue(String name, String value) {
        setStringValue(_getValidColumn(name), value);
    }
    
    /**
     * Sets a string column value by column number.
     * 
     * @param num the number of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setStringValue(int num, String value) {
        if (value == null) {
            _setNull(_internalRes, _editRow, num, _allocateNulls);
        }
        else {
            byte[] rawString;
            if (_encoding == null) {
                rawString = value.getBytes(Charset.defaultCharset());
            }
            else {
                try {
                    rawString = value.getBytes(_encoding);
                }
                catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException(e);
                }
            }
            _setString(_internalRes, _editRow, num, rawString);
        }
    }
    
    /**
     * Sets a integer column value by name.
     * 
     * @param name the name of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setIntValue(String name, int value) {
        _setInt(_internalRes, _editRow, _getValidColumn(name), value);
    }
    
    /**
     * Sets a integer column value by column number.
     * 
     * @param num the number of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setIntValue(int num, int value) {
        _setInt(_internalRes, _editRow, num, value);
    }
    
    /**
     * Sets a double column value by name.
     * 
     * @param name the name of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setDoubleValue(String name, double value) {
        _setDouble(_internalRes, _editRow, _getValidColumn(name), value);
    }
    
    /**
     * Sets a double column value by column number.
     * 
     * @param num the number of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setDoubleValue(int num, double value) {
        _setDouble(_internalRes, _editRow, num, value);
    }
    
    /**
     * Sets a date column value by name.
     * 
     * @param name the name of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setDateValue(String name, Date value) {
        setDateValue(_getValidColumn(name), value);
    }
    
    /**
     * Sets a date column value by column number.
     * @param num the number of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setDateValue(int num, Date value) {
        if (value != null) {
            SimpleDateFormat fmt = (SimpleDateFormat) DATE_FORMAT.clone();
            String tmp = fmt.format(value);
            byte[] rawString;
            if (_encoding == null) {
                rawString = tmp.getBytes(Charset.defaultCharset());
            }
            else {
                try {
                    rawString = tmp.getBytes(_encoding);
                }
                catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException(e);
                }
            }
            _setString(_internalRes, _editRow, num, rawString);
        }
        else {
            _setNull(_internalRes, _editRow, num, _allocateNulls);
        }
    }
    
    /**
     * Sets a binary column value by name.
     * 
     * @param name the name of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setBinaryValue(String name, byte[] value) {
        setBinaryValue(_getValidColumn(name), value);
    }
    
    /**
     * Sets a binary column value by column number.
     * 
     * @param num the number of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setBinaryValue(int num, byte[] value) {
        if (value == null) {
            _setNull(_internalRes, _editRow, num, _allocateNulls);
        }
        else {
            _setBinary(_internalRes, _editRow, num, value);
        }
    }
    
    /**
     * Sets a boolean column value by name.
     * 
     * @param name the name of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setBooleanValue(String name, boolean value) {
        _setBoolean(_internalRes, _editRow, _getValidColumn(name), value);
    }
    
    /**
     * Sets a boolean column value by column number.
     * @param num the number of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setBooleanValue(int num, boolean value) {
        _setBoolean(_internalRes, _editRow, num, value);
    }
    
    public void setResultsValue(String name, MocaResults value) {
        setResultsValue(_getValidColumn(name), value);
    }
    
    public void setResultsValue(int num, MocaResults value) {
        if (! (value instanceof WrappedResults)) {
            WrappedResults subRes = new WrappedResults(_newResults(), true);
            try {
                ResultUtils.copyResults(subRes, value);
                _setResults(_internalRes, _editRow, num, subRes);
            }
            finally {
                subRes.close();
            }
        }
        else {
            _setResults(_internalRes, _editRow, num, (WrappedResults)value);
        }
    }
    
    /**
     * Sets a column value by name.
     * 
     * @param name the name of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setValue(String name, Object value) {
        setValue(_getValidColumn(name), value);
    }
    
    /**
     * Sets a column value by column number.
     * 
     * @param num the number of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setValue(int num, Object value) {
        if (value == null) {
            _setNull(_internalRes, _editRow, num, _allocateNulls);
        }
        else if (value instanceof Date) {
            setDateValue(num, (Date)value);
        }
        else if (value instanceof Integer) {
            setIntValue(num, ((Integer)value).intValue());
        }
        else if (value instanceof Number) {
            setDoubleValue(num, ((Number)value).doubleValue());
        }
        else if (value instanceof String) {
            setStringValue(num, (String)value);
        }
        else if (value instanceof Boolean) {
            setBooleanValue(num, ((Boolean)value).booleanValue());
        }
        else if (value instanceof byte[]) {
            setBinaryValue(num, (byte[])value);
        }
        else if (value instanceof MocaResults) {
            setResultsValue(num, (MocaResults)value);
        }
        else if (value instanceof GenericPointer) {
            _setPointer(_internalRes, _editRow, num, ((GenericPointer) value).get32bitValue());
        }
        else {
            _setObjectValue(_internalRes, _editRow, num, value);
        }
    }
    

    // javadoc inherited from interface
    public void setNull(int num) {
        _setNull(_internalRes, _editRow, num, _allocateNulls);
    }
    
    public void setNull(String name) {
        _setNull(_internalRes, _editRow, _getValidColumn(name), _allocateNulls);
    }
    
    public void close() {
        int res = _internalRes;
        _internalRes = 0;
        _internalRow = 0;
        _nextRow = 0;

        if (_needToDispose && res != 0) {
            _dispose(res);
        }
    }
    
    //
    // Subclass Interface
    //
    
    /**
     * Clean up if the close method is never called.
     */
    protected void finalize()
    {
        int res = _internalRes;
        _internalRes = 0;
        _internalRow = 0;
        _nextRow = 0;

        if (_needToDispose && res != 0) {
            _dispose(res);
        }
    }

    @Override
    public void addRow(Map<String, Object> rowData) {
        
        // First, add the new row.  All column additions that are needed will be done 
        addRow();
        
        setValues(rowData);
    }

    @Override
    public RowIterator getRows() {
        // For the purposes of API completeness, WrappedResults returns itself as
        // iterator. Since this implementation is only concerned with compatibility
        // with legacy libraries, it's safe to ignore the need for a separate,
        // independent iterator.
        reset();
        return this;
    }
    
    @Override
    public void setValues(Map<String, Object> rowData) {
        for (Map.Entry<String, Object> i : rowData.entrySet()) {
            String name = i.getKey().toLowerCase();
            Object value = i.getValue();
            
            Integer column = _getColumnNum(_internalRes, name);
            if (column == null) {
                MocaType type = MocaType.forValue(value);
                addColumn(name, type);
            }
            
            setValue(name, value);
        }
    }

    // 
    // Implementation
    //
    private native int _newResults();
    private native String _getEncoding();
    private native void _dispose(int res);
    
    // Add a row to the current result set
    private native int _addRow(int res);
    private native void _addColumn(int res, String name, char typeCode, int maxLength, boolean isNullable);
    private native void _setObjectValue(int res, int row, int index, Object value);
    private native void _setInt(int res, int row, int index, int value);
    private native void _setDouble(int res, int row, int index, double value);
    private native void _setBinary(int res, int row, int index, byte[] value);
    private native void _setBoolean(int res, int row, int index, boolean value);
    private native void _setString(int res, int row, int index, byte[] value);
    private native void _setResults(int res, int row, int index, WrappedResults value);
    private native void _setPointer(int res, int row, int index, int value);
    private native void _setNull(int res, int row, int index, boolean allocate);

    private native int _firstRow(int res);
    private native int _nextRow(int res, int row);
    private native int _removeRow(int res, int row);
    
    private native boolean _hasNextRow(int res, int row, boolean needFirstRow);

    private native String _getStringValue(int res, int row, int index);
    private native int _getIntValue(int res, int row, int index);
    private native double _getDoubleValue(int res, int row, int index);
    private native boolean _getBooleanValue(int res, int row, int index);
    private native byte[] _getBinaryValue(int res, int row, int index);
    private native Object _getObjectValue(int res, int row, int index);
    private native WrappedResults _getResultsValue(int res, int row, int index);
    private native int _getPointerValue(int res, int row, int index);

    private Date _getDateValue(int index) {
        String dateString = _getStringValue(_internalRes, _internalRow, index);
        if (dateString == null) return null;
        SimpleDateFormat fmt = (SimpleDateFormat) DATE_FORMAT.clone();
        try {
            return fmt.parse(dateString);
        }
        catch (ParseException e) {
            return null;
        }
    }
    
    private int _getValidColumn(String name) {
        int column = _getColumnNum(_internalRes, name);
        
        if (column == -1) {
            throw new ColumnNotFoundException(name);
        }
        
        return column;
    }
    
    private native boolean _isNull(int res, int row, int index);

    private native char _getDataTypeCode(int res, int index);
    private native boolean _isNullable(int res, int index);

    private native int _getColumnNum(int res, String name);
    private native String _getColumnName(int res, int index);
    private native int _getColumnCount(int res);
    private native int _getRowCount(int res);
    private native int _getDefinedMaxLength(int res, int index);

    // JNI Support method.
    private static native void _initIDs();

    // Private Fields
    private boolean _needFirstRow;
    private boolean _needToDispose;
    private boolean _allocateNulls;
    private int _internalRes;
    private int _internalRow;
    private int _nextRow;
    private int _editRow;
    private String _encoding;
    
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    
    static
    {
        System.loadLibrary("MOCA");
        _initIDs();
    }

}
