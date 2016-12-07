/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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

package com.redprairie.moca;

import java.io.Serializable;


/**
 * Represents an argument to a MOCA command.  MOCA arguments contain a name, an operator, and
 * a value.  Since it's possible for an argument to be <code>null</code>, type information
 * is also available.
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MocaArgument implements Serializable {
    /**
     * Constructor that takes all information about an argument.
     * @param name The argument name.  This name is derived from the actual argument name sent to
     *     the MOCA command.
     * @param oper The argument operator.   
     * @param typeCode
     * @param value
     */
    public MocaArgument(String name, MocaOperator oper, MocaType type, Object value) {
        _name = name;
        _oper = oper;
        _value = new MocaValue(type, value);
    }
    
    public MocaArgument(String name, MocaType type, Object value) {
        this(name, MocaOperator.EQ, type, value);
    }
    
    public MocaArgument(String name, Object value) {
        this(name, MocaType.forValue(value), value);
    }
    
    /**
     * @return Returns the operator of this argument.
     */
    public MocaOperator getOper() {
        return _oper;
    }
    
    /**
     * @return Returns the argument name.
     */
    public String getName() {
        return _name;
    }
    
    /**
     * @return Returns the argument value.  The type of the object returned from this method
     *         depends on the type of the argument.
     */
    public Object getValue() {
        return _value.getValue();
    }
    
    /**
     * @return Returns the argument type.
     */
    public MocaType getType() {
        return _value.getType();
    }
    
    public MocaValue getDataValue() {
        return _value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MocaArgument)) return false;
        
        MocaArgument other = (MocaArgument) obj;
        if (!_name.equals(other._name)) return false;
        if (_oper != other._oper) return false;
        return _value.equals(other._value);
    }

    @Override
    public int hashCode() {
        int hash = _name.hashCode();
        hash = hash * 37 + _oper.hashCode();
        hash = hash * 37 + _value.hashCode();
        return hash;
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return "Arg: {name=" + _name +", oper=" + _oper + ", value=[" + _value + "]}";
    }
    
    //
    // Implementation
    //
    private final String _name;
    private final MocaOperator _oper;
    private final MocaValue _value;
    private static final long serialVersionUID = -8191585227718928236L;
}
