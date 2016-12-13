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

import java.sql.Types;
import java.util.Date;

import com.redprairie.moca.server.legacy.GenericPointer;

/**
 * Enumeration class to define the known data types that can be
 * a part of a MOCA result set. 
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
public enum MocaType
{
    // Order is important, for type promotion.
    // BOOLEAN < INTEGER < DOUBLE < DATETIME < STRING
    // All others are not significant.
    BOOLEAN('O', Boolean.class, Types.BOOLEAN),
    INTEGER('I', Integer.class, Types.INTEGER),
    INTEGER_REF('P', Integer.class, Types.INTEGER),
    DOUBLE('F', Double.class, Types.DOUBLE),
    DOUBLE_REF('X', Double.class, Types.DOUBLE),
    DATETIME('D', Date.class, Types.TIMESTAMP),
    STRING('S', String.class, Types.VARCHAR),
    STRING_REF('Z', String.class, Types.VARCHAR),
    BINARY('V', byte[].class, Types.BINARY),
    RESULTS('R', MocaResults.class, Types.OTHER),
    OBJECT('J', Object.class, Types.OTHER),
    GENERIC('G', GenericPointer.class, Types.OTHER),
    UNKNOWN('?', Object.class, Types.OTHER);
    
    /**
     * Returns the MOCA type code (COMTYP_*) corresponding to this type.
     * @return the MOCA type code for this type.
     */
    public char getTypeCode() {
    	return _code;
    }
    
    /**
     * Returns the Java class that represents this type.
     * @return the Java class that represents this type.
     */
    public Class<? extends Object> getValueClass() {
        return _valueClass;
    }
    
    /**
     * Returns the JDBC type of the given MOCA type.  This is mainly useful
     * for use with MOCA's BindList class.
     * @return the JDBC type code corresponding to this data type.
     */
    public int getSQLType() {
        return _sqlType;
    }
    
    /**
     * Returns the <code>MocaType</code> corresponding to the given low-level
     * MOCA type code.
     * @param code a MOCA type code (COMTYP_*).
     * @return the corresponding instance of this enumerated class, or
     * <code>UNKNOWN</code> if the type code is unrecognized.
     */
    public static MocaType lookup(char code) {
        switch (code) {
            case 'I': 
            case 'L': return INTEGER;
            case 'P': return INTEGER_REF;
            case 'F': return DOUBLE;
            case 'X': return DOUBLE_REF;
            case 'S': return STRING;
            case 'Z': return STRING_REF;
            case 'D': return DATETIME;
            case 'O': return BOOLEAN;
            case 'V': return BINARY;
            case 'R': return RESULTS;
            case 'J': return OBJECT;
            case 'G': return GENERIC;
            default: return UNKNOWN;
        }
    }
    
    /**
     * Determine what MOCA type is appropriate for the given class.  This
     * method will never return reference types, as they are indistinguishable
     * from their value counterparts.
     * @param cls the class to be used to look up a MOCA type object.
     * @return the <code>MocaType</code> object corresponding to the given
     * class.  If there is no known type for <code>cls</code>,
     * <code>UNKNOWN</code> is returned.
     */
    public static MocaType lookupClass(Class<?> cls) {
        if (cls.equals(String.class))
            return STRING;
        else if (cls.equals(Integer.class) || cls.equals(Integer.TYPE))
            return INTEGER;
        else if (cls.equals(Double.class) || cls.equals(Double.TYPE))
            return DOUBLE;
        else if (cls.equals(Boolean.class) || cls.equals(Boolean.TYPE))
            return BOOLEAN;
        else if (Date.class.isAssignableFrom(cls))
            return DATETIME;
        else if (cls.equals(GenericPointer.class))
            return GENERIC;
        else if (MocaResults.class.isAssignableFrom(cls))
            return RESULTS;
        else if (Number.class.isAssignableFrom(cls))
            return DOUBLE;
        else if (cls.isArray() && cls.getComponentType().equals(Byte.TYPE))
            return BINARY;
        else if (cls.equals(GenericPointer.class))
            return GENERIC;
        else if (!cls.isPrimitive() && !cls.isArray())
            return OBJECT;
        else
            return UNKNOWN;
    }
    
    public static MocaType forValue(Object value) {
        if (value == null) {
            return MocaType.STRING;
        }
        else {
            return lookupClass(value.getClass());
        }
    }
    
    //
    // Implementation
    //
    
    /**
     * Private constructor for data types.
     * @param code
     * @param cls
     */
    private MocaType(char code, Class<? extends Object> cls, int sqlType) {
        _code = code;
        _valueClass = cls;
        _sqlType = sqlType;
    }
    
    private static final long serialVersionUID = 3834305120656962609L;
    private final char _code;
    private final transient Class<? extends Object> _valueClass;
    private final transient int _sqlType;
}
