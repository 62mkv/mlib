/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20167
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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import com.redprairie.moca.util.DateUtils;


public class MocaValue implements Serializable, Comparable<MocaValue> {
    // Set up a double formatter that has no decimal point if it's not needed
    // and the same 15 precision that legacy supported (340 is the max here)
    // DecimalFormat's are not thread safe, so clone when necessary
    private static DecimalFormat doubleFormatter = new DecimalFormat("0.#", new DecimalFormatSymbols(Locale.ROOT));
    static { doubleFormatter.setMaximumFractionDigits(15); }
    
    public MocaValue(MocaType type, Object value) {
        _type = type;
        _value = value;
    }
    
    public MocaType getType() {
        return _type;
    }
    
    public Object getValue() {
        return _value;
    }
    
    public boolean asBoolean() {
       
        // If the value is null, it becomes false.
        if (isNull()) return false;
        
        if (_type == MocaType.BOOLEAN) {
            return ((Boolean)_value).booleanValue();
        }
        
        // If the value is an integer, zero == false, all else == true
        if (_type == MocaType.INTEGER) {
            return ((Integer)_value).intValue() != 0;
        }
        
        // If the value is an double, zero == false, all else == true
        if (_type == MocaType.DOUBLE) {
            return ((Number)_value).doubleValue() != 0.0;
        }
        
        // Otherwise, non-null equals true;
        return true;
    }
    
    public String asString() {
        if (_type == MocaType.STRING) {
            return (String)_value;
        }
        
        if (isNull()) {
            return null;
        }
        else {
            if (_type == MocaType.DATETIME) {
                return DateUtils.formatDate((Date)_value);
            }
            else if (_type == MocaType.BOOLEAN) {
                return ((Boolean)_value).booleanValue() ? "1" : "0"; 
            }
            else if (_type == MocaType.DOUBLE) {
                return ((DecimalFormat)doubleFormatter.clone()).format(_value);
            }
            else {
                return String.valueOf(_value);
            }
        }
    }
    
    public int asInt() {
        if (isNull()) return 0;
        
        if (_value instanceof Number) {
            return ((Number)_value).intValue();
        }
        
        if (_value instanceof String) {
            return Double.valueOf((String)_value).intValue();
        }
        
        if (_value instanceof Boolean) {
            return (Boolean)_value ? 1 : 0;
        }
        
        return 0;
    }
    
    public double asDouble() {
        if (isNull()) return 0.0;
        
        if (_value instanceof Number) {
            return ((Number)_value).doubleValue();
        }
        
        if (_value instanceof String) {
            return Double.parseDouble((String)_value);
        }
        
        if (_value instanceof Boolean) {
            return (Boolean)_value ? 1.0 : 0.0;
        }
        
        return 0.0;
    }
    
    public Date asDate() {
        if (isNull()) return null;
        
        if (_value instanceof Date) {
            return (Date)_value;
        }
        
        if (_value instanceof String) {
            return DateUtils.parseDate((String)_value);
        }
        
        return null; // TODO throw type cast exception
    }
    
    public boolean isNull() {
        // If the value is null or we have an empty string then we consider it
        // null.  Also we don't want to trim the string since any character
        // would represent this as not null.
        if (_value == null || 
                (_value instanceof String && ((String)_value).isEmpty())) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Returns the value as an object of the passed type. If the type conversion
     * cannot occur, <code>null</code> is returned.  Otherwise, the standard type
     * conversion occurs.
     * @param type
     * @return
     */
    public Object asType(MocaType type) {
        if (type == _type || type == MocaType.OBJECT) {
            return _value;
        }
        
        else {
            switch (type) {
            case STRING:
                return asString();
            case BOOLEAN:
                return asBoolean();
            case DOUBLE:
                return asDouble();
            case INTEGER:
                return asInt();
            case DATETIME:
                return asDate();
            }
        }
        return null;
    }
    
    // @see java.lang.Object#hashCode()
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_type == null) ? 0 : _type.hashCode());
        result = prime * result + ((_value == null) ? 0 : _value.hashCode());
        return result;
    }

    // @see java.lang.Object#equals(java.lang.Object)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MocaValue other = (MocaValue) obj;
        
        if (this.isNull() && other.isNull()) {
            return true;
        }
        
        if (this._type == MocaType.STRING || other._type == MocaType.STRING) {
            return this.asSafeString().equals(other.asSafeString());
        }
        else if (this._type == MocaType.DOUBLE || other._type == MocaType.DOUBLE) {
            //This is to ensure precision when checking equality.
            return Double.doubleToLongBits(this.asDouble()) == Double
                .doubleToLongBits(other.asDouble());
        }        
        else if (this._type == MocaType.INTEGER || other._type == MocaType.INTEGER) {
            return this.asInt() == other.asInt();
        }
        else if (this._type == MocaType.BINARY && other._type == MocaType.BINARY) {
            return Arrays.equals((byte[])this._value, (byte[])other._value);
        }
        else {
            return this.asSafeString().equals(other.asSafeString());
        }
    }

    // @see java.lang.Comparable#compareTo(java.lang.Object)
    @Override
    public int compareTo(MocaValue right) {
        MocaValue left = this;

        if (left.isNull() && right.isNull()) {
            return 0;
        }
        
        if (left._type == MocaType.STRING || right._type == MocaType.STRING) {
            return left.asSafeString().compareTo(right.asSafeString());
        }
        else if (left._type == MocaType.DOUBLE || right._type == MocaType.DOUBLE) {
            double l = left.asDouble();
            double r = right.asDouble();
            return (l < r) ? -1 : ((l > r) ? 1 : 0);
        }
        else if (left._type == MocaType.INTEGER || right._type == MocaType.INTEGER) {
            return left.asInt() - right.asInt();
        }
        else {
            return left.asSafeString().compareTo(right.asSafeString());
        }
    }
    
    // @see java.lang.Object#toString()
    
    @Override
    public String toString() {
        return _value + "(" +_type + ")";
    }
    
    //
    // Implementation
    //
    
    private String asSafeString() {
        if (isNull()) {
            return "";
        }
        else {
            return asString();
        }
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        
        if (_value instanceof Serializable || _value instanceof Externalizable) {
            out.writeObject(_value);
        }
        else {
            out.writeObject(null);
        }
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        _value = in.readObject();
    }
    
    private static final long serialVersionUID = -732742411090773154L;
    private final MocaType _type;
    private transient Object _value;
}
