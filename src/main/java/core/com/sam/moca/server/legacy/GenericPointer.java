/**
 * 
 */
package com.sam.moca.server.legacy;

import java.io.Serializable;

/**
 * Holder of "generic" pointer value.  This value represents a pointer in legacy code.  Pointers are, by their
 * nature, only valid within the process that allocated them.  Although this class represents a pointer value
 * that can exist across process boundaries, the pointer itself is only valid in the original process.
 * @author dinksett
 *
 */
public class GenericPointer implements Serializable {
    public GenericPointer(long value) {
        _pointerValue = value;
    }
    
    public GenericPointer(int value) {
        _pointerValue = (long) value;
    }
    
    /**
     * Gets the 64-bit (Java long) value for this pointer.
     * @return
     */
    public long getValue() {
        return _pointerValue;
    }
    
    /**
     * gets the 32-bit (java int) value for this pointer.
     * @return
     */
    public int get32bitValue() {
        return (int) (_pointerValue & 0xffffffffL);
    }
    
    @Override
    public String toString() {
        return Long.toHexString(_pointerValue);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof GenericPointer)) return false;
        return ((GenericPointer)obj)._pointerValue == _pointerValue;
    }
    
    @Override
    public int hashCode() {
        return (int)_pointerValue;
    }
    
    //
    // Implementation
    //
    private final long _pointerValue;
    private static final long serialVersionUID = -1271809622261833168L;
}
